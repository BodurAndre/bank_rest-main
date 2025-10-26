package com.example.bankcards.controller;

import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import com.example.bankcards.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Optional;

/**
 * Веб-контроллер для страниц логина и регистрации
 */
@Controller
public class WebController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthenticationManager authenticationManager;


    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        return "register";
    }

    @PostMapping("/register")
    public String createNewUser(HttpServletRequest request, @ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        try {
            String rawPassword = user.getPassword();
            user.setRole(User.Role.USER);
            user.setUsername(user.getEmail()); // Устанавливаем username равным email

            User newUser = userService.createUser(user);
            if (newUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: Не удалось создать пользователя.");
                return "redirect:/register";
            }
            
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(user.getEmail(), rawPassword);

            Authentication authentication = authenticationManager.authenticate(authToken);

            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);

            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

            return "redirect:/";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.invalidate();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(null);
        return "redirect:/login";
    }
    
    /**
     * Обработка ошибок
     */
    @GetMapping("/error")
    public String error() {
        return "error";
    }
    
    /**
     * Обработка запросов favicon.ico
     */
    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
    /**
     * Обработка запросов Chrome DevTools
     */
    @GetMapping("/.well-known/appspecific/com.chrome.devtools.json")
    public ResponseEntity<Void> chromeDevTools() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
