package com.connect.connector.controller;

import com.connect.auth.common.util.AsymmetricJwtUtil;
import com.connect.connector.configuration.TestSecurityConfig;
import com.connect.connector.dto.ConnectorSocialMediaDTO;
import com.connect.connector.dto.response.UploadSignatureResponseDTO;
import com.connect.connector.dto.response.ConnectorResponseDTO;
import com.connect.connector.dto.request.CreateConnectorRequestDTO;
import com.connect.connector.dto.request.UpdateConnectorRequestDTO;
import com.connect.connector.dto.ConnectorImageDTO;
import com.connect.connector.enums.SocialMediaPlatform;
import com.connect.connector.exception.*;
import com.connect.connector.service.ConnectorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ConnectorController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
class ConnectorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AsymmetricJwtUtil jwtUtil;

    @MockitoBean
    private ConnectorService connectorService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String URIPREFIX = "/connectors";

    @Test
    void updateMyProfile_returnsUpdatedProfile() throws Exception {
        UUID userId = UUID.randomUUID();

        UpdateConnectorRequestDTO updateRequest = new UpdateConnectorRequestDTO("Updated Name", "Updated Country", "Updated City", "Updated bio");

        ConnectorResponseDTO responseDTO = ConnectorResponseDTO.builder()
                .userId(userId)
                .firstName("Updated Name")
                .bio("Updated bio")
                .socialMediaLinks(List.of(new ConnectorSocialMediaDTO("INSTAGRAM", "instagram_handle")))
                .build();

        when(connectorService.updateMyProfile(userId, updateRequest)).thenReturn(responseDTO);

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(put(URIPREFIX + "/me")
                        .principal(auth)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.firstName").value("Updated Name"))
                .andExpect(jsonPath("$.bio").value("Updated bio"))
                .andExpect(jsonPath("$.socialMediaLinks[0].platform").value("INSTAGRAM"))
                .andExpect(jsonPath("$.socialMediaLinks[0].profileUrl").value("instagram_handle"));
        verify(connectorService).updateMyProfile(userId, updateRequest);
    }

    @Test
    void updateMyProfile_withInvalidProfileUrl_returnsBadRequest() throws Exception {
        UUID userId = UUID.randomUUID();

        UpdateConnectorRequestDTO updateRequest = new UpdateConnectorRequestDTO("Invalid User", "Country", "City", "Bio with invalid URL");

        when(connectorService.updateMyProfile(userId, updateRequest)).thenThrow(InvalidProfileUrlException.class);

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(put(URIPREFIX + "/me")
                        .principal(auth)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(connectorService).updateMyProfile(userId, updateRequest);
    }

    @Test
    void createMyProfile_returnsCreatedProfile() throws Exception {
        UUID userId = UUID.randomUUID();

        CreateConnectorRequestDTO createRequest = new CreateConnectorRequestDTO("New User", "Country", "City", "Bio for new user");

        ConnectorResponseDTO createdResponse = ConnectorResponseDTO.builder()
                .userId(userId)
                .firstName("New User")
                .bio("Bio for new user")
                .socialMediaLinks(Collections.emptyList())
                .build();

        when(connectorService.createMyProfile(userId, createRequest)).thenReturn(createdResponse);

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(post(URIPREFIX + "/me")
                        .principal(auth)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.firstName").value("New User"))
                .andExpect(jsonPath("$.bio").value("Bio for new user"));

        verify(connectorService).createMyProfile(eq(userId), any(CreateConnectorRequestDTO.class));
    }

    @Test
    void createMyProfile_withExistingProfile_returnsConflict() throws Exception {
        UUID userId = UUID.randomUUID();

        CreateConnectorRequestDTO createRequest = new CreateConnectorRequestDTO("Existing User", "Country", "City", "Bio for existing user");

        when(connectorService.createMyProfile(userId, createRequest)).thenThrow(ExistingConnectorException.class);

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(post(URIPREFIX + "/me")
                        .principal(auth)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict());

        verify(connectorService).createMyProfile(eq(userId), any(CreateConnectorRequestDTO.class));
    }

    @Test
    void addGalleryPhoto_returnsUpdatedProfile() throws Exception {
        UUID userId = UUID.randomUUID();

        ConnectorImageDTO connectorImageDTO = new ConnectorImageDTO( "Sample Photo", 0);

        ConnectorResponseDTO responseDTO = ConnectorResponseDTO.builder()
                .userId(userId)
                .firstName("User")
                .bio("Bio")
                .socialMediaLinks(Collections.emptyList())
                .build();

        when(connectorService.addGalleryPhoto(eq(userId), any(ConnectorImageDTO.class))).thenReturn(responseDTO);

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(post(URIPREFIX + "/me/gallery")
                        .principal(auth)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(connectorImageDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.firstName").value("User"))
                .andExpect(jsonPath("$.bio").value("Bio"));

        verify(connectorService).addGalleryPhoto(eq(userId), any(ConnectorImageDTO.class));
    }

    @Test
    void addGalleryPhoto_ServiceThrowrsImageIndexOutOfBoundException() throws Exception {
        UUID userId = UUID.randomUUID();

        ConnectorImageDTO connectorImageDTO = new ConnectorImageDTO( "Sample Photo", 0);

        when(connectorService.addGalleryPhoto(eq(userId), any(ConnectorImageDTO.class))).thenThrow(ImageIndexOutOfBoundException.class);

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(post(URIPREFIX + "/me/gallery")
                        .principal(auth)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(connectorImageDTO)))
                .andExpect(status().isBadRequest());
        verify(connectorService).addGalleryPhoto(eq(userId), any(ConnectorImageDTO.class));
    }

    @Test
    void addGalleryPhoto_notNextAvailableOrderIndex_returnsBadRequest() throws Exception {
        UUID userId = UUID.randomUUID();

        ConnectorImageDTO connectorImageDTO = new ConnectorImageDTO("Sample Photo", 10); // Invalid order index

        when(connectorService.addGalleryPhoto(eq(userId), any(ConnectorImageDTO.class)))
                .thenThrow(new InvalidImageOrderException("Order index must be between 0 and 5"));

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(post(URIPREFIX + "/me/gallery")
                        .principal(auth)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(connectorImageDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Image Order"))
                .andExpect(jsonPath("$.message").value("Order index must be between 0 and 5"));

        verify(connectorService).addGalleryPhoto(eq(userId), any(ConnectorImageDTO.class));
    }

    @Test
    void addGalleryPhoto_withImageNotFound_returnsNotFound() throws Exception {
        UUID userId = UUID.randomUUID();

        ConnectorImageDTO connectorImageDTO = new ConnectorImageDTO("Non-existent Photo", 0);

        when(connectorService.addGalleryPhoto(eq(userId), any(ConnectorImageDTO.class)))
                .thenThrow(new ImageNotFoundException("Image not found"));

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(post(URIPREFIX + "/me/gallery")
                        .principal(auth)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(connectorImageDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Image Not Found"))
                .andExpect(jsonPath("$.message").value("Image not found"));

        verify(connectorService).addGalleryPhoto(eq(userId), any(ConnectorImageDTO.class));
    }

    @Test
    void getPublicProfile_returnsProfile() throws Exception {
        UUID userId = UUID.randomUUID();

        ConnectorResponseDTO responseDTO = ConnectorResponseDTO.builder()
                .userId(userId)
                .firstName("Public User")
                .bio("Public bio")
                .socialMediaLinks(Collections.emptyList())
                .build();

        when(connectorService.getPublicProfile(userId)).thenReturn(responseDTO);

        mockMvc.perform(get(URIPREFIX + "/public/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.firstName").value("Public User"))
                .andExpect(jsonPath("$.bio").value("Public bio"));

        verify(connectorService).getPublicProfile(userId);
    }

    @Test
    void getPublicBatch_returnsProfiles() throws Exception {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        List<UUID> userIds = List.of(userId1, userId2);

        ConnectorResponseDTO profile1 = ConnectorResponseDTO.builder()
                .userId(userId1)
                .firstName("User1")
                .bio("Bio1")
                .socialMediaLinks(Collections.emptyList())
                .build();

        ConnectorResponseDTO profile2 = ConnectorResponseDTO.builder()
                .userId(userId2)
                .firstName("User2")
                .bio("Bio2")
                .socialMediaLinks(Collections.emptyList())
                .build();

        when(connectorService.getPublicProfiles(userIds)).thenReturn(List.of(profile1, profile2));

        mockMvc.perform(post(URIPREFIX + "/internal/batch")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId1.toString()))
                .andExpect(jsonPath("$[0].firstName").value("User1"))
                .andExpect(jsonPath("$[1].userId").value(userId2.toString()))
                .andExpect(jsonPath("$[1].firstName").value("User2"));

        verify(connectorService).getPublicProfiles(userIds);
    }

    @Test
    void deleteGalleryPhoto_shouldReturnUpdatedProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        int orderIndex = 2;

        ConnectorResponseDTO responseDTO = ConnectorResponseDTO.builder()
                .userId(userId)
                .firstName("John")
                .bio("Updated bio")
                .galleryImages(Collections.emptyList())
                .socialMediaLinks(List.of())
                .build();

        when(connectorService.deleteGalleryPhoto(userId, orderIndex)).thenReturn(responseDTO);

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(delete(URIPREFIX + "/me/gallery/{orderIndex}", orderIndex)
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.galleryImages").isArray())
                .andExpect(jsonPath("$.galleryImages").isEmpty());

        verify(connectorService).deleteGalleryPhoto(userId, orderIndex);
    }


    @Test
    void deleteGalleryPhoto_shouldReturnBadRequest_whenImageIndexOutOfBound() throws Exception {
        UUID userId = UUID.randomUUID();
        int invalidIndex = 99;

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        when(connectorService.deleteGalleryPhoto(userId, invalidIndex))
                .thenThrow(new ImageIndexOutOfBoundException("Order index must be between 0 and 5"));

        mockMvc.perform(delete(URIPREFIX + "/me/gallery/{orderIndex}", invalidIndex)
                        .principal(auth))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Image Index Out of Bound"))
                .andExpect(jsonPath("$.message").value("Order index must be between 0 and 5"));
        verify(connectorService).deleteGalleryPhoto(userId, invalidIndex);
    }

    @Test
    void deleteGalleryPhoto_shouldReturnNotFound_whenImageNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        int orderIndex = 1;

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        when(connectorService.deleteGalleryPhoto(userId, orderIndex))
                .thenThrow(new ImageNotFoundException("Image not found"));

        mockMvc.perform(delete(URIPREFIX + "/me/gallery/{orderIndex}", orderIndex)
                        .principal(auth))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Image Not Found"))
                .andExpect(jsonPath("$.message").value("Image not found"));
        verify(connectorService).deleteGalleryPhoto(userId, orderIndex);
    }

    @Test
    void deleteGalleryPhoto_shouldReturnBadRequest_whenProfilePictureRequired() throws Exception {
        UUID userId = UUID.randomUUID();
        int orderIndex = 0; // Assuming 0 is the profile picture index

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        when(connectorService.deleteGalleryPhoto(userId, orderIndex))
                .thenThrow(new ProfilePictureRequiredException("Profile picture cannot be deleted"));

        mockMvc.perform(delete(URIPREFIX + "/me/gallery/{orderIndex}", orderIndex)
                        .principal(auth))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Profile Picture Required"))
                .andExpect(jsonPath("$.message").value("Profile picture cannot be deleted"));
        verify(connectorService).deleteGalleryPhoto(userId, orderIndex);
    }

    @Test
    void addSocialMediaPlatformLink_shouldReturnCreatedLink() throws Exception {
        UUID userId = UUID.randomUUID();
        String platform = SocialMediaPlatform.INSTAGRAM.name();
        String profileUrl = "https://instagram.com/user";

        ConnectorSocialMediaDTO connectorSocialMediaDTO = new ConnectorSocialMediaDTO(platform, profileUrl);
        ConnectorResponseDTO responseDTO = ConnectorResponseDTO.builder()
                .userId(userId)
                .firstName("John")
                .bio("Updated bio")
                .galleryImages(Collections.emptyList())
                .socialMediaLinks(List.of(connectorSocialMediaDTO))
                .build();
        when(connectorService.addSocialMediaPlatformLink(userId, connectorSocialMediaDTO)).thenReturn(responseDTO);

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(post(URIPREFIX + "/me/social-media")
                        .principal(auth)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(connectorSocialMediaDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.socialMediaLinks[0].platform").value(platform))
                .andExpect(jsonPath("$.socialMediaLinks[0].profileUrl").value(profileUrl));

        verify(connectorService).addSocialMediaPlatformLink(userId, connectorSocialMediaDTO);
    }

    @Test
    void addSocialMediaPlatformLink_shouldReturnBadRequest_whenInvalidUrl() throws Exception {
        UUID userId = UUID.randomUUID();
        String platform = SocialMediaPlatform.INSTAGRAM.name();
        String invalidProfileUrl = "invalid_url";

        ConnectorSocialMediaDTO connectorSocialMediaDTO = new ConnectorSocialMediaDTO(platform, invalidProfileUrl);
        when(connectorService.addSocialMediaPlatformLink(userId, connectorSocialMediaDTO))
                .thenThrow(new InvalidProfileUrlException("Invalid profile URL"));

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(post(URIPREFIX + "/me/social-media")
                        .principal(auth)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(connectorSocialMediaDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Profile URL"))
                .andExpect(jsonPath("$.message").value("Invalid profile URL"));

        verify(connectorService).addSocialMediaPlatformLink(userId, connectorSocialMediaDTO);
    }

    @Test
    void updateSocialMediaPlatformLink_shouldReturnUpdatedLink() throws Exception {
        UUID userId = UUID.randomUUID();
        String platform = "INSTAGRAM";
        String profileUrl = "https://instagram.com/updated_user";
        ConnectorResponseDTO responseDTO = ConnectorResponseDTO.builder()
                .userId(userId)
                .firstName("John")
                .bio("Updated bio")
                .galleryImages(Collections.emptyList())
                .socialMediaLinks(List.of(new ConnectorSocialMediaDTO(SocialMediaPlatform.INSTAGRAM.name(), profileUrl)))
                .build();

        when(connectorService.updateSocialMediaPlatformLink(userId, platform, profileUrl))
                .thenReturn(responseDTO);

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(put(URIPREFIX + "/me/social-media/{platform}", platform)
                        .principal(auth)
                        .contentType("text/plain")
                        .content(profileUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.socialMediaLinks[0].platform").value(platform))
                .andExpect(jsonPath("$.socialMediaLinks[0].profileUrl").value(profileUrl));

        verify(connectorService).updateSocialMediaPlatformLink(userId, platform, profileUrl);
    }

    @Test
    void updateSocialMediaPlatformLink_shouldReturnBadRequest_whenInvalidUrl() throws Exception {
        UUID userId = UUID.randomUUID();
        String platform = "INSTAGRAM";
        String invalidProfileUrl = "invalid_url";

        when(connectorService.updateSocialMediaPlatformLink(userId, platform, invalidProfileUrl))
                .thenThrow(new InvalidProfileUrlException("Invalid profile URL"));

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(put(URIPREFIX + "/me/social-media/{platform}", platform)
                        .principal(auth)
                        .contentType("text/plain")
                        .content(invalidProfileUrl))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Profile URL"))
                .andExpect(jsonPath("$.message").value("Invalid profile URL"));

        verify(connectorService).updateSocialMediaPlatformLink(userId, platform, invalidProfileUrl);
    }

    @Test
    void updateSocialMediaPlatformLink_shouldReturnNotFound_whenPlatformNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        String platform = "INSTAGRAM";
        String profileUrl = "https://instagram.com/updated_user";

        when(connectorService.updateSocialMediaPlatformLink(userId, platform, profileUrl))
                .thenThrow(new ConnectorSocialMediaNotFoundException("Social media platform not found"));

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(put(URIPREFIX + "/me/social-media/{platform}", platform)
                        .principal(auth)
                        .contentType("text/plain")
                        .content(profileUrl))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Connector Social Media Not Found"))
                .andExpect(jsonPath("$.message").value("Social media platform not found"));

        verify(connectorService).updateSocialMediaPlatformLink(userId, platform, profileUrl);
    }

    @ParameterizedTest
    @ValueSource(strings = { "INSTAGRAM", "FACEBOOK", "LINKEDIN", "TIKTOK" })
    void deleteSocialMediaPlatformLink_shouldReturnDeletedLink(String platform) throws Exception {
        UUID userId = UUID.randomUUID();
        ConnectorResponseDTO responseDTO = ConnectorResponseDTO.builder()
                .userId(userId)
                .firstName("John")
                .bio("Updated bio")
                .galleryImages(Collections.emptyList())
                .socialMediaLinks(List.of(new ConnectorSocialMediaDTO(platform, null)))
                .build();
        when(connectorService.deleteSocialMediaPlatformLink(userId, platform))
                .thenReturn(responseDTO);

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(delete(URIPREFIX + "/me/social-media/{platform}", platform)
                        .principal(auth))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.socialMediaLinks[0].platform").value(platform))
                .andExpect(jsonPath("$.profileUrl").doesNotExist());

        verify(connectorService).deleteSocialMediaPlatformLink(userId, platform);
    }

    @ParameterizedTest
    @ValueSource(strings = { "GITHUB", "TWITTER" })
    void deleteSocialMediaPlatformLink_shouldReturnNotFound(String platform) throws Exception {
        UUID userId = UUID.randomUUID();

        when(connectorService.deleteSocialMediaPlatformLink(userId, platform))
                .thenThrow(new ConnectorSocialMediaNotFoundException("Social media platform not found"));

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(delete(URIPREFIX + "/me/social-media/{platform}", platform)
                        .principal(auth))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Connector Social Media Not Found"))
                .andExpect(jsonPath("$.message").value("Social media platform not found"));

        verify(connectorService).deleteSocialMediaPlatformLink(userId, platform);
    }

    @Test
    void generateUploadSignature_shouldReturnValidSignature() throws Exception {
        UUID userId = UUID.randomUUID();
        int orderIndex = 2;

        UploadSignatureResponseDTO mockResponse = new UploadSignatureResponseDTO(
                "mockApiKey", "mockCloudName", "mockSignature", "1234567890", "connectors/" + userId, String.valueOf(orderIndex)
        );

        when(connectorService.generateGalleryUploadSignature(userId, orderIndex)).thenReturn(mockResponse);

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(post(URIPREFIX + "/me/gallery/signature")
                        .principal(auth)
                        .param("orderIndex", String.valueOf(orderIndex)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiKey").value("mockApiKey"))
                .andExpect(jsonPath("$.cloudName").value("mockCloudName"))
                .andExpect(jsonPath("$.signature").value("mockSignature"))
                .andExpect(jsonPath("$.timestamp").value("1234567890"))
                .andExpect(jsonPath("$.folder").value("connectors/" + userId.toString()))
                .andExpect(jsonPath("$.publicId").value(String.valueOf(orderIndex)));

        verify(connectorService).generateGalleryUploadSignature(userId, orderIndex);
    }

    @Test
    void generateUploadSignature_invalidOrderIndex_shouldReturnBadRequest() throws Exception {
        UUID userId = UUID.randomUUID();
        int invalidOrderIndex = 99;

        when(connectorService.generateGalleryUploadSignature(userId, invalidOrderIndex))
                .thenThrow(new ImageIndexOutOfBoundException("Order index must be between 0 and 5"));

        Authentication auth = new TestingAuthenticationToken(userId.toString(), null);

        mockMvc.perform(post(URIPREFIX + "/me/gallery/signature")
                        .principal(auth)
                        .param("orderIndex", String.valueOf(invalidOrderIndex)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Image Index Out of Bound"))
                .andExpect(jsonPath("$.message").value("Order index must be between 0 and 5"));

        verify(connectorService).generateGalleryUploadSignature(userId, invalidOrderIndex);
    }
}
