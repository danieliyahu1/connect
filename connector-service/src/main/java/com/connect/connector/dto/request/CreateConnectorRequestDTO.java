package com.connect.connector.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class CreateConnectorRequestDTO {
    @NotBlank
    @Size(min = 2, max = 20)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 20)
    private String country;

    @NotBlank
    @Size(min = 2, max = 20)
    private String city;

    @NotBlank
    @Size(min = 15)
    private String bio;
}
