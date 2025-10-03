package com.akatsuki.discovery.service;

import com.akatsuki.discovery.dto.ConnectorResponseDTO;
import com.akatsuki.discovery.dto.TripResponseDTO;
import com.akatsuki.discovery.dto.UserSuggestionDTO;
import com.akatsuki.discovery.mapper.TripMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscoveryService {

    private final TripService tripService;
    private final ConnectorService connectorService;
    private final AiService aiService;

    /**
     * Called by /discover/locals — used by travelers to find locals at their destinations.
     */
    public List<UserSuggestionDTO> discoverLocals() throws JsonProcessingException {
        List<TripResponseDTO> destinations = getTripDestinations();
        ConnectorResponseDTO requester = fetchUserProfile(); // Changed here
        List<String> localCountries = getLocalCountries(destinations);
        List<ConnectorResponseDTO> candidates = fetchProfilesByCountry(localCountries); // Changed here
        return scoreMatches(requester, candidates);
    }

    /**
     * Called by /discover/travelers — used by locals to find travelers coming to their country.
     */
    public List<UserSuggestionDTO> discoverTravelers() throws JsonProcessingException {
        ConnectorResponseDTO requester = fetchUserProfile(); // Changed here
        List<TripResponseDTO> tripsOfTravelersToLocalCountry = fetchTravelersVisiting(requester.getCountry());
        List<ConnectorResponseDTO> candidates = fetchProfilesById(tripsOfTravelersToLocalCountry); // Changed here
        return scoreMatches(requester, candidates);
    }

    // ==== PRIVATE METHODS ====

    private List<TripResponseDTO> getTripDestinations() {
        return tripService.getMyTrips();
    }

    private List<String> getLocalCountries(List<TripResponseDTO> trips) {
        return trips.stream()
                .map(TripResponseDTO::getCountry)
                .distinct()
                .toList();
    }

    private List<TripResponseDTO> fetchTravelersVisiting(String country) {
        return tripService.fetchTravelersVisiting(country);
    }

    private ConnectorResponseDTO fetchUserProfile() {
        return connectorService.fetchUserProfile();
    }

    private List<ConnectorResponseDTO> fetchProfilesByCountry(List<String> userCountriesDestinations) {
        return connectorService.fetchProfilesByCountry(userCountriesDestinations);
    }

    private List<ConnectorResponseDTO> fetchProfilesById(List<TripResponseDTO> userCountriesDestinations) {
        return connectorService.fetchProfilesById(userCountriesDestinations);
    }

    private List<UserSuggestionDTO> scoreMatches(ConnectorResponseDTO requester, List<ConnectorResponseDTO> candidates) throws JsonProcessingException {
        return aiService.rankCandidatesByRelevance(requester, candidates);
    }
}