package com.eventpulse.payment.service.impl;

import com.eventpulse.payment.entity.OutboxStatus;
import com.eventpulse.payment.entity.PaymentOutbox;
import com.eventpulse.payment.repository.PaymentOutboxRepository;
import com.eventpulse.payment.service.OutboxService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {

    private final PaymentOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordEvent(String aggregateType, String aggregateId, String eventType, Object payload) {
        String jsonPayload;
        try {
            jsonPayload = (payload instanceof String str) ? str : objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox event payload for {} - {}: {}", aggregateType, aggregateId, e.getMessage());
            jsonPayload = "{\"error\":\"Serialization failure\"}";
        }

        PaymentOutbox outbox = PaymentOutbox.builder()
                .id(UUID.randomUUID())
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(jsonPayload)
                .status(OutboxStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        outboxRepository.save(outbox);
        log.info("Recorded transactional outbox event '{}' for aggregate '{}' with ID: {}",
                eventType, aggregateId, outbox.getId());
    }
}
