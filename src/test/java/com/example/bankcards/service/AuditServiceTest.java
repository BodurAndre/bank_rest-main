package com.example.bankcards.service;

import com.example.bankcards.entity.AuditLog;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    private User testUser;
    private BankCard testCard;

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
        testCard.setOwner(testUser);
    }

    @Test
    void logLogin_Success_ShouldSaveAuditLog() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logLogin(testUser);

        // Then
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logLogout_Success_ShouldSaveAuditLog() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logLogout(testUser);

        // Then
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logCardCreation_Success_ShouldSaveAuditLog() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logCardCreation(testUser, 1L, "**** **** **** 1234");

        // Then
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logCardBlock_Success_ShouldSaveAuditLog() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logCardBlock(testUser, 1L, "**** **** **** 1234", "Suspicious activity");

        // Then
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logCardActivation_Success_ShouldSaveAuditLog() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logCardActivation(testUser, 1L, "**** **** **** 1234");

        // Then
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logCardDeletion_Success_ShouldSaveAuditLog() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logCardDeletion(testUser, 1L, "**** **** **** 1234");

        // Then
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logCardTopup_Success_ShouldSaveAuditLog() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logCardTopup(testUser, 1L, "**** **** **** 1234", 500.0);

        // Then
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logTransfer_Success_ShouldSaveAuditLog() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logTransfer(testUser, 1L, "**** **** **** 1234", "**** **** **** 5678", 100.0);

        // Then
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logUserCreation_Success_ShouldSaveAuditLog() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logUserAction(testUser, "CREATE_USER", "USER", 2L, "Создан пользователь newuser@example.com");

        // Then
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logUserUpdate_Success_ShouldSaveAuditLog() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logUserAction(testUser, "UPDATE_USER", "USER", 2L, "Обновлен пользователь updateduser@example.com");

        // Then
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logUserDeletion_Success_ShouldSaveAuditLog() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logUserAction(testUser, "DELETE_USER", "USER", 2L, "Удален пользователь deleteduser@example.com");

        // Then
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logNotificationCreation_Success_ShouldSaveAuditLog() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logUserAction(testUser, "CREATE_NOTIFICATION", "NOTIFICATION", 1L, "Создано уведомление CARD_BLOCK_REQUEST: Card block request");

        // Then
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logNotificationProcessing_Success_ShouldSaveAuditLog() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logUserAction(testUser, "PROCESS_NOTIFICATION", "NOTIFICATION", 1L, "Обработано уведомление CARD_BLOCK_REQUEST: Card blocked");

        // Then
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logExport_Success_ShouldSaveAuditLog() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logDataExport(testUser, "CARDS", "CSV");

        // Then
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logError_Success_ShouldSaveAuditLog() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logFailedAction(testUser, "TRANSFER", "TRANSFER", 1L, "Transfer failed", "Insufficient funds");

        // Then
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void getAuditLogs_ShouldReturnPageOfLogs() {
        // Given
        AuditLog auditLog = new AuditLog();
        auditLog.setId(1L);
        auditLog.setUser(testUser);
        auditLog.setAction("LOGIN");
        auditLog.setStatus("SUCCESS");
        auditLog.setCreatedAt(LocalDateTime.now());

        List<AuditLog> logs = Arrays.asList(auditLog);
        Page<AuditLog> logPage = new PageImpl<>(logs);
        when(auditLogRepository.findByUserOrderByCreatedAtDesc(testUser, PageRequest.of(0, 10)))
                .thenReturn(logPage);

        // When
        Page<AuditLog> result = auditService.getUserAuditLogs(testUser, PageRequest.of(0, 10));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("LOGIN", result.getContent().get(0).getAction());
        assertEquals("SUCCESS", result.getContent().get(0).getStatus());
    }

    @Test
    void getAuditLogsWithFilters_ShouldReturnFilteredLogs() {
        // Given
        AuditLog auditLog = new AuditLog();
        auditLog.setId(1L);
        auditLog.setUser(testUser);
        auditLog.setAction("LOGIN");
        auditLog.setStatus("SUCCESS");
        auditLog.setCreatedAt(LocalDateTime.now());

        List<AuditLog> logs = Arrays.asList(auditLog);
        Page<AuditLog> logPage = new PageImpl<>(logs);
        when(auditLogRepository.findWithFilters("LOGIN", "SUCCESS", "test@example.com", PageRequest.of(0, 10)))
                .thenReturn(logPage);

        // When
        Page<AuditLog> result = auditService.getAuditLogsWithFilters("LOGIN", "SUCCESS", "test@example.com", PageRequest.of(0, 10));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("LOGIN", result.getContent().get(0).getAction());
        assertEquals("SUCCESS", result.getContent().get(0).getStatus());
    }

    @Test
    void getAuditLogs_Admin_ShouldReturnAllLogs() {
        // Given
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(User.Role.ADMIN);

        AuditLog auditLog = new AuditLog();
        auditLog.setId(1L);
        auditLog.setUser(testUser);
        auditLog.setAction("LOGIN");
        auditLog.setStatus("SUCCESS");
        auditLog.setCreatedAt(LocalDateTime.now());

        List<AuditLog> logs = Arrays.asList(auditLog);
        Page<AuditLog> logPage = new PageImpl<>(logs);
        when(auditLogRepository.findAllForAdmins(PageRequest.of(0, 10)))
                .thenReturn(logPage);

        // When
        Page<AuditLog> result = auditService.getAllAuditLogsForAdmin(PageRequest.of(0, 10));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(auditLogRepository).findAllForAdmins(PageRequest.of(0, 10));
    }

    @Test
    void getAuditLogs_RegularUser_ShouldReturnUserLogs() {
        // Given
        AuditLog auditLog = new AuditLog();
        auditLog.setId(1L);
        auditLog.setUser(testUser);
        auditLog.setAction("LOGIN");
        auditLog.setStatus("SUCCESS");
        auditLog.setCreatedAt(LocalDateTime.now());

        List<AuditLog> logs = Arrays.asList(auditLog);
        Page<AuditLog> logPage = new PageImpl<>(logs);
        when(auditLogRepository.findByUserOrderByCreatedAtDesc(testUser, PageRequest.of(0, 10)))
                .thenReturn(logPage);

        // When
        Page<AuditLog> result = auditService.getUserAuditLogs(testUser, PageRequest.of(0, 10));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(auditLogRepository).findByUserOrderByCreatedAtDesc(testUser, PageRequest.of(0, 10));
    }

    @Test
    void logLogin_WithNullUser_ShouldNotSave() {
        // When
        auditService.logLogin(null);

        // Then
        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    void logCardCreation_WithNullUser_ShouldNotSave() {
        // When
        auditService.logCardCreation(null, 1L, "**** **** **** 1234");

        // Then
        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    void logTransfer_WithNullUser_ShouldNotSave() {
        // When
        auditService.logTransfer(null, 1L, "**** **** **** 1234", "**** **** **** 5678", 100.0);

        // Then
        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    void logError_WithNullUser_ShouldNotSave() {
        // When
        auditService.logFailedAction(null, "TRANSFER", "TRANSFER", 1L, "Error details", "Error message");

        // Then
        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }
}
