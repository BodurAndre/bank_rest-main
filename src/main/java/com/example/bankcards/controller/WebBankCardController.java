package com.example.bankcards.controller;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.CreateBankCardRequest;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.BankCardService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.service.NotificationService;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.entity.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
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
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private BankCardRepository bankCardRepository;

    /**
     * Страница со списком карт пользователя (только для обычных пользователей)
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
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
        model.addAttribute("isAdmin", currentUser.getRole() == User.Role.ADMIN);
        
        return "cards/list";
    }

    /**
     * Страница создания новой карты (только для админа)
     */
    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createCardForm(Model model) {
        model.addAttribute("createRequest", new CreateBankCardRequest());
        return "cards/create";
    }

    /**
     * Обработка создания карты (только для админа)
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createCard(
            @Valid @ModelAttribute("createRequest") CreateBankCardRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        // Проверяем ошибки валидации
        if (bindingResult.hasErrors()) {
            model.addAttribute("createRequest", request);
            return "cards/create";
        }
        
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
     * Блокировка карты (только для админа)
     */
    @PostMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
     * Страница пополнения карты (только для админа)
     */
    @GetMapping("/{id}/topup")
    @PreAuthorize("hasRole('ADMIN')")
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
     * Обработка пополнения карты (AJAX) (только для админа)
     */
    @PostMapping("/{id}/topup")
    @PreAuthorize("hasRole('ADMIN')")
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
    
    /**
     * Запрос на блокировку карты (для пользователей)
     */
    @PostMapping("/{id}/request-block")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> requestCardBlock(
            @PathVariable Long id,
            @RequestParam String reason,
            Authentication authentication) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        try {
            // Проверяем, что пользователь может управлять этой картой
            if (!bankCardService.canUserManageCard(currentUser, id)) {
                return ResponseEntity.status(403).body("Нет доступа к карте");
            }
            
            // Получаем карту через сервис
            BankCardDto cardDto = bankCardService.findById(id)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));
            
            // Получаем сущность карты для уведомления
            BankCard card = bankCardService.getCardEntityById(id)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));
            
            // Создаем уведомление для админов
            notificationService.createCardBlockRequest(currentUser, card, reason);
            
            // Устанавливаем флаг, что запрос на блокировку отправлен
            card.setBlockRequestSent(true);
            bankCardService.saveCard(card);
            
            return ResponseEntity.ok("✅ Запрос на блокировку карты отправлен администраторам");
            
        } catch (Exception e) {
            System.err.println("Error requesting card block: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Ошибка при отправке запроса: " + e.getMessage());
        }
    }
    
    /**
     * Запрос на пополнение карты (для пользователей)
     */
    @PostMapping("/{id}/request-topup")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> requestCardTopup(
            @PathVariable Long id,
            @RequestParam Double amount,
            Authentication authentication) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        try {
            // Проверяем, что пользователь может управлять этой картой
            if (!bankCardService.canUserManageCard(currentUser, id)) {
                return ResponseEntity.status(403).body("Нет доступа к карте");
            }
            
            // Получаем карту через сервис
            BankCardDto cardDto = bankCardService.findById(id)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));
            
            // Проверяем, что карта не заблокирована
            if (cardDto.getStatus() == BankCard.Status.BLOCKED) {
                return ResponseEntity.badRequest().body("❌ Карта заблокирована, пополнение невозможно");
            }
            
            // Проверяем, что не был отправлен запрос на блокировку
            if (cardDto.getBlockRequestSent() != null && cardDto.getBlockRequestSent()) {
                return ResponseEntity.badRequest().body("❌ По карте отправлен запрос на блокировку, пополнение невозможно");
            }
            
            // Получаем сущность карты для уведомления
            BankCard card = bankCardService.getCardEntityById(id)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));
            
            // Создаем уведомление для админов
            notificationService.createCardTopupRequest(currentUser, card, amount);
            
            return ResponseEntity.ok("✅ Запрос на пополнение карты отправлен администраторам");
            
        } catch (Exception e) {
            System.err.println("Error requesting card topup: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Ошибка при отправке запроса: " + e.getMessage());
        }
    }
    
    /**
     * Запрос на разблокировку карты (для пользователей)
     */
    @PostMapping("/{id}/request-unblock")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> requestCardUnblock(
            @PathVariable Long id,
            @RequestParam String reason,
            Authentication authentication) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        try {
            // Проверяем, что пользователь может управлять этой картой
            if (!bankCardService.canUserManageCard(currentUser, id)) {
                return ResponseEntity.status(403).body("Нет доступа к карте");
            }
            
            // Получаем карту через сервис
            BankCardDto cardDto = bankCardService.findById(id)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));
            
            // Проверяем, что карта заблокирована
            if (cardDto.getStatus() != BankCard.Status.BLOCKED) {
                return ResponseEntity.badRequest().body("❌ Карта не заблокирована, разблокировка не требуется");
            }
            
            // Получаем сущность карты для уведомления
            BankCard card = bankCardService.getCardEntityById(id)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));
            
            // Создаем уведомление для админов
            notificationService.createCardUnblockRequest(currentUser, card, reason);
            
            return ResponseEntity.ok("✅ Запрос на разблокировку карты отправлен администраторам");
            
        } catch (Exception e) {
            System.err.println("Error requesting card unblock: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Ошибка при отправке запроса: " + e.getMessage());
        }
    }
    
    /**
     * Запрос на создание новой карты (только для пользователей)
     */
    @PostMapping("/request-create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> requestCardCreate(
            @RequestParam String expiryDate,
            Authentication authentication) {

        String username = authentication.getName();
        User currentUser = userService.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        try {
            notificationService.createCardCreateRequest(currentUser, expiryDate);
            return ResponseEntity.ok("✅ Запрос на создание карты отправлен администраторам");

        } catch (Exception e) {
            System.err.println("Error requesting card creation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Ошибка при отправке запроса: " + e.getMessage());
        }
    }
    
    @PostMapping("/{cardId}/request-recreate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> requestCardRecreate(
            @PathVariable Long cardId,
            @RequestParam String newExpiryDate,
            Authentication authentication) {

        String username = authentication.getName();
        User currentUser = userService.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        try {
            System.out.println("🔄 Запрос на пересоздание карты: cardId=" + cardId + ", newExpiryDate=" + newExpiryDate + ", user=" + currentUser.getEmail());
            
            // Найти карту и проверить, что она принадлежит пользователю
            BankCard card = bankCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));
                
            System.out.println("🔍 Найдена карта: " + card.getMaskedNumber() + ", статус: " + card.getStatus() + ", истекла: " + card.isExpired());
                
            if (!card.getOwner().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).body("❌ Нет доступа к этой карте");
            }
            
            // Проверить, что карта истекла
            if (!card.isExpired()) {
                return ResponseEntity.badRequest().body("❌ Карта еще не истекла");
            }

            Notification notification = notificationService.createCardRecreateRequest(currentUser, card, newExpiryDate);
            System.out.println("✅ Создано уведомление: id=" + notification.getId() + ", тип=" + notification.getType());
            
            return ResponseEntity.ok("✅ Запрос на пересоздание карты отправлен администраторам");

        } catch (Exception e) {
            System.err.println("Error requesting card recreation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Ошибка при отправке запроса: " + e.getMessage());
        }
    }
}
