package com.example.bankcards.service;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.CreateBankCardRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardEncryptionUtil;
import com.example.bankcards.util.ValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для граничных случаев и обработки ошибок
 */
@ExtendWith(MockitoExtension.class)
class EdgeCaseAndErrorHandlingTest {

    @Mock
    private BankCardRepository bankCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private CardEncryptionUtil cardEncryptionUtil;

    @Mock
    private ValidationUtils validationUtils;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private BankCardService bankCardService;

    @InjectMocks
    private TransferService transferService;

    private User testUser;
    private BankCard testCard;
    private BankCard testCard2;
    private CreateBankCardRequest createRequest;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(User.Role.USER);

        testCard = new BankCard();
        testCard.setId(1L);
        testCard.setMaskedNumber("**** **** **** 1234");
        testCard.setBalance(BigDecimal.valueOf(1000.00));
        testCard.setStatus(BankCard.Status.ACTIVE);
        testCard.setExpiryDate(LocalDate.now().plusYears(2));
        testCard.setOwner(testUser);

        testCard2 = new BankCard();
        testCard2.setId(2L);
        testCard2.setMaskedNumber("**** **** **** 5678");
        testCard2.setBalance(BigDecimal.valueOf(500.00));
        testCard2.setStatus(BankCard.Status.ACTIVE);
        testCard2.setExpiryDate(LocalDate.now().plusYears(2));
        testCard2.setOwner(testUser);

        createRequest = new CreateBankCardRequest();
        createRequest.setOwnerEmail("test@example.com");
        createRequest.setExpiryDate("12/26");

