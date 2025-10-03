package com.akatsuki.trip.controller;

import com.akatsuki.trip.dto.request.TripRequestDTO;
import com.akatsuki.trip.dto.response.TripResponseDTO;
import com.akatsuki.trip.exception.InvalidDateException;
import com.akatsuki.trip.exception.OverlapTripException;
import com.akatsuki.trip.exception.IllegalEnumException;
import com.akatsuki.trip.exception.TripNotFoundException;
import com.akatsuki.trip.service.TripService;
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
    ) throws OverlapTripException, IllegalEnumException, InvalidDateException {
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
    ) throws TripNotFoundException, IllegalEnumException, InvalidDateException {
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
    ) throws IllegalEnumException, InvalidDateException {
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