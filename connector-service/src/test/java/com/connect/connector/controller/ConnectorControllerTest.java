package com.connect.connector.controller;

import com.connect.connector.dto.response.ConnectorResponseDTO;
import com.connect.connector.dto.request.CreateConnectorRequestDTO;
import com.connect.connector.dto.request.UpdateConnectorRequestDTO;
import com.connect.connector.dto.ConnectorImageDTO;
import com.connect.connector.exception.ImageIndexOutOfBoundException;
import com.connect.connector.service.ConnectorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ConnectorController.class)
@AutoConfigureMockMvc
class ConnectorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConnectorService connectorService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String URIPREFIX = "/connectors";

    @Test
    void updateMyProfile_returnsUpdatedProfile() throws Exception {
        UUID userId = UUID.randomUUID();

        UpdateConnectorRequestDTO updateRequest = new UpdateConnectorRequestDTO();
        updateRequest.setFirstName("Updated Name");
        updateRequest.setBio("Updated bio");
        updateRequest.setSocialMediaLinks(Map.of("INSTAGRAM", "instagram_url"));

        ConnectorResponseDTO responseDTO = ConnectorResponseDTO.builder()
                .userId(userId)
                .firstName("Updated Name")
                .bio("Updated bio")
                .socialMediaLinks(Map.of("INSTAGRAM", "instagram_handle"))
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
                .andExpect(jsonPath("$.socialMediaLinks.INSTAGRAM").value("instagram_handle"));

        verify(connectorService).updateMyProfile(userId, updateRequest);
    }

    @Test
    void createMyProfile_returnsCreatedProfile() throws Exception {
        UUID userId = UUID.randomUUID();

        CreateConnectorRequestDTO createRequest = new CreateConnectorRequestDTO();
        createRequest.setFirstName("New User");
        createRequest.setBio("Bio for new user");

        ConnectorResponseDTO createdResponse = ConnectorResponseDTO.builder()
                .userId(userId)
                .firstName("New User")
                .bio("Bio for new user")
                .socialMediaLinks(Collections.emptyMap())
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
    void addGalleryPhoto_returnsUpdatedProfile() throws Exception {
        UUID userId = UUID.randomUUID();

        ConnectorImageDTO connectorImageDTO = new ConnectorImageDTO( "Sample Photo", 0);

        ConnectorResponseDTO responseDTO = ConnectorResponseDTO.builder()
                .userId(userId)
                .firstName("User")
                .bio("Bio")
                .socialMediaLinks(Collections.emptyMap())
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
    void getPublicProfile_returnsProfile() throws Exception {
        UUID userId = UUID.randomUUID();

        ConnectorResponseDTO responseDTO = ConnectorResponseDTO.builder()
                .userId(userId)
                .firstName("Public User")
                .bio("Public bio")
                .socialMediaLinks(Collections.emptyMap())
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
                .socialMediaLinks(Collections.emptyMap())
                .build();

        ConnectorResponseDTO profile2 = ConnectorResponseDTO.builder()
                .userId(userId2)
                .firstName("User2")
                .bio("Bio2")
                .socialMediaLinks(Collections.emptyMap())
                .build();

        when(connectorService.getPublicProfiles(userIds)).thenReturn(List.of(profile1, profile2));

        mockMvc.perform(post(URIPREFIX + "/internal/public-batch")
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
                .socialMediaLinks(Map.of())
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
}
