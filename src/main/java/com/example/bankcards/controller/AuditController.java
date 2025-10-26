package com.example.bankcards.controller;

import com.example.bankcards.entity.AuditLog;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.AuditService;
import com.example.bankcards.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для просмотра аудита действий пользователей
 */
@Controller
@RequestMapping("/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserService userService;

    /**
     * Страница аудита для пользователя
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public String myAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            Authentication authentication,
            Model model) {

        String username = authentication.getName();
        User currentUser = userService.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs;

        if (action != null && !action.isEmpty()) {
            auditLogs = auditService.getUserAuditLogsByAction(currentUser, action, pageable);
        } else {
            auditLogs = auditService.getUserAuditLogs(currentUser, pageable);
        }

        // Получаем последние действия для быстрого доступа
        List<AuditLog> recentActions = auditService.getRecentUserActions(currentUser);

        model.addAttribute("auditLogs", auditLogs);
        model.addAttribute("recentActions", recentActions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", auditLogs.getTotalPages());
        model.addAttribute("totalElements", auditLogs.getTotalElements());
        model.addAttribute("currentAction", action);
        model.addAttribute("user", currentUser);

        return "audit/my-logs";
    }

    /**
     * Страница аудита для администратора
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userEmail,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs;

        // Применяем фильтры
        if ((action != null && !action.isEmpty()) || 
            (status != null && !status.isEmpty()) || 
            (userEmail != null && !userEmail.isEmpty())) {
            auditLogs = auditService.getAuditLogsWithFilters(action, status, userEmail, pageable);
        } else {
            auditLogs = auditService.getAllAuditLogsForAdmin(pageable);
        }

        // Получаем статистику
        List<AuditLog> securityAlerts = auditService.getRecentFailedLogins();
        long failedLoginsCount = auditService.getFailedLoginsCount();
        long todayActionsCount = auditService.getTodayActionsCount();
        long uniqueUsersCount = auditService.getUniqueUsersCount();

        model.addAttribute("auditLogs", auditLogs);
        model.addAttribute("securityAlerts", securityAlerts);
        model.addAttribute("failedLogins", failedLoginsCount);
        model.addAttribute("todayActions", todayActionsCount);
        model.addAttribute("uniqueUsers", uniqueUsersCount);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", auditLogs.getTotalPages());
        model.addAttribute("totalElements", auditLogs.getTotalElements());
        model.addAttribute("currentAction", action);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentUserEmail", userEmail);

        return "audit/admin-logs";
    }
}
