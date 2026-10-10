package com.eventpulse.payment.service;

import java.math.BigDecimal;

public interface RazorpayGatewayService {

    String getKeyId();

    String createOrder(BigDecimal amount, String currency, String receipt);

    boolean verifyPaymentSignature(String orderId, String paymentId, String receivedSignature);

    boolean verifyWebhookSignature(String payload, String receivedSignature);

    String calculateHmacSha256(String data, String secret);
}
