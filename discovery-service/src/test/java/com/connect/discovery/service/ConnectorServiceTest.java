package com.akatsuki.discovery.service;

import com.akatsuki.discovery.client.ConnectorServiceClient;
import com.akatsuki.discovery.dto.ConnectorResponseDTO;
import com.akatsuki.discovery.dto.TripResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectorServiceTest {

    @Mock
    private ConnectorServiceClient connectorServiceClient;

    @InjectMocks
    private ConnectorService connectorService;

    private ConnectorResponseDTO userProfile;
    private ConnectorResponseDTO profile1;
    private ConnectorResponseDTO profile2;
    private TripResponseDTO trip1;
    private TripResponseDTO trip2;

    @BeforeEach
    void setup() throws Exception {
        userProfile = createConnectorResponse(UUID.randomUUID(), "Alice", "USA", "New York");
        profile1 = createConnectorResponse(UUID.randomUUID(), "Bob", "USA", "Chicago");
        profile2 = createConnectorResponse(UUID.randomUUID(), "Eve", "USA", "San Francisco");

        trip1 = TripResponseDTO.builder()
                .userId(profile1.getUserId().toString())
                .publicId("trip-1")
                .country("USA")
                .city("Chicago")
                .build();

        trip2 = TripResponseDTO.builder()
                .userId(profile2.getUserId().toString())
                .publicId("trip-2")
                .country("USA")
                .city("San Francisco")
                .build();

    }

    // ========== Happy Path Tests ==========

    @Test
    void fetchUserProfile_shouldReturnUserProfile() {
        when(connectorServiceClient.getPublicProfile()).thenReturn(userProfile);

        ConnectorResponseDTO result = connectorService.fetchUserProfile();

        assertNotNull(result);
        assertEquals(userProfile.getUserId(), result.getUserId());
        assertEquals("Alice", result.getFirstName());
        verify(connectorServiceClient).getPublicProfile();
    }

    @Test
    void fetchProfilesByCountry_shouldReturnListOfProfiles() {
        List<String> countries = List.of("USA");
        List<ConnectorResponseDTO> profiles = List.of(profile1, profile2);

        when(connectorServiceClient.fetchProfilesByCountry(countries)).thenReturn(profiles);

        List<ConnectorResponseDTO> result = connectorService.fetchProfilesByCountry(countries);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(profile1.getUserId(), result.get(0).getUserId());
        verify(connectorServiceClient).fetchProfilesByCountry(countries);
    }

    @Test
    void fetchProfilesById_shouldReturnProfilesByTripUserIds() {
        List<TripResponseDTO> trips = List.of(trip1, trip2);
        List<ConnectorResponseDTO> profiles = List.of(profile1, profile2);

        when(connectorServiceClient.fetchProfilesById(anyList())).thenReturn(profiles);

        List<ConnectorResponseDTO> result = connectorService.fetchProfilesById(trips);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(profile2.getUserId(), result.get(1).getUserId());

        ArgumentCaptor<List<UUID>> captor = ArgumentCaptor.forClass(List.class);
        verify(connectorServiceClient).fetchProfilesById(captor.capture());

        List<UUID> capturedIds = captor.getValue();
        assertTrue(capturedIds.contains(UUID.fromString(trip1.getUserId())));
        assertTrue(capturedIds.contains(UUID.fromString(trip2.getUserId())));
    }

    // ========== Edge Cases ==========

    @Test
    void fetchProfilesByCountry_withEmptyList_shouldReturnEmptyList() {
        List<String> emptyCountries = Collections.emptyList();
        when(connectorServiceClient.fetchProfilesByCountry(emptyCountries)).thenReturn(Collections.emptyList());

        List<ConnectorResponseDTO> result = connectorService.fetchProfilesByCountry(emptyCountries);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(connectorServiceClient).fetchProfilesByCountry(emptyCountries);
    }

    @Test
    void fetchProfilesById_withEmptyList_shouldReturnEmptyList() {
        List<TripResponseDTO> emptyTrips = Collections.emptyList();
        when(connectorServiceClient.fetchProfilesById(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<ConnectorResponseDTO> result = connectorService.fetchProfilesById(emptyTrips);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(connectorServiceClient).fetchProfilesById(Collections.emptyList());
    }

    // ========== Error Handling ==========

    @Test
    void fetchUserProfile_whenClientThrowsException_shouldPropagate() {
        when(connectorServiceClient.getPublicProfile()).thenThrow(new RuntimeException("Service unavailable"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> connectorService.fetchUserProfile());
        assertEquals("Service unavailable", ex.getMessage());

        verify(connectorServiceClient).getPublicProfile();
    }

    @Test
    void fetchProfilesByCountry_whenClientThrowsException_shouldPropagate() {
        List<String> countries = List.of("USA");
        when(connectorServiceClient.fetchProfilesByCountry(countries)).thenThrow(new RuntimeException("Service unavailable"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> connectorService.fetchProfilesByCountry(countries));
        assertEquals("Service unavailable", ex.getMessage());

        verify(connectorServiceClient).fetchProfilesByCountry(countries);
    }

    @Test
    void fetchProfilesById_whenClientThrowsException_shouldPropagate() {
        List<TripResponseDTO> trips = List.of(trip1);
        when(connectorServiceClient.fetchProfilesById(anyList())).thenThrow(new RuntimeException("Service unavailable"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> connectorService.fetchProfilesById(trips));
        assertEquals("Service unavailable", ex.getMessage());

        verify(connectorServiceClient).fetchProfilesById(anyList());
    }

    // ========== Helper method to create DTOs ==========

    private ConnectorResponseDTO createConnectorResponse(UUID id, String firstName, String country, String city) throws Exception {
        ConnectorResponseDTO dto = new ConnectorResponseDTO();

        var userIdField = ConnectorResponseDTO.class.getDeclaredField("userId");
        userIdField.setAccessible(true);
        userIdField.set(dto, id);

        var firstNameField = ConnectorResponseDTO.class.getDeclaredField("firstName");
        firstNameField.setAccessible(true);
        firstNameField.set(dto, firstName);

        var countryField = ConnectorResponseDTO.class.getDeclaredField("country");
        countryField.setAccessible(true);
        countryField.set(dto, country);

        var cityField = ConnectorResponseDTO.class.getDeclaredField("city");
        cityField.setAccessible(true);
        cityField.set(dto, city);

        return dto;
    }
}
