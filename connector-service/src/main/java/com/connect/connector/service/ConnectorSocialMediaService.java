package com.connect.connector.service;

import com.connect.connector.dto.ConnectorSocialMediaDTO;
import com.connect.connector.enums.SocialMediaPlatform;
import com.connect.connector.enums.util.EnumUtil;
import com.connect.connector.exception.ConnectorSocialMediaNotFoundException;
import com.connect.connector.exception.InvalidProfileUrlException;
import com.connect.connector.model.Connector;
import com.connect.connector.model.ConnectorSocialMedia;
import com.connect.connector.repository.ConnectorSocialMediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConnectorSocialMediaService {
    private final ConnectorSocialMediaRepository connectorSocialMediaRepository;

    public List<ConnectorSocialMedia> findByConnector_ConnectorId(UUID id) {
        return connectorSocialMediaRepository.findByConnector_ConnectorId(id);
    }

    public ConnectorSocialMediaDTO addSocialMediaPlatformLink(Connector connector, ConnectorSocialMediaDTO connectorSocialMediaDTO) throws InvalidProfileUrlException {
        validateProfileUrl(connectorSocialMediaDTO.getProfileUrl());

        ConnectorSocialMedia connectorSocialMedia = ConnectorSocialMedia.builder()
                .connector(connector)
                .platform(EnumUtil.fromDisplayName(SocialMediaPlatform.class, connectorSocialMediaDTO.getPlatform()))
                .profileUrl(connectorSocialMediaDTO.getProfileUrl())
                .build();
        connectorSocialMediaRepository.save(connectorSocialMedia);
        return convertSocialMediaModelToDTO(connectorSocialMedia);
    }

    public ConnectorSocialMediaDTO updateSocialMediaPlatformLink(Connector connector, String platform, String profileUrl) throws InvalidProfileUrlException, ConnectorSocialMediaNotFoundException {
        validateProfileUrl(profileUrl);
        ConnectorSocialMedia connectorSocialMedia = findSocialMediaPlatformLink(connector, platform);
        return convertSocialMediaModelToDTO(updateSocialMediaPlatformLink(connectorSocialMedia, profileUrl));
    }

    public ConnectorSocialMediaDTO deleteSocialMediaPlatformLink(Connector connectorByUserId, String platform) throws ConnectorSocialMediaNotFoundException {
        ConnectorSocialMedia connectorSocialMedia = findSocialMediaPlatformLink(connectorByUserId, platform);
        deleteSocialMediaPlatformLink(connectorSocialMedia);
        return convertSocialMediaModelToDTO(connectorSocialMedia);
    }

    private void deleteSocialMediaPlatformLink(ConnectorSocialMedia connectorSocialMedia) {
        connectorSocialMediaRepository.delete(connectorSocialMedia);
    }

    private ConnectorSocialMedia findSocialMediaPlatformLink(Connector connector, String platform) throws ConnectorSocialMediaNotFoundException {
        return connectorSocialMediaRepository.findByConnector_ConnectorIdAndPlatform(
                connector.getConnectorId(),
                        EnumUtil.fromDisplayName(SocialMediaPlatform.class, platform))
                .orElseThrow(() -> new ConnectorSocialMediaNotFoundException
                        ("No social media link found for the specified platform"));
    }

    private ConnectorSocialMedia updateSocialMediaPlatformLink(ConnectorSocialMedia connectorSocialMedia, String profileUrl) {
        connectorSocialMedia.setProfileUrl(profileUrl);
        return connectorSocialMediaRepository.save(connectorSocialMedia);
    }

    private void validateProfileUrl(String profileUrl) throws InvalidProfileUrlException {
        if (profileUrl == null || profileUrl.isEmpty()) {
            throw new InvalidProfileUrlException("Profile URL cannot be null or empty");
        }
    }

    private ConnectorSocialMediaDTO convertSocialMediaModelToDTO(ConnectorSocialMedia connectorSocialMedia) {
        return ConnectorSocialMediaDTO.builder()
                .platform(connectorSocialMedia.getPlatform().name())
                .profileUrl(connectorSocialMedia.getProfileUrl())
                .build();
    }
}
