package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setRole(User.Role.USER);
    }

    @Test
    void loadUserByUsername_ValidEmail_ShouldReturnUserDetails() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Then
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("$2a$10$encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertNotNull(userDetails.getAuthorities());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_AdminUser_ShouldReturnUserDetailsWithAdminRole() {
        // Given
        testUser.setRole(User.Role.ADMIN);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin@example.com");

        // Then
        assertNotNull(userDetails);
        assertEquals("admin@example.com", userDetails.getUsername());
        assertEquals("$2a$10$encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertNotNull(userDetails.getAuthorities());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_UserNotFound_ShouldThrowUsernameNotFoundException() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("nonexistent@example.com");
        });
        assertEquals("Пользователь не найден: nonexistent@example.com", exception.getMessage());
    }

    @Test
    void loadUserByUsername_EmptyEmail_ShouldThrowUsernameNotFoundException() {
        // Given
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("");
        });
        assertEquals("Пользователь не найден: ", exception.getMessage());
    }

    @Test
    void loadUserByUsername_NullEmail_ShouldThrowUsernameNotFoundException() {
        // Given
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(null);
        });
        assertEquals("Пользователь не найден: null", exception.getMessage());
    }

    @Test
    void loadUserByUsername_UserWithNullRole_ShouldReturnUserDetailsWithDefaultRole() {
        // Given
        testUser.setRole(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Then
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("$2a$10$encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertNotNull(userDetails.getAuthorities());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_UserWithNullPassword_ShouldReturnUserDetailsWithNullPassword() {
        // Given
        testUser.setPassword(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Then
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertNull(userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertNotNull(userDetails.getAuthorities());
        assertEquals(1, userDetails.getAuthorities().size());
    }

    @Test
    void loadUserByUsername_UserWithEmptyPassword_ShouldReturnUserDetailsWithEmptyPassword() {
        // Given
        testUser.setPassword("");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Then
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertNotNull(userDetails.getAuthorities());
        assertEquals(1, userDetails.getAuthorities().size());
    }

    @Test
    void loadUserByUsername_UserWithSpecialCharacters_ShouldReturnUserDetails() {
        // Given
        testUser.setEmail("test+special@example.com");
        when(userRepository.findByEmail("test+special@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test+special@example.com");

        // Then
        assertNotNull(userDetails);
        assertEquals("test+special@example.com", userDetails.getUsername());
        assertEquals("$2a$10$encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertNotNull(userDetails.getAuthorities());
        assertEquals(1, userDetails.getAuthorities().size());
    }

    @Test
    void loadUserByUsername_UserWithUnicodeCharacters_ShouldReturnUserDetails() {
        // Given
        testUser.setEmail("тест@example.com");
        when(userRepository.findByEmail("тест@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("тест@example.com");

        // Then
        assertNotNull(userDetails);
        assertEquals("тест@example.com", userDetails.getUsername());
        assertEquals("$2a$10$encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertNotNull(userDetails.getAuthorities());
        assertEquals(1, userDetails.getAuthorities().size());
    }

    @Test
    void loadUserByUsername_UserWithLongEmail_ShouldReturnUserDetails() {
        // Given
        String longEmail = "a".repeat(1000) + "@example.com";
        testUser.setEmail(longEmail);
        when(userRepository.findByEmail(longEmail)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(longEmail);

        // Then
        assertNotNull(userDetails);
        assertEquals(longEmail, userDetails.getUsername());
        assertEquals("$2a$10$encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertNotNull(userDetails.getAuthorities());
        assertEquals(1, userDetails.getAuthorities().size());
    }

    @Test
    void loadUserByUsername_UserWithNullEmail_ShouldThrowUsernameNotFoundException() {
        // Given
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(null);
        });
        assertEquals("Пользователь не найден: null", exception.getMessage());
    }

    @Test
    void loadUserByUsername_UserWithEmptyEmail_ShouldThrowUsernameNotFoundException() {
        // Given
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("");
        });
        assertEquals("Пользователь не найден: ", exception.getMessage());
    }

    @Test
    void loadUserByUsername_UserWithWhitespaceEmail_ShouldThrowUsernameNotFoundException() {
        // Given
        when(userRepository.findByEmail("   ")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("   ");
        });
        assertEquals("Пользователь не найден:    ", exception.getMessage());
    }

    @Test
    void loadUserByUsername_UserWithTabEmail_ShouldThrowUsernameNotFoundException() {
        // Given
        when(userRepository.findByEmail("\t")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("\t");
        });
        assertEquals("Пользователь не найден: \t", exception.getMessage());
    }

    @Test
    void loadUserByUsername_UserWithNewlineEmail_ShouldThrowUsernameNotFoundException() {
        // Given
        when(userRepository.findByEmail("\n")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("\n");
        });
        assertEquals("Пользователь не найден: \n", exception.getMessage());
    }

    @Test
    void loadUserByUsername_UserWithCarriageReturnEmail_ShouldThrowUsernameNotFoundException() {
        // Given
        when(userRepository.findByEmail("\r")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("\r");
        });
        assertEquals("Пользователь не найден: \r", exception.getMessage());
    }

    @Test
    void loadUserByUsername_UserWithMixedWhitespaceEmail_ShouldThrowUsernameNotFoundException() {
        // Given
        when(userRepository.findByEmail(" \t\n\r ")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(" \t\n\r ");
        });
        assertEquals("Пользователь не найден:  \t\n\r ", exception.getMessage());
    }

    @Test
    void loadUserByUsername_UserWithSpecialCharactersEmail_ShouldThrowUsernameNotFoundException() {
        // Given
        when(userRepository.findByEmail("!@#$%^&*()")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("!@#$%^&*()");
        });
        assertEquals("Пользователь не найден: !@#$%^&*()", exception.getMessage());
    }

    @Test
    void loadUserByUsername_UserWithUnicodeEmail_ShouldThrowUsernameNotFoundException() {
        // Given
        when(userRepository.findByEmail("тест@example.com")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("тест@example.com");
        });
        assertEquals("Пользователь не найден: тест@example.com", exception.getMessage());
    }

    @Test
    void loadUserByUsername_UserWithLongEmail_ShouldThrowUsernameNotFoundException() {
        // Given
        String longEmail = "a".repeat(1000) + "@example.com";
        when(userRepository.findByEmail(longEmail)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(longEmail);
        });
        assertEquals("Пользователь не найден: " + longEmail, exception.getMessage());
    }
}
