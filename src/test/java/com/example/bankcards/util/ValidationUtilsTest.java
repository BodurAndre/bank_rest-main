package com.example.bankcards.util;

import com.example.bankcards.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для ValidationUtils
 */
@ExtendWith(MockitoExtension.class)
class ValidationUtilsTest {

    @InjectMocks
    private ValidationUtils validationUtils;

    @Test
    @DisplayName("Валидация корректного email")
    void validateEmail_ValidEmail() {
        // Given
        String validEmail = "test@example.com";

        // When & Then
        assertDoesNotThrow(() -> validationUtils.validateEmail(validEmail));
    }

    @Test
    @DisplayName("Валидация email с поддоменами")
    void validateEmail_ValidEmailWithSubdomains() {
        // Given
        String validEmail = "user@mail.example.com";

        // When & Then
        assertDoesNotThrow(() -> validationUtils.validateEmail(validEmail));
    }

    @Test
    @DisplayName("Валидация email с цифрами")
    void validateEmail_ValidEmailWithNumbers() {
        // Given
        String validEmail = "user123@example.com";

        // When & Then
        assertDoesNotThrow(() -> validationUtils.validateEmail(validEmail));
    }

    @Test
    @DisplayName("Валидация некорректного email")
    void validateEmail_InvalidEmail() {
        // Given
        String invalidEmail = "invalid-email";

        // When & Then
        assertThrows(ValidationException.class, () -> validationUtils.validateEmail(invalidEmail));
    }

    @Test
    @DisplayName("Валидация пустого email")
    void validateEmail_EmptyEmail() {
        // Given
        String emptyEmail = "";

        // When & Then
        assertThrows(ValidationException.class, () -> validationUtils.validateEmail(emptyEmail));
    }

    @Test
    @DisplayName("Валидация null email")
    void validateEmail_NullEmail() {
        // Given
        String nullEmail = null;

        // When & Then
        assertThrows(ValidationException.class, () -> validationUtils.validateEmail(nullEmail));
    }

    @Test
    @DisplayName("Валидация email без @")
    void validateEmail_EmailWithoutAt() {
        // Given
        String emailWithoutAt = "userexample.com";

        // When & Then
        assertThrows(ValidationException.class, () -> validationUtils.validateEmail(emailWithoutAt));
    }

    @Test
    @DisplayName("Валидация email с несколькими @")
    void validateEmail_EmailWithMultipleAt() {
        // Given
        String emailWithMultipleAt = "user@example@com";

        // When & Then
        assertThrows(ValidationException.class, () -> validationUtils.validateEmail(emailWithMultipleAt));
    }

    @Test
    @DisplayName("Валидация корректного ID")
    void validateId_ValidId() {
        // Given
        Long validId = 1L;

        // When & Then
        assertDoesNotThrow(() -> validationUtils.validateId(validId, "карты"));
    }

    @Test
    @DisplayName("Валидация нулевого ID")
    void validateId_ZeroId() {
        // Given
        Long zeroId = 0L;

        // When & Then
        assertThrows(ValidationException.class, () -> validationUtils.validateId(zeroId, "карты"));
    }

    @Test
    @DisplayName("Валидация отрицательного ID")
    void validateId_NegativeId() {
        // Given
        Long negativeId = -1L;

        // When & Then
        assertThrows(ValidationException.class, () -> validationUtils.validateId(negativeId, "карты"));
    }

    @Test
    @DisplayName("Валидация null ID")
    void validateId_NullId() {
        // Given
        Long nullId = null;

        // When & Then
        assertThrows(ValidationException.class, () -> validationUtils.validateId(nullId, "карты"));
    }

    @Test
    @DisplayName("Валидация корректной суммы")
    void validateMinAmount_ValidAmount() {
        // Given
        BigDecimal validAmount = BigDecimal.valueOf(100.50);
        BigDecimal minAmount = BigDecimal.valueOf(0.01);

        // When & Then
        assertDoesNotThrow(() -> validationUtils.validateMinAmount(validAmount, minAmount));
    }

    @Test
    @DisplayName("Валидация минимальной суммы")
    void validateMinAmount_MinimumAmount() {
        // Given
        BigDecimal minimumAmount = BigDecimal.valueOf(0.01);
        BigDecimal minAmount = BigDecimal.valueOf(0.01);

        // When & Then
        assertDoesNotThrow(() -> validationUtils.validateMinAmount(minimumAmount, minAmount));
    }

