package com.connect.connector.dto.request;

import com.connect.connector.dto.ConnectorImageDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
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

    @NotNull
    private Map<String, String> socialMediaLinks; // <platform, profileUrl>

    private List<ConnectorImageDTO> gallery; // optional
}
