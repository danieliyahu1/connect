package com.connect.connector.repository;

import com.connect.connector.model.ConnectorImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConnectorImageRepository  extends JpaRepository<ConnectorImage, UUID> {
}
