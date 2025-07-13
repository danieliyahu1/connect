package com.connect.discovery.client;

import com.connect.discovery.config.FeignClientConfig;
import com.connect.discovery.dto.IncomingTripRequestDto;
import com.connect.discovery.dto.TripResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(
        name = "tripServiceClient",
        url = "${trip.service.base.url}",
        configuration = FeignClientConfig.class
)
public interface TripServiceClient {

    @GetMapping("/internal/incoming")
    List<TripResponseDTO> getIncomingTrips(@SpringQueryMap IncomingTripRequestDto query);


    @GetMapping("/me")
    List<TripResponseDTO> getMyTrips();

}
