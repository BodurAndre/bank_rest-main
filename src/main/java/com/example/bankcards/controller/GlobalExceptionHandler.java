package com.example.bankcards.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Глобальный обработчик исключений для веб-контроллеров
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает общие исключения
     */
    @ExceptionHandler(Exception.class)
    public RedirectView handleException(Exception e, RedirectAttributes redirectAttributes) {
        System.err.println("Global exception handler caught: " + e.getMessage());
        e.printStackTrace();
        
        redirectAttributes.addFlashAttribute("errorMessage", "Произошла ошибка: " + e.getMessage());
        
        // Редирект на главную страницу
        return new RedirectView("/cards", true);
    }
    
    /**
     * Обрабатывает исключения IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public RedirectView handleIllegalArgumentException(IllegalArgumentException e, RedirectAttributes redirectAttributes) {
        System.err.println("Illegal argument exception: " + e.getMessage());
        
        redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        
        // Редирект на главную страницу
        return new RedirectView("/cards", true);
    }
}
