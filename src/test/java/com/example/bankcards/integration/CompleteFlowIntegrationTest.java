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

/**
 * Комплексные интеграционные тесты для полного flow приложения
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CompleteFlowIntegrationTest {

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

    private User adminUser;
    private User regularUser1;
    private User regularUser2;

    @BeforeEach
    void setUp() {
        // Clean up existing data
        bankCardRepository.deleteAll();
        userRepository.deleteAll();

        // Create admin user
        adminUser = new User();
        adminUser.setEmail("admin@test.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRole(User.Role.ADMIN);
        adminUser = userService.createUser(adminUser);

        // Create regular users
        regularUser1 = new User();
        regularUser1.setEmail("user1@test.com");
        regularUser1.setFirstName("User");
        regularUser1.setLastName("One");
        regularUser1.setPassword(passwordEncoder.encode("password123"));
        regularUser1.setRole(User.Role.USER);
        regularUser1 = userService.createUser(regularUser1);

        regularUser2 = new User();
        regularUser2.setEmail("user2@test.com");
        regularUser2.setFirstName("User");
        regularUser2.setLastName("Two");
        regularUser2.setPassword(passwordEncoder.encode("password123"));
        regularUser2.setRole(User.Role.USER);
        regularUser2 = userService.createUser(regularUser2);
    }

    @Test
    void completeBankingFlow_AdminCreatesCards_UsersManageCards_Transfers() {
        // 1. Admin creates cards for both users
        CreateBankCardRequest cardRequest1 = new CreateBankCardRequest();
        cardRequest1.setOwnerEmail("user1@test.com");
        cardRequest1.setExpiryDate("12/26");
        BankCardDto card1 = bankCardService.createCard(cardRequest1);

        CreateBankCardRequest cardRequest2 = new CreateBankCardRequest();
        cardRequest2.setOwnerEmail("user1@test.com");
        cardRequest2.setExpiryDate("12/26");
        BankCardDto card2 = bankCardService.createCard(cardRequest2);

        CreateBankCardRequest cardRequest3 = new CreateBankCardRequest();
        cardRequest3.setOwnerEmail("user2@test.com");
        cardRequest3.setExpiryDate("12/26");
        BankCardDto card3 = bankCardService.createCard(cardRequest3);

        // Verify cards were created
        assertNotNull(card1);
        assertNotNull(card2);
        assertNotNull(card3);
        assertEquals(BigDecimal.ZERO, card1.getBalance());
        assertEquals(BankCard.Status.ACTIVE, card1.getStatus());

        // 2. Users top up their cards
        bankCardService.topupCard(card1.getId(), 2000.0);
        bankCardService.topupCard(card2.getId(), 1000.0);
        bankCardService.topupCard(card3.getId(), 1500.0);

        // Verify balances
        var updatedCard1 = bankCardService.findByOwner(regularUser1, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(card -> card.getId().equals(card1.getId()))
                .findFirst().orElseThrow();
        
        var updatedCard2 = bankCardService.findByOwner(regularUser1, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(card -> card.getId().equals(card2.getId()))
                .findFirst().orElseThrow();

        var updatedCard3 = bankCardService.findByOwner(regularUser2, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(card -> card.getId().equals(card3.getId()))
                .findFirst().orElseThrow();

        assertEquals(BigDecimal.valueOf(2000.0), updatedCard1.getBalance());
        assertEquals(BigDecimal.valueOf(1000.0), updatedCard2.getBalance());
        assertEquals(BigDecimal.valueOf(1500.0), updatedCard3.getBalance());

        // 3. User1 transfers money between their own cards
        TransferRequest internalTransfer = new TransferRequest();
        internalTransfer.setFromCardId(card1.getId());
        internalTransfer.setToCardId(card2.getId());
        internalTransfer.setAmount(BigDecimal.valueOf(500.0));
        internalTransfer.setDescription("Internal transfer between my cards");

        TransferResponse internalTransferResponse = transferService.transfer(internalTransfer, regularUser1);
        assertNotNull(internalTransferResponse);
        assertEquals("COMPLETED", internalTransferResponse.getStatus());

        // Verify internal transfer balances
        var finalCard1 = bankCardService.findByOwner(regularUser1, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(card -> card.getId().equals(card1.getId()))
                .findFirst().orElseThrow();
        
        var finalCard2 = bankCardService.findByOwner(regularUser1, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(card -> card.getId().equals(card2.getId()))
                .findFirst().orElseThrow();

        assertEquals(BigDecimal.valueOf(1500.0), finalCard1.getBalance());
        assertEquals(BigDecimal.valueOf(1500.0), finalCard2.getBalance());

        // 4. User1 blocks one of their cards
        BankCardDto blockedCard = bankCardService.blockCard(card1.getId(), "Suspicious activity detected");
        assertEquals(BankCard.Status.BLOCKED, blockedCard.getStatus());

        // 5. Try to transfer from blocked card (should fail)
        TransferRequest blockedTransfer = new TransferRequest();
        blockedTransfer.setFromCardId(card1.getId());
        blockedTransfer.setToCardId(card2.getId());
        blockedTransfer.setAmount(BigDecimal.valueOf(100.0));
        blockedTransfer.setDescription("Transfer from blocked card");

        assertThrows(Exception.class, () -> {
            transferService.transfer(blockedTransfer, regularUser1);
        });

        // 6. User1 unblocks the card
        BankCardDto unblockedCard = bankCardService.activateCard(card1.getId());
        assertEquals(BankCard.Status.ACTIVE, unblockedCard.getStatus());

        // 7. Verify transfer works again after unblocking
        TransferRequest finalTransfer = new TransferRequest();
        finalTransfer.setFromCardId(card1.getId());
        finalTransfer.setToCardId(card2.getId());
        finalTransfer.setAmount(BigDecimal.valueOf(200.0));
        finalTransfer.setDescription("Transfer after unblocking");

        TransferResponse finalTransferResponse = transferService.transfer(finalTransfer, regularUser1);
        assertNotNull(finalTransferResponse);
        assertEquals("COMPLETED", finalTransferResponse.getStatus());

        // 8. Verify final balances
        var finalCard1AfterUnblock = bankCardService.findByOwner(regularUser1, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(card -> card.getId().equals(card1.getId()))
                .findFirst().orElseThrow();
        
        var finalCard2AfterUnblock = bankCardService.findByOwner(regularUser1, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(card -> card.getId().equals(card2.getId()))
                .findFirst().orElseThrow();

        assertEquals(BigDecimal.valueOf(1300.0), finalCard1AfterUnblock.getBalance());
        assertEquals(BigDecimal.valueOf(1700.0), finalCard2AfterUnblock.getBalance());
    }

    @Test
    void completeFlow_WithExpiredCards_AndCleanup() {
        // 1. Create cards with different expiry dates
        CreateBankCardRequest cardRequest1 = new CreateBankCardRequest();
        cardRequest1.setOwnerEmail("user1@test.com");
        cardRequest1.setExpiryDate("12/25"); // Future date
        BankCardDto card1 = bankCardService.createCard(cardRequest1);

        CreateBankCardRequest cardRequest2 = new CreateBankCardRequest();
        cardRequest2.setOwnerEmail("user1@test.com");
        cardRequest2.setExpiryDate("01/20"); // Past date
        BankCardDto card2 = bankCardService.createCard(cardRequest2);

        // 2. Top up both cards
        bankCardService.topupCard(card1.getId(), 1000.0);
        bankCardService.topupCard(card2.getId(), 500.0);

        // 3. Run expired cards cleanup
        int expiredCount = bankCardService.updateExpiredCards();
        assertTrue(expiredCount >= 1); // At least one card should be expired

        // 4. Verify card statuses
        var activeCard = bankCardService.findByOwner(regularUser1, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(card -> card.getId().equals(card1.getId()))
                .findFirst().orElseThrow();
        
        var expiredCard = bankCardService.findByOwner(regularUser1, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(card -> card.getId().equals(card2.getId()))
                .findFirst().orElseThrow();

        assertEquals(BankCard.Status.ACTIVE, activeCard.getStatus());
        assertEquals(BankCard.Status.EXPIRED, expiredCard.getStatus());

        // 5. Try to transfer from expired card (should fail)
        TransferRequest expiredTransfer = new TransferRequest();
        expiredTransfer.setFromCardId(card2.getId());
        expiredTransfer.setToCardId(card1.getId());
        expiredTransfer.setAmount(BigDecimal.valueOf(100.0));
        expiredTransfer.setDescription("Transfer from expired card");

        assertThrows(Exception.class, () -> {
            transferService.transfer(expiredTransfer, regularUser1);
        });

        // 6. Transfer from active card should work
        TransferRequest activeTransfer = new TransferRequest();
        activeTransfer.setFromCardId(card1.getId());
        activeTransfer.setToCardId(card1.getId()); // Same card for simplicity
        activeTransfer.setAmount(BigDecimal.valueOf(100.0));
        activeTransfer.setDescription("Transfer from active card");

        TransferResponse activeTransferResponse = transferService.transfer(activeTransfer, regularUser1);
        assertNotNull(activeTransferResponse);
        assertEquals("COMPLETED", activeTransferResponse.getStatus());
    }

    @Test
    void completeFlow_MultipleUsers_ComplexTransfers() {
        // 1. Create multiple cards for each user
        CreateBankCardRequest cardRequest1 = new CreateBankCardRequest();
        cardRequest1.setOwnerEmail("user1@test.com");
        cardRequest1.setExpiryDate("12/26");
        BankCardDto user1Card1 = bankCardService.createCard(cardRequest1);

        CreateBankCardRequest cardRequest2 = new CreateBankCardRequest();
        cardRequest2.setOwnerEmail("user1@test.com");
        cardRequest2.setExpiryDate("12/26");
        BankCardDto user1Card2 = bankCardService.createCard(cardRequest2);

        CreateBankCardRequest cardRequest3 = new CreateBankCardRequest();
        cardRequest3.setOwnerEmail("user2@test.com");
        cardRequest3.setExpiryDate("12/26");
        BankCardDto user2Card1 = bankCardService.createCard(cardRequest3);

        // 2. Top up all cards
        bankCardService.topupCard(user1Card1.getId(), 3000.0);
        bankCardService.topupCard(user1Card2.getId(), 2000.0);
        bankCardService.topupCard(user2Card1.getId(), 1000.0);

        // 3. Perform multiple transfers
        for (int i = 0; i < 3; i++) {
            TransferRequest transfer = new TransferRequest();
            transfer.setFromCardId(user1Card1.getId());
            transfer.setToCardId(user1Card2.getId());
            transfer.setAmount(BigDecimal.valueOf(200.0));
            transfer.setDescription("Transfer " + (i + 1));

            TransferResponse response = transferService.transfer(transfer, regularUser1);
            assertEquals("COMPLETED", response.getStatus());
        }

        // 4. Verify balances after multiple transfers
        var finalUser1Card1 = bankCardService.findByOwner(regularUser1, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(card -> card.getId().equals(user1Card1.getId()))
                .findFirst().orElseThrow();
        
        var finalUser1Card2 = bankCardService.findByOwner(regularUser1, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(card -> card.getId().equals(user1Card2.getId()))
                .findFirst().orElseThrow();

        assertEquals(BigDecimal.valueOf(2400.0), finalUser1Card1.getBalance());
        assertEquals(BigDecimal.valueOf(2600.0), finalUser1Card2.getBalance());

        // 5. Verify transfer history
        var transferHistory = transferService.getTransferHistory(regularUser1, PageRequest.of(0, 10));
        assertEquals(3, transferHistory.getContent().size());
        assertTrue(transferHistory.getContent().stream()
                .allMatch(transfer -> "COMPLETED".equals(transfer.getStatus())));

        // 6. Test edge case - transfer exact balance
        TransferRequest exactBalanceTransfer = new TransferRequest();
        exactBalanceTransfer.setFromCardId(user1Card1.getId());
        exactBalanceTransfer.setToCardId(user1Card2.getId());
        exactBalanceTransfer.setAmount(BigDecimal.valueOf(2400.0)); // Exact balance
        exactBalanceTransfer.setDescription("Transfer exact balance");

        TransferResponse exactBalanceResponse = transferService.transfer(exactBalanceTransfer, regularUser1);
        assertNotNull(exactBalanceResponse);
        assertEquals("COMPLETED", exactBalanceResponse.getStatus());

        // 7. Verify zero balance
        var zeroBalanceCard = bankCardService.findByOwner(regularUser1, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(card -> card.getId().equals(user1Card1.getId()))
                .findFirst().orElseThrow();

        assertEquals(BigDecimal.ZERO, zeroBalanceCard.getBalance());
    }

    @Test
    void completeFlow_ErrorHandling_AndRecovery() {
        // 1. Create cards
        CreateBankCardRequest cardRequest1 = new CreateBankCardRequest();
        cardRequest1.setOwnerEmail("user1@test.com");
        cardRequest1.setExpiryDate("12/26");
        BankCardDto card1 = bankCardService.createCard(cardRequest1);

        CreateBankCardRequest cardRequest2 = new CreateBankCardRequest();
        cardRequest2.setOwnerEmail("user1@test.com");
        cardRequest2.setExpiryDate("12/26");
        BankCardDto card2 = bankCardService.createCard(cardRequest2);

        // 2. Top up first card
        bankCardService.topupCard(card1.getId(), 1000.0);

        // 3. Try to transfer more than balance (should fail)
        TransferRequest insufficientFundsTransfer = new TransferRequest();
        insufficientFundsTransfer.setFromCardId(card1.getId());
        insufficientFundsTransfer.setToCardId(card2.getId());
        insufficientFundsTransfer.setAmount(BigDecimal.valueOf(1500.0)); // More than balance
        insufficientFundsTransfer.setDescription("Transfer more than balance");

        assertThrows(Exception.class, () -> {
            transferService.transfer(insufficientFundsTransfer, regularUser1);
        });

        // 4. Verify balance unchanged
        var unchangedCard = bankCardService.findByOwner(regularUser1, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(card -> card.getId().equals(card1.getId()))
                .findFirst().orElseThrow();

        assertEquals(BigDecimal.valueOf(1000.0), unchangedCard.getBalance());

        // 5. Try to transfer to same card (should fail)
        TransferRequest sameCardTransfer = new TransferRequest();
        sameCardTransfer.setFromCardId(card1.getId());
        sameCardTransfer.setToCardId(card1.getId()); // Same card
        sameCardTransfer.setAmount(BigDecimal.valueOf(100.0));
        sameCardTransfer.setDescription("Transfer to same card");

        assertThrows(Exception.class, () -> {
            transferService.transfer(sameCardTransfer, regularUser1);
        });

        // 6. Successful transfer after errors
        TransferRequest successfulTransfer = new TransferRequest();
        successfulTransfer.setFromCardId(card1.getId());
        successfulTransfer.setToCardId(card2.getId());
        successfulTransfer.setAmount(BigDecimal.valueOf(500.0));
        successfulTransfer.setDescription("Successful transfer after errors");

        TransferResponse successfulResponse = transferService.transfer(successfulTransfer, regularUser1);
        assertNotNull(successfulResponse);
        assertEquals("COMPLETED", successfulResponse.getStatus());

        // 7. Verify final balances
        var finalCard1 = bankCardService.findByOwner(regularUser1, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(card -> card.getId().equals(card1.getId()))
                .findFirst().orElseThrow();
        
        var finalCard2 = bankCardService.findByOwner(regularUser1, PageRequest.of(0, 10))
                .getContent().stream()
                .filter(card -> card.getId().equals(card2.getId()))
                .findFirst().orElseThrow();

        assertEquals(BigDecimal.valueOf(500.0), finalCard1.getBalance());
        assertEquals(BigDecimal.valueOf(500.0), finalCard2.getBalance());
    }

    @Test
    void completeFlow_AdminOperations_AndUserRestrictions() {
        // 1. Admin creates cards for users
        CreateBankCardRequest cardRequest1 = new CreateBankCardRequest();
        cardRequest1.setOwnerEmail("user1@test.com");
        cardRequest1.setExpiryDate("12/26");
        BankCardDto card1 = bankCardService.createCard(cardRequest1);

        CreateBankCardRequest cardRequest2 = new CreateBankCardRequest();
        cardRequest2.setOwnerEmail("user2@test.com");
        cardRequest2.setExpiryDate("12/26");
        BankCardDto card2 = bankCardService.createCard(cardRequest2);

        // 2. Admin can view all cards
        var allCards = bankCardService.findAllWithFilters(null, "", PageRequest.of(0, 10));
        assertTrue(allCards.getContent().size() >= 2);

        // 3. Admin can view all users
        var allUsers = userService.findAllWithPagination(PageRequest.of(0, 10));
        assertTrue(allUsers.getContent().size() >= 3); // admin + 2 users

        // 4. Users can only see their own cards
        var user1Cards = bankCardService.findByOwner(regularUser1, PageRequest.of(0, 10));
        assertEquals(1, user1Cards.getContent().size());
        assertEquals(card1.getId(), user1Cards.getContent().get(0).getId());

        var user2Cards = bankCardService.findByOwner(regularUser2, PageRequest.of(0, 10));
        assertEquals(1, user2Cards.getContent().size());
        assertEquals(card2.getId(), user2Cards.getContent().get(0).getId());

        // 5. Users cannot transfer between different users' cards
        bankCardService.topupCard(card1.getId(), 1000.0);
        bankCardService.topupCard(card2.getId(), 1000.0);

        TransferRequest crossUserTransfer = new TransferRequest();
        crossUserTransfer.setFromCardId(card1.getId());
        crossUserTransfer.setToCardId(card2.getId());
        crossUserTransfer.setAmount(BigDecimal.valueOf(100.0));
        crossUserTransfer.setDescription("Cross-user transfer");

        assertThrows(Exception.class, () -> {
            transferService.transfer(crossUserTransfer, regularUser1);
        });

        // 6. Admin can delete users
        userService.deleteUser(regularUser2.getId());
        
        // Verify user2 is deleted
        var remainingUsers = userService.findAllWithPagination(PageRequest.of(0, 10));
        assertTrue(remainingUsers.getContent().stream()
                .noneMatch(user -> user.getId().equals(regularUser2.getId())));
    }
}
