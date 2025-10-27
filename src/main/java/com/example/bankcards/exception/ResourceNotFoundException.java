package com.example.bankcards.exception;

/**
 * Исключение для случаев, когда ресурс не найден
 */
public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }
    
    public ResourceNotFoundException(String resourceType, Object id) {
        super(String.format("%s с ID %s не найден", resourceType, id), "RESOURCE_NOT_FOUND");
    }
}

