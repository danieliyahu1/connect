package com.connect.discovery.client;

import com.connect.discovery.dto.openai.OpenAiRequestDTO;
import com.connect.discovery.dto.openai.OpenAiResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "openAiClient",
        url = "https://api.openai.com/v1"
)
public interface OpenAiClient {

    @PostMapping("/chat/completions")
    OpenAiResponseDTO createChatCompletion(
            @RequestHeader("Authorization") String authorization,
            @RequestBody OpenAiRequestDTO request
    );

}
