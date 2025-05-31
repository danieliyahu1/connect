package com.connect.connector.dto;

import com.connect.connector.dto.validation.ValidCountryCity;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@ValidCountryCity
public class ConnectorRequestDTO {
    @Size(min = 2, max = 20, message = "First name must be between 2 and 20 characters")
    private String firstName;

    @Size(min = 2, max = 20, message = "Country must be between 2 and 20 characters")
    private String country;

    @Size(min = 2, max = 20, message = "City must be between 2 and 20 characters")
    private String city;

    @Size(min = 15, message = "Bio must be at least 15 characters long")
    private String bio;

    @Pattern(regexp = "^(http|https)://.*$", message = "Profile picture must be a valid URL")
    private String profilePictureUrl;
}