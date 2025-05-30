package com.connect.connector.model;

import com.connect.connector.enums.SocialMedia;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "social_media")
@Data
@NoArgsConstructor
public class ConnectorSocialMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    private UUID id;

    private SocialMedia platformName;
    private String profileUrl;
}
