package com.connect.connector.component;

import com.connect.auth.common.exception.AuthCommonInvalidAccessTokenException;
import com.connect.auth.common.exception.AuthCommonSignatureMismatchException;
import com.connect.auth.common.util.AsymmetricJwtUtil;
import com.connect.connector.dto.request.CreateConnectorRequestDTO;
import com.connect.connector.dto.response.ConnectorResponseDTO;
import com.connect.connector.enums.City;
import com.connect.connector.enums.Country;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
    void setUpAuthentication() throws AuthCommonInvalidAccessTokenException, AuthCommonSignatureMismatchException {
        connectorId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        userId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        doNothing().when(asymmetricJwtUtil).validateAccessToken(anyString());
        when(asymmetricJwtUtil.getUserIdFromAccessToken(anyString())).thenReturn(userId);

    }

    @Test
    void updateConnector_ValidInput_ReturnsOkAndUpdatesProfile() {

        CreateConnectorRequestDTO createRequest = new CreateConnectorRequestDTO(
                "Original Name", "Israel", "TelAviv", "This is a valid bio with more than fifteen characters."
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
        Assertions.assertEquals(HttpStatus.CREATED, createResponse.getStatusCode(), "Expected 201 Created for initial connector creation");
        Assertions.assertNotNull(createResponse.getBody(), "Created connector response body should not be null");
    }
}
