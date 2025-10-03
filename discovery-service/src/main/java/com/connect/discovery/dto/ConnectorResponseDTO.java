package com.akatsuki.discovery.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Getter
public class ConnectorResponseDTO {
    private UUID userId;
    private String firstName;
    private String country;
    private String city;
    private String bio;
    private List<ConnectorImageDTO> galleryImages;
    private List<ConnectorSocialMediaDTO> socialMediaLinks;
}
