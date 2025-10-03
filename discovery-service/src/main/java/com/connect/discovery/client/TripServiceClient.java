package com.akatsuki.discovery.client;

import com.akatsuki.discovery.config.FeignClientConfig;
import com.akatsuki.discovery.dto.IncomingTripRequestDto;
import com.akatsuki.discovery.dto.TripResponseDTO;
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
