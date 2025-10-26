package com.example.bankcards.service;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.CreateBankCardRequest;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardEncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с банковскими картами
 */
@Service
@Transactional
public class BankCardService {

    @Autowired
    private BankCardRepository bankCardRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CardEncryptionUtil cardEncryptionUtil;

    /**
     * Создает новую банковскую карту (только для админа)
     */
    public BankCardDto createCard(CreateBankCardRequest request) {
        // Находим пользователя
        User owner = userRepository.findByEmail(request.getOwnerEmail())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        // Генерируем номер карты
        String cardNumber = generateCardNumber();
        
        // Проверяем валидность номера по алгоритму Луна
        if (!cardEncryptionUtil.isValidCardNumber(cardNumber)) {
            // Если номер невалидный, генерируем новый
            cardNumber = generateValidCardNumber();
        }
        
        // Шифруем номер карты
        String encryptedCardNumber = cardEncryptionUtil.encryptCardNumber(cardNumber);
        
        // Генерируем маскированный номер
        String maskedNumber = cardEncryptionUtil.getMaskedNumberFromEncrypted(encryptedCardNumber);

        // Проверяем уникальность зашифрованного номера
        if (bankCardRepository.existsByCardNumber(encryptedCardNumber)) {
            throw new IllegalArgumentException("Карта с таким номером уже существует");
        }

        // Создаем карту с зашифрованным номером
        BankCard bankCard = new BankCard(
                encryptedCardNumber,
                maskedNumber,
                owner,
                request.getExpiryDateAsLocalDate()
        );

        BankCard savedCard = bankCardRepository.save(bankCard);
        return BankCardDto.fromEntity(savedCard);
    }

    /**
     * Находит карту по ID
     */
    @Transactional(readOnly = true)
    public Optional<BankCardDto> findById(Long id) {
        return bankCardRepository.findById(id)
                .map(BankCardDto::fromEntity);
    }

    /**
     * Находит все карты пользователя
     */
    @Transactional(readOnly = true)
    public Page<BankCardDto> findByOwner(User owner, Pageable pageable) {
        return bankCardRepository.findByOwner(owner, pageable)
                .map(BankCardDto::fromEntity);
    }

    /**
     * Находит все карты пользователя с фильтрацией
     */
    @Transactional(readOnly = true)
    public Page<BankCardDto> findByOwnerWithFilters(User owner, BankCard.Status status, String searchTerm, Pageable pageable) {
        return bankCardRepository.findByOwnerWithFilters(owner, status, searchTerm, pageable)
                .map(BankCardDto::fromEntity);
    }

    /**
     * Находит все карты с фильтрацией (для админа)
     */
    @Transactional(readOnly = true)
    public Page<BankCardDto> findAllWithFilters(BankCard.Status status, String searchTerm, Pageable pageable) {
        return bankCardRepository.findAllWithFilters(status, searchTerm, pageable)
                .map(BankCardDto::fromEntity);
    }

    /**
     * Блокирует карту
     */
    public BankCardDto blockCard(Long cardId, String reason) {
        BankCard card = bankCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Карта не найдена"));

        if (card.getStatus() == BankCard.Status.BLOCKED) {
            throw new IllegalArgumentException("Карта уже заблокирована");
        }

        card.block(reason);
        BankCard savedCard = bankCardRepository.save(card);
        return BankCardDto.fromEntity(savedCard);
    }

    /**
     * Активирует карту
     */
    @Transactional
    public BankCardDto activateCard(Long cardId) {
        try {
            BankCard card = bankCardRepository.findById(cardId)
                    .orElseThrow(() -> new IllegalArgumentException("Карта не найдена"));

            if (card.getStatus() == BankCard.Status.ACTIVE) {
                throw new IllegalArgumentException("Карта уже активна");
            }

            card.activate();
            BankCard savedCard = bankCardRepository.save(card);
            
            // Принудительно загружаем owner для избежания lazy loading проблем
            savedCard.getOwner().getEmail();
            
            return BankCardDto.fromEntity(savedCard);
        } catch (Exception e) {
            System.err.println("Error in activateCard service: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Простая активация карты без возврата DTO
     */
    @Transactional
    public void activateCardSimple(Long cardId) {
        try {
            BankCard card = bankCardRepository.findById(cardId)
                    .orElseThrow(() -> new IllegalArgumentException("Карта не найдена"));

            if (card.getStatus() == BankCard.Status.ACTIVE) {
                throw new IllegalArgumentException("Карта уже активна");
            }

            card.activate();
            bankCardRepository.save(card);
        } catch (Exception e) {
            System.err.println("Error in activateCardSimple service: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Удаляет карту (только для админа)
     */
    public void deleteCard(Long cardId) {
        if (!bankCardRepository.existsById(cardId)) {
            throw new IllegalArgumentException("Карта не найдена");
        }
        bankCardRepository.deleteById(cardId);
    }

    /**
     * Находит активные карты пользователя для переводов
     */
    @Transactional(readOnly = true)
    public List<BankCardDto> findActiveCardsForUser(User user) {
        return bankCardRepository.findActiveCardsForUser(user)
                .stream()
                .map(BankCardDto::fromEntity)
                .toList();
    }

    /**
     * Обновляет статус истекших карт
     */
    public void updateExpiredCards() {
        List<BankCard> expiredCards = bankCardRepository.findExpiredCards();
        for (BankCard card : expiredCards) {
            card.setStatus(BankCard.Status.EXPIRED);
        }
        bankCardRepository.saveAll(expiredCards);
    }

    /**
     * Генерирует номер карты (16 цифр)
     */
    private String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            cardNumber.append((int) (Math.random() * 10));
        }
        return cardNumber.toString();
    }
    
    /**
     * Генерирует валидный номер карты по алгоритму Луна
     */
    private String generateValidCardNumber() {
        String cardNumber;
        int attempts = 0;
        int maxAttempts = 100;
        
        do {
            cardNumber = generateCardNumber();
            attempts++;
        } while (!cardEncryptionUtil.isValidCardNumber(cardNumber) && attempts < maxAttempts);
        
        if (attempts >= maxAttempts) {
            // Если не удалось сгенерировать валидный номер, используем фиксированный
            cardNumber = "4532015112830366"; // Валидный номер для тестирования
        }
        
        return cardNumber;
    }

    /**
     * Проверяет, может ли пользователь управлять картой
     */
    public boolean canUserManageCard(User user, Long cardId) {
        return bankCardRepository.findById(cardId)
                .map(card -> card.getOwner().getId().equals(user.getId()))
                .orElse(false);
    }

    /**
     * Пополнение карты
     */
    public void topupCard(Long cardId, Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма пополнения должна быть больше 0");
        }
        
        BankCard card = bankCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Карта не найдена"));
        
        if (card.getStatus() != BankCard.Status.ACTIVE) {
            throw new IllegalArgumentException("Нельзя пополнить неактивную карту");
        }
        
        BigDecimal currentBalance = card.getBalance();
        BigDecimal topupAmount = BigDecimal.valueOf(amount);
        card.setBalance(currentBalance.add(topupAmount));
        
        bankCardRepository.save(card);
    }
    
    /**
     * Получение сущности карты по ID
     */
    public Optional<BankCard> getCardEntityById(Long id) {
        return bankCardRepository.findById(id);
    }
    
    /**
     * Сохранение карты
     */
    @Transactional
    public BankCard saveCard(BankCard card) {
        return bankCardRepository.save(card);
    }
}
