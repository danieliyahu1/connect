package com.connect.auth.service.token;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static com.connect.auth.common.constants.JwtConstants.ACCESS_TOKEN_LIFE_SPAN;
import static com.connect.auth.common.constants.JwtConstants.REFRESH_TOKEN_LIFE_SPAN;

@Service
public class JwtGenerator {

    private final SecretKey secretKey;

    public JwtGenerator(@Value("${jwt.secret}") String secret){
        byte[] keyBytes = Base64.getDecoder()
                .decode(secret.getBytes(StandardCharsets.UTF_8));
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UUID userId){
        return generateToken(userId, "access", ACCESS_TOKEN_LIFE_SPAN);

    }

    public String generateRefreshToken(UUID userId) {
        return generateToken(userId, "refresh", REFRESH_TOKEN_LIFE_SPAN);
    }

    private String generateToken(UUID userId, String tokenType, long expirationMillis) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("token_type", tokenType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(secretKey)
                .compact();
    }
}
