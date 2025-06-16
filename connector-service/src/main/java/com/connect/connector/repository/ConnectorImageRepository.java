package com.connect.connector.repository;

import com.connect.connector.dto.ConnectorImageDTO;
import com.connect.connector.model.Connector;
import com.connect.connector.model.ConnectorImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConnectorImageRepository  extends JpaRepository<ConnectorImage, UUID> {
    List<ConnectorImage> findByConnector(Connector connector);

    // OR if you want to search by the connector's ID directly:
    List<ConnectorImage> findByConnector_Id(UUID connectorId);
}
