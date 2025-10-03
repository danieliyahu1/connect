package com.akatsuki.discovery.service;

import com.akatsuki.discovery.dto.*;
import com.akatsuki.discovery.mapper.TripMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscoveryServiceTest {

    @Mock
    private TripService tripService;

    @Mock
    private ConnectorService connectorService;

    @Mock
    private AiService aiService;

    @Mock
    private TripMapper tripMapper;

    @InjectMocks
    private DiscoveryService discoveryService;

    private ConnectorResponseDTO requester;
    private ConnectorResponseDTO candidate1;
    private ConnectorResponseDTO candidate2;
    private TripResponseDTO trip1;
    private TripResponseDTO trip2;
    private List<UserSuggestionDTO> mockSuggestions;

    @BeforeEach
    void setup() throws Exception {
        requester = createConnectorResponse(UUID.randomUUID(), "Alice", "GERMANY", "Berlin");
        candidate1 = createConnectorResponse(UUID.randomUUID(), "Bob", "GERMANY", "Munich");
        candidate2 = createConnectorResponse(UUID.randomUUID(), "Eve", "GERMANY", "Hamburg");

        trip1 = TripResponseDTO.builder()
                .userId(candidate1.getUserId().toString())
                .publicId("trip-1")
                .country("GERMANY")
                .city("Munich")
                .startDate("2024-12-01")
                .endDate("2024-12-10")
                .build();

        trip2 = TripResponseDTO.builder()
                .userId(candidate2.getUserId().toString())
                .publicId("trip-2")
                .country("GERMANY")
                .city("Hamburg")
                .startDate("2024-12-05")
                .endDate("2024-12-12")
                .build();

        mockSuggestions = List.of(
                createUserSuggestionDTO(candidate1.getUserId(), "Bob", "Munich", "GERMANY", "Matched on music", 0.058386),
                createUserSuggestionDTO(candidate2.getUserId(), "Eve", "Hamburg", "GERMANY", "Matched on sports", 0.32592)
        );
    }

    @Test
    void discoverLocals_shouldReturnDetailedSuggestions() throws JsonProcessingException {
        List<TripResponseDTO> myTrips = List.of(trip1, trip2);
        List<String> countries = List.of("GERMANY");
        List<ConnectorResponseDTO> candidates = List.of(candidate1, candidate2);

        when(tripService.getMyTrips()).thenReturn(myTrips);
        when(connectorService.fetchUserProfile()).thenReturn(requester);
        when(connectorService.fetchProfilesByCountry(countries)).thenReturn(candidates);
        when(aiService.rankCandidatesByRelevance(requester, candidates)).thenReturn(mockSuggestions);

        List<UserSuggestionDTO> results = discoveryService.discoverLocals();

        assertNotNull(results);
        assertEquals(2, results.size());

        UserSuggestionDTO suggestion1 = results.get(0);
        assertEquals(candidate1.getUserId().toString(), suggestion1.getUserId());
        assertEquals("Bob", suggestion1.getName());
        assertEquals("Munich", suggestion1.getCity());
        assertEquals("GERMANY", suggestion1.getCountry());
        assertEquals("Matched on music", suggestion1.getReason());
        assertEquals("http://example.com/profile.jpg", suggestion1.getProfilePictureUrl());

        UserSuggestionDTO suggestion2 = results.get(1);
        assertEquals(candidate2.getUserId().toString(), suggestion2.getUserId());
        assertEquals("Eve", suggestion2.getName());
        assertEquals("Hamburg", suggestion2.getCity());
        assertEquals("GERMANY", suggestion2.getCountry());
        assertEquals("Matched on sports", suggestion2.getReason());
        assertEquals("http://example.com/profile.jpg", suggestion2.getProfilePictureUrl());

        verify(tripService).getMyTrips();
        verify(connectorService).fetchUserProfile();
        verify(connectorService).fetchProfilesByCountry(countries);
        verify(aiService).rankCandidatesByRelevance(requester, candidates);
    }

    @Test
    void discoverTravelers_shouldReturnDetailedSuggestions() throws JsonProcessingException {
        List<TripResponseDTO> trips = List.of(trip1, trip2);
        List<ConnectorResponseDTO> candidates = List.of(candidate1, candidate2);

        when(connectorService.fetchUserProfile()).thenReturn(requester);
        when(tripService.fetchTravelersVisiting(anyString())).thenReturn(trips);
        when(connectorService.fetchProfilesById(anyList())).thenReturn(candidates);
        when(aiService.rankCandidatesByRelevance(requester, candidates)).thenReturn(mockSuggestions);

        List<UserSuggestionDTO> results = discoveryService.discoverTravelers();

        assertNotNull(results);
        assertEquals(2, results.size());

        UserSuggestionDTO suggestion1 = results.get(0);
        assertEquals("Bob", suggestion1.getName());
        assertEquals("GERMANY", suggestion1.getCountry());

        UserSuggestionDTO suggestion2 = results.get(1);
        assertEquals("Eve", suggestion2.getName());
        assertEquals("Hamburg", suggestion2.getCity());

        verify(connectorService).fetchUserProfile();
        verify(tripService).fetchTravelersVisiting(anyString());
        verify(connectorService).fetchProfilesById(anyList());
        verify(aiService).rankCandidatesByRelevance(requester, candidates);
    }

    // === Helpers ===

    private ConnectorResponseDTO createConnectorResponse(UUID id, String firstName, String country, String city) throws Exception {
        ConnectorImageDTO image = new ConnectorImageDTO();
        setField(image, "mediaUrl", "http://example.com/image.jpg");
        setField(image, "orderIndex", 0);

        ConnectorSocialMediaDTO social = new ConnectorSocialMediaDTO();
        setField(social, "platform", "Instagram");
        setField(social, "profileUrl", "http://instagram.com/profile");

        ConnectorResponseDTO dto = new ConnectorResponseDTO();
        setField(dto, "userId", id);
        setField(dto, "firstName", firstName);
        setField(dto, "country", country);
        setField(dto, "city", city);
        setField(dto, "galleryImages", List.of(image));
        setField(dto, "socialMediaLinks", List.of(social));
        return dto;
    }

    private UserSuggestionDTO createUserSuggestionDTO(UUID userId, String name, String city, String country, String reason, double score) {
        return UserSuggestionDTO.builder()
                .userId(userId.toString())
                .name(name)
                .city(city)
                .country(country)
                .reason(reason)
                .score(score)
                .profilePictureUrl("http://example.com/profile.jpg")
                .build();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
