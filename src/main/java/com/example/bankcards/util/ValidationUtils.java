package com.example.bankcards.util;

import com.example.bankcards.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Утилитный класс для валидации данных
 */
@Component
public class ValidationUtils {
    
    private static final Pattern EMAIL_PATTERN = 
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    private static final Pattern PHONE_PATTERN = 
            Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    
    private static final Pattern NAME_PATTERN = 
            Pattern.compile("^[А-Яа-яA-Za-z\\s]{2,50}$");
    
    /**
     * Валидация email
     */
    public void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email не может быть пустым");
        }
        
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Некорректный формат email");
        }
    }
    
    /**
     * Валидация имени
     */
    public void validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException(fieldName + " не может быть пустым");
        }
        
        if (!NAME_PATTERN.matcher(name.trim()).matches()) {
            throw new ValidationException(fieldName + " должно содержать только буквы и быть длиной от 2 до 50 символов");
        }
    }
    
    /**
     * Валидация пароля
     */
    public void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Пароль не может быть пустым");
        }
        
        if (password.length() < 6) {
            throw new ValidationException("Пароль должен содержать минимум 6 символов");
        }
        
        if (password.length() > 100) {
            throw new ValidationException("Пароль не может быть длиннее 100 символов");
        }
    }
    
    /**
     * Валидация суммы денег
     */
    public void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new ValidationException("Сумма не может быть пустой");
        }
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Сумма должна быть больше нуля");
        }
        
        if (amount.compareTo(new BigDecimal("1000000")) > 0) {
            throw new ValidationException("Сумма не может превышать 1,000,000 ₽");
        }
        
        // Проверяем, что не более 2 знаков после запятой
        if (amount.scale() > 2) {
            throw new ValidationException("Сумма не может иметь более 2 знаков после запятой");
        }
    }
    
    /**
     * Валидация минимальной суммы для операции
     */
    public void validateMinAmount(BigDecimal amount, BigDecimal minAmount) {
        validateAmount(amount);
        
        if (amount.compareTo(minAmount) < 0) {
            throw new ValidationException(String.format("Минимальная сумма операции: %.2f ₽", minAmount));
        }
    }
    
    /**
     * Валидация даты рождения
     */
    public void validateDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new ValidationException("Дата рождения не может быть пустой");
        }
        
        LocalDate now = LocalDate.now();
        LocalDate minDate = now.minusYears(120);
        LocalDate maxDate = now.minusYears(18);
        
        if (dateOfBirth.isBefore(minDate)) {
            throw new ValidationException("Некорректная дата рождения (слишком старая)");
        }
        
        if (dateOfBirth.isAfter(maxDate)) {
            throw new ValidationException("Возраст должен быть не менее 18 лет");
        }
    }
    
    /**
     * Валидация даты рождения (строковый формат)
     */
    public void validateDateOfBirth(String dateOfBirth) {
        if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) {
            return; // Дата рождения необязательна
        }
        
        try {
            LocalDate parsedDate = parseDate(dateOfBirth.trim());
            validateDateOfBirth(parsedDate);
        } catch (ValidationException e) {
            // ValidationException уже содержит правильное сообщение (например, о возрасте)
            throw e; // Пробрасываем ValidationException как есть
        } catch (Exception e) {
            // Только ошибки парсинга заменяем на сообщение о формате
            throw new ValidationException("Некорректный формат даты рождения. Используйте формат YYYY-MM-DD или DD.MM.YYYY");
        }
    }
    
    /**
     * Парсит дату из строки, поддерживая разные форматы
     */
    private LocalDate parseDate(String dateString) {
        // Пробуем разные форматы
        String[] formats = {
            "yyyy-MM-dd",     // ISO формат (HTML date input)
            "dd.MM.yyyy",     // Русский формат
            "dd/MM/yyyy",     // Альтернативный формат
            "yyyy/MM/dd"      // Альтернативный ISO
        };
        
        for (String format : formats) {
            try {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(format);
                return LocalDate.parse(dateString, formatter);
            } catch (Exception ignored) {
                // Продолжаем пробовать другие форматы
            }
        }
        
        // Если ни один формат не подошел, пробуем стандартный парсер
        return LocalDate.parse(dateString);
    }
    
    /**
     * Валидация срока действия карты
     */
    public void validateExpiryDate(LocalDate expiryDate) {
        if (expiryDate == null) {
            throw new ValidationException("Срок действия карты не может быть пустым");
        }
        
        LocalDate now = LocalDate.now();
        LocalDate maxDate = now.plusYears(10);
        
        if (expiryDate.isBefore(now)) {
            throw new ValidationException("Срок действия карты не может быть в прошлом");
        }
        
        if (expiryDate.isAfter(maxDate)) {
            throw new ValidationException("Срок действия карты не может превышать 10 лет");
        }
    }
    
    /**
     * Валидация описания/причины
     */
    public void validateDescription(String description, String fieldName) {
        if (description != null && description.length() > 500) {
            throw new ValidationException(fieldName + " не может быть длиннее 500 символов");
        }
    }
    
    /**
     * Валидация ID
     */
    public void validateId(Long id, String entityName) {
        if (id == null || id <= 0) {
            throw new ValidationException("ID " + entityName + " должен быть положительным числом");
        }
    }
    
    /**
     * Проверка, что строка не пустая
     */
    public void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " не может быть пустым");
        }
    }
}
