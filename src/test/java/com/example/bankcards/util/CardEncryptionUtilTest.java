package com.example.bankcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для утилиты шифрования номеров карт
 */
class CardEncryptionUtilTest {

    private CardEncryptionUtil encryptionUtil;

    @BeforeEach
    void setUp() {
        encryptionUtil = new CardEncryptionUtil();
    }

    @Test
    @DisplayName("Шифрование и расшифровка номера карты")
    void testEncryptDecryptCardNumber() {
        // Arrange
        String originalCardNumber = "4532015112830366";

        // Act
        String encrypted = encryptionUtil.encryptCardNumber(originalCardNumber);
        String decrypted = encryptionUtil.decryptCardNumber(encrypted);

        // Assert
        assertNotNull(encrypted);
        assertNotEquals(originalCardNumber, encrypted);
        assertEquals(originalCardNumber, decrypted);
    }

    @Test
    @DisplayName("Проверка валидности номера карты по алгоритму Луна")
    void testValidCardNumber() {
        // Arrange
        String validCardNumber = "4532015112830366"; // Валидный номер
        String invalidCardNumber = "1234567890123456"; // Невалидный номер

        // Act & Assert
        assertTrue(encryptionUtil.isValidCardNumber(validCardNumber));
        assertFalse(encryptionUtil.isValidCardNumber(invalidCardNumber));
    }

    @Test
    @DisplayName("Проверка определения зашифрованной строки")
    void testIsEncrypted() {
        // Arrange
        String plainText = "4532015112830366";
        String encrypted = encryptionUtil.encryptCardNumber(plainText);

        // Act & Assert
        assertFalse(encryptionUtil.isEncrypted(plainText));
        assertTrue(encryptionUtil.isEncrypted(encrypted));
    }

    @Test
    @DisplayName("Генерация маскированного номера из зашифрованного")
    void testGetMaskedNumberFromEncrypted() {
        // Arrange
        String originalCardNumber = "4532015112830366";
        String encrypted = encryptionUtil.encryptCardNumber(originalCardNumber);

        // Act
        String masked = encryptionUtil.getMaskedNumberFromEncrypted(encrypted);

        // Assert
        assertNotNull(masked);
        assertTrue(masked.startsWith("**** **** **** "));
        assertTrue(masked.endsWith("0366")); // Последние 4 цифры
    }

    @Test
    @DisplayName("Обработка некорректных данных")
    void testInvalidInput() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            encryptionUtil.encryptCardNumber(null));
        
        assertThrows(IllegalArgumentException.class, () -> 
            encryptionUtil.encryptCardNumber(""));
        
        assertThrows(IllegalArgumentException.class, () -> 
            encryptionUtil.encryptCardNumber("123")); // Слишком короткий
        
        assertThrows(IllegalArgumentException.class, () -> 
            encryptionUtil.decryptCardNumber(null));
        
        assertThrows(IllegalArgumentException.class, () -> 
            encryptionUtil.decryptCardNumber(""));
    }

    @Test
    @DisplayName("Проверка различных валидных номеров карт")
    void testVariousValidCardNumbers() {
        // Arrange
        String[] validNumbers = {
            "4532015112830366", // Visa
            "5555555555554444", // Mastercard
            "378282246310005",  // American Express
            "6011111111111117"  // Discover
        };

        // Act & Assert
        for (String cardNumber : validNumbers) {
            assertTrue(encryptionUtil.isValidCardNumber(cardNumber), 
                "Номер " + cardNumber + " должен быть валидным");
        }
    }

    @Test
    @DisplayName("Проверка различных невалидных номеров карт")
    void testVariousInvalidCardNumbers() {
        // Arrange
        String[] invalidNumbers = {
            "1234567890123456", // Невалидный
            "0000000000000000", // Все нули
            "1111111111111111", // Все единицы
            "123456789012345"   // Слишком короткий
        };

        // Act & Assert
        for (String cardNumber : invalidNumbers) {
            assertFalse(encryptionUtil.isValidCardNumber(cardNumber), 
                "Номер " + cardNumber + " должен быть невалидным");
        }
    }
}
