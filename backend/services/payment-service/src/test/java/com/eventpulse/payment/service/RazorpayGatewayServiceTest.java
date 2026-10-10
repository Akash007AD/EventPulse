package com.eventpulse.payment.service;

import com.eventpulse.payment.service.impl.RazorpayGatewayServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RazorpayGatewayServiceTest {

    private RazorpayGatewayService razorpayGatewayService;
    private final String keySecret = "secret_key_abcdef123456";
    private final String webhookSecret = "webhook_secret_789012";

    @BeforeEach
    void setUp() {
        razorpayGatewayService = new RazorpayGatewayServiceImpl(
                RestClient.builder(),
                "rzp_test_sample",
                keySecret,
                webhookSecret
        );
    }

    @Test
    @DisplayName("verifyPaymentSignature should succeed when HMAC-SHA256 signature matches")
    void verifyPaymentSignature_success() {
        String orderId = "order_EKfLvuafaoqqUt";
        String paymentId = "pay_29Ae07wjhJStores";
        String payload = orderId + "|" + paymentId;
        String validSignature = razorpayGatewayService.calculateHmacSha256(payload, keySecret);

        boolean result = razorpayGatewayService.verifyPaymentSignature(orderId, paymentId, validSignature);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("verifyPaymentSignature should fail when signature is tampered")
    void verifyPaymentSignature_failsWhenTampered() {
        String orderId = "order_EKfLvuafaoqqUt";
        String paymentId = "pay_29Ae07wjhJStores";

        boolean result = razorpayGatewayService.verifyPaymentSignature(orderId, paymentId, "invalid_tampered_signature_hex");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("verifyWebhookSignature should succeed when signature matches webhook secret")
    void verifyWebhookSignature_success() {
        String payload = "{\"event\":\"order.paid\",\"payload\":{}}";
        String validSignature = razorpayGatewayService.calculateHmacSha256(payload, webhookSecret);

        boolean result = razorpayGatewayService.verifyWebhookSignature(payload, validSignature);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("createOrder should generate simulated order ID in offline/test mode")
    void createOrder_offlineSimulation() {
        String orderId = razorpayGatewayService.createOrder(new BigDecimal("999.00"), "INR", "booking-123");

        assertThat(orderId).isNotNull();
        assertThat(orderId).startsWith("order_");
    }
}
