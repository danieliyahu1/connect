package com.akatsuki.auth.service;

import com.akatsuki.auth.common.exception.AuthCommonInvalidRefreshTokenException;
import com.akatsuki.auth.common.exception.AuthCommonUnauthorizedException;
import com.akatsuki.auth.dto.AuthResponseDTO;
import com.akatsuki.auth.dto.LoginRequestDTO;
import com.akatsuki.auth.dto.RegisterRequestDTO;
import com.akatsuki.auth.exception.*;
import com.akatsuki.auth.model.RefreshToken;
import com.akatsuki.auth.model.User;
import com.akatsuki.auth.repository.AuthRepository;
import com.akatsuki.auth.common.util.AsymmetricJwtUtil;
import com.akatsuki.auth.service.token.JwtGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AsymmetricJwtUtil jwtUtil;
    @Mock
    private JwtGenerator jwtGenerator;
    @Mock
    private AuthRepository authRepository;
    @InjectMocks
    private AuthService authService;


    // Test cases for the register method in AuthService

    @Test
    void register_WithValidRequest_ReturnsAuthResponseDTO() throws Exception {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword";
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        UUID userId = UUID.randomUUID();

        RegisterRequestDTO request = mock(RegisterRequestDTO.class);

        when(request.getEmail()).thenReturn(email);
        when(request.getPassword()).thenReturn(password);
        when(request.getConfirmedPassword()).thenReturn(password);
        when(userService.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        User savedUser = mock(User.class);
        when(userService.save(any(User.class))).thenReturn(savedUser);

        when(savedUser.getUserId()).thenReturn(userId);
        when(savedUser.getId()).thenReturn(userId);
        when(jwtGenerator.generateAccessToken(userId)).thenReturn(accessToken);
        when(jwtGenerator.generateRefreshToken(userId)).thenReturn(refreshToken);

        doNothing().when(authRepository).deleteByUser_Id(userId);
        // Act
        AuthResponseDTO response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());

        verify(userService).findByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userService).save(any(User.class));
        verify(jwtGenerator).generateAccessToken(userId);
        verify(jwtGenerator).generateRefreshToken(userId);
        verify(authRepository).deleteByUser_Id(savedUser.getId());
        verify(authRepository).save(any());
    }

    @Test
    void register_WithExistingEmail_ThrowsUserExistException() throws Exception {
        // Arrange
        String email = "test@example.com";
        Optional<User> mockOptionalUser = Optional.of(mock(User.class));        String password = "password123";

        RegisterRequestDTO request = mock(RegisterRequestDTO.class);

        when(request.getEmail()).thenReturn(email);
        when(userService.findByEmail(email)).thenReturn(mockOptionalUser);

        assertThrows(UserExistException.class, () -> {
            authService.register(request);
        });
    }

    @Test
    void register_WithMismatchedPasswords_ThrowsPasswordNotMatchException() throws Exception {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        String confirmedPassword = "differentPassword";
        Optional<User> mockOptionalUser = Optional.of(mock(User.class));

        RegisterRequestDTO request = mock(RegisterRequestDTO.class);

        when(request.getEmail()).thenReturn(email);
        when(request.getPassword()).thenReturn(password);
        when(request.getConfirmedPassword()).thenReturn(confirmedPassword);
        when(userService.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(PasswordNotMatchException.class, () -> {
            authService.register(request);
        });
    }


    // Test cases for the login method in AuthService

    @Test
    void login_WithValidCredentials_ReturnsAuthResponseDTO() throws Exception {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword";
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        UUID userId = UUID.randomUUID();

        LoginRequestDTO request = mock(LoginRequestDTO.class);
        User user = mock(User.class);

        when(request.getEmail()).thenReturn(email);
        when(request.getPassword()).thenReturn(password);
        when(userService.findByEmail(email)).thenReturn(Optional.of(user));
        when(user.getEncodedPassword()).thenReturn(encodedPassword);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(user.getUserId()).thenReturn(userId);
        when(user.getId()).thenReturn(userId);
        when(jwtGenerator.generateAccessToken(userId)).thenReturn(accessToken);
        when(jwtGenerator.generateRefreshToken(userId)).thenReturn(refreshToken);

        doNothing().when(authRepository).deleteByUser_Id(userId);

        // Act
        AuthResponseDTO response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        verify(userService).findByEmail(email);
        verify(passwordEncoder).matches(password, encodedPassword);
        verify(jwtGenerator).generateAccessToken(userId);
        verify(jwtGenerator).generateRefreshToken(userId);
        verify(authRepository).deleteByUser_Id(userId);
        verify(authRepository).save(any());
    }

    @Test
    void login_WithNonExistentEmail_ThrowsUnauthorizedException() {
        // Arrange
        String email = "notfound@example.com";
        String password = "password123";
        LoginRequestDTO request = mock(LoginRequestDTO.class);

        when(request.getEmail()).thenReturn(email);
        when(userService.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AuthCommonUnauthorizedException.class, () -> authService.login(request));
        verify(userService).findByEmail(email);
    }

    @Test
    void login_WithIncorrectPassword_ThrowsUnauthorizedException() {
        // Arrange
        String email = "test@example.com";
        String password = "wrongPassword";
        String encodedPassword = "encodedPassword";
        LoginRequestDTO request = mock(LoginRequestDTO.class);
        User user = mock(User.class);

        when(request.getEmail()).thenReturn(email);
        when(request.getPassword()).thenReturn(password);
        when(userService.findByEmail(email)).thenReturn(Optional.of(user));
        when(user.getEncodedPassword()).thenReturn(encodedPassword);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        // Act & Assert
        assertThrows(AuthCommonUnauthorizedException.class, () -> authService.login(request));
        verify(userService).findByEmail(email);
        verify(passwordEncoder).matches(password, encodedPassword);
    }


    // Test cases for the refresh method in AuthService

    @Test
    void refresh_WithValidRefreshToken_ReturnsAuthResponseDTO() throws Exception {
        // Arrange
        String refreshToken = "validRefreshToken";
        String accessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";
        UUID userId = UUID.randomUUID();

        User user = mock(User.class);

        // Mock JWT validation and repository lookup
        doNothing().when(jwtUtil).validateRefreshToken(refreshToken);
        when(authRepository.findByToken(refreshToken)).thenReturn(Optional.of(new RefreshToken(refreshToken, user, null, null)));
        when(user.getUserId()).thenReturn(userId);
        when(user.getId()).thenReturn(userId);
        when(jwtGenerator.generateAccessToken(userId)).thenReturn(accessToken);
        when(jwtGenerator.generateRefreshToken(userId)).thenReturn(newRefreshToken);

        doNothing().when(authRepository).deleteByUser_Id(userId);

        // Act
        AuthResponseDTO response = authService.refresh(refreshToken);

        // Assert
        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(newRefreshToken, response.getRefreshToken());
        verify(jwtUtil).validateRefreshToken(refreshToken);
        verify(authRepository).findByToken(refreshToken);
        verify(jwtGenerator).generateAccessToken(userId);
        verify(jwtGenerator).generateRefreshToken(userId);
        verify(authRepository).deleteByUser_Id(userId);
        verify(authRepository).save(any());
    }

    @Test
    void refresh_WithInvalidRefreshToken_ThrowsInvalidRefreshTokenException() throws AuthCommonInvalidRefreshTokenException {
        // Arrange
        String refreshToken = "invalidRefreshToken";
        doThrow(AuthCommonInvalidRefreshTokenException.class).when(jwtUtil).validateRefreshToken(refreshToken);        // Act & Assert
        assertThrows(AuthCommonInvalidRefreshTokenException.class, () -> authService.refresh(refreshToken));
        verify(jwtUtil).validateRefreshToken(refreshToken);
    }

    @Test
    void refresh_WithNonExistentRefreshToken_ThrowsInvalidRefreshTokenException() throws AuthCommonInvalidRefreshTokenException {
        // Arrange
        String refreshToken = "notFoundRefreshToken";
        doNothing().when(jwtUtil).validateRefreshToken(refreshToken);
        when(authRepository.findByToken(refreshToken)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AuthCommonInvalidRefreshTokenException.class, () -> authService.refresh(refreshToken));
        verify(jwtUtil).validateRefreshToken(refreshToken);
        verify(authRepository).findByToken(refreshToken);
    }

    // Test cases for the logout method in AuthService

    @Test
    void logout_WithValidAccessToken_DeletesRefreshToken() throws AuthCommonUnauthorizedException {
        // Arrange
        String userId = UUID.randomUUID().toString();
        User user = mock(User.class);
        Optional<User> optionalUser = Optional.of(user);

        when(userService.getUserByUserId(any())).thenReturn(optionalUser);
        doNothing().when(authRepository).deleteByUser_Id(any());

        // Act
        authService.logout(userId);

    }

    @Test
    void logout_UserNotFound_ThrowsUnauthorizedException() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        when(userService.getUserByUserId(any())).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(AuthCommonUnauthorizedException.class, () -> authService.logout(userId));
    }


    // Test cases for the deleteUserByUserId method in AuthService

    @Test
    void deleteUserByUserId_WithExistingUser_DeletesUserAndRefreshTokens() throws AuthCommonUnauthorizedException {
        // Arrange
        UUID userId = UUID.randomUUID();
        String userIdStr = userId.toString();
        User user = mock(User.class);
        when(userService.getUserByUserId(userId)).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(userId);

        // Act
        authService.deleteUserByUserId(userIdStr);

        // Assert
        verify(userService).getUserByUserId(userId);
        verify(authRepository).deleteByUser_Id(userId);
        verify(userService).deleteByUserId(userId);
    }

    @Test
    void deleteUserByUserId_WithNonExistentUser_DoesNothing() throws AuthCommonUnauthorizedException {
        // Arrange
        UUID userId = UUID.randomUUID();
        String userIdStr = userId.toString();
        when(userService.getUserByUserId(userId)).thenReturn(Optional.empty());

        // Act
        assertThrows(AuthCommonUnauthorizedException.class, () -> authService.logout(userId.toString()));
    }
}