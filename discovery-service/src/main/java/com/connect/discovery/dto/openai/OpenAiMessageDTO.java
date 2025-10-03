package com.akatsuki.discovery.dto.openai;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenAiMessageDTO {
    private String role;     // "system", "user", etc.
    private String content;  // Message content
}
