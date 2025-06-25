package com.connect.auth.controller;

import com.connect.auth.common.exception.AuthCommonInvalidRefreshTokenException;
import com.connect.auth.common.exception.AuthCommonUnauthorizedException;
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
import com.connect.auth.configuration.TestSecurityConfig;
import com.connect.auth.dto.AuthResponseDTO;
import com.connect.auth.dto.LoginRequestDTO;
import com.connect.auth.dto.RegisterRequestDTO;
import com.connect.auth.exception.PasswordNotMatchException;
import com.connect.auth.exception.UserExistException;
import com.connect.auth.service.AuthService;
import com.connect.auth.service.UserService;
import com.connect.auth.common.util.JwtUtil;

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
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserService userService;

    private static final String URIPREFIX = "/auth";
    private String PUBLICPREFIX = "/public";
    private String INTERNALPREFIX = "/internal";

    //---------------------------------register tests---------------------------------

    @Test
    void register_ValidInput_Successful() throws Exception {
        String registerJson = buildRegisterJson("naruto@gmail.com", "password", "password");

        AuthResponseDTO response = new AuthResponseDTO("accessToken", "refreshToken");

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post(URIPREFIX + PUBLICPREFIX + "/register")
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

        mockMvc.perform(post(URIPREFIX + PUBLICPREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isConflict());
    }

    @Test
    void register_missingEmail_returnsBadRequest() throws Exception {
        String registerJson = buildRegisterJson("", "password123", "password123");

        mockMvc.perform(post(URIPREFIX + PUBLICPREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_missingPassword_returnsBadRequest() throws Exception {
        String registerJson = buildRegisterJson("user@example.com", "", "password123");

        mockMvc.perform(post(URIPREFIX + PUBLICPREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_missingConfirmedPassword_returnsBadRequest() throws Exception {
        String registerJson = buildRegisterJson("user@example.com", "password123", "");

        mockMvc.perform(post(URIPREFIX + PUBLICPREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_invalidEmailFormat_returnsBadRequest() throws Exception {
        String registerJson = buildRegisterJson("invalid-email", "password123", "password123");

        mockMvc.perform(post(URIPREFIX + PUBLICPREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordsDoNotMatch_throwsPasswordNotMatchException() throws Exception {
        String registerJson = buildRegisterJson("user@example.com", "password123", "different123");

        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new PasswordNotMatchException("Passwords do not match"));

        mockMvc.perform(post(URIPREFIX + PUBLICPREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordTooShort_returnsBadRequest() throws Exception {
        String registerJson = buildRegisterJson("user@example.com", "short", "validPassword");

        mockMvc.perform(post(URIPREFIX + PUBLICPREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_confirmedPasswordTooShort_returnsBadRequest() throws Exception {
        String registerJson = buildRegisterJson("user@example.com", "validPassword", "short");

        mockMvc.perform(post(URIPREFIX + PUBLICPREFIX + "/register")
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

        mockMvc.perform(post(URIPREFIX + PUBLICPREFIX + "/login")
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

        mockMvc.perform(post(URIPREFIX + PUBLICPREFIX + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_missingEmail_returnsBadRequest() throws Exception {
        String loginJson = buildLoginJson("", "validPassword");

        mockMvc.perform(post(URIPREFIX + PUBLICPREFIX + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_invalidEmailFormat_returnsBadRequest() throws Exception {
        String loginJson = buildLoginJson("not-an-email", "validPassword");

        mockMvc.perform(post(URIPREFIX + PUBLICPREFIX + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_missingPassword_returnsBadRequest() throws Exception {
        String loginJson = buildLoginJson("user@example.com", "");

        mockMvc.perform(post(URIPREFIX + PUBLICPREFIX + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_passwordTooShort_returnsBadRequest() throws Exception {
        String loginJson = buildLoginJson("user@example.com", "short");

        mockMvc.perform(post(URIPREFIX + PUBLICPREFIX + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest());
    }


    //---------------------------------refresh tests---------------------------------
    @Test
    void refresh_ValidCookie_Ok() throws Exception {
        AuthResponseDTO response = new AuthResponseDTO("accessToken", "newRefreshToken");
        when(authService.refresh(eq("refreshTokenValue"))).thenReturn(response);

        mockMvc.perform(post(URIPREFIX + PUBLICPREFIX + "/refresh")
                        .cookie(new Cookie("refreshToken", "refreshTokenValue")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("newRefreshToken"));
    }

    @Test
    void refresh_MissingCookie_BadRequest() throws Exception {
        mockMvc.perform(post(URIPREFIX + PUBLICPREFIX + "/refresh"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_InvalidToken_NotFound() throws Exception {
        when(authService.refresh(any(String.class)))
                .thenThrow(new AuthCommonInvalidRefreshTokenException("Invalid refresh token"));

        mockMvc.perform(post(URIPREFIX + PUBLICPREFIX + "/refresh")
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
            mockMvc.perform(post(URIPREFIX + INTERNALPREFIX + "/logout")
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
            mockMvc.perform(post(URIPREFIX + INTERNALPREFIX + "/logout")
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
            mockMvc.perform(delete(URIPREFIX + INTERNALPREFIX + "/deleteUser")
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
            mockMvc.perform(delete(URIPREFIX + INTERNALPREFIX + "/deleteUser")
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
          "confirmed_password": "%s"
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