    @Test
    @DisplayName("Валидация нулевой суммы")
    void validateMinAmount_ZeroAmount() {
        // Given
        BigDecimal zeroAmount = BigDecimal.ZERO;
        BigDecimal minAmount = BigDecimal.valueOf(0.01);

        // When & Then
        assertThrows(ValidationException.class, () -> validationUtils.validateMinAmount(zeroAmount, minAmount));
    }

    @Test
    @DisplayName("Валидация отрицательной суммы")
    void validateMinAmount_NegativeAmount() {
        // Given
        BigDecimal negativeAmount = BigDecimal.valueOf(-100.00);
        BigDecimal minAmount = BigDecimal.valueOf(0.01);

        // When & Then
        assertThrows(ValidationException.class, () -> validationUtils.validateMinAmount(negativeAmount, minAmount));
    }

    @Test
    @DisplayName("Валидация null суммы")
    void validateMinAmount_NullAmount() {
        // Given
        BigDecimal nullAmount = null;
        BigDecimal minAmount = BigDecimal.valueOf(0.01);

        // When & Then
        assertThrows(ValidationException.class, () -> validationUtils.validateMinAmount(nullAmount, minAmount));
    }

    @Test
    @DisplayName("Валидация суммы меньше минимальной")
    void validateMinAmount_AmountLessThanMinimum() {
        // Given
        BigDecimal smallAmount = BigDecimal.valueOf(0.005);
        BigDecimal minAmount = BigDecimal.valueOf(0.01);

        // When & Then
        assertThrows(ValidationException.class, () -> validationUtils.validateMinAmount(smallAmount, minAmount));
    }

    @Test
    @DisplayName("Валидация корректного описания")
    void validateDescription_ValidDescription() {
        // Given
        String validDescription = "Valid description";
        String fieldName = "Описание";

        // When & Then
        assertDoesNotThrow(() -> validationUtils.validateDescription(validDescription, fieldName));
    }

    @Test
    @DisplayName("Валидация описания с специальными символами")
    void validateDescription_DescriptionWithSpecialChars() {
        // Given
        String descriptionWithSpecialChars = "Description with special chars: !@#$%^&*()";
        String fieldName = "Описание";

        // When & Then
        assertDoesNotThrow(() -> validationUtils.validateDescription(descriptionWithSpecialChars, fieldName));
    }

    @Test
    @DisplayName("Валидация описания с эмодзи")
    void validateDescription_DescriptionWithEmoji() {
        // Given
        String descriptionWithEmoji = "Description with emoji 🏦💰";
        String fieldName = "Описание";

        // When & Then
        assertDoesNotThrow(() -> validationUtils.validateDescription(descriptionWithEmoji, fieldName));
    }

    @Test
    @DisplayName("Валидация пустого описания")
    void validateDescription_EmptyDescription() {
        // Given
        String emptyDescription = "";
        String fieldName = "Описание";

        // When & Then
        assertThrows(ValidationException.class, () -> validationUtils.validateDescription(emptyDescription, fieldName));
    }

    @Test
    @DisplayName("Валидация null описания")
    void validateDescription_NullDescription() {
        // Given
        String nullDescription = null;
        String fieldName = "Описание";

        // When & Then
        assertThrows(ValidationException.class, () -> validationUtils.validateDescription(nullDescription, fieldName));
    }

    @Test
    @DisplayName("Валидация описания только из пробелов")
    void validateDescription_WhitespaceOnlyDescription() {
        // Given
        String whitespaceOnlyDescription = "   ";
        String fieldName = "Описание";

        // When & Then
        assertThrows(ValidationException.class, () -> validationUtils.validateDescription(whitespaceOnlyDescription, fieldName));
    }

    @Test
    @DisplayName("Валидация слишком длинного описания")
    void validateDescription_TooLongDescription() {
        // Given
        String tooLongDescription = "a".repeat(1001); // Exceeds maximum length
        String fieldName = "Описание";

        // When & Then
        assertThrows(ValidationException.class, () -> validationUtils.validateDescription(tooLongDescription, fieldName));
    }

    @Test
    @DisplayName("Валидация описания максимальной длины")
    void validateDescription_MaximumLengthDescription() {
        // Given
        String maximumLengthDescription = "a".repeat(1000); // Maximum allowed length
        String fieldName = "Описание";

        // When & Then
        assertDoesNotThrow(() -> validationUtils.validateDescription(maximumLengthDescription, fieldName));
    }

    @Test
    @DisplayName("Валидация корректной даты истечения")
    void validateExpiryDate_ValidExpiryDate() {
        // Given
        LocalDate validExpiryDate = LocalDate.now().plusYears(2);

        // When & Then
        assertDoesNotThrow(() -> validationUtils.validateExpiryDate(validExpiryDate));
    }

