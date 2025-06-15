package com.connect.connector.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConnectorImageDTO {
    @NotBlank
    private String mediaUrl; // ID received from Media Service
    @NotBlank
    @Min(value = 0, message = "Order index must be non-negative")
    @Max(value = 5, message = "Order index must be less than or equal to 5")
    private int orderIndex; // for ordering images in gallery
}