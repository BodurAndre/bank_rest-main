package com.example.bankcards.repository;

import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с банковскими картами
 */
@Repository
public interface BankCardRepository extends JpaRepository<BankCard, Long> {

    /**
     * Находит все карты пользователя
     */
    Page<BankCard> findByOwner(User owner, Pageable pageable);

    /**
     * Находит все карты пользователя по ID
     */
    Page<BankCard> findByOwnerId(Long ownerId, Pageable pageable);

    /**
     * Находит карту по номеру (зашифрованному)
     */
    Optional<BankCard> findByCardNumber(String cardNumber);

    /**
     * Находит карту по маскированному номеру
     */
    Optional<BankCard> findByMaskedNumber(String maskedNumber);

    /**
     * Находит все карты по статусу
     */
    List<BankCard> findByStatus(BankCard.Status status);

    /**
     * Находит все карты пользователя по статусу
     */
    List<BankCard> findByOwnerAndStatus(User owner, BankCard.Status status);

    /**
     * Находит все карты пользователя по статусу с пагинацией
     */
    Page<BankCard> findByOwnerAndStatus(User owner, BankCard.Status status, Pageable pageable);

    /**
     * Находит все карты пользователя с фильтрацией по статусу
     */
    @Query("SELECT bc FROM BankCard bc WHERE bc.owner = :owner AND " +
           "(:status IS NULL OR bc.status = :status) AND " +
           "(:searchTerm IS NULL OR bc.maskedNumber LIKE %:searchTerm%)")
    Page<BankCard> findByOwnerWithFilters(@Param("owner") User owner,
                                         @Param("status") BankCard.Status status,
                                         @Param("searchTerm") String searchTerm,
                                         Pageable pageable);

    /**
     * Находит все карты с фильтрацией (для админа)
     */
    @Query("SELECT bc FROM BankCard bc WHERE " +
           "(:status IS NULL OR bc.status = :status) AND " +
           "(:searchTerm IS NULL OR bc.maskedNumber LIKE %:searchTerm% OR bc.owner.email LIKE %:searchTerm%)")
    Page<BankCard> findAllWithFilters(@Param("status") BankCard.Status status,
                                     @Param("searchTerm") String searchTerm,
                                     Pageable pageable);

    /**
     * Находит все истекшие карты
     */
    @Query("SELECT bc FROM BankCard bc WHERE bc.expiryDate < CURRENT_DATE AND bc.status = 'ACTIVE'")
    List<BankCard> findExpiredCards();

    /**
     * Находит карты пользователя, которые можно использовать для переводов
     */
    @Query("SELECT bc FROM BankCard bc WHERE bc.owner = :owner AND bc.status = 'ACTIVE' AND bc.expiryDate >= CURRENT_DATE")
    List<BankCard> findActiveCardsForUser(@Param("owner") User owner);

    /**
     * Подсчитывает количество карт пользователя
     */
    long countByOwner(User owner);

    /**
     * Подсчитывает количество карт по статусу
     */
    long countByStatus(BankCard.Status status);

    /**
     * Проверяет, существует ли карта с таким номером
     */
    boolean existsByCardNumber(String cardNumber);

    /**
     * Проверяет, существует ли карта с таким маскированным номером
     */
    boolean existsByMaskedNumber(String maskedNumber);
}
