package com.example.bankcards.service;

import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.AuditLog;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.AuditLogRepository;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock
    private BankCardRepository bankCardRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private ExportService exportService;

    private User testUser;
    private BankCard testCard;
    private TransferResponse testTransfer;
    private Transfer testTransferEntity;
    private AuditLog testAuditLog;

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
        testCard.setCreatedAt(LocalDateTime.now());
        testCard.setOwner(testUser);

        testTransfer = new TransferResponse();
        testTransfer.setId(1L);
        testTransfer.setFromCardId(1L);
        testTransfer.setFromCardMasked("**** **** **** 1234");
        testTransfer.setToCardId(2L);
        testTransfer.setToCardMasked("**** **** **** 5678");
        testTransfer.setAmount(BigDecimal.valueOf(100.0));
        testTransfer.setDescription("Test transfer");
        testTransfer.setStatus("COMPLETED");
        testTransfer.setCreatedAt(LocalDateTime.now());

        testTransferEntity = new Transfer();
        testTransferEntity.setId(1L);
        testTransferEntity.setFromCard(testCard);
        testTransferEntity.setToCard(testCard);
        testTransferEntity.setAmount(BigDecimal.valueOf(100.0));
        testTransferEntity.setDescription("Test transfer");
        testTransferEntity.setStatus(Transfer.Status.COMPLETED);
        testTransferEntity.setCreatedAt(LocalDateTime.now());

        testAuditLog = new AuditLog();
        testAuditLog.setId(1L);
        testAuditLog.setUser(testUser);
        testAuditLog.setAction("LOGIN");
        testAuditLog.setStatus("SUCCESS");
        testAuditLog.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void exportCardsToCSVForAdmin_ShouldReturnCsvData() throws IOException {
        // Given
        List<BankCard> cards = Arrays.asList(testCard);
        Page<BankCard> cardPage = new PageImpl<>(cards);
        when(bankCardRepository.findAllWithFilters(any(), anyString(), any(PageRequest.class)))
                .thenReturn(cardPage);

        // When
        byte[] result = exportService.exportCardsToCSVForAdmin("ACTIVE", "1234", "test@example.com");

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        String csvContent = new String(result);
        assertTrue(csvContent.contains("Номер карты"));
        assertTrue(csvContent.contains("Владелец"));
        assertTrue(csvContent.contains("Email владельца"));
        assertTrue(csvContent.contains("Баланс"));
        assertTrue(csvContent.contains("Статус"));
        assertTrue(csvContent.contains("Срок действия"));
        assertTrue(csvContent.contains("Дата создания"));
    }

    @Test
    void exportCardsToPDFForAdmin_ShouldReturnPdfData() throws IOException {
        // Given
        List<BankCard> cards = Arrays.asList(testCard);
        Page<BankCard> cardPage = new PageImpl<>(cards);
        when(bankCardRepository.findAllWithFilters(any(), anyString(), any(PageRequest.class)))
                .thenReturn(cardPage);

        // When
        byte[] result = exportService.exportCardsToPDFForAdmin("ACTIVE", "1234", "test@example.com");

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        // Check PDF header
        String pdfHeader = new String(result, 0, 4);
        assertEquals("%PDF", pdfHeader);
    }

    @Test
    void exportUsersToCSV_ShouldReturnCsvData() throws IOException {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByEmailContainingIgnoreCase(anyString()))
                .thenReturn(users);

        // When
        byte[] result = exportService.exportUsersToCSV("USER", "test");

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        String csvContent = new String(result);
        assertTrue(csvContent.contains("ID"));
        assertTrue(csvContent.contains("Email"));
        assertTrue(csvContent.contains("Имя"));
        assertTrue(csvContent.contains("Фамилия"));
        assertTrue(csvContent.contains("Роль"));
        assertTrue(csvContent.contains("Дата создания"));
    }

    @Test
    void exportUsersToPDF_ShouldReturnPdfData() throws IOException {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByEmailContainingIgnoreCase(anyString()))
                .thenReturn(users);

        // When
        byte[] result = exportService.exportUsersToPDF("USER", "test");

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        // Check PDF header
        String pdfHeader = new String(result, 0, 4);
        assertEquals("%PDF", pdfHeader);
    }

    @Test
    void exportTransfersToCSVForAdmin_ShouldReturnCsvData() throws IOException {
        // Given
        List<Transfer> transfers = Arrays.asList(testTransferEntity);
        Page<Transfer> transferPage = new PageImpl<>(transfers);
        when(transferRepository.findByUser(any(User.class), any(PageRequest.class)))
                .thenReturn(transferPage);

        // When
        byte[] result = exportService.exportTransfersToCSVForAdmin("test@example.com", "2024-01-01", "2024-12-31");

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        String csvContent = new String(result);
        assertTrue(csvContent.contains("Дата"));
        assertTrue(csvContent.contains("От карты"));
        assertTrue(csvContent.contains("К карте"));
        assertTrue(csvContent.contains("Сумма"));
        assertTrue(csvContent.contains("Описание"));
        assertTrue(csvContent.contains("Статус"));
    }

    @Test
    void exportTransfersToPDFForAdmin_ShouldReturnPdfData() throws IOException {
        // Given
        List<Transfer> transfers = Arrays.asList(testTransferEntity);
        Page<Transfer> transferPage = new PageImpl<>(transfers);
        when(transferRepository.findByUser(any(User.class), any(PageRequest.class)))
                .thenReturn(transferPage);

        // When
        byte[] result = exportService.exportTransfersToPDFForAdmin("test@example.com", "2024-01-01", "2024-12-31");

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        // Check PDF header
        String pdfHeader = new String(result, 0, 4);
        assertEquals("%PDF", pdfHeader);
    }

    @Test
    void exportAuditLogsToCSV_ShouldReturnCsvData() throws IOException {
        // Given
        List<AuditLog> auditLogs = Arrays.asList(testAuditLog);
        Page<AuditLog> auditLogPage = new PageImpl<>(auditLogs);
        when(auditLogRepository.findWithFilters(anyString(), anyString(), anyString(), any(PageRequest.class)))
                .thenReturn(auditLogPage);

        // When
        byte[] result = exportService.exportAuditLogsToCSV("LOGIN", "SUCCESS", "test@example.com");

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        String csvContent = new String(result);
        assertTrue(csvContent.contains("Дата"));
        assertTrue(csvContent.contains("Пользователь"));
        assertTrue(csvContent.contains("Действие"));
        assertTrue(csvContent.contains("Статус"));
        assertTrue(csvContent.contains("IP адрес"));
        assertTrue(csvContent.contains("User Agent"));
        assertTrue(csvContent.contains("Детали"));
    }

    @Test
    void exportAuditLogsToPDF_ShouldReturnPdfData() throws IOException {
        // Given
        List<AuditLog> auditLogs = Arrays.asList(testAuditLog);
        Page<AuditLog> auditLogPage = new PageImpl<>(auditLogs);
        when(auditLogRepository.findWithFilters(anyString(), anyString(), anyString(), any(PageRequest.class)))
                .thenReturn(auditLogPage);

        // When
        byte[] result = exportService.exportAuditLogsToPDF("LOGIN", "SUCCESS", "test@example.com");

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        // Check PDF header
        String pdfHeader = new String(result, 0, 4);
        assertEquals("%PDF", pdfHeader);
    }

    @Test
    void exportCardsToCSVForAdmin_EmptyData_ShouldReturnEmptyCsv() throws IOException {
        // Given
        Page<BankCard> emptyPage = new PageImpl<>(Arrays.asList());
        when(bankCardRepository.findAllWithFilters(any(), anyString(), any(PageRequest.class)))
                .thenReturn(emptyPage);

        // When
        byte[] result = exportService.exportCardsToCSVForAdmin("ACTIVE", "1234", "test@example.com");

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        String csvContent = new String(result);
        assertTrue(csvContent.contains("Номер карты"));
        assertTrue(csvContent.contains("Владелец"));
        // Should only contain headers, no data rows
    }

    @Test
    void exportUsersToCSV_EmptyData_ShouldReturnEmptyCsv() throws IOException {
        // Given
        when(userRepository.findByEmailContainingIgnoreCase(anyString()))
                .thenReturn(Arrays.asList());

        // When
        byte[] result = exportService.exportUsersToCSV("USER", "test");

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        String csvContent = new String(result);
        assertTrue(csvContent.contains("ID"));
        assertTrue(csvContent.contains("Email"));
        // Should only contain headers, no data rows
    }

    @Test
    void exportTransfersToCSVForAdmin_EmptyData_ShouldReturnEmptyCsv() throws IOException {
        // Given
        Page<Transfer> emptyPage = new PageImpl<>(Arrays.asList());
        when(transferRepository.findByUser(any(User.class), any(PageRequest.class)))
                .thenReturn(emptyPage);

        // When
        byte[] result = exportService.exportTransfersToCSVForAdmin("test@example.com", "2024-01-01", "2024-12-31");

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        String csvContent = new String(result);
        assertTrue(csvContent.contains("Дата"));
        assertTrue(csvContent.contains("От карты"));
        // Should only contain headers, no data rows
    }

    @Test
    void exportAuditLogsToCSV_EmptyData_ShouldReturnEmptyCsv() throws IOException {
        // Given
        Page<AuditLog> emptyPage = new PageImpl<>(Arrays.asList());
        when(auditLogRepository.findWithFilters(anyString(), anyString(), anyString(), any(PageRequest.class)))
                .thenReturn(emptyPage);

        // When
        byte[] result = exportService.exportAuditLogsToCSV("LOGIN", "SUCCESS", "test@example.com");

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        String csvContent = new String(result);
        assertTrue(csvContent.contains("Дата"));
        assertTrue(csvContent.contains("Пользователь"));
        // Should only contain headers, no data rows
    }

    @Test
    void exportCardsToCSVForAdmin_WithSpecialCharacters_ShouldHandleCorrectly() throws IOException {
        // Given
        User specialUser = new User();
        specialUser.setId(2L);
        specialUser.setEmail("special@example.com");
        specialUser.setFirstName("Тест");
        specialUser.setLastName("Пользователь");
        specialUser.setRole(User.Role.USER);

        BankCard specialCard = new BankCard();
        specialCard.setId(2L);
        specialCard.setMaskedNumber("**** **** **** 5678");
        specialCard.setBalance(BigDecimal.valueOf(2000.00));
        specialCard.setStatus(BankCard.Status.ACTIVE);
        specialCard.setExpiryDate(LocalDate.now().plusYears(2));
        specialCard.setCreatedAt(LocalDateTime.now());
        specialCard.setOwner(specialUser);

        List<BankCard> cards = Arrays.asList(specialCard);
        Page<BankCard> cardPage = new PageImpl<>(cards);
        when(bankCardRepository.findAllWithFilters(any(), anyString(), any(PageRequest.class)))
                .thenReturn(cardPage);

        // When
        byte[] result = exportService.exportCardsToCSVForAdmin("ACTIVE", "5678", "special@example.com");

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        String csvContent = new String(result);
        assertTrue(csvContent.contains("Тест"));
        assertTrue(csvContent.contains("Пользователь"));
    }

    @Test
    void exportUsersToCSV_WithSpecialCharacters_ShouldHandleCorrectly() throws IOException {
        // Given
        User specialUser = new User();
        specialUser.setId(2L);
        specialUser.setEmail("special@example.com");
        specialUser.setFirstName("Тест");
        specialUser.setLastName("Пользователь");
        specialUser.setRole(User.Role.USER);

        List<User> users = Arrays.asList(specialUser);
        when(userRepository.findByEmailContainingIgnoreCase(anyString()))
                .thenReturn(users);

        // When
        byte[] result = exportService.exportUsersToCSV("USER", "special");

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        String csvContent = new String(result);
        assertTrue(csvContent.contains("Тест"));
        assertTrue(csvContent.contains("Пользователь"));
    }

    @Test
    void exportCardsToPDFForAdmin_WithSpecialCharacters_ShouldHandleCorrectly() throws IOException {
        // Given
        User specialUser = new User();
        specialUser.setId(2L);
        specialUser.setEmail("special@example.com");
        specialUser.setFirstName("Тест");
        specialUser.setLastName("Пользователь");
        specialUser.setRole(User.Role.USER);

        BankCard specialCard = new BankCard();
        specialCard.setId(2L);
        specialCard.setMaskedNumber("**** **** **** 5678");
        specialCard.setBalance(BigDecimal.valueOf(2000.00));
        specialCard.setStatus(BankCard.Status.ACTIVE);
        specialCard.setExpiryDate(LocalDate.now().plusYears(2));
        specialCard.setCreatedAt(LocalDateTime.now());
        specialCard.setOwner(specialUser);

        List<BankCard> cards = Arrays.asList(specialCard);
        Page<BankCard> cardPage = new PageImpl<>(cards);
        when(bankCardRepository.findAllWithFilters(any(), anyString(), any(PageRequest.class)))
                .thenReturn(cardPage);

        // When
        byte[] result = exportService.exportCardsToPDFForAdmin("ACTIVE", "5678", "special@example.com");

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        // Check PDF header
        String pdfHeader = new String(result, 0, 4);
        assertEquals("%PDF", pdfHeader);
    }

    @Test
    void exportUsersToPDF_WithSpecialCharacters_ShouldHandleCorrectly() throws IOException {
        // Given
        User specialUser = new User();
        specialUser.setId(2L);
        specialUser.setEmail("special@example.com");
        specialUser.setFirstName("Тест");
        specialUser.setLastName("Пользователь");
        specialUser.setRole(User.Role.USER);

        List<User> users = Arrays.asList(specialUser);
        when(userRepository.findByEmailContainingIgnoreCase(anyString()))
                .thenReturn(users);

        // When
        byte[] result = exportService.exportUsersToPDF("USER", "special");

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        // Check PDF header
        String pdfHeader = new String(result, 0, 4);
        assertEquals("%PDF", pdfHeader);
    }
}
