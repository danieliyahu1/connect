package com.akatsuki.discovery.controller;

import com.akatsuki.discovery.dto.UserSuggestionDTO;
import com.akatsuki.discovery.service.DiscoveryService;
import com.fasterxml.jackson.core.JsonProcessingException;
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
@RequestMapping("/discovery")
public class DiscoveryController {

    private final DiscoveryService discoveryService;

    @GetMapping("/public/locals")
    public List<UserSuggestionDTO> discoverLocals(Authentication authentication) throws JsonProcessingException {
        return discoveryService.discoverLocals();
    }

    @GetMapping("/public/travelers")
    public List<UserSuggestionDTO> discoverTravelers(Authentication authentication) throws JsonProcessingException {
        return discoveryService.discoverTravelers();
    }
}