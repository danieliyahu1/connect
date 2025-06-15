package com.connect.connector.service;

import com.connect.connector.dto.ConnectorImageDTO;
import com.connect.connector.dto.response.ConnectorResponseDTO;
import com.connect.connector.dto.request.CreateConnectorRequestDTO;
import com.connect.connector.dto.request.UpdateConnectorRequestDTO;
import com.connect.connector.enums.City;
import com.connect.connector.enums.Country;
import com.connect.connector.exception.ConnectorNotFoundException;
import com.connect.connector.exception.ExistingConnectorException;
import com.connect.connector.exception.ImageIndexOutOfBoundException;
import com.connect.connector.exception.ImageNotFoundException;
import com.connect.connector.mapper.ConnectorImageMapper;
import com.connect.connector.model.Connector;
import com.connect.connector.model.ConnectorImage;
import com.connect.connector.service.ConnectorImageService;
import com.connect.connector.repository.ConnectorRepository;
import com.connect.connector.repository.ConnectorSocialMediaRepository;
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
    @Mock private ConnectorSocialMediaRepository connectorSocialMediaRepository;
    @Mock private ConnectorImageService connectorImageService;
    @Mock private ConnectorImageMapper connectorImageMapper;

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
    void updateMyProfile_shouldUpdate_whenFieldsAreNotNull(String firstName, String country, String city, String bio) {
        UpdateConnectorRequestDTO dto = new UpdateConnectorRequestDTO();
        dto.setFirstName(firstName);
        dto.setCountry(country);
        dto.setCity(city);
        dto.setBio(bio);

        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(connector));
        when(connectorRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(connectorImageService.findByConnectorId(connector.getId())).thenReturn(Collections.emptyList());
        when(connectorImageMapper.toDtoList(any())).thenReturn(Collections.emptyList());
        when(connectorSocialMediaRepository.findByConnectorId(connector.getId())).thenReturn(Collections.emptyMap());

        ConnectorResponseDTO response = connectorService.updateMyProfile(userId, dto);

        assertEquals(firstName, response.getFirstName());
        assertEquals(country, response.getCountry());
        assertEquals(city, response.getCity());
        assertEquals(bio, response.getBio());

        verify(connectorRepository).save(any(Connector.class));
    }

    @Test
    void updateMyProfile_shouldThrow_whenConnectorNotFound() {
        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.empty());

        UpdateConnectorRequestDTO dto = new UpdateConnectorRequestDTO();

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

        CreateConnectorRequestDTO dto = new CreateConnectorRequestDTO();
        dto.setFirstName(firstName);
        dto.setCountry(country);
        dto.setCity(city);
        dto.setBio(bio);

        when(connectorRepository.existsByUserId(userId)).thenReturn(false);
        when(connectorRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(connectorImageService.findByConnectorId(any())).thenReturn(Collections.emptyList());
        when(connectorImageMapper.toDtoList(any())).thenReturn(Collections.emptyList());
        when(connectorSocialMediaRepository.findByConnectorId(any())).thenReturn(Collections.emptyMap());

        ConnectorResponseDTO response = connectorService.createMyProfile(userId, dto);

        assertEquals(firstName, response.getFirstName());
        assertEquals(country, response.getCountry());
        assertEquals(city, response.getCity());
        assertEquals(bio, response.getBio());

        verify(connectorRepository).save(any(Connector.class));
    }

    @Test
    void createMyProfile_shouldThrow_whenConnectorExists() {
        CreateConnectorRequestDTO dto = new CreateConnectorRequestDTO();
        when(connectorRepository.existsByUserId(userId)).thenReturn(true);

        ExistingConnectorException exception = assertThrows(ExistingConnectorException.class,
                () -> connectorService.createMyProfile(userId, dto));
        assertTrue(exception.getMessage().contains("Connector already exists"));
    }

    @Test
    void getPublicProfile_shouldReturnProfile_whenFound() {
        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(connector));
        when(connectorImageService.findByConnectorId(connector.getId())).thenReturn(Collections.emptyList());
        when(connectorImageMapper.toDtoList(any())).thenReturn(Collections.emptyList());
        when(connectorSocialMediaRepository.findByConnectorId(connector.getId())).thenReturn(Collections.emptyMap());

        ConnectorResponseDTO response = connectorService.getPublicProfile(userId);

        assertEquals(userId, response.getUserId());
        assertEquals("John", response.getFirstName());
        assertEquals("POLAND", response.getCountry());
        assertEquals("KRAKOW", response.getCity());
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
        when(connectorImageService.findByConnectorId(connector.getId())).thenReturn(Collections.emptyList());
        when(connectorImageMapper.toDtoList(any())).thenReturn(Collections.emptyList());
        when(connectorSocialMediaRepository.findByConnectorId(connector.getId())).thenReturn(Collections.emptyMap());

        List<ConnectorResponseDTO> result = connectorService.getPublicProfiles(userIds);

        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
    }

    @Test
    void addGalleryPhoto_validRequest_addsImageAndReturnsUpdatedResponse() throws Exception {
        UUID connectorId = UUID.randomUUID();
        ConnectorImageDTO imageDTO = new ConnectorImageDTO("http://image.jpg", 1);

        Connector mockedConnector = mock(Connector.class);
        when(mockedConnector.getId()).thenReturn(connectorId);
        when(mockedConnector.getUserId()).thenReturn(userId);
        when(mockedConnector.getFirstName()).thenReturn("John");
        when(mockedConnector.getCountry()).thenReturn(Country.POLAND);
        when(mockedConnector.getCity()).thenReturn(City.KRAKOW);
        when(mockedConnector.getBio()).thenReturn("Bio");

        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(mockedConnector));

        List<ConnectorImageDTO> imageDTOs = List.of(imageDTO);
        when(connectorImageService.findByConnectorId(connectorId)).thenReturn(List.of());
        when(connectorImageMapper.toDtoList(any())).thenReturn(imageDTOs);
        when(connectorSocialMediaRepository.findByConnectorId(connectorId)).thenReturn(Map.of());

        ConnectorResponseDTO response = connectorService.addGalleryPhoto(userId, imageDTO);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals("John", response.getFirstName());
        assertEquals(1, response.getGalleryImages().size());

        verify(connectorImageService).addGalleryPhoto(imageDTO, mockedConnector);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 6, 100})
    void addGalleryPhoto_invalidOrderIndex_throwsImageIndexOutOfBoundException(int orderIndex) throws ImageNotFoundException, ImageIndexOutOfBoundException {
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

    @Test
    void deleteGalleryPhoto_validRequest_deletesImageAndReturnsUpdatedResponse() throws Exception {
        int orderIndexToDelete = 1;
        UUID connectorId = UUID.randomUUID();

        Connector mockedConnector = mock(Connector.class);
        when(mockedConnector.getId()).thenReturn(connectorId);
        when(mockedConnector.getUserId()).thenReturn(userId);
        when(mockedConnector.getFirstName()).thenReturn("John");
        when(mockedConnector.getCountry()).thenReturn(Country.POLAND);
        when(mockedConnector.getCity()).thenReturn(City.KRAKOW);
        when(mockedConnector.getBio()).thenReturn("Bio");

        when(connectorRepository.findByUserId(userId)).thenReturn(Optional.of(mockedConnector));
        doNothing().when(connectorImageService).deleteGalleryPhoto(orderIndexToDelete, mockedConnector);

        List<ConnectorImageDTO> remainingImages = List.of(
                new ConnectorImageDTO("http://image1.jpg", 0),
                new ConnectorImageDTO("http://image3.jpg", 2)
        );
        when(connectorImageService.findByConnectorId(connectorId)).thenReturn(List.of());
        when(connectorImageMapper.toDtoList(any())).thenReturn(remainingImages);
        when(connectorSocialMediaRepository.findByConnectorId(connectorId)).thenReturn(Map.of());

        ConnectorResponseDTO response = connectorService.deleteGalleryPhoto(userId, orderIndexToDelete);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals("John", response.getFirstName());
        assertEquals(2, response.getGalleryImages().size());
        verify(connectorImageService).deleteGalleryPhoto(orderIndexToDelete, mockedConnector);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 6, 10})
    void deleteGalleryPhoto_invalidIndex_throwsImageIndexOutOfBoundException(int orderIndexToDelete) throws ImageIndexOutOfBoundException {
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


}
