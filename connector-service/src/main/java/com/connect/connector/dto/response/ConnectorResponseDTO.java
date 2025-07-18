package com.connect.connector.dto.response;

import com.connect.connector.dto.ConnectorImageDTO;
import com.connect.connector.dto.ConnectorSocialMediaDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
public class ConnectorResponseDTO {
    private UUID userId;
    private String firstName;
    private String country;
    private String city;
    private String bio;
    private List<ConnectorImageDTO> galleryImages;
    private List<ConnectorSocialMediaDTO> socialMediaLinks;

    public void setUserId(UUID userId) {
        if(this.userId == null) {
            this.userId = userId;
        }
    }
}
