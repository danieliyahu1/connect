package com.connect.connector.repository;

import com.connect.connector.enums.Country;
import com.connect.connector.model.Connector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConnectorRepository extends JpaRepository<Connector, UUID> {
    boolean existsByUserId(UUID userId);
    Optional<Connector> findByUserId(UUID userId);
    List<Connector> findAllByUserIdIn(List<UUID> userIds);
    List<Connector> findAllByCountryIn(List<Country> countries);
}
