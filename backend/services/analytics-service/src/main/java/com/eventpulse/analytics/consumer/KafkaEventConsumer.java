package com.eventpulse.analytics.consumer;

import com.eventpulse.analytics.service.AnalyticsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class KafkaEventConsumer {

    private final AnalyticsService analyticsService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"eventpulse.bookings", "eventpulse.payments"}, groupId = "eventpulse-analytics-group")
    public void consumeEvent(String message) {
        log.info("Received Kafka event message: {}", message);
        try {
            JsonNode node = objectMapper.readTree(message);
            String eventType = node.path("eventType").asText();
            String eventId = node.path("eventId").asText();

            if ("BookingConfirmed".equalsIgnoreCase(eventType)) {
                UUID catalogEventId = UUID.fromString(node.path("catalogEventId").asText());
                String title = node.path("eventTitle").asText(null);
                int quantity = node.path("quantity").asInt(1);
                BigDecimal amount = new BigDecimal(node.path("amount").asText("0.00"));
                String currency = node.path("currency").asText("INR");

                analyticsService.processBookingConfirmed(catalogEventId, title, quantity, amount, currency, eventId);
            } else if ("PaymentSucceeded".equalsIgnoreCase(eventType)) {
                String eventIdStr = node.path("catalogEventId").asText(null);
                if (eventIdStr != null && !eventIdStr.isBlank()) {
                    UUID catalogEventId = UUID.fromString(eventIdStr);
                    BigDecimal amount = new BigDecimal(node.path("amount").asText("0.00"));
                    String currency = node.path("currency").asText("INR");
                    analyticsService.processPaymentSucceeded(catalogEventId, amount, currency, eventId);
                }
            }
        } catch (Exception ex) {
            log.error("Failed to parse and project Kafka message: {}", ex.getMessage());
        }
    }
}
