package com.eventpulse.ai.client.impl;

import com.eventpulse.ai.client.GroqClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class GroqClientImpl implements GroqClient {

    private final String apiKey;
    private final String model;
    private final RestClient restClient;

    public GroqClientImpl(
            @Value("${eventpulse.ai.groq.base-url:https://api.groq.com/openai/v1}") String baseUrl,
            @Value("${eventpulse.ai.groq.api-key:mock-key}") String apiKey,
            @Value("${eventpulse.ai.groq.model:gpt-oss-20b}") String model,
            @Value("${eventpulse.ai.groq.connect-timeout-ms:4000}") int connectTimeoutMs,
            @Value("${eventpulse.ai.groq.read-timeout-ms:15000}") int readTimeoutMs) {
        this.apiKey = apiKey;
        this.model = model;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    @Override
    public boolean isLiveAvailable() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("mock");
    }

    @Override
    public String generateChatResponse(String systemPrompt, String userQuery) {
        if (!isLiveAvailable()) {
            log.debug("Groq API key not provided or set to mock. Using local fallback engine.");
            return null;
        }

        try {
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .temperature(0.2)
                    .maxTokens(800)
                    .messages(Arrays.asList(
                            new ChatMessage("system", systemPrompt),
                            new ChatMessage("user", userQuery)
                    ))
                    .build();

            ChatCompletionResponse response = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ChatCompletionResponse.class);

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                ChatMessage choiceMessage = response.getChoices().get(0).getMessage();
                if (choiceMessage != null) {
                    return choiceMessage.getContent();
                }
            }

            return null;
        } catch (Exception ex) {
            log.warn("Groq inference call failed (falling back to local engine): {}", ex.getMessage());
            return null;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ChatCompletionRequest {
        private String model;
        private List<ChatMessage> messages;
        private Double temperature;
        private Integer maxTokens;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ChatMessage {
        private String role;
        private String content;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ChatCompletionResponse {
        private List<Choice> choices;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Choice {
        private ChatMessage message;
    }
}
