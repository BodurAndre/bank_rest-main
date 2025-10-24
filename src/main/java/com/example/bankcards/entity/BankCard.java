package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Сущность банковской карты
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bank_cards")
public class BankCard {
    
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", nullable = false, unique = true)
    private String cardNumber; // Зашифрованный номер карты

    @Column(name = "masked_number", nullable = false)
    private String maskedNumber; // Маскированный номер для отображения (**** **** **** 1234)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(name = "expiry_date", nullable = false)
    @DateTimeFormat(pattern = "MM/yy")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;
    
    @Column(name = "block_request_sent")
    private Boolean blockRequestSent = false;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

    @Column(name = "block_reason")
    private String blockReason;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Проверяет, активна ли карта
     */
    public boolean isActive() {
        return status == Status.ACTIVE && !isExpired();
    }

    /**
     * Проверяет, истек ли срок действия карты
     */
    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Проверяет, можно ли использовать карту для операций
     */
    public boolean canBeUsed() {
        return isActive() && balance.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Блокирует карту
     */
    public void block(String reason) {
        this.status = Status.BLOCKED;
        this.blockedAt = LocalDateTime.now();
        this.blockReason = reason;
    }

    /**
     * Активирует карту
     */
    public void activate() {
        this.status = Status.ACTIVE;
        this.blockedAt = null;
        this.blockReason = null;
    }

    /**
     * Статусы карты
     */
    public enum Status {
        ACTIVE,     // Активна
        BLOCKED,    // Заблокирована
        EXPIRED     // Истек срок
    }

    /**
     * Конструктор для создания новой карты
     */
    public BankCard(String cardNumber, String maskedNumber, User owner, LocalDate expiryDate) {
        this.cardNumber = cardNumber;
        this.maskedNumber = maskedNumber;
        this.owner = owner;
        this.expiryDate = expiryDate;
        this.status = Status.ACTIVE;
        this.balance = BigDecimal.ZERO;
    }
    
    public Boolean getBlockRequestSent() {
        return blockRequestSent;
    }
    
    public void setBlockRequestSent(Boolean blockRequestSent) {
        this.blockRequestSent = blockRequestSent;
    }
}
