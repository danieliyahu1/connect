package com.connect.connector.dto;

import com.connect.connector.enums.SocialMedia;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class ConnectorResponseDTO {
    private UUID userId;
    private String firstName;
    private String country;
    private String city;
    private String bio;
    private String profilePictureUrl;
    private Map<SocialMedia, String> socialMediaLinks;

    public void setUserId(UUID userId) {
        if(this.userId == null) {
            this.userId = userId;
        }
    }
}
