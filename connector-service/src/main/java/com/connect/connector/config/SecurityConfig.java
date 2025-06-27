package com.connect.connector.config;

import com.connect.auth.common.config.CommonSecurityConfig;
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
        log.info("entering to common security filter");
        System.out.println("entering common security");
        commonSecurityConfig.commonSecurityFilterChain(http);
        log.info("exiting from common security filter");
        return http.build();
    }
}
