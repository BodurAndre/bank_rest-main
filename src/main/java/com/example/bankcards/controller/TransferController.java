package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.dto.TransferStats;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.service.TransferService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для переводов между картами
 */
@RestController
@RequestMapping("/api/transfers")
@Tag(name = "Transfers", description = "Переводы между банковскими картами")
@SecurityRequirement(name = "bearerAuth")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @Autowired
    private UserService userService;

    /**
     * Выполняет перевод между картами пользователя
     */
    @PostMapping
    @Operation(summary = "Выполнить перевод", description = "Выполняет перевод между картами текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Перевод выполнен успешно"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные или недостаточно средств"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к карте")
    })
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", username));
        
        TransferResponse response = transferService.transfer(request, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Получает историю переводов пользователя
     */
    @GetMapping("/history")
    @Operation(summary = "История переводов", description = "Получает историю переводов текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "История переводов получена")
    })
    public ResponseEntity<Page<TransferResponse>> getTransferHistory(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Page<TransferResponse> history = transferService.getTransferHistory(currentUser, pageable);
        return ResponseEntity.ok(history);
    }

    /**
     * Получает перевод по ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить перевод", description = "Получает информацию о переводе по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Перевод найден"),
            @ApiResponse(responseCode = "404", description = "Перевод не найден"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к переводу")
    })
    public ResponseEntity<TransferResponse> getTransfer(
            @Parameter(description = "ID перевода") @PathVariable Long id,
            Authentication authentication) {
        
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        return transferService.findById(id, currentUser)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Получает статистику переводов пользователя
     */
    @GetMapping("/stats")
    @Operation(summary = "Статистика переводов", description = "Получает статистику переводов текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статистика получена")
    })
    public ResponseEntity<TransferStats> getTransferStats(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userService.findByEmail(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        TransferStats stats = transferService.getTransferStats(currentUser);
        return ResponseEntity.ok(stats);
    }
}
