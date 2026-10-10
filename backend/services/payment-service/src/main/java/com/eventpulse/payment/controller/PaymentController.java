package com.eventpulse.payment.controller;

import com.eventpulse.payment.dto.CreatePaymentOrderRequest;
import com.eventpulse.payment.dto.PagedResponse;
import com.eventpulse.payment.dto.PaymentOrderResponse;
import com.eventpulse.payment.dto.PaymentResponse;
import com.eventpulse.payment.dto.VerifyPaymentRequest;
import com.eventpulse.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<PaymentOrderResponse> createPaymentOrder(
            @Valid @RequestBody CreatePaymentOrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal Jwt jwt) {
        String customerSubject = (jwt != null) ? jwt.getSubject() : "anonymous-user";
        log.info("Received request to create payment order for booking: {}, customer: {}", request.getBookingId(), customerSubject);

        PaymentOrderResponse response = paymentService.createPaymentOrder(request, customerSubject, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String customerSubject = (jwt != null) ? jwt.getSubject() : null;
        log.info("Received request to verify payment for order: {}, customer: {}", request.getRazorpayOrderId(), customerSubject);

        PaymentResponse response = paymentService.verifyPayment(request, customerSubject);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String customerSubject = (jwt != null) ? jwt.getSubject() : null;
        return ResponseEntity.ok(paymentService.getPaymentById(id, customerSubject));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentResponse> getPaymentByBookingId(
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal Jwt jwt) {
        String customerSubject = (jwt != null) ? jwt.getSubject() : null;
        return ResponseEntity.ok(paymentService.getPaymentByBookingId(bookingId, customerSubject));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<PagedResponse<PaymentResponse>> getMyPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {
        String customerSubject = (jwt != null) ? jwt.getSubject() : "anonymous-user";
        return ResponseEntity.ok(paymentService.getMyPayments(customerSubject, page, size));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal Jwt jwt) {
        String customerSubject = (jwt != null) ? jwt.getSubject() : null;
        boolean isAdmin = jwt != null && jwt.getClaimAsStringList("roles") != null &&
                jwt.getClaimAsStringList("roles").contains("ADMIN");

        PaymentResponse response = paymentService.processRefund(id, reason, customerSubject, isAdmin);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        log.info("Received Razorpay webhook callback");
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
