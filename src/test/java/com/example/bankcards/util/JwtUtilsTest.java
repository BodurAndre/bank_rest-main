package com.example.bankcards.util;

import com.example.bankcards.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    private User testUser;
    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(User.Role.USER);
        
        testUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username(testUser.getEmail())
                .password("password")
                .roles(testUser.getRole().name())
                .build();
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        // When
        String token = jwtUtils.generateToken(testUserDetails);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(token.contains("."));
    }

    @Test
    void generateToken_DifferentUsers_ShouldReturnDifferentTokens() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setRole(User.Role.USER);
        UserDetails userDetails2 = org.springframework.security.core.userdetails.User.builder()
                .username(user2.getEmail())
                .password("password")
                .roles(user2.getRole().name())
                .build();

        // When
        String token1 = jwtUtils.generateToken(testUserDetails);
        String token2 = jwtUtils.generateToken(userDetails2);

        // Then
        assertNotEquals(token1, token2);
    }

    @Test
    void generateToken_SameUser_ShouldReturnDifferentTokens() {
        // When
        String token1 = jwtUtils.generateToken(testUserDetails);
        String token2 = jwtUtils.generateToken(testUserDetails);

        // Then
        assertNotEquals(token1, token2); // Different timestamps
    }

    @Test
    void validateToken_ValidToken_ShouldReturnTrue() {
        // Given
        String token = jwtUtils.generateToken(testUserDetails);

        // When
        boolean isValid = jwtUtils.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_InvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtUtils.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_NullToken_ShouldReturnFalse() {
        // When
        boolean isValid = jwtUtils.validateToken(null);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_EmptyToken_ShouldReturnFalse() {
        // When
        boolean isValid = jwtUtils.validateToken("");

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_MalformedToken_ShouldReturnFalse() {
        // Given
        String malformedToken = "not.a.valid.jwt.token";

        // When
        boolean isValid = jwtUtils.validateToken(malformedToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void getUsernameFromToken_ValidToken_ShouldReturnUsername() {
        // Given
        String token = jwtUtils.generateToken(testUserDetails);

        // When
        String username = jwtUtils.getUsernameFromToken(token);

        // Then
        assertEquals(testUser.getEmail(), username);
    }

    @Test
    void getUsernameFromToken_InvalidToken_ShouldReturnNull() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        String username = jwtUtils.getUsernameFromToken(invalidToken);

        // Then
        assertNull(username);
    }

    @Test
    void getUsernameFromToken_NullToken_ShouldReturnNull() {
        // When
        String username = jwtUtils.getUsernameFromToken(null);

        // Then
        assertNull(username);
    }

    @Test
    void validateToken_WithUserDetails_ValidToken_ShouldReturnTrue() {
        // Given
        String token = jwtUtils.generateToken(testUserDetails);

        // When
        boolean isValid = jwtUtils.validateToken(token, testUserDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithUserDetails_InvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtUtils.validateToken(invalidToken, testUserDetails);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithUserDetails_WrongUser_ShouldReturnFalse() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setRole(User.Role.USER);
        UserDetails userDetails2 = org.springframework.security.core.userdetails.User.builder()
                .username(user2.getEmail())
                .password("password")
                .roles(user2.getRole().name())
                .build();
        
        String token = jwtUtils.generateToken(testUserDetails);

        // When
        boolean isValid = jwtUtils.validateToken(token, userDetails2);

        // Then
        assertFalse(isValid);
    }

    @Test
    void generateToken_AdminUser_ShouldReturnValidToken() {
        // Given
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(User.Role.ADMIN);
        UserDetails adminUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username(adminUser.getEmail())
                .password("password")
                .roles(adminUser.getRole().name())
                .build();

        // When
        String token = jwtUtils.generateToken(adminUserDetails);

        // Then
        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));
        assertEquals(adminUser.getEmail(), jwtUtils.getUsernameFromToken(token));
    }

    @Test
    void generateToken_UserWithSpecialCharacters_ShouldReturnValidToken() {
        // Given
        User specialUser = new User();
        specialUser.setId(3L);
        specialUser.setEmail("test+special@example.com");
        specialUser.setFirstName("Test");
        specialUser.setLastName("User");
        specialUser.setRole(User.Role.USER);
        UserDetails specialUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username(specialUser.getEmail())
                .password("password")
                .roles(specialUser.getRole().name())
                .build();

        // When
        String token = jwtUtils.generateToken(specialUserDetails);

        // Then
        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));
        assertEquals(specialUser.getEmail(), jwtUtils.getUsernameFromToken(token));
    }

    @Test
    void generateToken_UserWithLongName_ShouldReturnValidToken() {
        // Given
        User longNameUser = new User();
        longNameUser.setId(4L);
        longNameUser.setEmail("longname@example.com");
        longNameUser.setFirstName("VeryLongFirstNameThatExceedsNormalLength");
        longNameUser.setLastName("VeryLongLastNameThatExceedsNormalLength");
        longNameUser.setRole(User.Role.USER);
        UserDetails longNameUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username(longNameUser.getEmail())
                .password("password")
                .roles(longNameUser.getRole().name())
                .build();

        // When
        String token = jwtUtils.generateToken(longNameUserDetails);

        // Then
        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));
        assertEquals(longNameUser.getEmail(), jwtUtils.getUsernameFromToken(token));
    }

    @Test
    void generateToken_UserWithUnicodeCharacters_ShouldReturnValidToken() {
        // Given
        User unicodeUser = new User();
        unicodeUser.setId(5L);
        unicodeUser.setEmail("unicode@example.com");
        unicodeUser.setFirstName("Тест");
        unicodeUser.setLastName("Пользователь");
        unicodeUser.setRole(User.Role.USER);
        UserDetails unicodeUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username(unicodeUser.getEmail())
                .password("password")
                .roles(unicodeUser.getRole().name())
                .build();

        // When
        String token = jwtUtils.generateToken(unicodeUserDetails);

        // Then
        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));
        assertEquals(unicodeUser.getEmail(), jwtUtils.getUsernameFromToken(token));
    }
}