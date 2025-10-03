package com.akatsuki.trip.config;

import com.akatsuki.auth.common.config.CommonSecurityConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CommonSecurityConfig commonSecurityConfig;

    @Bean
    public SecurityFilterChain connectorSecurityFilterChain(HttpSecurity http) throws Exception {
        commonSecurityConfig.commonSecurityFilterChain(http);
        return http.build();
    }
}
