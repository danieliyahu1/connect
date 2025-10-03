package com.akatsuki.auth.service.oauth.extractor;

import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2UserInfoExtractor {
    String getProviderUserId(OAuth2User user);
    String getEmail(OAuth2User user);
}