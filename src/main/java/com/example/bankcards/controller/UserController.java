package com.example.bankcards.controller;

import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Контроллер для управления пользователями
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Поиск пользователей по имени, email или ID
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSearchResult>> searchUsers(@RequestParam String q) {
        List<User> users = userService.searchUsers(q);
        
        List<UserSearchResult> results = users.stream()
                .map(user -> new UserSearchResult(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getRole().name()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(results);
    }

    /**
     * DTO для результатов поиска пользователей
     */
    public static class UserSearchResult {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String role;

        public UserSearchResult(Long id, String firstName, String lastName, String email, String role) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.role = role;
        }

        // Getters
        public Long getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
    }
}
