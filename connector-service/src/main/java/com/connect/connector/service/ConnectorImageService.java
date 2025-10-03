package com.akatsuki.connector.service;

import com.akatsuki.connector.dto.ConnectorImageDTO;
import com.akatsuki.connector.exception.*;
import com.akatsuki.connector.mapper.ConnectorImageMapper;
import com.akatsuki.connector.model.Connector;
import com.akatsuki.connector.model.ConnectorImage;
import com.akatsuki.connector.repository.ConnectorImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.akatsuki.connector.constants.ConnectorServiceConstants.*;

@Service
@RequiredArgsConstructor
public class ConnectorImageService {
    private final ConnectorImageRepository connectorImageRepository;
    private final ConnectorImageMapper connectorImageMapper;

    public ConnectorImageDTO addGalleryPhoto(ConnectorImageDTO connectorImageDTO, Connector connector) throws ImageIndexOutOfBoundException, InvalidImageOrderException, ImageNotFoundException, ExistingImageException {
        validateConnectorImageIndex(connectorImageDTO.getOrderIndex());
        checkImageNotExists(connectorImageDTO, connector);
        return connectorImageMapper.toDto(saveConnectorImage(connectorImageDTO, connector));
    }

    private void checkImageNotExists(ConnectorImageDTO connectorImageDTO, Connector connector) throws ExistingImageException {
        if( connectorImageRepository.existsByConnectorAndMediaUrl(connector, connectorImageDTO.getMediaUrl())) {
            throw new ExistingImageException("Image with the same media URL already exists for this connector.");
        }
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
        if (imageOrderIndex < GALLERY_MIN_INDEX || imageOrderIndex > GALLERY_MAX_INDEX) {
            throw new ImageIndexOutOfBoundException(
                    String.format("Order index must be between %d and %d", GALLERY_MIN_INDEX, GALLERY_MAX_INDEX));
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
        ConnectorImage[] tempArray = new ConnectorImage[GALLERY_MAX_SIZE];
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
