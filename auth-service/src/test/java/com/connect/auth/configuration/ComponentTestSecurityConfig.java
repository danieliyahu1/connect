package com.connect.auth.configuration;

import com.connect.auth.common.security.JwtAuthenticationFilter;
import com.connect.auth.common.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@org.springframework.boot.test.context.TestConfiguration
@Profile("component-test")
public class ComponentTestSecurityConfig {

    @Bean
    @Primary // This is still fine for injection, but the matching needs to be specific
    public SecurityFilterChain componentTestSecurityFilterChain(HttpSecurity http) throws Exception {
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
                .addFilterBefore(new JwtAuthenticationFilter(this.jwtUtil(), List.of("/auth/public/**")), UsernamePasswordAuthenticationFilter.class);
        ;
        return http.build();
    }

    @Bean
    public JwtUtil jwtUtil() {
        // Use a test secret for JWT in component tests
        return new JwtUtil("YW5vdGhlci12ZXJ5LXN0cm9uZy1zZWNyZXQta2V5LTEyMyE=");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}