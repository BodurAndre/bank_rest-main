package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с пользователями
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Создает нового пользователя
     */
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует.");
        }
        
        // Устанавливаем username равным email, если он не установлен
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            user.setUsername(user.getEmail());
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User newUser = userRepository.save(user);
        userRepository.flush();
        return newUser;
    }

    /**
     * Находит пользователя по email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Проверяет существование пользователя по email
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Находит пользователя по ID
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Сохраняет пользователя
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Удаляет пользователя
     */
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Поиск пользователей по имени, email или ID
     */
    public List<User> searchUsers(String query) {
        return userRepository.searchUsers(query);
    }

    /**
     * Получение всех пользователей с пагинацией
     */
    public Page<User> findAllWithPagination(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Поиск пользователей с пагинацией
     */
    public Page<User> searchUsersWithPagination(String query, Pageable pageable) {
        return userRepository.searchUsersWithPagination(query, pageable);
    }


    /**
     * Обновление пользователя
     */
    public User updateUser(Long id, User userData) {
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        // Обновляем только непустые поля
        if (userData.getFirstName() != null && !userData.getFirstName().isEmpty()) {
            existingUser.setFirstName(userData.getFirstName());
        }
        if (userData.getLastName() != null && !userData.getLastName().isEmpty()) {
            existingUser.setLastName(userData.getLastName());
        }
        if (userData.getEmail() != null && !userData.getEmail().isEmpty()) {
            // Проверяем, что email не занят другим пользователем
            if (!existingUser.getEmail().equals(userData.getEmail()) && 
                userRepository.existsByEmail(userData.getEmail())) {
                throw new IllegalArgumentException("Пользователь с таким email уже существует");
            }
            existingUser.setEmail(userData.getEmail());
            existingUser.setUsername(userData.getEmail());
        }
        if (userData.getPassword() != null && !userData.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userData.getPassword()));
        }
        if (userData.getRole() != null) {
            existingUser.setRole(userData.getRole());
        }
        if (userData.getCountry() != null && !userData.getCountry().isEmpty()) {
            existingUser.setCountry(userData.getCountry());
        }
        if (userData.getGender() != null) {
            existingUser.setGender(userData.getGender());
        }
        if (userData.getDateOfBirth() != null) {
            existingUser.setDateOfBirth(userData.getDateOfBirth());
        }
        
        return userRepository.save(existingUser);
    }

    /**
     * Удаление пользователя
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Пользователь не найден");
        }
        userRepository.deleteById(id);
    }
}
