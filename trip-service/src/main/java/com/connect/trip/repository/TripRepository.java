package com.akatsuki.trip.repository;

import com.akatsuki.trip.enums.City;
import com.akatsuki.trip.enums.Country;
import com.akatsuki.trip.model.Trip;
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
    Optional<Trip> findByPublicIdAndUserId(UUID publicId, UUID userId);

    @Query("""
        SELECT t FROM Trip t
        WHERE t.country = :country
          AND (:city IS NULL OR t.city = :city)
          AND (:from IS NULL OR t.startDate >= :from)
          AND (:to IS NULL OR t.endDate <= :to)
    """)
    List<Trip> searchTrips(
            @Param("country") Country country,
            @Param("city") City city,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
    SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
    FROM Trip t
    WHERE t.userId = :userId
      AND t.startDate <= :newEndDate
      AND t.endDate >= :newStartDate
""")
    boolean existsOverlappingTripDates(
            @Param("userId") UUID userId,
            @Param("newStartDate") LocalDate newStartDate,
            @Param("newEndDate") LocalDate newEndDate
    );

}
