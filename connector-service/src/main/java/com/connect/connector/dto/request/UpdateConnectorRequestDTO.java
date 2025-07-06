package com.connect.connector.dto.request;

import com.connect.connector.dto.ConnectorImageDTO;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class UpdateConnectorRequestDTO {
    @Size(min = 2, max = 20, message = "First name must be between 2 and 20 characters")
    private String firstName;

    @Size(min = 2, max = 20, message = "Country must be between 2 and 20 characters")
    private String country;

    @Size(min = 2, max = 20, message = "City must be between 2 and 20 characters")
    private String city;

    @Size(min = 15, message = "Bio must be at least 15 characters long")
    private String bio;
}