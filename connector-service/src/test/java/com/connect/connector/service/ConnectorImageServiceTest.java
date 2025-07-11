package com.connect.connector.service;

import com.connect.connector.dto.ConnectorImageDTO;
import com.connect.connector.exception.ImageIndexOutOfBoundException;
import com.connect.connector.exception.ImageNotFoundException;
import com.connect.connector.exception.InvalidImageOrderException;
import com.connect.connector.exception.ProfilePictureRequiredException;
import com.connect.connector.mapper.ConnectorImageMapper;
import com.connect.connector.model.Connector;
import com.connect.connector.model.ConnectorImage;
import com.connect.connector.repository.ConnectorImageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static com.connect.connector.constants.ConnectorServiceConstants.GALLERY_MAX_INDEX;
import static com.connect.connector.constants.ConnectorServiceConstants.GALLERY_MIN_INDEX;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectorImageServiceTest {

    @Mock
    private ConnectorImageRepository connectorImageRepository;

    @Mock
    private ConnectorImageMapper connectorImageMapper;

    @InjectMocks
    private ConnectorImageService connectorImageService;

    @ParameterizedTest
    @ValueSource(ints = {1, 0})
    void addGalleryPhoto_shouldAdd_whenIndexValid(int orderIndex) {
        Connector connector = mock(Connector.class);

        ConnectorImageDTO imageDTO1 = new ConnectorImageDTO("http://image.jpg", orderIndex);
        ConnectorImage imageDTO2 = new ConnectorImage(connector,"http://image.jpg", 0);
        ConnectorImage image1 = new ConnectorImage(connector, "http://image.jpg", orderIndex);
        List<ConnectorImage> imageDTOs = List.of(imageDTO2);

        Connector mockedConnector = mock(Connector.class);

        when(connectorImageRepository.findByConnector_ConnectorId(mockedConnector.getConnectorId())).thenReturn(imageDTOs);
        when(connectorImageMapper.toDto(any(ConnectorImage.class))).thenReturn(imageDTO1);
        when(connectorImageRepository.save(any(ConnectorImage.class))).thenReturn(image1);
        assertDoesNotThrow(() -> connectorImageService.addGalleryPhoto(imageDTO1, mockedConnector));

    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 6, 100})
    void addGalleryPhoto_shouldThrow_whenIndexInvalid(int orderIndex) {

        ConnectorImageDTO imageDTO = new ConnectorImageDTO("http://image.jpg", orderIndex);

        ImageIndexOutOfBoundException exception = assertThrows(ImageIndexOutOfBoundException.class,
                () -> connectorImageService.addGalleryPhoto(imageDTO, mock(Connector.class)));
        assertTrue(exception.getMessage().contains(
                String.format("Order index must be between %d and %d", GALLERY_MIN_INDEX, GALLERY_MAX_INDEX)
        ));
    }

    @ParameterizedTest
    @ValueSource(ints = { 3, 4 })
    void addGalleryPhoto_whenImageOrderIndexIsNotNextAvailable_shouldThrowInvalidImageOrderException(int orderIndex) {
        ConnectorImageDTO imageDTO = new ConnectorImageDTO("http://image.jpg", orderIndex);
        Connector mockedConnector = mock(Connector.class);

        List<ConnectorImage> existingImages = new ArrayList<>();
        existingImages.add(new ConnectorImage(mockedConnector, "http://image1.jpg", 0));
        existingImages.add(new ConnectorImage(mockedConnector, "http://image2.jpg", 1));

        when(connectorImageRepository.findByConnector_ConnectorId(mockedConnector.getConnectorId())).thenReturn(existingImages);

        InvalidImageOrderException exception = assertThrows(InvalidImageOrderException.class,
                () -> connectorImageService.addGalleryPhoto(imageDTO, mockedConnector));
        assertTrue(exception.getMessage().contains(String.format("Order index is %d but should be equal to the next spot available in the images list: %d", orderIndex, existingImages.size())));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    void deleteGalleryPhoto_validRequest_deletesImageAndReturnsUpdatedResponse(int orderIndexToDelete) {
        UUID userId = UUID.randomUUID();
        Connector connector = mock(Connector.class);

        // Mock connector with id and userId
        Connector mockedConnector = mock(Connector.class);
        when(mockedConnector.getConnectorId()).thenReturn(userId);

        ConnectorImage image0 = ConnectorImage.builder().orderIndex(0).connector(connector).mediaUrl("0").build();
        ConnectorImage image1 = ConnectorImage.builder().orderIndex(1).connector(connector).mediaUrl("1").build();
        ConnectorImage image2 = ConnectorImage.builder().orderIndex(2).connector(connector).mediaUrl("2").build();
        List<ConnectorImage> imagesBeforeDeletion = new ArrayList<>(List.of(image0, image1, image2));

        ConnectorImage deletedImage = imagesBeforeDeletion.get(orderIndexToDelete);


        List<ConnectorImage> imagesAfterDeletion = new ArrayList<>(imagesBeforeDeletion.stream().filter(image -> image.getOrderIndex() != orderIndexToDelete).toList());
        when(connectorImageRepository.findByConnector_ConnectorId(userId))
                .thenReturn(imagesBeforeDeletion)
                .thenReturn(imagesAfterDeletion)
                .thenReturn(imagesAfterDeletion);

        assertDoesNotThrow(() -> connectorImageService.deleteGalleryPhoto(orderIndexToDelete, mockedConnector));

        assertTrue(imagesAfterDeletion.stream().noneMatch(image -> image.getMediaUrl().equals(deletedImage.getMediaUrl())));

    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 6, 100})
    void deleteGalleryPhoto_invalidOrderIndex_throwsImageIndexOutOfBoundException(int orderIndexToDelete) {
        ImageIndexOutOfBoundException exception = assertThrows(ImageIndexOutOfBoundException.class,
                () -> connectorImageService.deleteGalleryPhoto(orderIndexToDelete, mock(Connector.class)));
        assertTrue(exception.getMessage().contains(
                String.format("Order index must be between %d and %d", GALLERY_MIN_INDEX, GALLERY_MAX_INDEX)
        ));
    }

    @Test
    void deleteGalleryPhoto_removeAllPictures_throwsProfilePictureRequiredException() {
        int orderIndexToDelete = 0;
        UUID userId = UUID.randomUUID();

        // Mock connector with id and userId
        Connector mockedConnector = mock(Connector.class);
        when(mockedConnector.getConnectorId()).thenReturn(userId);

        ConnectorImage image0 = ConnectorImage.builder().orderIndex(0).connector(mockedConnector).build();
        List<ConnectorImage> imagesBeforeDeletion = new ArrayList<>(List.of(image0));
        when(connectorImageRepository.findByConnector_ConnectorId(userId))
                .thenReturn(imagesBeforeDeletion);

        assertThrows(ProfilePictureRequiredException.class, () -> connectorImageService.deleteGalleryPhoto(orderIndexToDelete, mockedConnector));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4})
    void deleteGalleryPhoto_imageNotFound_throwsImageNotFoundException(int orderIndexToDelete) {
        UUID userId = UUID.randomUUID();
        Connector mockedConnector = mock(Connector.class);
        when(mockedConnector.getConnectorId()).thenReturn(userId);

        List<ConnectorImage> imagesBeforeDeletion = new ArrayList<>();
        for(int i = 0; i < 6; i++) {
            imagesBeforeDeletion.add(new ConnectorImage(mockedConnector, "http://image" + i + ".jpg", i));
        }
        imagesBeforeDeletion.remove(orderIndexToDelete);
        when(connectorImageRepository.findByConnector_ConnectorId(userId)).thenReturn(imagesBeforeDeletion);

        ImageNotFoundException exception = assertThrows(ImageNotFoundException.class,
                () -> connectorImageService.deleteGalleryPhoto(orderIndexToDelete, mockedConnector));
        assertTrue(exception.getMessage().contains("Image not found at the specified order index: " + orderIndexToDelete));
    }

    @Test
    void findByConnectorId_shouldReturnEmptyList_whenNoImagesFound() {
        UUID connectorId = UUID.randomUUID();
        when(connectorImageRepository.findByConnector_ConnectorId(connectorId)).thenReturn(Collections.emptyList());

        List<ConnectorImageDTO> result = connectorImageService.findByConnector_ConnectorId(connectorId);

        assertTrue(result.isEmpty());
    }
}
