package com.example.bankcards.exception;

/**
 * Исключение для операций с заблокированными картами
 */
public class CardBlockedException extends BusinessException {
    
    private final String cardNumber;
    private final String blockReason;
    
    public CardBlockedException(String cardNumber, String blockReason) {
        super(String.format("Карта %s заблокирована. Причина: %s", cardNumber, blockReason), "CARD_BLOCKED");
        this.cardNumber = cardNumber;
        this.blockReason = blockReason;
    }
    
    public String getCardNumber() {
        return cardNumber;
    }
    
    public String getBlockReason() {
        return blockReason;
    }
}
