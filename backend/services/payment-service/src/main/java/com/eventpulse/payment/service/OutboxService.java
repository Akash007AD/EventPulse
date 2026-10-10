package com.eventpulse.payment.service;

public interface OutboxService {

    void recordEvent(String aggregateType, String aggregateId, String eventType, Object payload);
}
