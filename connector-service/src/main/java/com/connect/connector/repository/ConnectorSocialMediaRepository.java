package com.connect.connector.repository;

import com.connect.connector.enums.SocialMediaPlatform;
import com.connect.connector.model.ConnectorSocialMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConnectorSocialMediaRepository extends JpaRepository<ConnectorSocialMedia, UUID> {
    List<ConnectorSocialMedia> findByConnector_ConnectorId(UUID connectorId);
    boolean existsByConnector_ConnectorIdAndPlatform(UUID connectorId, SocialMediaPlatform platform);

    Optional<ConnectorSocialMedia> findByConnector_ConnectorIdAndPlatform(UUID connectorId, SocialMediaPlatform platform);
}
