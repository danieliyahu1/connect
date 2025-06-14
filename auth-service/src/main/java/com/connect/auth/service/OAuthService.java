package com.connect.auth.service;

import com.connect.auth.dto.AuthResponseDTO;
import com.connect.auth.dto.OAuthResponseDTO;
import com.connect.auth.enums.AuthProvider;
import com.connect.auth.exception.WrongProviderException;
import com.connect.auth.model.RefreshToken;
import com.connect.auth.model.User;
import com.connect.auth.repository.AuthRepository;
import com.connect.auth.service.oauth.extractor.OAuth2UserInfoExtractor;
import com.connect.auth.service.oauth.extractor.OAuth2UserInfoExtractorRegistry;
import com.connect.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthRepository authRepository;
    private final OAuth2UserInfoExtractorRegistry oAuth2UserInfoExtractorRegistry;

    public OAuthResponseDTO processOAuthPostLogin(OAuth2AuthenticationToken oauthToken) throws WrongProviderException {
        OAuth2User oauthUser = oauthToken.getPrincipal();

        AuthProvider provider = AuthProvider.valueOf(oauthToken.getAuthorizedClientRegistrationId().toUpperCase());
        OAuth2UserInfoExtractor extractor = oAuth2UserInfoExtractorRegistry.getExtractor(provider);

        // Extract required details from the OAuth2User
        String email = extractor.getEmail(oauthUser);
        String providerUserId = extractor.getProviderUserId(oauthUser);

        Optional<User> userOpt = userService.findByEmail(email);
        User user;

        if (userOpt.isEmpty()) {
            // User does not exist -> create a new user
            user = new User(email, provider, providerUserId);
            user = userService.save(user);
        } else {
            user = userOpt.get();
            if(!sameProvider(provider.name(), user.getProvider().name()))
            {
                throw new WrongProviderException("User already registered with a different provider");
            }
        }
        return createOAuthResponse(user, userOpt.isPresent());
    }

    private boolean sameProvider(String oauthProvider, String userProvider) {
        return oauthProvider.equalsIgnoreCase(userProvider);
    }

    private OAuthResponseDTO createOAuthResponse(User user, boolean isNewUser){
        String accessToken = jwtUtil.generateAccessToken(user.getUserId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());
        log.info("generated access and refresh tokens for user: {}", user.getUserId());
        log.info("Access Token: {}", accessToken);
        log.info("Refresh Token: {}", refreshToken);

        authRepository.save(new RefreshToken(refreshToken, user, jwtUtil.getIssuedAt(refreshToken), jwtUtil.getExpiration(refreshToken)));

        return new OAuthResponseDTO(accessToken, refreshToken, isNewUser);
    }
}
