package com.connect.trip.service;

import com.connect.trip.dto.request.TripRequestDTO;
import com.connect.trip.dto.response.TripResponseDTO;
import com.connect.trip.enums.City;
import com.connect.trip.enums.Country;
import com.connect.trip.enums.util.EnumUtil;
import com.connect.trip.mapper.TripMapper;
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
    private final TripMapper tripMapper;

    public TripResponseDTO createTrip(TripRequestDTO request, UUID userId) {
        Trip trip = Trip.builder()
                .userId(userId)
                .country(EnumUtil.fromDisplayName(Country.class, request.getCountry()))
                .city(EnumUtil.fromDisplayName(City.class, request.getCity()))
                .startDate(parseDate(request.getStartDate()))
                .endDate(parseDate(request.getEndDate()))
                .build();

        return tripMapper.toDto(tripRepository.save(trip));
    }

    public List<TripResponseDTO> getTripsByUser(UUID userId) {
        return tripRepository.findByUserId(userId)
                .stream()
                .map(tripMapper::toDto)
                .collect(Collectors.toList());
    }

    public TripResponseDTO updateTrip(String id, TripRequestDTO request, UUID userId) {
        Trip trip = tripRepository.findByIdAndUserId(UUID.fromString(id), userId)
                .orElseThrow(() -> new NoSuchElementException("Trip not found or unauthorized"));

        // Update only the relevant fields
        if (request.getCountry() != null) {
            trip.setCountry(EnumUtil.fromDisplayName(Country.class, request.getCountry()));
        }
        if (request.getCity() != null) {
            trip.setCity(EnumUtil.fromDisplayName(City.class, request.getCity()));
        }
        if (request.getStartDate() != null) {
            trip.setStartDate(parseDate(request.getStartDate()));
        }
        if (request.getEndDate() != null) {
            trip.setEndDate(parseDate(request.getEndDate()));
        }

        Trip updatedTrip = tripRepository.save(trip);
        return tripMapper.toDto(updatedTrip);
    }

    public TripResponseDTO deleteTrip(String id, UUID userId) {
        Trip trip = tripRepository.findByIdAndUserId(UUID.fromString(id), userId)
                .orElseThrow(() -> new NoSuchElementException("Trip not found"));

        tripRepository.deleteById(id);
        return tripMapper.toDto(trip);
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
        result.put("items", filtered.stream().map(tripMapper::toDto).collect(Collectors.toList()));
        return result;
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        return LocalDate.parse(dateStr);
    }
}
