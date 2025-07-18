package com.connect.connector.repository;

import com.connect.connector.model.Connector;
import com.connect.connector.model.ConnectorImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConnectorImageRepository extends JpaRepository<ConnectorImage, UUID> {
    List<ConnectorImage> findByConnector(Connector connector);

    boolean existsByConnectorAndMediaUrl(Connector connector, String mediaUrl);

    List<ConnectorImage> findByConnector_ConnectorId(UUID connectorId);
}
