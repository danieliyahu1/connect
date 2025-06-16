package com.connect.connector.model;

import com.connect.connector.enums.City;
import com.connect.connector.enums.Country;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "connectors")
@Getter
@NoArgsConstructor
public class Connector {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID connectorId;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Setter
    private String firstName;

    @Enumerated(EnumType.STRING)
    @Setter
    private Country country;

    @Enumerated(EnumType.STRING)
    @Setter
    private City city;

    @Setter
    private String bio;

    @Builder
    public Connector(UUID userId, String firstName, Country country, City city, String bio) {
        this.userId = userId;
        this.firstName = firstName;
        this.country = country;
        this.city = city;
        this.bio = bio;
    }
}
