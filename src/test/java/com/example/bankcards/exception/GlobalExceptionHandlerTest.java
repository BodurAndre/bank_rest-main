package com.example.bankcards.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleValidationException_ShouldReturnBadRequest() {
        // Given
        ValidationException exception = new ValidationException("Test validation error");

        // When
        Object response = globalExceptionHandler.handleValidationException(exception, mock(HttpServletRequest.class));

        // Then
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) response;
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    void handleBusinessException_ShouldReturnBadRequest() {
        // Given
        BusinessException exception = new BusinessException("Test business error");

        // When
        Object response = globalExceptionHandler.handleBusinessException(exception, mock(HttpServletRequest.class));

        // Then
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) response;
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    void handleResourceNotFoundException_ShouldReturnNotFound() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Test resource not found");

        // When
        Object response = globalExceptionHandler.handleBusinessException(exception, mock(HttpServletRequest.class));

        // Then
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) response;
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) responseEntity.getBody();
        assertTrue(body.containsKey("error"));
    }

    @Test
    void handleInsufficientFundsException_ShouldReturnBadRequest() {
        // Given
        InsufficientFundsException exception = new InsufficientFundsException(
                java.math.BigDecimal.valueOf(100.0), 
                java.math.BigDecimal.valueOf(200.0)
        );

        // When
        Object response = globalExceptionHandler.handleBusinessException(exception, mock(HttpServletRequest.class));

        // Then
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) response;
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    void handleCardBlockedException_ShouldReturnBadRequest() {
        // Given
        CardBlockedException exception = new CardBlockedException("1234", "Test reason");

        // When
        Object response = globalExceptionHandler.handleBusinessException(exception, mock(HttpServletRequest.class));

        // Then
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) response;
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    void handleMethodArgumentNotValidException_ShouldReturnBadRequest() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "Test field error");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError));

        // When
        Object response = globalExceptionHandler.handleValidationException(exception, mock(HttpServletRequest.class));

        // Then
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) response;
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) responseEntity.getBody();
        assertTrue(body.containsKey("error"));
    }

    @Test
    void handleMethodArgumentNotValidException_MultipleErrors_ShouldReturnBadRequest() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("object", "field1", "Error 1");
        FieldError fieldError2 = new FieldError("object", "field2", "Error 2");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));

        // When
        Object response = globalExceptionHandler.handleValidationException(exception, mock(HttpServletRequest.class));

        // Then
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) response;
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) responseEntity.getBody();
        assertTrue(body.containsKey("error"));
    }

    @Test
    void handleIllegalArgumentException_ShouldReturnBadRequest() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Test illegal argument");

        // When
        Object response = globalExceptionHandler.handleGenericException(exception, mock(HttpServletRequest.class));

        // Then
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) response;
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) responseEntity.getBody();
        assertTrue(body.containsKey("error"));
    }

    @Test
    void handleRuntimeException_ShouldReturnInternalServerError() {
        // Given
        RuntimeException exception = new RuntimeException("Test runtime error");

        // When
        Object response = globalExceptionHandler.handleGenericException(exception, mock(HttpServletRequest.class));

        // Then
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) response;
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) responseEntity.getBody();
        assertTrue(body.containsKey("error"));
    }

    @Test
    void handleException_ShouldReturnInternalServerError() {
        // Given
        Exception exception = new Exception("Test general error");

        // When
        Object response = globalExceptionHandler.handleGenericException(exception, mock(HttpServletRequest.class));

        // Then
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) response;
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) responseEntity.getBody();
        assertTrue(body.containsKey("error"));
    }

    @Test
    void handleValidationException_WithNullMessage_ShouldReturnDefaultMessage() {
        // Given
        ValidationException exception = new ValidationException(null);

        // When
        Object response = globalExceptionHandler.handleValidationException(exception, mock(HttpServletRequest.class));

        // Then
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) response;
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) responseEntity.getBody();
        assertTrue(body.containsKey("error"));
    }

    @Test
    void handleBusinessException_WithNullMessage_ShouldReturnDefaultMessage() {
        // Given
        BusinessException exception = new BusinessException(null);

        // When
        Object response = globalExceptionHandler.handleBusinessException(exception, mock(HttpServletRequest.class));

        // Then
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) response;
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) responseEntity.getBody();
        assertTrue(body.containsKey("error"));
    }

    @Test
    void handleResourceNotFoundException_WithNullMessage_ShouldReturnDefaultMessage() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException(null);

        // When
        Object response = globalExceptionHandler.handleBusinessException(exception, mock(HttpServletRequest.class));

        // Then
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) response;
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    void handleInsufficientFundsException_WithNullMessage_ShouldReturnDefaultMessage() {
        // Given
        InsufficientFundsException exception = new InsufficientFundsException(
                java.math.BigDecimal.valueOf(100.0), 
                java.math.BigDecimal.valueOf(200.0)
        );

        // When
        Object response = globalExceptionHandler.handleBusinessException(exception, mock(HttpServletRequest.class));

        // Then
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) response;
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) responseEntity.getBody();
        assertTrue(body.containsKey("error"));
    }

    @Test
    void handleCardBlockedException_WithNullMessage_ShouldReturnDefaultMessage() {
        // Given
        CardBlockedException exception = new CardBlockedException("1234", "Test reason");

        // When
        Object response = globalExceptionHandler.handleBusinessException(exception, mock(HttpServletRequest.class));

        // Then
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) response;
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) responseEntity.getBody();
        assertTrue(body.containsKey("error"));
    }

    @Test
    void handleMethodArgumentNotValidException_WithEmptyErrors_ShouldReturnBadRequest() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList());

        // When
        Object response = globalExceptionHandler.handleValidationException(exception, mock(HttpServletRequest.class));

        // Then
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) response;
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) responseEntity.getBody();
        assertTrue(body.containsKey("error"));
    }

    @Test
    void handleIllegalArgumentException_WithNullMessage_ShouldReturnDefaultMessage() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // When
        Object response = globalExceptionHandler.handleGenericException(exception, mock(HttpServletRequest.class));

        // Then
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) response;
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) responseEntity.getBody();
        assertTrue(body.containsKey("error"));
    }

    @Test
    void handleRuntimeException_WithNullMessage_ShouldReturnInternalServerError() {
        // Given
        RuntimeException exception = new RuntimeException((String) null);

        // When
        Object response = globalExceptionHandler.handleGenericException(exception, mock(HttpServletRequest.class));

        // Then
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) response;
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) responseEntity.getBody();
        assertTrue(body.containsKey("error"));
    }

    @Test
    void handleException_WithNullMessage_ShouldReturnInternalServerError() {
        // Given
        Exception exception = new Exception((String) null);

        // When
        Object response = globalExceptionHandler.handleGenericException(exception, mock(HttpServletRequest.class));

        // Then
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) response;
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) responseEntity.getBody();
        assertTrue(body.containsKey("error"));
    }
}
