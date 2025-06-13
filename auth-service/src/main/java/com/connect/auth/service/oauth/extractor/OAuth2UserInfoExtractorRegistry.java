package com.connect.auth.service.oauth.extractor;

import com.connect.auth.enums.AuthProvider;
import com.connect.auth.exception.UnsupportedProviderException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2UserInfoExtractorRegistry {

    private final Map<AuthProvider, OAuth2UserInfoExtractor> extractorMap;

    public OAuth2UserInfoExtractor getExtractor(AuthProvider provider) {
        if (!extractorMap.containsKey(provider)) {
            throw new UnsupportedProviderException("Unsupported provider: " + provider);
        }
        return extractorMap.get(provider);
    }
}