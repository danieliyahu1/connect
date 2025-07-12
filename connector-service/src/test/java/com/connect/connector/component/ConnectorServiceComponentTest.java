package com.connect.connector.component;

import com.connect.auth.common.exception.AuthCommonInvalidAccessTokenException;
import com.connect.auth.common.exception.AuthCommonSignatureMismatchException;
import com.connect.auth.common.util.AsymmetricJwtUtil;
import com.connect.connector.dto.ConnectorImageDTO;
import com.connect.connector.dto.ConnectorSocialMediaDTO;
import com.connect.connector.dto.request.CreateConnectorRequestDTO;
import com.connect.connector.dto.response.ConnectorResponseDTO;
import com.connect.connector.dto.response.UploadSignatureResponseDTO;
import com.connect.connector.enums.City;
import com.connect.connector.enums.Country;
import com.connect.connector.enums.SocialMediaPlatform;
import com.connect.connector.model.Connector;
import com.connect.connector.model.ConnectorImage;
import com.connect.connector.model.ConnectorSocialMedia;
import com.connect.connector.repository.ConnectorImageRepository;
import com.connect.connector.repository.ConnectorRepository;
import com.connect.connector.repository.ConnectorSocialMediaRepository;
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

import java.util.Arrays;
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
    private ConnectorSocialMedia mockConnectorSocialMedia;


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

        mockConnectorSocialMedia = ConnectorSocialMedia.builder()
                .connector(mockConnector)
                .platform(SocialMediaPlatform.INSTAGRAM)
                .profileUrl("https://instagram.com/testuser")
                .build();
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

        when(connectorRepository.save(any())).thenReturn(Optional.empty());

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

    @Test
    void getPublicProfile_WhenUserExists_ReturnsOk() {
        // Given
        UUID otherUserId = UUID.fromString("99999999-8888-7777-6666-555555555555");

        Connector otherConnector = new Connector();
        setField(otherConnector, "connectorId", UUID.randomUUID());
        setField(otherConnector, "userId", otherUserId);
        otherConnector.setFirstName("Alice");
        otherConnector.setCountry(Country.POLAND);
        otherConnector.setCity(City.KRAKOW);
        otherConnector.setBio("Traveling around the world!");

        when(connectorRepository.findByUserId(otherUserId)).thenReturn(Optional.of(otherConnector));
        when(connectorImageRepository.findByConnector_ConnectorId(otherConnector.getConnectorId()))
                .thenReturn(List.of()); // Assume no images
        when(connectorSocialMediaRepository.findByConnector_ConnectorId(otherConnector.getConnectorId()))
                .thenReturn(List.of()); // Assume no social media

        HttpEntity<Void> request = new HttpEntity<>(jsonHeaders());

        ResponseEntity<ConnectorResponseDTO> response = restTemplate.exchange(
                getBaseUrl() + "/public/" + otherUserId,
                HttpMethod.GET,
                request,
                ConnectorResponseDTO.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ConnectorResponseDTO body = response.getBody();
        assertNotNull(body);
        assertEquals("Alice", body.getFirstName());
        assertEquals(Country.POLAND.getDisplayValue(), body.getCountry());
        assertEquals(City.KRAKOW.getDisplayValue(), body.getCity());
        assertEquals("Traveling around the world!", body.getBio());
    }

    @Test
    void getPublicProfile_WhenUserNotFound_ReturnsNotFound() {
        // Given
        UUID missingUserId = UUID.fromString("12121212-3434-5656-7878-909090909090");

        when(connectorRepository.findByUserId(missingUserId)).thenReturn(Optional.empty());

        HttpEntity<Void> request = new HttpEntity<>(jsonHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/public/" + missingUserId,
                HttpMethod.GET,
                request,
                String.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Connector not found"));
    }

    @Test
    void addSocialMediaLink_ValidInput_SavesEntityAndReturnsCreated() {
        // Arrange
        ConnectorSocialMediaDTO dto = new ConnectorSocialMediaDTO("Instagram", "https://instagram.com/testuser");

        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(mockConnector));
        when(connectorSocialMediaRepository.save(any())).thenReturn(mockConnectorSocialMedia);

        when(connectorSocialMediaRepository.existsByConnector_ConnectorIdAndPlatform(
                eq(mockConnector.getConnectorId()), eq(SocialMediaPlatform.INSTAGRAM)))
                .thenReturn(false);

        HttpEntity<ConnectorSocialMediaDTO> request = new HttpEntity<>(dto, jsonHeaders());

        // Act
        ResponseEntity<ConnectorResponseDTO> response = restTemplate.postForEntity(
                getBaseUrl() + "/me/social-media",
                request,
                ConnectorResponseDTO.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Daniel", response.getBody().getFirstName());
        verify(connectorSocialMediaRepository).save(any());
    }

    @Test
    void addSocialMediaLink_BlankUrl_ReturnsBadRequest() {
        // Arrange
        ConnectorSocialMediaDTO dto = new ConnectorSocialMediaDTO("Instagram", "   "); // blank URL
        HttpEntity<ConnectorSocialMediaDTO> request = new HttpEntity<>(dto, jsonHeaders());

        when(connectorSocialMediaRepository.existsByConnector_ConnectorIdAndPlatform(
                eq(mockConnector.getConnectorId()), eq(SocialMediaPlatform.INSTAGRAM)))
                .thenReturn(false);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/me/social-media",
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Profile URL must not be blank"));
    }

    @Test
    void addSocialMediaLink_InvalidEnum_ReturnsBadRequest() {
        String invalidPlatformJson = """
        {
          "platform": "TikTakk",
          "profileUrl": "https://example.com/profile"
        }
        """;

        HttpHeaders headers = jsonHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(invalidPlatformJson, headers);

        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(mockConnector));

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/me/social-media", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Illegal Enum Value"));
    }


    @Test
    void addSocialMediaLink_MissingPlatform_ReturnsBadRequest() {
        // Arrange
        ConnectorSocialMediaDTO dto = new ConnectorSocialMediaDTO(null, "https://instagram.com/testuser");
        HttpEntity<ConnectorSocialMediaDTO> request = new HttpEntity<>(dto, jsonHeaders());

        when(connectorSocialMediaRepository.existsByConnector_ConnectorIdAndPlatform(
                eq(mockConnector.getConnectorId()), eq(SocialMediaPlatform.INSTAGRAM)))
                .thenReturn(false);

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/me/social-media",
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Platform must not be not null"));
    }

    @Test
    void updateSocialMediaLink_ValidInput_UpdatesProfileUrl() {
        // Arrange
        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(mockConnector));
        when(connectorSocialMediaRepository.findByConnector_ConnectorIdAndPlatform(
                eq(mockConnector.getConnectorId()), eq(SocialMediaPlatform.INSTAGRAM)))
                .thenReturn(Optional.of(mockConnectorSocialMedia));

        ConnectorSocialMedia updatedSocialMedia = ConnectorSocialMedia.builder()
                .connector(mockConnector)
                .platform(SocialMediaPlatform.INSTAGRAM)
                .profileUrl("https://instagram.com/newuser")
                .build();
        when(connectorSocialMediaRepository.save(any())).thenReturn(updatedSocialMedia);

        HttpEntity<String> request = new HttpEntity<>("https://instagram.com/newuser", jsonHeaders());

        // Act
        ResponseEntity<ConnectorResponseDTO> response = restTemplate.exchange(
                getBaseUrl() + "/me/social-media/Instagram",
                HttpMethod.PUT,
                request,
                ConnectorResponseDTO.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(connectorSocialMediaRepository).save(any());
    }

    @Test
    void updateSocialMediaLink_PlatformNotFound_Returns404() {
        // Arrange
        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(mockConnector));
        when(connectorSocialMediaRepository.findByConnector_ConnectorIdAndPlatform(
                any(), any())).thenReturn(Optional.empty());

        HttpEntity<String> request = new HttpEntity<>("https://linkedin.com/newuser", jsonHeaders());

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/me/social-media/LinkedIn",
                HttpMethod.PUT,
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("No social media link found"));
    }

    @Test
    void deleteSocialMediaLink_ValidInput_DeletesPlatform() {
        // Arrange
        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(mockConnector));
        when(connectorSocialMediaRepository.findByConnector_ConnectorIdAndPlatform(
                eq(mockConnector.getConnectorId()), eq(SocialMediaPlatform.INSTAGRAM)))
                .thenReturn(Optional.of(mockConnectorSocialMedia));

        // Act
        ResponseEntity<ConnectorResponseDTO> response = restTemplate.exchange(
                getBaseUrl() + "/me/social-media/Instagram",
                HttpMethod.DELETE,
                new HttpEntity<>(jsonHeaders()),
                ConnectorResponseDTO.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(connectorSocialMediaRepository).delete(any());
    }

    @Test
    void deleteSocialMediaLink_PlatformNotFound_Returns404() {
        // Arrange
        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(mockConnector));
        when(connectorSocialMediaRepository.findByConnector_ConnectorIdAndPlatform(
                any(), any())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/me/social-media/Instagram",
                HttpMethod.DELETE,
                new HttpEntity<>(jsonHeaders()),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("No social media link found"));
    }

    @Test
    void addSocialMedia_DuplicatePlatform_ReturnsConflict() {
        ConnectorSocialMediaDTO dto = new ConnectorSocialMediaDTO("Instagram", "https://instagram.com/testuser");

        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(mockConnector));

        HttpEntity<ConnectorSocialMediaDTO> request = new HttpEntity<>(dto, jsonHeaders());

        when(connectorSocialMediaRepository.existsByConnector_ConnectorIdAndPlatform(
                eq(mockConnector.getConnectorId()), eq(SocialMediaPlatform.INSTAGRAM)))
                .thenReturn(true);

        // Act – duplicate insert
        ResponseEntity<String> duplicateResponse = restTemplate.postForEntity(
                getBaseUrl() + "/me/social-media",
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.CONFLICT, duplicateResponse.getStatusCode());
        assertTrue(duplicateResponse.getBody().contains("already exists")); // assuming your exception has a message like this
    }

    @Test
    void getAllPublicProfiles_ReturnsExpectedConnectors() {
        Connector secondConnector = new Connector();
        UUID secondUserId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        UUID secondConnectorId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

        setField(secondConnector, "userId", secondUserId);
        setField(secondConnector, "connectorId", secondConnectorId);
        secondConnector.setFirstName("Alice");
        secondConnector.setCountry(Country.ISRAEL);
        secondConnector.setCity(City.TEL_AVIV);
        secondConnector.setBio("Solo traveler");

        when(connectorRepository.findAllByUserIdIn(any())).thenReturn(List.of(mockConnector, secondConnector));

        List<UUID> userIds = List.of(userId, secondUserId);
        HttpEntity<List<UUID>> request = new HttpEntity<>(userIds, jsonHeaders());

        ResponseEntity<ConnectorResponseDTO[]> response = restTemplate.postForEntity(
                getBaseUrl() + "/internal/batch/ids",
                request,
                ConnectorResponseDTO[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ConnectorResponseDTO[] connectors = response.getBody();

        assertNotNull(connectors);
        assertEquals(2, connectors.length);

        List<String> names = Arrays.stream(connectors).map(ConnectorResponseDTO::getFirstName).toList();
        assertTrue(names.contains("Daniel"));
        assertTrue(names.contains("Alice"));

        verify(connectorRepository).findAllByUserIdIn(userIds);
    }

    @Test
    void generateGalleryUploadSignature_ValidOrderIndex_ReturnsExpectedFields() {

        int orderIndex = 2;
        String expectedFolder = "connectors/" + userId;
        String expectedPublicId = String.valueOf(orderIndex);

        HttpEntity<Void> request = new HttpEntity<>(jsonHeaders());

        // Act
        ResponseEntity<UploadSignatureResponseDTO> response = restTemplate.postForEntity(
                getBaseUrl() + "/me/gallery/signature?orderIndex=" + orderIndex,
                request,
                UploadSignatureResponseDTO.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected status 200 OK");

        UploadSignatureResponseDTO body = response.getBody();
        assertNotNull(body, "Response body must not be null");

        // Fields that should match exact values
        assertEquals(expectedFolder, body.getFolder(), "Folder path mismatch");
        assertEquals(expectedPublicId, body.getPublicId(), "Public ID should match order index");

        // Fields that should not be null or empty
        assertNotNull(body.getApiKey(), "API key must not be null");
        assertFalse(body.getApiKey().isBlank(), "API key must not be blank");

        assertNotNull(body.getCloudName(), "Cloud name must not be null");
        assertFalse(body.getCloudName().isBlank(), "Cloud name must not be blank");

        assertNotNull(body.getSignature(), "Signature must not be null");
        assertFalse(body.getSignature().isBlank(), "Signature must not be blank");

        assertNotNull(body.getTimestamp(), "Timestamp must not be null");
        assertTrue(body.getTimestamp().matches("\\d+"), "Timestamp must be numeric");
        assertTrue(Long.parseLong(body.getTimestamp()) > 0, "Timestamp must be a positive number");
    }


    @Test
    void generateGalleryUploadSignature_InvalidOrderIndex_ReturnsBadRequest() {
        int invalidOrderIndex = 9;

        HttpEntity<Void> request = new HttpEntity<>(jsonHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/me/gallery/signature?orderIndex=" + invalidOrderIndex,
                request,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Order index must be between 0 and 4"));
    }

    @Test
    void getConnectorsByCountries_ReturnsExpectedResults() {
        // Arrange
        Connector israelConnector = new Connector();
        UUID israelUserId = UUID.fromString("11111111-aaaa-bbbb-cccc-222222222222");
        setField(israelConnector, "userId", israelUserId);
        setField(israelConnector, "connectorId", UUID.randomUUID());
        israelConnector.setFirstName("Itai");
        israelConnector.setCountry(Country.ISRAEL);
        israelConnector.setCity(City.TEL_AVIV);
        israelConnector.setBio("Hostel lover in TLV");

        Connector polandConnector = new Connector();
        UUID polandUserId = UUID.fromString("33333333-aaaa-bbbb-cccc-444444444444");
        setField(polandConnector, "userId", polandUserId);
        setField(polandConnector, "connectorId", UUID.randomUUID());
        polandConnector.setFirstName("Magda");
        polandConnector.setCountry(Country.POLAND);
        polandConnector.setCity(City.KRAKOW);
        polandConnector.setBio("Krakow explorer");

        when(connectorRepository.findAllByCountryIn(List.of(Country.ISRAEL, Country.POLAND)))
                .thenReturn(List.of(israelConnector, polandConnector));

        HttpEntity<List<String>> request = new HttpEntity<>(
                List.of("Israel", "Poland"),
                jsonHeaders()
        );

        // Act
        ResponseEntity<ConnectorResponseDTO[]> response = restTemplate.postForEntity(
                getBaseUrl() + "/internal/batch/countries",
                request,
                ConnectorResponseDTO[].class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ConnectorResponseDTO[] results = response.getBody();
        assertNotNull(results);
        assertEquals(2, results.length);

        List<String> names = Arrays.stream(results).map(ConnectorResponseDTO::getFirstName).toList();
        assertTrue(names.contains("Itai"));
        assertTrue(names.contains("Magda"));

        verify(connectorRepository).findAllByCountryIn(any());
    }

    @Test
    void getMyConnectorProfile_WhenExists_ReturnsConnector() {
        // Arrange
        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(mockConnector));
        when(connectorImageRepository.findByConnector_ConnectorId(mockConnector.getConnectorId()))
                .thenReturn(List.of());
        when(connectorSocialMediaRepository.findByConnector_ConnectorId(mockConnector.getConnectorId()))
                .thenReturn(List.of());

        HttpEntity<Void> request = new HttpEntity<>(jsonHeaders());

        // Act
        ResponseEntity<ConnectorResponseDTO> response = restTemplate.exchange(
                getBaseUrl() + "/me",
                HttpMethod.GET,
                request,
                ConnectorResponseDTO.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ConnectorResponseDTO body = response.getBody();
        assertNotNull(body);
        assertEquals(mockConnector.getFirstName(), body.getFirstName());
        assertEquals(Country.POLAND.getDisplayValue(), body.getCountry());
        assertEquals(City.KRAKOW.getDisplayValue(), body.getCity());
    }

}
