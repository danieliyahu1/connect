package com.connect.discovery.service;

import com.connect.discovery.dto.ConnectorResponseDTO;
import com.connect.discovery.dto.UserSuggestionDTO;
import com.connect.discovery.dto.openai.OpenAiMatchResultDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.connect.discovery.constants.DiscoveryServiceConstants.*;

@Service
@Primary
public class OpenAiService implements AiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public OpenAiService(ChatClient openAiClient,
                         ObjectMapper objectMapper) {
        this.chatClient = openAiClient;
        this.objectMapper = objectMapper;
    }

    public List<UserSuggestionDTO> rankCandidatesByRelevance(ConnectorResponseDTO requester, List<ConnectorResponseDTO> candidates) {
        List<UserSuggestionDTO> suggestions = new ArrayList<>();

        for (ConnectorResponseDTO candidate : candidates) {
            String response = chatClient.prompt().system(SYSTEM_MESSAGE)
                    .user("Requester: " + requester + "\nCandidate: " + candidate)
                    .call().content();

            OpenAiMatchResultDTO matchResult = parseOpenAiResponse(response);

            suggestions.add(mapToSuggestionDTO(candidate, matchResult));
        }
        sortByScoreDesc(suggestions);
        return suggestions;
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