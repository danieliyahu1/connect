package com.connect.discovery.service;

import com.connect.discovery.client.ConnectorServiceClient;
import com.connect.discovery.dto.ConnectorResponseDTO;
import com.connect.discovery.dto.TripResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConnectorService {

    private final ConnectorServiceClient connectorServiceClient;


    public ConnectorResponseDTO fetchUserProfile() {
        return connectorServiceClient.getPublicProfile();
    }

    public List<ConnectorResponseDTO> fetchProfilesByCountry(List<String> userCountriesDestinations) {
        return connectorServiceClient.fetchProfilesByCountry(userCountriesDestinations);
    }

    public List<ConnectorResponseDTO> fetchProfilesById(List<TripResponseDTO> userCountriesDestinations) {
        return connectorServiceClient.fetchProfilesById(
                userCountriesDestinations.stream()
                        .map(TripResponseDTO::getUserId)
                        .distinct()
                        .map(UUID::fromString)
                        .toList()
        );
    }
}
