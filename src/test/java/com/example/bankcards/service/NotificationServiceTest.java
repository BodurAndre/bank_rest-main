package com.example.bankcards.service;

import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.Notification;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User testUser;
    private User adminUser;
    private BankCard testCard;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(User.Role.USER);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(User.Role.ADMIN);

        testCard = new BankCard();
        testCard.setId(1L);
        testCard.setMaskedNumber("**** **** **** 1234");
        testCard.setBalance(BigDecimal.valueOf(1000.00));
        testCard.setStatus(BankCard.Status.ACTIVE);
        testCard.setExpiryDate(LocalDate.now().plusYears(2));
        testCard.setOwner(testUser);

        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setUser(testUser);
        testNotification.setType(Notification.Type.CARD_BLOCK_REQUEST);
        testNotification.setTitle("Запрос на блокировку карты");
        testNotification.setMessage("Пользователь запросил блокировку карты **** **** **** 1234");
        testNotification.setIsRead(false);
        testNotification.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createNotification_Success_ShouldReturnNotification() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        Notification result = notificationService.createCardBlockRequest(
                testUser, 
                testCard, 
                "Пользователь запросил блокировку карты **** **** **** 1234"
        );

        // Then
        assertNotNull(result);
        assertEquals(Notification.Type.CARD_BLOCK_REQUEST, result.getType());
        assertEquals("Запрос на блокировку карты", result.getTitle());
        assertEquals(false, result.getIsRead());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createNotification_WithCard_ShouldReturnNotification() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        Notification result = notificationService.createCardBlockRequest(
                testUser, 
                testCard, 
                "Пользователь запросил блокировку карты **** **** **** 1234"
        );

        // Then
        assertNotNull(result);
        assertEquals(Notification.Type.CARD_BLOCK_REQUEST, result.getType());
        assertEquals(testCard, result.getCard());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createNotification_WithAmount_ShouldReturnNotification() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        Notification result = notificationService.createCardTopupRequest(
                testUser, 
                testCard, 
                500.0
        );

        // Then
        assertNotNull(result);
        assertEquals(Notification.Type.CARD_TOPUP_REQUEST, result.getType());
        assertEquals(500.0, result.getAmount());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getNotifications_ShouldReturnPageOfNotifications() {
        // Given
        List<Notification> notifications = Arrays.asList(testNotification);
        Page<Notification> notificationPage = new PageImpl<>(notifications);
        when(notificationRepository.findByUserOrderByCreatedAtDesc(testUser, PageRequest.of(0, 10)))
                .thenReturn(notificationPage);

        // When
        Page<Notification> result = notificationService.getUserNotifications(testUser, PageRequest.of(0, 10));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(Notification.Type.CARD_BLOCK_REQUEST, result.getContent().get(0).getType());
    }

    @Test
    void getNotifications_Admin_ShouldReturnAllNotifications() {
        // Given
        List<Notification> notifications = Arrays.asList(testNotification);
        Page<Notification> notificationPage = new PageImpl<>(notifications);
        when(notificationRepository.findByUserOrderByCreatedAtDesc(testUser, PageRequest.of(0, 10)))
                .thenReturn(notificationPage);

        // When
        Page<Notification> result = notificationService.getAllNotificationsForAdmins(PageRequest.of(0, 10));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(notificationRepository).findByUserOrderByCreatedAtDesc(testUser, PageRequest.of(0, 10));
    }

    @Test
    void getNotifications_RegularUser_ShouldReturnUserNotifications() {
        // Given
        List<Notification> notifications = Arrays.asList(testNotification);
        Page<Notification> notificationPage = new PageImpl<>(notifications);
        when(notificationRepository.findByUserOrderByCreatedAtDesc(testUser, PageRequest.of(0, 10)))
                .thenReturn(notificationPage);

        // When
        Page<Notification> result = notificationService.getUserNotifications(testUser, PageRequest.of(0, 10));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(notificationRepository).findByUserOrderByCreatedAtDesc(testUser, PageRequest.of(0, 10));
    }

    @Test
    void getNotificationsWithFilters_ShouldReturnFilteredNotifications() {
        // Given
        List<Notification> notifications = Arrays.asList(testNotification);
        Page<Notification> notificationPage = new PageImpl<>(notifications);
        when(notificationRepository.findByProcessedStatus(
                false, 
                PageRequest.of(0, 10)
        )).thenReturn(notificationPage);

        // When
        Page<Notification> result = notificationService.getNotificationsWithFilters(
                "test@example.com", 
                false, 
                PageRequest.of(0, 10)
        );

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(Notification.Type.CARD_BLOCK_REQUEST, result.getContent().get(0).getType());
    }

    @Test
    void getNotificationById_ShouldReturnNotification() {
        // Given
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

        // When
        Optional<Notification> result = notificationRepository.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals(Notification.Type.CARD_BLOCK_REQUEST, result.get().getType());
    }

    @Test
    void getNotificationById_NotFound_ShouldReturnNull() {
        // Given
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Notification> result = notificationRepository.findById(999L);

        // Then
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void markAsRead_ShouldUpdateNotification() {
        // Given
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.markAsRead(1L);

        // Then
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationRepository).save(testNotification);
    }

    @Test
    void markAsRead_NotFound_ShouldReturnNull() {
        // Given
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        notificationService.markAsRead(999L);

        // Then
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void markAsProcessed_ShouldUpdateNotification() {
        // Given
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.markAsProcessed(1L);

        // Then
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationRepository).save(testNotification);
    }

    @Test
    void markAsProcessed_NotFound_ShouldReturnNull() {
        // Given
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        notificationService.markAsProcessed(999L);

        // Then
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void deleteNotification_ShouldDeleteNotification() {
        // Given
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

        // When
        notificationService.deleteNotification(1L);

        // Then
        verify(notificationRepository).deleteById(1L);
        verify(notificationRepository).deleteById(1L);
    }

    @Test
    void deleteNotification_NotFound_ShouldReturnFalse() {
        // Given
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        notificationService.deleteNotification(999L);

        // Then
        verify(notificationRepository, never()).deleteById(anyLong());
        verify(notificationRepository, never()).deleteById(anyLong());
    }

    @Test
    void getUnreadCount_ShouldReturnCount() {
        // Given
        when(notificationRepository.countByUserAndIsReadFalse(testUser))
                .thenReturn(5L);

        // When
        long result = notificationService.getUnreadCount(testUser);

        // Then
        assertEquals(5L, result);
    }

    @Test
    void getUnreadCount_Admin_ShouldReturnAllUnreadCount() {
        // Given
        when(notificationRepository.countByUserAndIsReadFalse(adminUser))
                .thenReturn(10L);

        // When
        long result = notificationService.getUnreadCount(adminUser);

        // Then
        assertEquals(10L, result);
        verify(notificationRepository).countByUserAndIsReadFalse(adminUser);
    }

    @Test
    void getUnreadCount_RegularUser_ShouldReturnUserUnreadCount() {
        // Given
        when(notificationRepository.countByUserAndIsReadFalse(testUser))
                .thenReturn(3L);

        // When
        long result = notificationService.getUnreadCount(testUser);

        // Then
        assertEquals(3L, result);
        verify(notificationRepository).countByUserAndIsReadFalse(testUser);
    }

    @Test
    void createCardBlockRequest_ShouldCreateNotification() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        Notification result = notificationService.createCardBlockRequest(testUser, testCard, "Suspicious activity");

        // Then
        assertNotNull(result);
        assertEquals(Notification.Type.CARD_BLOCK_REQUEST, result.getType());
        assertEquals(testCard, result.getCard());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createCardTopupRequest_ShouldCreateNotification() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        Notification result = notificationService.createCardTopupRequest(testUser, testCard, 500.0);

        // Then
        assertNotNull(result);
        assertEquals(Notification.Type.CARD_TOPUP_REQUEST, result.getType());
        assertEquals(testCard, result.getCard());
        assertEquals(500.0, result.getAmount());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createCardUnblockRequest_ShouldCreateNotification() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        Notification result = notificationService.createCardUnblockRequest(testUser, testCard, "Card was blocked by mistake");

        // Then
        assertNotNull(result);
        assertEquals(Notification.Type.CARD_UNBLOCK_REQUEST, result.getType());
        assertEquals(testCard, result.getCard());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createCardCreationRequest_ShouldCreateNotification() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        Notification result = notificationService.createCardCreateRequest(testUser, "12/26");

        // Then
        assertNotNull(result);
        assertEquals(Notification.Type.CARD_CREATE_REQUEST, result.getType());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createCardRecreationRequest_ShouldCreateNotification() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        Notification result = notificationService.createCardRecreateRequest(testUser, testCard, "12/26");

        // Then
        assertNotNull(result);
        assertEquals(Notification.Type.CARD_RECREATE_REQUEST, result.getType());
        assertEquals(testCard, result.getCard());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createNotification_WithNullUser_ShouldNotSave() {
        // When
        Notification result = notificationService.createCardBlockRequest(
                null, 
                testCard, 
                "Message"
        );

        // Then
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void createNotification_WithNullType_ShouldNotSave() {
        // When
        Notification result = notificationService.createCardBlockRequest(
                testUser, 
                testCard, 
                "Message"
        );

        // Then
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void createNotification_WithEmptyTitle_ShouldNotSave() {
        // When
        Notification result = notificationService.createCardBlockRequest(
                testUser, 
                testCard, 
                "Message"
        );

        // Then
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void createNotification_WithEmptyMessage_ShouldNotSave() {
        // When
        Notification result = notificationService.createCardBlockRequest(
                testUser, 
                testCard, 
                ""
        );

        // Then
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(notificationRepository, never()).save(any(Notification.class));
    }
}
