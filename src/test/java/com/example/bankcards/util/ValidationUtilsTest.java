package com.example.bankcards.util;

import com.example.bankcards.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для ValidationUtils
 */
class ValidationUtilsTest {
    
    private ValidationUtils validationUtils;
    
    @BeforeEach
    void setUp() {
        validationUtils = new ValidationUtils();
    }
    
    @Test
    void testValidateEmail_ValidEmail() {
        assertDoesNotThrow(() -> validationUtils.validateEmail("test@example.com"));
        assertDoesNotThrow(() -> validationUtils.validateEmail("user.name+tag@domain.co.uk"));
    }
    
    @Test
    void testValidateEmail_InvalidEmail() {
        assertThrows(ValidationException.class, () -> validationUtils.validateEmail(null));
        assertThrows(ValidationException.class, () -> validationUtils.validateEmail(""));
        assertThrows(ValidationException.class, () -> validationUtils.validateEmail("invalid-email"));
        assertThrows(ValidationException.class, () -> validationUtils.validateEmail("@domain.com"));
        assertThrows(ValidationException.class, () -> validationUtils.validateEmail("user@"));
    }
    
    @Test
    void testValidateName_ValidName() {
        assertDoesNotThrow(() -> validationUtils.validateName("Иван", "Имя"));
        assertDoesNotThrow(() -> validationUtils.validateName("John", "Name"));
        assertDoesNotThrow(() -> validationUtils.validateName("Анна-Мария", "Имя"));
    }
    
    @Test
    void testValidateName_InvalidName() {
        assertThrows(ValidationException.class, () -> validationUtils.validateName(null, "Имя"));
        assertThrows(ValidationException.class, () -> validationUtils.validateName("", "Имя"));
        assertThrows(ValidationException.class, () -> validationUtils.validateName("A", "Имя")); // слишком короткое
        assertThrows(ValidationException.class, () -> validationUtils.validateName("123", "Имя")); // цифры
    }
    
    @Test
    void testValidatePassword_ValidPassword() {
        assertDoesNotThrow(() -> validationUtils.validatePassword("password123"));
        assertDoesNotThrow(() -> validationUtils.validatePassword("strongPassword!"));
    }
    
    @Test
    void testValidatePassword_InvalidPassword() {
        assertThrows(ValidationException.class, () -> validationUtils.validatePassword(null));
        assertThrows(ValidationException.class, () -> validationUtils.validatePassword(""));
        assertThrows(ValidationException.class, () -> validationUtils.validatePassword("12345")); // слишком короткий
    }
    
    @Test
    void testValidateAmount_ValidAmount() {
        assertDoesNotThrow(() -> validationUtils.validateAmount(new BigDecimal("100.50")));
        assertDoesNotThrow(() -> validationUtils.validateAmount(new BigDecimal("0.01")));
        assertDoesNotThrow(() -> validationUtils.validateAmount(new BigDecimal("999999")));
    }
    
    @Test
    void testValidateAmount_InvalidAmount() {
        assertThrows(ValidationException.class, () -> validationUtils.validateAmount(null));
        assertThrows(ValidationException.class, () -> validationUtils.validateAmount(BigDecimal.ZERO));
        assertThrows(ValidationException.class, () -> validationUtils.validateAmount(new BigDecimal("-10")));
        assertThrows(ValidationException.class, () -> validationUtils.validateAmount(new BigDecimal("1000001"))); // слишком большая
        assertThrows(ValidationException.class, () -> validationUtils.validateAmount(new BigDecimal("10.123"))); // слишком много знаков после запятой
    }
    
    @Test
    void testValidateDateOfBirth_ValidDate() {
        LocalDate validDate = LocalDate.now().minusYears(25);
        assertDoesNotThrow(() -> validationUtils.validateDateOfBirth(validDate));
    }
    
