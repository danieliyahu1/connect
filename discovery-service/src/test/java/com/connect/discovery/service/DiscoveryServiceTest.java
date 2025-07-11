package com.connect.discovery.service;

import com.connect.discovery.client.ConnectorServiceClient;
import com.connect.discovery.client.OpenAiClient;
import com.connect.discovery.client.TripServiceClient;
import com.connect.discovery.dto.ConnectorResponseDTO;
import com.connect.discovery.dto.ConnectorImageDTO;
import com.connect.discovery.dto.ConnectorSocialMediaDTO;
import com.connect.discovery.dto.UserSuggestionDTO;
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

    @InjectMocks
    private DiscoveryService discoveryService;

    private UUID requesterId;
    private ConnectorResponseDTO requester;
    private ConnectorResponseDTO candidate1;
    private ConnectorResponseDTO candidate2;
    private List<UserSuggestionDTO> mockSuggestions;

    @BeforeEach
    void setup() throws Exception {
        requesterId = UUID.randomUUID();

        requester = createConnectorResponse(requesterId, "Alice", "GERMANY", "Berlin");
        candidate1 = createConnectorResponse(UUID.randomUUID(), "Bob", "GERMANY", "Munich");
        candidate2 = createConnectorResponse(UUID.randomUUID(), "Eve", "GERMANY", "Hamburg");

        mockSuggestions = List.of(
                createUserSuggestionDTO(candidate1.getUserId(), candidate1.getFirstName(), candidate1.getCity(), candidate1.getCountry(), 30, "Recommended based on interests"),
                createUserSuggestionDTO(candidate2.getUserId(), candidate2.getFirstName(), candidate2.getCity(), candidate2.getCountry(), 25, "Recommended based on interests")
        );
    }

    @Test
    void discoverLocals_shouldReturnSuggestions() {
        // Arrange
        List<String> destinations = List.of("GERMANY", "FRANCE");
        List<UUID> localIds = List.of(candidate1.getUserId(), candidate2.getUserId());
        List<ConnectorResponseDTO> candidates = List.of(candidate1, candidate2);

        when(tripServiceClient.getAllTripDestinations(requesterId)).thenReturn(destinations);
        when(connectorServiceClient.getPublicProfile(requesterId)).thenReturn(requester);
        when(connectorServiceClient.getLocalsByCountries(destinations)).thenReturn(localIds);
        when(connectorServiceClient.getPublicProfiles(localIds)).thenReturn(candidates);
        when(openAiClient.rankCandidatesByRelevance(requester, candidates)).thenReturn(mockSuggestions);

        // Act
        List<UserSuggestionDTO> results = discoveryService.discoverLocals(requesterId);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(candidate1.getUserId().toString(), results.get(0).getUserId());
        assertEquals(candidate2.getUserId().toString(), results.get(1).getUserId());

        verify(tripServiceClient).getAllTripDestinations(requesterId);
        verify(connectorServiceClient).getPublicProfile(requesterId);
        verify(connectorServiceClient).getLocalsByCountries(destinations);
        verify(connectorServiceClient).getPublicProfiles(localIds);
        verify(openAiClient).rankCandidatesByRelevance(requester, candidates);
    }

    @Test
    void discoverTravelers_shouldReturnSuggestions() {
        // Arrange
        String requesterCountry = "GERMANY";
        List<UUID> travelerIds = List.of(candidate1.getUserId(), candidate2.getUserId());
        List<ConnectorResponseDTO> candidates = List.of(candidate1, candidate2);

        when(connectorServiceClient.getUserCountry(requesterId)).thenReturn(requesterCountry);
        when(connectorServiceClient.getPublicProfile(requesterId)).thenReturn(requester);
        when(tripServiceClient.getTravelersByCountry(requesterCountry)).thenReturn(travelerIds);
        when(connectorServiceClient.getPublicProfiles(travelerIds)).thenReturn(candidates);
        when(openAiClient.rankCandidatesByRelevance(requester, candidates)).thenReturn(mockSuggestions);

        // Act
        List<UserSuggestionDTO> results = discoveryService.discoverTravelers(requesterId);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(candidate1.getUserId().toString(), results.get(0).getUserId());
        assertEquals(candidate2.getUserId().toString(), results.get(1).getUserId());

        verify(connectorServiceClient).getUserCountry(requesterId);
        verify(connectorServiceClient).getPublicProfile(requesterId);
        verify(tripServiceClient).getTravelersByCountry(requesterCountry);
        verify(connectorServiceClient).getPublicProfiles(travelerIds);
        verify(openAiClient).rankCandidatesByRelevance(requester, candidates);
    }

    // Helper methods for creating DTOs with reflection due to no setters

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
