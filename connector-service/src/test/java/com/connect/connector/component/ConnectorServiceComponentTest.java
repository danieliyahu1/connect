//package com.connect.connector.component;
//
//import com.connect.connector.configuration.ComponentTestConfiguration;
//import com.connect.connector.dto.request.CreateConnectorRequestDTO;
//import com.connect.connector.dto.response.ConnectorResponseDTO;
//import com.connect.connector.enums.City;
//import com.connect.connector.enums.Country;
//import com.connect.connector.enums.SocialMediaPlatform;
//import com.connect.connector.model.Connector;
//import com.connect.connector.model.ConnectorImage;
//import com.connect.connector.model.ConnectorSocialMedia;
//import com.connect.connector.repository.ConnectorImageRepository;
//import com.connect.connector.repository.ConnectorRepository;
//import com.connect.connector.repository.ConnectorSocialMediaRepository;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
//import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.boot.test.web.server.LocalServerPort;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.*;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//@EnableAutoConfiguration(exclude = {
//        DataSourceAutoConfiguration.class,
//        JpaRepositoriesAutoConfiguration.class,
//        HibernateJpaAutoConfiguration.class
//})
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Import(ComponentTestConfiguration.class)
//@ActiveProfiles("component-test")
//class ConnectorServiceComponentTest {
//
//    @LocalServerPort
//    private int port;
//
//    @Autowired
//    private TestRestTemplate restTemplate;
//
//    @Autowired
//    private String accessToken;
//
//    @MockitoBean
//    ConnectorImageRepository connectorImageRepository;
//
//    @MockitoBean
//    ConnectorRepository connectorRepository;
//
//    @MockitoBean
//    ConnectorSocialMediaRepository connectorSocialMediaRepository;
//
//    private String bearerPrefix = "Bearer ";
//
//    private final String URI_TEMPLATE = "/connectors";
//
//    // Helper method for base URL, consistent with reference
//    private String getBaseUrl(String path) {
//        return "http://localhost:" + port + path;
//    }
//
//    @Test
//    void updateConnector_ValidInput_ReturnsOkAndUpdatesProfile() {
//        // 1. Simulate user ID (as authentication is likely simulated by header)
//        UUID connectorId = UUID.fromString("d326f3c0-ac99-4a58-64c0-a5864c0a5864");
//
//        // 2. Create a connector first (POST /connectors/me)
//        String createUrl = getBaseUrl(URI_TEMPLATE + "/me");
//        CreateConnectorRequestDTO createRequest = new CreateConnectorRequestDTO(
//                "Original Name", "Israel", "TelAviv", "This is a valid bio with more than fifteen characters."
//        );
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(accessToken); // 👈 this is needed!
//
//        Connector connector = mock(Connector.class);
//        when(connector.getFirstName()).thenReturn("John");
//        when(connector.getCountry()).thenReturn(Country.POLAND);
//        when(connector.getCity()).thenReturn(City.KRAKOW);
//        when(connector.getBio()).thenReturn("Bio");
//        // And if you interact with userId:
//        when(connector.getUserId()).thenReturn(UUID.randomUUID());
//        List<ConnectorImage> images = new ArrayList<>();
//        for(int i = 0; i < 6; i++) {
//            images.add(new ConnectorImage(connector, "http://image" + i + ".jpg", i));
//        }
//
//        ConnectorSocialMedia socialMedia1 = new ConnectorSocialMedia(connector, SocialMediaPlatform.INSTAGRAM, "https://instagram.com/user");
//        ConnectorSocialMedia socialMedia2 = new ConnectorSocialMedia(connector, SocialMediaPlatform.FACEBOOK, "https://facebook.com/user");
//        List<ConnectorSocialMedia> expectedList = List.of(socialMedia1, socialMedia2);
//
//        when(connector.getConnectorId()).thenReturn(connectorId);
//        when(connectorRepository.save(any(Connector.class))).thenReturn(connector);
//        when(connectorImageRepository.findByConnector_Id(connectorId)).thenReturn(images);
//        when(connectorSocialMediaRepository.findByConnectorId(connectorId)).thenReturn(expectedList);
//
//        ResponseEntity<ConnectorResponseDTO> createResponse = restTemplate.postForEntity(createUrl,
//                new HttpEntity<>(createRequest, headers)
//                , ConnectorResponseDTO.class);
//
//        // Assert initial creation
//        Assertions.assertEquals(HttpStatus.CREATED, createResponse.getStatusCode(), "Expected 201 Created for initial connector creation");
//        Assertions.assertNotNull(createResponse.getBody(), "Created connector response body should not be null");
//    }
//}
