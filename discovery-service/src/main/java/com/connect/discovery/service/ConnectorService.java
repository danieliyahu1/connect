package com.akatsuki.discovery.service;

import com.akatsuki.discovery.client.ConnectorServiceClient;
import com.akatsuki.discovery.dto.ConnectorResponseDTO;
import com.akatsuki.discovery.dto.TripResponseDTO;
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
