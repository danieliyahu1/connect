package com.akatsuki.auth.component;

import com.akatsuki.auth.configuration.ComponentTestSecurityConfig;
import com.akatsuki.auth.dto.AuthResponseDTO;
import com.akatsuki.auth.model.RefreshToken;
import com.akatsuki.auth.model.User;
import com.akatsuki.auth.repository.AuthRepository;
import com.akatsuki.auth.repository.UserRepository;
import com.akatsuki.auth.common.util.AsymmetricJwtUtil;
import com.akatsuki.auth.service.token.JwtGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import com.akatsuki.auth.enums.AuthProvider;
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
@Import(ComponentTestSecurityConfig.class)
@ActiveProfiles("component-test")
class AuthServiceComponentTest {

    private String AUTH_PREFIX = "/auth";
    private String PUBLIC_PREFIX = "/public";
    private String INTERNAL_PREFIX = "/internal";
    private String ME_PREFIX = "/me";
    private String BEARER_SPACE_PREFIX = "Bearer ";

    @MockitoBean
    private AuthRepository authRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private AsymmetricJwtUtil jwtUtil;

    @Autowired
    private JwtGenerator jwtGenerator;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl(String path) {
        return "http://localhost:" + port + AUTH_PREFIX + path;
    }

    private String getPublicBaseUrl(String path) {
        return getBaseUrl(PUBLIC_PREFIX + path);
    }

    private String getInternalBaseUrl(String path) {
        return getBaseUrl(INTERNAL_PREFIX + path);
    }

    private String getMeBaseUrl(String path) {
        return getBaseUrl(ME_PREFIX + path);
    }

    // --- /auth/register ---

    @Test
    void register_ValidInput_ReturnsCreated() {
        String url = getPublicBaseUrl("/register");
        String requestJson = """
            {
              "email": "component-test@example.com",
              "password": "password123",
              "confirmedPassword": "password123"
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
        String url = getPublicBaseUrl("/register");
        String requestJson = """
            {
              "email": "existing@example.com",
              "password": "password123",
              "confirmedPassword": "password123"
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
        String url = getPublicBaseUrl("/register");
        String requestJson = """
            {
              "email": "component-test@example.com",
              "password": "password123",
              "confirmedPassword": "differentPassword"
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
        String url = getPublicBaseUrl("/login");
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
        String url = getPublicBaseUrl("/login");
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
        String url = getPublicBaseUrl("/refresh");
        String token = jwtGenerator.generateRefreshToken(UUID.randomUUID());
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
        String url = getPublicBaseUrl("/refresh");
        HttpHeaders headers = new HttpHeaders();
        String invalidRefreshToken = jwtGenerator.generateAccessToken(UUID.randomUUID());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.COOKIE, "refreshToken="+invalidRefreshToken);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }


    // --- /auth/logout ---

    @Test
    void logout_ValidAccessToken_ReturnsNoContent() {
        String url = getMeBaseUrl("/logout");
        String accessToken = jwtGenerator.generateAccessToken(UUID.randomUUID());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", BEARER_SPACE_PREFIX + accessToken);
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
        String url = getMeBaseUrl("/logout");
        String invalidAccessToken = jwtGenerator.generateRefreshToken(UUID.randomUUID());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", BEARER_SPACE_PREFIX + invalidAccessToken);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void logout_userNotFound_ReturnsUnauthorized() {
        String url = getMeBaseUrl("/logout");
        String accessToken = jwtGenerator.generateAccessToken(UUID.randomUUID());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", BEARER_SPACE_PREFIX + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        when(userRepository.getUserByUserId(any(UUID.class))).thenReturn(Optional.empty());

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // --- /auth/deleteUser ---

    @Test
    void deleteUser_ValidAccessToken_ReturnsNoContent() {
        String url = getInternalBaseUrl("/deleteUser");
        String accessToken = jwtGenerator.generateAccessToken(UUID.randomUUID());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", BEARER_SPACE_PREFIX + accessToken);
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
        String url = getInternalBaseUrl("/deleteUser");
        String invalidAccessToken = jwtGenerator.generateRefreshToken(UUID.randomUUID());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", BEARER_SPACE_PREFIX + invalidAccessToken);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void deleteUser_UserNotFound_ReturnsNoContent() {
        String url = getInternalBaseUrl("/deleteUser");
        String accessToken = jwtGenerator.generateAccessToken(UUID.randomUUID());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", BEARER_SPACE_PREFIX + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        when(userRepository.getUserByUserId(any(UUID.class))).thenReturn(Optional.empty());

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}