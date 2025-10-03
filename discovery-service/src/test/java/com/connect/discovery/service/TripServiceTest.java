package com.akatsuki.discovery.service;

import com.akatsuki.discovery.client.TripServiceClient;
import com.akatsuki.discovery.dto.IncomingTripRequestDto;
import com.akatsuki.discovery.dto.TripResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripServiceClient tripServiceClient;

    @InjectMocks
    private TripService tripService;

    private List<TripResponseDTO> mockTrips;

    @BeforeEach
    void setup() {
        TripResponseDTO trip1 = TripResponseDTO.builder()
                .userId(UUID.randomUUID().toString())
                .publicId("trip-1")
                .country("USA")
                .city("New York")
                .startDate("2024-09-01")
                .endDate("2024-09-10")
                .build();

        TripResponseDTO trip2 = TripResponseDTO.builder()
                .userId(UUID.randomUUID().toString())
                .publicId("trip-2")
                .country("France")
                .city("Paris")
                .startDate("2024-10-05")
                .endDate("2024-10-15")
                .build();

        mockTrips = List.of(trip1, trip2);
    }

    // === Normal Behavior Tests ===

    @Test
    void getMyTrips_shouldReturnListFromClient() {
        when(tripServiceClient.getMyTrips()).thenReturn(mockTrips);

        List<TripResponseDTO> result = tripService.getMyTrips();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("trip-1", result.get(0).getPublicId());
        assertEquals("trip-2", result.get(1).getPublicId());

        verify(tripServiceClient, times(1)).getMyTrips();
        verifyNoMoreInteractions(tripServiceClient);
    }

    @Test
    void fetchTravelersVisiting_shouldCallClientWithCorrectCountry() {
        String country = "Italy";

        when(tripServiceClient.getIncomingTrips(any(IncomingTripRequestDto.class))).thenReturn(mockTrips);

        List<TripResponseDTO> result = tripService.fetchTravelersVisiting(country);

        assertNotNull(result);
        assertEquals(2, result.size());

        ArgumentCaptor<IncomingTripRequestDto> captor = ArgumentCaptor.forClass(IncomingTripRequestDto.class);
        verify(tripServiceClient).getIncomingTrips(captor.capture());
        IncomingTripRequestDto passedRequest = captor.getValue();

        assertEquals(country, passedRequest.getCountry());

        verifyNoMoreInteractions(tripServiceClient);
    }

    // === Edge Cases: Empty or Null Returns ===

    @Test
    void getMyTrips_shouldReturnEmptyListWhenClientReturnsEmpty() {
        when(tripServiceClient.getMyTrips()).thenReturn(List.of());

        List<TripResponseDTO> result = tripService.getMyTrips();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(tripServiceClient).getMyTrips();
    }

    @Test
    void getMyTrips_shouldHandleNullReturnGracefully() {
        when(tripServiceClient.getMyTrips()).thenReturn(null);

        List<TripResponseDTO> result = tripService.getMyTrips();

        // Confirm null is returned without exception (or change based on your service logic)
        assertNull(result);

        verify(tripServiceClient).getMyTrips();
    }

    @Test
    void fetchTravelersVisiting_shouldReturnEmptyListWhenClientReturnsEmpty() {
        when(tripServiceClient.getIncomingTrips(any(IncomingTripRequestDto.class))).thenReturn(List.of());

        List<TripResponseDTO> result = tripService.fetchTravelersVisiting("Spain");

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(tripServiceClient).getIncomingTrips(any(IncomingTripRequestDto.class));
    }

    @Test
    void fetchTravelersVisiting_shouldHandleNullReturnGracefully() {
        when(tripServiceClient.getIncomingTrips(any(IncomingTripRequestDto.class))).thenReturn(null);

        List<TripResponseDTO> result = tripService.fetchTravelersVisiting("Spain");

        assertNull(result);

        verify(tripServiceClient).getIncomingTrips(any(IncomingTripRequestDto.class));
    }

    // === Exception Handling ===

    @Test
    void getMyTrips_shouldThrowRuntimeExceptionIfClientThrows() {
        when(tripServiceClient.getMyTrips()).thenThrow(new RuntimeException("Service unavailable"));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> tripService.getMyTrips());
        assertEquals("Service unavailable", thrown.getMessage());

        verify(tripServiceClient).getMyTrips();
    }

    @Test
    void fetchTravelersVisiting_shouldThrowRuntimeExceptionIfClientThrows() {
        when(tripServiceClient.getIncomingTrips(any(IncomingTripRequestDto.class)))
                .thenThrow(new RuntimeException("Service down"));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> tripService.fetchTravelersVisiting("Brazil"));
        assertEquals("Service down", thrown.getMessage());

        verify(tripServiceClient).getIncomingTrips(any(IncomingTripRequestDto.class));
    }

    // === Verify No Extra Calls ===

    @Test
    void noUnexpectedCallsToClient() {
        when(tripServiceClient.getMyTrips()).thenReturn(mockTrips);
        tripService.getMyTrips();

        verify(tripServiceClient).getMyTrips();
        verifyNoMoreInteractions(tripServiceClient);
    }
}
