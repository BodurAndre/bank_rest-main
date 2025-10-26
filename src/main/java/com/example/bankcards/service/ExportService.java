package com.example.bankcards.service;

import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.entity.AuditLog;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.AuditLogRepository;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Сервис для экспорта данных в различные форматы
 */
@Service
public class ExportService {

    @Autowired
    private TransferService transferService;

    @Autowired
    private BankCardService bankCardService;
    
    @Autowired
    private AuditService auditService;

    @Autowired
    private BankCardRepository bankCardRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Экспорт истории переводов в CSV
     */
    public String exportTransfersToCSV(User user) throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("Дата", "От карты", "На карту", "Сумма (₽)", "Описание", "Статус")
                .build();

        try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
            // Получаем все переводы пользователя (большой размер страницы)
            Pageable pageable = PageRequest.of(0, 1000);
            Page<TransferResponse> transfers = transferService.getTransferHistory(user, pageable);

            for (TransferResponse transfer : transfers.getContent()) {
                csvPrinter.printRecord(
                        transfer.getCreatedAt().format(DATE_FORMATTER),
                        transfer.getFromCardMasked(),
                        transfer.getToCardMasked(),
                        String.format("%.2f", transfer.getAmount()),
                        transfer.getDescription(),
                        transfer.getStatus()
                );
            }
            
