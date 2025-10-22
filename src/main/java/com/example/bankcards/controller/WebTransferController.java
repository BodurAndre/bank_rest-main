package com.example.bankcards.controller;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.dto.TransferStats;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.BankCardService;
import com.example.bankcards.service.TransferService;
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

import java.util.List;

/**
 * Веб-контроллер для переводов между картами
 */
@Controller
@RequestMapping("/transfers")
public class WebTransferController {

    @Autowired
    private TransferService transferService;

    @Autowired
    private BankCardService bankCardService;

    @Autowired
    private UserService userService;

    /**
     * Страница переводов
     */
    @GetMapping
    public String transfersPage(Authentication authentication, Model model) {
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        // Получаем активные карты пользователя
        List<BankCardDto> activeCards = bankCardService.findActiveCardsForUser(currentUser);
        
        model.addAttribute("activeCards", activeCards);
        model.addAttribute("transferRequest", new TransferRequest());
        
        return "transfers/transfer";
    }

    /**
     * Выполнение перевода
     */
    @PostMapping("/execute")
    public String executeTransfer(
            @ModelAttribute("transferRequest") TransferRequest request,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        try {
            TransferResponse response = transferService.transfer(request, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Перевод выполнен успешно! Сумма: " + response.getAmount());
            return "redirect:/transfers/history";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при переводе: " + e.getMessage());
            return "redirect:/transfers";
        }
    }

    /**
     * История переводов
     */
    @GetMapping("/history")
    public String transferHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication,
            Model model) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<TransferResponse> history = transferService.getTransferHistory(currentUser, pageable);
        
        model.addAttribute("history", history);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", history.getTotalPages());
        
        return "transfers/history";
    }

    /**
     * Детали перевода
     */
    @GetMapping("/{id}")
    public String transferDetails(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        return transferService.findById(id, currentUser)
                .map(transfer -> {
                    model.addAttribute("transfer", transfer);
                    return "transfers/details";
                })
                .orElse("redirect:/transfers/history?error=not_found");
    }

    /**
     * Статистика переводов
     */
    @GetMapping("/stats")
    public String transferStats(Authentication authentication, Model model) {
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        TransferStats stats = transferService.getTransferStats(currentUser);
        
        model.addAttribute("stats", stats);
        return "transfers/stats";
    }
}
