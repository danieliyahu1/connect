package com.connect.connector.service;

import com.connect.connector.dto.ConnectorImageDTO;
import com.connect.connector.dto.response.UploadSignatureResponseDTO;
import com.connect.connector.dto.response.ConnectorResponseDTO;
import com.connect.connector.dto.request.CreateConnectorRequestDTO;
import com.connect.connector.dto.request.UpdateConnectorRequestDTO;
import com.connect.connector.enums.City;
import com.connect.connector.enums.Country;
import com.connect.connector.exception.*;
import com.connect.connector.model.Connector;
import com.connect.connector.repository.ConnectorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectorServiceTest {

    @Mock private ConnectorRepository connectorRepository;
    @Mock private ConnectorSocialMediaService connectorSocialMediaService;
    @Mock private ConnectorImageService connectorImageService;
    @Mock private MediaStorageService mediaService;

    @InjectMocks private ConnectorService connectorService;

    private UUID userId;
    private Connector connector;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        connector = Connector.builder()
                .userId(userId)
                .firstName("John")
                .country(Country.POLAND)
                .city(City.KRAKOW)
                .bio("Bio")
                .build();
    }

    @ParameterizedTest
    @CsvSource({
            "Jane,POLAND,KRAKOW,New bio",
            "Mike,ISRAEL,TEL_AVIV,Another bio"
    })
    void updateMyProfile_shouldUpdate_whenFieldsAreNotNull(String firstName, String country, String city, String bio) throws InvalidProfileUrlException {
        UpdateConnectorRequestDTO dto = new UpdateConnectorRequestDTO(firstName, country, city, bio);

        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(connector));
        when(connectorRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(connectorImageService.findByConnector_ConnectorId(connector.getConnectorId())).thenReturn(Collections.emptyList());
        when(connectorSocialMediaService.findByConnector_ConnectorId(connector.getConnectorId())).thenReturn(Collections.emptyList());

        ConnectorResponseDTO response = connectorService.updateMyProfile(userId, dto);

        assertEquals(firstName, response.getFirstName());
        assertEquals(Country.valueOf(country).getDisplayValue(), response.getCountry());
        assertEquals(City.valueOf(city).getDisplayValue(), response.getCity());
        assertEquals(bio, response.getBio());

        verify(connectorRepository).save(any(Connector.class));
    }

    @Test
    void updateMyProfile_shouldThrow_whenConnectorNotFound() {
        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.empty());

        UpdateConnectorRequestDTO dto = new UpdateConnectorRequestDTO(null, null, null, null);

        ConnectorNotFoundException exception = assertThrows(ConnectorNotFoundException.class,
                () -> connectorService.updateMyProfile(userId, dto));
        assertTrue(exception.getMessage().contains("Connector not found"));
    }

    @ParameterizedTest
    @CsvSource({
            "John,POLAND,KRAKOW,Bio",
            "Dina,ISRAEL,JERUSALEM,Hi there"
    })
    void createMyProfile_shouldCreate_whenNoExistingConnector(String firstName, String country, String city, String bio)
            throws ExistingConnectorException {

        CreateConnectorRequestDTO dto = new CreateConnectorRequestDTO(firstName, country, city, bio);

        when(connectorRepository.existsByUserId(userId)).thenReturn(false);
        when(connectorRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(connectorImageService.findByConnector_ConnectorId(any())).thenReturn(Collections.emptyList());
        when(connectorSocialMediaService.findByConnector_ConnectorId(any())).thenReturn(Collections.emptyList());

        ConnectorResponseDTO response = connectorService.createMyProfile(userId, dto);

        assertEquals(firstName, response.getFirstName());
        assertEquals(Country.valueOf(country).getDisplayValue(), response.getCountry());
        assertEquals(City.valueOf(city).getDisplayValue(), response.getCity());
        assertEquals(bio, response.getBio());

        verify(connectorRepository).save(any(Connector.class));
    }

    @Test
    void createMyProfile_shouldThrow_whenConnectorExists() {
        CreateConnectorRequestDTO dto = new CreateConnectorRequestDTO(null, null, null, null);
        when(connectorRepository.existsByUserId(userId)).thenReturn(true);

        ExistingConnectorException exception = assertThrows(ExistingConnectorException.class,
                () -> connectorService.createMyProfile(userId, dto));
        assertTrue(exception.getMessage().contains("Connector already exists"));
    }

    @Test
    void getPublicProfile_shouldReturnProfile_whenFound() {
        String country = "POLAND";
        String city = "KRAKOW";
        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(connector));
        when(connectorImageService.findByConnector_ConnectorId(connector.getConnectorId())).thenReturn(Collections.emptyList());
        when(connectorSocialMediaService.findByConnector_ConnectorId(connector.getConnectorId())).thenReturn(Collections.emptyList());

        ConnectorResponseDTO response = connectorService.getPublicProfile(userId);

        assertEquals(userId, response.getUserId());
        assertEquals("John", response.getFirstName());
        assertEquals(Country.valueOf(country).getDisplayValue(), response.getCountry());
        assertEquals(City.valueOf(city).getDisplayValue(), response.getCity());
    }

    @Test
    void getPublicProfile_shouldThrow_whenNotFound() {
        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.empty());

        ConnectorNotFoundException exception = assertThrows(ConnectorNotFoundException.class,
                () -> connectorService.getPublicProfile(userId));
        assertTrue(exception.getMessage().contains("Connector not found"));
    }

    @Test
    void getPublicProfiles_shouldReturnList() {
        List<Connector> connectors = List.of(connector);
        List<UUID> userIds = List.of(userId);

        when(connectorRepository.findAllByUserIdIn(userIds)).thenReturn(connectors);
        when(connectorImageService.findByConnector_ConnectorId(connector.getConnectorId())).thenReturn(Collections.emptyList());
        when(connectorSocialMediaService.findByConnector_ConnectorId(connector.getConnectorId())).thenReturn(Collections.emptyList());

        List<ConnectorResponseDTO> result = connectorService.getPublicProfiles(userIds);

        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
    }

    @Test
    void addGalleryPhoto_validRequest_addsImageAndReturnsUpdatedResponse() throws Exception {
        UUID connectorId = UUID.randomUUID();
        ConnectorImageDTO imageDTO = new ConnectorImageDTO("http://image.jpg", 1);

        Connector mockedConnector = mock(Connector.class);
        when(mockedConnector.getConnectorId()).thenReturn(connectorId);
        when(mockedConnector.getUserId()).thenReturn(userId);
        when(mockedConnector.getFirstName()).thenReturn("John");
        when(mockedConnector.getCountry()).thenReturn(Country.POLAND);
        when(mockedConnector.getCity()).thenReturn(City.KRAKOW);
        when(mockedConnector.getBio()).thenReturn("Bio");

        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(mockedConnector));

        when(connectorImageService.findByConnector_ConnectorId(connectorId)).thenReturn(List.of(imageDTO));
        when(connectorSocialMediaService.findByConnector_ConnectorId(connectorId)).thenReturn(List.of());

        ConnectorResponseDTO response = connectorService.addGalleryPhoto(userId, imageDTO);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals("John", response.getFirstName());
        assertEquals(1, response.getGalleryImages().size());

        verify(connectorImageService).addGalleryPhoto(imageDTO, mockedConnector);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 6, 100})
    void addGalleryPhoto_invalidOrderIndex_throwsImageIndexOutOfBoundException(int orderIndex) throws ImageNotFoundException, ImageIndexOutOfBoundException, InvalidImageOrderException {
        ConnectorImageDTO imageDTO = new ConnectorImageDTO("http://image.jpg", orderIndex);
        Connector mockedConnector = mock(Connector.class);

        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(mockedConnector));

        doThrow(new ImageIndexOutOfBoundException("Order index must be between 0 and 5"))
                .when(connectorImageService).addGalleryPhoto(eq(imageDTO), eq(mockedConnector));

        ImageIndexOutOfBoundException exception = assertThrows(
                ImageIndexOutOfBoundException.class,
                () -> connectorService.addGalleryPhoto(userId, imageDTO)
        );

        assertTrue(exception.getMessage().contains("Order index must be between 0 and 5"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5})
    void deleteGalleryPhoto_validRequest_deletesImageAndReturnsUpdatedResponse(int orderIndexToDelete) throws Exception {
        UUID connectorId = UUID.randomUUID();

        Connector mockedConnector = mock(Connector.class);
        when(mockedConnector.getConnectorId()).thenReturn(connectorId);
        when(mockedConnector.getUserId()).thenReturn(userId);
        when(mockedConnector.getFirstName()).thenReturn("John");
        when(mockedConnector.getCountry()).thenReturn(Country.POLAND);
        when(mockedConnector.getCity()).thenReturn(City.KRAKOW);
        when(mockedConnector.getBio()).thenReturn("Bio");

        List<ConnectorImageDTO> existingImages = new ArrayList<>();
        for(int i = 0; i < 6; i++) {
            existingImages.add(new ConnectorImageDTO("http://image" + i + ".jpg", i));
        }
        List<ConnectorImageDTO> imagesAfterDelete = existingImages.stream().filter(img -> img.getOrderIndex() != orderIndexToDelete).toList();

        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(mockedConnector));
        when(connectorImageService.deleteGalleryPhoto(orderIndexToDelete, mockedConnector)).thenReturn(mock(ConnectorImageDTO.class));

        when(connectorImageService.findByConnector_ConnectorId(connectorId)).thenReturn(imagesAfterDelete);
        when(connectorSocialMediaService.findByConnector_ConnectorId(connectorId)).thenReturn(List.of());

        ConnectorResponseDTO response = connectorService.deleteGalleryPhoto(userId, orderIndexToDelete);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals("John", response.getFirstName());
        assertEquals(5, response.getGalleryImages().size());
        verify(connectorImageService).deleteGalleryPhoto(orderIndexToDelete, mockedConnector);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 6, 10})
    void deleteGalleryPhoto_invalidIndex_throwsImageIndexOutOfBoundException(int orderIndexToDelete) throws ImageIndexOutOfBoundException, ImageNotFoundException, ProfilePictureRequiredException {
        Connector mockedConnector = mock(Connector.class);
        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(mockedConnector));

        doThrow(new ImageIndexOutOfBoundException("Order index must be between 0 and 5"))
                .when(connectorImageService).deleteGalleryPhoto(orderIndexToDelete, mockedConnector);

        ImageIndexOutOfBoundException exception = assertThrows(
                ImageIndexOutOfBoundException.class,
                () -> connectorService.deleteGalleryPhoto(userId, orderIndexToDelete)
        );

        assertTrue(exception.getMessage().contains("Order index must be between 0 and 5"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4}) // Valid order indexes
    void generateGalleryUploadSignature_validIndex_shouldReturnSignature(int orderIndex) throws ImageIndexOutOfBoundException {
        String expectedFolder = "connectors/" + userId;
        String expectedImageName = String.valueOf(orderIndex);
        UploadSignatureResponseDTO mockResponse = new UploadSignatureResponseDTO(
                "mockApiKey", "mockCloudName", "mockSignature", "1234567890", expectedFolder, expectedImageName
        );

        when(mediaService.generateUploadSignature(expectedImageName, expectedFolder)).thenReturn(mockResponse);

        UploadSignatureResponseDTO result = connectorService.generateGalleryUploadSignature(userId, orderIndex);

        assertNotNull(result);
        assertEquals("mockSignature", result.getSignature());
        assertEquals(expectedFolder, result.getFolder());
        assertEquals(expectedImageName, result.getPublicId());

        verify(mediaService).generateUploadSignature(expectedImageName, expectedFolder);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 5, 100})
    void generateGalleryUploadSignature_invalidIndex_shouldThrow(int invalidIndex) {
        ConnectorService service = new ConnectorService(connectorRepository, connectorSocialMediaService, connectorImageService, mediaService);

        ImageIndexOutOfBoundException exception = assertThrows(
                ImageIndexOutOfBoundException.class,
                () -> service.generateGalleryUploadSignature(userId, invalidIndex)
        );

        assertTrue(exception.getMessage().contains("Order index must be between"));
    }

}
