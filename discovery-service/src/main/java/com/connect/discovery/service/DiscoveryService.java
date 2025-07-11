package com.connect.discovery.service;

import com.connect.discovery.client.ConnectorServiceClient;
import com.connect.discovery.client.TripServiceClient;
import com.connect.discovery.client.OpenAiClient;
import com.connect.discovery.dto.ConnectorResponseDTO; // Keep this import
import com.connect.discovery.dto.TripResponseDTO;
import com.connect.discovery.dto.UserSuggestionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiscoveryService {

    private final TripServiceClient tripServiceClient;
    private final ConnectorServiceClient connectorServiceClient;
    private final OpenAiClient openAiClient;
    /**
     * Called by /discover/locals — used by travelers to find locals at their destinations.
     */
    public List<UserSuggestionDTO> discoverLocals(UUID requesterId) {
        List<TripResponseDTO> destinations = getTripDestinations(requesterId);
        ConnectorResponseDTO requester = fetchUserProfile(requesterId); // Changed here
        List<UUID> localIds = fetchLocalsInCountries(destinations);
        List<ConnectorResponseDTO> candidates = fetchProfiles(localIds); // Changed here
        return scoreMatches(requester, candidates);
    }

    /**
     * Called by /discover/travelers — used by locals to find travelers coming to their country.
     */
    public List<UserSuggestionDTO> discoverTravelers(UUID requesterId) {
        String requesterCountry = fetchUserCountry(requesterId);
        ConnectorResponseDTO requester = fetchUserProfile(requesterId); // Changed here
        List<UUID> travelerIds = fetchTravelersVisiting(requesterCountry);
        List<ConnectorResponseDTO> candidates = fetchProfiles(travelerIds); // Changed here
        return scoreMatches(requester, candidates);
    }

    // ==== PRIVATE METHODS ====

    private List<TripResponseDTO> getTripDestinations(UUID userId) {
        return tripServiceClient.getMyTrips();
    }

    private List<UUID> fetchLocalsInCountries(List<String> countries) {
        return connectorServiceClient.getLocalsByCountries(countries);
    }

    private List<UUID> fetchTravelersVisiting(String country) {
        return tripServiceClient.getIncomingTrips(country);
    }

    private String fetchUserCountry(UUID userId) {
        return connectorServiceClient.getUserCountry(userId);
    }

    private ConnectorResponseDTO fetchUserProfile(UUID userId) {
        return connectorServiceClient.getPublicProfile(userId);
    }

    private List<ConnectorResponseDTO> fetchProfiles(List<UUID> userIds) {
        return connectorServiceClient.getPublicProfiles(userIds);
    }

    private List<UserSuggestionDTO> scoreMatches(ConnectorResponseDTO requester, List<ConnectorResponseDTO> candidates) {
        return openAiClient.rankCandidatesByRelevance(requester, candidates);
    }
}