package com.connect.trip.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "trips")
@Getter
@NoArgsConstructor
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id = UUID.randomUUID().toString();

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull
    @Column(nullable = false)
    private String country;

    private String city;
    private LocalDate startDate;
    private LocalDate endDate;

    @Builder
    public Trip(UUID userId, String country, String city, LocalDate startDate, LocalDate endDate) {
        this.userId = userId;
        this.country = country;
        this.city = city;
        this.startDate = startDate;
        this.endDate = endDate;
    }

}