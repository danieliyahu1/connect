package com.connect.discovery.controller;

import com.connect.discovery.dto.UserSuggestionDTO;
import com.connect.discovery.service.DiscoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/discover")
public class DiscoveryController {

    private final DiscoveryService discoveryService;

    @GetMapping("/locals")
    public List<UserSuggestionDTO> discoverLocals(Authentication authentication,
                                                  @RequestHeader("Authorization") String authorizationHeader) {
        return discoveryService.discoverLocals(getUserIdFromAuth(authentication));
    }

    @GetMapping("/travelers")
    public List<UserSuggestionDTO> discoverTravelers(Authentication authentication) {
        return discoveryService.discoverTravelers(getUserIdFromAuth(authentication));
    }

    private UUID getUserIdFromAuth(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("Authentication not set in SecurityContext. This endpoint requires authentication.");
        }
        return UUID.fromString(authentication.getName());
    }
}