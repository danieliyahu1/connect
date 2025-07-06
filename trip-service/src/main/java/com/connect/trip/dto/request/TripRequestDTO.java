package com.connect.trip.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class TripRequestDTO {
    @NotBlank
    private String country;
    @NotBlank
    private String city;
    @NotBlank
    private String startDate;
    @NotBlank
    private String endDate;
}
