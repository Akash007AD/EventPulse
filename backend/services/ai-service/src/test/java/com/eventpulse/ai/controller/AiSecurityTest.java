package com.eventpulse.ai.controller;

import com.eventpulse.ai.config.SecurityConfig;
import com.eventpulse.ai.config.TestSecurityConfig;
import com.eventpulse.ai.dto.AiChatRequest;
import com.eventpulse.ai.dto.AiChatResponse;
import com.eventpulse.ai.dto.ChatIntent;
import com.eventpulse.ai.service.AiDiscoveryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiDiscoveryController.class)
@Import({SecurityConfig.class, TestSecurityConfig.class})
@ActiveProfiles("test")
class AiSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AiDiscoveryService aiDiscoveryService;

    @Test
    @DisplayName("Public unauthenticated access to /api/v1/ai/chat is allowed")
    void publicAccess_toChatEndpoint_isAllowed() throws Exception {
        AiChatResponse response = AiChatResponse.builder()
                .answer("Hello! How can I help you find events today?")
                .intent(ChatIntent.OFF_TOPIC)
                .events(Collections.emptyList())
                .build();

        when(aiDiscoveryService.handleChat(any(AiChatRequest.class))).thenReturn(response);

        AiChatRequest request = AiChatRequest.builder()
                .query("Hello")
                .build();

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Hello! How can I help you find events today?"));
    }

    @Test
    @DisplayName("Public unauthenticated access to /api/v1/ai/faq/topics is allowed")
    void publicAccess_toFaqTopics_isAllowed() throws Exception {
        when(aiDiscoveryService.getFaqTopics()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/ai/faq/topics"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Authenticated CUSTOMER access to /api/v1/ai/chat is allowed")
    void authenticatedCustomerAccess_toChat_isAllowed() throws Exception {
        AiChatResponse response = AiChatResponse.builder()
                .answer("I found 2 events in Bangalore")
                .intent(ChatIntent.EVENT_DISCOVERY)
                .events(Collections.emptyList())
                .build();

        when(aiDiscoveryService.handleChat(any(AiChatRequest.class))).thenReturn(response);

        AiChatRequest request = AiChatRequest.builder()
                .query("Events in Bangalore")
                .build();

        mockMvc.perform(post("/api/v1/ai/chat")
                        .with(jwt().jwt(jwt -> jwt.subject("cust-user-1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intent").value("EVENT_DISCOVERY"));
    }
}
