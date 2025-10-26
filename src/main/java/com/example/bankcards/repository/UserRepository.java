package com.example.bankcards.repository;

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
 * Репозиторий для работы с пользователями
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Находит пользователя по email
     */
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);
    
    /**
     * Проверяет существование пользователя по email
     */
    boolean existsByEmail(String email);
    
    /**
     * Поиск пользователей по имени, email или ID
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "CAST(u.id AS string) LIKE CONCAT('%', :query, '%')")
    List<User> searchUsers(@Param("query") String query);

    /**
     * Поиск пользователей с пагинацией
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "CAST(u.id AS string) LIKE CONCAT('%', :query, '%')")
    Page<User> searchUsersWithPagination(@Param("query") String query, Pageable pageable);

    /**
     * Находит пользователей по роли
     */
    List<User> findByRole(User.Role role);

    /**
     * Находит пользователей по роли и email (содержит)
     */
    List<User> findByRoleAndEmailContainingIgnoreCase(User.Role role, String email);

    /**
     * Находит пользователей по email (содержит)
     */
    List<User> findByEmailContainingIgnoreCase(String email);
}
