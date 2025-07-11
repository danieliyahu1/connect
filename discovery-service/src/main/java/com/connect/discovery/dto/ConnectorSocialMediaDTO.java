package com.connect.discovery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class ConnectorSocialMediaDTO {
    @NotNull(message = "Platform must not be not null")
    private String platform;

    @NotBlank(message = "Profile URL must not be blank")
    private String profileUrl;
}
