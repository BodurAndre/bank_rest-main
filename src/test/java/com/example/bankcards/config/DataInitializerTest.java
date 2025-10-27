package com.example.bankcards.config;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void run_NoUsersExist_ShouldCreateDefaultUsers() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // When
        dataInitializer.run((String[]) null);

        // Then
        verify(userRepository).count();
        verify(passwordEncoder, atLeastOnce()).encode(anyString());
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    @Test
    void run_UsersExist_ShouldNotCreateDefaultUsers() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(5L);

        // When
        dataInitializer.run((String[]) null);

        // Then
        verify(userRepository).count();
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void run_AdminUserExists_ShouldNotCreateAdminUser() throws Exception {
        // Given
        User existingAdmin = new User();
        existingAdmin.setEmail("admin@bankcards.com");
        existingAdmin.setRole(User.Role.ADMIN);

        when(userRepository.count()).thenReturn(1L);
        when(userRepository.findByEmail("admin@bankcards.com")).thenReturn(Optional.of(existingAdmin));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // When
        dataInitializer.run((String[]) null);

        // Then
        verify(userRepository).count();
        verify(userRepository).findByEmail("admin@bankcards.com");
        verify(passwordEncoder, atLeastOnce()).encode(anyString());
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    @Test
    void run_RegularUserExists_ShouldNotCreateRegularUser() throws Exception {
        // Given
        User existingUser = new User();
        existingUser.setEmail("user@bankcards.com");
        existingUser.setRole(User.Role.USER);

        when(userRepository.count()).thenReturn(1L);
        when(userRepository.findByEmail("user@bankcards.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // When
        dataInitializer.run((String[]) null);

        // Then
        verify(userRepository).count();
        verify(userRepository).findByEmail("user@bankcards.com");
        verify(passwordEncoder, atLeastOnce()).encode(anyString());
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    @Test
    void run_AdminUserNotExists_ShouldCreateAdminUser() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.findByEmail("admin@bankcards.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // When
        dataInitializer.run((String[]) null);

        // Then
        verify(userRepository).count();
        verify(userRepository).findByEmail("admin@bankcards.com");
        verify(passwordEncoder, atLeastOnce()).encode(anyString());
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    @Test
    void run_RegularUserNotExists_ShouldCreateRegularUser() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.findByEmail("user@bankcards.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // When
        dataInitializer.run((String[]) null);

        // Then
        verify(userRepository).count();
        verify(userRepository).findByEmail("user@bankcards.com");
        verify(passwordEncoder, atLeastOnce()).encode(anyString());
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    @Test
    void run_WithException_ShouldHandleGracefully() throws Exception {
        // Given
        when(userRepository.count()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        // Should not throw exception
        assertDoesNotThrow(() -> dataInitializer.run((String[]) null));
    }

    @Test
    void run_WithSaveException_ShouldHandleGracefully() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Save error"));

        // When & Then
        // Should not throw exception
        assertDoesNotThrow(() -> dataInitializer.run((String[]) null));
    }

    @Test
    void run_WithEncodeException_ShouldHandleGracefully() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenThrow(new RuntimeException("Encode error"));

        // When & Then
        // Should not throw exception
        assertDoesNotThrow(() -> dataInitializer.run((String[]) null));
    }

    @Test
    void run_WithFindByEmailException_ShouldHandleGracefully() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.findByEmail(anyString())).thenThrow(new RuntimeException("Find error"));

        // When & Then
        // Should not throw exception
        assertDoesNotThrow(() -> dataInitializer.run((String[]) null));
    }

    @Test
    void run_WithNullUserRepository_ShouldHandleGracefully() throws Exception {
        // Given
        DataInitializer dataInitializerWithNullRepo = new DataInitializer();

        // When & Then
        // Should not throw exception
        assertDoesNotThrow(() -> dataInitializerWithNullRepo.run((String[]) null));
    }

    @Test
    void run_WithNullPasswordEncoder_ShouldHandleGracefully() throws Exception {
        // Given
        DataInitializer dataInitializerWithNullEncoder = new DataInitializer();

        // When & Then
        // Should not throw exception
        assertDoesNotThrow(() -> dataInitializerWithNullEncoder.run((String[]) null));
    }

    @Test
    void run_WithBothNullDependencies_ShouldHandleGracefully() throws Exception {
        // Given
        DataInitializer dataInitializerWithNullDeps = new DataInitializer();

        // When & Then
        // Should not throw exception
        assertDoesNotThrow(() -> dataInitializerWithNullDeps.run((String[]) null));
    }

    @Test
    void run_WithNegativeCount_ShouldCreateDefaultUsers() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(-1L);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // When
        dataInitializer.run((String[]) null);

        // Then
        verify(userRepository).count();
        verify(passwordEncoder, atLeastOnce()).encode(anyString());
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    @Test
    void run_WithZeroCount_ShouldCreateDefaultUsers() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // When
        dataInitializer.run((String[]) null);

        // Then
        verify(userRepository).count();
        verify(passwordEncoder, atLeastOnce()).encode(anyString());
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    @Test
    void run_WithPositiveCount_ShouldNotCreateDefaultUsers() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(1L);

        // When
        dataInitializer.run((String[]) null);

        // Then
        verify(userRepository).count();
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void run_WithLargeCount_ShouldNotCreateDefaultUsers() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(1000L);

        // When
        dataInitializer.run((String[]) null);

        // Then
        verify(userRepository).count();
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void run_WithMaxValueCount_ShouldNotCreateDefaultUsers() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(Long.MAX_VALUE);

        // When
        dataInitializer.run((String[]) null);

        // Then
        verify(userRepository).count();
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void run_WithMinValueCount_ShouldCreateDefaultUsers() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(Long.MIN_VALUE);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // When
        dataInitializer.run((String[]) null);

        // Then
        verify(userRepository).count();
        verify(passwordEncoder, atLeastOnce()).encode(anyString());
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }
}
