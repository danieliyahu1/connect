package com.akatsuki.auth.service;

import com.akatsuki.auth.common.exception.AuthCommonInvalidTokenException;
import com.akatsuki.auth.common.exception.AuthCommonSignatureMismatchException;
import com.akatsuki.auth.dto.AuthResponseDTO;
import com.akatsuki.auth.dto.OAuthResponseDTO;
import com.akatsuki.auth.enums.AuthProvider;
import com.akatsuki.auth.exception.WrongProviderException;
import com.akatsuki.auth.model.RefreshToken;
import com.akatsuki.auth.model.User;
import com.akatsuki.auth.repository.AuthRepository;
import com.akatsuki.auth.service.oauth.extractor.OAuth2UserInfoExtractor;
import com.akatsuki.auth.service.oauth.extractor.OAuth2UserInfoExtractorRegistry;
import com.akatsuki.auth.common.util.AsymmetricJwtUtil;
import com.akatsuki.auth.service.token.JwtGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
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
    private AsymmetricJwtUtil jwtUtil;
    @Mock
    private JwtGenerator jwtGenerator;
    @Mock
    private AuthRepository authRepository;
    @Mock
    private OAuth2UserInfoExtractorRegistry oAuth2UserInfoExtractorRegistry;
    @Mock
    OAuth2UserInfoExtractor oAuth2UserInfoExtractor;
    @InjectMocks
    private OAuthService oAuthService;

    @Test
    void processOAuthPostLogin_NewUser_CreatesUserAndReturnsAuthResponse() throws WrongProviderException, AuthCommonSignatureMismatchException, AuthCommonInvalidTokenException {
        // Arrange
        String email = "test@example.com";
        String providerUserId = "google-123";
        AuthProvider provider = AuthProvider.GOOGLE;
        UUID userId = UUID.randomUUID();
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        OAuth2AuthenticationToken oauthToken = mock(OAuth2AuthenticationToken.class);
        OAuth2User oauthUser = mock(OAuth2User.class);
        when(oAuth2UserInfoExtractorRegistry.getExtractor(provider)).thenReturn(oAuth2UserInfoExtractor);
        when(oAuth2UserInfoExtractor.getEmail(oauthUser)).thenReturn(email);
        when(oAuth2UserInfoExtractor.getProviderUserId(oauthUser)).thenReturn(providerUserId);
        when(oauthToken.getPrincipal()).thenReturn(oauthUser);
        when(oauthToken.getAuthorizedClientRegistrationId()).thenReturn(provider.name().toLowerCase());
        when(userService.findByEmail(email)).thenReturn(Optional.empty());

        User savedUser = mock(User.class);
        when(userService.save(any(User.class))).thenReturn(savedUser);
        when(savedUser.getUserId()).thenReturn(userId);

        when(jwtGenerator.generateAccessToken(userId)).thenReturn(accessToken);
        when(jwtGenerator.generateRefreshToken(userId)).thenReturn(refreshToken);

        // Act
        OAuthResponseDTO response = oAuthService.processOAuthPostLogin(oauthToken);

        // Assert
        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        verify(userService).save(any(User.class));
        verify(authRepository).save(any(RefreshToken.class));
    }

    @Test
    void processOAuthPostLogin_ExistingUserWithSameProvider_ReturnsAuthResponse() throws WrongProviderException, AuthCommonSignatureMismatchException, AuthCommonInvalidTokenException {
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
        when(oAuth2UserInfoExtractorRegistry.getExtractor(provider)).thenReturn(oAuth2UserInfoExtractor);
        when(oAuth2UserInfoExtractor.getEmail(oauthUser)).thenReturn(email);
        when(oAuth2UserInfoExtractor.getProviderUserId(oauthUser)).thenReturn(providerUserId);
        when(oauthToken.getAuthorizedClientRegistrationId()).thenReturn(provider.name().toLowerCase());

        User existingUser = mock(User.class);
        when(userService.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(existingUser.getProvider()).thenReturn(provider);
        when(existingUser.getUserId()).thenReturn(userId);

        when(jwtGenerator.generateAccessToken(userId)).thenReturn(accessToken);
        when(jwtGenerator.generateRefreshToken(userId)).thenReturn(refreshToken);

        // Act
        OAuthResponseDTO response = oAuthService.processOAuthPostLogin(oauthToken);

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
        when(oAuth2UserInfoExtractorRegistry.getExtractor(provider)).thenReturn(oAuth2UserInfoExtractor);
        when(oAuth2UserInfoExtractor.getEmail(oauthUser)).thenReturn(email);
        when(oAuth2UserInfoExtractor.getProviderUserId(oauthUser)).thenReturn(providerUserId);
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