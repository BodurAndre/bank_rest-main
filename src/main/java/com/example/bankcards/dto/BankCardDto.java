package com.example.bankcards.dto;

import com.example.bankcards.entity.BankCard;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO для банковской карты
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankCardDto {
    
    private Long id;
    private String maskedNumber;
    private String ownerEmail;
    private String ownerName;
    private LocalDate expiryDate;
    private BankCard.Status status;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime blockedAt;
    private String blockReason;
    private Boolean blockRequestSent;
    private boolean isExpired;
    private boolean canBeUsed;

    /**
     * Создает DTO из сущности BankCard
     */
    public static BankCardDto fromEntity(BankCard bankCard) {
        BankCardDto dto = new BankCardDto();
        dto.setId(bankCard.getId());
        dto.setMaskedNumber(bankCard.getMaskedNumber());
        dto.setOwnerEmail(bankCard.getOwner().getEmail());
        dto.setOwnerName(bankCard.getOwner().getFirstName() + " " + bankCard.getOwner().getLastName());
        dto.setExpiryDate(bankCard.getExpiryDate());
        dto.setStatus(bankCard.getStatus());
        dto.setBalance(bankCard.getBalance());
        dto.setCreatedAt(bankCard.getCreatedAt());
        dto.setUpdatedAt(bankCard.getUpdatedAt());
        dto.setBlockedAt(bankCard.getBlockedAt());
        dto.setBlockReason(bankCard.getBlockReason());
        dto.setBlockRequestSent(bankCard.getBlockRequestSent());
        dto.setExpired(bankCard.isExpired());
        dto.setCanBeUsed(bankCard.canBeUsed());
        return dto;
    }
}
