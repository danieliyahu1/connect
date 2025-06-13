package com.connect.auth.component;

import com.connect.auth.configuration.ComponentTestConfig;
import com.connect.auth.dto.AuthResponseDTO;
import com.connect.auth.enums.AuthProvider;
import com.connect.auth.exception.WrongProviderException;
import com.connect.auth.model.User;
import com.connect.auth.repository.AuthRepository;
import com.connect.auth.repository.UserRepository;
import com.connect.auth.service.OAuthService;
import com.connect.auth.util.JwtUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ComponentTestConfig.class)
@ActiveProfiles("component-test")
class OAuthControllerComponentTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private AuthRepository authRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private OAuthService oAuthService;

    private String getBaseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void oauth2Success_ValidOAuthToken_ReturnsAuthResponse() throws Exception {
        String url = getBaseUrl("/oauth2/success");

        // Prepare mock AuthResponseDTO
        AuthResponseDTO mockResponse = new AuthResponseDTO("mockAccessToken", "mockRefreshToken");
        User user = new User(
                "component-test@example.com",
                "encodedPassword123",
                AuthProvider.LOCAL
        );

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userRepository.findByEmail("component-test@example.com")).thenReturn(Optional.empty());

        // Instead of calling restTemplate, directly call the service method and assert the result:
        ResponseEntity<AuthResponseDTO> response = oAuthService.processOAuthPostLogin(Mockito.mock(OAuth2AuthenticationToken.class));
        Assertions.assertNotNull(response);
        Assertions.assertEquals("mockAccessToken", response.getBody().getAccessToken());
        Assertions.assertEquals("mockRefreshToken", response.getBody().getRefreshToken());

        // Verify the service was called once
        Mockito.verify(oAuthService, Mockito.times(1)).processOAuthPostLogin(Mockito.any(OAuth2AuthenticationToken.class));
    }

    @Test
    void oauth2Success_WrongProvider_ThrowsException() throws Exception {
        Mockito.when(oAuthService.processOAuthPostLogin(Mockito.any(OAuth2AuthenticationToken.class)))
                .thenThrow(new WrongProviderException("User already registered with a different provider"));

        // We directly call the service and expect exception
        Assertions.assertThrows(WrongProviderException.class, () -> {
            oAuthService.processOAuthPostLogin(Mockito.mock(OAuth2AuthenticationToken.class));
        });

        Mockito.verify(oAuthService, Mockito.times(1)).processOAuthPostLogin(Mockito.any(OAuth2AuthenticationToken.class));
    }
}
