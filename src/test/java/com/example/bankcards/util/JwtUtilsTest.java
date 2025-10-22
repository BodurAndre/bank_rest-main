package com.example.bankcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        
        // Устанавливаем тестовые значения через рефлексию
        ReflectionTestUtils.setField(jwtUtils, "secret", "testSecretKey123456789012345678901234567890");
        ReflectionTestUtils.setField(jwtUtils, "expiration", 3600000L); // 1 час
        
        userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();
    }

    @Test
    void testGenerateToken() {
        // Act
        String token = jwtUtils.generateToken(userDetails);
        
        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains(".")); // JWT должен содержать точки
    }

    @Test
    void testGenerateTokenWithExtraClaims() {
        // Arrange
        Map<String, Object> extraClaims = Map.of("role", "USER", "userId", 123L);
        
        // Act
        String token = jwtUtils.generateToken(userDetails, extraClaims);
        
        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testExtractUsername() {
        // Arrange
        String token = jwtUtils.generateToken(userDetails);
        
        // Act
        String username = jwtUtils.extractUsername(token);
        
        // Assert
        assertEquals("test@example.com", username);
    }

    @Test
    void testExtractExpiration() {
        // Arrange
        String token = jwtUtils.generateToken(userDetails);
        
        // Act
        Date expiration = jwtUtils.extractExpiration(token);
        
        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testValidateTokenWithValidToken() {
        // Arrange
        String token = jwtUtils.generateToken(userDetails);
        
        // Act
        boolean isValid = jwtUtils.validateToken(token, userDetails);
        
        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateTokenWithInvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";
        
        // Act
        boolean isValid = jwtUtils.validateToken(invalidToken, userDetails);
        
        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateTokenWithWrongUser() {
        // Arrange
        String token = jwtUtils.generateToken(userDetails);
        UserDetails wrongUser = User.builder()
                .username("wrong@example.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();
        
        // Act
        boolean isValid = jwtUtils.validateToken(token, wrongUser);
        
        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateTokenWithoutUserDetails() {
        // Arrange
        String token = jwtUtils.generateToken(userDetails);
        
        // Act
        boolean isValid = jwtUtils.validateToken(token);
        
        // Assert
        assertTrue(isValid);
    }

    @Test
    void testGetUsernameFromToken() {
        // Arrange
        String token = jwtUtils.generateToken(userDetails);
        
        // Act
        String username = jwtUtils.getUsernameFromToken(token);
        
        // Assert
        assertEquals("test@example.com", username);
    }

    @Test
    void testGetUsernameFromInvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";
        
        // Act
        String username = jwtUtils.getUsernameFromToken(invalidToken);
        
        // Assert
        assertNull(username);
    }

    @Test
    void testTokenStructure() {
        // Arrange
        String token = jwtUtils.generateToken(userDetails);
        
        // Act & Assert
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT должен содержать 3 части, разделенные точками");
        
        // Проверяем, что каждая часть не пустая
        for (String part : parts) {
            assertFalse(part.isEmpty(), "Части JWT не должны быть пустыми");
        }
    }
}
