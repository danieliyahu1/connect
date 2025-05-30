package com.connect.connector.controller;

import com.connect.connector.service.ConnectorService;
import com.connect.connector.client.AuthServiceClient;
import com.connect.connector.dto.ConnectorRequestDTO;
import com.connect.connector.dto.ConnectorResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/connectors")
public class ConnectorController {

    AuthServiceClient authService;
    ConnectorService connectorService;

    @GetMapping("/me")
    public ResponseEntity<ConnectorResponseDTO> getCurrentConnector() {
        // This method should return the current connector's details.
        // For now, returning a placeholder object.
        return ResponseEntity.ok().body(new ConnectorResponseDTO());
    }

    @PutMapping("/me")
    public ResponseEntity<ConnectorResponseDTO> updateCurrentConnector(@RequestHeader("Authorization") String authorizationHeader,
                                                                       @RequestBody @Valid ConnectorRequestDTO connectorRequestDTO) {
        String accessToken = authorizationHeader.replace("Bearer ", "").trim();
        UUID userId = authService.getUserIdFromAccessToken(accessToken);

        ConnectorResponseDTO updatedConnector = connectorService.updateConnector(userId, connectorRequestDTO);

        return ResponseEntity.ok(updatedConnector);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ConnectorResponseDTO> getPublicConnector(String id) {
        // This method should return a public connector's details by ID.
        // For now, returning a placeholder object.
        return ResponseEntity.ok().body(new ConnectorResponseDTO());
    }

    @PostMapping("/internal/create")
    public ResponseEntity<Void> createInternalConnector(@RequestBody UUID userId) {
        // This method should create a new internal connector.
        // For now, returning the created object as a placeholder.
        return ResponseEntity.ok().body(connectorService.createConnector(userId));
    }

    @GetMapping("/public/getAll")
    public ResponseEntity<List<ConnectorResponseDTO>> getAllPublicConnectors() {
        // This method should return all public connectors.
        // For now, returning an empty list as a placeholder.
        return ResponseEntity.ok().body(connectorService.getAllPublicConnectors());
    }
}
