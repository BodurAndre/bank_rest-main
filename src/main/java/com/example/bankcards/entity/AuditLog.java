package com.example.bankcards.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Сущность для аудита действий пользователей
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(length = 1000)
    private String description;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 50)
    private String status; // SUCCESS, FAILED, ERROR

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    // Конструкторы
    public AuditLog() {
        this.createdAt = LocalDateTime.now();
        this.status = "SUCCESS";
    }

    public AuditLog(User user, String action, String entityType, Long entityId, String description) {
        this();
        this.user = user;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.description = description;
    }

    // Типы действий
    public static class Actions {
        public static final String LOGIN = "LOGIN";
        public static final String LOGOUT = "LOGOUT";
        public static final String CREATE_CARD = "CREATE_CARD";
        public static final String BLOCK_CARD = "BLOCK_CARD";
        public static final String ACTIVATE_CARD = "ACTIVATE_CARD";
        public static final String DELETE_CARD = "DELETE_CARD";
        public static final String TOPUP_CARD = "TOPUP_CARD";
        public static final String TRANSFER = "TRANSFER";
        public static final String VIEW_CARDS = "VIEW_CARDS";
        public static final String VIEW_TRANSFERS = "VIEW_TRANSFERS";
        public static final String EXPORT_DATA = "EXPORT_DATA";
        public static final String CREATE_USER = "CREATE_USER";
        public static final String UPDATE_USER = "UPDATE_USER";
        public static final String DELETE_USER = "DELETE_USER";
        public static final String REQUEST_CARD_BLOCK = "REQUEST_CARD_BLOCK";
        public static final String REQUEST_CARD_TOPUP = "REQUEST_CARD_TOPUP";
        public static final String REQUEST_CARD_UNBLOCK = "REQUEST_CARD_UNBLOCK";
        public static final String REQUEST_CARD_CREATE = "REQUEST_CARD_CREATE";
        public static final String REQUEST_CARD_RECREATE = "REQUEST_CARD_RECREATE";
    }

    // Типы сущностей
    public static class EntityTypes {
        public static final String CARD = "CARD";
        public static final String TRANSFER = "TRANSFER";
        public static final String USER = "USER";
        public static final String NOTIFICATION = "NOTIFICATION";
        public static final String SYSTEM = "SYSTEM";
    }

    // Статусы
    public static class Status {
        public static final String SUCCESS = "SUCCESS";
        public static final String FAILED = "FAILED";
        public static final String ERROR = "ERROR";
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

