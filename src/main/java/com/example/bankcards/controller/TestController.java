package com.example.bankcards.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Тестовый контроллер для проверки паролей
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/password")
    public String testPassword(@RequestParam String password) {
        // Генерируем хеш для пароля
        String hash = passwordEncoder.encode(password);
        
        // Проверяем, что хеш работает
        boolean matches = passwordEncoder.matches(password, hash);
        
        return String.format("Password: %s\nHash: %s\nMatches: %s", password, hash, matches);
    }

    @GetMapping("/verify")
    public String verifyPassword(@RequestParam String password, @RequestParam String hash) {
        boolean matches = passwordEncoder.matches(password, hash);
        return String.format("Password: %s\nHash: %s\nMatches: %s", password, hash, matches);
    }
}
