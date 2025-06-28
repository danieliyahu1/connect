package com.connect.trip.repository;

import com.connect.trip.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, String> {

    List<Trip> findByUserId(UUID userId);
    Optional<Trip> findByIdAndUserId(UUID id, UUID userId);

    // Incoming trip queries
    List<Trip> findByCountryIgnoreCase(String country);
    List<Trip> findByCountryIgnoreCaseAndCityIgnoreCase(String country, String city);
    List<Trip> findByCountryIgnoreCaseAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            String country, LocalDate start, LocalDate end
    );

    List<Trip> findByCountryIgnoreCaseAndCityIgnoreCaseAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            String country, String city, LocalDate start, LocalDate end
    );
}
