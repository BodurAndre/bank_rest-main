package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для статистики переводов
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferStats {
    private long totalTransfers;
    private double totalAmount;
    private double averageAmount;
    private long transfersThisMonth;
    private double amountThisMonth;
}
