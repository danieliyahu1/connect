package com.connect.trip.service;

import com.connect.trip.dto.request.TripRequestDTO;
import com.connect.trip.dto.response.TripResponseDTO;
import com.connect.trip.enums.City;
import com.connect.trip.enums.Country;
import com.connect.trip.enums.util.EnumUtil;
import com.connect.trip.exception.OverlapTripException;
import com.connect.trip.exception.IllegalEnumException;
import com.connect.trip.exception.TripNotFoundException;
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

    public TripResponseDTO createTrip(TripRequestDTO request, UUID userId) throws OverlapTripException, IllegalEnumException {
        checkTripExists(request, userId);
        Trip trip = Trip.builder()
                .userId(userId)
                .country(EnumUtil.getEnumFromDisplayName(Country.class, request.getCountry()))
                .city(EnumUtil.getEnumFromDisplayName(City.class, request.getCity()))
                .startDate(parseDate(request.getStartDate()))
                .endDate(parseDate(request.getEndDate()))
                .build();
        tripRepository.save(trip);
        return tripMapper.toDto(tripRepository.save(trip));
    }

    public List<TripResponseDTO> getTripsByUser(UUID userId) {
        return tripRepository.findByUserId(userId)
                .stream()
                .map(tripMapper::toDto)
                .collect(Collectors.toList());
    }

    public TripResponseDTO updateTrip(String publicId, TripRequestDTO request, UUID userId) throws TripNotFoundException, IllegalEnumException {
        Trip trip = tripRepository.findByPublicIdAndUserId(UUID.fromString(publicId), userId)
                .orElseThrow(() -> new TripNotFoundException("Trip not found or unauthorized"));

        // Update only the relevant fields
        if (request.getCountry() != null) {
            trip.setCountry(EnumUtil.getEnumFromDisplayName(Country.class, request.getCountry()));
        }
        if (request.getCity() != null) {
            trip.setCity(EnumUtil.getEnumFromDisplayName(City.class, request.getCity()));
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

    public TripResponseDTO deleteTrip(String publicId, UUID userId) throws TripNotFoundException {
        Trip trip = tripRepository.findByPublicIdAndUserId(UUID.fromString(publicId), userId)
                .orElseThrow(() -> new TripNotFoundException("Trip not found"));

        tripRepository.deleteById(trip.getDbId());
        return tripMapper.toDto(trip);
    }

    public List<TripResponseDTO> getIncomingTrips(String country, String city, String from, String to) throws IllegalEnumException {
        return tripRepository.searchTrips(
                        getCountryEnumOrNull(country),
                        getCityEnumOrNull(city),
                        parseDate(from),
                        parseDate(to)
                ).stream()
                .map(tripMapper::toDto)
                .collect(Collectors.toList());
    }

    private void checkTripExists(TripRequestDTO request, UUID userId) throws OverlapTripException, IllegalEnumException {
        LocalDate startDate = parseDate(request.getStartDate());
        LocalDate endDate = parseDate(request.getEndDate());

        if (tripRepository.existsOverlappingTripDates(userId, startDate, endDate)) {
            throw new OverlapTripException("Trip dates overlap with an existing trip");
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        return LocalDate.parse(dateStr);
    }

    private Country getCountryEnumOrNull(String countryDisplayName) throws IllegalEnumException {
        if (countryDisplayName != null && !countryDisplayName.trim().isEmpty()) {
            return EnumUtil.getEnumFromDisplayName(Country.class, countryDisplayName);
        }
        return null;
    }

    private City getCityEnumOrNull(String cityDisplayName) throws IllegalEnumException {
        if (cityDisplayName != null && !cityDisplayName.trim().isEmpty()) {
            return EnumUtil.getEnumFromDisplayName(City.class, cityDisplayName);
        }
        return null;
    }
}
