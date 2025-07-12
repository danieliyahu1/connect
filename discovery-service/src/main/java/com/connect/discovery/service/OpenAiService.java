package com.connect.discovery.service;

import com.connect.discovery.client.OpenAiClient;
import com.connect.discovery.dto.ConnectorResponseDTO;
import com.connect.discovery.dto.UserSuggestionDTO;
import com.connect.discovery.dto.openai.OpenAiMatchResultDTO;
import com.connect.discovery.dto.openai.OpenAiMessageDTO;
import com.connect.discovery.dto.openai.OpenAiRequestDTO;
import com.connect.discovery.dto.openai.OpenAiResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.connect.discovery.constants.DiscoveryServiceConstants.*;

@Service
public class OpenAiService {

    private final OpenAiClient openAiClient;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public OpenAiService(OpenAiClient openAiClient,
                         @Value("${openai.api.key}") String apiKey,
                         ObjectMapper objectMapper) {
        this.openAiClient = openAiClient;
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
    }

    public List<UserSuggestionDTO> rankCandidatesByRelevance(ConnectorResponseDTO requester, List<ConnectorResponseDTO> candidates) {
        String bearerToken = AUTHORIZATION_HEADER_BEARER_SPACE_PREFIX + apiKey;
        List<UserSuggestionDTO> suggestions = new ArrayList<>();

        for (ConnectorResponseDTO candidate : candidates) {
            OpenAiRequestDTO request = buildRequest(requester, candidate);
            OpenAiResponseDTO response = openAiClient.createChatCompletion(bearerToken, request);

            String jsonContent = response.getChoices().get(0).getMessage().getContent();
            OpenAiMatchResultDTO matchResult = parseOpenAiResponse(jsonContent);

            suggestions.add(mapToSuggestionDTO(candidate, matchResult));
        }
        sortByScoreDesc(suggestions);
        return suggestions;
    }

    private OpenAiRequestDTO buildRequest(ConnectorResponseDTO requester, ConnectorResponseDTO candidate) {
        return OpenAiRequestDTO.builder()
                .model("gpt-4")
                .temperature(0.5)
                .messages(List.of(
                        new OpenAiMessageDTO(ROLE_SYSTEM, SYSTEM_MESSAGE),
                        new OpenAiMessageDTO(ROLE_USER,
                                "Requester: " + requester +
                                        "\nCandidate: " + candidate)
                ))
                .build();
    }

    private UserSuggestionDTO mapToSuggestionDTO(ConnectorResponseDTO candidate, OpenAiMatchResultDTO matchResult) {
        return UserSuggestionDTO.builder()
                .userId(candidate.getUserId().toString())
                .name(candidate.getFirstName())
                .profilePictureUrl(candidate.getGalleryImages() != null && !candidate.getGalleryImages().isEmpty()
                        ? candidate.getGalleryImages().get(0).getMediaUrl()
                        : null)
                .city(candidate.getCity())
                .country(candidate.getCountry())
                .reason(matchResult.getReason())
                .score(matchResult.getScore())
                .build();
    }

    private OpenAiMatchResultDTO parseOpenAiResponse(String jsonContent) {
        try {
            return objectMapper.readValue(jsonContent, OpenAiMatchResultDTO.class);
        } catch (Exception e) {
            // Log parsing error if needed
            return new OpenAiMatchResultDTO(jsonContent, 0.0); // fallback: raw text as reason, score=0
        }
    }

    private void sortByScoreDesc(List<UserSuggestionDTO> suggestions) {
        suggestions.sort((s1, s2) -> Double.compare(s2.getScore(), s1.getScore()));
    }
}
