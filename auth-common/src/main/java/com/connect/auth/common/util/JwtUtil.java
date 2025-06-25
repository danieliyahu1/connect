package com.connect.auth.common.util;

import com.connect.auth.common.exception.AuthCommonInvalidAccessTokenException;
import com.connect.auth.common.exception.AuthCommonInvalidRefreshTokenException;
import com.connect.auth.common.exception.AuthCommonInvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import static com.connect.auth.common.constants.JwtConstants.ACCESS_TOKEN_LIFE_SPAN;
import static com.connect.auth.common.constants.JwtConstants.REFRESH_TOKEN_LIFE_SPAN;

@Component
public class JwtUtil {
    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret){
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

    public void validateAccessToken(String token) throws AuthCommonInvalidAccessTokenException {
        if(!isValidAccessToken(token)) {
            throw new AuthCommonInvalidAccessTokenException("Invalid access token");
        }
    }

    public void validateRefreshToken(String token) throws AuthCommonInvalidRefreshTokenException {
        if(!isValidRefreshToken(token)) {
            throw new AuthCommonInvalidRefreshTokenException("Invalid refresh token");
        }
    }

    public boolean isValidAccessToken(String token) {
        try{
            validateToken(token);
            Claims claims = getTokenClaims(token);
            return claims.get("token_type", String.class).equals("access");
        }
        catch (AuthCommonInvalidTokenException e)
        {
            return false;
        }

    }

    public boolean isValidRefreshToken(String token) {
        try{
            validateToken(token);
            Claims claims = getTokenClaims(token);
            return claims.get("token_type", String.class).equals("refresh");
        }
        catch (AuthCommonInvalidTokenException e)
        {
            return false;
        }

    }

    public Instant getIssuedAt(String token) {
        Claims claims = getTokenClaims(token);

        return claims.getIssuedAt().toInstant();
    }

    public Instant getExpiration(String refreshToken) {
        Claims claims = getTokenClaims(refreshToken);

        return claims.getExpiration().toInstant();
    }

    public UUID getUserIdFromAccessToken(String accessToken) throws AuthCommonInvalidAccessTokenException {
        validateAccessToken(accessToken);
        Claims claims = getTokenClaims(accessToken);
        String userIdStr = claims.getSubject();
        return UUID.fromString(userIdStr);
    }

    private Claims getTokenClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
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

    private void validateToken(String token) throws AuthCommonInvalidTokenException {
        try{
            Jwts.parser().verifyWith((SecretKey) secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch(SignatureException e){
            throw new AuthCommonInvalidTokenException("Invalid JWT signature");
        } catch (JwtException e) {
            throw new AuthCommonInvalidTokenException("Invalid JWT");
        }
    }
}
