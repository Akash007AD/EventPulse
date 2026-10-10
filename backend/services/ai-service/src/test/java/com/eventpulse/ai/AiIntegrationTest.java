package com.eventpulse.ai;

import com.eventpulse.ai.client.EventCatalogClient;
import com.eventpulse.ai.client.GroqClient;
import com.eventpulse.ai.config.TestSecurityConfig;
import com.eventpulse.ai.dto.AiChatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class AiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventCatalogClient eventCatalogClient;

    @MockitoBean
    private GroqClient groqClient;

    @Test
    @DisplayName("Actuator health returns UP")
    void actuatorHealth_returnsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("End-to-end chat flow processes FAQ question and returns grounded response")
    void e2eChat_processesFaqSuccessfully() throws Exception {
        AiChatRequest request = AiChatRequest.builder()
                .query("What is the cancellation and refund policy?")
                .build();

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intent").value("FAQ_SUPPORT"))
                .andExpect(jsonPath("$.guardrailTriggered").value(false))
                .andExpect(jsonPath("$.answer").isNotEmpty());
    }

    @Test
    @DisplayName("End-to-end chat flow processes event discovery query and returns clean response")
    void e2eChat_processesDiscoverySuccessfully() throws Exception {
        when(eventCatalogClient.searchPublishedEvents(any())).thenReturn(Collections.emptyList());

        AiChatRequest request = AiChatRequest.builder()
                .query("Find music events in Mumbai under 1000")
                .build();

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intent").value("EVENT_DISCOVERY"))
                .andExpect(jsonPath("$.searchCriteria.city").value("Mumbai"))
                .andExpect(jsonPath("$.searchCriteria.maxPrice").value(1000));
    }
}
