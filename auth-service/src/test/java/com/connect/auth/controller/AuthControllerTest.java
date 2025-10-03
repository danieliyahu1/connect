package com.akatsuki.auth.controller;

import com.akatsuki.auth.common.exception.AuthCommonInvalidRefreshTokenException;
import com.akatsuki.auth.common.exception.AuthCommonUnauthorizedException;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.akatsuki.auth.configuration.TestSecurityConfig;
import com.akatsuki.auth.dto.AuthResponseDTO;
import com.akatsuki.auth.dto.LoginRequestDTO;
import com.akatsuki.auth.dto.RegisterRequestDTO;
import com.akatsuki.auth.exception.PasswordNotMatchException;
import com.akatsuki.auth.exception.UserExistException;
import com.akatsuki.auth.service.AuthService;
import com.akatsuki.auth.service.UserService;
import com.akatsuki.auth.common.util.AsymmetricJwtUtil;

import jakarta.servlet.http.Cookie;

@WebMvcTest(value = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AsymmetricJwtUtil jwtUtil;

    @MockitoBean
    private UserService userService;

    private static final String AUTH_PREFIX = "/auth";
    private String PUBLIC_PREFIX = "/public";
    private String INTERNAL_PREFIX = "/internal";
    public String ME_PREFIX = "/me";

    //---------------------------------register tests---------------------------------

    @Test
    void register_ValidInput_Successful() throws Exception {
        String registerJson = buildRegisterJson("naruto@gmail.com", "password", "password");

        AuthResponseDTO response = new AuthResponseDTO("accessToken", "refreshToken");

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post(AUTH_PREFIX + PUBLIC_PREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));

    }

    @Test
    void register_userAlreadyExists_throwsUserExistException() throws Exception {
        String registerJson = buildRegisterJson("naruto@gmail.com", "password", "password");
        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new UserExistException("User already exists"));

        mockMvc.perform(post(AUTH_PREFIX + PUBLIC_PREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isConflict());
    }

    @Test
    void register_missingEmail_returnsBadRequest() throws Exception {
        String registerJson = buildRegisterJson("", "password123", "password123");

        mockMvc.perform(post(AUTH_PREFIX + PUBLIC_PREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_missingPassword_returnsBadRequest() throws Exception {
        String registerJson = buildRegisterJson("user@example.com", "", "password123");

        mockMvc.perform(post(AUTH_PREFIX + PUBLIC_PREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_missingConfirmedPassword_returnsBadRequest() throws Exception {
        String registerJson = buildRegisterJson("user@example.com", "password123", "");

        mockMvc.perform(post(AUTH_PREFIX + PUBLIC_PREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_invalidEmailFormat_returnsBadRequest() throws Exception {
        String registerJson = buildRegisterJson("invalid-email", "password123", "password123");

        mockMvc.perform(post(AUTH_PREFIX + PUBLIC_PREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordsDoNotMatch_throwsPasswordNotMatchException() throws Exception {
        String registerJson = buildRegisterJson("user@example.com", "password123", "different123");

        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new PasswordNotMatchException("Passwords do not match"));

        mockMvc.perform(post(AUTH_PREFIX + PUBLIC_PREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordTooShort_returnsBadRequest() throws Exception {
        String registerJson = buildRegisterJson("user@example.com", "short", "validPassword");

        mockMvc.perform(post(AUTH_PREFIX + PUBLIC_PREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_confirmedPasswordTooShort_returnsBadRequest() throws Exception {
        String registerJson = buildRegisterJson("user@example.com", "validPassword", "short");

        mockMvc.perform(post(AUTH_PREFIX + PUBLIC_PREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }


    //---------------------------------login tests---------------------------------

    @Test
    void login_ValidCredentials_Ok() throws Exception {
        String loginJson = buildLoginJson("naruto@gmail.com", "password");
        AuthResponseDTO response = new AuthResponseDTO("accessToken", "refreshToken");
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post(AUTH_PREFIX + PUBLIC_PREFIX + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
    }

    @Test
    void login_InvalidCredentials_Unauthorized() throws Exception {
        String loginJson = buildLoginJson("naruto@gmail.com", "wrongpassword");
        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new AuthCommonUnauthorizedException("Invalid credentials"));

        mockMvc.perform(post(AUTH_PREFIX + PUBLIC_PREFIX + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_missingEmail_returnsBadRequest() throws Exception {
        String loginJson = buildLoginJson("", "validPassword");

        mockMvc.perform(post(AUTH_PREFIX + PUBLIC_PREFIX + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_invalidEmailFormat_returnsBadRequest() throws Exception {
        String loginJson = buildLoginJson("not-an-email", "validPassword");

        mockMvc.perform(post(AUTH_PREFIX + PUBLIC_PREFIX + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_missingPassword_returnsBadRequest() throws Exception {
        String loginJson = buildLoginJson("user@example.com", "");

        mockMvc.perform(post(AUTH_PREFIX + PUBLIC_PREFIX + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_passwordTooShort_returnsBadRequest() throws Exception {
        String loginJson = buildLoginJson("user@example.com", "short");

        mockMvc.perform(post(AUTH_PREFIX + PUBLIC_PREFIX + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest());
    }


    //---------------------------------refresh tests---------------------------------
    @Test
    void refresh_ValidCookie_Ok() throws Exception {
        AuthResponseDTO response = new AuthResponseDTO("accessToken", "newRefreshToken");
        when(authService.refresh(eq("refreshTokenValue"))).thenReturn(response);

        mockMvc.perform(post(AUTH_PREFIX + PUBLIC_PREFIX + "/refresh")
                        .cookie(new Cookie("refreshToken", "refreshTokenValue")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("newRefreshToken"));
    }

    @Test
    void refresh_MissingCookie_BadRequest() throws Exception {
        mockMvc.perform(post(AUTH_PREFIX + PUBLIC_PREFIX + "/refresh"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_InvalidToken_NotFound() throws Exception {
        when(authService.refresh(any(String.class)))
                .thenThrow(new AuthCommonInvalidRefreshTokenException("Invalid refresh token"));

        mockMvc.perform(post(AUTH_PREFIX + PUBLIC_PREFIX + "/refresh")
                        .cookie(new Cookie("refreshToken", "invalid")))
                .andExpect(status().isUnauthorized());
    }


    //---------------------------------logout tests---------------------------------

    @Test
    void logout_ValidHeader_NoContent() throws Exception {
        String userId = "123e4567-e89b-12d3-a456-426614174000";

        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn(userId);
        when(authentication.isAuthenticated()).thenReturn(false);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        Mockito.doNothing().when(authService).logout(any(String.class));

        try {
            mockMvc.perform(post(AUTH_PREFIX + ME_PREFIX + "/logout")
                            .with(request -> {
                                request.setUserPrincipal(authentication);
                                return request;
                            }))
                    .andExpect(status().isNoContent());

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void logout_UserNotFound_ReturnsNotFound() throws Exception {
        String userId = "123e4567-e89b-12d3-a456-426614174000";

        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn(userId);
        when(authentication.isAuthenticated()).thenReturn(false);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        doThrow(new AuthCommonUnauthorizedException("User not authenticated"))
                .when(authService).logout(Mockito.anyString());

        try {
            mockMvc.perform(post(AUTH_PREFIX + ME_PREFIX + "/logout")
                            .with(request -> {
                                request.setUserPrincipal(authentication);
                                return request;
                            }))
                    .andExpect(status().isUnauthorized());

        } finally {
            SecurityContextHolder.clearContext();
        }
    }


    //---------------------------------deleteUser tests---------------------------------
    @Test
    void deleteUser_ValidUser_Successful() throws Exception {
        String userId = "123e4567-e89b-12d3-a456-426614174000";

        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn(userId);
        when(authentication.isAuthenticated()).thenReturn(true);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        Mockito.doNothing().when(authService).deleteUserByUserId(userId);

        try {
            mockMvc.perform(delete(AUTH_PREFIX + INTERNAL_PREFIX + "/deleteUser")
                            .with(request -> {
                                request.setUserPrincipal(authentication);
                                return request;
                            }))
                    .andExpect(status().isNoContent());

            Mockito.verify(authService).deleteUserByUserId(userId);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void deleteUser_NotAuthenticated_ReturnsUnauthorized() throws Exception {
        String userId = "123e4567-e89b-12d3-a456-426614174000";

        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn(userId);
        when(authentication.isAuthenticated()).thenReturn(false);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        doThrow(new AuthCommonUnauthorizedException("User not authenticated"))
                .when(authService).deleteUserByUserId(userId);
        try {
            mockMvc.perform(delete(AUTH_PREFIX + INTERNAL_PREFIX + "/deleteUser")
                            .with(request -> {
                                request.setUserPrincipal(authentication);
                                return request;
                            })
                            .with(request -> {
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                return request;
                            }))
                    .andExpect(status().isUnauthorized());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }


    //---------------------------------Helper Methods---------------------------------

    private String buildRegisterJson(String email, String password, String confirmedPassword) {
        return """
        {
          "email": "%s",
          "password": "%s",
          "confirmedPassword": "%s"
        }
        """.formatted(email, password, confirmedPassword);
    }

    private String buildLoginJson(String email, String password) {
        return """
        {
          "email": "%s",
          "password": "%s"
        }
        """.formatted(email, password);
    }
}
