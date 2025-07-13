package com.connect.discovery.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

import static com.connect.discovery.constants.DiscoveryServiceConstants.AUTHORIZATION_HEADER_BEARER_SPACE_PREFIX;

@Configuration
@Slf4j
public class GroqFeignConfig {
    @Bean
    public RequestInterceptor openAiRequestInterceptor(@Value("${groq.api.key}") String apiKey) {
        return requestTemplate -> {
            requestTemplate.header("Authorization", AUTHORIZATION_HEADER_BEARER_SPACE_PREFIX + apiKey);
            requestTemplate.header("Content-Type", "application/json");
            requestTemplate.header("Accept", "application/json");
            log.info("Headers: Authorization={}, Content-Type=application/json", AUTHORIZATION_HEADER_BEARER_SPACE_PREFIX+apiKey);
        };
    }
}
