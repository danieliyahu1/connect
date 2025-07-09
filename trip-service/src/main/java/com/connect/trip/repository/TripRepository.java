package com.connect.trip.repository;

import com.connect.trip.enums.City;
import com.connect.trip.enums.Country;
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
        SELECT COUNT(t) > 0 FROM Trip t
        WHERE t.userId = :userId
          AND t.country = :country
          AND ((:city IS NULL AND t.city IS NULL) OR t.city = :city)
          AND ((:startDate IS NULL AND t.startDate IS NULL) OR t.startDate = :startDate)
          AND ((:endDate IS NULL AND t.endDate IS NULL) OR t.endDate = :endDate)
    """)
    boolean existsTrip(
            @Param("userId") UUID userId,
            @Param("country") Country country,
            @Param("city") City city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
