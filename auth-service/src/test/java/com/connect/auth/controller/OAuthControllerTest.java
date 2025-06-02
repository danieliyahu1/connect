package com.connect.auth.controller;

import com.connect.auth.dto.AuthResponseDTO;
import com.connect.auth.exception.WrongProviderException;
import com.connect.auth.service.OAuthService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = OAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OAuthService oAuthService;

    @Test
    void oauth2Success_ValidAuthentication_ReturnsAuthResponse() throws Exception {
        AuthResponseDTO response = new AuthResponseDTO("accessToken", "refreshToken");
        Mockito.when(oAuthService.processOAuthPostLogin(any(OAuth2AuthenticationToken.class)))
                .thenReturn(response);

        mockMvc.perform(get("/oauth2/success")
                        .principal(Mockito.mock(OAuth2AuthenticationToken.class)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
    }

    @Test
    void oauth2Success_WrongProviderException_ReturnsBadRequest() throws Exception {
        Mockito.when(oAuthService.processOAuthPostLogin(any(OAuth2AuthenticationToken.class)))
                .thenThrow(new WrongProviderException("Wrong provider"));

        mockMvc.perform(get("/oauth2/success")
                        .principal(Mockito.mock(OAuth2AuthenticationToken.class)))
                .andExpect(status().isBadRequest());
    }
}