package com.eventpulse.notification;

import com.eventpulse.notification.config.TestSecurityConfig;
import com.eventpulse.notification.entity.NotificationDelivery;
import com.eventpulse.notification.entity.NotificationStatus;
import com.eventpulse.notification.entity.NotificationType;
import com.eventpulse.notification.repository.NotificationDeliveryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class NotificationIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private NotificationDeliveryRepository deliveryRepository;

    @BeforeEach
    void cleanUp() {
        deliveryRepository.deleteAll();
    }

    @Test
    @DisplayName("Health endpoint /actuator/health should return UP")
    void healthCheck_returnsUp() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/actuator/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    @DisplayName("Repositories should persist and retrieve NotificationDelivery records")
    void persistence_integration() {
        UUID id = UUID.randomUUID();
        NotificationDelivery delivery = NotificationDelivery.builder()
                .id(id)
                .recipientSubject("cust-john-doe")
                .recipientEmail("john.doe@example.com")
                .notificationType(NotificationType.BOOKING_CONFIRMATION)
                .title("Order Confirmed")
                .body("Hello John, your tickets are ready.")
                .status(NotificationStatus.SENT)
                .attemptCount(1)
                .build();

        deliveryRepository.save(delivery);

        Optional<NotificationDelivery> retrieved = deliveryRepository.findById(id);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getRecipientEmail()).isEqualTo("john.doe@example.com");
        assertThat(retrieved.get().getStatus()).isEqualTo(NotificationStatus.SENT);
    }
}
