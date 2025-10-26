package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.dto.TransferStats;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Сервис для работы с переводами
 */
@Service
@Transactional
public class TransferService {

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private BankCardRepository bankCardRepository;
    
    @Autowired
    private ValidationUtils validationUtils;

    /**
     * Выполняет перевод между картами
     */
    public TransferResponse transfer(TransferRequest request, User user) {
        // Валидация входных данных
        validationUtils.validateId(request.getFromCardId(), "карты отправителя");
        validationUtils.validateId(request.getToCardId(), "карты получателя");
        validationUtils.validateMinAmount(request.getAmount(), new BigDecimal("0.01"));
        validationUtils.validateDescription(request.getDescription(), "Описание перевода");
        
        // Находим карты
        BankCard fromCard = bankCardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Карта отправителя", request.getFromCardId()));

        BankCard toCard = bankCardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Карта получателя", request.getToCardId()));

        // Проверяем, что обе карты принадлежат пользователю
        if (!fromCard.getOwner().getId().equals(user.getId()) || 
            !toCard.getOwner().getId().equals(user.getId())) {
            throw new BusinessException("Нет доступа к одной из карт", "ACCESS_DENIED");
        }

        // Проверяем, что карты разные
        if (fromCard.getId().equals(toCard.getId())) {
            throw new ValidationException("Нельзя переводить на ту же карту");
        }

        // Проверяем, что карты активны
        if (!fromCard.canBeUsed()) {
            throw new CardBlockedException(fromCard.getMaskedNumber(), "Карта отправителя заблокирована");
        }
        
        if (!toCard.canBeUsed()) {
            throw new CardBlockedException(toCard.getMaskedNumber(), "Карта получателя заблокирована");
        }

        // Проверяем баланс
        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(fromCard.getBalance(), request.getAmount());
        }

        // Создаем перевод
        Transfer transfer = new Transfer(fromCard, toCard, request.getAmount(), request.getDescription());
        transfer = transferRepository.save(transfer);

        try {
            // Выполняем перевод
            fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
            toCard.setBalance(toCard.getBalance().add(request.getAmount()));

            bankCardRepository.save(fromCard);
            bankCardRepository.save(toCard);

            // Обновляем статус перевода
            transfer.setStatus(Transfer.Status.COMPLETED);
            transfer.setProcessedAt(LocalDateTime.now());
            transfer = transferRepository.save(transfer);

            return createTransferResponse(transfer);

        } catch (Exception e) {
            // В случае ошибки отменяем перевод
            transfer.setStatus(Transfer.Status.FAILED);
            transfer.setErrorMessage(e.getMessage());
            transferRepository.save(transfer);

            throw new RuntimeException("Ошибка при выполнении перевода: " + e.getMessage());
        }
    }

    /**
     * Получает историю переводов пользователя
     */
    @Transactional(readOnly = true)
    public Page<TransferResponse> getTransferHistory(User user, Pageable pageable) {
        return transferRepository.findByUser(user, pageable)
                .map(this::createTransferResponse);
    }

    /**
     * Находит перевод по ID
     */
    @Transactional(readOnly = true)
    public Optional<TransferResponse> findById(Long id, User user) {
        return transferRepository.findById(id)
                .filter(transfer -> 
                    transfer.getFromCard().getOwner().getId().equals(user.getId()) ||
                    transfer.getToCard().getOwner().getId().equals(user.getId()))
                .map(this::createTransferResponse);
    }

    /**
     * Получает статистику переводов пользователя
     */
    @Transactional(readOnly = true)
    public TransferStats getTransferStats(User user) {
        long totalTransfers = transferRepository.countByUser(user);
        double totalAmount = transferRepository.getTotalAmountByUser(user);
        double averageAmount = totalTransfers > 0 ? totalAmount / totalTransfers : 0;

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long transfersThisMonth = transferRepository.countByUserThisMonth(user, startOfMonth);
        double amountThisMonth = transferRepository.getAmountByUserThisMonth(user, startOfMonth);

        return new TransferStats(
                totalTransfers, totalAmount, averageAmount, 
                transfersThisMonth, amountThisMonth
        );
    }

    /**
     * Создает DTO ответа из сущности перевода
     */
    private TransferResponse createTransferResponse(Transfer transfer) {
        TransferResponse response = new TransferResponse();
        response.setId(transfer.getId());
        response.setFromCardId(transfer.getFromCard().getId());
        response.setFromCardMasked(transfer.getFromCard().getMaskedNumber());
        response.setToCardId(transfer.getToCard().getId());
        response.setToCardMasked(transfer.getToCard().getMaskedNumber());
        response.setAmount(transfer.getAmount());
        response.setDescription(transfer.getDescription());
        response.setStatus(transfer.getStatus().name());
        response.setCreatedAt(transfer.getCreatedAt());
        response.setErrorMessage(transfer.getErrorMessage());
        return response;
    }
}
