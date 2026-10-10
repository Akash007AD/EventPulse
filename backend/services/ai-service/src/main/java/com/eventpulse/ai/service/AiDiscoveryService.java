package com.eventpulse.ai.service;

import com.eventpulse.ai.dto.AiChatRequest;
import com.eventpulse.ai.dto.AiChatResponse;
import com.eventpulse.ai.dto.FaqItemDto;

import java.util.List;

public interface AiDiscoveryService {

    /**
     * Processes a conversational user query, handling natural-language event discovery,
     * platform FAQ, and customer support with strict guardrails.
     */
    AiChatResponse handleChat(AiChatRequest request);

    /**
     * Returns the list of standard support FAQ topics.
     */
    List<FaqItemDto> getFaqTopics();
}
