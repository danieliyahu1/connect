package com.connect.auth.controller;

import com.connect.auth.dto.AuthResponseDTO;
import com.connect.auth.exception.WrongProviderException;
import com.connect.auth.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth2")
public class OAuthController {

    private final OAuthService oAuthService;

    @GetMapping("/success")
    public ResponseEntity<AuthResponseDTO> oauth2Success(OAuth2AuthenticationToken authentication) throws WrongProviderException {
        // Extract OAuth2 user info and pass it to service for registration/login
        return ResponseEntity.ok(oAuthService.processOAuthPostLogin(authentication));
    }
}
