package com.example.bankcards.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO для создания банковской карты
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBankCardRequest {
    
    @NotNull(message = "Email владельца обязателен")
    @Email(message = "Некорректный формат email")
    private String ownerEmail;
    
    @NotNull(message = "Срок действия обязателен")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Срок действия должен быть в формате MM/yy")
    private String expiryDate;
    
    /**
     * Преобразует строку срока действия в LocalDate
     */
    public LocalDate getExpiryDateAsLocalDate() {
        if (expiryDate == null || expiryDate.length() != 5) {
            throw new IllegalArgumentException("Некорректный формат срока действия");
        }
        
        String[] parts = expiryDate.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = 2000 + Integer.parseInt(parts[1]); // Преобразуем YY в YYYY
        
        // Возвращаем последний день месяца
        return LocalDate.of(year, month, 1).withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth());
    }
}