            // Логируем экспорт данных
            auditService.logDataExport(user, "Переводы", "CSV");
        }

        return writer.toString();
    }

    /**
     * Экспорт списка карт в CSV
     */
    public String exportCardsToCSV(User user) throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("Номер карты", "Баланс (₽)", "Статус", "Срок действия", "Дата создания")
                .build();

        try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
            // Получаем все карты пользователя
            Pageable pageable = PageRequest.of(0, 100);
            Page<BankCardDto> cards = bankCardService.findByOwner(user, pageable);

            for (BankCardDto card : cards.getContent()) {
                csvPrinter.printRecord(
                        card.getMaskedNumber(),
                        String.format("%.2f", card.getBalance()),
                        card.getStatus(),
                        card.getExpiryDate(),
                        card.getCreatedAt().format(DATE_FORMATTER)
                );
            }
            
            // Логируем экспорт данных
            auditService.logDataExport(user, "Карты", "CSV");
        }

        return writer.toString();
    }

    /**
     * Экспорт истории переводов в PDF
     */
    public byte[] exportTransfersToPDF(User user) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Заголовок
        document.add(new Paragraph("История переводов")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold());

        document.add(new Paragraph("Пользователь: " + user.getFirstName() + " " + user.getLastName())
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12));

        document.add(new Paragraph("Дата формирования: " + LocalDateTime.now().format(DATE_FORMATTER))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10));

        document.add(new Paragraph("\n"));

        // Получаем переводы
        Pageable pageable = PageRequest.of(0, 1000);
        Page<TransferResponse> transfers = transferService.getTransferHistory(user, pageable);

        if (transfers.getContent().isEmpty()) {
            document.add(new Paragraph("Переводы отсутствуют")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12));
        } else {
            // Создаем таблицу
            Table table = new Table(UnitValue.createPercentArray(new float[]{15, 20, 20, 15, 20, 10}))
                    .useAllAvailableWidth();

            // Заголовки таблицы
            table.addHeaderCell(new Cell().add(new Paragraph("Дата").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("От карты").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("На карту").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Сумма (₽)").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Описание").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Статус").setBold()));

            // Данные
            for (TransferResponse transfer : transfers.getContent()) {
                table.addCell(new Cell().add(new Paragraph(transfer.getCreatedAt().format(DATE_FORMATTER))));
                table.addCell(new Cell().add(new Paragraph(transfer.getFromCardMasked())));
                table.addCell(new Cell().add(new Paragraph(transfer.getToCardMasked())));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", transfer.getAmount()))));
                table.addCell(new Cell().add(new Paragraph(transfer.getDescription())));
                table.addCell(new Cell().add(new Paragraph(transfer.getStatus())));
            }

            document.add(table);
        }

        document.close();
        
        // Логируем экспорт данных
        auditService.logDataExport(user, "Переводы", "PDF");
        
        return baos.toByteArray();
    }

    /**
     * Экспорт списка карт в PDF
     */
    public byte[] exportCardsToPDF(User user) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Заголовок
        document.add(new Paragraph("Мои банковские карты")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold());

        document.add(new Paragraph("Пользователь: " + user.getFirstName() + " " + user.getLastName())
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12));

        document.add(new Paragraph("Дата формирования: " + LocalDateTime.now().format(DATE_FORMATTER))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10));

        document.add(new Paragraph("\n"));

        // Получаем карты
        Pageable pageable = PageRequest.of(0, 100);
        Page<BankCardDto> cards = bankCardService.findByOwner(user, pageable);

        if (cards.getContent().isEmpty()) {
            document.add(new Paragraph("Карты отсутствуют")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12));
        } else {
            // Создаем таблицу
            Table table = new Table(UnitValue.createPercentArray(new float[]{25, 20, 15, 20, 20}))
                    .useAllAvailableWidth();

            // Заголовки таблицы
            table.addHeaderCell(new Cell().add(new Paragraph("Номер карты").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Баланс (₽)").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Статус").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Срок действия").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Дата создания").setBold()));

            // Данные
            for (BankCardDto card : cards.getContent()) {
                table.addCell(new Cell().add(new Paragraph(card.getMaskedNumber())));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", card.getBalance()))));
                table.addCell(new Cell().add(new Paragraph(card.getStatus().toString())));
                table.addCell(new Cell().add(new Paragraph(card.getExpiryDate().toString())));
                table.addCell(new Cell().add(new Paragraph(card.getCreatedAt().format(DATE_FORMATTER))));
            }

            document.add(table);
        }

        document.close();
        
        // Логируем экспорт данных
        auditService.logDataExport(user, "Карты", "PDF");
        
        return baos.toByteArray();
    }

    // ============= МЕТОДЫ ЭКСПОРТА ДЛЯ АДМИНИСТРАТОРОВ =============

    /**
     * Экспорт карт в CSV для администратора с фильтрами
     */
    public byte[] exportCardsToCSVForAdmin(String status, String search, String ownerEmail) throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("Номер карты", "Владелец", "Email владельца", "Баланс (₽)", "Статус", "Срок действия", "Дата создания")
                .build();

        try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
            // Получаем карты с фильтрами
            Pageable pageable = PageRequest.of(0, 10000); // Большой размер для экспорта всех данных
            Page<BankCard> cards = getFilteredCards(status, search, ownerEmail, pageable);

            for (BankCard card : cards.getContent()) {
                csvPrinter.printRecord(
                        card.getMaskedNumber(),
                        card.getOwner().getFirstName() + " " + card.getOwner().getLastName(),
                        card.getOwner().getEmail(),
                        String.format("%.2f", card.getBalance()),
                        card.getStatus().toString(),
                        card.getExpiryDate().format(DateTimeFormatter.ofPattern("MM/yy")),
                        card.getCreatedAt().format(DATE_FORMATTER)
                );
            }
        }

        return writer.toString().getBytes();
    }

    /**
     * Экспорт карт в PDF для администратора с фильтрами
     */
    public byte[] exportCardsToPDFForAdmin(String status, String search, String ownerEmail) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter pdfWriter = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(pdfWriter);
        Document document = new Document(pdf);

        // Заголовок
        document.add(new Paragraph("Отчет по банковским картам")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold());

        document.add(new Paragraph("Дата формирования: " + LocalDateTime.now().format(DATE_FORMATTER))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10));

        // Фильтры
        StringBuilder filters = new StringBuilder("Фильтры: ");
        if (status != null && !status.isEmpty()) filters.append("Статус: ").append(status).append("; ");
        if (search != null && !search.isEmpty()) filters.append("Поиск: ").append(search).append("; ");
        if (ownerEmail != null && !ownerEmail.isEmpty()) filters.append("Email: ").append(ownerEmail).append("; ");
        
        document.add(new Paragraph(filters.toString())
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10));

        document.add(new Paragraph("\n"));

        // Получаем карты с фильтрами
        Pageable pageable = PageRequest.of(0, 10000);
        Page<BankCard> cards = getFilteredCards(status, search, ownerEmail, pageable);

        if (cards.getContent().isEmpty()) {
            document.add(new Paragraph("Карты не найдены")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12));
        } else {
            // Создаем таблицу
            Table table = new Table(UnitValue.createPercentArray(new float[]{20, 15, 20, 15, 10, 10, 10}))
                    .useAllAvailableWidth();

            // Заголовки таблицы
            table.addHeaderCell(new Cell().add(new Paragraph("Номер карты").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Владелец").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Email").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Баланс (₽)").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Статус").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Срок действия").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Создана").setBold()));

            // Данные
            for (BankCard card : cards.getContent()) {
                table.addCell(new Cell().add(new Paragraph(card.getMaskedNumber())));
                table.addCell(new Cell().add(new Paragraph(card.getOwner().getFirstName() + " " + card.getOwner().getLastName())));
                table.addCell(new Cell().add(new Paragraph(card.getOwner().getEmail())));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", card.getBalance()))));
                table.addCell(new Cell().add(new Paragraph(card.getStatus().toString())));
                table.addCell(new Cell().add(new Paragraph(card.getExpiryDate().format(DateTimeFormatter.ofPattern("MM/yy")))));
                table.addCell(new Cell().add(new Paragraph(card.getCreatedAt().format(DATE_FORMATTER))));
            }

            document.add(table);
        }

        document.close();
        return baos.toByteArray();
    }

    /**
     * Экспорт переводов в CSV для администратора с фильтрами
     */
    public byte[] exportTransfersToCSVForAdmin(String userEmail, String fromDate, String toDate) throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("Дата", "Пользователь", "Email", "С карты", "На карту", "Сумма (₽)", "Описание", "Статус")
                .build();

        try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
            // Получаем переводы с фильтрами
            List<TransferResponse> transfers = getFilteredTransfers(userEmail, fromDate, toDate);

            for (TransferResponse transfer : transfers) {
                // Получаем информацию о пользователе (нужно добавить в TransferResponse)
                csvPrinter.printRecord(
                        transfer.getCreatedAt().format(DATE_FORMATTER),
                        "Пользователь", // TODO: добавить информацию о пользователе в TransferResponse
                        "email@example.com", // TODO: добавить email пользователя
                        transfer.getFromCardMasked(),
                        transfer.getToCardMasked(),
                        String.format("%.2f", transfer.getAmount()),
                        transfer.getDescription(),
                        transfer.getStatus()
                );
            }
        }

        return writer.toString().getBytes();
    }

    /**
     * Экспорт переводов в PDF для администратора с фильтрами
     */
    public byte[] exportTransfersToPDFForAdmin(String userEmail, String fromDate, String toDate) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter pdfWriter = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(pdfWriter);
        Document document = new Document(pdf);

        // Заголовок
        document.add(new Paragraph("Отчет по переводам")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold());

        document.add(new Paragraph("Дата формирования: " + LocalDateTime.now().format(DATE_FORMATTER))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10));

        document.add(new Paragraph("\n"));

        // Получаем переводы с фильтрами
        List<TransferResponse> transfers = getFilteredTransfers(userEmail, fromDate, toDate);

        if (transfers.isEmpty()) {
            document.add(new Paragraph("Переводы не найдены")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12));
        } else {
            // Создаем таблицу
            Table table = new Table(UnitValue.createPercentArray(new float[]{15, 15, 15, 15, 15, 15, 10}))
                    .useAllAvailableWidth();

            // Заголовки таблицы
            table.addHeaderCell(new Cell().add(new Paragraph("Дата").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Пользователь").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("С карты").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("На карту").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Сумма (₽)").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Описание").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Статус").setBold()));

            // Данные
            for (TransferResponse transfer : transfers) {
                table.addCell(new Cell().add(new Paragraph(transfer.getCreatedAt().format(DATE_FORMATTER))));
                table.addCell(new Cell().add(new Paragraph("Пользователь"))); // TODO: добавить информацию о пользователе
                table.addCell(new Cell().add(new Paragraph(transfer.getFromCardMasked())));
                table.addCell(new Cell().add(new Paragraph(transfer.getToCardMasked())));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", transfer.getAmount()))));
                table.addCell(new Cell().add(new Paragraph(transfer.getDescription())));
                table.addCell(new Cell().add(new Paragraph(transfer.getStatus())));
            }

            document.add(table);
        }

        document.close();
        return baos.toByteArray();
    }

    /**
     * Экспорт пользователей в CSV
     */
    public byte[] exportUsersToCSV(String role, String search) throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("Email", "Имя", "Фамилия", "Роль", "Дата рождения", "Дата регистрации", "Количество карт")
                .build();

        try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
            List<User> users = getFilteredUsers(role, search);

            for (User user : users) {
                long cardCount = bankCardRepository.countByOwner(user);
                csvPrinter.printRecord(
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getRole().toString(),
                        user.getDateOfBirth() != null ? user.getDateOfBirth() : "Не указана",
                        user.getCreatedAt().format(DATE_FORMATTER),
                        cardCount
                );
            }
        }

        return writer.toString().getBytes();
    }

    /**
     * Экспорт пользователей в PDF
     */
    public byte[] exportUsersToPDF(String role, String search) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter pdfWriter = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(pdfWriter);
        Document document = new Document(pdf);

        // Заголовок
        document.add(new Paragraph("Отчет по пользователям")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold());

        document.add(new Paragraph("Дата формирования: " + LocalDateTime.now().format(DATE_FORMATTER))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10));

        document.add(new Paragraph("\n"));

        List<User> users = getFilteredUsers(role, search);

        if (users.isEmpty()) {
            document.add(new Paragraph("Пользователи не найдены")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12));
        } else {
            // Создаем таблицу
            Table table = new Table(UnitValue.createPercentArray(new float[]{20, 15, 15, 10, 15, 15, 10}))
                    .useAllAvailableWidth();

            // Заголовки таблицы
            table.addHeaderCell(new Cell().add(new Paragraph("Email").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Имя").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Фамилия").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Роль").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Дата рождения").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Регистрация").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Карт").setBold()));

            // Данные
            for (User user : users) {
                long cardCount = bankCardRepository.countByOwner(user);
                table.addCell(new Cell().add(new Paragraph(user.getEmail())));
                table.addCell(new Cell().add(new Paragraph(user.getFirstName())));
                table.addCell(new Cell().add(new Paragraph(user.getLastName())));
                table.addCell(new Cell().add(new Paragraph(user.getRole().toString())));
                table.addCell(new Cell().add(new Paragraph(user.getDateOfBirth() != null ? user.getDateOfBirth() : "Не указана")));
                table.addCell(new Cell().add(new Paragraph(user.getCreatedAt().format(DATE_FORMATTER))));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(cardCount))));
            }

            document.add(table);
        }

        document.close();
        return baos.toByteArray();
    }

    /**
     * Экспорт аудит-логов в CSV
     */
    public byte[] exportAuditLogsToCSV(String action, String status, String userEmail) throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("Дата", "Пользователь", "Email", "Действие", "Описание", "IP адрес", "Статус", "Ошибка")
                .build();

        try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
            Pageable pageable = PageRequest.of(0, 10000);
            Page<AuditLog> auditLogs = auditLogRepository.findWithFilters(action, status, userEmail, pageable);

            for (AuditLog log : auditLogs.getContent()) {
                csvPrinter.printRecord(
                        log.getCreatedAt().format(DATE_FORMATTER),
                        log.getUser() != null ? log.getUser().getFirstName() + " " + log.getUser().getLastName() : "Система",
                        log.getUser() != null ? log.getUser().getEmail() : "N/A",
                        log.getAction(),
                        log.getDescription(),
                        log.getIpAddress() != null ? log.getIpAddress() : "N/A",
                        log.getStatus().toString(),
                        log.getErrorMessage() != null ? log.getErrorMessage() : ""
                );
            }
        }

        return writer.toString().getBytes();
    }

    /**
     * Экспорт аудит-логов в PDF
     */
    public byte[] exportAuditLogsToPDF(String action, String status, String userEmail) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter pdfWriter = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(pdfWriter);
        Document document = new Document(pdf);

        // Заголовок
        document.add(new Paragraph("Отчет по аудиту системы")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold());

        document.add(new Paragraph("Дата формирования: " + LocalDateTime.now().format(DATE_FORMATTER))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10));

        document.add(new Paragraph("\n"));

        Pageable pageable = PageRequest.of(0, 10000);
        Page<AuditLog> auditLogs = auditLogRepository.findWithFilters(action, status, userEmail, pageable);

        if (auditLogs.getContent().isEmpty()) {
            document.add(new Paragraph("Записи аудита не найдены")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12));
        } else {
            // Создаем таблицу
            Table table = new Table(UnitValue.createPercentArray(new float[]{15, 15, 15, 15, 15, 10, 15}))
                    .useAllAvailableWidth();

            // Заголовки таблицы
            table.addHeaderCell(new Cell().add(new Paragraph("Дата").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Пользователь").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Email").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Действие").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Описание").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Статус").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("IP адрес").setBold()));

            // Данные
            for (AuditLog log : auditLogs.getContent()) {
                table.addCell(new Cell().add(new Paragraph(log.getCreatedAt().format(DATE_FORMATTER))));
                table.addCell(new Cell().add(new Paragraph(log.getUser() != null ? log.getUser().getFirstName() + " " + log.getUser().getLastName() : "Система")));
                table.addCell(new Cell().add(new Paragraph(log.getUser() != null ? log.getUser().getEmail() : "N/A")));
                table.addCell(new Cell().add(new Paragraph(log.getAction())));
                table.addCell(new Cell().add(new Paragraph(log.getDescription())));
                table.addCell(new Cell().add(new Paragraph(log.getStatus().toString())));
                table.addCell(new Cell().add(new Paragraph(log.getIpAddress() != null ? log.getIpAddress() : "N/A")));
            }

            document.add(table);
        }

        document.close();
        return baos.toByteArray();
    }

    // ============= ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =============

    /**
     * Получает отфильтрованные карты
     */
    private Page<BankCard> getFilteredCards(String status, String search, String ownerEmail, Pageable pageable) {
        BankCard.Status statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = BankCard.Status.valueOf(status);
            } catch (IllegalArgumentException e) {
                // Игнорируем неверный статус
            }
        }
        
        return bankCardRepository.findAllWithFilters(statusEnum, search, pageable);
    }

    /**
     * Получает отфильтрованные переводы
     */
    private List<TransferResponse> getFilteredTransfers(String userEmail, String fromDate, String toDate) {
        // TODO: Реализовать фильтрацию переводов по пользователю и датам
        // Пока возвращаем пустой список
        return List.of();
    }

    /**
     * Получает отфильтрованных пользователей
     */
    private List<User> getFilteredUsers(String role, String search) {
        User.Role roleEnum = null;
        if (role != null && !role.isEmpty()) {
            try {
                roleEnum = User.Role.valueOf(role);
            } catch (IllegalArgumentException e) {
                // Игнорируем неверную роль
            }
        }
        
        if (roleEnum != null && search != null && !search.isEmpty()) {
            return userRepository.findByRoleAndEmailContainingIgnoreCase(roleEnum, search);
        } else if (roleEnum != null) {
            return userRepository.findByRole(roleEnum);
        } else if (search != null && !search.isEmpty()) {
            return userRepository.findByEmailContainingIgnoreCase(search);
        } else {
            return userRepository.findAll();
        }
    }
}
