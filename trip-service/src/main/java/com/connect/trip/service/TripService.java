package com.connect.trip.service;

import com.connect.trip.dto.request.TripRequestDTO;
import com.connect.trip.dto.response.TripResponseDTO;
import com.connect.trip.model.Trip;
import com.connect.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;

    public TripResponseDTO createTrip(TripRequestDTO request, UUID userId) {
        Trip trip = Trip.builder()
                .userId(userId)
                .country(request.getCountry())
                .city(request.getCity())
                .startDate(parseDate(request.getStartDate()))
                .endDate(parseDate(request.getEndDate()))
                .build();

        return mapToDTO(tripRepository.save(trip));
    }

    public List<TripResponseDTO> getTripsByUser(UUID userId) {
        return tripRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TripResponseDTO updateTrip(String id, TripRequestDTO request, UUID userId) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Trip not found"));

        if (!trip.getUserId().equals(userId)) {
            throw new SecurityException("You are not authorized to update this trip");
        }

        // Update fields
        trip = Trip.builder()
                .userId(userId)
                .country(request.getCountry())
                .city(request.getCity())
                .startDate(parseDate(request.getStartDate()))
                .endDate(parseDate(request.getEndDate()))
                .build();

        // Set the existing ID
        trip = new Trip(trip.getUserId(), trip.getCountry(), trip.getCity(), trip.getStartDate(), trip.getEndDate());
        tripRepository.deleteById(id); // Remove old trip
        trip.setId(id); // Reuse the same ID
        return mapToDTO(tripRepository.save(trip));
    }

    public void deleteTrip(String id, UUID userId) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Trip not found"));

        if (!trip.getUserId().equals(userId)) {
            throw new SecurityException("You are not authorized to delete this trip");
        }

        tripRepository.deleteById(id);
    }

    public Map<String, Object> getIncomingTrips(String country, String city, String from, String to) {
        LocalDate fromDate = parseDate(from);
        LocalDate toDate = parseDate(to);

        List<Trip> filtered;

        if (city != null && fromDate != null && toDate != null) {
            filtered = tripRepository.findByCountryIgnoreCaseAndCityIgnoreCaseAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                    country, city, fromDate, toDate
            );
        } else if (city != null) {
            filtered = tripRepository.findByCountryIgnoreCaseAndCityIgnoreCase(country, city);
        } else if (fromDate != null && toDate != null) {
            filtered = tripRepository.findByCountryIgnoreCaseAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                    country, fromDate, toDate
            );
        } else {
            filtered = tripRepository.findByCountryIgnoreCase(country);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", filtered.size());
        result.put("items", filtered.stream().map(this::mapToDTO).collect(Collectors.toList()));
        return result;
    }

    private TripResponseDTO mapToDTO(Trip trip) {
        return TripResponseDTO.builder()
                .id(trip.getId())
                .country(trip.getCountry())
                .city(trip.getCity())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .userId(trip.getUserId())
                .build();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        return LocalDate.parse(dateStr);
    }
}
