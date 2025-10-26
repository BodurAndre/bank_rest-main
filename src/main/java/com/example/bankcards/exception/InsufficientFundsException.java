package com.example.bankcards.exception;

import java.math.BigDecimal;

/**
 * Исключение для случаев недостатка средств
 */
public class InsufficientFundsException extends BusinessException {
    
    private final BigDecimal availableBalance;
    private final BigDecimal requestedAmount;
    
    public InsufficientFundsException(BigDecimal availableBalance, BigDecimal requestedAmount) {
        super(String.format("Недостаточно средств. Доступно: %.2f ₽, запрошено: %.2f ₽", 
                availableBalance, requestedAmount), "INSUFFICIENT_FUNDS");
        this.availableBalance = availableBalance;
        this.requestedAmount = requestedAmount;
    }
    
    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }
    
    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
}
