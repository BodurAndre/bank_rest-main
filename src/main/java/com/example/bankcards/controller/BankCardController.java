package com.example.bankcards.controller;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.CreateBankCardRequest;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.BankCardService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST контроллер для управления банковскими картами
 */
@RestController
@RequestMapping("/api/cards")
@Tag(name = "Bank Cards", description = "Управление банковскими картами")
@SecurityRequirement(name = "bearerAuth")
public class BankCardController {

    @Autowired
    private BankCardService bankCardService;

    @Autowired
    private UserService userService;

    /**
     * Создает новую банковскую карту (только для админа)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать карту", description = "Создает новую банковскую карту. Доступно только администраторам.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Карта успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    public ResponseEntity<BankCardDto> createCard(@Valid @RequestBody CreateBankCardRequest request) {
        BankCardDto card = bankCardService.createCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    /**
     * Получает карту по ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить карту", description = "Получает информацию о карте по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта найдена"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к карте")
    })
    public ResponseEntity<BankCardDto> getCard(
            @Parameter(description = "ID карты") @PathVariable Long id,
            Authentication authentication) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        // Проверяем, может ли пользователь просматривать эту карту
        if (!bankCardService.canUserManageCard(currentUser, id) && !currentUser.getRole().equals(User.Role.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return bankCardService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Получает все карты пользователя
     */
    @GetMapping("/my")
    @Operation(summary = "Мои карты", description = "Получает все карты текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список карт получен")
    })
    public ResponseEntity<Page<BankCardDto>> getMyCards(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Page<BankCardDto> cards = bankCardService.findByOwner(currentUser, pageable);
        return ResponseEntity.ok(cards);
    }

    /**
     * Получает все карты пользователя с фильтрацией
     */
    @GetMapping("/my/filter")
    @Operation(summary = "Мои карты с фильтром", description = "Получает карты пользователя с фильтрацией по статусу и поиску")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Отфильтрованный список карт")
    })
    public ResponseEntity<Page<BankCardDto>> getMyCardsWithFilter(
            Authentication authentication,
            @Parameter(description = "Статус карты") @RequestParam(required = false) BankCard.Status status,
            @Parameter(description = "Поисковый запрос") @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Page<BankCardDto> cards = bankCardService.findByOwnerWithFilters(currentUser, status, search, pageable);
        return ResponseEntity.ok(cards);
    }

    /**
     * Получает все карты (только для админа)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Все карты", description = "Получает все карты в системе. Доступно только администраторам.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список всех карт"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    public ResponseEntity<Page<BankCardDto>> getAllCards(
            @Parameter(description = "Статус карты") @RequestParam(required = false) BankCard.Status status,
            @Parameter(description = "Поисковый запрос") @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<BankCardDto> cards = bankCardService.findAllWithFilters(status, search, pageable);
        return ResponseEntity.ok(cards);
    }

    /**
     * Блокирует карту
     */
    @PutMapping("/{id}/block")
    @Operation(summary = "Заблокировать карту", description = "Блокирует карту с указанием причины")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта заблокирована"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к карте")
    })
    public ResponseEntity<BankCardDto> blockCard(
            @Parameter(description = "ID карты") @PathVariable Long id,
            @Parameter(description = "Причина блокировки") @RequestParam String reason,
            Authentication authentication) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        // Проверяем, может ли пользователь управлять этой картой
        if (!bankCardService.canUserManageCard(currentUser, id) && !currentUser.getRole().equals(User.Role.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            BankCardDto card = bankCardService.blockCard(id, reason);
            return ResponseEntity.ok(card);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Активирует карту
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Активировать карту", description = "Активирует заблокированную карту. Доступно только администраторам.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта активирована"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    public ResponseEntity<BankCardDto> activateCard(
            @Parameter(description = "ID карты") @PathVariable Long id) {
        
        try {
            BankCardDto card = bankCardService.activateCard(id);
            return ResponseEntity.ok(card);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Удаляет карту (только для админа)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить карту", description = "Удаляет карту из системы. Доступно только администраторам.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Карта удалена"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    public ResponseEntity<Void> deleteCard(@Parameter(description = "ID карты") @PathVariable Long id) {
        try {
            bankCardService.deleteCard(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Получает активные карты пользователя для переводов
     */
    @GetMapping("/my/active")
    @Operation(summary = "Активные карты", description = "Получает активные карты пользователя для переводов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список активных карт")
    })
    public ResponseEntity<java.util.List<BankCardDto>> getActiveCards(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        java.util.List<BankCardDto> cards = bankCardService.findActiveCardsForUser(currentUser);
        return ResponseEntity.ok(cards);
    }
}
