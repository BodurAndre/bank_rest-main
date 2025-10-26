package com.example.bankcards.util;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Утилита для шифрования номеров банковских карт
 */
@Component
public class CardEncryptionUtil {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
    // Секретный ключ для шифрования (в продакшене должен быть в переменных окружения)
    private static final String SECRET_KEY = "MySecretKey12345"; // 16 символов для AES-128
    
    private SecretKey secretKey;
    
    public CardEncryptionUtil() {
        try {
            // Создаем ключ из строки
            byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
            this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации шифрования", e);
        }
    }
    
    /**
     * Шифрует номер карты
     */
    public String encryptCardNumber(String cardNumber) {
        try {
            if (cardNumber == null || cardNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("Номер карты не может быть пустым");
            }
            
            // Убираем пробелы и проверяем формат
            String cleanNumber = cardNumber.replaceAll("\\s", "");
            if (!cleanNumber.matches("\\d{16}")) {
                throw new IllegalArgumentException("Номер карты должен содержать 16 цифр");
            }
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = cipher.doFinal(cleanNumber.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
            
        } catch (Exception e) {
            throw new RuntimeException("Ошибка шифрования номера карты", e);
        }
    }
    
    /**
     * Расшифровывает номер карты
     */
    public String decryptCardNumber(String encryptedCardNumber) {
        try {
            if (encryptedCardNumber == null || encryptedCardNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("Зашифрованный номер карты не может быть пустым");
            }
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedCardNumber);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            return new String(decryptedBytes, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            throw new RuntimeException("Ошибка расшифровки номера карты", e);
        }
    }
    
    /**
     * Проверяет, является ли строка зашифрованным номером карты
     */
    public boolean isEncrypted(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Пытаемся декодировать как Base64
            Base64.getDecoder().decode(value);
            // Если успешно, то это зашифрованная строка
            return true;
        } catch (IllegalArgumentException e) {
            // Если не Base64, то это обычная строка
            return false;
        }
    }
    
    /**
     * Генерирует маскированный номер карты из зашифрованного
     */
    public String getMaskedNumberFromEncrypted(String encryptedCardNumber) {
        try {
            String decryptedNumber = decryptCardNumber(encryptedCardNumber);
            return maskCardNumber(decryptedNumber);
        } catch (Exception e) {
            // Если не удается расшифровать, возвращаем общую маску
            return "**** **** **** ****";
        }
    }
    
    /**
     * Маскирует номер карты (показывает только последние 4 цифры)
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
    
    /**
     * Проверяет валидность номера карты по алгоритму Луна
     */
    public boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
            return false;
        }
        
        int sum = 0;
        boolean alternate = false;
        
        // Проходим по цифрам справа налево
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = digit / 10 + digit % 10;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return sum % 10 == 0;
    }
}
