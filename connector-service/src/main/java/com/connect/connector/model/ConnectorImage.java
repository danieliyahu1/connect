package com.connect.connector.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "connector_images")
@Getter
@NoArgsConstructor
public class ConnectorImage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connector_id", nullable = false)
    private Connector connector;

    @Column(name = "media_id", nullable = false, unique = true)
    private String mediaUrl; // ID received from Media Service

    @Setter
    private int orderIndex; // for ordering images in gallery

    @Builder
    public ConnectorImage(Connector connector, String mediaUrl, int orderIndex) {
        this.connector = connector;
        this.mediaUrl = mediaUrl;
        this.orderIndex = orderIndex;
    }
}
