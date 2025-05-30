package com.connect.connector.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "connectors")
@Data
public class Connector {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    private String firstName;
    private String country;
    private String city;
    private String bio;
    private String profilePictureUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "connector_social_media_links",
            joinColumns = @JoinColumn(name = "connector_id")
    )
    @Column(name = "url")
    @MapKeyColumn(name = "platform")

    private List<ConnectorSocialMedia> socialMediaLinks;

    public void setUserId(UUID userId) {
        if(this.userId == null) {
            this.userId = userId;
        }
    }
}
