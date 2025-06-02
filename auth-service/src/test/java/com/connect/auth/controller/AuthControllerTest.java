package com.connect.auth.controller;

import com.connect.auth.configuration.TestSecurityConfig;
import com.connect.auth.dto.AuthResponseDTO;
import com.connect.auth.dto.LoginRequestDTO;
import com.connect.auth.dto.RegisterRequestDTO;
import com.connect.auth.exception.*;
import com.connect.auth.service.AuthService;
import com.connect.auth.service.UserService;
import com.connect.auth.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    //---------------------------------register tests---------------------------------

    @Test
    void register_ValidInput_Successful() throws Exception {
        String registerJson = buildRegisterJson("naruto@gmail.com", "password", "password");

        AuthResponseDTO response = new AuthResponseDTO("accessToken", "refreshToken");

        Mockito.when(authService.register(any(RegisterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post(URIPREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));

    }

    @Test
    void register_userAlreadyExists_throwsUserExistException() throws Exception {
        String registerJson = buildRegisterJson("naruto@gmail.com", "password", "password");
        Mockito.when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new UserExistException("User already exists"));

        mockMvc.perform(post(URIPREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isConflict());
    }

    @Test
    void register_missingEmail_returnsBadRequest() throws Exception {
        String registerJson = buildRegisterJson("", "password123", "password123");

        mockMvc.perform(post(URIPREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_missingPassword_returnsBadRequest() throws Exception {
        String registerJson = buildRegisterJson("user@example.com", "", "password123");

        mockMvc.perform(post(URIPREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_missingConfirmedPassword_returnsBadRequest() throws Exception {
        String registerJson = buildRegisterJson("user@example.com", "password123", "");

        mockMvc.perform(post(URIPREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_invalidEmailFormat_returnsBadRequest() throws Exception {
        String registerJson = buildRegisterJson("invalid-email", "password123", "password123");

        mockMvc.perform(post(URIPREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordsDoNotMatch_throwsPasswordNotMatchException() throws Exception {
        String registerJson = buildRegisterJson("user@example.com", "password123", "different123");

        Mockito.when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new PasswordNotMatchException("Passwords do not match"));

        mockMvc.perform(post(URIPREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordTooShort_returnsBadRequest() throws Exception {
        String registerJson = buildRegisterJson("user@example.com", "short", "validPassword");

        mockMvc.perform(post(URIPREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_confirmedPasswordTooShort_returnsBadRequest() throws Exception {
        String registerJson = buildRegisterJson("user@example.com", "validPassword", "short");

        mockMvc.perform(post(URIPREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }


    //---------------------------------login tests---------------------------------

    @Test
    void login_ValidCredentials_Ok() throws Exception {
        String loginJson = buildLoginJson("naruto@gmail.com", "password");
        AuthResponseDTO response = new AuthResponseDTO("accessToken", "refreshToken");
        Mockito.when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post(URIPREFIX + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
    }

    @Test
    void login_InvalidCredentials_Unauthorized() throws Exception {
        String loginJson = buildLoginJson("naruto@gmail.com", "wrongpassword");
        Mockito.when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new UnauthorizedException("Invalid credentials"));

        mockMvc.perform(post(URIPREFIX + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_missingEmail_returnsBadRequest() throws Exception {
        String loginJson = buildLoginJson("", "validPassword");

        mockMvc.perform(post(URIPREFIX + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_invalidEmailFormat_returnsBadRequest() throws Exception {
        String loginJson = buildLoginJson("not-an-email", "validPassword");

        mockMvc.perform(post(URIPREFIX + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_missingPassword_returnsBadRequest() throws Exception {
        String loginJson = buildLoginJson("user@example.com", "");

        mockMvc.perform(post(URIPREFIX + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_passwordTooShort_returnsBadRequest() throws Exception {
        String loginJson = buildLoginJson("user@example.com", "short");

        mockMvc.perform(post(URIPREFIX + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest());
    }


    //---------------------------------refresh tests---------------------------------
    @Test
    void refresh_ValidCookie_Ok() throws Exception {
        AuthResponseDTO response = new AuthResponseDTO("accessToken", "newRefreshToken");
        Mockito.when(authService.refresh(eq("refreshTokenValue"))).thenReturn(response);

        mockMvc.perform(post(URIPREFIX + "/refresh")
                        .cookie(new Cookie("refreshToken", "refreshTokenValue")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("newRefreshToken"));
    }

    @Test
    void refresh_MissingCookie_BadRequest() throws Exception {
        mockMvc.perform(post(URIPREFIX + "/refresh"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_InvalidToken_NotFound() throws Exception {
        Mockito.when(authService.refresh(any(String.class)))
                .thenThrow(new InvalidRefreshTokenException("Invalid refresh token"));

        mockMvc.perform(post(URIPREFIX + "/refresh")
                        .cookie(new Cookie("refreshToken", "invalid")))
                .andExpect(status().isUnauthorized());
    }


    //---------------------------------logout tests---------------------------------

    @Test
    void logout_ValidHeader_NoContent() throws Exception {
        Mockito.doNothing().when(authService).logout(any(String.class));

        mockMvc.perform(post(URIPREFIX + "/logout")
                        .header("Authorization", "Bearer accessToken"))
                .andExpect(status().isNoContent());
    }

    @Test
    void logout_MissingHeader_BadRequest() throws Exception {
        mockMvc.perform(post(URIPREFIX + "/logout"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logout_UserNotFound_ReturnsNotFound() throws Exception {
        Mockito.doThrow(new UserNotFoundException("User not found"))
                .when(authService).logout(Mockito.anyString());

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer accessToken"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User Not Found"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    //--------------------------------deleteUser tests---------------------------------
//
//    @Test
//    void deleteUser_ValidAuthentication_NoContent() throws Exception {
//        String userId = UUID.randomUUID().toString();
//        Mockito.doNothing().when(authService).deleteUserByUserId(userId);
//
//        Authentication authenticationMock = Mockito.mock(Authentication.class);
//        Mockito.when(authenticationMock.getName()).thenReturn(userId);
//
//        mockMvc.perform(delete(URIPREFIX + "/deleteUser")
//                        .with(mockAuthentication(userId)))
//                .andExpect(status().isNoContent());
//    }
//
//    @Test
//    void deleteUser_ValidAuthentication_NoContent2() throws Exception {
//        Authentication authentication = Mockito.mock(Authentication.class);
//        Mockito.when(authentication.getName()).thenReturn("someUserEmail@example.com");
//
//        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
//        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
//
//        SecurityContextHolder.setContext(securityContext);
//
//        Mockito.doNothing().when(authService).deleteUserByUserId("someUserEmail@example.com");
//
//        mockMvc.perform(delete(URIPREFIX + "/deleteUser"))
//                .andExpect(status().isNoContent());
//
//        // Optional cleanup
//        SecurityContextHolder.clearContext();
//    }
//
//    @Test
//    void deleteUser_ValidAuthentication_NoContent3() throws Exception {
//        Mockito.doNothing().when(authService).deleteUserByUserId("test-user-id");
//
//        mockMvc.perform(delete(URIPREFIX + "/deleteUser"))
//                .andExpect(status().isNoContent());
//    }
//
//    @Test
//    void deleteUser_WithValidJwtToken_NoContent4() throws Exception {
//        // Arrange: the userId that JwtUtil will extract from the token
//        String userId = "test-user-id";
//        String jwtToken = "valid.jwt.token"; // This should be a token your JwtUtil accepts as valid
//
//        Mockito.doNothing().when(authService).deleteUserByUserId(userId);
//
//        mockMvc.perform(delete(URIPREFIX + "/deleteUser")
//                        .header("Authorization", "Bearer " + jwtToken))
//                .andExpect(status().isNoContent());
//    }

    //---------------------------------getUserIdFromAccessToken tests---------------------------------


    //----------------------------------validAuthentication---------------------------------


    //---------------------------------Helper Methods---------------------------------

    private RequestPostProcessor mockAuthentication(String userId) {
        return request -> {
            var auth = Mockito.mock(Authentication.class);
            Mockito.when(auth.getName()).thenReturn(userId);

            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            return request;
        };
    }

    private RequestPostProcessor authenticationPrincipal(String userId) {
        return request -> {
            Authentication authentication = Mockito.mock(Authentication.class);
            Mockito.when(authentication.getName()).thenReturn(userId);
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            return request;
        };
    }

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
