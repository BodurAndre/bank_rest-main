package com.example.bankcards.controller;

import com.example.bankcards.entity.Notification;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.NotificationService;
import com.example.bankcards.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Контроллер для работы с уведомлениями
 */
@Controller
@RequestMapping("/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Страница уведомлений (только для админов)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String notificationsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            Authentication authentication,
            Model model) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // Определяем статус фильтрации
        Boolean processed = null;
        if ("processed".equals(status)) {
            processed = true;
        } else if ("unprocessed".equals(status)) {
            processed = false;
        }
        
        Page<Notification> notifications = notificationService.getNotificationsWithFilters(search, processed, pageable);
        Long unreadCount = notificationService.getUnreadCount(currentUser);
        
        model.addAttribute("notifications", notifications.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", notifications.getTotalPages());
        model.addAttribute("totalElements", notifications.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("isAdmin", true);
        
        return "notifications/list";
    }
    
    /**
     * Отметить уведомление как прочитанное (только для админов)
     */
    @PostMapping("/{id}/read")
    @PreAuthorize("hasRole('ADMIN')")
    public String markAsRead(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            notificationService.markAsRead(id);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Уведомление отмечено как прочитанное");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Ошибка при обновлении уведомления: " + e.getMessage());
        }
        
        return "redirect:/notifications";
    }
    
    /**
     * Отметить все уведомления как прочитанные (только для админов)
     */
    @PostMapping("/mark-all-read")
    @PreAuthorize("hasRole('ADMIN')")
    public String markAllAsRead(
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        try {
            notificationService.markAllAsRead(currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Все уведомления отмечены как прочитанные");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Ошибка при обновлении уведомлений: " + e.getMessage());
        }
        
        return "redirect:/notifications";
    }
    
    /**
     * Отметить уведомление как обработанное (только для админов)
     */
    @PostMapping("/{id}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public String markAsProcessed(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            notificationService.markAsProcessed(id);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Уведомление отмечено как обработанное");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Ошибка при обновлении уведомления: " + e.getMessage());
        }
        
        return "redirect:/notifications";
    }
    
    /**
     * Показать детали уведомления (только для админов)
     */
    @GetMapping("/{id}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public String notificationDetails(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {
        
        // Здесь можно добавить логику для показа деталей уведомления
        // Пока просто редиректим на список
        return "redirect:/notifications";
    }
}
