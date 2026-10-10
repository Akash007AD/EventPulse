package com.eventpulse.payment.service;

import com.eventpulse.payment.client.BookingClient;
import com.eventpulse.payment.dto.BookingDetailsDto;
import com.eventpulse.payment.dto.CreatePaymentOrderRequest;
import com.eventpulse.payment.dto.PaymentOrderResponse;
import com.eventpulse.payment.dto.PaymentResponse;
import com.eventpulse.payment.dto.VerifyPaymentRequest;
import com.eventpulse.payment.entity.Payment;
import com.eventpulse.payment.entity.PaymentStatus;
import com.eventpulse.payment.exception.BookingValidationException;
import com.eventpulse.payment.exception.InvalidPaymentSignatureException;
import com.eventpulse.payment.repository.PaymentRepository;
import com.eventpulse.payment.service.impl.PaymentServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RazorpayGatewayService razorpayGatewayService;

    @Mock
    private BookingClient bookingClient;

    @Mock
    private OutboxService outboxService;

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(
                paymentRepository,
                razorpayGatewayService,
                bookingClient,
                outboxService,
                new ObjectMapper()
        );
    }

    @Test
    @DisplayName("createPaymentOrder should succeed when booking is valid and PENDING_PAYMENT")
    void createPaymentOrder_success() {
        UUID bookingId = UUID.randomUUID();
        CreatePaymentOrderRequest request = new CreatePaymentOrderRequest(bookingId);

        BookingDetailsDto booking = BookingDetailsDto.builder()
                .id(bookingId)
                .customerSubject("cust-101")
                .amount(new BigDecimal("1500.00"))
                .currency("INR")
                .status("PENDING_PAYMENT")
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .build();

        when(bookingClient.getBooking(bookingId)).thenReturn(Optional.of(booking));
        when(razorpayGatewayService.createOrder(any(), any(), any())).thenReturn("order_TEST123456");
        when(razorpayGatewayService.getKeyId()).thenReturn("rzp_test_sample");
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentOrderResponse response = paymentService.createPaymentOrder(request, "cust-101", "idemp-001");

        assertThat(response).isNotNull();
        assertThat(response.getBookingId()).isEqualTo(bookingId);
        assertThat(response.getRazorpayOrderId()).isEqualTo("order_TEST123456");
        assertThat(response.getAmount()).isEqualByComparingTo("1500.00");
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.ORDER_CREATED);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().getIdempotencyKey()).isEqualTo("idemp-001");
    }

    @Test
    @DisplayName("createPaymentOrder should return existing order when idempotencyKey was already processed")
    void createPaymentOrder_idempotentReturn() {
        UUID bookingId = UUID.randomUUID();
        CreatePaymentOrderRequest request = new CreatePaymentOrderRequest(bookingId);

        Payment existing = Payment.builder()
                .id(UUID.randomUUID())
                .bookingId(bookingId)
                .customerSubject("cust-101")
                .idempotencyKey("idemp-duplicate")
                .amount(new BigDecimal("1500.00"))
                .currency("INR")
                .status(PaymentStatus.ORDER_CREATED)
                .razorpayOrderId("order_EXISTING789")
                .build();

        when(paymentRepository.findByIdempotencyKey("idemp-duplicate")).thenReturn(Optional.of(existing));
        when(razorpayGatewayService.getKeyId()).thenReturn("rzp_test_sample");

        PaymentOrderResponse response = paymentService.createPaymentOrder(request, "cust-101", "idemp-duplicate");

        assertThat(response).isNotNull();
        assertThat(response.getRazorpayOrderId()).isEqualTo("order_EXISTING789");
    }

    @Test
    @DisplayName("createPaymentOrder should fail when booking does not exist in Booking Service")
    void createPaymentOrder_failsWhenBookingNotFound() {
        UUID bookingId = UUID.randomUUID();
        CreatePaymentOrderRequest request = new CreatePaymentOrderRequest(bookingId);

        when(bookingClient.getBooking(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createPaymentOrder(request, "cust-101", null))
                .isInstanceOf(BookingValidationException.class)
                .hasMessageContaining("Booking not found");
    }

    @Test
    @DisplayName("createPaymentOrder should fail when booking hold has expired")
    void createPaymentOrder_failsWhenBookingExpired() {
        UUID bookingId = UUID.randomUUID();
        CreatePaymentOrderRequest request = new CreatePaymentOrderRequest(bookingId);

        BookingDetailsDto expiredBooking = BookingDetailsDto.builder()
                .id(bookingId)
                .customerSubject("cust-101")
                .amount(new BigDecimal("500.00"))
                .currency("INR")
                .status("PENDING_PAYMENT")
                .expiresAt(Instant.now().minus(5, ChronoUnit.MINUTES)) // past
                .build();

        when(bookingClient.getBooking(bookingId)).thenReturn(Optional.of(expiredBooking));

        assertThatThrownBy(() -> paymentService.createPaymentOrder(request, "cust-101", null))
                .isInstanceOf(BookingValidationException.class)
                .hasMessageContaining("reservation hold has expired");
    }

    @Test
    @DisplayName("verifyPayment should update state to CAPTURED, write outbox, and confirm booking")
    void verifyPayment_success() {
        String orderId = "order_VALID123";
        String paymentId = "pay_VALID456";
        String signature = "valid_hmac_signature";

        UUID bookingId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .bookingId(bookingId)
                .customerSubject("cust-101")
                .amount(new BigDecimal("1500.00"))
                .currency("INR")
                .status(PaymentStatus.ORDER_CREATED)
                .razorpayOrderId(orderId)
                .build();

        when(paymentRepository.findByRazorpayOrderId(orderId)).thenReturn(Optional.of(payment));
        when(razorpayGatewayService.verifyPaymentSignature(orderId, paymentId, signature)).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        VerifyPaymentRequest request = new VerifyPaymentRequest(orderId, paymentId, signature);
        PaymentResponse response = paymentService.verifyPayment(request, "cust-101");

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
        assertThat(response.getRazorpayPaymentId()).isEqualTo(paymentId);

        verify(outboxService).recordEvent(eq("Payment"), eq(payment.getId().toString()), eq("PaymentSucceeded"), any());
        verify(bookingClient).confirmBooking(bookingId);
    }

    @Test
    @DisplayName("verifyPayment should fail and record PaymentFailed outbox when signature is invalid")
    void verifyPayment_failsOnInvalidSignature() {
        String orderId = "order_VALID123";
        String paymentId = "pay_TAMPERED";
        String signature = "invalid_signature";

        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .bookingId(UUID.randomUUID())
                .customerSubject("cust-101")
                .amount(new BigDecimal("1500.00"))
                .currency("INR")
                .status(PaymentStatus.ORDER_CREATED)
                .razorpayOrderId(orderId)
                .build();

        when(paymentRepository.findByRazorpayOrderId(orderId)).thenReturn(Optional.of(payment));
        when(razorpayGatewayService.verifyPaymentSignature(orderId, paymentId, signature)).thenReturn(false);

        VerifyPaymentRequest request = new VerifyPaymentRequest(orderId, paymentId, signature);

        assertThatThrownBy(() -> paymentService.verifyPayment(request, "cust-101"))
                .isInstanceOf(InvalidPaymentSignatureException.class);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(outboxService).recordEvent(eq("Payment"), eq(payment.getId().toString()), eq("PaymentFailed"), any());
    }

    @Test
    @DisplayName("processRefund should update payment to REFUNDED and record RefundInitiated outbox")
    void processRefund_success() {
        UUID paymentId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .id(paymentId)
                .bookingId(UUID.randomUUID())
                .customerSubject("cust-101")
                .amount(new BigDecimal("1500.00"))
                .currency("INR")
                .status(PaymentStatus.CAPTURED)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.processRefund(paymentId, "Event rescheduled", "cust-101", false);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(outboxService).recordEvent(eq("Payment"), eq(paymentId.toString()), eq("RefundInitiated"), any());
    }
}
