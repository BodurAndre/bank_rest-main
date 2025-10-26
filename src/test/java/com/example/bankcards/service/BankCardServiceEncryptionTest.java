package com.example.bankcards.service;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.CreateBankCardRequest;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для BankCardService с шифрованием
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BankCardServiceEncryptionTest {

    @Autowired
    private BankCardService bankCardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankCardRepository bankCardRepository;

    @Autowired
    private CardEncryptionUtil cardEncryptionUtil;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Очищаем базу данных
        bankCardRepository.deleteAll();
        userRepository.deleteAll();

        // Создаем тестового пользователя
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(User.Role.USER);
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Создание карты с шифрованием номера")
    void testCreateCardWithEncryption() {
        // Arrange
        CreateBankCardRequest request = new CreateBankCardRequest();
        request.setOwnerEmail(testUser.getEmail());
        request.setExpiryDate("12/25");

        // Act
        BankCardDto cardDto = bankCardService.createCard(request);

        // Assert
        assertNotNull(cardDto);
        assertNotNull(cardDto.getMaskedNumber());
        assertTrue(cardDto.getMaskedNumber().startsWith("**** **** **** "));

        // Проверяем, что номер в базе зашифрован
        BankCard savedCard = bankCardRepository.findById(cardDto.getId()).orElseThrow();
        assertNotNull(savedCard.getCardNumber());
        assertNotEquals("", savedCard.getCardNumber());
        
        // Проверяем, что номер зашифрован (не является простой строкой из цифр)
        assertFalse(savedCard.getCardNumber().matches("\\d{16}"));
        assertTrue(cardEncryptionUtil.isEncrypted(savedCard.getCardNumber()));
    }

    @Test
    @DisplayName("Проверка уникальности зашифрованных номеров")
    void testUniqueEncryptedCardNumbers() {
        // Arrange
        CreateBankCardRequest request1 = new CreateBankCardRequest();
        request1.setOwnerEmail(testUser.getEmail());
        request1.setExpiryDate("12/25");

        CreateBankCardRequest request2 = new CreateBankCardRequest();
        request2.setOwnerEmail(testUser.getEmail());
        request2.setExpiryDate("12/26");

        // Act
        BankCardDto card1 = bankCardService.createCard(request1);
        BankCardDto card2 = bankCardService.createCard(request2);

        // Assert
        assertNotNull(card1);
        assertNotNull(card2);
        assertNotEquals(card1.getId(), card2.getId());

        // Проверяем, что зашифрованные номера разные
        BankCard savedCard1 = bankCardRepository.findById(card1.getId()).orElseThrow();
        BankCard savedCard2 = bankCardRepository.findById(card2.getId()).orElseThrow();
        
        assertNotEquals(savedCard1.getCardNumber(), savedCard2.getCardNumber());
    }

    @Test
    @DisplayName("Проверка маскированного номера")
    void testMaskedNumberGeneration() {
        // Arrange
        CreateBankCardRequest request = new CreateBankCardRequest();
        request.setOwnerEmail(testUser.getEmail());
        request.setExpiryDate("12/25");

        // Act
        BankCardDto cardDto = bankCardService.createCard(request);

        // Assert
        assertNotNull(cardDto.getMaskedNumber());
        assertEquals("**** **** **** ", cardDto.getMaskedNumber().substring(0, 15));
        assertEquals(19, cardDto.getMaskedNumber().length()); // "**** **** **** 1234"
        
        // Проверяем, что последние 4 символа - это цифры
        String lastFour = cardDto.getMaskedNumber().substring(15);
        assertTrue(lastFour.matches("\\d{4}"));
    }

    @Test
    @DisplayName("Проверка валидности генерируемых номеров")
    void testGeneratedCardNumberValidity() {
        // Arrange
        CreateBankCardRequest request = new CreateBankCardRequest();
        request.setOwnerEmail(testUser.getEmail());
        request.setExpiryDate("12/25");

        // Act
        BankCardDto cardDto = bankCardService.createCard(request);

        // Assert
        assertNotNull(cardDto);
        
        // Получаем зашифрованный номер и расшифровываем его
        BankCard savedCard = bankCardRepository.findById(cardDto.getId()).orElseThrow();
        String decryptedNumber = cardEncryptionUtil.decryptCardNumber(savedCard.getCardNumber());
        
        // Проверяем, что расшифрованный номер валидный
        assertTrue(cardEncryptionUtil.isValidCardNumber(decryptedNumber));
        assertEquals(16, decryptedNumber.length());
        assertTrue(decryptedNumber.matches("\\d{16}"));
    }

    @Test
    @DisplayName("Проверка обработки ошибок при создании карты")
    void testErrorHandling() {
        // Arrange
        CreateBankCardRequest request = new CreateBankCardRequest();
        request.setOwnerEmail("nonexistent@example.com");
        request.setExpiryDate("12/25");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            bankCardService.createCard(request));
    }
}
