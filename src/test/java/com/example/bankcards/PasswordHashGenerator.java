package com.example.bankcards;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Утилита для генерации BCrypt хешей паролей
 * Запустите этот класс для получения правильных хешей
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String adminPassword = "admin1";
        String userPassword = "user12";
        
        String adminHash = encoder.encode(adminPassword);
        String userHash = encoder.encode(userPassword);
        
        System.out.println("Admin password hash for 'admin1':");
        System.out.println(adminHash);
        System.out.println();
        
        System.out.println("User password hash for 'user12':");
        System.out.println(userHash);
        System.out.println();
        
        // Проверяем, что хеши работают
        System.out.println("Verification:");
        System.out.println("Admin hash matches 'admin1': " + encoder.matches(adminPassword, adminHash));
        System.out.println("User hash matches 'user12': " + encoder.matches(userPassword, userHash));
    }
}
