package com.connect.trip.controller;

import com.connect.trip.dto.request.TripRequestDTO;
import com.connect.trip.dto.response.TripResponseDTO;
import com.connect.trip.exception.OverlapTripException;
import com.connect.trip.exception.IllegalEnumException;
import com.connect.trip.exception.TripNotFoundException;
import com.connect.trip.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping("/me")
    public ResponseEntity<TripResponseDTO> createTrip(
            @RequestBody @Valid TripRequestDTO request,
            Authentication authentication
    ) throws OverlapTripException, IllegalEnumException {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                tripService.createTrip(request, getUserIdFromAuth(authentication))
        );
    }

    @GetMapping("/me")
    public ResponseEntity<List<TripResponseDTO>> getMyTrips(Authentication authentication) {
        return ResponseEntity.ok(
                tripService.getTripsByUser(getUserIdFromAuth(authentication))
        );
    }

    @PutMapping("/me/{publicId}")
    public ResponseEntity<TripResponseDTO> updateTrip(
            @PathVariable String publicId,
            @RequestBody @Valid TripRequestDTO request,
            Authentication authentication
    ) throws TripNotFoundException, IllegalEnumException {
        return ResponseEntity.ok(
                tripService.updateTrip(publicId, request, getUserIdFromAuth(authentication))
        );
    }

    @DeleteMapping("/me/{publicId}")
    public ResponseEntity<TripResponseDTO> deleteTrip(
            @PathVariable String publicId,
            Authentication authentication
    ) throws TripNotFoundException {
        return ResponseEntity.ok(
                tripService.deleteTrip(publicId, getUserIdFromAuth(authentication))
        );
    }

    @GetMapping("/internal/incoming")
    public ResponseEntity<List<TripResponseDTO>> getIncomingTrips(
            @RequestParam String country,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) throws IllegalEnumException {
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