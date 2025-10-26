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
        long userCount = userRepository.count();
        System.out.println("🔍 Проверка пользователей в базе данных: найдено " + userCount + " пользователей");
        
        // Проверяем, есть ли уже пользователи в базе
        if (userCount == 0) {
            System.out.println("📝 База данных пуста, создаем пользователей по умолчанию...");
            createDefaultUsers();
        } else {
            System.out.println("ℹ️ Пользователи уже существуют, пропускаем создание по умолчанию");
            
            // Проверяем, есть ли нужные пользователи
            boolean adminExists = userRepository.findByEmail("admin@git.com").isPresent();
            boolean userExists = userRepository.findByEmail("user@git.com").isPresent();
            
            System.out.println("   Админ (admin@git.com): " + (adminExists ? "✅ существует" : "❌ отсутствует"));
            System.out.println("   Пользователь (user@git.com): " + (userExists ? "✅ существует" : "❌ отсутствует"));
        }
    }

    private void createDefaultUsers() {
        try {
            // Создаем админа
            User admin = new User();
            admin.setEmail("admin@git.com");
            admin.setUsername("admin@git.com");
            String adminPasswordHash = passwordEncoder.encode("admin1");
            admin.setPassword(adminPasswordHash);
            admin.setFirstName("Администратор");
            admin.setLastName("Системы");
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            System.out.println("✅ Создан админ: admin@git.com");

            // Создаем обычного пользователя
            User user = new User();
            user.setEmail("user@git.com");
            user.setUsername("user@git.com");
            String userPasswordHash = passwordEncoder.encode("user12");
            user.setPassword(userPasswordHash);
            user.setFirstName("Пользователь");
            user.setLastName("Тестовый");
            user.setRole(User.Role.USER);
            userRepository.save(user);
            System.out.println("✅ Создан пользователь: user@git.com");

            System.out.println();
            System.out.println("🎉 Пользователи по умолчанию созданы успешно!");
            System.out.println("📋 Данные для входа:");
            System.out.println("   👑 Админ: admin@git.com / admin1");
            System.out.println("   👤 Пользователь: user@git.com / user12");
            System.out.println();
            
            // Проверяем, что пароли работают
            boolean adminPasswordWorks = passwordEncoder.matches("admin1", adminPasswordHash);
            boolean userPasswordWorks = passwordEncoder.matches("user12", userPasswordHash);
            
            System.out.println("🔐 Проверка паролей:");
            System.out.println("   Админ пароль: " + (adminPasswordWorks ? "✅ корректный" : "❌ ошибка"));
            System.out.println("   Пользователь пароль: " + (userPasswordWorks ? "✅ корректный" : "❌ ошибка"));
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка при создании пользователей по умолчанию: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
