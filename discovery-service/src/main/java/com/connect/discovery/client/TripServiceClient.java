package com.connect.discovery.client;

import com.connect.discovery.dto.TripResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "tripServiceClient",
        url = "http://connect-trip-service:4002/api/trips" // base URL from your gateway config
)
public interface TripServiceClient {

    @GetMapping("/internal/incoming")
    List<TripResponseDTO> getIncomingTrips(
            @RequestParam("country") String country,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to
    );

    @GetMapping("/me")
    List<TripResponseDTO> getMyTrips();

}
