package com.connect.trip.controller;

import com.connect.trip.dto.request.TripRequestDTO;
import com.connect.trip.dto.response.TripResponseDTO;
import com.connect.trip.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List; // Added missing import
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<TripResponseDTO> createTrip(@RequestBody TripRequestDTO request, Authentication authentication) {
        return ResponseEntity.ok(
                tripService.createTrip(request, getUserIdFromAuth(authentication))
        );
    }

    @GetMapping("/me")
    public ResponseEntity<List<TripResponseDTO>> getMyTrips(Authentication authentication) {
        return ResponseEntity.ok(
                tripService.getTripsByUser(getUserIdFromAuth(authentication))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<TripResponseDTO> updateTrip(@PathVariable String id,
                                                      @RequestBody TripRequestDTO request,
                                                      Authentication authentication) {
        return ResponseEntity.ok(
                tripService.updateTrip(id, request, getUserIdFromAuth(authentication))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable String id, Authentication authentication) {
        tripService.deleteTrip(id, getUserIdFromAuth(authentication));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/incoming")
    public ResponseEntity<Map<String, Object>> getIncomingTrips(@RequestParam String country,
                                                                @RequestParam(required = false) String city,
                                                                @RequestParam(required = false) String from,
                                                                @RequestParam(required = false) String to) {
        return ResponseEntity.ok(
                tripService.getIncomingTrips(country, city, from, to)
        );
    }

    private UUID getUserIdFromAuth(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("Authentication not set in SecurityContext");
        }
        return UUID.fromString(authentication.getName());
    }
}