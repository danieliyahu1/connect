package com.akatsuki.auth.service;

import com.akatsuki.auth.common.exception.AuthCommonInvalidTokenException;
import com.akatsuki.auth.common.exception.AuthCommonSignatureMismatchException;
import com.akatsuki.auth.dto.OAuthResponseDTO;
import com.akatsuki.auth.enums.AuthProvider;
import com.akatsuki.auth.exception.WrongProviderException;
import com.akatsuki.auth.model.RefreshToken;
import com.akatsuki.auth.model.User;
import com.akatsuki.auth.repository.AuthRepository;
import com.akatsuki.auth.service.oauth.extractor.OAuth2UserInfoExtractor;
import com.akatsuki.auth.service.oauth.extractor.OAuth2UserInfoExtractorRegistry;
import com.akatsuki.auth.common.util.AsymmetricJwtUtil;
import com.akatsuki.auth.service.token.JwtGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final UserService userService;
    private final AsymmetricJwtUtil jwtUtil;
    private final JwtGenerator jwtGenerator;
    private final AuthRepository authRepository;
    private final OAuth2UserInfoExtractorRegistry oAuth2UserInfoExtractorRegistry;

    public OAuthResponseDTO processOAuthPostLogin(OAuth2AuthenticationToken oauthToken) throws WrongProviderException, AuthCommonSignatureMismatchException, AuthCommonInvalidTokenException {
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

    private OAuthResponseDTO createOAuthResponse(User user, boolean isNewUser) throws AuthCommonSignatureMismatchException, AuthCommonInvalidTokenException {
        String accessToken = jwtGenerator.generateAccessToken(user.getUserId());
        String refreshToken = jwtGenerator.generateRefreshToken(user.getUserId());
        log.info("generated access and refresh tokens for user: {}", user.getUserId());
        log.info("Access Token: {}", accessToken);
        log.info("Refresh Token: {}", refreshToken);

        authRepository.save(new RefreshToken(refreshToken, user, jwtUtil.getIssuedAt(refreshToken), jwtUtil.getExpiration(refreshToken)));

        return new OAuthResponseDTO(accessToken, refreshToken, isNewUser);
    }
}
