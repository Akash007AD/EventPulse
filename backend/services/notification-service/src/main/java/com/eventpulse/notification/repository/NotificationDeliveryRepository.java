package com.eventpulse.notification.repository;

import com.eventpulse.notification.entity.NotificationDelivery;
import com.eventpulse.notification.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, UUID> {

    Page<NotificationDelivery> findByRecipientSubject(String recipientSubject, Pageable pageable);

    List<NotificationDelivery> findByStatus(NotificationStatus status);
}
