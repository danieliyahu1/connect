package com.connect.discovery.service;

import com.connect.discovery.client.ConnectorServiceClient;
import com.connect.discovery.client.TripServiceClient;
import com.connect.discovery.client.OpenAiClient;
import com.connect.discovery.dto.ConnectorResponseDTO;
import com.connect.discovery.dto.IncomingTripRequestDto;
import com.connect.discovery.dto.TripResponseDTO;
import com.connect.discovery.dto.UserSuggestionDTO;
import com.connect.discovery.mapper.TripMapper;
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
    private final TripMapper tripMapper;
    /**
     * Called by /discover/locals — used by travelers to find locals at their destinations.
     */
    public List<UserSuggestionDTO> discoverLocals() {
        List<TripResponseDTO> destinations = getTripDestinations();
        ConnectorResponseDTO requester = fetchUserProfile(); // Changed here
        List<String> localCountries = getLocalCountries(destinations);
        List<ConnectorResponseDTO> candidates = fetchProfilesByCountry(localCountries); // Changed here
        return scoreMatches(requester, candidates);
    }

    /**
     * Called by /discover/travelers — used by locals to find travelers coming to their country.
     */
    public List<UserSuggestionDTO> discoverTravelers() {
        ConnectorResponseDTO requester = fetchUserProfile(); // Changed here
        List<TripResponseDTO> tripsOfTravelersToLocalCountry = fetchTravelersVisiting(requester.getCountry());
        List<ConnectorResponseDTO> candidates = fetchProfilesById(tripsOfTravelersToLocalCountry); // Changed here
        return scoreMatches(requester, candidates);
    }

    // ==== PRIVATE METHODS ====

    private List<TripResponseDTO> getTripDestinations() {
        return tripServiceClient.getMyTrips();
    }

    private List<String> getLocalCountries(List<TripResponseDTO> trips) {
        return trips.stream()
                .map(TripResponseDTO::getCountry)
                .distinct()
                .toList();
    }

    private List<TripResponseDTO> fetchTravelersVisiting(String country) {
        return tripServiceClient.getIncomingTrips(
                IncomingTripRequestDto.builder()
                        .country(country)
                        .build()
        );
    }

    private ConnectorResponseDTO fetchUserProfile() {
        return connectorServiceClient.getPublicProfile();
    }

    private List<ConnectorResponseDTO> fetchProfilesByCountry(List<String> userCountriesDestinations) {
        return connectorServiceClient.fetchProfilesByCountry(userCountriesDestinations);
    }

    private List<ConnectorResponseDTO> fetchProfilesById(List<TripResponseDTO> userCountriesDestinations) {
        return connectorServiceClient.fetchProfilesById(
                userCountriesDestinations.stream()
                        .map(TripResponseDTO::getUserId)
                        .distinct()
                        .map(UUID::fromString)
                        .toList()
        );
    }

    private List<UserSuggestionDTO> scoreMatches(ConnectorResponseDTO requester, List<ConnectorResponseDTO> candidates) {
        return openAiClient.rankCandidatesByRelevance(requester, candidates);
    }
}