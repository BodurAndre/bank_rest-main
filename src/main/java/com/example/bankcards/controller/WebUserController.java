package com.example.bankcards.controller;

import com.example.bankcards.entity.User;
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

import java.util.List;

@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class WebUserController {

    @Autowired
    private UserService userService;

    /**
     * Страница управления пользователями
     */
    @GetMapping
    public String usersPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            Model model,
            Authentication authentication) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<User> usersPage;
            
            if (search != null && !search.trim().isEmpty()) {
                usersPage = userService.searchUsersWithPagination(search, pageable);
            } else {
                usersPage = userService.findAllWithPagination(pageable);
            }
            
            model.addAttribute("users", usersPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", usersPage.getTotalPages());
            model.addAttribute("totalElements", usersPage.getTotalElements());
            model.addAttribute("size", size);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("search", search);
            
            return "users/list";
            
        } catch (Exception e) {
            System.err.println("Error loading users page: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Ошибка при загрузке пользователей: " + e.getMessage());
            return "users/list";
        }
    }

    /**
     * Страница создания пользователя
     */
    @GetMapping("/create")
    public String createUserPage(Model model) {
        model.addAttribute("user", new User());
        return "users/create";
    }

    /**
     * Создание нового пользователя
     */
    @PostMapping("/create")
    public String createUser(
            @ModelAttribute User user,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (!user.getPassword().equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Пароли не совпадают");
                return "redirect:/users/create";
            }
            
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Пользователь успешно создан");
            return "redirect:/users";
            
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Ошибка при создании пользователя: " + e.getMessage());
            return "redirect:/users/create";
        }
    }

    /**
     * Страница редактирования пользователя
     */
    @GetMapping("/{id}/edit")
    public String editUserPage(@PathVariable Long id, Model model) {
        try {
            User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            
            model.addAttribute("user", user);
            return "users/edit";
            
        } catch (Exception e) {
            System.err.println("Error loading user for edit: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/users?error=user_not_found";
        }
    }

    /**
     * Обновление пользователя
     */
    @PostMapping("/{id}/edit")
    public String updateUser(
            @PathVariable Long id,
            @ModelAttribute User user,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (newPassword != null && !newPassword.isEmpty()) {
                if (!newPassword.equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Пароли не совпадают");
                    return "redirect:/users/" + id + "/edit";
                }
                user.setPassword(newPassword);
            }
            
            userService.updateUser(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Пользователь успешно обновлен");
            return "redirect:/users";
            
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Ошибка при обновлении пользователя: " + e.getMessage());
            return "redirect:/users/" + id + "/edit";
        }
    }

    /**
     * Удаление пользователя
     */
    @PostMapping("/{id}/delete")
    public String deleteUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "🗑️ Пользователь успешно удален");
            return "redirect:/users";
            
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Ошибка при удалении пользователя: " + e.getMessage());
            return "redirect:/users";
        }
    }
}
