-- Создание таблицы переводов
CREATE TABLE transfers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_card_id BIGINT NOT NULL COMMENT 'ID карты отправителя',
    to_card_id BIGINT NOT NULL COMMENT 'ID карты получателя',
    amount DECIMAL(19,2) NOT NULL COMMENT 'Сумма перевода',
    description VARCHAR(500) COMMENT 'Описание перевода',
    status ENUM('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT 'Статус перевода',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата создания',
    processed_at TIMESTAMP NULL COMMENT 'Дата обработки',
    error_message VARCHAR(1000) COMMENT 'Сообщение об ошибке',
    
    FOREIGN KEY (from_card_id) REFERENCES bank_cards(id) ON DELETE CASCADE,
    FOREIGN KEY (to_card_id) REFERENCES bank_cards(id) ON DELETE CASCADE,
    INDEX idx_from_card (from_card_id),
    INDEX idx_to_card (to_card_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_amount (amount)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Переводы между картами';
