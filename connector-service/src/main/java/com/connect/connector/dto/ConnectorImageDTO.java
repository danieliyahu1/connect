package com.connect.connector.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConnectorImageDTO {
    private String mediaId; // ID received from Media Service

    private int orderIndex; // for ordering images in gallery
}
