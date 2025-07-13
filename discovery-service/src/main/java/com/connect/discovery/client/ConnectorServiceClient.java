package com.connect.discovery.client;

import com.connect.discovery.config.FeignClientConfig;
import com.connect.discovery.dto.ConnectorResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "connectorServiceClient",
        url = "${connector.service.base.url}",
        configuration = FeignClientConfig.class
)
public interface ConnectorServiceClient {

    // Get my profile (requires Authorization header to be forwarded manually)
    @GetMapping("/me")
    ConnectorResponseDTO getPublicProfile();

    // Internal - batch fetch by IDs
    @PostMapping("/internal/batch/ids")
    List<ConnectorResponseDTO> fetchProfilesById(@RequestBody List<UUID> userIds);

    // Internal - batch fetch by countries
    @PostMapping("/internal/batch/countries")
    List<ConnectorResponseDTO> fetchProfilesByCountry(@RequestBody List<String> countries);
}
