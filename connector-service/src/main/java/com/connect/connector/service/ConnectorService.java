package com.connect.connector.service;

import com.connect.connector.dto.ConnectorImageDTO;
import com.connect.connector.dto.request.CreateConnectorRequestDTO;
import com.connect.connector.dto.request.UpdateConnectorRequestDTO;
import com.connect.connector.dto.response.ConnectorResponseDTO;
import com.connect.connector.enums.City;
import com.connect.connector.enums.Country;
import com.connect.connector.exception.*;
import com.connect.connector.mapper.ConnectorImageMapper;
import com.connect.connector.model.Connector;
import com.connect.connector.model.ConnectorImage;
import com.connect.connector.repository.ConnectorImageRepository;
import com.connect.connector.repository.ConnectorRepository;
import com.connect.connector.repository.ConnectorSocialMediaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConnectorService {

    private final ConnectorRepository connectorRepository;
    private final ConnectorSocialMediaRepository connectorSocialMediaRepository;
    private final ConnectorImageService connectorImageService;
    private final ConnectorImageMapper connectorImageMapper;

    public ConnectorResponseDTO updateMyProfile(UUID userId, @Valid UpdateConnectorRequestDTO updateConnectorRequestDTO) {
        Connector connector = findConnectorByUserId(userId);

        if (updateConnectorRequestDTO.getFirstName() != null) {
            connector.setFirstName(updateConnectorRequestDTO.getFirstName());
        }
        if (updateConnectorRequestDTO.getCountry() != null) {
            connector.setCountry(Country.valueOf(updateConnectorRequestDTO.getCountry()));
        }
        if (updateConnectorRequestDTO.getCity() != null) {
            connector.setCity(City.valueOf(updateConnectorRequestDTO.getCity()));
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
                .country(Country.valueOf(createConnectorRequestDTO.getCountry()))
                .city(City.valueOf(createConnectorRequestDTO.getCity()))
                .bio(createConnectorRequestDTO.getBio())
                .build();

        Connector savedConnector = connectorRepository.save(connector);

        return buildConnectorResponse(savedConnector);
    }

    public ConnectorResponseDTO addGalleryPhoto(UUID userId, ConnectorImageDTO connectorImageDTO) throws ImageIndexOutOfBoundException, ImageNotFoundException {
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

    public ConnectorResponseDTO deleteGalleryPhoto(UUID userId, int orderIndex) throws ImageIndexOutOfBoundException {
        Connector connector = findConnectorByUserId(userId);
        connectorImageService.deleteGalleryPhoto(orderIndex, connector);
        return buildConnectorResponse(connector);
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
                .galleryImages(connectorImageMapper.toDtoList(connectorImageService.findByConnectorId(connector.getId())))
                .socialMediaLinks(connectorSocialMediaRepository.findByConnectorId(connector.getId()).entrySet()
                        .stream()
                        .collect(Collectors.toMap(entry -> entry.getKey().name(),
                                Map.Entry::getValue)))
                .build();
    }
}
