package com.akatsuki.auth.configuration;

import com.akatsuki.auth.common.security.JwtAuthenticationFilter;
import com.akatsuki.auth.common.util.AsymmetricJwtUtil;
import com.akatsuki.auth.config.JpaAuditingConfig;
import org.springframework.boot.test.context.TestConfiguration;
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
import java.util.Optional;

@TestConfiguration
@Profile("component-test")
public class ComponentTestSecurityConfig {

    @Bean
    @Primary // This is still fine for injection, but the matching needs to be specific
    public SecurityFilterChain componentTestSecurityFilterChain(HttpSecurity http, AsymmetricJwtUtil asymmetricJwtUtil) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // **** IMPORTANT: ADD A SPECIFIC SECURITY MATCHER HERE ****
                .securityMatcher(
                        "/**"      // Example: paths specifically for testing
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}