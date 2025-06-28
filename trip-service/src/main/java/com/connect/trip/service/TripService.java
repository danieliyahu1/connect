package com.connect.trip.service;

import com.connect.trip.dto.request.TripRequestDTO;
import com.connect.trip.dto.response.TripResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TripService {
    public TripResponseDTO createTrip(TripRequestDTO request, UUID userIdFromAuth) {
        return null;
    }

    public List<TripResponseDTO> getTripsByUser(UUID userIdFromAuth) {
        return null;
    }

    public TripResponseDTO updateTrip(String id, TripRequestDTO request, UUID userIdFromAuth) {
        return null;
    }

    public void deleteTrip(String id, UUID userIdFromAuth) {
    }

    public Map<String, Object> getIncomingTrips(String country, String city, String from, String to) {
        return null;
    }
}
