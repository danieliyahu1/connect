package com.connect.auth.component;

import com.connect.auth.configuration.ComponentTestConfig;
import com.connect.auth.dto.AuthResponseDTO;
import com.connect.auth.model.RefreshToken;
import com.connect.auth.model.User;
import com.connect.auth.repository.AuthRepository;
import com.connect.auth.repository.UserRepository;
import com.connect.auth.util.JwtUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import com.connect.auth.enums.AuthProvider;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

@Import(ComponentTestConfig.class)
@ActiveProfiles("component-test")
class AuthServiceComponentTest {

    private String publicPrefix = "/auth/public";
    private String internalPrefix = "/auth/internal";
    private String bearerPrefix = "Bearer ";

    @MockitoBean
    private AuthRepository authRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl(String path) {
        return "http://localhost:" + port + path;
    }


    // --- /auth/register ---

    @Test
    void register_ValidInput_ReturnsCreated() {
        String url = getBaseUrl(publicPrefix + "/register");
        String requestJson = """
            {
              "email": "component-test@example.com",
              "password": "password123",
              "confirmed_password": "password123"
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        User user = new User(
                "component-test@example.com",
                "encodedPassword123",
                AuthProvider.LOCAL
        );
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userRepository.findByEmail("component-test@example.com")).thenReturn(Optional.empty());

        ResponseEntity<AuthResponseDTO> response = restTemplate.postForEntity(url, entity, AuthResponseDTO.class);
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void register_ExistingEmail_ReturnsConflict() {
        String url = getBaseUrl(publicPrefix + "/register");
        String requestJson = """
            {
              "email": "existing@example.com",
              "password": "password123",
              "confirmed_password": "password123"
            }
            """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void register_PasswordsDoNotMatch_ReturnsBadRequest() {
        String url = getBaseUrl(publicPrefix + "/register");
        String requestJson = """
            {
              "email": "component-test@example.com",
              "password": "password123",
              "confirmed_password": "differentPassword"
            }
            """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        when(userRepository.findByEmail("component-test@example.com")).thenReturn(Optional.empty());

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    // --- /auth/login ---

    @Test
    void login_ValidCredentials_ReturnsOkAndTokens() {
        String url = getBaseUrl(publicPrefix + "/login");
        String requestJson = """
            {
              "email": "component-test@example.com",
              "password": "password123"
            }
            """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
        String encodedPassword = new BCryptPasswordEncoder().encode("password123");

        User user = new User("component-test@example.com", encodedPassword, AuthProvider.LOCAL);
        when(userRepository.findByEmail("component-test@example.com")).thenReturn(Optional.of(user));
        // Simulate password match
        when(userRepository.save(any(User.class))).thenReturn(user);

        ResponseEntity<AuthResponseDTO> response = restTemplate.postForEntity(url, entity, AuthResponseDTO.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertNotNull(response.getBody().getAccessToken());
        Assertions.assertNotNull(response.getBody().getRefreshToken());
    }

    @Test
    void login_InvalidCredentials_ReturnsUnauthorized() {
        String url = getBaseUrl(publicPrefix + "/login");
        String requestJson = """
            {
              "email": "component-test@example.com",
              "password": "wrongpassword"
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        when(userRepository.findByEmail("component-test@example.com")).thenReturn(Optional.empty());

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(userRepository, times(1)).findByEmail("component-test@example.com");
    }


    // --- /auth/refresh ---

    @Test
    void refresh_ValidRefreshToken_ReturnsNewTokens() {
        String url = getBaseUrl(publicPrefix + "/refresh");
        String token = jwtUtil.generateRefreshToken(UUID.randomUUID());
        User mockUser = new User("component@test.com", AuthProvider.LOCAL, "providerUserId123");
        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken(token);
        mockRefreshToken.setUser(mockUser);
        Optional<RefreshToken> optionalMockRefreshToken = Optional.of(mockRefreshToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.add(HttpHeaders.COOKIE, "refreshToken=" + token); // Set cookie name and value
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        // Mocking would be needed for jwtUtil and repository, omitted for brevity
        when(authRepository.findByToken(any())).thenReturn(optionalMockRefreshToken);
        ResponseEntity<AuthResponseDTO> response = restTemplate.postForEntity(url, entity, AuthResponseDTO.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void refresh_InvalidRefreshToken_ReturnsUnauthorized() {
        String url = getBaseUrl(publicPrefix + "/refresh");
        HttpHeaders headers = new HttpHeaders();
        String invalidRefreshToken = jwtUtil.generateAccessToken(UUID.randomUUID());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.COOKIE, "refreshToken="+invalidRefreshToken);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }


    // --- /auth/logout ---

    @Test
    void logout_ValidAccessToken_ReturnsNoContent() {
        String url = getBaseUrl(internalPrefix + "/logout");
        String accessToken = jwtUtil.generateAccessToken(UUID.randomUUID());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerPrefix + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(UUID.randomUUID());

        when(userRepository.getUserByUserId(any(UUID.class))).thenReturn(Optional.of(mockUser));
        doNothing().when(authRepository).deleteByUser_Id(any(UUID.class));
        ResponseEntity<Void> response = restTemplate.postForEntity(url, entity, Void.class);
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void logout_InvalidAccessToken_ReturnsUnauthorized() {
        String url = getBaseUrl("/auth/logout");
        String invalidAccessToken = jwtUtil.generateRefreshToken(UUID.randomUUID());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerPrefix + invalidAccessToken);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void logout_userNotFound_ReturnsUnauthorized() {
        String url = getBaseUrl(internalPrefix + "/logout");
        String accessToken = jwtUtil.generateAccessToken(UUID.randomUUID());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerPrefix + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        when(userRepository.getUserByUserId(any(UUID.class))).thenReturn(Optional.empty());

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // --- /auth/deleteUser ---

    @Test
    void deleteUser_ValidAccessToken_ReturnsNoContent() {
        String url = getBaseUrl(internalPrefix + "/deleteUser");
        String accessToken = jwtUtil.generateAccessToken(UUID.randomUUID());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerPrefix + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(UUID.randomUUID());

        when(userRepository.getUserByUserId(any(UUID.class))).thenReturn(Optional.of(mockUser));
        doNothing().when(authRepository).deleteByUser_Id(any(UUID.class));
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deleteUser_InvalidAccessToken_ReturnsUnauthorized() {
        String url = getBaseUrl(internalPrefix + "/deleteUser");
        String invalidAccessToken = jwtUtil.generateRefreshToken(UUID.randomUUID());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerPrefix + invalidAccessToken);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void deleteUser_UserNotFound_ReturnsNoContent() {
        String url = getBaseUrl(internalPrefix + "/deleteUser");
        String accessToken = jwtUtil.generateAccessToken(UUID.randomUUID());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerPrefix + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        when(userRepository.getUserByUserId(any(UUID.class))).thenReturn(Optional.empty());

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}