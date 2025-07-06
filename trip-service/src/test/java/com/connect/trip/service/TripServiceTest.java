package com.connect.trip.service;

import com.connect.trip.dto.request.TripRequestDTO;
import com.connect.trip.dto.response.TripResponseDTO;
import com.connect.trip.enums.City;
import com.connect.trip.enums.Country;
import com.connect.trip.exception.TripNotFoundException;
import com.connect.trip.mapper.TripMapper;
import com.connect.trip.model.Trip;
import com.connect.trip.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock private TripRepository tripRepository;
    @Mock private TripMapper tripMapper;

    @InjectMocks private TripService tripService;

    private UUID userId;
    private Trip trip;
    private TripRequestDTO requestDto;
    private TripResponseDTO responseDto;
    private String tripId;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        tripId = UUID.randomUUID().toString();

        trip = Trip.builder()
                .userId(userId)
                .country(Country.ISRAEL)
                .city(City.TEL_AVIV)
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 6, 10))
                .build();

        requestDto = new TripRequestDTO("Israel", "Tel Aviv", "2025-06-01", "2025-06-10");

        responseDto = TripResponseDTO.builder()
                .userId(userId.toString())
                .country("Israel")
                .city("Tel Aviv")
                .startDate("2025-06-01")
                .endDate("2025-06-10")
                .build();
    }

    @Test
    void createTrip_withValidRequest_shouldSaveAndReturnDto() {
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);
        when(tripMapper.toDto(trip)).thenReturn(responseDto);

        TripResponseDTO result = tripService.createTrip(requestDto, userId);

        assertNotNull(result);
        assertEquals("Israel", result.getCountry());
        assertEquals("Tel Aviv", result.getCity());

        verify(tripRepository, times(2)).save(any(Trip.class)); // service currently saves twice
        verify(tripMapper).toDto(trip);
    }

    @Test
    void getTripsByUser_withExistingUserId_shouldReturnDtoList() {
        List<Trip> trips = List.of(trip);
        when(tripRepository.findByUserId(userId)).thenReturn(trips);
        when(tripMapper.toDto(trip)).thenReturn(responseDto);

        List<TripResponseDTO> result = tripService.getTripsByUser(userId);

        assertEquals(1, result.size());
        assertEquals("Israel", result.get(0).getCountry());

        verify(tripRepository).findByUserId(userId);
        verify(tripMapper).toDto(trip);
    }

    @Test
    void updateTrip_withValidIdAndRequest_shouldUpdateAndReturnDto() throws TripNotFoundException {
        TripRequestDTO updateRequest = new TripRequestDTO("Israel", "Jerusalem", "2025-07-01", "2025-07-05");
        Trip updatedTrip = Trip.builder()
                .userId(userId)
                .country(Country.ISRAEL)
                .city(City.JERUSALEM)
                .startDate(LocalDate.of(2025, 7, 1))
                .endDate(LocalDate.of(2025, 7, 5))
                .build();
        TripResponseDTO updatedDto = TripResponseDTO.builder()
                .userId(userId.toString())
                .country("Israel")
                .city("Jerusalem")
                .startDate("2025-07-01")
                .endDate("2025-07-05")
                .build();

        when(tripRepository.findByIdAndUserId(UUID.fromString(tripId), userId)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenReturn(updatedTrip);
        when(tripMapper.toDto(updatedTrip)).thenReturn(updatedDto);

        TripResponseDTO result = tripService.updateTrip(tripId, updateRequest, userId);

        assertEquals("Jerusalem", result.getCity());
        assertEquals("Israel", result.getCountry());
        assertEquals("2025-07-01", result.getStartDate());

        verify(tripRepository).findByIdAndUserId(UUID.fromString(tripId), userId);
        verify(tripRepository).save(any(Trip.class));
        verify(tripMapper).toDto(updatedTrip);
    }

    @Test
    void updateTrip_withNonExistingTrip_shouldThrowTripNotFoundException() {
        when(tripRepository.findByIdAndUserId(UUID.fromString(tripId), userId)).thenReturn(Optional.empty());

        TripNotFoundException ex = assertThrows(TripNotFoundException.class,
                () -> tripService.updateTrip(tripId, requestDto, userId));

        assertTrue(ex.getMessage().contains("Trip not found or unauthorized"));
        verify(tripRepository).findByIdAndUserId(UUID.fromString(tripId), userId);
        verifyNoMoreInteractions(tripRepository, tripMapper);
    }

    @Test
    void deleteTrip_withExistingTrip_shouldDeleteAndReturnDto() throws TripNotFoundException {
        when(tripRepository.findByIdAndUserId(UUID.fromString(tripId), userId)).thenReturn(Optional.of(trip));
        doNothing().when(tripRepository).deleteById(UUID.fromString(tripId));
        when(tripMapper.toDto(trip)).thenReturn(responseDto);

        TripResponseDTO result = tripService.deleteTrip(tripId, userId);

        assertEquals("Israel", result.getCountry());
        verify(tripRepository).findByIdAndUserId(UUID.fromString(tripId), userId);
        verify(tripRepository).deleteById(UUID.fromString(tripId));
        verify(tripMapper).toDto(trip);
    }

    @Test
    void deleteTrip_withNonExistingTrip_shouldThrowTripNotFoundException() {
        when(tripRepository.findByIdAndUserId(UUID.fromString(tripId), userId)).thenReturn(Optional.empty());

        TripNotFoundException ex = assertThrows(TripNotFoundException.class,
                () -> tripService.deleteTrip(tripId, userId));

        assertTrue(ex.getMessage().contains("Trip not found"));
        verify(tripRepository).findByIdAndUserId(UUID.fromString(tripId), userId);
        verifyNoMoreInteractions(tripRepository, tripMapper);
    }

    @Test
    void getIncomingTrips_withValidFilters_shouldReturnFilteredDtoList() {
        List<Trip> trips = List.of(trip);
        when(tripRepository.searchTrips(anyString(), anyString(), any(), any())).thenReturn(trips);
        when(tripMapper.toDto(trip)).thenReturn(responseDto);

        List<TripResponseDTO> result = tripService.getIncomingTrips("Israel", "Tel Aviv", "2025-06-01", "2025-06-10");

        assertEquals(1, result.size());
        assertEquals("Israel", result.get(0).getCountry());

        verify(tripRepository).searchTrips(eq("Israel"), eq("Tel Aviv"), eq(LocalDate.parse("2025-06-01")), eq(LocalDate.parse("2025-06-10")));
        verify(tripMapper).toDto(trip);
    }
}
