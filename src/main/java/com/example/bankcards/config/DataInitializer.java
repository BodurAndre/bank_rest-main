package com.example.bankcards.config;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Инициализатор данных - создает пользователей по умолчанию
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Проверяем, есть ли уже пользователи в базе
        if (userRepository.count() == 0) {
            createDefaultUsers();
        }
    }

    private void createDefaultUsers() {
        // Создаем админа
        User admin = new User();
        admin.setEmail("admin@git.com");
        admin.setUsername("admin@git.com");
        admin.setPassword(passwordEncoder.encode("admin1"));
        admin.setFirstName("Администратор");
        admin.setLastName("Системы");
        admin.setRole(User.Role.ADMIN);
        userRepository.save(admin);

        // Создаем обычного пользователя
        User user = new User();
        user.setEmail("user@git.com");
        user.setUsername("user@git.com");
        user.setPassword(passwordEncoder.encode("user12"));
        user.setFirstName("Пользователь");
        user.setLastName("Тестовый");
        user.setRole(User.Role.USER);
        userRepository.save(user);

        System.out.println("✅ Созданы пользователи по умолчанию:");
        System.out.println("   Админ: admin@git.com / admin1");
        System.out.println("   Пользователь: user@git.com / user12");
    }
}
