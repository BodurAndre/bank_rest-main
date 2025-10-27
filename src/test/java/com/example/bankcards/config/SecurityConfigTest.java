package com.example.bankcards.config;

import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer.AuthorizedUrl;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer.ExpressionInterceptUrlRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SecurityConfig securityConfig;

    @Test
    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {
        // When
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // Then
        assertNotNull(encoder);
        assertTrue(encoder instanceof org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder);
    }

    @Test
    void passwordEncoder_MultipleCalls_ShouldReturnSameInstance() {
        // When
        PasswordEncoder encoder1 = securityConfig.passwordEncoder();
        PasswordEncoder encoder2 = securityConfig.passwordEncoder();

        // Then
        assertSame(encoder1, encoder2);
    }

    @Test
    void passwordEncoder_ShouldEncodePassword() {
        // Given
        String password = "testPassword123";
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // When
        String encodedPassword = encoder.encode(password);

        // Then
        assertNotNull(encodedPassword);
        assertNotEquals(password, encodedPassword);
        assertTrue(encodedPassword.startsWith("$2a$"));
    }

    @Test
    void passwordEncoder_ShouldMatchPassword() {
        // Given
        String password = "testPassword123";
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String encodedPassword = encoder.encode(password);

        // When
        boolean matches = encoder.matches(password, encodedPassword);

        // Then
        assertTrue(matches);
    }

    @Test
    void passwordEncoder_ShouldNotMatchWrongPassword() {
        // Given
        String password = "testPassword123";
        String wrongPassword = "wrongPassword";
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String encodedPassword = encoder.encode(password);

        // When
        boolean matches = encoder.matches(wrongPassword, encodedPassword);

        // Then
        assertFalse(matches);
    }

    @Test
    void passwordEncoder_ShouldNotMatchNullPassword() {
        // Given
        String password = "testPassword123";
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String encodedPassword = encoder.encode(password);

        // When
        boolean matches = encoder.matches(null, encodedPassword);

        // Then
        assertFalse(matches);
    }

    @Test
    void passwordEncoder_ShouldNotMatchEmptyPassword() {
        // Given
        String password = "testPassword123";
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String encodedPassword = encoder.encode(password);

        // When
        boolean matches = encoder.matches("", encodedPassword);

        // Then
        assertFalse(matches);
    }

    @Test
    void passwordEncoder_ShouldHandleSpecialCharacters() {
        // Given
        String password = "testPassword123!@#$%^&*()_+-=[]{}|;':\",./<>?";
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // When
        String encodedPassword = encoder.encode(password);
        boolean matches = encoder.matches(password, encodedPassword);

        // Then
        assertNotNull(encodedPassword);
        assertTrue(matches);
    }

    @Test
    void passwordEncoder_ShouldHandleUnicodeCharacters() {
        // Given
        String password = "тестПароль123";
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // When
        String encodedPassword = encoder.encode(password);
        boolean matches = encoder.matches(password, encodedPassword);

        // Then
        assertNotNull(encodedPassword);
        assertTrue(matches);
    }

    @Test
    void passwordEncoder_ShouldHandleLongPassword() {
        // Given
        String password = "a".repeat(1000);
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // When
        String encodedPassword = encoder.encode(password);
        boolean matches = encoder.matches(password, encodedPassword);

        // Then
        assertNotNull(encodedPassword);
        assertTrue(matches);
    }

    @Test
    void passwordEncoder_ShouldHandleShortPassword() {
        // Given
        String password = "a";
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // When
        String encodedPassword = encoder.encode(password);
        boolean matches = encoder.matches(password, encodedPassword);

        // Then
        assertNotNull(encodedPassword);
        assertTrue(matches);
    }

    @Test
    void passwordEncoder_ShouldHandleEmptyPassword() {
        // Given
        String password = "";
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // When
        String encodedPassword = encoder.encode(password);
        boolean matches = encoder.matches(password, encodedPassword);

        // Then
        assertNotNull(encodedPassword);
        assertTrue(matches);
    }

    @Test
    void passwordEncoder_ShouldHandleNullPassword() {
        // Given
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            encoder.encode(null);
        });
    }

    @Test
    void passwordEncoder_ShouldHandleNullEncodedPassword() {
        // Given
        String password = "testPassword123";
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // When
        boolean matches = encoder.matches(password, null);

        // Then
        assertFalse(matches);
    }

    @Test
    void passwordEncoder_ShouldHandleEmptyEncodedPassword() {
        // Given
        String password = "testPassword123";
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // When
        boolean matches = encoder.matches(password, "");

        // Then
        assertFalse(matches);
    }

    @Test
    void passwordEncoder_ShouldHandleInvalidEncodedPassword() {
        // Given
        String password = "testPassword123";
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // When
        boolean matches = encoder.matches(password, "invalidEncodedPassword");

        // Then
        assertFalse(matches);
    }

    @Test
    void passwordEncoder_ShouldHandleMalformedEncodedPassword() {
        // Given
        String password = "testPassword123";
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // When
        boolean matches = encoder.matches(password, "$2a$10$invalid");

        // Then
        assertFalse(matches);
    }

    @Test
    void passwordEncoder_ShouldHandleTruncatedEncodedPassword() {
        // Given
        String password = "testPassword123";
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String encodedPassword = encoder.encode(password);
        String truncatedEncodedPassword = encodedPassword.substring(0, encodedPassword.length() - 10);

        // When
        boolean matches = encoder.matches(password, truncatedEncodedPassword);

        // Then
        assertFalse(matches);
    }

    @Test
    void passwordEncoder_ShouldHandleExtendedEncodedPassword() {
        // Given
        String password = "testPassword123";
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String encodedPassword = encoder.encode(password);
        String extendedEncodedPassword = encodedPassword + "extra";

        // When
        boolean matches = encoder.matches(password, extendedEncodedPassword);

        // Then
        assertFalse(matches);
    }

    @Test
    void passwordEncoder_ShouldHandleDifferentPasswords() {
        // Given
        String password1 = "testPassword123";
        String password2 = "testPassword456";
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String encodedPassword1 = encoder.encode(password1);
        String encodedPassword2 = encoder.encode(password2);

        // When
        boolean matches1 = encoder.matches(password1, encodedPassword1);
        boolean matches2 = encoder.matches(password2, encodedPassword2);
        boolean crossMatches1 = encoder.matches(password1, encodedPassword2);
        boolean crossMatches2 = encoder.matches(password2, encodedPassword1);

        // Then
        assertTrue(matches1);
        assertTrue(matches2);
        assertFalse(crossMatches1);
        assertFalse(crossMatches2);
    }

    @Test
    void passwordEncoder_ShouldHandleSamePasswordMultipleTimes() {
        // Given
        String password = "testPassword123";
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // When
        String encodedPassword1 = encoder.encode(password);
        String encodedPassword2 = encoder.encode(password);
        boolean matches1 = encoder.matches(password, encodedPassword1);
        boolean matches2 = encoder.matches(password, encodedPassword2);

        // Then
        assertNotNull(encodedPassword1);
        assertNotNull(encodedPassword2);
        assertNotEquals(encodedPassword1, encodedPassword2); // Different salts
        assertTrue(matches1);
        assertTrue(matches2);
    }
}
