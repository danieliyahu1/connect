package com.connect.auth.common.config;

import com.connect.auth.common.security.JwtAuthenticationFilter;
import com.connect.auth.common.security.SecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableConfigurationProperties(SecurityProperties.class)
@Configuration
public class CommonSecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final SecurityProperties properties;

    @Autowired
    public CommonSecurityConfig(JwtAuthenticationFilter jwtFilter, SecurityProperties properties) {
        this.jwtFilter = jwtFilter;
        this.properties = properties;
    }

    public HttpSecurity  commonSecurityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    properties.getPermitAllRoutes().forEach(route ->
                            auth.requestMatchers(route).permitAll());
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http;
    }
}