    @Test
    @DisplayName("Валидация даты истечения в прошлом")
    void validateExpiryDate_PastExpiryDate() {
        // Given
        LocalDate pastExpiryDate = LocalDate.now().minusDays(1);

        // When & Then
        assertThrows(ValidationException.class, () -> validationUtils.validateExpiryDate(pastExpiryDate));
    }

    @Test
    @DisplayName("Валидация null даты истечения")
    void validateExpiryDate_NullExpiryDate() {
        // Given
        LocalDate nullExpiryDate = null;

        // When & Then
        assertThrows(ValidationException.class, () -> validationUtils.validateExpiryDate(nullExpiryDate));
    }

    @Test
    @DisplayName("Валидация даты истечения сегодня")
    void validateExpiryDate_TodayExpiryDate() {
        // Given
        LocalDate todayExpiryDate = LocalDate.now();

        // When & Then
        assertThrows(ValidationException.class, () -> validationUtils.validateExpiryDate(todayExpiryDate));
    }

    @Test
    @DisplayName("Валидация даты истечения завтра")
    void validateExpiryDate_TomorrowExpiryDate() {
        // Given
        LocalDate tomorrowExpiryDate = LocalDate.now().plusDays(1);

        // When & Then
        assertDoesNotThrow(() -> validationUtils.validateExpiryDate(tomorrowExpiryDate));
    }

    @Test
    @DisplayName("Валидация даты истечения в далеком будущем")
    void validateExpiryDate_FarFutureExpiryDate() {
        // Given
        LocalDate farFutureExpiryDate = LocalDate.now().plusYears(10);

        // When & Then
        assertDoesNotThrow(() -> validationUtils.validateExpiryDate(farFutureExpiryDate));
    }

    @Test
    @DisplayName("Валидация граничных случаев для email")
    void validateEmail_EdgeCases() {
        // Test various edge cases
        String[] validEmails = {
            "a@b.co",
            "test+tag@example.com",
            "test.email@example.com",
            "test_email@example.com",
            "123@example.com"
        };

        String[] invalidEmails = {
            "@example.com",
            "test@",
            "test@.com",
            "test..email@example.com",
            "test@example..com",
            "test@example.com.",
            "test@example.com.."
        };

        for (String email : validEmails) {
            assertDoesNotThrow(() -> validationUtils.validateEmail(email), 
                "Email should be valid: " + email);
        }

        for (String email : invalidEmails) {
            assertThrows(ValidationException.class, () -> validationUtils.validateEmail(email), 
                "Email should be invalid: " + email);
        }
    }

    @Test
    @DisplayName("Валидация граничных случаев для суммы")
    void validateMinAmount_EdgeCases() {
        // Test various edge cases
        BigDecimal[] validAmounts = {
            BigDecimal.valueOf(0.01),
            BigDecimal.valueOf(0.1),
            BigDecimal.valueOf(1.0),
            BigDecimal.valueOf(100.0),
            BigDecimal.valueOf(999999.99)
        };

        BigDecimal[] invalidAmounts = {
            BigDecimal.valueOf(0.001),
            BigDecimal.valueOf(0.009),
            BigDecimal.valueOf(-0.01),
            BigDecimal.valueOf(-100.0)
        };

        BigDecimal minAmount = BigDecimal.valueOf(0.01);

        for (BigDecimal amount : validAmounts) {
            assertDoesNotThrow(() -> validationUtils.validateMinAmount(amount, minAmount), 
                "Amount should be valid: " + amount);
        }

        for (BigDecimal amount : invalidAmounts) {
            assertThrows(ValidationException.class, () -> validationUtils.validateMinAmount(amount, minAmount), 
                "Amount should be invalid: " + amount);
        }
    }

    @Test
    @DisplayName("Валидация граничных случаев для ID")
    void validateId_EdgeCases() {
        // Test various edge cases
        Long[] validIds = {
            1L,
            100L,
            999999L,
            Long.MAX_VALUE
        };

        Long[] invalidIds = {
            0L,
            -1L,
            -100L,
            Long.MIN_VALUE
        };

        for (Long id : validIds) {
            assertDoesNotThrow(() -> validationUtils.validateId(id, "карты"), 
                "ID should be valid: " + id);
        }

        for (Long id : invalidIds) {
            assertThrows(ValidationException.class, () -> validationUtils.validateId(id, "карты"), 
                "ID should be invalid: " + id);
        }
    }
}