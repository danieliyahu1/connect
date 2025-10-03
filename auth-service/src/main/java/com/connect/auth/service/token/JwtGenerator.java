package com.akatsuki.auth.service.token;

import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static com.akatsuki.auth.constants.JwtConstants.ACCESS_TOKEN_LIFE_SPAN;
import static com.akatsuki.auth.constants.JwtConstants.REFRESH_TOKEN_LIFE_SPAN;

@Service
public class JwtGenerator {

    private static final Logger log = LoggerFactory.getLogger(JwtGenerator.class);
    private final PrivateKey privateKey;

    public JwtGenerator(@Value("${jwt.private.key}") String privateKeyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        this.privateKey = kf.generatePrivate(spec);
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
                .signWith(privateKey)
                .compact();
    }
}
