package com.connect.trip.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TripRequestDTO {
    @NotBlank
    private String country;
    private String city;
    private String startDate;           // optional ISO-8601 string (e.g., "2025-08-01")
    private String endDate;
}
