package com.connect.connector.dto.response;

import com.connect.connector.dto.ConnectorImageDTO;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ConnectorResponseDTO {
    private UUID userId;
    private String firstName;
    private String country;
    private String city;
    private String bio;
    private List<ConnectorImageDTO> galleryImages;
    private Map<String, String> socialMediaLinks;

    public void setUserId(UUID userId) {
        if(this.userId == null) {
            this.userId = userId;
        }
    }
}
