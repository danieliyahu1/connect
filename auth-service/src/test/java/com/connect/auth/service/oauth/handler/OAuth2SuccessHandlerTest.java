package com.akatsuki.auth.service.oauth.handler;

import com.akatsuki.auth.dto.OAuthResponseDTO;
import com.akatsuki.auth.exception.WrongProviderException;
import com.akatsuki.auth.service.OAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class OAuth2SuccessHandlerTest {

    @Mock
    private OAuthService oAuthService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private OAuth2AuthenticationToken authenticationToken;

    @InjectMocks
    private OAuth2SuccessHandler successHandler;

    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void shouldReturn200OnExistingUser() throws Exception {
        // Arrange
        OAuthResponseDTO dto = new OAuthResponseDTO( "access", "refresh", false);
        when(oAuthService.processOAuthPostLogin(authenticationToken)).thenReturn(dto);

        // Act
        successHandler.onAuthenticationSuccess(request, response, authenticationToken);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setContentType("application/json");
        String result = responseWriter.toString();
        assertTrue(result.contains("access"));
        assertTrue(result.contains("refresh"));
    }

    @Test
    void shouldReturn201OnNewUser() throws Exception {
        // Arrange
        OAuthResponseDTO dto = new OAuthResponseDTO( "access", "refresh", true);
        when(oAuthService.processOAuthPostLogin(authenticationToken)).thenReturn(dto);

        // Act
        successHandler.onAuthenticationSuccess(request, response, authenticationToken);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        verify(response).setContentType("application/json");
        String result = responseWriter.toString();
        assertTrue(result.contains("access"));
        assertTrue(result.contains("refresh"));
    }

    @Test
    void shouldReturn400OnWrongProviderException() throws Exception {
        // Arrange
        when(oAuthService.processOAuthPostLogin(authenticationToken))
                .thenThrow(new WrongProviderException("Wrong provider"));

        // Act
        successHandler.onAuthenticationSuccess(request, response, authenticationToken);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(response).setContentType("application/json");
        assertTrue(responseWriter.toString().contains("Wrong provider"));
    }
}
