package com.connect.connector.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceClient {

    @Value("${auth.service.url}")
    private String authServiceUrl; // e.g. http://auth-service:4000/api/v1/auth

    private final RestTemplate restTemplate;

    public UUID getUserIdFromAccessToken(String accessToken) {
        String url = authServiceUrl + "/getUserIdFromAccessToken";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<UUID> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                UUID.class
        );
        return response.getBody();
    }
}
