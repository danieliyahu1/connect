package com.connect.auth.controller;

import com.connect.auth.dto.AuthResponseDTO;
import com.connect.auth.dto.LoginRequestDTO;
import com.connect.auth.dto.RegisterRequestDTO;
import com.connect.auth.exception.RefreshTokenNotFoundException;
import com.connect.auth.exception.UnauthorizedException;
import com.connect.auth.exception.UserExistException;
import com.connect.auth.model.User;
import com.connect.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody @Valid RegisterRequestDTO registerRequest) throws UserExistException {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginRequest) throws UnauthorizedException {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@CookieValue String refreshToken) throws RefreshTokenNotFoundException {
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(UUID userId) throws RefreshTokenNotFoundException {
        authService.logout(userId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @DeleteMapping("/deleteUser/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        authService.deleteUserByUserId(userId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @GetMapping("/getRefreshTokenList")
    public ResponseEntity<Map<UUID, String>> getRefreshTokenList() {
        return ResponseEntity.ok(authService.getRefreshTokenMap());
    }
}
