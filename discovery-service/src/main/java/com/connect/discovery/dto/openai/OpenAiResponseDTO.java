package com.akatsuki.discovery.dto.openai;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OpenAiResponseDTO {
    private List<OpenAiChoiceDTO> choices;
}
