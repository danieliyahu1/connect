package com.connect.discovery.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ConnectorImageDTO {
    @NotBlank(message = "media url must not be blank")
    private String mediaUrl; // ID received from Media Service

    @Min(value = 0, message = "Order index must be between 0 and 4")
    @Max(value = 4, message = "Order index must be between 0 and 4")
    private int orderIndex; // for ordering images in gallery
}