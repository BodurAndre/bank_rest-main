package com.example.bankcards.controller;

import com.example.bankcards.service.CardExpirationSchedulerService;
import com.example.bankcards.service.BankCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST контроллер для управления планировщиком задач
 */
@RestController
@RequestMapping("/api/scheduler")
@Tag(name = "Scheduler", description = "Управление планировщиком задач")
public class SchedulerController {

    @Autowired
    private CardExpirationSchedulerService schedulerService;

    @Autowired
    private BankCardService bankCardService;

    /**
     * Ручной запуск проверки истекших карт (только для админа)
     */
    @PostMapping("/check-expired-cards")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ручная проверка истекших карт", description = "Запускает проверку и обновление статуса истекших карт вручную")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Проверка выполнена успешно"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    public ResponseEntity<Map<String, Object>> checkExpiredCardsManually() {
        try {
            int updatedCount = bankCardService.updateExpiredCards();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Проверка истекших карт выполнена успешно");
            response.put("updatedCards", updatedCount);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Ошибка при проверке истекших карт: " + e.getMessage());
            response.put("updatedCards", 0);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Получение статистики по планировщику (только для админа)
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Статус планировщика", description = "Получает информацию о работе планировщика задач")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус получен успешно"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    public ResponseEntity<Map<String, Object>> getSchedulerStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("schedulerEnabled", true);
        response.put("dailyCheckTime", "00:01");
        response.put("frequentCheckInterval", "Every 6 hours at :05");
        response.put("nextDailyCheck", "Следующая проверка в 00:01");
        response.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
}
