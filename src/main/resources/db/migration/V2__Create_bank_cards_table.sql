-- Создание таблицы банковских карт
CREATE TABLE bank_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_number VARCHAR(255) NOT NULL UNIQUE COMMENT 'Зашифрованный номер карты',
    masked_number VARCHAR(255) NOT NULL COMMENT 'Маскированный номер для отображения',
    user_id BIGINT NOT NULL COMMENT 'ID владельца карты',
    expiry_date DATE NOT NULL COMMENT 'Срок действия карты',
    status ENUM('ACTIVE', 'BLOCKED', 'EXPIRED') NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус карты',
    balance DECIMAL(19,2) NOT NULL DEFAULT 0.00 COMMENT 'Баланс карты',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата создания',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Дата обновления',
    blocked_at TIMESTAMP NULL COMMENT 'Дата блокировки',
    block_reason VARCHAR(500) NULL COMMENT 'Причина блокировки',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_expiry_date (expiry_date),
    INDEX idx_created_at (created_at),
    INDEX idx_card_number (card_number),
    INDEX idx_masked_number (masked_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Банковские карты';
