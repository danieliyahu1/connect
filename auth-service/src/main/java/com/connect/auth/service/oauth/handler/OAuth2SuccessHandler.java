package com.connect.auth.service.oauth.handler;


import com.connect.auth.dto.AuthResponseDTO;
import com.connect.auth.dto.OAuthResponseDTO;
import com.connect.auth.exception.WrongProviderException;
import com.connect.auth.repository.UserRepository;
import com.connect.auth.service.OAuthService;
import com.connect.auth.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final OAuthService oAuthService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        try {
            OAuthResponseDTO oAuthResponse = oAuthService.processOAuthPostLogin(authToken);
            response.setContentType("application/json");
            setResponseStatus(response, oAuthResponse.isNewUser());
            objectMapper.writeValue(response.getWriter(), oAuthResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (WrongProviderException e) {

            // Set HTTP status to 400 Bad Request (or 409 Conflict, your choice)
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            // Write error message as JSON or plain text
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void setResponseStatus(HttpServletResponse response, boolean isNewUser) {
        response.setStatus(isNewUser ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_OK);
    }
}
