package com.connect.connector.component;

import com.connect.connector.configuration.ComponentTestConfiguration;

import com.connect.connector.repository.ConnectorImageRepository;
import com.connect.connector.repository.ConnectorRepository;
import com.connect.connector.repository.ConnectorSocialMediaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ComponentTestConfiguration.class)
@ActiveProfiles("component-test")
class ConnectorServiceComponentTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private String accessToken;

    @MockitoBean
    ConnectorImageRepository connectorImageRepository;

    @MockitoBean
    ConnectorRepository connectorRepository;

    @MockitoBean
    ConnectorSocialMediaRepository connectorSocialMediaRepository;

    private String bearerPrefix = "Bearer ";

    private final String URI_TEMPLATE = "/connectors";

    // Helper method for base URL, consistent with reference
    private String getBaseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void validUpdateRequest() {
        // 1. Create a profile first
        String userId = UUID.randomUUID().toString();
        String createUrl = "http://localhost:" + port + URI_TEMPLATE + "/me";
        var createRequest = new com.connect.connector.dto.request.CreateConnectorRequestDTO(
                "Original Name", "Israel", "TelAviv", "This is a valid bio with more than fifteen characters."
        );
        HttpHeaders createHeaders = new HttpHeaders();
        createHeaders.set("Authorization", userId); // Simulate authentication
        HttpEntity<com.connect.connector.dto.request.CreateConnectorRequestDTO> createEntity = new HttpEntity<>(createRequest, createHeaders);
        ResponseEntity<String> createResponse = restTemplate.postForEntity(createUrl, createEntity, String.class);
        assertThat(createResponse.getStatusCode().value()).isEqualTo(201);

        // 2. Prepare update request
        String updateUrl = "http://localhost:" + port + URI_TEMPLATE + "/me";
        var updateRequest = new com.connect.connector.dto.request.UpdateConnectorRequestDTO(
                "Updated Name", "Israel", "TelAviv", "This is an updated bio with more than fifteen characters."
        );
        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.set("Authorization", userId); // Simulate authentication
        HttpEntity<com.connect.connector.dto.request.UpdateConnectorRequestDTO> updateEntity = new HttpEntity<>(updateRequest, updateHeaders);

        // 3. Send PUT request
        ResponseEntity<String> updateResponse = restTemplate.exchange(updateUrl, HttpMethod.PUT, updateEntity, String.class);

        // 4. Assert
        assertThat(updateResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(updateResponse.getBody()).contains("Updated Name");
        assertThat(updateResponse.getBody()).contains("updated bio");
    }
}
