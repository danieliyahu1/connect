package com.connect.connector.service;

import com.connect.connector.dto.ConnectorSocialMediaDTO;
import com.connect.connector.enums.SocialMediaPlatform;
import com.connect.connector.exception.ConnectorSocialMediaNotFoundException;
import com.connect.connector.exception.ExistingSocialMediaPlatformException;
import com.connect.connector.exception.InvalidProfileUrlException;
import com.connect.connector.model.Connector;
import com.connect.connector.model.ConnectorSocialMedia;
import com.connect.connector.repository.ConnectorSocialMediaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectorSocialMediaServiceTest {

    @Mock
    private ConnectorSocialMediaRepository connectorSocialMediaPlatformRepository;

    @InjectMocks
    private ConnectorSocialMediaService connectorSocialMediaPlatformService;

    @ParameterizedTest
    @ValueSource(strings = {
            "instagram",
            "facebook",
            "linkedin",
            "tiktok",
    })
    void addSocialMediaPlatformLink_shouldSaveAllLinks_whenValidInput(String platform) throws InvalidProfileUrlException, ExistingSocialMediaPlatformException {
        Connector connector = Connector.builder()
                .userId(UUID.randomUUID())
                .firstName("John")
                .build();

        ConnectorSocialMediaDTO socialMediaDTO = new ConnectorSocialMediaDTO(
                platform, "https://" + platform + ".com/john");

        connectorSocialMediaPlatformService.addSocialMediaPlatformLink(connector, socialMediaDTO);

        // Capture the saved entities
        ArgumentCaptor<ConnectorSocialMedia> captor = ArgumentCaptor.forClass(ConnectorSocialMedia.class);
        verify(connectorSocialMediaPlatformRepository, times(1)).save(captor.capture());

        List<ConnectorSocialMedia> savedEntities = captor.getAllValues();
        assertThat(savedEntities).hasSize(1);
        assertThat(savedEntities).anySatisfy(sm -> {
            assertThat(sm.getPlatform()).isEqualTo(SocialMediaPlatform.valueOf(platform.toUpperCase()));
            assertThat(sm.getProfileUrl()).isEqualTo("https://" + platform + ".com/john");
        });
    }

    @ParameterizedTest
    @NullAndEmptySource
    void addSocialMediaPlatformLink_shouldThrowException_whenProfileUrlIsInvalid(String profileUrl) {
        Connector connector = Connector.builder()
                .userId(UUID.randomUUID())
                .firstName("John")
                .build();

        ConnectorSocialMediaDTO socialMediaDTO = new ConnectorSocialMediaDTO(
                SocialMediaPlatform.INSTAGRAM.name(), profileUrl);

        assertThrows(InvalidProfileUrlException.class, () ->
            connectorSocialMediaPlatformService.addSocialMediaPlatformLink(connector, socialMediaDTO));

        verify(connectorSocialMediaPlatformRepository, times(0)).save(any());
    }

    @Test
    void findByConnectorId_shouldReturnSocialMediaLinks_whenValidConnectorId() {
        Connector mockConnector = mock(Connector.class);
        ConnectorSocialMedia socialMedia1 = new ConnectorSocialMedia(mockConnector, SocialMediaPlatform.INSTAGRAM, "https://instagram.com/user");
        ConnectorSocialMedia socialMedia2 = new ConnectorSocialMedia(mockConnector, SocialMediaPlatform.FACEBOOK, "https://facebook.com/user");
        when(mockConnector.getConnectorId()).thenReturn(UUID.randomUUID());
        List<ConnectorSocialMedia> expectedList = List.of(socialMedia1, socialMedia2);
        when(connectorSocialMediaPlatformRepository.findByConnector_ConnectorId(mockConnector.getConnectorId())).thenReturn(expectedList);

        List<ConnectorSocialMedia> result = connectorSocialMediaPlatformService.findByConnector_ConnectorId(mockConnector.getConnectorId());

        assertThat(result).isEqualTo(expectedList);
    }

    @Test
    void updateSocialMediaPlatformLink_shouldUpdateLink_whenValidInput() throws InvalidProfileUrlException, ConnectorSocialMediaNotFoundException {
        Connector connector = Connector.builder()
                .userId(UUID.randomUUID())
                .firstName("John")
                .build();

        ConnectorSocialMedia existingSocialMedia = new ConnectorSocialMedia(connector, SocialMediaPlatform.INSTAGRAM, "https://instagram.com/olduser");
        when(connectorSocialMediaPlatformRepository.findByConnector_ConnectorIdAndPlatform(connector.getConnectorId(), SocialMediaPlatform.INSTAGRAM))
                .thenReturn(Optional.of(existingSocialMedia));
        String newProfileUrl = "https://instagram.com/newuser";
        ConnectorSocialMedia newConnectorSocialMedia = new ConnectorSocialMedia(connector, SocialMediaPlatform.INSTAGRAM, newProfileUrl);

        when(connectorSocialMediaPlatformRepository.save(any(ConnectorSocialMedia.class))).thenReturn(newConnectorSocialMedia);

        ConnectorSocialMediaDTO result = connectorSocialMediaPlatformService.updateSocialMediaPlatformLink(connector, "INSTAGRAM", newProfileUrl);

        assertThat(result.getProfileUrl()).isEqualTo(newProfileUrl);
        verify(connectorSocialMediaPlatformRepository, times(1)).save(existingSocialMedia);
    }

    @Test
    void updateSocialMediaPlatformLink_shouldThrowException_whenSocialMediaNotFound() {
        Connector connector = Connector.builder()
                .userId(UUID.randomUUID())
                .firstName("John")
                .build();

        when(connectorSocialMediaPlatformRepository.findByConnector_ConnectorIdAndPlatform(connector.getConnectorId(), SocialMediaPlatform.INSTAGRAM))
                .thenReturn(Optional.empty());

        String newProfileUrl = "https://instagram.com/newuser";

        assertThrows(ConnectorSocialMediaNotFoundException.class, () ->
            connectorSocialMediaPlatformService.updateSocialMediaPlatformLink(connector, "INSTAGRAM", newProfileUrl));

        verify(connectorSocialMediaPlatformRepository, times(0)).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void updateSocialMediaPlatformLink_shouldThrowException_whenProfileUrlIsInvalid(String invalidProfileUrl) {
        Connector connector = Connector.builder()
                .userId(UUID.randomUUID())
                .firstName("John")
                .build();
        assertThrows(InvalidProfileUrlException.class, () ->
            connectorSocialMediaPlatformService.updateSocialMediaPlatformLink(connector, "INSTAGRAM", invalidProfileUrl));

        verify(connectorSocialMediaPlatformRepository, times(0)).save(any());
    }

    @Test
    void deleteSocialMediaPlatformLink_shouldDeleteLink_whenValidInput() throws ConnectorSocialMediaNotFoundException {
        Connector connector = Connector.builder()
                .userId(UUID.randomUUID())
                .firstName("John")
                .build();

        ConnectorSocialMedia existingSocialMedia = new ConnectorSocialMedia(connector, SocialMediaPlatform.INSTAGRAM, "https://instagram.com/user");
        when(connectorSocialMediaPlatformRepository.findByConnector_ConnectorIdAndPlatform(connector.getConnectorId(), SocialMediaPlatform.INSTAGRAM))
                .thenReturn(Optional.of(existingSocialMedia));

        connectorSocialMediaPlatformService.deleteSocialMediaPlatformLink(connector, "INSTAGRAM");

        verify(connectorSocialMediaPlatformRepository, times(1)).delete(existingSocialMedia);
    }
}
