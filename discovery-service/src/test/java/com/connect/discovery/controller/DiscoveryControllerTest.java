package com.connect.discovery.controller;

import com.connect.discovery.dto.UserSuggestionDTO;
import com.connect.discovery.service.DiscoveryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DiscoveryController.class)
class DiscoveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DiscoveryService discoveryService;


    @Test
    void discoverLocals_shouldReturnSuggestions() throws Exception {
        String userId = UUID.randomUUID().toString();

        List<UserSuggestionDTO> mockSuggestions = List.of(
                new UserSuggestionDTO("user-1", "Alice", 25, "https://url.com/pic1", "Berlin", "Germany", "You both speak German."),
                new UserSuggestionDTO("user-2", "Bob", 30, "https://url.com/pic2", "Munich", "Germany", "You love techno and beer.")
        );

        when(discoveryService.discoverLocals(UUID.fromString(userId))).thenReturn(mockSuggestions);

        Authentication auth = new TestingAuthenticationToken(userId, null);

        mockMvc.perform(get("/discover/locals").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value("user-1"))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].reason").value("You both speak German."))
                .andExpect(jsonPath("$[1].id").value("user-2"));

        verify(discoveryService).discoverLocals(UUID.fromString(userId));
    }

    @Test
    void discoverTravelers_shouldReturnSuggestions() throws Exception {
        String userId = UUID.randomUUID().toString();

        List<UserSuggestionDTO> mockSuggestions = List.of(
                new UserSuggestionDTO("traveler-1", "Claire", 27, "https://url.com/pic3", "Paris", "France", "You both love art and food."),
                new UserSuggestionDTO("traveler-2", "David", 28, "https://url.com/pic4", "Lyon", "France", "You share interest in hiking.")
        );

        when(discoveryService.discoverLocals(UUID.fromString(userId))).thenReturn(mockSuggestions);

        Authentication auth = new TestingAuthenticationToken(userId, null);

        mockMvc.perform(get("/discover/travelers").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value("traveler-1"))
                .andExpect(jsonPath("$[0].name").value("Claire"))
                .andExpect(jsonPath("$[0].reason").value("You both love art and food."))
                .andExpect(jsonPath("$[1].id").value("traveler-2"));

        verify(discoveryService).discoverLocals(UUID.fromString(userId));
    }
}
