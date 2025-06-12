package com.connect.auth.controller;

import com.connect.auth.dto.AuthResponseDTO;
import com.connect.auth.dto.LoginRequestDTO;
import com.connect.auth.dto.RegisterRequestDTO;
import com.connect.auth.exception.*;
import com.connect.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/public/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody @Valid RegisterRequestDTO registerRequest) throws UserExistException, PasswordNotMatchException {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequest));
    }

    @PostMapping("/public/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginRequest) throws UnauthorizedException {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/public/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@CookieValue String refreshToken) throws InvalidRefreshTokenException {
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/internal/logout")
    public ResponseEntity<Void> logout(Authentication authentication) throws UnauthorizedException {
        authService.logout(getUserId(authentication));
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @DeleteMapping("/internal/deleteUser")
    public ResponseEntity<Void> deleteUser(Authentication authentication) throws UnauthorizedException {
        authService.deleteUserByUserId(getUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    private String getUserId(Authentication authentication) {
        return authentication.getName();
    }
}
