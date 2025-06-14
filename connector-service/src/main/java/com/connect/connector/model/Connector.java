package com.connect.connector.model;

import com.connect.connector.enums.City;
import com.connect.connector.enums.Country;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
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

    @Enumerated(EnumType.STRING)
    private Country country;

    @Enumerated(EnumType.STRING)
    private City city;
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
