package com.connect.connector.service;

import com.connect.connector.dto.ConnectorImageDTO;
import com.connect.connector.exception.ImageIndexOutOfBoundException;
import com.connect.connector.exception.ProfilePictureRequiredException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectorImageServiceTest {

    @Mock
    private ConnectorImageRepository connectorImageRepository;
    @InjectMocks
    private ConnectorImageService connectorImageService;

    @ParameterizedTest
    @ValueSource(ints = {1, 0, 4, 5})
    void addGalleryPhoto_shouldAdd_whenIndexValid(int orderIndex) {
        UUID userId = UUID.randomUUID();

        ConnectorImageDTO imageDTO1 = new ConnectorImageDTO("http://image.jpg", orderIndex);
        ConnectorImage imageDTO2 = new ConnectorImage(userId,"http://image.jpg", 0);
        List<ConnectorImage> imageDTOs = List.of(imageDTO2);

        Connector mockedConnector = mock(Connector.class);
        when(mockedConnector.getId()).thenReturn(UUID.randomUUID());

        when(connectorImageRepository.findByConnectorId(mockedConnector.getId())).thenReturn(imageDTOs);

        assertDoesNotThrow(() -> connectorImageService.addGalleryPhoto(imageDTO1, mockedConnector));

    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 6, 100})
    void addGalleryPhoto_shouldThrow_whenIndexInvalid(int orderIndex) {
        UUID userId = UUID.randomUUID();

        ConnectorImageDTO imageDTO = new ConnectorImageDTO("http://image.jpg", orderIndex);

        ImageIndexOutOfBoundException exception = assertThrows(ImageIndexOutOfBoundException.class,
                () -> connectorImageService.addGalleryPhoto(imageDTO, mock(Connector.class)));
        assertTrue(exception.getMessage().contains("Order index must be between 0 and 5"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5})
    void deleteGalleryPhoto_validRequest_deletesImageAndReturnsUpdatedResponse(int orderIndexToDelete) throws Exception {
        UUID userId = UUID.randomUUID();

        // Mock connector with id and userId
        Connector mockedConnector = mock(Connector.class);
        when(mockedConnector.getId()).thenReturn(userId);

        ConnectorImage image0 = ConnectorImage.builder().orderIndex(0).connectorId(userId).mediaUrl("1").build();
        ConnectorImage image1 = ConnectorImage.builder().orderIndex(1).connectorId(userId).mediaUrl("2").build();
        ConnectorImage image2 = ConnectorImage.builder().orderIndex(2).connectorId(userId).mediaUrl("3").build();
        List<ConnectorImage> imagesBeforeDeletion = new ArrayList<>(List.of(image0, image1, image2));
        ConnectorImage deletedImage;
        if(orderIndexToDelete < imagesBeforeDeletion.size()){
            deletedImage = imagesBeforeDeletion.remove(orderIndexToDelete);
        } else {
            deletedImage = null;
        }
        List<ConnectorImage> imagesAfterDeletion = new ArrayList<>(imagesBeforeDeletion);
        when(connectorImageRepository.findByConnectorId(userId))
                .thenReturn(imagesBeforeDeletion)
                .thenReturn(imagesAfterDeletion)
                .thenReturn(imagesAfterDeletion);

        assertDoesNotThrow(() -> connectorImageService.deleteGalleryPhoto(orderIndexToDelete, mockedConnector));
        // Verify that the image with the specified order index was deleted
        if(deletedImage != null)
        {
            assertTrue(imagesAfterDeletion.stream().noneMatch(image -> image.getMediaUrl().equals(deletedImage.getMediaUrl())));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 6, 100})
    void deleteGalleryPhoto_invalidOrderIndex_throwsIllegalArgumentException(int orderIndexToDelete) throws Exception {
        ImageIndexOutOfBoundException exception = assertThrows(ImageIndexOutOfBoundException.class,
                () -> connectorImageService.deleteGalleryPhoto(orderIndexToDelete, mock(Connector.class)));
        assertTrue(exception.getMessage().contains("Order index must be between 0 and 5"));
    }

    @Test
    void deleteGalleryPhoto_removeAllPictures_throwsProfilePictureRequiredException() throws Exception {
        int orderIndexToDelete = 0;
        UUID userId = UUID.randomUUID();

        // Mock connector with id and userId
        Connector mockedConnector = mock(Connector.class);
        when(mockedConnector.getId()).thenReturn(userId);

        ConnectorImage image0 = ConnectorImage.builder().orderIndex(0).connectorId(userId).build();
        List<ConnectorImage> imagesBeforeDeletion = new ArrayList<>(List.of(image0));
        when(connectorImageRepository.findByConnectorId(userId))
                .thenReturn(imagesBeforeDeletion);

        assertThrows(ProfilePictureRequiredException.class, () -> connectorImageService.deleteGalleryPhoto(orderIndexToDelete, mockedConnector));
    }

    @Test
    void findByConnectorId_shouldReturnEmptyList_whenNoImagesFound() {
        UUID connectorId = UUID.randomUUID();
        when(connectorImageRepository.findByConnectorId(connectorId)).thenReturn(Collections.emptyList());

        List<ConnectorImage> result = connectorImageService.findByConnectorId(connectorId);

        assertTrue(result.isEmpty());
    }
}
