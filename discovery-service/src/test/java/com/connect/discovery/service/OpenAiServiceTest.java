package com.connect.discovery.service;

import com.connect.discovery.client.OpenAiClient;
import com.connect.discovery.dto.ConnectorResponseDTO;
import com.connect.discovery.dto.ConnectorImageDTO;
import com.connect.discovery.dto.UserSuggestionDTO;
import com.connect.discovery.dto.openai.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenAiServiceTest {

    @Mock
    private OpenAiClient openAiClient;

    @InjectMocks
    private OpenAiService openAiService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private ConnectorResponseDTO requester;
    private ConnectorResponseDTO candidate1;
    private ConnectorResponseDTO candidate2;

    @BeforeEach
    void setup() throws Exception {
        openAiService = new OpenAiService(openAiClient, "test-key", objectMapper);

        requester = createConnector("Alice", "Germany", "Berlin", "http://example.com/a.jpg");
        candidate1 = createConnector("Bob", "Germany", "Munich", "http://example.com/b.jpg");
        candidate2 = createConnector("Eve", "Germany", "Hamburg", "http://example.com/c.jpg");
    }

    @Test
    void rankCandidatesByRelevance_shouldReturnSortedSuggestions() throws Exception {
        String json1 = objectMapper.writeValueAsString(new OpenAiMatchResultDTO("Matched on culture", 0.9));
        String json2 = objectMapper.writeValueAsString(new OpenAiMatchResultDTO("Shared hobbies", 0.4));

        mockOpenAiResponse(json1, json2);

        List<UserSuggestionDTO> results = openAiService.rankCandidatesByRelevance(requester, List.of(candidate1, candidate2));

        assertNotNull(results);
        assertEquals(2, results.size());

        assertEquals("Bob", results.get(0).getName());
        assertEquals(0.9, results.get(0).getScore());

        assertEquals("Eve", results.get(1).getName());
        assertEquals(0.4, results.get(1).getScore());

        verify(openAiClient, times(2)).createChatCompletion(anyString(), any());
    }

    @Test
    void rankCandidatesByRelevance_shouldHandleInvalidJsonGracefully() {
        mockOpenAiResponse("INVALID_JSON");

        List<UserSuggestionDTO> results = openAiService.rankCandidatesByRelevance(requester, List.of(candidate1));

        assertEquals(1, results.size());
        assertEquals(0.0, results.get(0).getScore());
        assertEquals("INVALID_JSON", results.get(0).getReason());
    }

    @Test
    void rankCandidatesByRelevance_shouldReturnEmptyWhenCandidatesEmpty() {
        List<UserSuggestionDTO> results = openAiService.rankCandidatesByRelevance(requester, List.of());

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verifyNoInteractions(openAiClient);
    }

    @Test
    void rankCandidatesByRelevance_shouldThrowIfClientFails() {
        when(openAiClient.createChatCompletion(anyString(), any())).thenThrow(new RuntimeException("OpenAI error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                openAiService.rankCandidatesByRelevance(requester, List.of(candidate1)));

        assertEquals("OpenAI error", ex.getMessage());
    }

    // === Helpers ===

    private ConnectorResponseDTO createConnector(String name, String country, String city, String imageUrl) throws Exception {
        ConnectorResponseDTO dto = new ConnectorResponseDTO();
        setField(dto, "userId", UUID.randomUUID());
        setField(dto, "firstName", name);
        setField(dto, "country", country);
        setField(dto, "city", city);

        ConnectorImageDTO img = new ConnectorImageDTO();
        setField(img, "mediaUrl", imageUrl);
        setField(dto, "galleryImages", List.of(img));

        return dto;
    }

    private void mockOpenAiResponse(String... responses) {
        OngoingStubbing<OpenAiResponseDTO> stubbing = when(openAiClient.createChatCompletion(anyString(), any(OpenAiRequestDTO.class)));

        for (String json : responses) {
            OpenAiResponseDTO mockResponse = new OpenAiResponseDTO();
            OpenAiChoiceDTO choice = new OpenAiChoiceDTO();
            OpenAiMessageDTO message = new OpenAiMessageDTO("assistant", json);
            setField(choice, "message", message);
            setField(mockResponse, "choices", List.of(choice));

            stubbing = stubbing.thenReturn(mockResponse); // chain return values in order
        }
    }


    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
