package com.eventpulse.payment.repository;

import com.eventpulse.payment.entity.OutboxStatus;
import com.eventpulse.payment.entity.PaymentOutbox;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentOutboxRepository extends JpaRepository<PaymentOutbox, UUID> {

    List<PaymentOutbox> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable);
}
