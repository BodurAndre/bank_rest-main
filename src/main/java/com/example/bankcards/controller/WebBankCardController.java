package com.example.bankcards.controller;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.CreateBankCardRequest;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.BankCardService;
import com.example.bankcards.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Веб-контроллер для управления банковскими картами
 */
@Controller
@RequestMapping("/cards")
public class WebBankCardController {

    @Autowired
    private BankCardService bankCardService;

    @Autowired
    private UserService userService;

    /**
     * Страница со списком карт пользователя
     */
    @GetMapping
    public String myCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Authentication authentication,
            Model model) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        BankCard.Status statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = BankCard.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Игнорируем некорректный статус
            }
        }
        
        Page<BankCardDto> cards = bankCardService.findByOwnerWithFilters(currentUser, statusEnum, search, pageable);
        
        model.addAttribute("cards", cards);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", cards.getTotalPages());
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentSearch", search);
        
        return "cards/list";
    }

    /**
     * Страница создания новой карты (только для админа)
     */
    @GetMapping("/create")
    public String createCardForm(Model model) {
        model.addAttribute("createRequest", new CreateBankCardRequest());
        return "cards/create";
    }

    /**
     * Обработка создания карты
     */
    @PostMapping("/create")
    public String createCard(
            @ModelAttribute("createRequest") CreateBankCardRequest request,
            RedirectAttributes redirectAttributes) {
        
        try {
            BankCardDto card = bankCardService.createCard(request);
            redirectAttributes.addFlashAttribute("successMessage", "Карта успешно создана: " + card.getMaskedNumber());
            return "redirect:/cards/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при создании карты: " + e.getMessage());
            return "redirect:/cards/create";
        }
    }

    /**
     * Страница детальной информации о карте
     */
    @GetMapping("/{id}")
    public String cardDetails(@PathVariable Long id, Authentication authentication, Model model) {
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        if (!bankCardService.canUserManageCard(currentUser, id) && !currentUser.getRole().equals(User.Role.ADMIN)) {
            return "redirect:/cards?error=access_denied";
        }
        
        return bankCardService.findById(id)
                .map(card -> {
                    model.addAttribute("card", card);
                    return "cards/details";
                })
                .orElse("redirect:/cards?error=not_found");
    }

    /**
     * Блокировка карты
     */
    @PostMapping("/{id}/block")
    public ResponseEntity<?> blockCard(
            @PathVariable Long id,
            @RequestParam String reason,
            Authentication authentication) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        if (!bankCardService.canUserManageCard(currentUser, id) && !currentUser.getRole().equals(User.Role.ADMIN)) {
            return ResponseEntity.status(403).body("Нет доступа к карте");
        }
        
        try {
            bankCardService.blockCard(id, reason);
            return ResponseEntity.ok("✅ Карта успешно заблокирована");
        } catch (Exception e) {
            System.err.println("Error blocking card " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Ошибка при блокировке: " + e.getMessage());
        }
    }

    /**
     * Активация карты (только для админа)
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activateCard(
            @PathVariable Long id,
            Authentication authentication) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        if (!currentUser.getRole().equals(User.Role.ADMIN)) {
            return ResponseEntity.status(403).body("Недостаточно прав");
        }
        
        try {
            bankCardService.activateCardSimple(id);
            return ResponseEntity.ok("✅ Карта успешно активирована");
        } catch (Exception e) {
            System.err.println("Error activating card " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Ошибка при активации: " + e.getMessage());
        }
    }

    /**
     * Удаление карты (только для админа)
     */
    @PostMapping("/{id}/delete")
    public ResponseEntity<?> deleteCard(
            @PathVariable Long id,
            Authentication authentication) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        if (!currentUser.getRole().equals(User.Role.ADMIN)) {
            return ResponseEntity.status(403).body("Недостаточно прав");
        }
        
        try {
            bankCardService.deleteCard(id);
            return ResponseEntity.ok("🗑️ Карта успешно удалена");
        } catch (Exception e) {
            System.err.println("Error deleting card " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Ошибка при удалении: " + e.getMessage());
        }
    }

    /**
     * Страница всех карт (только для админа)
     */
    @GetMapping("/admin")
    public String allCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Authentication authentication,
            Model model) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        if (!currentUser.getRole().equals(User.Role.ADMIN)) {
            return "redirect:/cards?error=access_denied";
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        BankCard.Status statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = BankCard.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Игнорируем некорректный статус
            }
        }
        
        Page<BankCardDto> cards = bankCardService.findAllWithFilters(statusEnum, search, pageable);
        
        model.addAttribute("cards", cards);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", cards.getTotalPages());
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentSearch", search);
        model.addAttribute("isAdmin", true);
        
        return "cards/admin";
    }

    /**
     * Страница пополнения карты
     */
    @GetMapping("/{id}/topup")
    public String topupCardForm(@PathVariable Long id, Authentication authentication, Model model) {
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        if (!bankCardService.canUserManageCard(currentUser, id) && !currentUser.getRole().equals(User.Role.ADMIN)) {
            return "redirect:/cards?error=access_denied";
        }
        
        return bankCardService.findById(id)
                .map(card -> {
                    model.addAttribute("card", card);
                    return "cards/topup";
                })
                .orElse("redirect:/cards?error=not_found");
    }

    /**
     * Обработка пополнения карты (AJAX)
     */
    @PostMapping("/{id}/topup")
    public ResponseEntity<?> topupCard(
            @PathVariable Long id,
            @RequestParam Double amount,
            Authentication authentication) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        if (!bankCardService.canUserManageCard(currentUser, id) && !currentUser.getRole().equals(User.Role.ADMIN)) {
            return ResponseEntity.status(403).body("Нет доступа к карте");
        }
        
        try {
            bankCardService.topupCard(id, amount);
            return ResponseEntity.ok("Карта успешно пополнена на " + amount + " ₽");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при пополнении: " + e.getMessage());
        }
    }
}
