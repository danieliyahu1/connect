package com.akatsuki.connector.service;

import com.akatsuki.connector.dto.ConnectorSocialMediaDTO;
import com.akatsuki.connector.enums.SocialMediaPlatform;
import com.akatsuki.connector.enums.util.EnumUtil;
import com.akatsuki.connector.exception.ConnectorSocialMediaNotFoundException;
import com.akatsuki.connector.exception.ExistingSocialMediaPlatformException;
import com.akatsuki.connector.exception.IllegalEnumException;
import com.akatsuki.connector.exception.InvalidProfileUrlException;
import com.akatsuki.connector.model.Connector;
import com.akatsuki.connector.model.ConnectorSocialMedia;
import com.akatsuki.connector.repository.ConnectorSocialMediaRepository;
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

    public ConnectorSocialMediaDTO addSocialMediaPlatformLink(Connector connector, ConnectorSocialMediaDTO connectorSocialMediaDTO) throws InvalidProfileUrlException, ExistingSocialMediaPlatformException, IllegalEnumException {
        validateProfileUrl(connectorSocialMediaDTO.getProfileUrl());
        validateConnectorDoesNotHaveSocialMediaPlatformLink(connector.getConnectorId(),
                EnumUtil.getEnumFromDisplayName(SocialMediaPlatform.class, connectorSocialMediaDTO.getPlatform()));
        ConnectorSocialMedia connectorSocialMedia = ConnectorSocialMedia.builder()
                .connector(connector)
                .platform(EnumUtil.getEnumFromDisplayName(SocialMediaPlatform.class, connectorSocialMediaDTO.getPlatform()))
                .profileUrl(connectorSocialMediaDTO.getProfileUrl())
                .build();
        connectorSocialMediaRepository.save(connectorSocialMedia);
        return convertSocialMediaModelToDTO(connectorSocialMedia);
    }

    public ConnectorSocialMediaDTO updateSocialMediaPlatformLink(Connector connector, String platform, String profileUrl) throws InvalidProfileUrlException, ConnectorSocialMediaNotFoundException, IllegalEnumException {
        validateProfileUrl(profileUrl);
        ConnectorSocialMedia connectorSocialMedia = findSocialMediaPlatformLink(connector, platform);
        return convertSocialMediaModelToDTO(updateSocialMediaPlatformLink(connectorSocialMedia, profileUrl));
    }

    public ConnectorSocialMediaDTO deleteSocialMediaPlatformLink(Connector connectorByUserId, String platform) throws ConnectorSocialMediaNotFoundException, IllegalEnumException {
        ConnectorSocialMedia connectorSocialMedia = findSocialMediaPlatformLink(connectorByUserId, platform);
        deleteSocialMediaPlatformLink(connectorSocialMedia);
        return convertSocialMediaModelToDTO(connectorSocialMedia);
    }

    private void deleteSocialMediaPlatformLink(ConnectorSocialMedia connectorSocialMedia) {
        connectorSocialMediaRepository.delete(connectorSocialMedia);
    }

    private void validateConnectorDoesNotHaveSocialMediaPlatformLink(UUID connectorId, SocialMediaPlatform platform) throws ExistingSocialMediaPlatformException {
        if(connectorSocialMediaRepository.existsByConnector_ConnectorIdAndPlatform(connectorId, platform)) {
            throw new ExistingSocialMediaPlatformException("Social media link for this platform already exists");
        }
    }

    private ConnectorSocialMedia findSocialMediaPlatformLink(Connector connector, String platform) throws ConnectorSocialMediaNotFoundException, IllegalEnumException {
        return connectorSocialMediaRepository.findByConnector_ConnectorIdAndPlatform(
                connector.getConnectorId(),
                        EnumUtil.getEnumFromDisplayName(SocialMediaPlatform.class, platform))
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
