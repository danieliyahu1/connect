package com.connect.discovery.controller;

import com.connect.auth.common.util.AsymmetricJwtUtil;
import com.connect.discovery.configuration.TestSecurityConfig;
import com.connect.discovery.dto.UserSuggestionDTO;
import com.connect.discovery.service.DiscoveryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class DiscoveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AsymmetricJwtUtil jwtUtil;

    @MockitoBean
    private DiscoveryService discoveryService;


    @Test
    void discoverLocals_shouldReturnSuggestions() throws Exception {
        String userId = UUID.randomUUID().toString();

        List<UserSuggestionDTO> mockSuggestions = List.of(
                new UserSuggestionDTO("user-1", "Alice", "https://url.com/pic1", "Berlin", "Germany", "You both speak German.", 0.00243),
                new UserSuggestionDTO("user-2", "Bob", "https://url.com/pic2", "Munich", "Germany", "You love techno and beer.", 0.0000553)
        );

        when(discoveryService.discoverLocals()).thenReturn(mockSuggestions);

        Authentication auth = new TestingAuthenticationToken(userId, null);

        mockMvc.perform(get("/discovery/public/locals").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].userId").value("user-1"))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].reason").value("You both speak German."))
                .andExpect(jsonPath("$[1].userId").value("user-2"));

        verify(discoveryService).discoverLocals();
    }

    @Test
    void discoverTravelers_shouldReturnSuggestions() throws Exception {
        String userId = UUID.randomUUID().toString();

        List<UserSuggestionDTO> mockSuggestions = List.of(
                new UserSuggestionDTO("traveler-1", "Claire", "https://url.com/pic3", "Paris", "France", "You both love art and food.", 0.3850234),
                new UserSuggestionDTO("traveler-2", "David", "https://url.com/pic4", "Lyon", "France", "You share interest in hiking.", 0.3850234)
        );

        when(discoveryService.discoverTravelers()).thenReturn(mockSuggestions);

        Authentication auth = new TestingAuthenticationToken(userId, null);

        mockMvc.perform(get("/discovery/public/travelers").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].userId").value("traveler-1"))
                .andExpect(jsonPath("$[0].name").value("Claire"))
                .andExpect(jsonPath("$[0].reason").value("You both love art and food."))
                .andExpect(jsonPath("$[1].userId").value("traveler-2"));

        verify(discoveryService).discoverTravelers();
    }
}
