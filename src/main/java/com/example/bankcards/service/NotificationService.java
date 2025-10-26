package com.example.bankcards.service;

import com.example.bankcards.entity.Notification;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.NotificationRepository;
import com.example.bankcards.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для работы с уведомлениями
 */
@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private ValidationUtils validationUtils;
    
    /**
     * Создает уведомление о запросе на блокировку карты
     */
    @Transactional
    public Notification createCardBlockRequest(User user, BankCard card, String reason) {
        String title = "Запрос на блокировку карты";
        String message = String.format(
            "Пользователь %s %s запросил блокировку карты %s. Причина: %s",
            user.getFirstName(),
            user.getLastName(),
            card.getMaskedNumber(),
            reason
        );
        
        Notification notification = new Notification(user, card, Notification.Type.CARD_BLOCK_REQUEST, title, message);
        return notificationRepository.save(notification);
    }
    
    /**
     * Создает уведомление о запросе на пополнение карты
     */
    @Transactional
    public Notification createCardTopupRequest(User user, BankCard card, Double amount) {
        String title = "Запрос на пополнение карты";
        String message = String.format(
            "Пользователь %s %s запросил пополнение карты %s на сумму %.2f руб.",
            user.getFirstName(),
            user.getLastName(),
            card.getMaskedNumber(),
            amount
        );
        
        Notification notification = new Notification(user, card, Notification.Type.CARD_TOPUP_REQUEST, title, message, amount);
        return notificationRepository.save(notification);
    }
    
    /**
     * Создает уведомление о запросе на разблокировку карты
     */
    @Transactional
    public Notification createCardUnblockRequest(User user, BankCard card, String reason) {
        String title = "Запрос на разблокировку карты #" + card.getMaskedNumber();
        String message = String.format(
            "Пользователь %s %s запросил разблокировку карты %s. Причина: %s",
            user.getFirstName(),
            user.getLastName(),
            card.getMaskedNumber(),
            reason
        );
        
        System.out.println("Creating unblock request notification:");
        System.out.println("Type: " + Notification.Type.CARD_UNBLOCK_REQUEST);
        System.out.println("Type name: " + Notification.Type.CARD_UNBLOCK_REQUEST.name());
        System.out.println("Type length: " + Notification.Type.CARD_UNBLOCK_REQUEST.name().length());
        
        Notification notification = new Notification(user, card, Notification.Type.CARD_UNBLOCK_REQUEST, title, message);
        return notificationRepository.save(notification);
    }
    
    /**
     * Создает уведомление о запросе на создание карты
     */
    @Transactional
    public Notification createCardCreateRequest(User user, String expiryDate) {
        String title = "Запрос на создание новой карты";
        String message = String.format(
            "Пользователь %s %s запросил создание новой банковской карты. Срок действия: %s",
            user.getFirstName(),
            user.getLastName(),
            expiryDate
        );
        
        // Создаем уведомление без привязки к конкретной карте (карта еще не создана)
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setCard(null); // Карта будет создана позже
        notification.setType(Notification.Type.CARD_CREATE_REQUEST);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }
    
    /**
     * Создает уведомление об активации карты
     */
    @Transactional
    public Notification createCardActivatedNotification(User user, BankCard card) {
        String title = "Карта активирована";
        String message = String.format(
            "Ваша карта %s была активирована администратором",
            card.getMaskedNumber()
        );
        
        Notification notification = new Notification(user, card, Notification.Type.CARD_ACTIVATED, title, message);
        return notificationRepository.save(notification);
    }
    
    /**
     * Создает уведомление о блокировке карты
     */
    @Transactional
    public Notification createCardBlockedNotification(User user, BankCard card, String reason) {
        String title = "Карта заблокирована";
        String message = String.format(
            "Ваша карта %s была заблокирована. Причина: %s",
            card.getMaskedNumber(),
            reason
        );
        
        Notification notification = new Notification(user, card, Notification.Type.CARD_BLOCKED, title, message);
        return notificationRepository.save(notification);
    }
    
    /**
     * Создает уведомление о выполненном переводе
     */
    @Transactional
    public Notification createTransferCompletedNotification(User user, BankCard fromCard, BankCard toCard, Double amount) {
        String title = "Перевод выполнен";
        String message = String.format(
            "Перевод с карты %s на карту %s на сумму %.2f руб. выполнен успешно",
            fromCard.getMaskedNumber(),
            toCard.getMaskedNumber(),
            amount
        );
        
        Notification notification = new Notification(user, fromCard, Notification.Type.TRANSFER_COMPLETED, title, message);
        return notificationRepository.save(notification);
    }
    
    /**
     * Получает уведомления для пользователя с пагинацией
     */
    public Page<Notification> getUserNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
    
    /**
     * Получает непрочитанные уведомления для пользователя
     */
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }
    
    /**
     * Подсчитывает количество непрочитанных уведомлений для пользователя
     */
    public Long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }
    
    /**
     * Получает все уведомления для админов
     */
    public Page<Notification> getAllNotificationsForAdmins(Pageable pageable) {
        return notificationRepository.findAllForAdmins(pageable);
    }
    
    /**
     * Отмечает уведомление как прочитанное
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Уведомление не найдено"));
        
        notification.markAsRead();
        notificationRepository.save(notification);
    }
    
    /**
     * Отмечает уведомление как обработанное
     */
    @Transactional
    public void markAsProcessed(Long notificationId) {
        // Валидация входных данных
        validationUtils.validateId(notificationId, "уведомления");
        
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Уведомление", notificationId));
        
        notification.markAsProcessed();
        notificationRepository.save(notification);
    }
    
    /**
     * Отмечает все уведомления пользователя как прочитанные
     */
    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        
        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
        }
        
        notificationRepository.saveAll(unreadNotifications);
    }
    
    /**
     * Удаляет уведомление
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        // Валидация входных данных
        validationUtils.validateId(notificationId, "уведомления");
        
        if (!notificationRepository.existsById(notificationId)) {
            throw new ResourceNotFoundException("Уведомление", notificationId);
        }
        
        notificationRepository.deleteById(notificationId);
    }
    
    /**
     * Получает уведомления с фильтрацией для админов
     */
    public Page<Notification> getNotificationsWithFilters(String search, Boolean processed, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return notificationRepository.searchNotificationsWithFilters(search, processed, pageable);
        } else {
            return notificationRepository.findByProcessedStatus(processed, pageable);
        }
    }
}
