package com.connect.discovery.service;

import com.connect.discovery.client.ConnectorServiceClient;
import com.connect.discovery.client.OpenAiClient;
import com.connect.discovery.client.TripServiceClient;
import com.connect.discovery.dto.*;
import com.connect.discovery.mapper.TripMapper;
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
    private TripServiceClient tripServiceClient;

    @Mock
    private ConnectorServiceClient connectorServiceClient;

    @Mock
    private OpenAiClient openAiClient;

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
                createUserSuggestionDTO(candidate1.getUserId(), "Bob", "Munich", "GERMANY", 30, "Matched on music"),
                createUserSuggestionDTO(candidate2.getUserId(), "Eve", "Hamburg", "GERMANY", 25, "Matched on sports")
        );
    }

    @Test
    void discoverLocals_shouldReturnDetailedSuggestions() {
        List<TripResponseDTO> myTrips = List.of(trip1, trip2);
        List<String> countries = List.of("GERMANY");
        List<ConnectorResponseDTO> candidates = List.of(candidate1, candidate2);

        when(tripServiceClient.getMyTrips()).thenReturn(myTrips);
        when(connectorServiceClient.getPublicProfile()).thenReturn(requester);
        when(connectorServiceClient.fetchProfilesByCountry(countries)).thenReturn(candidates);
        when(openAiClient.rankCandidatesByRelevance(requester, candidates)).thenReturn(mockSuggestions);

        List<UserSuggestionDTO> results = discoveryService.discoverLocals();

        assertNotNull(results);
        assertEquals(2, results.size());

        UserSuggestionDTO suggestion1 = results.get(0);
        assertEquals(candidate1.getUserId().toString(), suggestion1.getUserId());
        assertEquals("Bob", suggestion1.getName());
        assertEquals("Munich", suggestion1.getCity());
        assertEquals("GERMANY", suggestion1.getCountry());
        assertEquals(30, suggestion1.getAge());
        assertEquals("Matched on music", suggestion1.getReason());
        assertEquals("http://example.com/profile.jpg", suggestion1.getProfilePictureUrl());

        UserSuggestionDTO suggestion2 = results.get(1);
        assertEquals(candidate2.getUserId().toString(), suggestion2.getUserId());
        assertEquals("Eve", suggestion2.getName());
        assertEquals("Hamburg", suggestion2.getCity());
        assertEquals("GERMANY", suggestion2.getCountry());
        assertEquals(25, suggestion2.getAge());
        assertEquals("Matched on sports", suggestion2.getReason());
        assertEquals("http://example.com/profile.jpg", suggestion2.getProfilePictureUrl());

        verify(tripServiceClient).getMyTrips();
        verify(connectorServiceClient).getPublicProfile();
        verify(connectorServiceClient).fetchProfilesByCountry(countries);
        verify(openAiClient).rankCandidatesByRelevance(requester, candidates);
    }

    @Test
    void discoverTravelers_shouldReturnDetailedSuggestions() {
        List<TripResponseDTO> trips = List.of(trip1, trip2);
        List<ConnectorResponseDTO> candidates = List.of(candidate1, candidate2);

        when(connectorServiceClient.getPublicProfile()).thenReturn(requester);
        when(tripServiceClient.getIncomingTrips(any(IncomingTripRequestDto.class))).thenReturn(trips);
        when(connectorServiceClient.fetchProfilesById(anyList())).thenReturn(candidates);
        when(openAiClient.rankCandidatesByRelevance(requester, candidates)).thenReturn(mockSuggestions);

        List<UserSuggestionDTO> results = discoveryService.discoverTravelers();

        assertNotNull(results);
        assertEquals(2, results.size());

        UserSuggestionDTO suggestion1 = results.get(0);
        assertEquals("Bob", suggestion1.getName());
        assertEquals(30, suggestion1.getAge());
        assertEquals("GERMANY", suggestion1.getCountry());

        UserSuggestionDTO suggestion2 = results.get(1);
        assertEquals("Eve", suggestion2.getName());
        assertEquals(25, suggestion2.getAge());
        assertEquals("Hamburg", suggestion2.getCity());

        verify(connectorServiceClient).getPublicProfile();
        verify(tripServiceClient).getIncomingTrips(any(IncomingTripRequestDto.class));
        verify(connectorServiceClient).fetchProfilesById(anyList());
        verify(openAiClient).rankCandidatesByRelevance(requester, candidates);
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

    private UserSuggestionDTO createUserSuggestionDTO(UUID userId, String name, String city, String country, int age, String reason) {
        return UserSuggestionDTO.builder()
                .userId(userId.toString())
                .name(name)
                .city(city)
                .country(country)
                .age(age)
                .reason(reason)
                .profilePictureUrl("http://example.com/profile.jpg")
                .build();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
