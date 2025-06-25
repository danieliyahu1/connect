package com.connect.auth.common.util;

import com.connect.auth.common.exception.AuthCommonInvalidAccessTokenException;
import com.connect.auth.common.exception.AuthCommonInvalidRefreshTokenException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secret = Base64.getEncoder().encodeToString("my-very-strong-secret-key-1234567890!".getBytes());    private final UUID userId = UUID.randomUUID();


    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(secret);
    }

    @Test
    void generateAndValidateAccessToken() throws AuthCommonInvalidAccessTokenException {
        String token = jwtUtil.generateAccessToken(userId);
        assertNotNull(token);
        assertTrue(jwtUtil.isValidAccessToken(token));
        jwtUtil.validateAccessToken(token);
    }

    @Test
    void generateAndValidateRefreshToken() throws AuthCommonInvalidRefreshTokenException {
        String token = jwtUtil.generateRefreshToken(userId);
        assertNotNull(token);
        assertTrue(jwtUtil.isValidRefreshToken(token));
        jwtUtil.validateRefreshToken(token);
    }

    @Test
    void isValidAccessToken_WithRefreshToken_ReturnsFalse() {
        String refreshToken = jwtUtil.generateRefreshToken(userId);
        assertFalse(jwtUtil.isValidAccessToken(refreshToken));
    }

    @Test
    void isValidRefreshToken_WithAccessToken_ReturnsFalse() {
        String accessToken = jwtUtil.generateAccessToken(userId);
        assertFalse(jwtUtil.isValidRefreshToken(accessToken));
    }

    @Test
    void getIssuedAtAndExpiration() {
        String refreshToken = jwtUtil.generateRefreshToken(userId);
        Instant issuedAt = jwtUtil.getIssuedAt(refreshToken);
        Instant expiration = jwtUtil.getExpiration(refreshToken);
        assertNotNull(issuedAt);
        assertNotNull(expiration);
        assertTrue(expiration.isAfter(issuedAt));
    }

    @Test
    void getUserIdFromAccessToken_ReturnsCorrectUserId() throws AuthCommonInvalidAccessTokenException {
        String accessToken = jwtUtil.generateAccessToken(userId);
        UUID extracted = jwtUtil.getUserIdFromAccessToken(accessToken);
        assertEquals(userId, extracted);
    }

    @Test
    void validateAccessToken_InvalidToken_ThrowsException() {
        String invalidToken = jwtUtil.generateRefreshToken(userId);
        assertThrows(AuthCommonInvalidAccessTokenException.class, () -> jwtUtil.validateAccessToken(invalidToken));
    }

    @Test
    void validateRefreshToken_InvalidToken_ThrowsException() {
        String invalidToken = jwtUtil.generateAccessToken(userId);
        assertThrows(AuthCommonInvalidRefreshTokenException.class, () -> jwtUtil.validateRefreshToken(invalidToken));
    }

    @Test
    void validateToken_InvalidSignature_ThrowsJwtException() {
        // Generate a token with a different secret (invalid signature)
        String otherSecret = Base64.getEncoder().encodeToString("another-very-strong-secret-key-123!".getBytes());
        byte[] keyBytes = Base64.getDecoder().decode(otherSecret.getBytes(StandardCharsets.UTF_8));
        SecretKey otherKey = Keys.hmacShaKeyFor(keyBytes);

        String invalidSignatureToken = Jwts.builder()
                .subject(userId.toString())
                .claim("token_type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 1))
                .signWith(otherKey)
                .compact();

        assertThrows(AuthCommonInvalidAccessTokenException.class, () -> jwtUtil.validateAccessToken(invalidSignatureToken));
    }

    @Test
    void validateToken_MalformedToken_ThrowsJwtException() {
        String malformedToken = "this.is.not.a.jwt";
        assertThrows(AuthCommonInvalidAccessTokenException.class, () -> jwtUtil.validateAccessToken(malformedToken));

    }
}