    @Test
    void testValidateDateOfBirth_InvalidDate() {
        assertThrows(ValidationException.class, () -> validationUtils.validateDateOfBirth((LocalDate) null));
        
        LocalDate tooYoung = LocalDate.now().minusYears(17);
        assertThrows(ValidationException.class, () -> validationUtils.validateDateOfBirth(tooYoung));
        
        LocalDate tooOld = LocalDate.now().minusYears(121);
        assertThrows(ValidationException.class, () -> validationUtils.validateDateOfBirth(tooOld));
    }
    
    @Test
    void testValidateDateOfBirth_StringFormat_ValidDate() {
        assertDoesNotThrow(() -> validationUtils.validateDateOfBirth("1990-05-15")); // ISO format
        assertDoesNotThrow(() -> validationUtils.validateDateOfBirth("2000-01-01")); // ISO format
        assertDoesNotThrow(() -> validationUtils.validateDateOfBirth("15.05.1990")); // Russian format
        assertDoesNotThrow(() -> validationUtils.validateDateOfBirth("15/05/1990")); // Alternative format
        assertDoesNotThrow(() -> validationUtils.validateDateOfBirth(null)); // optional field
        assertDoesNotThrow(() -> validationUtils.validateDateOfBirth("")); // optional field
        assertDoesNotThrow(() -> validationUtils.validateDateOfBirth("   ")); // optional field
    }
    
    @Test
    void testValidateDateOfBirth_StringFormat_InvalidDate() {
        // Проверяем, что ошибки парсинга дают сообщение о формате
        ValidationException formatException1 = assertThrows(ValidationException.class, 
            () -> validationUtils.validateDateOfBirth("invalid-date"));
        assertTrue(formatException1.getMessage().contains("Некорректный формат даты рождения"));
        
        ValidationException formatException2 = assertThrows(ValidationException.class, 
            () -> validationUtils.validateDateOfBirth("32.13.1990")); // invalid date
        assertTrue(formatException2.getMessage().contains("Некорректный формат даты рождения"));
        
        ValidationException formatException3 = assertThrows(ValidationException.class, 
            () -> validationUtils.validateDateOfBirth("abc-def-ghij")); // completely wrong format
        assertTrue(formatException3.getMessage().contains("Некорректный формат даты рождения"));
        
        // Проверяем, что ошибки валидации возраста дают правильное сообщение
        ValidationException ageException1 = assertThrows(ValidationException.class, 
            () -> validationUtils.validateDateOfBirth("2010-01-01")); // too young
        assertTrue(ageException1.getMessage().contains("Возраст должен быть не менее 18 лет"));
        
        ValidationException ageException2 = assertThrows(ValidationException.class, 
            () -> validationUtils.validateDateOfBirth("01.01.1900")); // too old
        assertTrue(ageException2.getMessage().contains("Возраст должен быть не более 120 лет"));
    }
    
    @Test
    void testValidateExpiryDate_ValidDate() {
        LocalDate validDate = LocalDate.now().plusYears(2);
        assertDoesNotThrow(() -> validationUtils.validateExpiryDate(validDate));
    }
    
    @Test
    void testValidateExpiryDate_InvalidDate() {
        assertThrows(ValidationException.class, () -> validationUtils.validateExpiryDate(null));
        
        LocalDate pastDate = LocalDate.now().minusDays(1);
        assertThrows(ValidationException.class, () -> validationUtils.validateExpiryDate(pastDate));
        
        LocalDate tooFarFuture = LocalDate.now().plusYears(11);
        assertThrows(ValidationException.class, () -> validationUtils.validateExpiryDate(tooFarFuture));
    }
    
    @Test
    void testValidateId_ValidId() {
        assertDoesNotThrow(() -> validationUtils.validateId(1L, "пользователя"));
        assertDoesNotThrow(() -> validationUtils.validateId(999L, "карты"));
    }
    
    @Test
    void testValidateId_InvalidId() {
        assertThrows(ValidationException.class, () -> validationUtils.validateId(null, "пользователя"));
        assertThrows(ValidationException.class, () -> validationUtils.validateId(0L, "пользователя"));
        assertThrows(ValidationException.class, () -> validationUtils.validateId(-1L, "пользователя"));
    }
}
