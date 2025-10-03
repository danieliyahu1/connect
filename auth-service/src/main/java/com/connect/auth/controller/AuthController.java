package com.akatsuki.auth.controller;

import com.akatsuki.auth.common.exception.AuthCommonInvalidRefreshTokenException;
import com.akatsuki.auth.common.exception.AuthCommonInvalidTokenException;
import com.akatsuki.auth.common.exception.AuthCommonSignatureMismatchException;
import com.akatsuki.auth.common.exception.AuthCommonUnauthorizedException;
import com.akatsuki.auth.dto.AuthResponseDTO;
import com.akatsuki.auth.dto.LoginRequestDTO;
import com.akatsuki.auth.dto.RegisterRequestDTO;
import com.akatsuki.auth.exception.*;
import com.akatsuki.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/public/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody @Valid RegisterRequestDTO registerRequest) throws UserExistException, PasswordNotMatchException, AuthCommonSignatureMismatchException, AuthCommonInvalidTokenException {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequest));
    }

    @PostMapping("/public/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginRequest) throws AuthCommonUnauthorizedException, AuthCommonSignatureMismatchException, AuthCommonInvalidTokenException {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/public/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@CookieValue String refreshToken) throws AuthCommonInvalidRefreshTokenException, AuthCommonSignatureMismatchException, AuthCommonInvalidTokenException {
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/me/logout")
    public ResponseEntity<Void> logout(Authentication authentication) throws AuthCommonUnauthorizedException {
        authService.logout(getUserId(authentication));
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @DeleteMapping("/internal/deleteUser")
    public ResponseEntity<Void> deleteUser(Authentication authentication) throws AuthCommonUnauthorizedException {
        authService.deleteUserByUserId(getUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    private String getUserId(Authentication authentication) {
        return authentication.getName();
    }
}
