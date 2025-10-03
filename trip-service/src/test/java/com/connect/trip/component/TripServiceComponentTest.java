package com.akatsuki.trip.component;

import com.akatsuki.auth.common.exception.AuthCommonInvalidAccessTokenException;
import com.akatsuki.auth.common.exception.AuthCommonSignatureMismatchException;
import com.akatsuki.auth.common.util.AsymmetricJwtUtil;
import com.akatsuki.trip.dto.response.TripResponseDTO;
import com.akatsuki.trip.enums.City;
import com.akatsuki.trip.enums.Country;
import com.akatsuki.trip.model.Trip;
import com.akatsuki.trip.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("component-test")
@AutoConfigureMockMvc()
class TripServiceComponentTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private TripRepository tripRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private AsymmetricJwtUtil asymmetricJwtUtil;

    private UUID userId;
    private UUID tripId;
    private UUID publicTripId;

    @BeforeEach
    void setUpAuthentication() throws AuthCommonInvalidAccessTokenException, AuthCommonSignatureMismatchException, NoSuchFieldException, IllegalAccessException {
        userId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        tripId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        doNothing().when(asymmetricJwtUtil).validateAccessToken(anyString());
        when(asymmetricJwtUtil.getUserIdFromAccessToken(anyString())).thenReturn(userId);

        Trip mockTrip = Trip.builder()
                .userId(userId)
                .country(Country.ISRAEL)
                .city(City.TEL_AVIV)
                .startDate(LocalDate.parse("2025-06-01"))
                .endDate(LocalDate.parse("2025-06-10"))
                .build();

        // Use reflection or setId manually (if you add a setter only for test, see note below)
        Field idField = Trip.class.getDeclaredField("dbId");
        idField.setAccessible(true);
        idField.set(mockTrip, tripId);

        publicTripId = mockTrip.getPublicId();

        when(tripRepository.save(any(Trip.class))).thenReturn(mockTrip);
        when(tripRepository.findByUserId(userId)).thenReturn(List.of(mockTrip));
        when(tripRepository.findByPublicIdAndUserId(eq(mockTrip.getPublicId()), eq(userId))).thenReturn(Optional.of(mockTrip));
        when(tripRepository.searchTrips(any(Country.class), any(City.class), any(), any())).thenReturn(List.of(mockTrip));
    }

    private String baseUrl() {
        return "http://localhost:" + port + "/trips";
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("mock-token");
        return headers;
    }

    @Test
    void createTrip_validRequest_returnsCreatedTrip() {
        String jsonRequest = """
            {
                "country": "Israel",
                "city": "Tel Aviv",
                "startDate": "2025-06-01",
                "endDate": "2025-06-10"
            }
            """;

        HttpEntity<String> entity = new HttpEntity<>(jsonRequest, jsonHeaders());

        ResponseEntity<TripResponseDTO> response = restTemplate.postForEntity(baseUrl() + "/me", entity, TripResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCountry()).isEqualTo("Israel");
        assertThat(response.getBody().getCity()).isEqualTo("Tel Aviv");
        assertThat(response.getBody().getStartDate()).isEqualTo("2025-06-01");
        assertThat(response.getBody().getEndDate()).isEqualTo("2025-06-10");
        assertThat(response.getBody().getUserId()).isNotNull();
    }

    @Test
    void getMyTrips_returnsListOfTrips() {
        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl() + "/me",
                HttpMethod.GET,
                new HttpEntity<>(jsonHeaders()),
                List.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(List.class);
        assertThat(response.getBody().size()).isGreaterThan(0);
    }

    @Test
    void getIncomingTrips_withCountry_returnsTripsList() {
        String url = baseUrl() + "/internal/incoming?country=Israel";

        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(jsonHeaders()),
                List.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(List.class);
    }

    @Test
    void updateTrip_validRequest_returnsUpdatedTrip() {
        String jsonRequest = """
        {
            "country": "Israel",
            "city": "JeruSalEm",
            "startDate": "2025-06-05",
            "endDate": "2025-06-15"
        }
        """;

        HttpEntity<String> entity = new HttpEntity<>(jsonRequest, jsonHeaders());

        ResponseEntity<TripResponseDTO> response = restTemplate.exchange(
                baseUrl() + "/me/" + publicTripId,
                HttpMethod.PUT,
                entity,
                TripResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCity()).isEqualTo("Jerusalem");
        assertThat(response.getBody().getStartDate()).isEqualTo("2025-06-05");
        assertThat(response.getBody().getEndDate()).isEqualTo("2025-06-15");
    }

    @Test
    void deleteTrip_validId_returnsDeletedTrip() {
        ResponseEntity<TripResponseDTO> response = restTemplate.exchange(
                baseUrl() + "/me/" + publicTripId,
                HttpMethod.DELETE,
                new HttpEntity<>(jsonHeaders()),
                TripResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserId()).isEqualTo(userId.toString());
    }

    @Test
    void getIncomingTrips_withAllFilters_returnsFilteredTrips() {
        String url = baseUrl() + "/internal/incoming?country=Israel&city=Tel Aviv";

        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(jsonHeaders()),
                List.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(List.class);
        assertThat(response.getBody().size()).isGreaterThan(0);
    }

    @Test
    void createTrip_missingCity_returnsBadRequest() {
        String badJson = """
        {
            "country": "Israel",
            "startDate": "2025-06-01",
            "endDate": "2025-06-10"
        }
        """;

        HttpEntity<String> entity = new HttpEntity<>(badJson, jsonHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl() + "/me", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST); // depends on validation setup
    }
}
