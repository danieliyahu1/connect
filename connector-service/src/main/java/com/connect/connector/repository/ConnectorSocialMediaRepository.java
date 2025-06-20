package com.connect.connector.repository;

import com.connect.connector.enums.SocialMediaPlatform;
import com.connect.connector.model.ConnectorSocialMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConnectorSocialMediaRepository extends JpaRepository<ConnectorSocialMedia, UUID> {
    List<ConnectorSocialMedia> findByConnector_ConnectorId(UUID connectorId);

    ConnectorSocialMedia findByConnector_ConnectorIdAndPlatform(UUID connectorId, SocialMediaPlatform platform);
}
