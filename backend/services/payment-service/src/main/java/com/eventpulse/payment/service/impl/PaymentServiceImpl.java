package com.eventpulse.payment.service.impl;

import com.eventpulse.payment.client.BookingClient;
import com.eventpulse.payment.dto.BookingDetailsDto;
import com.eventpulse.payment.dto.CreatePaymentOrderRequest;
import com.eventpulse.payment.dto.PagedResponse;
import com.eventpulse.payment.dto.PaymentOrderResponse;
import com.eventpulse.payment.dto.PaymentResponse;
import com.eventpulse.payment.dto.VerifyPaymentRequest;
import com.eventpulse.payment.entity.Payment;
import com.eventpulse.payment.entity.PaymentStatus;
import com.eventpulse.payment.exception.BookingValidationException;
import com.eventpulse.payment.exception.InvalidPaymentSignatureException;
import com.eventpulse.payment.exception.PaymentAlreadyProcessedException;
import com.eventpulse.payment.exception.PaymentNotFoundException;
import com.eventpulse.payment.repository.PaymentRepository;
import com.eventpulse.payment.service.OutboxService;
import com.eventpulse.payment.service.PaymentService;
import com.eventpulse.payment.service.RazorpayGatewayService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayGatewayService razorpayGatewayService;
    private final BookingClient bookingClient;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    @Override
    public PaymentOrderResponse createPaymentOrder(CreatePaymentOrderRequest request, String customerSubject, String idempotencyKey) {
        log.info("Initiating payment order for bookingId: {}, customerSubject: {}, idempotencyKey: {}",
                request.getBookingId(), customerSubject, idempotencyKey);

        // 1. Check Idempotency Key
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Payment> existingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey.trim());
            if (existingPayment.isPresent()) {
                Payment p = existingPayment.get();
                log.info("Found existing payment order for idempotencyKey '{}': paymentId={}", idempotencyKey, p.getId());
                return PaymentOrderResponse.builder()
                        .paymentId(p.getId())
                        .bookingId(p.getBookingId())
                        .razorpayOrderId(p.getRazorpayOrderId())
                        .amount(p.getAmount())
                        .currency(p.getCurrency())
                        .keyId(razorpayGatewayService.getKeyId())
                        .status(p.getStatus())
                        .build();
            }
        }

        // 2. Authoritative Verification via Booking Service
        BookingDetailsDto booking = bookingClient.getBooking(request.getBookingId())
                .orElseThrow(() -> new BookingValidationException("Booking not found: " + request.getBookingId()));

        if (!"PENDING_PAYMENT".equalsIgnoreCase(booking.getStatus())) {
            throw new BookingValidationException("Booking " + request.getBookingId() + " is in state " + booking.getStatus() + ", expected PENDING_PAYMENT");
        }

        if (booking.getExpiresAt() != null && Instant.now().isAfter(booking.getExpiresAt())) {
            throw new BookingValidationException("Booking reservation hold has expired for booking: " + request.getBookingId());
        }

        String effectiveCustomer = (customerSubject != null && !customerSubject.isBlank()) ? customerSubject.trim() : booking.getCustomerSubject();
        if (customerSubject != null && !customerSubject.isBlank() && !customerSubject.equals(booking.getCustomerSubject())) {
            throw new AccessDeniedException("You are not authorized to pay for this booking.");
        }

        // Check if a payment already exists for this booking that is CAPTURED
        Optional<Payment> existingForBooking = paymentRepository.findByBookingId(request.getBookingId());
        if (existingForBooking.isPresent() && existingForBooking.get().getStatus() == PaymentStatus.CAPTURED) {
            throw new PaymentAlreadyProcessedException("Booking " + request.getBookingId() + " is already paid.");
        }

        // 3. Create Order via Razorpay Gateway Service
        String razorpayOrderId = razorpayGatewayService.createOrder(
                booking.getAmount(),
                booking.getCurrency(),
                request.getBookingId().toString()
        );

        // 4. Save Payment record
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .bookingId(booking.getId())
                .customerSubject(effectiveCustomer)
                .idempotencyKey(idempotencyKey != null && !idempotencyKey.isBlank() ? idempotencyKey.trim() : null)
                .amount(booking.getAmount())
                .currency(booking.getCurrency().toUpperCase())
                .status(PaymentStatus.ORDER_CREATED)
                .razorpayOrderId(razorpayOrderId)
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Saved Payment order '{}' with Razorpay Order ID '{}'", saved.getId(), razorpayOrderId);

        return PaymentOrderResponse.builder()
                .paymentId(saved.getId())
                .bookingId(saved.getBookingId())
                .razorpayOrderId(saved.getRazorpayOrderId())
                .amount(saved.getAmount())
                .currency(saved.getCurrency())
                .keyId(razorpayGatewayService.getKeyId())
                .status(saved.getStatus())
                .build();
    }

    @Override
    public PaymentResponse verifyPayment(VerifyPaymentRequest request, String customerSubject) {
        log.info("Verifying payment for Razorpay order: {}", request.getRazorpayOrderId());

        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order: " + request.getRazorpayOrderId()));

        if (customerSubject != null && !customerSubject.isBlank() && !customerSubject.equals(payment.getCustomerSubject())) {
            throw new AccessDeniedException("You are not authorized to verify this payment.");
        }

        // Idempotent retry: if already CAPTURED with same payment ID, return existing
        if (payment.getStatus() == PaymentStatus.CAPTURED) {
            log.info("Payment {} is already CAPTURED. Returning idempotent success.", payment.getId());
            return PaymentResponse.fromEntity(payment);
        }

        // 1. Verify Cryptographic Signature (HMAC-SHA256)
        boolean isValid = razorpayGatewayService.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!isValid) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Invalid Razorpay cryptographic signature");
            paymentRepository.save(payment);

            outboxService.recordEvent("Payment", payment.getId().toString(), "PaymentFailed",
                    PaymentResponse.fromEntity(payment));

            throw new InvalidPaymentSignatureException("Razorpay payment signature verification failed.");
        }

        // 2. Update Payment State to CAPTURED
        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        Payment saved = paymentRepository.save(payment);

        // 3. Record Transactional Outbox Event
        outboxService.recordEvent("Payment", saved.getId().toString(), "PaymentSucceeded",
                PaymentResponse.fromEntity(saved));

        // 4. Confirm Booking in Booking Service
        boolean confirmed = bookingClient.confirmBooking(saved.getBookingId());
        if (!confirmed) {
            log.warn("Booking {} confirmation callback failed or delayed. Will reconcile via outbox/events.", saved.getBookingId());
        }

        log.info("Payment {} verified and captured successfully for booking {}", saved.getId(), saved.getBookingId());
        return PaymentResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID id, String customerSubject) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        if (customerSubject != null && !customerSubject.isBlank() && !customerSubject.equals(payment.getCustomerSubject())) {
            throw new AccessDeniedException("You do not have permission to view this payment.");
        }

        return PaymentResponse.fromEntity(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByBookingId(UUID bookingId, String customerSubject) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for booking: " + bookingId));

        if (customerSubject != null && !customerSubject.isBlank() && !customerSubject.equals(payment.getCustomerSubject())) {
            throw new AccessDeniedException("You do not have permission to view this payment.");
        }

        return PaymentResponse.fromEntity(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponse> getMyPayments(String customerSubject, int page, int size) {
        int boundedPage = Math.max(page, 0);
        int boundedSize = (size <= 0 || size > 100) ? 20 : size;

        Pageable pageable = PageRequest.of(boundedPage, boundedSize, Sort.by("createdAt").descending());
        Page<Payment> paymentPage = paymentRepository.findByCustomerSubject(customerSubject, pageable);

        return PagedResponse.of(paymentPage, PaymentResponse::fromEntity);
    }

    @Override
    public PaymentResponse processRefund(UUID paymentId, String reason, String customerSubject, boolean isAdmin) {
        log.info("Processing refund for paymentId: {}, reason: {}", paymentId, reason);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if (!isAdmin && customerSubject != null && !customerSubject.equals(payment.getCustomerSubject())) {
            throw new AccessDeniedException("You do not have permission to refund this payment.");
        }

        if (payment.getStatus() != PaymentStatus.CAPTURED) {
            throw new PaymentAlreadyProcessedException("Cannot refund payment with status: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setFailureReason("Refund processed: " + (reason != null ? reason : "Customer or admin request"));
        Payment saved = paymentRepository.save(payment);

        outboxService.recordEvent("Payment", saved.getId().toString(), "RefundInitiated",
                PaymentResponse.fromEntity(saved));

        log.info("Payment {} refunded successfully", paymentId);
        return PaymentResponse.fromEntity(saved);
    }

    @Override
    public void handleWebhook(String payload, String signature) {
        log.info("Processing Razorpay webhook callback");

        boolean isValid = razorpayGatewayService.verifyWebhookSignature(payload, signature);
        if (!isValid) {
            log.warn("Invalid Razorpay webhook signature");
            throw new InvalidPaymentSignatureException("Invalid Razorpay webhook signature");
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            String eventType = root.path("event").asText();
            log.info("Received Razorpay webhook event: {}", eventType);

            if ("order.paid".equalsIgnoreCase(eventType) || "payment.captured".equalsIgnoreCase(eventType)) {
                String orderId = root.path("payload").path("order").path("entity").path("id").asText();
                if (orderId == null || orderId.isBlank()) {
                    orderId = root.path("payload").path("payment").path("entity").path("order_id").asText();
                }
                String paymentId = root.path("payload").path("payment").path("entity").path("id").asText();

                if (orderId != null && !orderId.isBlank()) {
                    Optional<Payment> paymentOpt = paymentRepository.findByRazorpayOrderId(orderId);
                    if (paymentOpt.isPresent()) {
                        Payment payment = paymentOpt.get();
                        if (payment.getStatus() != PaymentStatus.CAPTURED) {
                            payment.setStatus(PaymentStatus.CAPTURED);
                            payment.setRazorpayPaymentId(paymentId);
                            paymentRepository.save(payment);

                            outboxService.recordEvent("Payment", payment.getId().toString(), "PaymentSucceeded",
                                    PaymentResponse.fromEntity(payment));
                            bookingClient.confirmBooking(payment.getBookingId());
                            log.info("Webhook captured payment for order: {}", orderId);
                        }
                    }
                }
            } else if ("payment.failed".equalsIgnoreCase(eventType)) {
                String orderId = root.path("payload").path("payment").path("entity").path("order_id").asText();
                if (orderId != null && !orderId.isBlank()) {
                    paymentRepository.findByRazorpayOrderId(orderId).ifPresent(payment -> {
                        if (payment.getStatus() == PaymentStatus.ORDER_CREATED) {
                            payment.setStatus(PaymentStatus.FAILED);
                            payment.setFailureReason("Payment failed on gateway webhook");
                            paymentRepository.save(payment);

                            outboxService.recordEvent("Payment", payment.getId().toString(), "PaymentFailed",
                                    PaymentResponse.fromEntity(payment));
                        }
                    });
                }
            }
        } catch (Exception ex) {
            log.error("Failed to parse and handle Razorpay webhook JSON payload: {}", ex.getMessage());
        }
    }
}
