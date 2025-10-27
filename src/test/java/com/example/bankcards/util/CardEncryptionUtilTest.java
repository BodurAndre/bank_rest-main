package com.example.bankcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для CardEncryptionUtil
 */
@SpringBootTest
@ActiveProfiles("test")
class CardEncryptionUtilTest {

    @Autowired
    private CardEncryptionUtil cardEncryptionUtil;

    /**
     * Генерирует валидный номер карты для тестов
     */
    private String generateValidCardNumber() {
        // Генерируем случайный 15-значный номер и добавляем контрольную цифру по алгоритму Луна
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            sb.append((int) (Math.random() * 10));
        }
        
        String baseNumber = sb.toString();
        int checkDigit = calculateLuhnCheckDigit(baseNumber);
        return baseNumber + checkDigit;
    }
    
    /**
     * Вычисляет контрольную цифру по алгоритму Луна
     */
    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = digit / 10 + digit % 10;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (10 - (sum % 10)) % 10;
    }

    @Test
    @DisplayName("Генерация валидного номера карты")
    void generateCardNumber_ValidCardNumber() {
        // When
        String cardNumber = generateValidCardNumber();

        // Then
        assertNotNull(cardNumber);
        assertEquals(16, cardNumber.length());
        assertTrue(cardNumber.matches("\\d{16}"));
        assertTrue(cardEncryptionUtil.isValidCardNumber(cardNumber));
    }

    @Test
    @DisplayName("Генерация уникальных номеров карт")
    void generateCardNumber_UniqueCardNumbers() {
        // When
        String cardNumber1 = generateValidCardNumber();
        String cardNumber2 = generateValidCardNumber();

        // Then
        assertNotEquals(cardNumber1, cardNumber2);
        assertTrue(cardEncryptionUtil.isValidCardNumber(cardNumber1));
        assertTrue(cardEncryptionUtil.isValidCardNumber(cardNumber2));
    }

    @Test
    @DisplayName("Валидация корректного номера карты")
    void isValidCardNumber_ValidCardNumber() {
        // Given
        String validCardNumber = "4532015112830366";

        // When
        boolean isValid = cardEncryptionUtil.isValidCardNumber(validCardNumber);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Валидация некорректного номера карты")
    void isValidCardNumber_InvalidCardNumber() {
        // Given
        String invalidCardNumber = "1234567890123456";

        // When
        boolean isValid = cardEncryptionUtil.isValidCardNumber(invalidCardNumber);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Валидация номера карты с неправильной длиной")
    void isValidCardNumber_WrongLength() {
        // Given
        String shortCardNumber = "123456789012345";
        String longCardNumber = "12345678901234567";

        // When
        boolean isShortValid = cardEncryptionUtil.isValidCardNumber(shortCardNumber);
        boolean isLongValid = cardEncryptionUtil.isValidCardNumber(longCardNumber);

        // Then
        assertFalse(isShortValid);
        assertFalse(isLongValid);
    }

    @Test
    @DisplayName("Валидация номера карты с нецифровыми символами")
    void isValidCardNumber_NonNumericCharacters() {
        // Given
        String nonNumericCardNumber = "453201511283036a";

        // When
        boolean isValid = cardEncryptionUtil.isValidCardNumber(nonNumericCardNumber);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Валидация null номера карты")
    void isValidCardNumber_NullCardNumber() {
        // Given
        String nullCardNumber = null;

        // When
        boolean isValid = cardEncryptionUtil.isValidCardNumber(nullCardNumber);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Валидация пустого номера карты")
    void isValidCardNumber_EmptyCardNumber() {
        // Given
        String emptyCardNumber = "";

        // When
        boolean isValid = cardEncryptionUtil.isValidCardNumber(emptyCardNumber);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Шифрование номера карты")
    void encryptCardNumber_ValidCardNumber() {
        // Given
        String cardNumber = "4532015112830366";

        // When
        String encryptedNumber = cardEncryptionUtil.encryptCardNumber(cardNumber);

        // Then
        assertNotNull(encryptedNumber);
        assertNotEquals(cardNumber, encryptedNumber);
        assertTrue(cardEncryptionUtil.isEncrypted(encryptedNumber));
    }

    @Test
    @DisplayName("Расшифровка номера карты")
    void decryptCardNumber_ValidEncryptedNumber() {
        // Given
        String cardNumber = "4532015112830366";
        String encryptedNumber = cardEncryptionUtil.encryptCardNumber(cardNumber);

        // When
        String decryptedNumber = cardEncryptionUtil.decryptCardNumber(encryptedNumber);

        // Then
        assertNotNull(decryptedNumber);
        assertEquals(cardNumber, decryptedNumber);
    }

    @Test
    @DisplayName("Шифрование и расшифровка разных номеров карт")
    void encryptDecrypt_DifferentCardNumbers() {
        // Given
        String[] cardNumbers = {
            "4532015112830366",
            "4532015112830367",
            "4532015112830368"
        };

        // When & Then
        for (String cardNumber : cardNumbers) {
            String encryptedNumber = cardEncryptionUtil.encryptCardNumber(cardNumber);
            String decryptedNumber = cardEncryptionUtil.decryptCardNumber(encryptedNumber);
            
            assertNotNull(encryptedNumber);
            assertNotNull(decryptedNumber);
            assertEquals(cardNumber, decryptedNumber);
            assertNotEquals(cardNumber, encryptedNumber);
        }
    }

    @Test
    @DisplayName("Генерация маскированного номера из зашифрованного")
    void getMaskedNumberFromEncrypted_ValidEncryptedNumber() {
        // Given
        String cardNumber = "4532015112830366";
        String encryptedNumber = cardEncryptionUtil.encryptCardNumber(cardNumber);

        // When
        String maskedNumber = cardEncryptionUtil.getMaskedNumberFromEncrypted(encryptedNumber);

        // Then
        assertNotNull(maskedNumber);
        assertTrue(maskedNumber.startsWith("**** **** **** "));
        assertEquals(19, maskedNumber.length());
        assertTrue(maskedNumber.substring(15).matches("\\d{4}"));
    }

    @Test
    @DisplayName("Проверка уникальности маскированных номеров")
    void getMaskedNumberFromEncrypted_UniqueMaskedNumbers() {
        // Given
        String cardNumber1 = "4532015112830366";
        String cardNumber2 = "4532015112830367";
        String encryptedNumber1 = cardEncryptionUtil.encryptCardNumber(cardNumber1);
        String encryptedNumber2 = cardEncryptionUtil.encryptCardNumber(cardNumber2);

        // When
        String maskedNumber1 = cardEncryptionUtil.getMaskedNumberFromEncrypted(encryptedNumber1);
        String maskedNumber2 = cardEncryptionUtil.getMaskedNumberFromEncrypted(encryptedNumber2);

        // Then
        assertNotEquals(maskedNumber1, maskedNumber2);
        assertTrue(maskedNumber1.startsWith("**** **** **** "));
        assertTrue(maskedNumber2.startsWith("**** **** **** "));
    }

    @Test
    @DisplayName("Проверка что зашифрованный номер действительно зашифрован")
    void isEncrypted_ValidEncryptedNumber() {
        // Given
        String cardNumber = "4532015112830366";
        String encryptedNumber = cardEncryptionUtil.encryptCardNumber(cardNumber);

        // When
        boolean isEncrypted = cardEncryptionUtil.isEncrypted(encryptedNumber);

        // Then
        assertTrue(isEncrypted);
    }

    @Test
    @DisplayName("Проверка что незашифрованный номер не считается зашифрованным")
    void isEncrypted_UnencryptedNumber() {
        // Given
        String unencryptedNumber = "4532015112830366";

        // When
        boolean isEncrypted = cardEncryptionUtil.isEncrypted(unencryptedNumber);

        // Then
        assertFalse(isEncrypted);
    }

    @Test
    @DisplayName("Проверка что null не считается зашифрованным")
    void isEncrypted_NullNumber() {
        // Given
        String nullNumber = null;

        // When
        boolean isEncrypted = cardEncryptionUtil.isEncrypted(nullNumber);

        // Then
        assertFalse(isEncrypted);
    }

    @Test
    @DisplayName("Проверка что пустая строка не считается зашифрованной")
    void isEncrypted_EmptyNumber() {
        // Given
        String emptyNumber = "";

        // When
        boolean isEncrypted = cardEncryptionUtil.isEncrypted(emptyNumber);

        // Then
        assertFalse(isEncrypted);
    }

    @Test
    @DisplayName("Тест алгоритма Луна для различных номеров карт")
    void luhnAlgorithm_VariousCardNumbers() {
        // Given
        String[] validCardNumbers = {
            "4532015112830366",
            "4532015112830367",
            "4532015112830368"
        };

        String[] invalidCardNumbers = {
            "1234567890123456",
            "1111111111111111",
            "0000000000000000"
        };

        // When & Then
        for (String cardNumber : validCardNumbers) {
            assertTrue(cardEncryptionUtil.isValidCardNumber(cardNumber), 
                "Card number should be valid: " + cardNumber);
        }

        for (String cardNumber : invalidCardNumbers) {
            assertFalse(cardEncryptionUtil.isValidCardNumber(cardNumber), 
                "Card number should be invalid: " + cardNumber);
        }
    }

    @Test
    @DisplayName("Тест производительности шифрования")
    void encryptionPerformance() {
        // Given
        String cardNumber = "4532015112830366";
        int iterations = 1000;

        // When
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            String encryptedNumber = cardEncryptionUtil.encryptCardNumber(cardNumber);
            String decryptedNumber = cardEncryptionUtil.decryptCardNumber(encryptedNumber);
            assertEquals(cardNumber, decryptedNumber);
        }
        long endTime = System.currentTimeMillis();

        // Then
        long duration = endTime - startTime;
        assertTrue(duration < 5000, "Encryption/decryption took too long: " + duration + "ms");
    }

    @Test
    @DisplayName("Тест производительности генерации номеров карт")
    void cardNumberGenerationPerformance() {
        // Given
        int iterations = 1000;

        // When
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            String cardNumber = generateValidCardNumber();
            assertTrue(cardEncryptionUtil.isValidCardNumber(cardNumber));
        }
        long endTime = System.currentTimeMillis();

        // Then
        long duration = endTime - startTime;
        assertTrue(duration < 3000, "Card number generation took too long: " + duration + "ms");
    }

    @Test
    @DisplayName("Тест уникальности сгенерированных номеров карт")
    void cardNumberGeneration_Uniqueness() {
        // Given
        int iterations = 1000;
        String[] generatedNumbers = new String[iterations];

        // When
        for (int i = 0; i < iterations; i++) {
            generatedNumbers[i] = generateValidCardNumber();
        }

        // Then
        for (int i = 0; i < iterations; i++) {
            for (int j = i + 1; j < iterations; j++) {
                assertNotEquals(generatedNumbers[i], generatedNumbers[j], 
                    "Generated card numbers should be unique");
            }
        }
    }

    @Test
    @DisplayName("Тест обработки ошибок при шифровании")
    void encryption_ErrorHandling() {
        // Given
        String invalidCardNumber = "invalid";

        // When & Then
        assertThrows(Exception.class, () -> {
            cardEncryptionUtil.encryptCardNumber(invalidCardNumber);
        });
    }

    @Test
    @DisplayName("Тест обработки ошибок при расшифровке")
    void decryption_ErrorHandling() {
        // Given
        String invalidEncryptedNumber = "invalid_encrypted";

        // When & Then
        assertThrows(Exception.class, () -> {
            cardEncryptionUtil.decryptCardNumber(invalidEncryptedNumber);
        });
    }

    @Test
    @DisplayName("Тест обработки ошибок при генерации маскированного номера")
    void getMaskedNumberFromEncrypted_ErrorHandling() {
        // Given
        String invalidEncryptedNumber = "invalid_encrypted";

        // When & Then
        assertThrows(Exception.class, () -> {
            cardEncryptionUtil.getMaskedNumberFromEncrypted(invalidEncryptedNumber);
        });
    }
}