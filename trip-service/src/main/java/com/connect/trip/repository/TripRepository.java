package com.connect.trip.repository;

import com.connect.trip.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {

    List<Trip> findByUserId(UUID userId);
    Optional<Trip> findByIdAndUserId(UUID id, UUID userId);

    @Query("""
    SELECT t FROM Trip t
    WHERE LOWER(t.country) = LOWER(:country)
    AND (:city IS NULL OR LOWER(t.city) = LOWER(:city))
    AND (:from IS NULL OR t.startDate >= :from)
    AND (:to IS NULL OR t.endDate <= :to)
""")
    List<Trip> searchTrips(
            @Param("country") String country,
            @Param("city") String city,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

}
