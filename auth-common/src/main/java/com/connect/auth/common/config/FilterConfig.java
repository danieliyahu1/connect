package com.connect.auth.common.config;

import com.connect.auth.common.security.JwtAuthenticationFilter;
import com.connect.auth.common.security.SecurityProperties;
import com.connect.auth.common.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil, SecurityProperties securityProperties) {
        return new JwtAuthenticationFilter(jwtUtil, securityProperties.getPermitAllRoutes());
    }
}
