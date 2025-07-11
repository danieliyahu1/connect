package com.connect.trip.service;

import com.connect.trip.dto.request.TripRequestDTO;
import com.connect.trip.dto.response.TripResponseDTO;
import com.connect.trip.enums.City;
import com.connect.trip.enums.Country;
import com.connect.trip.exception.OverlapTripException;
import com.connect.trip.exception.IllegalEnumException;
import com.connect.trip.exception.TripNotFoundException;
import com.connect.trip.mapper.TripMapper;
import com.connect.trip.model.Trip;
import com.connect.trip.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
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
    private String publicId;

    @BeforeEach
    void setup() throws NoSuchFieldException, IllegalAccessException {
        userId = UUID.randomUUID();
        tripId = UUID.randomUUID().toString();

        trip = Trip.builder()
                .userId(userId)
                .country(Country.ISRAEL)
                .city(City.TEL_AVIV)
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 6, 10))
                .build();

        Field dbIdField = Trip.class.getDeclaredField("dbId");
        dbIdField.setAccessible(true);
        dbIdField.set(trip, UUID.fromString(tripId));

        publicId = trip.getPublicId().toString();

        requestDto = new TripRequestDTO("Israel", "Tel Aviv", "2025-06-01", "2025-06-10");

        responseDto = TripResponseDTO.builder()
                .userId(userId.toString())
                .country("Israel")
                .city("Tel Aviv")
                .startDate("2025-06-01")
                .endDate("2025-06-10")
                .publicId(publicId)
                .build();
    }

    @Test
    void createTrip_withValidRequest_shouldSaveAndReturnDto() throws OverlapTripException, IllegalEnumException {
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
    void updateTrip_withValidIdAndRequest_shouldUpdateAndReturnDto() throws TripNotFoundException, IllegalEnumException {
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
                .publicId(updatedTrip.getPublicId().toString())
                .build();

        when(tripRepository.findByPublicIdAndUserId(UUID.fromString(publicId), userId)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenReturn(updatedTrip);
        when(tripMapper.toDto(updatedTrip)).thenReturn(updatedDto);

        TripResponseDTO result = tripService.updateTrip(publicId, updateRequest, userId);

        assertEquals("Jerusalem", result.getCity());
        assertEquals("Israel", result.getCountry());
        assertEquals("2025-07-01", result.getStartDate());

        verify(tripRepository).findByPublicIdAndUserId(UUID.fromString(publicId), userId);
        verify(tripRepository).save(any(Trip.class));
        verify(tripMapper).toDto(updatedTrip);
    }

    @Test
    void updateTrip_withNonExistingTrip_shouldThrowTripNotFoundException() {
        when(tripRepository.findByPublicIdAndUserId(UUID.fromString(publicId), userId)).thenReturn(Optional.empty());

        TripNotFoundException ex = assertThrows(TripNotFoundException.class,
                () -> tripService.updateTrip(publicId, requestDto, userId));

        assertTrue(ex.getMessage().contains("Trip not found or unauthorized"));
        verify(tripRepository).findByPublicIdAndUserId(UUID.fromString(publicId), userId);
        verifyNoMoreInteractions(tripRepository, tripMapper);
    }

    @Test
    void deleteTrip_withExistingTrip_shouldDeleteAndReturnDto() throws TripNotFoundException {
        when(tripRepository.findByPublicIdAndUserId(UUID.fromString(publicId), userId)).thenReturn(Optional.of(trip));
        doNothing().when(tripRepository).deleteById(UUID.fromString(tripId));
        when(tripMapper.toDto(trip)).thenReturn(responseDto);

        TripResponseDTO result = tripService.deleteTrip(publicId, userId);

        assertEquals("Israel", result.getCountry());
        verify(tripRepository).findByPublicIdAndUserId(UUID.fromString(publicId), userId);
        verify(tripRepository).deleteById(UUID.fromString(tripId));
        verify(tripMapper).toDto(trip);
    }

    @Test
    void deleteTrip_withNonExistingTrip_shouldThrowTripNotFoundException() {
        when(tripRepository.findByPublicIdAndUserId(UUID.fromString(publicId), userId)).thenReturn(Optional.empty());

        TripNotFoundException ex = assertThrows(TripNotFoundException.class,
                () -> tripService.deleteTrip(publicId, userId));

        assertTrue(ex.getMessage().contains("Trip not found"));
        verify(tripRepository).findByPublicIdAndUserId(UUID.fromString(publicId), userId);
        verifyNoMoreInteractions(tripRepository, tripMapper);
    }

    @Test
    void getIncomingTrips_withValidFilters_shouldReturnFilteredDtoList() throws IllegalEnumException {
        List<Trip> trips = List.of(trip);
        when(tripRepository.searchTrips(any(Country.class), any(City.class), any(), any())).thenReturn(trips);
        when(tripMapper.toDto(trip)).thenReturn(responseDto);

        List<TripResponseDTO> result = tripService.getIncomingTrips("Israel", "Tel Aviv", "2025-06-01", "2025-06-10");

        assertEquals(1, result.size());
        assertEquals("Israel", result.get(0).getCountry());

        verify(tripRepository).searchTrips(eq(Country.ISRAEL), eq(City.TEL_AVIV), eq(LocalDate.parse("2025-06-01")), eq(LocalDate.parse("2025-06-10")));
        verify(tripMapper).toDto(trip);
    }
}
