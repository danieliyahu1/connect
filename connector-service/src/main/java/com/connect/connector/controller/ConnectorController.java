package com.connect.connector.controller;

import com.connect.connector.exception.ExistingConnectorException;
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

    @PutMapping("public/me")
    public ResponseEntity<ConnectorResponseDTO> updateConnector(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody ConnectorRequestDTO request
    ) {
        UUID userId = authService.getUserIdFromAccessToken(authorizationHeader.replace("Bearer ", "").trim());
        ConnectorResponseDTO response = connectorService.updateConnector(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/internal/create")
    public ResponseEntity<Void> createInternalConnector(@RequestBody UUID userId) throws ExistingConnectorException {
        connectorService.createConnector(userId);
        return ResponseEntity.ok().build();
    }
}
