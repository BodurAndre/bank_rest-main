package com.example.bankcards.repository;

import com.example.bankcards.entity.Transfer;
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
 * Репозиторий для работы с переводами
 */
@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    /**
     * Находит все переводы пользователя
     */
    @Query("SELECT t FROM Transfer t WHERE " +
           "(t.fromCard.owner = :user OR t.toCard.owner = :user)")
    Page<Transfer> findByUser(@Param("user") User user, Pageable pageable);

    /**
     * Находит переводы пользователя за период
     */
    @Query("SELECT t FROM Transfer t WHERE " +
           "(t.fromCard.owner = :user OR t.toCard.owner = :user) AND " +
           "t.createdAt BETWEEN :startDate AND :endDate")
    List<Transfer> findByUserAndDateRange(@Param("user") User user,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Находит переводы по статусу
     */
    List<Transfer> findByStatus(Transfer.Status status);

    /**
     * Подсчитывает количество переводов пользователя
     */
    @Query("SELECT COUNT(t) FROM Transfer t WHERE " +
           "(t.fromCard.owner = :user OR t.toCard.owner = :user)")
    long countByUser(@Param("user") User user);

    /**
     * Подсчитывает общую сумму переводов пользователя
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transfer t WHERE " +
           "(t.fromCard.owner = :user OR t.toCard.owner = :user) AND " +
           "t.status = 'COMPLETED'")
    double getTotalAmountByUser(@Param("user") User user);

    /**
     * Подсчитывает переводы пользователя за текущий месяц
     */
    @Query("SELECT COUNT(t) FROM Transfer t WHERE " +
           "(t.fromCard.owner = :user OR t.toCard.owner = :user) AND " +
           "t.createdAt >= :startOfMonth")
    long countByUserThisMonth(@Param("user") User user, @Param("startOfMonth") LocalDateTime startOfMonth);

    /**
     * Подсчитывает сумму переводов пользователя за текущий месяц
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transfer t WHERE " +
           "(t.fromCard.owner = :user OR t.toCard.owner = :user) AND " +
           "t.status = 'COMPLETED' AND t.createdAt >= :startOfMonth")
    double getAmountByUserThisMonth(@Param("user") User user, @Param("startOfMonth") LocalDateTime startOfMonth);
}
