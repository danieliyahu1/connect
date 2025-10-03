package com.akatsuki.connector.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class CreateConnectorRequestDTO {
    @NotBlank(message = "First name cannot be blank")
    @Size(min = 2, max = 20, message = "First name must be between 2 and 20 characters")
    private String firstName;

    @NotBlank(message = "Country cannot be blank")
    @Size(min = 2, max = 20, message = "Country must be between 2 and 20 characters")
    private String country;

    @NotBlank(message = "City cannot be blank")
    @Size(min = 2, max = 20, message = "City must be between 2 and 20 characters")
    private String city;

    @NotBlank(message = "Bio cannot be blank")
    @Size(min = 15, message = "Bio must be at least 15 characters long")
    private String bio;
}
