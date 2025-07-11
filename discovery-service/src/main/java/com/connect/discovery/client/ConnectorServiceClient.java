package com.connect.discovery.client;

import com.connect.discovery.dto.ConnectorResponseDTO;

import java.util.List;
import java.util.UUID;

public class ConnectorServiceClient {
    public List<UUID> getLocalsByCountries(List<String> countries) {
        return null;
    }

    public String getUserCountry(UUID userId) {
        return null;
    }

    public ConnectorResponseDTO getPublicProfile(UUID userId) {
        return null;
    }

    public List<ConnectorResponseDTO> getPublicProfiles(List<UUID> userIds) {
        return null;
    }
}
