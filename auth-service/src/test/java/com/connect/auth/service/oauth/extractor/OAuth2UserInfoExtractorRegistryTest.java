package com.akatsuki.auth.service.oauth.extractor;

import com.akatsuki.auth.enums.AuthProvider;
import com.akatsuki.auth.exception.UnsupportedProviderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class OAuth2UserInfoExtractorRegistryTest {

    @Mock
    private OAuth2UserInfoExtractor googleUserInfoExtractor;

    private OAuth2UserInfoExtractorRegistry oAuth2UserInfoExtractorRegistry;

    @BeforeEach
    void setup() {
        Map<AuthProvider, OAuth2UserInfoExtractor> map = new HashMap<>();
        map.put(AuthProvider.GOOGLE, googleUserInfoExtractor);
        oAuth2UserInfoExtractorRegistry = new OAuth2UserInfoExtractorRegistry(map);
    }

    @Test
    void getExtractor_shouldReturnCorrectExtractorForSupportedProvider() {
        OAuth2UserInfoExtractor retrievedGoogleExtractor = oAuth2UserInfoExtractorRegistry.getExtractor(AuthProvider.GOOGLE);
        assertNotNull(retrievedGoogleExtractor);
        assertEquals(googleUserInfoExtractor, retrievedGoogleExtractor);
    }

    @Test
    void getExtractor_shouldThrowExceptionForUnsupportedProvider() {
        AuthProvider unsupportedProvider = AuthProvider.FACEBOOK;
        UnsupportedProviderException thrown = assertThrows(UnsupportedProviderException.class, () -> {
            oAuth2UserInfoExtractorRegistry.getExtractor(unsupportedProvider);
        });

        assertEquals("Unsupported provider: " + unsupportedProvider, thrown.getMessage());
    }

    @Test
    void getExtractor_shouldHandleEmptyMap() {
        oAuth2UserInfoExtractorRegistry = new OAuth2UserInfoExtractorRegistry(new HashMap<>());

        AuthProvider anyProvider = AuthProvider.GOOGLE;
        UnsupportedProviderException thrown = assertThrows(UnsupportedProviderException.class, () -> {
            oAuth2UserInfoExtractorRegistry.getExtractor(anyProvider);
        });

        assertEquals("Unsupported provider: " + anyProvider, thrown.getMessage());
    }
}
