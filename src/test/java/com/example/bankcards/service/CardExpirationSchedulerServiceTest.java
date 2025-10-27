package com.example.bankcards.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Unit тесты для CardExpirationSchedulerService
 */
@ExtendWith(MockitoExtension.class)
class CardExpirationSchedulerServiceTest {

    @Mock
    private BankCardService bankCardService;

    @InjectMocks
    private CardExpirationSchedulerService schedulerService;

    @BeforeEach
    void setUp() {
        // Настройка перед каждым тестом
    }

    @Test
    void testCheckAndUpdateExpiredCards_WithExpiredCards() {
        // Arrange
        when(bankCardService.updateExpiredCards()).thenReturn(3);

        // Act
        schedulerService.checkAndUpdateExpiredCards();

        // Assert
        verify(bankCardService, times(1)).updateExpiredCards();
    }

    @Test
    void testCheckAndUpdateExpiredCards_NoExpiredCards() {
        // Arrange
        when(bankCardService.updateExpiredCards()).thenReturn(0);

        // Act
        schedulerService.checkAndUpdateExpiredCards();

        // Assert
        verify(bankCardService, times(1)).updateExpiredCards();
    }

    @Test
    void testCheckAndUpdateExpiredCards_WithException() {
        // Arrange
        when(bankCardService.updateExpiredCards()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert - метод не должен выбрасывать исключение, а только логировать
        schedulerService.checkAndUpdateExpiredCards();

        // Verify that the service method was called despite the exception
        verify(bankCardService, times(1)).updateExpiredCards();
    }

    @Test
    void testCheckExpiredCardsFrequently_WithExpiredCards() {
        // Arrange
        when(bankCardService.updateExpiredCards()).thenReturn(2);

        // Act
        schedulerService.checkExpiredCardsFrequently();

        // Assert
        verify(bankCardService, times(1)).updateExpiredCards();
    }

    @Test
    void testCheckExpiredCardsFrequently_NoExpiredCards() {
        // Arrange
        when(bankCardService.updateExpiredCards()).thenReturn(0);

        // Act
        schedulerService.checkExpiredCardsFrequently();

        // Assert
        verify(bankCardService, times(1)).updateExpiredCards();
    }
}

