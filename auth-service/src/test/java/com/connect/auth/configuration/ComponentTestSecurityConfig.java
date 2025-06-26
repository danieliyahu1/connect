package com.connect.auth.configuration;

import com.connect.auth.common.security.JwtAuthenticationFilter;
import com.connect.auth.common.util.AsymmetricJwtUtil;
import com.connect.auth.service.token.JwtGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@org.springframework.boot.test.context.TestConfiguration
@Profile("component-test")
public class ComponentTestSecurityConfig {

    @Bean
    @Primary // This is still fine for injection, but the matching needs to be specific
    public SecurityFilterChain componentTestSecurityFilterChain(HttpSecurity http, AsymmetricJwtUtil asymmetricJwtUtil) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // **** IMPORTANT: ADD A SPECIFIC SECURITY MATCHER HERE ****
                .securityMatcher(
                        "/auth/internal/**"      // Example: paths specifically for testing
                        // Do NOT include "/**" or anything that would catch ALL requests
                )
                .authorizeHttpRequests(auth -> auth
                        // For the paths matched above, define their authorization
                        .anyRequest().permitAll() // Allow all on these test-specific paths, or define test-specific rules
                )
                // If you still need OAuth2 login for *these specific test paths*
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/oauth2/success", true)
                )
                .addFilterBefore(new JwtAuthenticationFilter(asymmetricJwtUtil, List.of("/auth/public/**")), UsernamePasswordAuthenticationFilter.class);
        ;
        return http.build();
    }

//    @Bean
//    public AsymmetricJwtUtil jwtUtil(PublicKey publicKey) {
//        return new AsymmetricJwtUtil(publicKey);
//    }

//    @Bean
//    public PublicKey publicKey(@Value("${jwt.public.key}") String publicKeyBase64) throws Exception {
//        byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
//        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
//        KeyFactory factory = KeyFactory.getInstance("RSA");
//        return factory.generatePublic(spec);
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}