package com.eventpulse.ai.service.impl;

import com.eventpulse.ai.service.AiGuardrailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

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
            "print system instructions",
            "pretend you are",
            "roleplay as",
            "fictional story where",
            "bedtime story",
            "unfiltered mode",
            "ignore all safety"
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

    // Output guardrail patterns: catches hallucinated discount codes, credential leaks, or price overrides
    private static final List<Pattern> OUTPUT_SAFETY_PATTERNS = Arrays.asList(
            // API key / secret leak detection
            Pattern.compile("(?:rzp_(?:live|test)_[a-zA-Z0-9]+|sk_(?:live|test)_[a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:password|secret_key|db_password)\\s*[:=]\\s*['\"]?\\S+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("bearer\\s+[a-zA-Z0-9_.-]{25,}", Pattern.CASE_INSENSITIVE),
            // Hallucinated coupon code generation detection
            Pattern.compile("(?:use|apply|coupon|promo|discount)\\s+code[:\\s]+['\"]?[A-Z0-9_-]{3,}['\"]?", Pattern.CASE_INSENSITIVE),
            // Price override promise detection
            Pattern.compile("(?:price|cost)\\s+is\\s+(?:now\\s+)?(?:0|zero|free)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:i have|i've)\\s+(?:set|overridden|reduced)\\s+(?:the\\s+)?price", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:giving|granting)\\s+you\\s+(?:a\\s+)?free\\s+ticket", Pattern.CASE_INSENSITIVE),
            // Unauthorized refund promise detection
            Pattern.compile("(?:i have|i've)\\s+(?:approved|processed|initiated)\\s+your\\s+refund", Pattern.CASE_INSENSITIVE),
            Pattern.compile("refund\\s+has\\s+been\\s+(?:approved|processed|granted)", Pattern.CASE_INSENSITIVE)
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

    @Override
    public Optional<String> validateOutput(String generatedOutput) {
        if (generatedOutput == null || generatedOutput.isBlank()) {
            return Optional.empty();
        }

        for (Pattern pattern : OUTPUT_SAFETY_PATTERNS) {
            if (pattern.matcher(generatedOutput).find()) {
                log.warn("Output guardrail triggered: model output matched restricted pattern [{}]", pattern.pattern());
                return Optional.of("I can help you explore published events and platform FAQs. Official ticket rates and standard cancellation policies apply to all reservations.");
            }
        }

        return Optional.empty();
    }

    @Override
    public String quarantinePrompt(String rawQuery) {
        if (rawQuery == null) {
            return "<untrusted_user_query></untrusted_user_query>";
        }
        // Neutralize attempts by an attacker to close the XML delimiter early
        String sanitized = rawQuery
                .replace("<untrusted_user_query>", "")
                .replace("</untrusted_user_query>", "");

        return "<untrusted_user_query>\n" + sanitized.trim() + "\n</untrusted_user_query>";
    }
}
