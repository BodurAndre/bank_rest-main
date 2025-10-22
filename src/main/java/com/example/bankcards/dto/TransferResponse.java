package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для ответа перевода
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    
    private Long id;
    private Long fromCardId;
    private String fromCardMasked;
    private Long toCardId;
    private String toCardMasked;
    private BigDecimal amount;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private String errorMessage;
}
