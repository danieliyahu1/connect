package com.connect.connector.controller;

import com.connect.connector.exception.ExistingConnectorException;
import com.connect.connector.model.ConnectorImage;
import com.connect.connector.service.ConnectorService;
import com.connect.connector.client.AuthServiceClient;
import com.connect.connector.dto.ConnectorRequestDTO;
import com.connect.connector.dto.ConnectorResponseDTO;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/connectors")
public class    ConnectorController {

    AuthServiceClient authService;
    ConnectorService connectorService;

    @GetMapping("/me")
    public ConnectorResponseDTO getMyProfile(Authentication authentication) {
        UUID userId = getUserIdFromAuth(authentication);
        return connectorService.getMyProfile(userId);
    }

    @PutMapping("/me")
    public void updateMyProfile(@RequestBody ConnectorRequestDTO connectorRequestDTO, Authentication authentication) {
        UUID userId = getUserIdFromAuth(authentication);
        connectorService.updateMyProfile(userId, connectorRequestDTO);
    }

    @PostMapping("/me/gallery")
    public void addGalleryPhoto(@RequestBody ConnectorImage photoDTO, Authentication authentication) {
        UUID userId = getUserIdFromAuth(authentication);
        connectorService.addGalleryPhoto(userId, photoDTO);
    }

    @GetMapping("/public/{userId}")
    public ConnectorResponseDTO getPublicProfile(@PathVariable UUID userId) {
        return connectorService.getPublicProfile(userId);
    }

    @PostMapping("/internal/create")
    public void createConnector(@RequestBody UUID userId) {
        connectorService.createNewConnector(userId);
    }

    @PostMapping("/internal/public-batch")
    public List<ConnectorResponseDTO> getPublicBatch(@RequestBody List<UUID> userIds) {
        return connectorService.getPublicProfiles(userIds);
    }
}
