package com.example.bankcards.controller;

import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import com.example.bankcards.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для главной страницы
 */
@Controller
public class HomeController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userService.findByEmail(username).orElse(null);
            
            if (user != null) {
                model.addAttribute("user", user);
                boolean isAdmin = user.getRole().equals(User.Role.ADMIN);
                model.addAttribute("isAdmin", isAdmin);
                
                // Добавляем счетчик непрочитанных уведомлений
                Long unreadCount = notificationService.getUnreadCount(user);
                model.addAttribute("unreadNotifications", unreadCount);
            }
        } else {
            model.addAttribute("isAdmin", false);
        }
        
        return "home";
    }
}
