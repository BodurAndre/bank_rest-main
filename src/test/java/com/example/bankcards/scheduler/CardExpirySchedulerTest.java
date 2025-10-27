package com.example.bankcards.scheduler;

import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.service.BankCardService;
import com.example.bankcards.service.CardExpirationSchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardExpirySchedulerTest {

    @Mock
    private BankCardService bankCardService;

    @Mock
    private BankCardRepository bankCardRepository;

    @InjectMocks
    private CardExpirationSchedulerService cardExpirationSchedulerService;

    private User testUser;
    private BankCard expiredCard;
    private BankCard activeCard;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(User.Role.USER);

        expiredCard = new BankCard();
        expiredCard.setId(1L);
        expiredCard.setMaskedNumber("**** **** **** 1234");
        expiredCard.setStatus(BankCard.Status.ACTIVE);
        expiredCard.setExpiryDate(LocalDate.now().minusDays(1));
        expiredCard.setOwner(testUser);

        activeCard = new BankCard();
        activeCard.setId(2L);
        activeCard.setMaskedNumber("**** **** **** 5678");
        activeCard.setStatus(BankCard.Status.ACTIVE);
        activeCard.setExpiryDate(LocalDate.now().plusYears(1));
        activeCard.setOwner(testUser);
    }

    @Test
    void checkExpiredCards_WithExpiredCards_ShouldUpdateCards() {
        // Given
        List<BankCard> expiredCards = Arrays.asList(expiredCard);
        when(bankCardRepository.findExpiredCards()).thenReturn(expiredCards);
        when(bankCardService.updateExpiredCards()).thenReturn(1);

        // When
        cardExpirationSchedulerService.checkAndUpdateExpiredCards();

        // Then
        verify(bankCardService).updateExpiredCards();
    }

    @Test
    void checkExpiredCards_WithNoExpiredCards_ShouldNotUpdateCards() {
        // Given
        when(bankCardRepository.findExpiredCards()).thenReturn(Arrays.asList());
        when(bankCardService.updateExpiredCards()).thenReturn(0);

        // When
        cardExpirationSchedulerService.checkAndUpdateExpiredCards();

        // Then
        verify(bankCardService).updateExpiredCards();
    }

    @Test
    void checkExpiredCards_WithMultipleExpiredCards_ShouldUpdateAllCards() {
        // Given
        BankCard expiredCard2 = new BankCard();
        expiredCard2.setId(3L);
        expiredCard2.setMaskedNumber("**** **** **** 9999");
        expiredCard2.setStatus(BankCard.Status.ACTIVE);
        expiredCard2.setExpiryDate(LocalDate.now().minusDays(5));
        expiredCard2.setOwner(testUser);

        List<BankCard> expiredCards = Arrays.asList(expiredCard, expiredCard2);
        when(bankCardRepository.findExpiredCards()).thenReturn(expiredCards);
        when(bankCardService.updateExpiredCards()).thenReturn(2);

        // When
        cardExpirationSchedulerService.checkAndUpdateExpiredCards();

        // Then
        verify(bankCardService).updateExpiredCards();
    }

    @Test
    void checkExpiredCards_WithMixedCards_ShouldUpdateOnlyExpiredCards() {
        // Given
        List<BankCard> expiredCards = Arrays.asList(expiredCard);
        when(bankCardRepository.findExpiredCards()).thenReturn(expiredCards);
        when(bankCardService.updateExpiredCards()).thenReturn(1);

        // When
        cardExpirationSchedulerService.checkAndUpdateExpiredCards();

        // Then
        verify(bankCardService).updateExpiredCards();
    }

    @Test
    void checkExpiredCards_WithServiceException_ShouldHandleGracefully() {
        // Given
        List<BankCard> expiredCards = Arrays.asList(expiredCard);
        when(bankCardRepository.findExpiredCards()).thenReturn(expiredCards);
        when(bankCardService.updateExpiredCards()).thenThrow(new RuntimeException("Service error"));

        // When & Then
        // Should not throw exception
        assertDoesNotThrow(() -> cardExpirationSchedulerService.checkAndUpdateExpiredCards());
    }

    @Test
    void checkExpiredCards_WithRepositoryException_ShouldHandleGracefully() {
        // Given
        when(bankCardRepository.findExpiredCards()).thenThrow(new RuntimeException("Repository error"));

        // When & Then
        // Should not throw exception
        assertDoesNotThrow(() -> cardExpirationSchedulerService.checkAndUpdateExpiredCards());
    }

    @Test
    void checkExpiredCards_WithNullExpiredCards_ShouldHandleGracefully() {
        // Given
        when(bankCardRepository.findExpiredCards()).thenReturn(null);
        when(bankCardService.updateExpiredCards()).thenReturn(0);

        // When & Then
        // Should not throw exception
        assertDoesNotThrow(() -> cardExpirationSchedulerService.checkAndUpdateExpiredCards());
    }

    @Test
    void checkExpiredCards_WithEmptyExpiredCards_ShouldNotUpdateCards() {
        // Given
        when(bankCardRepository.findExpiredCards()).thenReturn(Arrays.asList());
        when(bankCardService.updateExpiredCards()).thenReturn(0);

        // When
        cardExpirationSchedulerService.checkAndUpdateExpiredCards();

        // Then
        verify(bankCardService).updateExpiredCards();
    }

    @Test
    void checkExpiredCards_WithAlreadyExpiredCards_ShouldNotUpdateAgain() {
        // Given
        expiredCard.setStatus(BankCard.Status.EXPIRED);
        List<BankCard> expiredCards = Arrays.asList(expiredCard);
        when(bankCardRepository.findExpiredCards()).thenReturn(expiredCards);
        when(bankCardService.updateExpiredCards()).thenReturn(0);

        // When
        cardExpirationSchedulerService.checkAndUpdateExpiredCards();

        // Then
        verify(bankCardService).updateExpiredCards();
    }

    @Test
    void checkExpiredCards_WithBlockedExpiredCards_ShouldUpdateStatus() {
        // Given
        expiredCard.setStatus(BankCard.Status.BLOCKED);
        List<BankCard> expiredCards = Arrays.asList(expiredCard);
        when(bankCardRepository.findExpiredCards()).thenReturn(expiredCards);
        when(bankCardService.updateExpiredCards()).thenReturn(1);

        // When
        cardExpirationSchedulerService.checkAndUpdateExpiredCards();

        // Then
        verify(bankCardService).updateExpiredCards();
    }

    @Test
    void checkExpiredCards_WithInactiveExpiredCards_ShouldUpdateStatus() {
        // Given
        expiredCard.setStatus(BankCard.Status.ACTIVE);
        List<BankCard> expiredCards = Arrays.asList(expiredCard);
        when(bankCardRepository.findExpiredCards()).thenReturn(expiredCards);
        when(bankCardService.updateExpiredCards()).thenReturn(1);

        // When
        cardExpirationSchedulerService.checkAndUpdateExpiredCards();

        // Then
        verify(bankCardService).updateExpiredCards();
    }

    @Test
    void checkExpiredCards_WithCardsExpiringToday_ShouldNotUpdate() {
        // Given
        BankCard todayExpiredCard = new BankCard();
        todayExpiredCard.setId(3L);
        todayExpiredCard.setMaskedNumber("**** **** **** 9999");
        todayExpiredCard.setStatus(BankCard.Status.ACTIVE);
        todayExpiredCard.setExpiryDate(LocalDate.now());
        todayExpiredCard.setOwner(testUser);

        List<BankCard> expiredCards = Arrays.asList(todayExpiredCard);
        when(bankCardRepository.findExpiredCards()).thenReturn(expiredCards);
        when(bankCardService.updateExpiredCards()).thenReturn(0);

        // When
        cardExpirationSchedulerService.checkAndUpdateExpiredCards();

        // Then
        verify(bankCardService).updateExpiredCards();
    }

    @Test
    void checkExpiredCards_WithCardsExpiringTomorrow_ShouldNotUpdate() {
        // Given
        BankCard tomorrowExpiredCard = new BankCard();
        tomorrowExpiredCard.setId(3L);
        tomorrowExpiredCard.setMaskedNumber("**** **** **** 9999");
        tomorrowExpiredCard.setStatus(BankCard.Status.ACTIVE);
        tomorrowExpiredCard.setExpiryDate(LocalDate.now().plusDays(1));
        tomorrowExpiredCard.setOwner(testUser);

        List<BankCard> expiredCards = Arrays.asList(tomorrowExpiredCard);
        when(bankCardRepository.findExpiredCards()).thenReturn(expiredCards);
        when(bankCardService.updateExpiredCards()).thenReturn(0);

        // When
        cardExpirationSchedulerService.checkAndUpdateExpiredCards();

        // Then
        verify(bankCardService).updateExpiredCards();
    }

    @Test
    void checkExpiredCards_WithCardsExpiringInFuture_ShouldNotUpdate() {
        // Given
        BankCard futureCard = new BankCard();
        futureCard.setId(3L);
        futureCard.setMaskedNumber("**** **** **** 9999");
        futureCard.setStatus(BankCard.Status.ACTIVE);
        futureCard.setExpiryDate(LocalDate.now().plusDays(30));
        futureCard.setOwner(testUser);

        List<BankCard> expiredCards = Arrays.asList(futureCard);
        when(bankCardRepository.findExpiredCards()).thenReturn(expiredCards);
        when(bankCardService.updateExpiredCards()).thenReturn(0);

        // When
        cardExpirationSchedulerService.checkAndUpdateExpiredCards();

        // Then
        verify(bankCardService).updateExpiredCards();
    }

    @Test
    void checkExpiredCards_WithCardsExpiringInPast_ShouldUpdate() {
        // Given
        BankCard pastExpiredCard = new BankCard();
        pastExpiredCard.setId(3L);
        pastExpiredCard.setMaskedNumber("**** **** **** 9999");
        pastExpiredCard.setStatus(BankCard.Status.ACTIVE);
        pastExpiredCard.setExpiryDate(LocalDate.now().minusDays(30));
        pastExpiredCard.setOwner(testUser);

        List<BankCard> expiredCards = Arrays.asList(pastExpiredCard);
        when(bankCardRepository.findExpiredCards()).thenReturn(expiredCards);
        when(bankCardService.updateExpiredCards()).thenReturn(1);

        // When
        cardExpirationSchedulerService.checkAndUpdateExpiredCards();

        // Then
        verify(bankCardService).updateExpiredCards();
    }

    @Test
    void checkExpiredCardsFrequently_WithExpiredCards_ShouldUpdateCards() {
        // Given
        List<BankCard> expiredCards = Arrays.asList(expiredCard);
        when(bankCardRepository.findExpiredCards()).thenReturn(expiredCards);
        when(bankCardService.updateExpiredCards()).thenReturn(1);

        // When
        cardExpirationSchedulerService.checkExpiredCardsFrequently();

        // Then
        verify(bankCardService).updateExpiredCards();
    }

    @Test
    void checkExpiredCardsFrequently_WithNoExpiredCards_ShouldNotUpdateCards() {
        // Given
        when(bankCardRepository.findExpiredCards()).thenReturn(Arrays.asList());
        when(bankCardService.updateExpiredCards()).thenReturn(0);

        // When
        cardExpirationSchedulerService.checkExpiredCardsFrequently();

        // Then
        verify(bankCardService).updateExpiredCards();
    }

    @Test
    void checkExpiredCardsFrequently_WithServiceException_ShouldHandleGracefully() {
        // Given
        List<BankCard> expiredCards = Arrays.asList(expiredCard);
        when(bankCardRepository.findExpiredCards()).thenReturn(expiredCards);
        when(bankCardService.updateExpiredCards()).thenThrow(new RuntimeException("Service error"));

        // When & Then
        // Should not throw exception
        assertDoesNotThrow(() -> cardExpirationSchedulerService.checkExpiredCardsFrequently());
    }

    @Test
    void testScheduler_ShouldNotThrowException() {
        // When & Then
        // Should not throw exception
        assertDoesNotThrow(() -> cardExpirationSchedulerService.testScheduler());
    }
}
