package com.connect.connector.config;

import com.connect.auth.common.config.CommonSecurityConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
public class SecurityConfig {

    private final CommonSecurityConfig commonSecurityConfig;

    @Bean
    public SecurityFilterChain connectorSecurityFilterChain(HttpSecurity http) throws Exception {
        commonSecurityConfig.commonSecurityFilterChain(http);

        return http.build();
    }
}
