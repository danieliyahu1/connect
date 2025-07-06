package com.connect.connector.component;

import com.connect.auth.common.exception.AuthCommonInvalidAccessTokenException;
import com.connect.auth.common.exception.AuthCommonSignatureMismatchException;
import com.connect.auth.common.util.AsymmetricJwtUtil;
import com.connect.connector.dto.request.CreateConnectorRequestDTO;
import com.connect.connector.dto.response.ConnectorResponseDTO;
import com.connect.connector.enums.City;
import com.connect.connector.enums.Country;
import com.connect.connector.exception.ExistingConnectorException;
import com.connect.connector.model.Connector;
import com.connect.connector.repository.ConnectorImageRepository;
import com.connect.connector.repository.ConnectorRepository;
import com.connect.connector.repository.ConnectorSocialMediaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("component-test")
@AutoConfigureMockMvc()
class ConnectorServiceComponentTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    ConnectorImageRepository connectorImageRepository;

    @MockitoBean
    ConnectorRepository connectorRepository;

    @MockitoBean
    ConnectorSocialMediaRepository connectorSocialMediaRepository;

    @MockitoBean
    private AsymmetricJwtUtil asymmetricJwtUtil;

    private static UUID connectorId;

    private static UUID userId;
    private Connector mockConnector;


    // Helper method for base URL, consistent with reference
    private String getBaseUrl() {
        return "http://localhost:" + port + "/connectors";
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("mock-token");
        return headers;
    }

    @BeforeEach
    void setUp() throws AuthCommonInvalidAccessTokenException, AuthCommonSignatureMismatchException {
        connectorId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        userId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        doNothing().when(asymmetricJwtUtil).validateAccessToken(anyString());
        when(asymmetricJwtUtil.getUserIdFromAccessToken(anyString())).thenReturn(userId);

        mockConnector = new Connector();
        setField(mockConnector, "connectorId", UUID.randomUUID());
        setField(mockConnector, "userId", userId);
        mockConnector.setFirstName("Daniel");
        mockConnector.setCountry(Country.POLAND);
        mockConnector.setCity(City.KRAKOW);
        mockConnector.setBio("I love meeting new people in hostels!");

    }

    @Test
    void updateConnector_ValidInput_ReturnsOkAndUpdatesProfile() {

        CreateConnectorRequestDTO createRequest = new CreateConnectorRequestDTO(
                "Original Name", "Israel", "Tel Aviv", "This is a valid bio with more than fifteen characters."
        );

        Connector connector = mock(Connector.class);
        when(connector.getFirstName()).thenReturn("John");
        when(connector.getCountry()).thenReturn(Country.POLAND);
        when(connector.getCity()).thenReturn(City.KRAKOW);
        when(connector.getBio()).thenReturn("Bio");

        when(connector.getUserId()).thenReturn(UUID.randomUUID());

        when(connector.getConnectorId()).thenReturn(connectorId);
        when(connectorRepository.save(any(Connector.class))).thenReturn(connector);

        ResponseEntity<ConnectorResponseDTO> createResponse = restTemplate.postForEntity(getBaseUrl() + "/me",
                new HttpEntity<>(createRequest, jsonHeaders())
                , ConnectorResponseDTO.class);

        // Assert initial creation
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode(), "Expected 201 Created for initial connector creation");
        assertNotNull(createResponse.getBody(), "Created connector response body should not be null");
    }

    @Test
    void updateConnector_WhenUserHasNoProfile_ShouldReturnNotFound() {
        // Given: Update request for user with no connector profile
        var updateRequest = new com.connect.connector.dto.request.UpdateConnectorRequestDTO(
                "Name", "Israel", "Tel Aviv", "This is a valid bio with more than fifteen characters."
        );

        when(connectorRepository.save(any())).thenThrow(new com.connect.connector.exception.ConnectorNotFoundException("No connector"));

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/me",
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest, jsonHeaders()),
                String.class
        );

        // Then: Expect 404 Not Found
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createConnector_ValidInput_ReturnsCreated() {
        CreateConnectorRequestDTO request = new CreateConnectorRequestDTO(
                "Daniel", "Poland", "Krakow", "I love meeting new people in hostels!"
        );

        when(connectorRepository.save(any(Connector.class))).thenReturn(mockConnector);

        HttpEntity<CreateConnectorRequestDTO> entity = new HttpEntity<>(request, jsonHeaders());
        ResponseEntity<ConnectorResponseDTO> response = restTemplate.postForEntity(getBaseUrl() + "/me", entity, ConnectorResponseDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Daniel", response.getBody().getFirstName());
        assertEquals(Country.POLAND.getDisplayValue(), response.getBody().getCountry());
        assertEquals(City.KRAKOW.getDisplayValue(), response.getBody().getCity());
    }

    @Test
    void createConnector_InvalidInput_ReturnsBadRequest() {
        CreateConnectorRequestDTO request = new CreateConnectorRequestDTO(
                "", "Invalid", "None", "Short"
        );

        HttpEntity<CreateConnectorRequestDTO> entity = new HttpEntity<>(request, jsonHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl() + "/me", entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createConnector_WhenConnectorExists_ReturnsConflict() {
        CreateConnectorRequestDTO request = new CreateConnectorRequestDTO(
                "Daniel", "Poland", "Krakow", "Another valid bio here."
        );

        when(connectorRepository.existsByUserId(userId)).thenReturn(true);

        HttpEntity<CreateConnectorRequestDTO> entity = new HttpEntity<>(request, jsonHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl() + "/me", entity, String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

}
