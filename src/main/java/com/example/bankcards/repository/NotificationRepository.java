package com.example.bankcards.repository;

import com.example.bankcards.entity.Notification;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с уведомлениями
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Находит уведомления для пользователя с пагинацией
     */
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    /**
     * Находит непрочитанные уведомления для пользователя
     */
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    /**
     * Подсчитывает количество непрочитанных уведомлений для пользователя
     */
    Long countByUserAndIsReadFalse(User user);
    
    /**
     * Находит уведомления с фильтрацией по статусу обработки
     */
    @Query("SELECT n FROM Notification n WHERE " +
           "(:processed IS NULL OR n.isProcessed = :processed) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> findByProcessedStatus(@Param("processed") Boolean processed, Pageable pageable);
    
    /**
     * Поиск уведомлений с фильтрацией
     */
    @Query("SELECT n FROM Notification n WHERE " +
           "(:processed IS NULL OR n.isProcessed = :processed) AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(n.message) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> searchNotificationsWithFilters(
            @Param("search") String search,
            @Param("processed") Boolean processed,
            Pageable pageable);
    
    /**
     * Находит уведомления по типу
     */
    @Query("SELECT n FROM Notification n WHERE n.type = :type ORDER BY n.createdAt DESC")
    Page<Notification> findByType(@Param("type") Notification.Type type, Pageable pageable);
    
    /**
     * Находит уведомления для админов (все уведомления)
     */
    @Query("SELECT n FROM Notification n ORDER BY n.createdAt DESC")
    Page<Notification> findAllForAdmins(Pageable pageable);
}
