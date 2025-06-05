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

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody @Valid RegisterRequestDTO registerRequest) throws UserExistException, PasswordNotMatchException {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginRequest) throws UnauthorizedException {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@CookieValue String refreshToken) throws InvalidRefreshTokenException {
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorizationHeader) throws UserNotFoundException, InvalidAccessTokenException {
        String accessToken = authorizationHeader.replace("Bearer ", "").trim();
        authService.logout(accessToken);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @DeleteMapping("/deleteUser")
    public ResponseEntity<Void> deleteUser(Authentication authentication) {
        String userId = authentication.getName();
        authService.deleteUserByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getUserIdFromAccessToken")
    public ResponseEntity<UUID> getUserIdFromAccessToken(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(userId);
    }

    @GetMapping("/isValidAccessToken")
    public ResponseEntity<Boolean> isValidAccessToken(Authentication authentication) {
        // If this method is called, the token is already validated by the filter
        return ResponseEntity.ok(true);
    }
}
