package com.connect.trip.controller;

import com.connect.auth.common.util.AsymmetricJwtUtil;
import com.connect.trip.configuration.TestSecurityConfig;
import com.connect.trip.dto.request.TripRequestDTO;
import com.connect.trip.dto.response.TripResponseDTO;
import com.connect.trip.service.TripService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TripController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AsymmetricJwtUtil jwtUtil;

    @MockitoBean
    private TripService tripService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String URI_PREFIX = "/trips";

    @Test
    void createTrip_shouldReturnCreatedTrip() throws Exception {
        UUID userId = UUID.randomUUID();
        TripRequestDTO request = new TripRequestDTO("Japan", "Tokyo", "2025-01-01", "2025-01-10");
        TripResponseDTO response = TripResponseDTO.builder()
                .userId(userId.toString())
                .country("Japan")
                .city("Tokyo")
                .startDate("2025-01-01")
                .endDate("2025-01-10")
                .build();

        when(tripService.createTrip(eq(request), eq(userId))).thenReturn(response);

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(post(URI_PREFIX)
                        .principal(auth)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country").value("Japan"))
                .andExpect(jsonPath("$.city").value("Tokyo"))
                .andExpect(jsonPath("$.userId").value(userId.toString()));

        verify(tripService).createTrip(eq(request), eq(userId));
    }

    @Test
    void getMyTrips_shouldReturnListOfTrips() throws Exception {
        UUID userId = UUID.randomUUID();
        TripResponseDTO trip = TripResponseDTO.builder()
                .userId(userId.toString())
                .country("Germany")
                .startDate("2025-06-01")
                .endDate("2025-06-15")
                .build();

        when(tripService.getTripsByUser(userId)).thenReturn(List.of(trip));

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(get(URI_PREFIX + "/me")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].country").value("Germany"))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()));

        verify(tripService).getTripsByUser(userId);
    }

    @Test
    void updateTrip_shouldReturnUpdatedTrip() throws Exception {
        UUID userId = UUID.randomUUID();
        String tripId = UUID.randomUUID().toString();
        TripRequestDTO request = new TripRequestDTO("France", "Paris", "2025-08-01", "2025-08-10");
        TripResponseDTO response = TripResponseDTO.builder()
                .userId(userId.toString())
                .country("France")
                .city("Paris")
                .startDate("2025-08-01")
                .endDate("2025-08-10")
                .build();

        when(tripService.updateTrip(eq(tripId), eq(request), eq(userId))).thenReturn(response);

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(put(URI_PREFIX + "/" + tripId)
                        .principal(auth)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country").value("France"))
                .andExpect(jsonPath("$.city").value("Paris"))
                .andExpect(jsonPath("$.userId").value(userId.toString()));

        verify(tripService).updateTrip(tripId, request, userId);
    }

    @Test
    void deleteTrip_shouldReturnNoContent() throws Exception {
        UUID userId = UUID.randomUUID();
        String tripId = UUID.randomUUID().toString();

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(delete(URI_PREFIX + "/" + tripId)
                        .principal(auth))
                .andExpect(status().isNoContent());

        verify(tripService).deleteTrip(tripId, userId);
    }

    @Test
    void getIncomingTrips_shouldReturnTrips() throws Exception {
        TripResponseDTO trip = TripResponseDTO.builder()
                .userId(UUID.randomUUID().toString())
                .country("Spain")
                .startDate("2025-07-01")
                .endDate("2025-07-10")
                .build();

        when(tripService.getIncomingTrips("Spain", "Barcelona", "2025-07-01", "2025-07-10"))
                .thenReturn(Map.of("results", List.of(trip)));

        mockMvc.perform(get(URI_PREFIX + "/incoming")
                        .param("country", "Spain")
                        .param("city", "Barcelona")
                        .param("from", "2025-07-01")
                        .param("to", "2025-07-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].country").value("Spain"))
                .andExpect(jsonPath("$.results[0].city").doesNotExist());

        verify(tripService).getIncomingTrips("Spain", "Barcelona", "2025-07-01", "2025-07-10");
    }
}
