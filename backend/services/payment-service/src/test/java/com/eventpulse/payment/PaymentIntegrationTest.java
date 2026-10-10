package com.eventpulse.payment;

import com.eventpulse.payment.client.BookingClient;
import com.eventpulse.payment.config.TestSecurityConfig;
import com.eventpulse.payment.entity.OutboxStatus;
import com.eventpulse.payment.entity.Payment;
import com.eventpulse.payment.entity.PaymentOutbox;
import com.eventpulse.payment.entity.PaymentStatus;
import com.eventpulse.payment.repository.PaymentOutboxRepository;
import com.eventpulse.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class PaymentIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentOutboxRepository outboxRepository;

    @MockBean
    private BookingClient bookingClient;

    @BeforeEach
    void cleanUp() {
        outboxRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    @Test
    @DisplayName("Health endpoint /actuator/health should return UP")
    void healthCheck_returnsUp() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/actuator/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    @DisplayName("Flyway schema and repositories should persist and retrieve Payment and Outbox records")
    void persistenceAndOutbox_integration() {
        UUID bookingId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .bookingId(bookingId)
                .customerSubject("cust-john")
                .idempotencyKey("idemp-integration-1")
                .amount(new BigDecimal("1200.00"))
                .currency("INR")
                .status(PaymentStatus.ORDER_CREATED)
                .razorpayOrderId("order_INT123")
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        assertThat(savedPayment.getId()).isNotNull();

        Optional<Payment> retrieved = paymentRepository.findByIdempotencyKey("idemp-integration-1");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getAmount()).isEqualByComparingTo("1200.00");

        PaymentOutbox outbox = PaymentOutbox.builder()
                .id(UUID.randomUUID())
                .aggregateType("Payment")
                .aggregateId(savedPayment.getId().toString())
                .eventType("PaymentSucceeded")
                .payload("{\"status\":\"CAPTURED\"}")
                .status(OutboxStatus.PENDING)
                .build();

        PaymentOutbox savedOutbox = outboxRepository.save(outbox);
        assertThat(savedOutbox.getId()).isNotNull();
        assertThat(outboxRepository.count()).isEqualTo(1);
    }
}
