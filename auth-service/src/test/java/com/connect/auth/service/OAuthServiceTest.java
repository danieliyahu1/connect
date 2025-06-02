package com.connect.auth.service;

import com.connect.auth.dto.AuthResponseDTO;
import com.connect.auth.enums.AuthProvider;
import com.connect.auth.exception.WrongProviderException;
import com.connect.auth.model.RefreshToken;
import com.connect.auth.model.User;
import com.connect.auth.repository.AuthRepository;
import com.connect.auth.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuthServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthRepository authRepository;
    @InjectMocks
    private OAuthService oAuthService;

    @Test
    void processOAuthPostLogin_NewUser_CreatesUserAndReturnsAuthResponse() throws WrongProviderException {
        // Arrange
        String email = "test@example.com";
        String providerUserId = "google-123";
        AuthProvider provider = AuthProvider.GOOGLE;
        UUID userId = UUID.randomUUID();
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        OAuth2AuthenticationToken oauthToken = mock(OAuth2AuthenticationToken.class);
        OAuth2User oauthUser = mock(OAuth2User.class);

        when(oauthToken.getPrincipal()).thenReturn(oauthUser);
        when(oauthUser.getAttribute("email")).thenReturn(email);
        when(oauthUser.getAttribute("sub")).thenReturn(providerUserId);
        when(oauthToken.getAuthorizedClientRegistrationId()).thenReturn(provider.name().toLowerCase());
        when(userService.findByEmail(email)).thenReturn(Optional.empty());

        User savedUser = mock(User.class);
        when(userService.save(any(User.class))).thenReturn(savedUser);
        when(savedUser.getUserId()).thenReturn(userId);

        when(jwtUtil.generateAccessToken(userId)).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(userId)).thenReturn(refreshToken);

        // Act
        AuthResponseDTO response = oAuthService.processOAuthPostLogin(oauthToken);

        // Assert
        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        verify(userService).save(any(User.class));
        verify(authRepository).save(any(RefreshToken.class));
    }

    @Test
    void processOAuthPostLogin_ExistingUserWithSameProvider_ReturnsAuthResponse() throws WrongProviderException {
        // Arrange
        String email = "test@example.com";
        String providerUserId = "google-123";
        AuthProvider provider = AuthProvider.GOOGLE;
        UUID userId = UUID.randomUUID();
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        OAuth2AuthenticationToken oauthToken = mock(OAuth2AuthenticationToken.class);
        OAuth2User oauthUser = mock(OAuth2User.class);

        when(oauthToken.getPrincipal()).thenReturn(oauthUser);
        when(oauthUser.getAttribute("email")).thenReturn(email);
        when(oauthUser.getAttribute("sub")).thenReturn(providerUserId);
        when(oauthToken.getAuthorizedClientRegistrationId()).thenReturn(provider.name().toLowerCase());

        User existingUser = mock(User.class);
        when(userService.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(existingUser.getProvider()).thenReturn(provider);
        when(existingUser.getUserId()).thenReturn(userId);

        when(jwtUtil.generateAccessToken(userId)).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(userId)).thenReturn(refreshToken);

        // Act
        AuthResponseDTO response = oAuthService.processOAuthPostLogin(oauthToken);

        // Assert
        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        verify(userService, never()).save(any(User.class));
        verify(authRepository).save(any(RefreshToken.class));
    }

    @Test
    void processOAuthPostLogin_ExistingUserWithDifferentProvider_ThrowsWrongProviderException() {
        // Arrange
        String email = "test@example.com";
        String providerUserId = "google-123";
        AuthProvider provider = AuthProvider.GOOGLE;
        AuthProvider existingProvider = AuthProvider.LOCAL;

        OAuth2AuthenticationToken oauthToken = mock(OAuth2AuthenticationToken.class);
        OAuth2User oauthUser = mock(OAuth2User.class);

        when(oauthToken.getPrincipal()).thenReturn(oauthUser);
        when(oauthUser.getAttribute("email")).thenReturn(email);
        when(oauthUser.getAttribute("sub")).thenReturn(providerUserId);
        when(oauthToken.getAuthorizedClientRegistrationId()).thenReturn(provider.name().toLowerCase());

        User existingUser = mock(User.class);
        when(userService.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(existingUser.getProvider()).thenReturn(existingProvider);

        // Act & Assert
        assertThrows(WrongProviderException.class, () -> oAuthService.processOAuthPostLogin(oauthToken));
        verify(userService, never()).save(any(User.class));
        verify(authRepository, never()).save(any(RefreshToken.class));
    }
}