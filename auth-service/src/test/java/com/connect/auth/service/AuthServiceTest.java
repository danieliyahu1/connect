package com.connect.auth.service;

import com.connect.auth.dto.AuthResponseDTO;
import com.connect.auth.dto.RegisterRequestDTO;
import com.connect.auth.enums.AuthProvider;
import com.connect.auth.model.User;
import com.connect.auth.repository.AuthRepository;
import com.connect.auth.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private JwtUtil jwtUtil;
    @Mock
    private AuthRepository authRepository;
    @InjectMocks
    private AuthService authService;


    @Test
    void register_Successful() throws Exception {
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
        when(userService.save(savedUser)).thenReturn(savedUser);

        when(jwtUtil.generateAccessToken(userId)).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(userId)).thenReturn(refreshToken);

        // Act
        AuthResponseDTO response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());

        verify(userService).findByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userService).save(any(User.class));
        verify(jwtUtil).generateAccessToken(userId);
        verify(jwtUtil).generateRefreshToken(userId);
        verify(authRepository).deleteByUser_Id(savedUser.getId());
        verify(authRepository).save(any());
    }
}