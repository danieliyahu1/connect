package com.akatsuki.discovery.service;

import com.akatsuki.discovery.dto.ConnectorResponseDTO;
import com.akatsuki.discovery.dto.UserSuggestionDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface AiService {
    List<UserSuggestionDTO> rankCandidatesByRelevance(ConnectorResponseDTO requester, List<ConnectorResponseDTO> candidates) throws JsonProcessingException;
    }
