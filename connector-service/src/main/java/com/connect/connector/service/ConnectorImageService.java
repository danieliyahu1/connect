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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConnectorImageService {
    private final ConnectorImageRepository connectorImageRepository;
    private final ConnectorImageMapper connectorImageMapper;

    public ConnectorImageDTO addGalleryPhoto(ConnectorImageDTO connectorImageDTO, Connector connector) throws ImageIndexOutOfBoundException, InvalidImageOrderException, ImageNotFoundException {
        validateConnectorImageIndex(connectorImageDTO.getOrderIndex());
        return connectorImageMapper.toDto(saveConnectorImage(connectorImageDTO, connector));
    }

    public ConnectorImageDTO deleteGalleryPhoto(int orderIndex, Connector connector) throws ImageIndexOutOfBoundException, ImageNotFoundException, ProfilePictureRequiredException {
        validateConnectorImageIndex(orderIndex);
        List<ConnectorImage> images = getConnectorImages(connector.getConnectorId());
        validateUserHasProfileImage(images);
        ConnectorImage deletedConnectorImage = removeGalleryImage(orderIndex, images);
        images = getConnectorImages(connector.getConnectorId());
        reorderGalleryImages(images, orderIndex);
        return connectorImageMapper.toDto(deletedConnectorImage);
    }

    public List<ConnectorImageDTO> findByConnector_ConnectorId(UUID id) {
        return connectorImageRepository.findByConnector_ConnectorId(id).stream()
                .map(connectorImageMapper::toDto)
                .toList();
    }

    private void validateConnectorImageIndex(int imageOrderIndex) throws ImageIndexOutOfBoundException {
        if (imageOrderIndex < 0 || imageOrderIndex > 5) {
            throw new ImageIndexOutOfBoundException("Order index must be between 0 and 5");
        }
    }

    private ConnectorImage saveConnectorImage(ConnectorImageDTO connectorImage, Connector connector) throws InvalidImageOrderException, ImageNotFoundException {
        List<ConnectorImage> existingImages = getConnectorImages(connector.getConnectorId());
        int orderIndex = connectorImage.getOrderIndex();

        if(isReplacingExistingImage(existingImages, orderIndex)) {
            removeGalleryImage(connectorImage.getOrderIndex(), existingImages);
        }
        else{
            validateIsNextAvailableIndex(existingImages, orderIndex);
        }

        return connectorImageRepository.save(createImageOrderIndex(connectorImage, connector));
    }

    private boolean isReplacingExistingImage(List<ConnectorImage> existingImages, int orderIndex) {
        return existingImages.stream()
                .anyMatch(image -> image.getOrderIndex() == orderIndex);
    }

    private List<ConnectorImage> getConnectorImages(UUID connectorId) {
        return connectorImageRepository.findByConnector_ConnectorId(connectorId);
    }

    private void validateIsNextAvailableIndex(List<ConnectorImage> images, int orderIndex) throws InvalidImageOrderException {
        if(orderIndex != images.size()) {
            throw new InvalidImageOrderException(
                    String.format("Order index is %d but should be equal to the next spot available in the images list: %d", orderIndex, images.size())
            );
        }
    }

    private ConnectorImage removeGalleryImage(int orderIndex, List<ConnectorImage> images) throws ImageNotFoundException {
        Optional<ConnectorImage> imageOpt = images.stream()
                .filter(img -> img.getOrderIndex() == orderIndex)
                .findFirst();

        imageOpt.ifPresent(connectorImageRepository::delete);
        return imageOpt.orElseThrow(() -> new ImageNotFoundException("Image not found at the specified order index: " + orderIndex));
    }

    private void validateUserHasProfileImage(List<ConnectorImage> images) throws ProfilePictureRequiredException {
        if(images.size() < 2){
            throw new ProfilePictureRequiredException("User must have at least one picture for profile .");
        }
    }

    private void reorderGalleryImages(List<ConnectorImage> images, int removedIndex) {
        ConnectorImage[] tempArray = new ConnectorImage[5];
        for(ConnectorImage image : images) {
            tempArray[image.getOrderIndex()] = image;
        }
        for(int i = removedIndex+1; i < tempArray.length - 1; i++) {
            // If the next image is null, we reached the end of pictures
            if(tempArray[i] == null) {
                return;
            }
            updateImageOrderIndex(tempArray[i],tempArray[i].getOrderIndex() - 1);
        }
    }

    private void updateImageOrderIndex(ConnectorImage image, int newOrderIndex) {
        image.setOrderIndex(newOrderIndex);
    }

    private ConnectorImage createImageOrderIndex(ConnectorImageDTO image, Connector connector) {
        return ConnectorImage.builder()
                .connector(connector)
                .mediaUrl(image.getMediaUrl())
                .orderIndex(image.getOrderIndex())
                .build();
    }
}
