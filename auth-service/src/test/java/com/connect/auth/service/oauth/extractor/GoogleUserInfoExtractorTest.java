package com.akatsuki.auth.service.oauth.extractor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GoogleUserInfoExtractorTest {

    @InjectMocks
    private GoogleUserInfoExtractor googleUserInfoExtractor;

    @Mock
    private OAuth2User mockOAuth2User;

    @Test
    void getProviderUserId_shouldReturnSubAttribute() {
        String expectedSub = "1234567890abcdef";
        when(mockOAuth2User.getAttribute("sub")).thenReturn(expectedSub);

        String actualSub = googleUserInfoExtractor.getProviderUserId(mockOAuth2User);

        assertEquals(expectedSub, actualSub, "Provider user ID should match the 'sub' attribute.");
    }

    @Test
    void getEmail_shouldReturnEmailAttribute() {
        String expectedEmail = "testuser@example.com";
        when(mockOAuth2User.getAttribute("email")).thenReturn(expectedEmail);

        String actualEmail = googleUserInfoExtractor.getEmail(mockOAuth2User);

        assertEquals(expectedEmail, actualEmail, "Email should match the 'email' attribute.");
    }

    @Test
    void getProviderUserId_shouldReturnNullIfSubAttributeMissing() {
        when(mockOAuth2User.getAttribute("sub")).thenReturn(null);

        String actualSub = googleUserInfoExtractor.getProviderUserId(mockOAuth2User);

        assertNull(actualSub, "Provider user ID should be null if 'sub' attribute is missing.");
    }

    @Test
    void getEmail_shouldReturnNullIfEmailAttributeMissing() {
        when(mockOAuth2User.getAttribute("email")).thenReturn(null);

        String actualEmail = googleUserInfoExtractor.getEmail(mockOAuth2User);

        assertNull(actualEmail, "Email should be null if 'email' attribute is missing.");
    }

    @Test
    void getProviderUserId_withOtherAttributesPresent() {
        String expectedSub = "another-google-id";
        when(mockOAuth2User.getAttribute("sub")).thenReturn(expectedSub);

        String actualSub = googleUserInfoExtractor.getProviderUserId(mockOAuth2User);

        assertEquals(expectedSub, actualSub);
    }
}
