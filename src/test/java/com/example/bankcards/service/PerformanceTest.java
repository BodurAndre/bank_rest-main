package com.example.bankcards.service;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.CreateBankCardRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.AuditService;
import com.example.bankcards.service.BankCardService;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.CardEncryptionUtil;
import com.example.bankcards.util.ValidationUtils;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты производительности для критических операций
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PerformanceTest {

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

    private User testUser;
    private List<BankCardDto> testCards;

    @BeforeEach
    void setUp() {
        // Clean up existing data
        bankCardRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setEmail("perf@test.com");
        testUser.setFirstName("Performance");
        testUser.setLastName("Test");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(User.Role.USER);
        testUser = userService.createUser(testUser);

        // Create test cards
        testCards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            CreateBankCardRequest cardRequest = new CreateBankCardRequest();
            cardRequest.setOwnerEmail(testUser.getEmail());
            cardRequest.setExpiryDate("12/26");
            BankCardDto card = bankCardService.createCard(cardRequest);
            bankCardService.topupCard(card.getId(), 1000.0);
            testCards.add(card);
        }
    }

    @Test
    void performanceTest_CreateMultipleCards() {
        // Test creating multiple cards in sequence
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 50; i++) {
            CreateBankCardRequest cardRequest = new CreateBankCardRequest();
            cardRequest.setOwnerEmail(testUser.getEmail());
            cardRequest.setExpiryDate("12/26");
            BankCardDto card = bankCardService.createCard(cardRequest);
            assertNotNull(card);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should complete within reasonable time (adjust threshold as needed)
        assertTrue(duration < 10000, "Creating 50 cards took too long: " + duration + "ms");
        
        // Verify all cards were created
        var userCards = bankCardService.findByOwner(testUser, PageRequest.of(0, 100));
        assertTrue(userCards.getContent().size() >= 50);
    }

    @Test
    void performanceTest_MultipleTransfers() {
        // Test multiple transfers between cards
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            TransferRequest transferRequest = new TransferRequest();
            transferRequest.setFromCardId(testCards.get(0).getId());
            transferRequest.setToCardId(testCards.get(1).getId());
            transferRequest.setAmount(BigDecimal.valueOf(10.0));
            transferRequest.setDescription("Performance test transfer " + i);
            
            TransferResponse response = transferService.transfer(transferRequest, testUser);
            assertNotNull(response);
            assertEquals("COMPLETED", response.getStatus());
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should complete within reasonable time
        assertTrue(duration < 15000, "100 transfers took too long: " + duration + "ms");
        
        // Verify transfer history
        var transferHistory = transferService.getTransferHistory(testUser, PageRequest.of(0, 100));
        assertTrue(transferHistory.getContent().size() >= 100);
    }

    @Test
    void performanceTest_LargePageRetrieval() {
        // Test retrieving large pages of data
        long startTime = System.currentTimeMillis();
        
        var largePage = bankCardService.findByOwner(testUser, PageRequest.of(0, 100));
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should complete quickly
        assertTrue(duration < 1000, "Large page retrieval took too long: " + duration + "ms");
        assertNotNull(largePage);
    }

    @Test
    void performanceTest_ConcurrentOperations() {
        // Test concurrent operations
        long startTime = System.currentTimeMillis();
        
        List<Thread> threads = new ArrayList<>();
        
        // Create multiple threads for concurrent operations
        for (int i = 0; i < 5; i++) {
            final int threadId = i;
            Thread thread = new Thread(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        TransferRequest transferRequest = new TransferRequest();
                        transferRequest.setFromCardId(testCards.get(threadId % testCards.size()).getId());
                        transferRequest.setToCardId(testCards.get((threadId + 1) % testCards.size()).getId());
                        transferRequest.setAmount(BigDecimal.valueOf(5.0));
                        transferRequest.setDescription("Concurrent transfer " + threadId + "-" + j);
                        
                        TransferResponse response = transferService.transfer(transferRequest, testUser);
                        assertNotNull(response);
                    }
                } catch (Exception e) {
                    // Some transfers might fail due to insufficient funds, which is expected
                }
            });
            threads.add(thread);
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should complete within reasonable time
        assertTrue(duration < 20000, "Concurrent operations took too long: " + duration + "ms");
    }

    @Test
    void performanceTest_BulkTopup() {
        // Test bulk topup operations
        long startTime = System.currentTimeMillis();
        
        for (BankCardDto card : testCards) {
            bankCardService.topupCard(card.getId(), 500.0);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should complete quickly
        assertTrue(duration < 5000, "Bulk topup took too long: " + duration + "ms");
        
        // Verify balances were updated
        var updatedCards = bankCardService.findByOwner(testUser, PageRequest.of(0, 20));
        assertTrue(updatedCards.getContent().size() >= 10);
    }

    @Test
    void performanceTest_ExpiredCardsCleanup() {
        // Create some expired cards
        for (int i = 0; i < 20; i++) {
            CreateBankCardRequest cardRequest = new CreateBankCardRequest();
            cardRequest.setOwnerEmail(testUser.getEmail());
            cardRequest.setExpiryDate("01/20"); // Past date
            BankCardDto card = bankCardService.createCard(cardRequest);
            assertNotNull(card);
        }
        
        long startTime = System.currentTimeMillis();
        
        int expiredCount = bankCardService.updateExpiredCards();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should complete quickly
        assertTrue(duration < 2000, "Expired cards cleanup took too long: " + duration + "ms");
        assertTrue(expiredCount >= 20);
    }

    @Test
    void performanceTest_MemoryUsage() {
        // Test memory usage with large datasets
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Create many cards
        for (int i = 0; i < 100; i++) {
            CreateBankCardRequest cardRequest = new CreateBankCardRequest();
            cardRequest.setOwnerEmail(testUser.getEmail());
            cardRequest.setExpiryDate("12/26");
            BankCardDto card = bankCardService.createCard(cardRequest);
            assertNotNull(card);
        }
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = finalMemory - initialMemory;
        
        // Memory usage should be reasonable (adjust threshold as needed)
        assertTrue(memoryUsed < 50 * 1024 * 1024, "Memory usage too high: " + memoryUsed + " bytes");
    }

    @Test
    void performanceTest_DatabaseConnectionPool() {
        // Test database connection pool efficiency
        long startTime = System.currentTimeMillis();
        
        // Perform many quick operations
        for (int i = 0; i < 50; i++) {
            var cards = bankCardService.findByOwner(testUser, PageRequest.of(0, 10));
            assertNotNull(cards);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should complete quickly due to connection pooling
        assertTrue(duration < 3000, "Database operations took too long: " + duration + "ms");
    }

    @Test
    void performanceTest_TransactionRollback() {
        // Test transaction rollback performance
        long startTime = System.currentTimeMillis();
        
        // Attempt transfers that will fail
        for (int i = 0; i < 20; i++) {
            try {
                TransferRequest transferRequest = new TransferRequest();
                transferRequest.setFromCardId(testCards.get(0).getId());
                transferRequest.setToCardId(testCards.get(0).getId()); // Same card - will fail
                transferRequest.setAmount(BigDecimal.valueOf(100.0));
                transferRequest.setDescription("Failed transfer " + i);
                
                transferService.transfer(transferRequest, testUser);
            } catch (Exception e) {
                // Expected to fail
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Rollbacks should be fast
        assertTrue(duration < 5000, "Transaction rollbacks took too long: " + duration + "ms");
    }
}
