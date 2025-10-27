package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.util.ValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private BankCardRepository bankCardRepository;

    @Mock
    private ValidationUtils validationUtils;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TransferService transferService;

    private User testUser;
    private BankCard fromCard;
    private BankCard toCard;
    private TransferRequest transferRequest;
    private Transfer testTransfer;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(User.Role.USER);

        fromCard = new BankCard();
        fromCard.setId(1L);
        fromCard.setMaskedNumber("**** **** **** 1234");
        fromCard.setBalance(BigDecimal.valueOf(1000.00));
        fromCard.setStatus(BankCard.Status.ACTIVE);
        fromCard.setExpiryDate(LocalDate.now().plusYears(2));
        fromCard.setOwner(testUser);

        toCard = new BankCard();
        toCard.setId(2L);
        toCard.setMaskedNumber("**** **** **** 5678");
        toCard.setBalance(BigDecimal.valueOf(500.00));
        toCard.setStatus(BankCard.Status.ACTIVE);
        toCard.setExpiryDate(LocalDate.now().plusYears(2));
        toCard.setOwner(testUser);

        transferRequest = new TransferRequest();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(BigDecimal.valueOf(200.00));
        transferRequest.setDescription("Test transfer");

        testTransfer = new Transfer();
        testTransfer.setId(1L);
        testTransfer.setFromCard(fromCard);
        testTransfer.setToCard(toCard);
        testTransfer.setAmount(BigDecimal.valueOf(200.00));
        testTransfer.setDescription("Test transfer");
        testTransfer.setStatus(Transfer.Status.PENDING);
        testTransfer.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void transfer_Success() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(transferRepository.save(any(Transfer.class))).thenReturn(testTransfer);
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(fromCard, toCard);
        when(transferRepository.save(any(Transfer.class))).thenReturn(testTransfer);

        // When
        TransferResponse result = transferService.transfer(transferRequest, testUser);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getFromCardId());
        assertEquals(2L, result.getToCardId());
        assertEquals(BigDecimal.valueOf(200.00), result.getAmount());
        assertEquals("Test transfer", result.getDescription());
        assertEquals("COMPLETED", result.getStatus());

        // Verify balances were updated
        assertEquals(BigDecimal.valueOf(800.00), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(700.00), toCard.getBalance());

        verify(validationUtils).validateId(1L, "карты отправителя");
        verify(validationUtils).validateId(2L, "карты получателя");
        verify(validationUtils).validateMinAmount(BigDecimal.valueOf(200.00), new BigDecimal("0.01"));
        verify(validationUtils).validateDescription("Test transfer", "Описание перевода");
        verify(transferRepository).save(any(Transfer.class));
        verify(bankCardRepository).save(fromCard);
        verify(bankCardRepository).save(toCard);
        verify(auditService).logTransfer(eq(testUser), eq(1L), eq("**** **** **** 1234"), eq("**** **** **** 5678"), eq(200.0));
    }

    @Test
    void transfer_FromCardNotFound() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_ToCardNotFound() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_SameCard() {
        // Given
        transferRequest.setToCardId(1L); // Same as fromCardId
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_FromCardBlocked() {
        // Given
        fromCard.setStatus(BankCard.Status.BLOCKED);
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // When & Then
        assertThrows(CardBlockedException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_ToCardBlocked() {
        // Given
        toCard.setStatus(BankCard.Status.BLOCKED);
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // When & Then
        assertThrows(CardBlockedException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_InsufficientFunds() {
        // Given
        transferRequest.setAmount(BigDecimal.valueOf(1500.00)); // More than balance
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // When & Then
        assertThrows(InsufficientFundsException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_AccessDenied_FromCard() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        fromCard.setOwner(otherUser);
        
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // When & Then
        assertThrows(BusinessException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_AccessDenied_ToCard() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        toCard.setOwner(otherUser);
        
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // When & Then
        assertThrows(BusinessException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_TransferFails_Rollback() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(transferRepository.save(any(Transfer.class))).thenReturn(testTransfer);
        when(bankCardRepository.save(fromCard)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });

        // Verify transfer was marked as failed
        verify(transferRepository, atLeastOnce()).save(any(Transfer.class));
    }

    @Test
    void transfer_ZeroAmount() {
        // Given
        transferRequest.setAmount(BigDecimal.ZERO);

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_NegativeAmount() {
        // Given
        transferRequest.setAmount(BigDecimal.valueOf(-100.00));

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_EmptyDescription() {
        // Given
        transferRequest.setDescription("");

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_NullDescription() {
        // Given
        transferRequest.setDescription(null);

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_ExactBalance() {
        // Given
        transferRequest.setAmount(BigDecimal.valueOf(1000.00)); // Exact balance
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(transferRepository.save(any(Transfer.class))).thenReturn(testTransfer);
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(fromCard, toCard);

        // When
        TransferResponse result = transferService.transfer(transferRequest, testUser);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(1500.00), toCard.getBalance());
    }

    @Test
    void transfer_ExpiredCard() {
        // Given
        fromCard.setExpiryDate(LocalDate.now().minusDays(1));
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // When & Then
        assertThrows(CardBlockedException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_ExpiredToCard() {
        // Given
        toCard.setExpiryDate(LocalDate.now().minusDays(1));
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // When & Then
        assertThrows(CardBlockedException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_MaximumAmount() {
        // Given
        transferRequest.setAmount(BigDecimal.valueOf(999999.99)); // Large amount
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(transferRepository.save(any(Transfer.class))).thenReturn(testTransfer);
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(fromCard, toCard);

        // When
        TransferResponse result = transferService.transfer(transferRequest, testUser);

        // Then
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(BigDecimal.valueOf(999999.99), result.getAmount());
    }

    @Test
    void transfer_ConcurrentTransferAttempts() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(transferRepository.save(any(Transfer.class))).thenReturn(testTransfer);
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(fromCard, toCard);

        // When - First transfer
        TransferResponse result1 = transferService.transfer(transferRequest, testUser);
        
        // Reset balance for second transfer
        fromCard.setBalance(BigDecimal.valueOf(1000.00));
        toCard.setBalance(BigDecimal.valueOf(500.00));
        
        // When - Second transfer
        TransferResponse result2 = transferService.transfer(transferRequest, testUser);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals("COMPLETED", result1.getStatus());
        assertEquals("COMPLETED", result2.getStatus());
    }

    @Test
    void transfer_InvalidFromCardId() {
        // Given
        transferRequest.setFromCardId(0L); // Invalid ID

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_InvalidToCardId() {
        // Given
        transferRequest.setToCardId(-1L); // Invalid ID

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_NullAmount() {
        // Given
        transferRequest.setAmount(null);

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_TooLargeAmount() {
        // Given
        transferRequest.setAmount(BigDecimal.valueOf(1000000.00)); // Very large amount

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_WhitespaceDescription() {
        // Given
        transferRequest.setDescription("   "); // Only whitespace

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_VeryLongDescription() {
        // Given
        transferRequest.setDescription("a".repeat(1001)); // Too long

        // When & Then
        assertThrows(ValidationException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_SpecialCharactersInDescription() {
        // Given
        transferRequest.setDescription("Transfer with special chars: !@#$%^&*()");
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(transferRepository.save(any(Transfer.class))).thenReturn(testTransfer);
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(fromCard, toCard);

        // When
        TransferResponse result = transferService.transfer(transferRequest, testUser);

        // Then
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals("Transfer with special chars: !@#$%^&*()", result.getDescription());
    }

    @Test
    void transfer_UnicodeDescription() {
        // Given
        transferRequest.setDescription("Перевод с кириллицей и эмодзи 🏦💰");
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(transferRepository.save(any(Transfer.class))).thenReturn(testTransfer);
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(fromCard, toCard);

        // When
        TransferResponse result = transferService.transfer(transferRequest, testUser);

        // Then
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals("Перевод с кириллицей и эмодзи 🏦💰", result.getDescription());
    }

    @Test
    void transfer_DatabaseErrorDuringSave() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(transferRepository.save(any(Transfer.class))).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });
    }

    @Test
    void transfer_PartialFailure_Rollback() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(transferRepository.save(any(Transfer.class))).thenReturn(testTransfer);
        when(bankCardRepository.save(fromCard)).thenReturn(fromCard);
        when(bankCardRepository.save(toCard)).thenThrow(new RuntimeException("Failed to save to card"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            transferService.transfer(transferRequest, testUser);
        });

        // Verify transfer was marked as failed
        verify(transferRepository, atLeastOnce()).save(any(Transfer.class));
    }

    @Test
    void transfer_EdgeCase_OneCent() {
        // Given
        transferRequest.setAmount(BigDecimal.valueOf(0.01));
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(transferRepository.save(any(Transfer.class))).thenReturn(testTransfer);
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(fromCard, toCard);

        // When
        TransferResponse result = transferService.transfer(transferRequest, testUser);

        // Then
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(BigDecimal.valueOf(0.01), result.getAmount());
        assertEquals(BigDecimal.valueOf(999.99), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(500.01), toCard.getBalance());
    }

    @Test
    void transfer_EdgeCase_MaximumPrecision() {
        // Given
        transferRequest.setAmount(BigDecimal.valueOf(123.456789)); // High precision
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(bankCardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(transferRepository.save(any(Transfer.class))).thenReturn(testTransfer);
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(fromCard, toCard);

        // When
        TransferResponse result = transferService.transfer(transferRequest, testUser);

        // Then
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(BigDecimal.valueOf(123.456789), result.getAmount());
    }
}
