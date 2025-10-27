package com.example.bankcards.security;

import com.example.bankcards.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private String validToken;
    private String invalidToken;
    private String expiredToken;

    @BeforeEach
    void setUp() {
        validToken = "valid.jwt.token";
        invalidToken = "invalid.jwt.token";
        expiredToken = "expired.jwt.token";

        // Clear security context
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ValidToken_ShouldSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtils.validateToken(validToken)).thenReturn(true);
        when(jwtUtils.getUsernameFromToken(validToken)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(new ArrayList<>());

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils).validateToken(validToken);
        verify(jwtUtils).getUsernameFromToken(validToken);
        verify(userDetailsService).loadUserByUsername("test@example.com");
        verify(filterChain).doFilter(request, response);
        
        // Check that authentication was set
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken);
    }

    @Test
    void doFilterInternal_NoAuthorizationHeader_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils, never()).validateToken(anyString());
        verify(jwtUtils, never()).getUsernameFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
        
        // Check that no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_EmptyAuthorizationHeader_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils, never()).validateToken(anyString());
        verify(jwtUtils, never()).getUsernameFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
        
        // Check that no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_InvalidAuthorizationHeader_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Invalid " + validToken);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils, never()).validateToken(anyString());
        verify(jwtUtils, never()).getUsernameFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
        
        // Check that no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_InvalidToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtUtils.validateToken(invalidToken)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils).validateToken(invalidToken);
        verify(jwtUtils, never()).getUsernameFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
        
        // Check that no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ExpiredToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + expiredToken);
        when(jwtUtils.validateToken(expiredToken)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils).validateToken(expiredToken);
        verify(jwtUtils, never()).getUsernameFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
        
        // Check that no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidTokenButUserNotFound_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtils.validateToken(validToken)).thenReturn(true);
        when(jwtUtils.getUsernameFromToken(validToken)).thenReturn("nonexistent@example.com");
        when(userDetailsService.loadUserByUsername("nonexistent@example.com"))
                .thenThrow(new RuntimeException("User not found"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils).validateToken(validToken);
        verify(jwtUtils).getUsernameFromToken(validToken);
        verify(userDetailsService).loadUserByUsername("nonexistent@example.com");
        verify(filterChain).doFilter(request, response);
        
        // Check that no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidTokenButUsernameIsNull_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtils.validateToken(validToken)).thenReturn(true);
        when(jwtUtils.getUsernameFromToken(validToken)).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils).validateToken(validToken);
        verify(jwtUtils).getUsernameFromToken(validToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
        
        // Check that no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidTokenButUsernameIsEmpty_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtils.validateToken(validToken)).thenReturn(true);
        when(jwtUtils.getUsernameFromToken(validToken)).thenReturn("");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils).validateToken(validToken);
        verify(jwtUtils).getUsernameFromToken(validToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
        
        // Check that no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidTokenWithWhitespace_ShouldSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(" Bearer " + validToken + " ");
        when(jwtUtils.validateToken(validToken)).thenReturn(true);
        when(jwtUtils.getUsernameFromToken(validToken)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(new ArrayList<>());

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils).validateToken(validToken);
        verify(jwtUtils).getUsernameFromToken(validToken);
        verify(userDetailsService).loadUserByUsername("test@example.com");
        verify(filterChain).doFilter(request, response);
        
        // Check that authentication was set
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidTokenWithDifferentCase_ShouldSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("bearer " + validToken);
        when(jwtUtils.validateToken(validToken)).thenReturn(true);
        when(jwtUtils.getUsernameFromToken(validToken)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(new ArrayList<>());

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils).validateToken(validToken);
        verify(jwtUtils).getUsernameFromToken(validToken);
        verify(userDetailsService).loadUserByUsername("test@example.com");
        verify(filterChain).doFilter(request, response);
        
        // Check that authentication was set
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidTokenWithMixedCase_ShouldSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("BeArEr " + validToken);
        when(jwtUtils.validateToken(validToken)).thenReturn(true);
        when(jwtUtils.getUsernameFromToken(validToken)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(new ArrayList<>());

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils).validateToken(validToken);
        verify(jwtUtils).getUsernameFromToken(validToken);
        verify(userDetailsService).loadUserByUsername("test@example.com");
        verify(filterChain).doFilter(request, response);
        
        // Check that authentication was set
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidTokenWithSpecialCharacters_ShouldSetAuthentication() throws ServletException, IOException {
        // Given
        String specialToken = "valid.jwt.token.with-special_chars";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + specialToken);
        when(jwtUtils.validateToken(specialToken)).thenReturn(true);
        when(jwtUtils.getUsernameFromToken(specialToken)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(new ArrayList<>());

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils).validateToken(specialToken);
        verify(jwtUtils).getUsernameFromToken(specialToken);
        verify(userDetailsService).loadUserByUsername("test@example.com");
        verify(filterChain).doFilter(request, response);
        
        // Check that authentication was set
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidTokenWithUnicodeCharacters_ShouldSetAuthentication() throws ServletException, IOException {
        // Given
        String unicodeToken = "valid.jwt.token.с.unicode";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + unicodeToken);
        when(jwtUtils.validateToken(unicodeToken)).thenReturn(true);
        when(jwtUtils.getUsernameFromToken(unicodeToken)).thenReturn("тест@example.com");
        when(userDetailsService.loadUserByUsername("тест@example.com")).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(new ArrayList<>());

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils).validateToken(unicodeToken);
        verify(jwtUtils).getUsernameFromToken(unicodeToken);
        verify(userDetailsService).loadUserByUsername("тест@example.com");
        verify(filterChain).doFilter(request, response);
        
        // Check that authentication was set
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidTokenWithLongUsername_ShouldSetAuthentication() throws ServletException, IOException {
        // Given
        String longUsername = "a".repeat(1000) + "@example.com";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtils.validateToken(validToken)).thenReturn(true);
        when(jwtUtils.getUsernameFromToken(validToken)).thenReturn(longUsername);
        when(userDetailsService.loadUserByUsername(longUsername)).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(new ArrayList<>());

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils).validateToken(validToken);
        verify(jwtUtils).getUsernameFromToken(validToken);
        verify(userDetailsService).loadUserByUsername(longUsername);
        verify(filterChain).doFilter(request, response);
        
        // Check that authentication was set
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidTokenWithEmptyUsername_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtils.validateToken(validToken)).thenReturn(true);
        when(jwtUtils.getUsernameFromToken(validToken)).thenReturn("");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils).validateToken(validToken);
        verify(jwtUtils).getUsernameFromToken(validToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
        
        // Check that no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidTokenWithNullUsername_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtils.validateToken(validToken)).thenReturn(true);
        when(jwtUtils.getUsernameFromToken(validToken)).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils).validateToken(validToken);
        verify(jwtUtils).getUsernameFromToken(validToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
        
        // Check that no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidTokenWithUserDetailsServiceException_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtils.validateToken(validToken)).thenReturn(true);
        when(jwtUtils.getUsernameFromToken(validToken)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils).validateToken(validToken);
        verify(jwtUtils).getUsernameFromToken(validToken);
        verify(userDetailsService).loadUserByUsername("test@example.com");
        verify(filterChain).doFilter(request, response);
        
        // Check that no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidTokenWithJwtUtilsException_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtils.validateToken(validToken)).thenThrow(new RuntimeException("JWT parsing failed"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils).validateToken(validToken);
        verify(jwtUtils, never()).getUsernameFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
        
        // Check that no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
