package com.eventpulse.notification.service;

import com.eventpulse.notification.dto.NotificationPayload;
import com.eventpulse.notification.dto.NotificationResponse;
import com.eventpulse.notification.entity.NotificationDelivery;
import com.eventpulse.notification.entity.NotificationStatus;
import com.eventpulse.notification.entity.NotificationType;
import com.eventpulse.notification.repository.NotificationDeliveryRepository;
import com.eventpulse.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationDeliveryRepository deliveryRepository;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(deliveryRepository, notificationDispatcher);
    }

    @Test
    @DisplayName("sendNotification should persist record and update status to SENT when dispatcher succeeds")
    void sendNotification_success() {
        NotificationPayload payload = NotificationPayload.builder()
                .recipientSubject("cust-john")
                .recipientEmail("john@example.com")
                .notificationType(NotificationType.BOOKING_CONFIRMATION)
                .title("Your Tickets are Confirmed!")
                .body("Thank you for your reservation for Event Pulse.")
                .build();

        when(deliveryRepository.save(any(NotificationDelivery.class))).thenAnswer(inv -> inv.getArgument(0));
        when(notificationDispatcher.dispatch(any(NotificationDelivery.class))).thenReturn(true);

        NotificationResponse response = notificationService.sendNotification(payload);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(response.getAttemptCount()).isEqualTo(1);

        ArgumentCaptor<NotificationDelivery> captor = ArgumentCaptor.forClass(NotificationDelivery.class);
        verify(notificationDispatcher).dispatch(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Your Tickets are Confirmed!");
    }

    @Test
    @DisplayName("sendNotification should set status to FAILED when dispatcher throws exception")
    void sendNotification_failure() {
        NotificationPayload payload = NotificationPayload.builder()
                .recipientSubject("cust-alice")
                .recipientEmail("alice@example.com")
                .notificationType(NotificationType.PAYMENT_RECEIPT)
                .title("Payment Receipt")
                .body("Amount: INR 500")
                .build();

        when(deliveryRepository.save(any(NotificationDelivery.class))).thenAnswer(inv -> inv.getArgument(0));
        when(notificationDispatcher.dispatch(any())).thenThrow(new RuntimeException("SMTP connection timed out"));

        NotificationResponse response = notificationService.sendNotification(payload);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(response.getLastError()).contains("SMTP connection timed out");
    }

    @Test
    @DisplayName("retryNotification should increment attempts and update to SENT on success")
    void retryNotification_success() {
        UUID id = UUID.randomUUID();
        NotificationDelivery failed = NotificationDelivery.builder()
                .id(id)
                .recipientSubject("cust-bob")
                .notificationType(NotificationType.REFUND_NOTICE)
                .title("Refund Processed")
                .body("Your refund is complete")
                .status(NotificationStatus.FAILED)
                .attemptCount(1)
                .lastError("Previous timeout")
                .build();

        when(deliveryRepository.findById(id)).thenReturn(Optional.of(failed));
        when(notificationDispatcher.dispatch(failed)).thenReturn(true);
        when(deliveryRepository.save(any(NotificationDelivery.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationResponse response = notificationService.retryNotification(id);

        assertThat(response.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(response.getAttemptCount()).isEqualTo(2);
        assertThat(response.getLastError()).isNull();
    }
}
