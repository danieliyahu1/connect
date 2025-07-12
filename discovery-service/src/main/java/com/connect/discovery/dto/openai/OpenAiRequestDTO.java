package com.connect.discovery.dto.openai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenAiRequestDTO {
    private String model; // e.g., "gpt-4"
    private Double temperature; // Optional, for randomness control
    private List<OpenAiMessageDTO> messages;
}
