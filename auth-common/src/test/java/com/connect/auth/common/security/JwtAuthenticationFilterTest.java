// src/test/java/com/connect/auth/security/JwtAuthenticationFilterTest.java
package com.connect.auth.common.security;

import com.connect.auth.common.exception.AuthCommonInvalidAccessTokenException;
import com.connect.auth.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock
    JwtUtil jwtUtil;

    @Mock
    SecurityProperties securityProperties;

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
        filter = new JwtAuthenticationFilter(jwtUtil, List.of("/auth/public/**"));
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException, AuthCommonInvalidAccessTokenException {
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
    void doFilterInternal_EmptyAuthorizationHeader_DoesNotSetAuthentication() throws ServletException, IOException, AuthCommonInvalidAccessTokenException {
        when(request.getHeader("Authorization")).thenReturn("");
        when(request.getRequestURI()).thenReturn("/auth/internal/someEndpoint");

        filter.doFilterInternal(request, response, filterChain);

        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidAccessTokenException_DoesNotSetAuthentication() throws ServletException, IOException, AuthCommonInvalidAccessTokenException {
        String token = "invalidToken";

        when(request.getRequestURI()).thenReturn("/auth/internal/someEndpoint");

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        doThrow(new AuthCommonInvalidAccessTokenException("Invalid")).when(jwtUtil).validateAccessToken(token);

        filter.doFilterInternal(request, response, filterChain);

        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(request, response);

    }

    @Test
    void doFilterInternal_NoAuthorizationHeader_DoesNotSetAuthentication() throws ServletException, IOException, AuthCommonInvalidAccessTokenException {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/auth/internal/someEndpoint");

        filter.doFilterInternal(request, response, filterChain);

        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_PublicEndpoint_DoesNotSetAuthentication() throws ServletException, IOException, AuthCommonInvalidAccessTokenException {

        when(request.getRequestURI()).thenReturn("/auth/public/someEndpoint");
        filter.doFilterInternal(request, response, filterChain);

        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}