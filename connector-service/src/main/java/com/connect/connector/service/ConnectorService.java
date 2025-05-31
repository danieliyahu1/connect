package com.connect.connector.service;

import com.connect.connector.dto.ConnectorRequestDTO;
import com.connect.connector.dto.ConnectorResponseDTO;
import com.connect.connector.exception.ExistingConnectorException;
import com.connect.connector.model.Connector;
import com.connect.connector.repository.ConnectorRepository;
import com.connect.connector.repository.ConnectorSocialMediaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConnectorService {

    private final ConnectorRepository connectorRepository;
    private final ConnectorSocialMediaRepository connectorSocialMediaRepository;

    public ConnectorResponseDTO updateConnector(UUID userId, @Valid ConnectorRequestDTO connectorRequestDTO) {
        // This method should update the connector's details based on the provided DTO.
        // For now, returning a placeholder object with the updated details.
        ConnectorResponseDTO response = new ConnectorResponseDTO();
        response.setUserId(userId);
        response.setFirstName(connectorRequestDTO.getFirstName());
        response.setCountry(connectorRequestDTO.getCountry());
        response.setCity(connectorRequestDTO.getCity());
        response.setBio(connectorRequestDTO.getBio());
        response.setProfilePictureUrl(connectorRequestDTO.getProfilePictureUrl());

        return response;
    }

    public void createConnector(UUID userId) throws ExistingConnectorException {
        if (connectorRepository.existsByUserId(userId)) {
            throw new ExistingConnectorException("Connector with this userId already exists.");
        }
        connectorRepository.save(new Connector(userId));
    }

}
