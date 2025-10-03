package com.akatsuki.discovery.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.akatsuki.discovery.constants.DiscoveryServiceConstants.SYSTEM_MESSAGE;

@Configuration
public class AiClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem(SYSTEM_MESSAGE)
                .build();
    }
}
