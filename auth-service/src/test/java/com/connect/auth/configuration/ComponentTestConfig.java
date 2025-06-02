package com.connect.auth.configuration;

import com.connect.auth.repository.AuthRepository;
import com.connect.auth.service.UserService;
import com.connect.auth.util.JwtUtil;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@org.springframework.boot.test.context.TestConfiguration
@Profile("component-test")
public class ComponentTestConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
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
