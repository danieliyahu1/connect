package com.connect.connector.dto;

import com.connect.connector.enums.SocialMediaPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
public class ConnectorSocialMediaDTO {
    @NotNull(message = "Platform must not be not null")
    private String platform;

    @NotBlank(message = "Profile URL must not be blank")
    private String profileUrl;
}
