package com.connect.connector.repository;

import com.connect.connector.enums.SocialMedia;
import com.connect.connector.model.ConnectorSocialMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.UUID;

@Repository
public interface ConnectorSocialMediaRepository extends JpaRepository<ConnectorSocialMedia, UUID> {
    Map<SocialMedia, String> findByConnectorId(UUID id);
}
