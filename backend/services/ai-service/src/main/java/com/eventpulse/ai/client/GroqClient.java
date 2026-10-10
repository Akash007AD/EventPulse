package com.eventpulse.ai.client;

public interface GroqClient {

    /**
     * Sends a chat prompt to Groq LPU inference API.
     * @param systemPrompt authoritative system instructions and context
     * @param userQuery the user's question
     * @return Model response text, or null if unavailable
     */
    String generateChatResponse(String systemPrompt, String userQuery);

    /**
     * Evaluates whether a query is safe using semantic AI classification (LLM-as-a-judge).
     * @param userQuery the user's input
     * @return true if safe, false if classified as unsafe
     */
    boolean evaluateSafety(String userQuery);

    /**
     * Returns true if a live Groq API key is configured.
     */
    boolean isLiveAvailable();
}
