package com.connect.connector.controller;

import com.connect.connector.dto.ConnectorImageDTO;
import com.connect.connector.dto.ConnectorSocialMediaDTO;
import com.connect.connector.dto.request.CreateConnectorRequestDTO;
import com.connect.connector.exception.*;
import com.connect.connector.service.ConnectorService;
import com.connect.connector.dto.request.UpdateConnectorRequestDTO;
import com.connect.connector.dto.response.ConnectorResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/connectors")
public class ConnectorController {

    private final ConnectorService connectorService;

    @PutMapping("/me")
    public ResponseEntity<ConnectorResponseDTO> updateMyProfile(@RequestBody UpdateConnectorRequestDTO updateConnectorRequestDTO, Authentication authentication) throws InvalidProfileUrlException {
        return ResponseEntity.ok(connectorService.updateMyProfile(getUserIdFromAuth(authentication), updateConnectorRequestDTO));
    }

    @PostMapping("/me")
    public ResponseEntity<ConnectorResponseDTO> createMyProfile(@RequestBody CreateConnectorRequestDTO createConnectorRequestDTO, Authentication authentication) throws ExistingConnectorException {
        return ResponseEntity.status(HttpStatus.CREATED).body(connectorService.createMyProfile(getUserIdFromAuth(authentication), createConnectorRequestDTO));
    }

    @PostMapping("/me/gallery")
    public ResponseEntity<ConnectorResponseDTO> addGalleryPhoto(@RequestBody ConnectorImageDTO connectorImageDTO, Authentication authentication) throws ImageIndexOutOfBoundException, ImageNotFoundException, InvalidImageOrderException {
        return ResponseEntity.ok(connectorService.addGalleryPhoto(getUserIdFromAuth(authentication), connectorImageDTO));
    }

    @DeleteMapping("/me/gallery/{orderIndex}")
    public ResponseEntity<ConnectorResponseDTO> deleteGalleryPhoto(
            @PathVariable int orderIndex,
            Authentication authentication) throws ImageIndexOutOfBoundException, ImageNotFoundException, ProfilePictureRequiredException {
        return ResponseEntity.ok(connectorService.deleteGalleryPhoto(getUserIdFromAuth(authentication), orderIndex));
    }

    @GetMapping("/public/{userId}")
    public ResponseEntity<ConnectorResponseDTO> getPublicProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(connectorService.getPublicProfile(userId));
    }

    @PostMapping("/me/social-media")
    public ResponseEntity<ConnectorResponseDTO> addSocialMediaPlatformLink(@Valid @RequestBody ConnectorSocialMediaDTO dto, Authentication auth) throws InvalidProfileUrlException {
        return ResponseEntity.status(HttpStatus.CREATED).body(connectorService.addSocialMediaPlatformLink(getUserIdFromAuth(auth), dto));
    }

    @PutMapping("/me/social-media/{platform}")
    public ResponseEntity<ConnectorResponseDTO> updateSocialMediaPlatformLink(@PathVariable String platform, @RequestBody String profileUrl, Authentication auth) throws InvalidProfileUrlException, ConnectorSocialMediaNotFoundException {
        return ResponseEntity.ok(connectorService.updateSocialMediaPlatformLink(getUserIdFromAuth(auth), platform, profileUrl));
    }

    @DeleteMapping("/me/social-media/{platform}")
    public ResponseEntity<ConnectorResponseDTO> deleteSocialMediaPlatformLink(@PathVariable String platform, Authentication auth) throws ConnectorSocialMediaNotFoundException {
        return ResponseEntity.ok(connectorService.deleteSocialMediaPlatformLink(getUserIdFromAuth(auth), platform));
    }

    @PostMapping("/internal/batch")
    public ResponseEntity<List<ConnectorResponseDTO>> getPublicBatch(@RequestBody List<UUID> userIds) {
        return ResponseEntity.ok(connectorService.getPublicProfiles(userIds));
    }

    private UUID getUserIdFromAuth(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("Authentication not set in SecurityContext");
        }
        return UUID.fromString(authentication.getName());
    }
}
