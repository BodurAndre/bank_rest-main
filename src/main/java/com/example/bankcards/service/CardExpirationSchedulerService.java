package com.example.bankcards.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Сервис для автоматической проверки и обновления истекших карт
 */
@Service
public class CardExpirationSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(CardExpirationSchedulerService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private BankCardService bankCardService;

    /**
     * Проверяет и обновляет статус истекших карт каждый день в 00:01
     * Cron: секунды минуты часы день месяц день_недели
     */
    @Scheduled(cron = "0 1 0 * * ?")
    public void checkAndUpdateExpiredCards() {
        String currentTime = LocalDateTime.now().format(formatter);
        logger.info("🕐 Запуск проверки истекших карт: {}", currentTime);
        
        try {
            int updatedCount = bankCardService.updateExpiredCards();
            
            if (updatedCount > 0) {
                logger.info("✅ Обновлено {} истекших карт на статус EXPIRED", updatedCount);
            } else {
                logger.info("ℹ️ Истекших карт не найдено");
            }
            
        } catch (Exception e) {
            logger.error("❌ Ошибка при обновлении истекших карт: {}", e.getMessage(), e);
        }
        
        logger.info("🏁 Завершена проверка истекших карт: {}", LocalDateTime.now().format(formatter));
    }

    /**
     * Дополнительная проверка каждые 6 часов (для более частого мониторинга)
     * Cron: каждые 6 часов (в 00:05, 06:05, 12:05, 18:05)
     */
    @Scheduled(cron = "0 5 */6 * * ?")
    public void checkExpiredCardsFrequently() {
        String currentTime = LocalDateTime.now().format(formatter);
        logger.debug("🔍 Частая проверка истекших карт: {}", currentTime);
        
        try {
            int updatedCount = bankCardService.updateExpiredCards();
            
            if (updatedCount > 0) {
                logger.warn("⚠️ Найдено {} истекших карт при частой проверке! Обновлен статус на EXPIRED", updatedCount);
            } else {
                logger.debug("✓ Частая проверка: истекших карт не найдено");
            }
            
        } catch (Exception e) {
            logger.error("❌ Ошибка при частой проверке истекших карт: {}", e.getMessage(), e);
        }
    }

    /**
     * Тестовый метод для проверки работы планировщика (каждые 30 секунд)
     * Можно отключить в продакшене, убрав аннотацию @Scheduled
     */
    // @Scheduled(fixedRate = 30000) // Каждые 30 секунд (отключено по умолчанию)
    public void testScheduler() {
        logger.debug("🧪 Тест планировщика: {}", LocalDateTime.now().format(formatter));
    }
}

