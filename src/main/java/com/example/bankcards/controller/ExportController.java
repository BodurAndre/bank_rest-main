package com.example.bankcards.controller;

import com.example.bankcards.entity.User;
import com.example.bankcards.service.ExportService;
import com.example.bankcards.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Контроллер для экспорта данных
 */
@RestController
@RequestMapping("/api/export")
@PreAuthorize("hasRole('USER')")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @Autowired
    private UserService userService;

    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");

    /**
     * Экспорт истории переводов в CSV
     */
    @GetMapping("/transfers/csv")
    public ResponseEntity<String> exportTransfersCSV(Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            String csvData = exportService.exportTransfersToCSV(currentUser);
            String filename = "transfers_" + LocalDateTime.now().format(FILE_DATE_FORMATTER) + ".csv";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при экспорте данных: " + e.getMessage());
        }
    }

    /**
     * Экспорт списка карт в CSV
     */
    @GetMapping("/cards/csv")
    public ResponseEntity<String> exportCardsCSV(Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            String csvData = exportService.exportCardsToCSV(currentUser);
            String filename = "cards_" + LocalDateTime.now().format(FILE_DATE_FORMATTER) + ".csv";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при экспорте данных: " + e.getMessage());
        }
    }

    /**
     * Экспорт истории переводов в PDF
     */
    @GetMapping("/transfers/pdf")
    public ResponseEntity<byte[]> exportTransfersPDF(Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            byte[] pdfData = exportService.exportTransfersToPDF(currentUser);
            String filename = "transfers_" + LocalDateTime.now().format(FILE_DATE_FORMATTER) + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfData);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Экспорт списка карт в PDF
     */
    @GetMapping("/cards/pdf")
    public ResponseEntity<byte[]> exportCardsPDF(Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            byte[] pdfData = exportService.exportCardsToPDF(currentUser);
            String filename = "cards_" + LocalDateTime.now().format(FILE_DATE_FORMATTER) + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfData);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}
