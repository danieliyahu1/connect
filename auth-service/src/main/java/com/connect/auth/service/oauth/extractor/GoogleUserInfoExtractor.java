package com.akatsuki.auth.service.oauth.extractor;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class GoogleUserInfoExtractor implements OAuth2UserInfoExtractor {

    @Override
    public String getProviderUserId(OAuth2User user) {
        return user.getAttribute("sub");
    }

    @Override
    public String getEmail(OAuth2User user) {
        return user.getAttribute("email");
    }
}
