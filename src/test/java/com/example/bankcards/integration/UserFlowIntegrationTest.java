package com.example.bankcards.integration;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.AuditService;
import com.example.bankcards.service.BankCardService;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserFlowIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private BankCardService bankCardService;

    @Autowired
    private TransferService transferService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankCardRepository bankCardRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser1;
    private User testUser2;
    private BankCard card1;
    private BankCard card2;

    @BeforeEach
    void setUp() {
        // Clean up existing data
        bankCardRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        testUser1 = new User();
        testUser1.setEmail("user1@test.com");
        testUser1.setFirstName("User");
        testUser1.setLastName("One");
        testUser1.setPassword(passwordEncoder.encode("password123"));
        testUser1.setRole(User.Role.USER);
        testUser1 = userService.createUser(testUser1);

        testUser2 = new User();
        testUser2.setEmail("user2@test.com");
        testUser2.setFirstName("User");
        testUser2.setLastName("Two");
        testUser2.setPassword(passwordEncoder.encode("password123"));
        testUser2.setRole(User.Role.USER);
        testUser2 = userService.createUser(testUser2);
    }

    @Test
    void completeUserFlow_CardCreation_Transfer_Block_Unblock() {
        // 1. Create cards for both users
        CreateBankCardRequest cardRequest1 = new CreateBankCardRequest();
        cardRequest1.setOwnerEmail("user1@test.com");
        cardRequest1.setExpiryDate("12/26");
        BankCardDto cardDto1 = bankCardService.createCard(cardRequest1);

        CreateBankCardRequest cardRequest2 = new CreateBankCardRequest();
        cardRequest2.setOwnerEmail("user2@test.com");
        cardRequest2.setExpiryDate("12/26");
        BankCardDto cardDto2 = bankCardService.createCard(cardRequest2);

        // Verify cards were created
        assertNotNull(cardDto1);
        assertNotNull(cardDto2);
        assertEquals(BigDecimal.ZERO, cardDto1.getBalance());
        assertEquals(BigDecimal.ZERO, cardDto2.getBalance());
        assertEquals(BankCard.Status.ACTIVE, cardDto1.getStatus());
        assertEquals(BankCard.Status.ACTIVE, cardDto2.getStatus());

        // 2. Top up both cards
        bankCardService.topupCard(cardDto1.getId(), 1000.0);
        bankCardService.topupCard(cardDto2.getId(), 500.0);

        // Verify balances
        var updatedCard1 = bankCardService.findByOwner(testUser1, PageRequest.of(0, 10))
                .getContent().get(0);
        var updatedCard2 = bankCardService.findByOwner(testUser2, PageRequest.of(0, 10))
                .getContent().get(0);
        
        assertEquals(BigDecimal.valueOf(1000.0), updatedCard1.getBalance());
        assertEquals(BigDecimal.valueOf(500.0), updatedCard2.getBalance());

        // 3. Create a second card for user1
        BankCardDto cardDto1Second = bankCardService.createCard(cardRequest1);
        bankCardService.topupCard(cardDto1Second.getId(), 200.0);

        // 4. Transfer money between user1's cards
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromCardId(cardDto1.getId());
        transferRequest.setToCardId(cardDto1Second.getId());
        transferRequest.setAmount(BigDecimal.valueOf(300.0));
        transferRequest.setDescription("Transfer between my cards");

        TransferResponse transferResponse = transferService.transfer(transferRequest, testUser1);

        // Verify transfer
        assertNotNull(transferResponse);
        assertEquals("COMPLETED", transferResponse.getStatus());
        assertEquals(BigDecimal.valueOf(300.0), transferResponse.getAmount());

        // Verify balances after transfer
        var finalCard1 = bankCardService.findByOwner(testUser1, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(card -> card.getId().equals(cardDto1.getId()))
                .findFirst().orElseThrow();
        
        var finalCard1Second = bankCardService.findByOwner(testUser1, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(card -> card.getId().equals(cardDto1Second.getId()))
                .findFirst().orElseThrow();

        assertEquals(BigDecimal.valueOf(700.0), finalCard1.getBalance());
        assertEquals(BigDecimal.valueOf(500.0), finalCard1Second.getBalance());

        // 5. Block one of the cards
        BankCardDto blockedCard = bankCardService.blockCard(cardDto1.getId(), "Suspicious activity");

        // Verify card is blocked
        assertEquals(BankCard.Status.BLOCKED, blockedCard.getStatus());

        // 6. Try to transfer from blocked card (should fail)
        TransferRequest blockedTransferRequest = new TransferRequest();
        blockedTransferRequest.setFromCardId(cardDto1.getId());
        blockedTransferRequest.setToCardId(cardDto1Second.getId());
        blockedTransferRequest.setAmount(BigDecimal.valueOf(100.0));
        blockedTransferRequest.setDescription("Transfer from blocked card");

        assertThrows(Exception.class, () -> {
            transferService.transfer(blockedTransferRequest, testUser1);
        });

        // 7. Unblock the card
        BankCardDto unblockedCard = bankCardService.activateCard(cardDto1.getId());

        // Verify card is active again
        assertEquals(BankCard.Status.ACTIVE, unblockedCard.getStatus());

        // 8. Verify we can transfer again
        TransferRequest finalTransferRequest = new TransferRequest();
        finalTransferRequest.setFromCardId(cardDto1.getId());
        finalTransferRequest.setToCardId(cardDto1Second.getId());
        finalTransferRequest.setAmount(BigDecimal.valueOf(50.0));
        finalTransferRequest.setDescription("Transfer after unblock");

        TransferResponse finalTransferResponse = transferService.transfer(finalTransferRequest, testUser1);
        assertNotNull(finalTransferResponse);
        assertEquals("COMPLETED", finalTransferResponse.getStatus());
    }

    @Test
    void userFlow_WithExpiredCard() {
        // Create a card with past expiry date
        CreateBankCardRequest cardRequest = new CreateBankCardRequest();
        cardRequest.setOwnerEmail("user1@test.com");
        cardRequest.setExpiryDate("01/20"); // Past date
        BankCardDto cardDto = bankCardService.createCard(cardRequest);

        // Top up the card
        bankCardService.topupCard(cardDto.getId(), 1000.0);

        // Update expired cards
        int updatedCount = bankCardService.updateExpiredCards();
        assertTrue(updatedCount >= 0);

        // Verify card status after expiry check
        var expiredCard = bankCardService.findByOwner(testUser1, PageRequest.of(0, 10))
                .getContent().get(0);
        
        // Card should be marked as expired
        assertEquals(BankCard.Status.EXPIRED, expiredCard.getStatus());

        // Try to transfer from expired card (should fail)
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromCardId(cardDto.getId());
        transferRequest.setToCardId(cardDto.getId()); // Same card for simplicity
        transferRequest.setAmount(BigDecimal.valueOf(100.0));
        transferRequest.setDescription("Transfer from expired card");

        assertThrows(Exception.class, () -> {
            transferService.transfer(transferRequest, testUser1);
        });
    }

    @Test
    void userFlow_MultipleTransfers_Statistics() {
        // Create cards
        CreateBankCardRequest cardRequest1 = new CreateBankCardRequest();
        cardRequest1.setOwnerEmail("user1@test.com");
        cardRequest1.setExpiryDate("12/26");
        BankCardDto card1 = bankCardService.createCard(cardRequest1);

        CreateBankCardRequest cardRequest2 = new CreateBankCardRequest();
        cardRequest2.setOwnerEmail("user1@test.com");
        cardRequest2.setExpiryDate("12/26");
        BankCardDto card2 = bankCardService.createCard(cardRequest2);

        // Top up first card
        bankCardService.topupCard(card1.getId(), 2000.0);

        // Perform multiple transfers
        for (int i = 0; i < 5; i++) {
            TransferRequest transferRequest = new TransferRequest();
            transferRequest.setFromCardId(card1.getId());
            transferRequest.setToCardId(card2.getId());
            transferRequest.setAmount(BigDecimal.valueOf(100.0));
            transferRequest.setDescription("Transfer " + (i + 1));

            TransferResponse response = transferService.transfer(transferRequest, testUser1);
            assertEquals("COMPLETED", response.getStatus());
        }

        // Verify final balances
        var finalCard1 = bankCardService.findByOwner(testUser1, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(card -> card.getId().equals(card1.getId()))
                .findFirst().orElseThrow();
        
        var finalCard2 = bankCardService.findByOwner(testUser1, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(card -> card.getId().equals(card2.getId()))
                .findFirst().orElseThrow();

        assertEquals(BigDecimal.valueOf(1500.0), finalCard1.getBalance());
        assertEquals(BigDecimal.valueOf(500.0), finalCard2.getBalance());

        // Verify transfer history
        var transferHistory = transferService.getTransferHistory(testUser1, PageRequest.of(0, 10));
        assertEquals(5, transferHistory.getContent().size());
        assertTrue(transferHistory.getContent().stream()
                .allMatch(transfer -> "COMPLETED".equals(transfer.getStatus())));
    }

    @Test
    void userFlow_EdgeCases() {
        // Create card
        CreateBankCardRequest cardRequest = new CreateBankCardRequest();
        cardRequest.setOwnerEmail("user1@test.com");
        cardRequest.setExpiryDate("12/26");
        BankCardDto card = bankCardService.createCard(cardRequest);

        // Top up with minimum amount
        bankCardService.topupCard(card.getId(), 0.01);
        
        var updatedCard = bankCardService.findByOwner(testUser1, PageRequest.of(0, 10))
                .getContent().get(0);
        assertEquals(BigDecimal.valueOf(0.01), updatedCard.getBalance());

        // Try to transfer more than balance
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromCardId(card.getId());
        transferRequest.setToCardId(card.getId());
        transferRequest.setAmount(BigDecimal.valueOf(1.0));
        transferRequest.setDescription("Transfer more than balance");

        assertThrows(Exception.class, () -> {
            transferService.transfer(transferRequest, testUser1);
        });

        // Try to transfer exact balance
        transferRequest.setAmount(BigDecimal.valueOf(0.01));
        TransferResponse response = transferService.transfer(transferRequest, testUser1);
        assertEquals("COMPLETED", response.getStatus());

        // Verify balance is now zero
        var zeroBalanceCard = bankCardService.findByOwner(testUser1, PageRequest.of(0, 10))
                .getContent().get(0);
        assertEquals(BigDecimal.ZERO, zeroBalanceCard.getBalance());
    }
}
