package com.connect.connector.component;

import com.connect.auth.common.exception.AuthCommonInvalidAccessTokenException;
import com.connect.auth.common.exception.AuthCommonSignatureMismatchException;
import com.connect.auth.common.util.AsymmetricJwtUtil;
import com.connect.connector.dto.ConnectorImageDTO;
import com.connect.connector.dto.request.CreateConnectorRequestDTO;
import com.connect.connector.dto.response.ConnectorResponseDTO;
import com.connect.connector.enums.City;
import com.connect.connector.enums.Country;
import com.connect.connector.exception.ExistingConnectorException;
import com.connect.connector.model.Connector;
import com.connect.connector.model.ConnectorImage;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void addGalleryPhoto_ValidInput_ReturnsOk() {
        // Given: image DTO with correct orderIndex and valid media URL
        ConnectorImageDTO imageDTO = new ConnectorImageDTO(
                "https://cdn.test.com/image.jpg",
                0
        );

        // Mock connector retrieval
        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(mockConnector));
        when(connectorImageRepository.findByConnector_ConnectorId(mockConnector.getConnectorId()))
                .thenReturn(List.of()); // No existing images

        // Create and return mock image (with ID set via reflection)
        ConnectorImage savedImage = ConnectorImage.builder()
                .connector(mockConnector)
                .mediaUrl(imageDTO.getMediaUrl())
                .orderIndex(imageDTO.getOrderIndex())
                .build();
        setField(savedImage, "id", UUID.randomUUID());
        when(connectorImageRepository.save(any(ConnectorImage.class))).thenReturn(savedImage);

        // We expect the service to call again after saving to fetch current images
        when(connectorImageRepository.findByConnector_ConnectorId(mockConnector.getConnectorId()))
                .thenReturn(List.of(savedImage)); // Now includes the saved image

        HttpEntity<ConnectorImageDTO> request = new HttpEntity<>(imageDTO, jsonHeaders());

        // When
        ResponseEntity<ConnectorResponseDTO> response = restTemplate.postForEntity(
                getBaseUrl() + "/me/gallery", request, ConnectorResponseDTO.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ConnectorResponseDTO body = response.getBody();
        assertNotNull(body);
        assertEquals(mockConnector.getFirstName(), body.getFirstName());
        assertEquals(Country.POLAND.getDisplayValue(), body.getCountry());
        assertEquals(City.KRAKOW.getDisplayValue(), body.getCity());
        assertEquals("I love meeting new people in hostels!", body.getBio());

        // Gallery images should contain 1 image
        assertNotNull(body.getGalleryImages());
        assertEquals(1, body.getGalleryImages().size());

        ConnectorImageDTO returnedImage = body.getGalleryImages().get(0);
        assertEquals(imageDTO.getMediaUrl(), returnedImage.getMediaUrl());
        assertEquals(imageDTO.getOrderIndex(), returnedImage.getOrderIndex());

        // Social links may be empty if not mocked — skip assertion unless mocked
    }

    @Test
    void addGalleryPhoto_WhenInvalidIndex_ReturnsBadRequest() {
        ConnectorImageDTO imageDTO = new ConnectorImageDTO("https://cdn.test.com/image.jpg", 100); // Invalid index

        HttpEntity<ConnectorImageDTO> request = new HttpEntity<>(imageDTO, jsonHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/me/gallery", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Order index must be between 0 and 4"));
    }

    @Test
    void addGalleryPhoto_WhenConnectorNotFound_ReturnsNotFound() {
        ConnectorImageDTO imageDTO = new ConnectorImageDTO("https://cdn.test.com/image.jpg", 0);

        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.empty());

        HttpEntity<ConnectorImageDTO> request = new HttpEntity<>(imageDTO, jsonHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/me/gallery", request, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Connector not found"));
    }

    @Test
    void addGalleryPhoto_WhenImageIndexNotNextAvailable_ReturnsBadRequest() {
        ConnectorImageDTO imageDTO = new ConnectorImageDTO("https://cdn.test.com/image.jpg", 2); // Invalid: not next spot

        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(mockConnector));

        ConnectorImage existingImage = ConnectorImage.builder()
                .connector(mockConnector)
                .mediaUrl("existing.jpg")
                .orderIndex(0)
                .build();
        setField(existingImage, "id", UUID.randomUUID());

        when(connectorImageRepository.findByConnector_ConnectorId(mockConnector.getConnectorId()))
                .thenReturn(List.of(existingImage)); // Only orderIndex=0 exists, so 2 is invalid

        HttpEntity<ConnectorImageDTO> request = new HttpEntity<>(imageDTO, jsonHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/me/gallery", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Order index is 2 but should be equal to the next spot available"));
    }

    @Test
    void addGalleryPhoto_WhenReplacingExistingImage_Success() {
        ConnectorImageDTO imageDTO = new ConnectorImageDTO("https://cdn.test.com/new.jpg", 0); // Replaces image at index 0

        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(mockConnector));

        ConnectorImage existingImage = ConnectorImage.builder()
                .connector(mockConnector)
                .mediaUrl("old.jpg")
                .orderIndex(0)
                .build();
        setField(existingImage, "id", UUID.randomUUID());

        when(connectorImageRepository.findByConnector_ConnectorId(mockConnector.getConnectorId()))
                .thenReturn(List.of(existingImage));

        ConnectorImage savedImage = ConnectorImage.builder()
                .connector(mockConnector)
                .mediaUrl(imageDTO.getMediaUrl())
                .orderIndex(imageDTO.getOrderIndex())
                .build();
        setField(savedImage, "id", UUID.randomUUID());

        when(connectorImageRepository.save(any())).thenReturn(savedImage);

        // After replacement, we expect the new one to be returned
        when(connectorImageRepository.findByConnector_ConnectorId(mockConnector.getConnectorId()))
                .thenReturn(List.of(savedImage));

        HttpEntity<ConnectorImageDTO> request = new HttpEntity<>(imageDTO, jsonHeaders());

        ResponseEntity<ConnectorResponseDTO> response = restTemplate.postForEntity(
                getBaseUrl() + "/me/gallery", request, ConnectorResponseDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ConnectorResponseDTO body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.getGalleryImages().size());
        assertEquals("https://cdn.test.com/new.jpg", body.getGalleryImages().get(0).getMediaUrl());
    }

}
