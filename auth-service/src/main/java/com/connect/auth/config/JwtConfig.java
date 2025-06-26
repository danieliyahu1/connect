package com.connect.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@Slf4j
public class JwtConfig {

        @Bean
        public PublicKey publicKey(@Value("${jwt.public.key}") String publicKeyBase64) throws NoSuchAlgorithmException, InvalidKeySpecException {
            log.info("public key is = " + publicKeyBase64);

            byte[] keyBytes;
            try {
                keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            } catch (IllegalArgumentException e) {
                // Catches errors if the Base64 string itself is not valid
                log.error("Error decoding public key Base64 string: {}", e.getMessage(), e);
                // Re-throw as InvalidKeySpecException as it's a parsing failure related to the key spec
                throw new InvalidKeySpecException("Malformed Base64 public key string", e);
            }            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory factory;
            try {
                factory = KeyFactory.getInstance("RSA");
            } catch (NoSuchAlgorithmException e) {
                // This should ideally not happen if "RSA" is a valid algorithm name
                log.error("RSA algorithm not found when creating KeyFactory: {}", e.getMessage(), e);
                throw e; // Re-throw the original exception
            }

            try {
                return factory.generatePublic(spec);
            } catch (InvalidKeySpecException e) {
                // This happens if the decoded byte array is not a valid X.509 public key format
                log.error("Error generating public key from X509EncodedKeySpec: {}", e.getMessage(), e);
                throw e; // Re-throw the original exception
            } catch (Exception e) { // Catch any other unexpected runtime exceptions during key generation
                log.error("An unexpected error occurred during public key generation: {}", e.getMessage(), e);
                // You might want to wrap this in a more specific exception if needed,
                // or re-throw as a RuntimeException if the method signature doesn't allow broader checked exceptions.
                throw new RuntimeException("Failed to generate public key due to unexpected error", e);
            }
        }
}