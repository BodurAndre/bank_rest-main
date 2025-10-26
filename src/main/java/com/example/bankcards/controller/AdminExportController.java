package com.example.bankcards.controller;

import com.example.bankcards.entity.BankCard;
import com.example.bankcards.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Контроллер для экспорта данных администратором
 */
@Controller
@RequestMapping("/admin/export")
@PreAuthorize("hasRole('ADMIN')")
public class AdminExportController {

    @Autowired
    private ExportService exportService;

    /**
     * Экспорт карт в CSV с фильтрами
     */
    @GetMapping("/cards/csv")
    public ResponseEntity<ByteArrayResource> exportCardsCSV(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String ownerEmail) {

        try {
            byte[] csvData = exportService.exportCardsToCSVForAdmin(status, search, ownerEmail);
            
            String filename = generateFilename("cards", "csv");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(new ByteArrayResource(csvData));
                    
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при экспорте карт в CSV: " + e.getMessage());
        }
    }

    /**
     * Экспорт карт в PDF с фильтрами
     */
    @GetMapping("/cards/pdf")
    public ResponseEntity<ByteArrayResource> exportCardsPDF(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String ownerEmail) {

        try {
            byte[] pdfData = exportService.exportCardsToPDFForAdmin(status, search, ownerEmail);
            
            String filename = generateFilename("cards", "pdf");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new ByteArrayResource(pdfData));
                    
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при экспорте карт в PDF: " + e.getMessage());
        }
    }

    /**
     * Экспорт переводов в CSV с фильтрами
     */
    @GetMapping("/transfers/csv")
    public ResponseEntity<ByteArrayResource> exportTransfersCSV(
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {

        try {
            byte[] csvData = exportService.exportTransfersToCSVForAdmin(userEmail, fromDate, toDate);
            
            String filename = generateFilename("transfers", "csv");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(new ByteArrayResource(csvData));
                    
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при экспорте переводов в CSV: " + e.getMessage());
        }
    }

    /**
     * Экспорт переводов в PDF с фильтрами
     */
    @GetMapping("/transfers/pdf")
    public ResponseEntity<ByteArrayResource> exportTransfersPDF(
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {

        try {
            byte[] pdfData = exportService.exportTransfersToPDFForAdmin(userEmail, fromDate, toDate);
            
            String filename = generateFilename("transfers", "pdf");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new ByteArrayResource(pdfData));
                    
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при экспорте переводов в PDF: " + e.getMessage());
        }
    }

    /**
     * Экспорт пользователей в CSV
     */
    @GetMapping("/users/csv")
    public ResponseEntity<ByteArrayResource> exportUsersCSV(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search) {

        try {
            byte[] csvData = exportService.exportUsersToCSV(role, search);
            
            String filename = generateFilename("users", "csv");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(new ByteArrayResource(csvData));
                    
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при экспорте пользователей в CSV: " + e.getMessage());
        }
    }

    /**
     * Экспорт пользователей в PDF
     */
    @GetMapping("/users/pdf")
    public ResponseEntity<ByteArrayResource> exportUsersPDF(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search) {

        try {
            byte[] pdfData = exportService.exportUsersToPDF(role, search);
            
            String filename = generateFilename("users", "pdf");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new ByteArrayResource(pdfData));
                    
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при экспорте пользователей в PDF: " + e.getMessage());
        }
    }

    /**
     * Экспорт аудит-логов в CSV с фильтрами
     */
    @GetMapping("/audit/csv")
    public ResponseEntity<ByteArrayResource> exportAuditLogsCSV(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userEmail) {

        try {
            byte[] csvData = exportService.exportAuditLogsToCSV(action, status, userEmail);
            
            String filename = generateFilename("audit-logs", "csv");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(new ByteArrayResource(csvData));
                    
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при экспорте аудит-логов в CSV: " + e.getMessage());
        }
    }

    /**
     * Экспорт аудит-логов в PDF с фильтрами
     */
    @GetMapping("/audit/pdf")
    public ResponseEntity<ByteArrayResource> exportAuditLogsPDF(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userEmail) {

        try {
            byte[] pdfData = exportService.exportAuditLogsToPDF(action, status, userEmail);
            
            String filename = generateFilename("audit-logs", "pdf");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new ByteArrayResource(pdfData));
                    
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при экспорте аудит-логов в PDF: " + e.getMessage());
        }
    }

    /**
     * Генерирует имя файла с текущей датой
     */
    private String generateFilename(String dataType, String format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        return String.format("%s_%s.%s", dataType, timestamp, format);
    }
}
