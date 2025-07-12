package com.connect.discovery.dto.openai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpenAiMatchResultDTO {
    private String reason;      // explanation text
    private double score;       // float between 0 and 1 (exclusive)
}
