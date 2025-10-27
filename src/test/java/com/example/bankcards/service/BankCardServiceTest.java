package com.example.bankcards.service;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.CreateBankCardRequest;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardEncryptionUtil;
import com.example.bankcards.util.ValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankCardServiceTest {

    @Mock
    private BankCardRepository bankCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardEncryptionUtil cardEncryptionUtil;

    @Mock
    private ValidationUtils validationUtils;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private BankCardService bankCardService;

    private User testUser;
    private BankCard testCard;
    private CreateBankCardRequest createRequest;

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

        createRequest = new CreateBankCardRequest();
        createRequest.setOwnerEmail("test@example.com");
        createRequest.setExpiryDate("12/26");
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

    @Test
    void createCard_Success() {
        // Given
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
        assertEquals("**** **** **** 1234", result.getMaskedNumber());
        assertEquals(BigDecimal.valueOf(1000.00), result.getBalance());
        assertEquals(BankCard.Status.ACTIVE, result.getStatus());
        
        verify(validationUtils).validateEmail("test@example.com");
        verify(validationUtils).validateExpiryDate(any(LocalDate.class));
        verify(cardEncryptionUtil).encryptCardNumber(anyString());
        verify(bankCardRepository).save(any(BankCard.class));
        verify(auditService).logCardCreation(eq(testUser), eq(1L), eq("**** **** **** 1234"));
    }

    @Test
    void createCard_UserNotFound() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bankCardService.createCard(createRequest);
        });
    }

    @Test
    void createCard_DuplicateCardNumber() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cardEncryptionUtil.isValidCardNumber(anyString())).thenReturn(true);
        when(cardEncryptionUtil.encryptCardNumber(anyString())).thenReturn("encrypted_card_number");
        when(bankCardRepository.existsByCardNumber(anyString())).thenReturn(true);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            bankCardService.createCard(createRequest);
        });
    }

    @Test
    void topupCard_Success() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(testCard);

        // When
        bankCardService.topupCard(1L, 500.0);

        // Then
        assertEquals(BigDecimal.valueOf(1500.00), testCard.getBalance());
        verify(validationUtils).validateId(1L, "карты");
        verify(validationUtils).validateMinAmount(BigDecimal.valueOf(500.0), new BigDecimal("0.01"));
        verify(bankCardRepository).save(testCard);
        verify(auditService).logCardTopup(eq(testUser), eq(1L), eq("**** **** **** 1234"), eq(500.0));
    }

    @Test
    void topupCard_CardNotFound() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bankCardService.topupCard(1L, 500.0);
        });
    }

    @Test
    void topupCard_CardNotActive() {
        // Given
        testCard.setStatus(BankCard.Status.BLOCKED);
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(CardBlockedException.class, () -> {
            bankCardService.topupCard(1L, 500.0);
        });
    }

    @Test
    void blockCard_Success() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(testCard);

        // When
        BankCardDto result = bankCardService.blockCard(1L, "Test reason");

        // Then
        assertNotNull(result);
        assertEquals(BankCard.Status.BLOCKED, testCard.getStatus());
        verify(validationUtils).validateId(1L, "карты");
        verify(validationUtils).validateDescription("Test reason", "Причина блокировки");
        verify(bankCardRepository).save(testCard);
        verify(auditService).logCardBlock(eq(testUser), eq(1L), eq("**** **** **** 1234"), eq("Test reason"));
    }

    @Test
    void blockCard_AlreadyBlocked() {
        // Given
        testCard.setStatus(BankCard.Status.BLOCKED);
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(BusinessException.class, () -> {
            bankCardService.blockCard(1L, "Test reason");
        });
    }

    @Test
    void activateCard_Success() {
        // Given
        testCard.setStatus(BankCard.Status.BLOCKED);
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(testCard);

        // When
        BankCardDto result = bankCardService.activateCard(1L);

        // Then
        assertNotNull(result);
        assertEquals(BankCard.Status.ACTIVE, testCard.getStatus());
        verify(validationUtils).validateId(1L, "карты");
        verify(bankCardRepository).save(testCard);
        verify(auditService).logCardActivation(eq(testUser), eq(1L), eq("**** **** **** 1234"));
    }

    @Test
    void activateCard_AlreadyActive() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(BusinessException.class, () -> {
            bankCardService.activateCard(1L);
        });
    }

    @Test
    void deleteCard_Success() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // When
        bankCardService.deleteCard(1L);

        // Then
        verify(validationUtils).validateId(1L, "карты");
        verify(bankCardRepository).deleteById(1L);
        verify(auditService).logCardDeletion(eq(testUser), eq(1L), eq("**** **** **** 1234"));
    }

    @Test
    void deleteCard_NotFound() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bankCardService.deleteCard(1L);
        });
    }

    @Test
    void findByOwner_Success() {
        // Given
        List<BankCard> cards = Arrays.asList(testCard);
        Page<BankCard> cardPage = new PageImpl<>(cards);
        when(bankCardRepository.findByOwner(testUser, PageRequest.of(0, 10))).thenReturn(cardPage);
        when(bankCardRepository.findExpiredCards()).thenReturn(Arrays.asList());

        // When
        Page<BankCardDto> result = bankCardService.findByOwner(testUser, PageRequest.of(0, 10));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("**** **** **** 1234", result.getContent().get(0).getMaskedNumber());
    }

    @Test
    void findByOwnerWithFilters_Success() {
        // Given
        List<BankCard> cards = Arrays.asList(testCard);
        Page<BankCard> cardPage = new PageImpl<>(cards);
        when(bankCardRepository.findByOwnerWithFilters(eq(testUser), eq(BankCard.Status.ACTIVE), anyString(), any(Pageable.class)))
                .thenReturn(cardPage);
        when(bankCardRepository.findExpiredCards()).thenReturn(Arrays.asList());

        // When
        Page<BankCardDto> result = bankCardService.findByOwnerWithFilters(testUser, BankCard.Status.ACTIVE, "1234", PageRequest.of(0, 10));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void findAllWithFilters_Success() {
        // Given
        List<BankCard> cards = Arrays.asList(testCard);
        Page<BankCard> cardPage = new PageImpl<>(cards);
        when(bankCardRepository.findAllWithFilters(eq(BankCard.Status.ACTIVE), anyString(), any(Pageable.class)))
                .thenReturn(cardPage);
        when(bankCardRepository.findExpiredCards()).thenReturn(Arrays.asList());

        // When
        Page<BankCardDto> result = bankCardService.findAllWithFilters(BankCard.Status.ACTIVE, "1234", PageRequest.of(0, 10));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void findActiveCardsForUser_Success() {
        // Given
        List<BankCard> cards = Arrays.asList(testCard);
        when(bankCardRepository.findActiveCardsForUser(testUser)).thenReturn(cards);
        when(bankCardRepository.findExpiredCards()).thenReturn(Arrays.asList());

        // When
        List<BankCardDto> result = bankCardService.findActiveCardsForUser(testUser);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("**** **** **** 1234", result.get(0).getMaskedNumber());
    }

    @Test
    void updateExpiredCards_Success() {
        // Given
        BankCard expiredCard = new BankCard();
        expiredCard.setId(2L);
        expiredCard.setStatus(BankCard.Status.ACTIVE);
        expiredCard.setExpiryDate(LocalDate.now().minusDays(1));
        
        when(bankCardRepository.findExpiredCards()).thenReturn(Arrays.asList(expiredCard));
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(expiredCard);

        // When
        int result = bankCardService.updateExpiredCards();

        // Then
        assertEquals(1, result);
        assertEquals(BankCard.Status.EXPIRED, expiredCard.getStatus());
        verify(bankCardRepository).save(expiredCard);
    }

    @Test
    void updateExpiredCards_NoExpiredCards() {
        // Given
        when(bankCardRepository.findExpiredCards()).thenReturn(Arrays.asList());

        // When
        int result = bankCardService.updateExpiredCards();

        // Then
        assertEquals(0, result);
        verify(bankCardRepository, never()).save(any(BankCard.class));
    }

    @Test
    void createCard_InvalidEmail() {
        // Given
        createRequest.setOwnerEmail("invalid-email");

        // When & Then
        assertThrows(ValidationException.class, () -> {
            bankCardService.createCard(createRequest);
        });
    }

    @Test
    void createCard_InvalidExpiryDate() {
        // Given
        createRequest.setExpiryDate("13/25"); // Invalid month

        // When & Then
        assertThrows(ValidationException.class, () -> {
            bankCardService.createCard(createRequest);
        });
    }

    @Test
    void createCard_PastExpiryDate() {
        // Given
        createRequest.setExpiryDate("01/20"); // Past date

        // When & Then
        assertThrows(ValidationException.class, () -> {
            bankCardService.createCard(createRequest);
        });
    }

    @Test
    void topupCard_InvalidAmount() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(ValidationException.class, () -> {
            bankCardService.topupCard(1L, -100.0);
        });
    }

    @Test
    void topupCard_ZeroAmount() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(ValidationException.class, () -> {
            bankCardService.topupCard(1L, 0.0);
        });
    }

    @Test
    void topupCard_VerySmallAmount() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(testCard);

        // When
        bankCardService.topupCard(1L, 0.01);

        // Then
        assertEquals(BigDecimal.valueOf(1000.01), testCard.getBalance());
    }

    @Test
    void blockCard_EmptyReason() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(ValidationException.class, () -> {
            bankCardService.blockCard(1L, "");
        });
    }

    @Test
    void blockCard_NullReason() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(ValidationException.class, () -> {
            bankCardService.blockCard(1L, null);
        });
    }

    @Test
    void blockCard_VeryLongReason() {
        // Given
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(ValidationException.class, () -> {
            bankCardService.blockCard(1L, "a".repeat(1001)); // Too long
        });
    }

    @Test
    void findByOwnerWithFilters_EmptySearch() {
        // Given
        List<BankCard> cards = Arrays.asList(testCard);
        Page<BankCard> cardPage = new PageImpl<>(cards);
        when(bankCardRepository.findByOwnerWithFilters(eq(testUser), eq(BankCard.Status.ACTIVE), eq(""), any(Pageable.class)))
                .thenReturn(cardPage);
        when(bankCardRepository.findExpiredCards()).thenReturn(Arrays.asList());

        // When
        Page<BankCardDto> result = bankCardService.findByOwnerWithFilters(testUser, BankCard.Status.ACTIVE, "", PageRequest.of(0, 10));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void findAllWithFilters_AllStatuses() {
        // Given
        List<BankCard> cards = Arrays.asList(testCard);
        Page<BankCard> cardPage = new PageImpl<>(cards);
        when(bankCardRepository.findAllWithFilters(eq(null), anyString(), any(Pageable.class)))
                .thenReturn(cardPage);
        when(bankCardRepository.findExpiredCards()).thenReturn(Arrays.asList());

        // When
        Page<BankCardDto> result = bankCardService.findAllWithFilters(null, "1234", PageRequest.of(0, 10));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void findActiveCardsForUser_NoActiveCards() {
        // Given
        when(bankCardRepository.findActiveCardsForUser(testUser)).thenReturn(Arrays.asList());
        when(bankCardRepository.findExpiredCards()).thenReturn(Arrays.asList());

        // When
        List<BankCardDto> result = bankCardService.findActiveCardsForUser(testUser);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateExpiredCards_MultipleExpiredCards() {
        // Given
        BankCard expiredCard1 = new BankCard();
        expiredCard1.setId(2L);
        expiredCard1.setStatus(BankCard.Status.ACTIVE);
        expiredCard1.setExpiryDate(LocalDate.now().minusDays(1));

        BankCard expiredCard2 = new BankCard();
        expiredCard2.setId(3L);
        expiredCard2.setStatus(BankCard.Status.ACTIVE);
        expiredCard2.setExpiryDate(LocalDate.now().minusDays(2));

        when(bankCardRepository.findExpiredCards()).thenReturn(Arrays.asList(expiredCard1, expiredCard2));
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(expiredCard1, expiredCard2);

        // When
        int result = bankCardService.updateExpiredCards();

        // Then
        assertEquals(2, result);
        assertEquals(BankCard.Status.EXPIRED, expiredCard1.getStatus());
        assertEquals(BankCard.Status.EXPIRED, expiredCard2.getStatus());
        verify(bankCardRepository, times(2)).save(any(BankCard.class));
    }

    @Test
    void createCard_GenerateValidCardNumber() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cardEncryptionUtil.isValidCardNumber("1234567890123456")).thenReturn(false);
        when(cardEncryptionUtil.isValidCardNumber(anyString())).thenReturn(true);
        when(cardEncryptionUtil.encryptCardNumber(anyString())).thenReturn("encrypted_card_number");
        when(cardEncryptionUtil.getMaskedNumberFromEncrypted(anyString())).thenReturn("**** **** **** 1234");
        when(bankCardRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(testCard);

        // When
        BankCardDto result = bankCardService.createCard(createRequest);

        // Then
        assertNotNull(result);
        verify(cardEncryptionUtil).isValidCardNumber("1234567890123456");
        verify(cardEncryptionUtil).isValidCardNumber(anyString());
    }
}
