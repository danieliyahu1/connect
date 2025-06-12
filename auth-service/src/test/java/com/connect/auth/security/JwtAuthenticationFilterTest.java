// src/test/java/com/connect/auth/security/JwtAuthenticationFilterTest.java
package com.connect.auth.security;

import com.connect.auth.exception.InvalidAccessTokenException;
import com.connect.auth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.UUID;

import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock
    JwtUtil jwtUtil;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    FilterChain filterChain;

    JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new JwtAuthenticationFilter(jwtUtil);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException, InvalidAccessTokenException {
        String token = "validToken";
        UUID userId = UUID.randomUUID();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        doNothing().when(jwtUtil).validateAccessToken(token);
        when(jwtUtil.getUserIdFromAccessToken(token)).thenReturn(userId);
        when(request.getRequestURI()).thenReturn("/auth/internal/someEndpoint");

        filter.doFilterInternal(request, response, filterChain);

        Assertions.assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        Assertions.assertEquals(userId.toString(), SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_EmptyAuthorizationHeader_DoesNotSetAuthentication() throws ServletException, IOException, InvalidAccessTokenException {
        when(request.getHeader("Authorization")).thenReturn("");
        when(request.getRequestURI()).thenReturn("/auth/internal/someEndpoint");

        filter.doFilterInternal(request, response, filterChain);

        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidAccessTokenException_DoesNotSetAuthentication() throws ServletException, IOException, InvalidAccessTokenException {
        String token = "invalidToken";

        when(request.getRequestURI()).thenReturn("/auth/internal/someEndpoint");

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        doThrow(new InvalidAccessTokenException("Invalid")).when(jwtUtil).validateAccessToken(token);

        filter.doFilterInternal(request, response, filterChain);

        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(request, response);

    }

    @Test
    void doFilterInternal_NoAuthorizationHeader_DoesNotSetAuthentication() throws ServletException, IOException, InvalidAccessTokenException {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/auth/internal/someEndpoint");

        filter.doFilterInternal(request, response, filterChain);

        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_PublicEndpoint_DoesNotSetAuthentication() throws ServletException, IOException, InvalidAccessTokenException {

        when(request.getRequestURI()).thenReturn("/auth/public/someEndpoint");

        filter.doFilterInternal(request, response, filterChain);

        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}