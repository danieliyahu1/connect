package com.connect.discovery.client;

import com.connect.discovery.config.GroqFeignConfig;
import com.connect.discovery.dto.openai.OpenAiRequestDTO;
import com.connect.discovery.dto.openai.OpenAiResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "groqClient",
        url = "https://api.groq.com/openai/v1",
        configuration = GroqFeignConfig.class
)
public interface GroqClient {

    @PostMapping("/chat/completions")
    OpenAiResponseDTO createChatCompletion(
            @RequestBody OpenAiRequestDTO request
    );
}
