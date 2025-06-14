package com.connect.connector.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "connector_images")
@Data
public class ConnectorImage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Setter(AccessLevel.NONE)
    @Column(name = "connector_id", nullable = false)
    private UUID connectorId;

    @Setter(AccessLevel.NONE)
    @Column(name = "media_id", nullable = false, unique = true)
    private String mediaUrl; // ID received from Media Service

    private int orderIndex; // for ordering images in gallery

    @Builder
    public ConnectorImage(UUID connectorId, String mediaUrl, int orderIndex) {
        this.connectorId = connectorId;
        this.mediaUrl = mediaUrl;
        this.orderIndex = orderIndex;
    }

}
