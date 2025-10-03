package com.akatsuki.connector.model;

import com.akatsuki.connector.enums.SocialMediaPlatform;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "social_media")
@Getter
@NoArgsConstructor
public class ConnectorSocialMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connector_id", nullable = false)
    private Connector connector;

    @Enumerated(EnumType.STRING)
    private SocialMediaPlatform platform;

    @Setter
    private String profileUrl;

    @Builder
    public ConnectorSocialMedia(Connector connector, SocialMediaPlatform platform, String profileUrl) {
        this.connector = connector;
        this.platform = platform;
        this.profileUrl = profileUrl;
    }
}
