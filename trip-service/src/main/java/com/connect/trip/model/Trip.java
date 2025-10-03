package com.akatsuki.trip.model;

import com.akatsuki.trip.enums.City;
import com.akatsuki.trip.enums.Country;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "trips")
@Getter
@NoArgsConstructor
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID dbId;

    @Column(name = "public_id", unique = true, nullable = false)
    private UUID publicId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Setter
    private Country country;

    @Setter
    @Enumerated(EnumType.STRING)
    private City city;

    @Setter
    private LocalDate startDate;

    @Setter
    private LocalDate endDate;

    @Builder
    public Trip(UUID userId, Country country, City city, LocalDate startDate, LocalDate endDate) {
        this.userId = userId;
        this.country = country;
        this.city = city;
        this.startDate = startDate;
        this.endDate = endDate;
        this.publicId = UUID.randomUUID();
    }

}