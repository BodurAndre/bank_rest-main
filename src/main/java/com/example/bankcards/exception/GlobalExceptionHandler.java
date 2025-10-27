package com.example.bankcards.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Обработка бизнес-исключений
     */
    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(BusinessException ex, HttpServletRequest request) {
        logger.warn("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        if (isApiRequest(request)) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(ex.getErrorCode(), ex.getMessage()));
        } else {
            ModelAndView modelAndView = new ModelAndView("error");
            modelAndView.addObject("error", ex.getMessage());
            modelAndView.addObject("errorCode", ex.getErrorCode());
            return modelAndView;
        }
    }
    
    /**
     * Обработка исключений валидации
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Object handleValidationException(Exception ex, HttpServletRequest request) {
        logger.warn("Validation exception: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        
        if (ex instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException validationEx = (MethodArgumentNotValidException) ex;
            for (FieldError error : validationEx.getBindingResult().getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
        } else if (ex instanceof BindException) {
            BindException bindEx = (BindException) ex;
            for (FieldError error : bindEx.getBindingResult().getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
        }
        
        String errorMessage = errors.isEmpty() ? "Ошибка валидации данных" : 
                errors.values().stream().collect(Collectors.joining(", "));
        
        if (isApiRequest(request)) {
            Map<String, Object> response = createErrorResponse("VALIDATION_ERROR", errorMessage);
            response.put("fieldErrors", errors);
            return ResponseEntity.badRequest().body(response);
        } else {
            ModelAndView modelAndView = new ModelAndView("error");
            modelAndView.addObject("error", errorMessage);
            modelAndView.addObject("errorCode", "VALIDATION_ERROR");
            return modelAndView;
        }
    }
    
    /**
     * Обработка исключений доступа
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        logger.warn("Access denied: {}", ex.getMessage());
        
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("ACCESS_DENIED", "Недостаточно прав для выполнения операции"));
        } else {
            ModelAndView modelAndView = new ModelAndView("error");
            modelAndView.addObject("error", "Недостаточно прав для выполнения операции");
            modelAndView.addObject("errorCode", "ACCESS_DENIED");
            return modelAndView;
        }
    }
    
    /**
     * Обработка всех остальных исключений
     */
    @ExceptionHandler(Exception.class)
    public Object handleGenericException(Exception ex, HttpServletRequest request) {
        logger.error("Unexpected exception: ", ex);
        
        String errorMessage = "К сожалению, произошла непредвиденная ошибка. Попробуйте вернуться на главную страницу.";
        
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("INTERNAL_ERROR", errorMessage));
        } else {
            ModelAndView modelAndView = new ModelAndView("error");
            modelAndView.addObject("error", errorMessage);
            modelAndView.addObject("errorCode", "INTERNAL_ERROR");
            return modelAndView;
        }
    }
    
    /**
     * Проверяет, является ли запрос API запросом
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String acceptHeader = request.getHeader("Accept");
        
        return requestURI.startsWith("/api/") || 
               (acceptHeader != null && acceptHeader.contains("application/json"));
    }
    
    /**
     * Создает стандартный ответ об ошибке
     */
    private Map<String, Object> createErrorResponse(String errorCode, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("errorCode", errorCode);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}

