package com.connect.trip.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public class TripResponseDTO {
    @NonNull
    private String userId;
    @NonNull
    private String country;
    private String city;
    private String startDate;
    private String endDate;
}
