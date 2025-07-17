package com.connect.discovery.service;

import com.connect.discovery.dto.ConnectorResponseDTO;
import com.connect.discovery.dto.UserSuggestionDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface AiService {
    List<UserSuggestionDTO> rankCandidatesByRelevance(ConnectorResponseDTO requester, List<ConnectorResponseDTO> candidates) throws JsonProcessingException;
    }
