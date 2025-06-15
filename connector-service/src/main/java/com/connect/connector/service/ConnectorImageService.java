package com.connect.connector.service;

import com.connect.connector.dto.ConnectorImageDTO;
import com.connect.connector.exception.ImageIndexOutOfBoundException;
import com.connect.connector.exception.ImageNotFoundException;
import com.connect.connector.exception.ProfilePictureRequiredException;
import com.connect.connector.model.Connector;
import com.connect.connector.model.ConnectorImage;
import com.connect.connector.repository.ConnectorImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConnectorImageService {
    private final ConnectorImageRepository connectorImageRepository;

    public void addGalleryPhoto(ConnectorImageDTO connectorImageDTO, Connector connector) throws ImageIndexOutOfBoundException, ImageNotFoundException {
        validateConnectorImageIndex(connectorImageDTO.getOrderIndex());
        ConnectorImage connectorImage = ConnectorImage.builder()
                .connectorId(connector.getId())
                .mediaUrl(connectorImageDTO.getMediaUrl())
                .build();

        saveConnectorImage(connectorImage);
    }

    public void deleteGalleryPhoto(int orderIndex, Connector connector) throws ImageIndexOutOfBoundException {
        validateConnectorImageIndex(orderIndex);
        List<ConnectorImage> images = getConnectorImages(connector.getId());
        checkUserHasProfileImage(images);
        removeGalleryImage(orderIndex, images);
        images = getConnectorImages(connector.getId());
        reOrderGalleryImages(images, orderIndex);
    }

    private void validateConnectorImageIndex(int imageOrderIndex) throws ImageIndexOutOfBoundException {
        if (imageOrderIndex < 0 || imageOrderIndex > 5) {
            throw new ImageIndexOutOfBoundException("Order index must be between 0 and 5");
        }
    }

    private void saveConnectorImage(ConnectorImage connectorImage) {
        List<ConnectorImage> existingImages = getConnectorImages(connectorImage.getConnectorId());
        int imageIndex = getNewImageIndex(existingImages, connectorImage.getOrderIndex());
        connectorImage.setOrderIndex(imageIndex);
        removeGalleryImage(imageIndex, existingImages);
        connectorImageRepository.save(connectorImage);
    }

    private List<ConnectorImage> getConnectorImages(UUID connectorId) {
        return connectorImageRepository.findByConnectorId(connectorId);
    }

    private int getNewImageIndex(List<ConnectorImage> images, int orderIndex) {
        if(orderIndex > images.size()) {
            return images.size() + 1;
        }
        return orderIndex;
    }

    private void removeGalleryImage(int orderIndex, List<ConnectorImage> images) {
        images.stream()
                .filter(img -> img.getOrderIndex() == orderIndex)
                .findFirst()
                .ifPresent(connectorImageRepository::delete);
    }

    private void checkUserHasProfileImage(List<ConnectorImage> images) {
        if(images.size() < 2){
            throw new ProfilePictureRequiredException("User must have at least one picture for profile .");
        }
    }

    private void reOrderGalleryImages(List<ConnectorImage> images, int removedIndex) {
        ConnectorImage[] tempArray = new ConnectorImage[5];
        for(ConnectorImage image : images) {
            tempArray[image.getOrderIndex()] = image;
        }
        for(int i = removedIndex; i < tempArray.length - 1; i++) {
            if(tempArray[i] == null) {
                return;
            }
            tempArray[i].setOrderIndex(tempArray[i + 1].getOrderIndex() - 1);
        }
    }

    public List<ConnectorImage> findByConnectorId(UUID id) {
        return connectorImageRepository.findByConnectorId(id);
    }
}
