package com.connect.discovery.service;

import com.connect.discovery.client.GroqClient;
import com.connect.discovery.dto.ConnectorResponseDTO;
import com.connect.discovery.dto.UserSuggestionDTO;
import com.connect.discovery.dto.openai.OpenAiMatchResultDTO;
import com.connect.discovery.dto.openai.OpenAiMessageDTO;
import com.connect.discovery.dto.openai.OpenAiRequestDTO;
import com.connect.discovery.dto.openai.OpenAiResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.connect.discovery.constants.DiscoveryServiceConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroqService {

    private final GroqClient groqClient;
    private final ObjectMapper objectMapper;

    public List<UserSuggestionDTO> rankCandidatesByRelevance(ConnectorResponseDTO requester, List<ConnectorResponseDTO> candidates) throws JsonProcessingException {
        List<UserSuggestionDTO> suggestions = new ArrayList<>();

        for (ConnectorResponseDTO candidate : candidates) {
            OpenAiRequestDTO request = buildRequest(requester, candidate);
            log.info("-------------------------------------------------------------");
            log.info("Sending to Groq: {}", objectMapper.writeValueAsString(request));
            log.info("-------------------------------------------------------------");
            OpenAiResponseDTO response = groqClient.createChatCompletion(request);
            String jsonContent = response.getChoices().get(0).getMessage().getContent();
            OpenAiMatchResultDTO matchResult = parseOpenAiResponse(jsonContent);

            suggestions.add(mapToSuggestionDTO(candidate, matchResult));
        }
        sortByScoreDesc(suggestions);
        return suggestions;
    }

    private OpenAiRequestDTO buildRequest(ConnectorResponseDTO requester, ConnectorResponseDTO candidate) throws JsonProcessingException {
        return OpenAiRequestDTO.builder()
                .model(GROQ_MODEL)
                .temperature(0.5)
                .messages(List.of(
                        new OpenAiMessageDTO(ROLE_SYSTEM, SYSTEM_MESSAGE),
                        new OpenAiMessageDTO(ROLE_USER,
                                "Requester: " + objectMapper.writeValueAsString(requester) +
                                        "\nCandidate: " + objectMapper.writeValueAsString(candidate))
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
