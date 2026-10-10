package com.eventpulse.ai.service;

import java.util.Optional;

public interface AiGuardrailService {

    /**
     * Inspects a user query for prompt injection, policy violations, or company exploitation attempts.
     * @param query the raw user query
     * @return Optional containing a safe refusal response if blocked, or empty if safe.
     */
    Optional<String> validateAndFilter(String query);
}
