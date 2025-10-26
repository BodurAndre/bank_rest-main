package com.example.bankcards.repository;

import com.example.bankcards.entity.AuditLog;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы с логами аудита
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Находит все логи пользователя
     */
    Page<AuditLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Находит логи пользователя по действию
     */
    Page<AuditLog> findByUserAndActionOrderByCreatedAtDesc(User user, String action, Pageable pageable);

    /**
     * Находит логи пользователя за период
     */
    @Query("SELECT a FROM AuditLog a WHERE a.user = :user AND a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<AuditLog> findByUserAndDateRange(@Param("user") User user, 
                                         @Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate, 
                                         Pageable pageable);

    /**
     * Находит все логи для админа
     */
    @Query("SELECT a FROM AuditLog a ORDER BY a.createdAt DESC")
    Page<AuditLog> findAllForAdmins(Pageable pageable);

    /**
     * Находит логи по действию для админа
     */
    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    /**
     * Находит логи по типу сущности для админа
     */
    Page<AuditLog> findByEntityTypeOrderByCreatedAtDesc(String entityType, Pageable pageable);

    /**
     * Находит логи за период для админа
     */
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<AuditLog> findByDateRangeForAdmins(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate, 
                                           Pageable pageable);

    /**
     * Подсчитывает количество действий пользователя за период
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.user = :user AND a.createdAt BETWEEN :startDate AND :endDate")
    long countByUserAndDateRange(@Param("user") User user, 
                                @Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);

    /**
     * Находит последние действия пользователя
     */
    List<AuditLog> findTop10ByUserOrderByCreatedAtDesc(User user);

    /**
     * Находит неудачные попытки входа
     */
    @Query("SELECT a FROM AuditLog a WHERE a.action = 'LOGIN' AND a.status = 'FAILED' AND a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<AuditLog> findFailedLoginAttempts(@Param("since") LocalDateTime since);

    /**
     * Находит логи с фильтрами для админа
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:userEmail IS NULL OR a.user.email LIKE %:userEmail%) " +
           "ORDER BY a.createdAt DESC")
    Page<AuditLog> findWithFilters(@Param("action") String action, 
                                  @Param("status") String status, 
                                  @Param("userEmail") String userEmail, 
                                  Pageable pageable);

    /**
     * Подсчитывает количество неудачных попыток входа
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action = 'LOGIN' AND a.status = 'FAILED' AND a.createdAt >= :since")
    long countFailedLoginAttempts(@Param("since") LocalDateTime since);

    /**
     * Подсчитывает количество действий за период
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    long countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Подсчитывает количество уникальных пользователей за период
     */
    @Query("SELECT COUNT(DISTINCT a.user) FROM AuditLog a WHERE a.user IS NOT NULL AND a.createdAt >= :since")
    long countUniqueUsers(@Param("since") LocalDateTime since);
}
