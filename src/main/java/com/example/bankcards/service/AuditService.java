package com.example.bankcards.service;

import com.example.bankcards.entity.AuditLog;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для работы с аудитом действий пользователей
 */
@Service
@Transactional
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Записывает действие пользователя в лог аудита
     */
    public void logUserAction(User user, String action, String entityType, Long entityId, String description) {
        try {
            AuditLog auditLog = new AuditLog(user, action, entityType, entityId, description);
            
            // Получаем информацию о запросе
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Не бросаем исключение, чтобы не нарушить основную логику
            System.err.println("Error logging user action: " + e.getMessage());
        }
    }

    /**
     * Записывает неудачное действие пользователя
     */
    public void logFailedAction(User user, String action, String entityType, Long entityId, String description, String errorMessage) {
        try {
            AuditLog auditLog = new AuditLog(user, action, entityType, entityId, description);
            auditLog.setStatus(AuditLog.Status.FAILED);
            auditLog.setErrorMessage(errorMessage);
            
            // Получаем информацию о запросе
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            System.err.println("Error logging failed action: " + e.getMessage());
        }
    }

    /**
     * Записывает системное действие (без пользователя)
     */
    public void logSystemAction(String action, String entityType, Long entityId, String description) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setDescription(description);
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            System.err.println("Error logging system action: " + e.getMessage());
        }
    }

    /**
     * Получает логи пользователя
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getUserAuditLogs(User user, Pageable pageable) {
        return auditLogRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    /**
     * Получает логи пользователя по действию
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getUserAuditLogsByAction(User user, String action, Pageable pageable) {
        return auditLogRepository.findByUserAndActionOrderByCreatedAtDesc(user, action, pageable);
    }

    /**
     * Получает все логи для админа
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAllAuditLogsForAdmin(Pageable pageable) {
        return auditLogRepository.findAllForAdmins(pageable);
    }

    /**
     * Получает логи по действию для админа
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable);
    }

    /**
     * Получает последние действия пользователя
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getRecentUserActions(User user) {
        return auditLogRepository.findTop10ByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Получает неудачные попытки входа за последние 24 часа
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getRecentFailedLogins() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return auditLogRepository.findFailedLoginAttempts(since);
    }

    /**
     * Получает логи с фильтрами для админа
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsWithFilters(String action, String status, String userEmail, Pageable pageable) {
        return auditLogRepository.findWithFilters(action, status, userEmail, pageable);
    }

    /**
     * Получает количество неудачных попыток входа за последние 24 часа
     */
    @Transactional(readOnly = true)
    public long getFailedLoginsCount() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return auditLogRepository.countFailedLoginAttempts(since);
    }

    /**
     * Получает количество действий за сегодня
     */
    @Transactional(readOnly = true)
    public long getTodayActionsCount() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return auditLogRepository.countByDateRange(startOfDay, endOfDay);
    }

    /**
     * Получает количество уникальных пользователей за последние 30 дней
     */
    @Transactional(readOnly = true)
    public long getUniqueUsersCount() {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        return auditLogRepository.countUniqueUsers(since);
    }

    /**
     * Подсчитывает активность пользователя за период
     */
    @Transactional(readOnly = true)
    public long getUserActivityCount(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.countByUserAndDateRange(user, startDate, endDate);
    }

    /**
     * Получает IP адрес клиента
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    // Удобные методы для часто используемых действий
    
    public void logLogin(User user) {
        logUserAction(user, AuditLog.Actions.LOGIN, AuditLog.EntityTypes.SYSTEM, null, "Пользователь вошел в систему");
    }

    public void logLogout(User user) {
        logUserAction(user, AuditLog.Actions.LOGOUT, AuditLog.EntityTypes.SYSTEM, null, "Пользователь вышел из системы");
    }

    public void logCardCreation(User user, Long cardId, String cardNumber) {
        logUserAction(user, AuditLog.Actions.CREATE_CARD, AuditLog.EntityTypes.CARD, cardId, 
                     "Создана карта " + cardNumber);
    }

    public void logCardBlock(User user, Long cardId, String cardNumber, String reason) {
        logUserAction(user, AuditLog.Actions.BLOCK_CARD, AuditLog.EntityTypes.CARD, cardId, 
                     "Заблокирована карта " + cardNumber + ". Причина: " + reason);
    }

    public void logCardActivation(User user, Long cardId, String cardNumber) {
        logUserAction(user, AuditLog.Actions.ACTIVATE_CARD, AuditLog.EntityTypes.CARD, cardId, 
                     "Активирована карта " + cardNumber);
    }

    public void logCardDeletion(User user, Long cardId, String cardNumber) {
        logUserAction(user, AuditLog.Actions.DELETE_CARD, AuditLog.EntityTypes.CARD, cardId, 
                     "Удалена карта " + cardNumber);
    }

    public void logCardTopup(User user, Long cardId, String cardNumber, Double amount) {
        logUserAction(user, AuditLog.Actions.TOPUP_CARD, AuditLog.EntityTypes.CARD, cardId, 
                     "Пополнена карта " + cardNumber + " на сумму " + amount + " ₽");
    }

    public void logTransfer(User user, Long transferId, String fromCard, String toCard, Double amount) {
        logUserAction(user, AuditLog.Actions.TRANSFER, AuditLog.EntityTypes.TRANSFER, transferId, 
                     "Перевод " + amount + " ₽ с карты " + fromCard + " на карту " + toCard);
    }

    public void logDataExport(User user, String dataType, String format) {
        logUserAction(user, AuditLog.Actions.EXPORT_DATA, AuditLog.EntityTypes.SYSTEM, null, 
                     "Экспорт данных: " + dataType + " в формате " + format);
    }

    public void logViewCards(User user) {
        logUserAction(user, AuditLog.Actions.VIEW_CARDS, AuditLog.EntityTypes.CARD, null, 
                     "Просмотр списка карт");
    }

    public void logViewTransfers(User user) {
        logUserAction(user, AuditLog.Actions.VIEW_TRANSFERS, AuditLog.EntityTypes.TRANSFER, null, 
                     "Просмотр истории переводов");
    }
}
