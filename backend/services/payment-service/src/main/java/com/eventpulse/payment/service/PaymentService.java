package com.eventpulse.payment.service;

import com.eventpulse.payment.dto.CreatePaymentOrderRequest;
import com.eventpulse.payment.dto.PagedResponse;
import com.eventpulse.payment.dto.PaymentOrderResponse;
import com.eventpulse.payment.dto.PaymentResponse;
import com.eventpulse.payment.dto.VerifyPaymentRequest;

import java.util.UUID;

public interface PaymentService {

    PaymentOrderResponse createPaymentOrder(CreatePaymentOrderRequest request, String customerSubject, String idempotencyKey);

    PaymentResponse verifyPayment(VerifyPaymentRequest request, String customerSubject);

    PaymentResponse getPaymentById(UUID id, String customerSubject);

    PaymentResponse getPaymentByBookingId(UUID bookingId, String customerSubject);

    PagedResponse<PaymentResponse> getMyPayments(String customerSubject, int page, int size);

    PaymentResponse processRefund(UUID paymentId, String reason, String customerSubject, boolean isAdmin);

    void handleWebhook(String payload, String signature);
}
