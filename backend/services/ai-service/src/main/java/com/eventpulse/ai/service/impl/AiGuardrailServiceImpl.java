package com.eventpulse.ai.service.impl;

import com.eventpulse.ai.service.AiGuardrailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
public class AiGuardrailServiceImpl implements AiGuardrailService {

    @Value("${eventpulse.ai.guardrails.max-query-length:500}")
    private int maxQueryLength;

    // Prompt injection patterns attempting to hijack model instructions
    private static final List<String> PROMPT_INJECTION_PATTERNS = Arrays.asList(
            "ignore previous instructions",
            "disregard all previous",
            "system prompt",
            "you are now in developer mode",
            "you are now an unrestricted ai",
            "jailbreak",
            "act as dan",
            "forget your rules",
            "print system instructions"
    );

    // Company-harming exploitation attempts (free tickets, price override, credential leaks, unauthorized refunds)
    private static final List<String> COMPANY_EXPLOITATION_PATTERNS = Arrays.asList(
            "give me free tickets",
            "give me free passes",
            "bypass payment",
            "make price 0",
            "override price",
            "give me admin access",
            "leak database credentials",
            "show secret keys",
            "razorpay key",
            "internal architecture",
            "force refund",
            "bypass cancellation policy",
            "refund within 48 hours",
            "generate coupon",
            "give promo code",
            "give discount code",
            "dump database",
            "dump user data",
            "customer pii",
            "other users' bookings",
            "change my role to admin"
    );

    @Override
    public Optional<String> validateAndFilter(String query) {
        if (query == null || query.isBlank()) {
            return Optional.of("Please provide a query about upcoming events or platform support.");
        }

        if (query.length() > maxQueryLength) {
            log.warn("Blocked oversized AI query: length={}", query.length());
            return Optional.of("Your query is too long. Please shorten your question to under " + maxQueryLength + " characters.");
        }

        String lowerQuery = query.toLowerCase(Locale.ROOT);

        for (String pattern : PROMPT_INJECTION_PATTERNS) {
            if (lowerQuery.contains(pattern)) {
                log.warn("Prompt injection attempt detected and blocked: [{}]", pattern);
                return Optional.of("I cannot process this request because it contains instructions that violate our system safety guidelines. How can I help you discover events or answer platform questions today?");
            }
        }

        for (String pattern : COMPANY_EXPLOITATION_PATTERNS) {
            if (lowerQuery.contains(pattern)) {
                log.warn("Company exploitation attempt detected and blocked: [{}]", pattern);
                return Optional.of("I cannot perform price overrides, generate free passes, or disclose internal company credentials. You can browse published events at their official ticket rates or review our standard cancellation policy.");
            }
        }

        return Optional.empty();
    }
}
