package com.connect.connector.dto;

import com.connect.connector.model.ConnectorSocialMedia;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class ConnectorRequestDTO {
    @Size(min = 2, max = 20, message = "First name must be between 2 and 20 characters")
    private String firstName;

    @Size(min = 2, max = 20, message = "Country must be between 2 and 20 characters")
    private String country;

    @Size(min = 2, max = 20, message = "City must be between 2 and 20 characters")
    private String city;

    @Size(min = 300, message = "Bio must be at least 15 characters long")
    private String bio;

    private List<ConnectorSocialMedia> socialLinks;

    private List<ConnectorImageDTO> gallery;

}