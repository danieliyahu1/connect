package com.connect.discovery.service;

import com.connect.discovery.client.TripServiceClient;
import com.connect.discovery.dto.IncomingTripRequestDto;
import com.connect.discovery.dto.TripResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripServiceClient tripServiceClient;

    public List<TripResponseDTO> getMyTrips() {
        return tripServiceClient.getMyTrips();
    }

    public List<TripResponseDTO> fetchTravelersVisiting(String country) {
        return tripServiceClient.getIncomingTrips(
                IncomingTripRequestDto.builder()
                        .country(country)
                        .build()
        );
    }
}
