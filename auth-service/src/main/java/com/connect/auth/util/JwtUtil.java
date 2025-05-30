package com.connect.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret){
        byte[] keyBytes = Base64.getDecoder()
                .decode(secret.getBytes(StandardCharsets.UTF_8));
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UUID userId){
        return Jwts.builder()
                .subject(userId.toString())
                .claim("token_type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 1))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("token_type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 3)) // 7 days
                .signWith(secretKey)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        validateToken(token);
        Claims claims = getTokenClaims(token);
        return claims.get("token_type", String.class).equals("access");
    }

    public boolean validateRefreshToken(String token) {
        validateToken(token);
        Claims claims = getTokenClaims(token);
        return claims.get("token_type", String.class).equals("refresh");
    }

    public Instant getIssuedAt(String token) {
        Claims claims = getTokenClaims(token);

        return claims.getIssuedAt().toInstant();
    }

    public Instant getExpiration(String refreshToken) {
        Claims claims = getTokenClaims(refreshToken);

        return claims.getExpiration().toInstant();
    }

    private void validateToken(String token) {
        try{
            Jwts.parser().verifyWith((SecretKey) secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch(SignatureException e){
            throw new JwtException("Invalid JWT signature");
        } catch (JwtException e) {
            throw new JwtException("Invalid JWT");
        }
    }

    private Claims getTokenClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
