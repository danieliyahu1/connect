package com.connect.auth.controller;

import com.connect.auth.dto.AuthResponseDTO;
import com.connect.auth.dto.RegisterRequestDTO;
import com.connect.auth.service.AuthService;
import com.connect.auth.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    private static final String URIPREFIX = "/auth";

    @Test
    void register() throws Exception {
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

    private String buildRegisterJson(String email, String password, String confirmedPassword) {
        return """
        {
          "email": "%s",
          "password": "%s",
          "confirmed_password": "%s"
        }
        """.formatted(email, password, confirmedPassword);
    }
}
