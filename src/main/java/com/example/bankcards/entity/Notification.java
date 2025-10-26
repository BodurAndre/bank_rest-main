package com.example.bankcards.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Сущность уведомления
 */
@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = true)
    private BankCard card;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private Type type;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, length = 1000)
    private String message;
    
    @Column
    private Double amount;
    
    @Column
    private String newExpiryDate; // Для запросов на пересоздание карты (формат MM/YY)
    
    @Column(nullable = false)
    private Boolean isRead = false;
    
    @Column(nullable = false)
    private Boolean isProcessed = false;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime readAt;
    
    @Column
    private LocalDateTime processedAt;
    
    // Конструкторы
    public Notification() {}
    
    public Notification(User user, BankCard card, Type type, String title, String message) {
        this.user = user;
        this.card = card;
        this.type = type;
        this.title = title;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
    
    public Notification(User user, BankCard card, Type type, String title, String message, Double amount) {
        this.user = user;
        this.card = card;
        this.type = type;
        this.title = title;
        this.message = message;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }
    
    // Типы уведомлений
    public enum Type {
        CARD_BLOCK_REQUEST("Запрос на блокировку карты"),
        CARD_TOPUP_REQUEST("Запрос на пополнение карты"),
        CARD_UNBLOCK_REQUEST("Запрос на разблокировку карты"),
        CARD_CREATE_REQUEST("Запрос на создание карты"),
        CARD_RECREATE_REQUEST("Запрос на пересоздание карты"), // New type
        CARD_ACTIVATED("Карта активирована"),
        CARD_BLOCKED("Карта заблокирована"),
        TRANSFER_COMPLETED("Перевод выполнен");
        
        private final String description;
        
        Type(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Методы
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
    
    public void markAsProcessed() {
        this.isProcessed = true;
        this.processedAt = LocalDateTime.now();
    }
    
    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public BankCard getCard() {
        return card;
    }
    
    public void setCard(BankCard card) {
        this.card = card;
    }
    
    public Type getType() {
        return type;
    }
    
    public void setType(Type type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getReadAt() {
        return readAt;
    }
    
    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
    
    public Boolean getIsProcessed() {
        return isProcessed;
    }
    
    public void setIsProcessed(Boolean isProcessed) {
        this.isProcessed = isProcessed;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public String getNewExpiryDate() {
        return newExpiryDate;
    }
    
    public void setNewExpiryDate(String newExpiryDate) {
        this.newExpiryDate = newExpiryDate;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