        transferRequest = new TransferRequest();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(BigDecimal.valueOf(200.00));
        transferRequest.setDescription("Test transfer");
    }

    /**
     * Генерирует валидный номер карты используя алгоритм Луна
     */
    private String generateValidCardNumber() {
        // Генерируем 15 цифр (без контрольной)
        StringBuilder cardNumber = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            cardNumber.append((int) (Math.random() * 10));
        }
        
        // Вычисляем контрольную цифру по алгоритму Луна
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        
        int checkDigit = (10 - (sum % 10)) % 10;
        cardNumber.append(checkDigit);
        
        return cardNumber.toString();
    }

    // ========== BankCardService Edge Cases ==========

    @Test
    @DisplayName("Создание карты с максимально длинным email")
    void createCard_MaxLengthEmail() {
        // Given
        String maxLengthEmail = "a".repeat(250) + "@test.com";
        createRequest.setOwnerEmail(maxLengthEmail);
        when(userRepository.findByEmail(maxLengthEmail)).thenReturn(Optional.of(testUser));
        when(cardEncryptionUtil.isValidCardNumber(anyString())).thenReturn(true);
        when(cardEncryptionUtil.encryptCardNumber(anyString())).thenReturn("encrypted_card_number");
        when(cardEncryptionUtil.getMaskedNumberFromEncrypted(anyString())).thenReturn("**** **** **** 1234");
        when(bankCardRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(testCard);

        // When
        BankCardDto result = bankCardService.createCard(createRequest);

        // Then
        assertNotNull(result);
        verify(validationUtils).validateEmail(maxLengthEmail);
    }

    @Test
    @DisplayName("Создание карты с минимально допустимой датой истечения")
    void createCard_MinimumExpiryDate() {
        // Given
        createRequest.setExpiryDate("01/25"); // Minimum valid date
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cardEncryptionUtil.isValidCardNumber(anyString())).thenReturn(true);
        when(cardEncryptionUtil.encryptCardNumber(anyString())).thenReturn("encrypted_card_number");
        when(cardEncryptionUtil.getMaskedNumberFromEncrypted(anyString())).thenReturn("**** **** **** 1234");
        when(bankCardRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(testCard);

        // When
        BankCardDto result = bankCardService.createCard(createRequest);

        // Then
        assertNotNull(result);
        verify(validationUtils).validateExpiryDate(any(LocalDate.class));
    }

    @Test
    @DisplayName("Пополнение карты с максимально допустимой суммой")
    void topupCard_MaximumAmount() {
        // Given
        double maxAmount = 999999.99;
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(testCard);

        // When
        bankCardService.topupCard(1L, maxAmount);

        // Then
        assertEquals(BigDecimal.valueOf(1009999.99), testCard.getBalance());
        verify(validationUtils).validateMinAmount(BigDecimal.valueOf(maxAmount), new BigDecimal("0.01"));
    }

    @Test
    @DisplayName("Пополнение карты с минимально допустимой суммой")
    void topupCard_MinimumAmount() {
        // Given
        double minAmount = 0.01;
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(testCard);

        // When
        bankCardService.topupCard(1L, minAmount);

        // Then
        assertEquals(BigDecimal.valueOf(1000.01), testCard.getBalance());
        verify(validationUtils).validateMinAmount(BigDecimal.valueOf(minAmount), new BigDecimal("0.01"));
    }

    @Test
    @DisplayName("Блокировка карты с максимально длинной причиной")
    void blockCard_MaximumLengthReason() {
        // Given
        String maxLengthReason = "a".repeat(1000); // Maximum allowed length
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(testCard);

        // When
        BankCardDto result = bankCardService.blockCard(1L, maxLengthReason);

        // Then
        assertNotNull(result);
        assertEquals(BankCard.Status.BLOCKED, testCard.getStatus());
        verify(validationUtils).validateDescription(maxLengthReason, "Причина блокировки");
    }

    @Test
    @DisplayName("Обработка ошибки при генерации номера карты")
    void createCard_CardNumberGenerationError() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cardEncryptionUtil.isValidCardNumber(anyString())).thenReturn(true);
        when(cardEncryptionUtil.encryptCardNumber(anyString())).thenThrow(new RuntimeException("Card number generation failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            bankCardService.createCard(createRequest);
        });
    }

    @Test
    @DisplayName("Обработка ошибки при шифровании номера карты")
    void createCard_EncryptionError() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cardEncryptionUtil.isValidCardNumber(anyString())).thenReturn(true);
        when(cardEncryptionUtil.encryptCardNumber(anyString())).thenThrow(new RuntimeException("Encryption failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            bankCardService.createCard(createRequest);
        });
    }

    @Test
    @DisplayName("Обработка ошибки базы данных при сохранении карты")
    void createCard_DatabaseError() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cardEncryptionUtil.isValidCardNumber(anyString())).thenReturn(true);
        when(cardEncryptionUtil.encryptCardNumber(anyString())).thenReturn("encrypted_card_number");
        when(cardEncryptionUtil.getMaskedNumberFromEncrypted(anyString())).thenReturn("**** **** **** 1234");
        when(bankCardRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(bankCardRepository.save(any(BankCard.class))).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            bankCardService.createCard(createRequest);
        });
    }

    // ========== TransferService Edge Cases ==========

    @Test
    @DisplayName("Перевод с максимально допустимой суммой")
    void transfer_MaximumAmount() {
        // Given
        BigDecimal maxAmount = BigDecimal.valueOf(999999.99);
        transferRequest.setAmount(maxAmount);
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(testCard2));
        when(transferRepository.save(any(Transfer.class))).thenReturn(new Transfer());
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(testCard, testCard2);

        // When
        TransferResponse result = transferService.transfer(transferRequest, testUser);

        // Then
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(maxAmount, result.getAmount());
    }

    @Test
    @DisplayName("Перевод с минимально допустимой суммой")
    void transfer_MinimumAmount() {
        // Given
        BigDecimal minAmount = BigDecimal.valueOf(0.01);
        transferRequest.setAmount(minAmount);
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(testCard2));
        when(transferRepository.save(any(Transfer.class))).thenReturn(new Transfer());
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(testCard, testCard2);

        // When
        TransferResponse result = transferService.transfer(transferRequest, testUser);

        // Then
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(minAmount, result.getAmount());
    }

    @Test
    @DisplayName("Перевод с максимально длинным описанием")
    void transfer_MaximumLengthDescription() {
        // Given
        String maxLengthDescription = "a".repeat(1000); // Maximum allowed length
        transferRequest.setDescription(maxLengthDescription);
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(testCard2));
        when(transferRepository.save(any(Transfer.class))).thenReturn(new Transfer());
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(testCard, testCard2);

        // When
        TransferResponse result = transferService.transfer(transferRequest, testUser);

        // Then
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(maxLengthDescription, result.getDescription());
    }

    @Test
    @DisplayName("Перевод с описанием на разных языках")
    void transfer_MultilingualDescription() {
        // Given
        String multilingualDescription = "Transfer: перевод, 转账, transfert, перевод";
        transferRequest.setDescription(multilingualDescription);
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(testCard2));
        when(transferRepository.save(any(Transfer.class))).thenReturn(new Transfer());
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(testCard, testCard2);

        // When
        TransferResponse result = transferService.transfer(transferRequest, testUser);

        // Then
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(multilingualDescription, result.getDescription());
    }

    @Test
    @DisplayName("Перевод с описанием содержащим специальные символы")
    void transfer_SpecialCharactersDescription() {
        // Given
        String specialCharsDescription = "Transfer: !@#$%^&*()_+-=[]{}|;':\",./<>?`~";
        transferRequest.setDescription(specialCharsDescription);
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(testCard2));
        when(transferRepository.save(any(Transfer.class))).thenReturn(new Transfer());
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(testCard, testCard2);

        // When
        TransferResponse result = transferService.transfer(transferRequest, testUser);

        // Then
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(specialCharsDescription, result.getDescription());
    }

    @Test
    @DisplayName("Перевод с высокой точностью суммы")
    void transfer_HighPrecisionAmount() {
        // Given
        BigDecimal highPrecisionAmount = BigDecimal.valueOf(123.456789);
        transferRequest.setAmount(highPrecisionAmount);
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(testCard2));
        when(transferRepository.save(any(Transfer.class))).thenReturn(new Transfer());
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(testCard, testCard2);

        // When
        TransferResponse result = transferService.transfer(transferRequest, testUser);

        // Then
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(highPrecisionAmount, result.getAmount());
    }

    @Test
    @DisplayName("Перевод с нулевой суммой")
    void transfer_ZeroAmount() {
        // Given
        transferRequest.setAmount(BigDecimal.ZERO);

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    @DisplayName("Перевод с отрицательной суммой")
    void transfer_NegativeAmount() {
        // Given
        transferRequest.setAmount(BigDecimal.valueOf(-100.00));

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    @DisplayName("Перевод с пустым описанием")
    void transfer_EmptyDescription() {
        // Given
        transferRequest.setDescription("");

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    @DisplayName("Перевод с null описанием")
    void transfer_NullDescription() {
        // Given
        transferRequest.setDescription(null);

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    @DisplayName("Перевод с описанием только из пробелов")
    void transfer_WhitespaceOnlyDescription() {
        // Given
        transferRequest.setDescription("   ");

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    @DisplayName("Перевод с описанием превышающим максимальную длину")
    void transfer_TooLongDescription() {
        // Given
        transferRequest.setDescription("a".repeat(1001)); // Exceeds maximum length

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    @DisplayName("Перевод с несуществующим ID карты отправителя")
    void transfer_NonExistentFromCard() {
        // Given
        when(bankCardRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    @DisplayName("Перевод с несуществующим ID карты получателя")
    void transfer_NonExistentToCard() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    @DisplayName("Перевод с одинаковыми картами")
    void transfer_SameCard() {
        // Given
        transferRequest.setToCardId(1L); // Same as fromCardId
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    @DisplayName("Перевод с заблокированной картой отправителя")
    void transfer_BlockedFromCard() {
        // Given
        testCard.setStatus(BankCard.Status.BLOCKED);
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(testCard2));

        // When & Then
        assertThrows(CardBlockedException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    @DisplayName("Перевод с заблокированной картой получателя")
    void transfer_BlockedToCard() {
        // Given
        testCard2.setStatus(BankCard.Status.BLOCKED);
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(testCard2));

        // When & Then
        assertThrows(CardBlockedException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    @DisplayName("Перевод с истекшей картой отправителя")
    void transfer_ExpiredFromCard() {
        // Given
        testCard.setExpiryDate(LocalDate.now().minusDays(1));
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(testCard2));

        // When & Then
        assertThrows(CardBlockedException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    @DisplayName("Перевод с истекшей картой получателя")
    void transfer_ExpiredToCard() {
        // Given
        testCard2.setExpiryDate(LocalDate.now().minusDays(1));
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(testCard2));

        // When & Then
        assertThrows(CardBlockedException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    @DisplayName("Перевод с недостаточными средствами")
    void transfer_InsufficientFunds() {
        // Given
        transferRequest.setAmount(BigDecimal.valueOf(1500.00)); // More than balance
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(testCard2));

        // When & Then
        assertThrows(InsufficientFundsException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    @DisplayName("Перевод с точной суммой баланса")
    void transfer_ExactBalance() {
        // Given
        transferRequest.setAmount(BigDecimal.valueOf(1000.00)); // Exact balance
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(testCard2));
        when(transferRepository.save(any(Transfer.class))).thenReturn(new Transfer());
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(testCard, testCard2);

        // When
        TransferResponse result = transferService.transfer(transferRequest, testUser);

        // Then
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(BigDecimal.ZERO, testCard.getBalance());
        assertEquals(BigDecimal.valueOf(1500.00), testCard2.getBalance());
    }

    @Test
    @DisplayName("Перевод с ошибкой базы данных")
    void transfer_DatabaseError() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(testCard2));
        when(transferRepository.save(any(Transfer.class))).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    @DisplayName("Перевод с частичной ошибкой сохранения")
    void transfer_PartialSaveError() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(testCard2));
        when(transferRepository.save(any(Transfer.class))).thenReturn(new Transfer());
        when(bankCardRepository.save(testCard)).thenReturn(testCard);
        when(bankCardRepository.save(testCard2)).thenThrow(new RuntimeException("Failed to save to card"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });

        // Verify transfer was marked as failed
        verify(transferRepository, atLeastOnce()).save(any(Transfer.class));
    }

    @Test
    @DisplayName("Перевод с неверным ID карты")
    void transfer_InvalidCardId() {
        // Given
        transferRequest.setFromCardId(0L); // Invalid ID

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    @DisplayName("Перевод с неверным ID карты получателя")
    void transfer_InvalidToCardId() {
        // Given
        transferRequest.setToCardId(-1L); // Invalid ID

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    @DisplayName("Перевод с null суммой")
    void transfer_NullAmount() {
        // Given
        transferRequest.setAmount(null);

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    @DisplayName("Перевод с суммой превышающей максимальную")
    void transfer_TooLargeAmount() {
        // Given
        transferRequest.setAmount(BigDecimal.valueOf(1000000.00)); // Exceeds maximum

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }
}
