package com.connect.connector.service;

import com.connect.connector.dto.ConnectorImageDTO;
import com.connect.connector.dto.ConnectorSocialMediaDTO;
import com.connect.connector.dto.request.CreateConnectorRequestDTO;
import com.connect.connector.dto.request.UpdateConnectorRequestDTO;
import com.connect.connector.dto.response.CloudinaryUploadSignatureResponseDTO;
import com.connect.connector.dto.response.ConnectorResponseDTO;
import com.connect.connector.enums.City;
import com.connect.connector.enums.Country;
import com.connect.connector.enums.util.EnumUtil;
import com.connect.connector.exception.*;
import com.connect.connector.mapper.ConnectorImageMapper;
import com.connect.connector.model.Connector;
import com.connect.connector.repository.ConnectorRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.connect.connector.constants.ConnectorServiceConstants.GALLERY_MAX_INDEX;
import static com.connect.connector.constants.ConnectorServiceConstants.GALLERY_MIN_INDEX;

@Service
@RequiredArgsConstructor
public class ConnectorService {

    private final ConnectorRepository connectorRepository;
    private final ConnectorSocialMediaService connectorSocialMediaService;
    private final ConnectorImageService connectorImageService;
    private final MediaService mediaService;

    public ConnectorResponseDTO updateMyProfile(UUID userId, @Valid UpdateConnectorRequestDTO updateConnectorRequestDTO) throws InvalidProfileUrlException {
        Connector connector = findConnectorByUserId(userId);

        if (updateConnectorRequestDTO.getFirstName() != null) {
            connector.setFirstName(updateConnectorRequestDTO.getFirstName());
        }
        if (updateConnectorRequestDTO.getCountry() != null) {
            connector.setCountry(EnumUtil.fromDisplayName(Country.class, updateConnectorRequestDTO.getCountry()));
        }
        if (updateConnectorRequestDTO.getCity() != null) {
            connector.setCity(EnumUtil.fromDisplayName(City.class, updateConnectorRequestDTO.getCity()));
        }
        if (updateConnectorRequestDTO.getBio() != null) {
            connector.setBio(updateConnectorRequestDTO.getBio());
        }

        Connector updatedConnector = connectorRepository.save(connector);

        return buildConnectorResponse(updatedConnector);
    }

    public ConnectorResponseDTO createMyProfile(UUID userId, CreateConnectorRequestDTO createConnectorRequestDTO) throws ExistingConnectorException {
        validateConnectorDoesNotExist(userId);
        Connector connector = Connector.builder()
                .userId(userId)
                .firstName(createConnectorRequestDTO.getFirstName())
                .country(EnumUtil.fromDisplayName(Country.class, createConnectorRequestDTO.getCountry()))
                .city(EnumUtil.fromDisplayName(City.class, createConnectorRequestDTO.getCity()))
                .bio(createConnectorRequestDTO.getBio())
                .build();
        Connector savedConnector = connectorRepository.save(connector);

        return buildConnectorResponse(savedConnector);
    }

    public ConnectorResponseDTO addGalleryPhoto(UUID userId, ConnectorImageDTO connectorImageDTO) throws ImageIndexOutOfBoundException, ImageNotFoundException, InvalidImageOrderException {
        Connector connector = findConnectorByUserId(userId);
        connectorImageService.addGalleryPhoto(connectorImageDTO, connector);
        return buildConnectorResponse(connector);
    }

    public ConnectorResponseDTO getPublicProfile(UUID userId) {
        return connectorRepository.findByUserId(userId)
                .map(this::buildConnectorResponse)
                .orElseThrow(() -> new ConnectorNotFoundException("Connector not found for user ID: " + userId));
    }

    public List<ConnectorResponseDTO> getPublicProfiles(List<UUID> userIds) {
        return connectorRepository.findAllByUserIdIn(userIds)
                .stream()
                .map(this::buildConnectorResponse)
                .toList();
    }

    public ConnectorResponseDTO deleteGalleryPhoto(UUID userId, int orderIndex) throws ImageIndexOutOfBoundException, ImageNotFoundException, ProfilePictureRequiredException {
        Connector connector = findConnectorByUserId(userId);
        connectorImageService.deleteGalleryPhoto(orderIndex, connector);
        return buildConnectorResponse(connector);
    }

    public ConnectorResponseDTO addSocialMediaPlatformLink(UUID userIdFromAuth, @Valid ConnectorSocialMediaDTO dto) throws InvalidProfileUrlException {
        Connector connector = findConnectorByUserId(userIdFromAuth);
        connectorSocialMediaService.addSocialMediaPlatformLink(connector, dto);
        return buildConnectorResponse(connector);
    }

    public ConnectorResponseDTO updateSocialMediaPlatformLink(UUID userIdFromAuth, String platform, String profileUrl) throws InvalidProfileUrlException, ConnectorSocialMediaNotFoundException {
        Connector connector = findConnectorByUserId(userIdFromAuth);
        connectorSocialMediaService.updateSocialMediaPlatformLink(findConnectorByUserId(userIdFromAuth), platform, profileUrl);
        return buildConnectorResponse(connector);
    }

    public ConnectorResponseDTO deleteSocialMediaPlatformLink(UUID userIdFromAuth, String platform) throws ConnectorSocialMediaNotFoundException {
        Connector connector = findConnectorByUserId(userIdFromAuth);
        connectorSocialMediaService.deleteSocialMediaPlatformLink(findConnectorByUserId(userIdFromAuth), platform);
        return buildConnectorResponse(connector);
    }

    public CloudinaryUploadSignatureResponseDTO generateGalleryUploadSignature(UUID userId, int orderIndex) throws ImageIndexOutOfBoundException {
        if (orderIndex < GALLERY_MIN_INDEX || orderIndex > GALLERY_MAX_INDEX) {
            throw new ImageIndexOutOfBoundException("Order index must be between " + GALLERY_MIN_INDEX + " and " + GALLERY_MAX_INDEX);
        }

        String folder = "connectors/" + userId;

        String imageName = String.valueOf(orderIndex);

        return mediaService.createCloudinaryUploadSignature(imageName, folder);
    }

    private void validateConnectorDoesNotExist(UUID userId) throws ExistingConnectorException {
        if (connectorRepository.existsByUserId(userId)) {
            throw new ExistingConnectorException("Connector already exists for user ID: " + userId);
        }
    }

    private Connector findConnectorByUserId(UUID userId) {
        return connectorRepository.findByUserId(userId)
                .orElseThrow(() -> new ConnectorNotFoundException("Connector not found for user ID: " + userId));
    }

    private ConnectorResponseDTO buildConnectorResponse(Connector connector) {
        return ConnectorResponseDTO.builder()
                .userId(connector.getUserId())
                .firstName(connector.getFirstName())
                .country(connector.getCountry().name())
                .city(connector.getCity().name())
                .bio(connector.getBio())
                .galleryImages(connectorImageService.findByConnector_ConnectorId(connector.getConnectorId()))
                .socialMediaLinks(connectorSocialMediaService.findByConnector_ConnectorId(connector.getConnectorId())
                        .stream()
                        .map(socialMedia -> ConnectorSocialMediaDTO.builder()
                                .platform(socialMedia.getPlatform().name())
                                .profileUrl(socialMedia.getProfileUrl())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
