package com.connect.discovery.config;

import feign.RequestInterceptor;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
@EnableFeignClients(basePackages = "com.connect.discovery.client")
@Slf4j
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        log.info("Feign Client Config initialized");
        return template -> {
            // Get current HTTP request
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null) {
                return;
            }

            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            String authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader != null) {
                // Forward the Authorization header
                template.header("Authorization", authorizationHeader);
            }
        };
    }
}
