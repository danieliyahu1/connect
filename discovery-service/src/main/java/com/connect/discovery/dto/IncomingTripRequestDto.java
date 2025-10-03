package com.akatsuki.discovery.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class IncomingTripRequestDto {
    @NonNull
    private String country;
    private String city;
    private String startDate;
    private String endDate;
}
