package com.eventpulse.payment.controller;

import com.eventpulse.payment.config.KeycloakJwtAuthenticationConverter;
import com.eventpulse.payment.config.SecurityConfig;
import com.eventpulse.payment.dto.CreatePaymentOrderRequest;
import com.eventpulse.payment.dto.PaymentOrderResponse;
import com.eventpulse.payment.dto.PaymentResponse;
import com.eventpulse.payment.dto.VerifyPaymentRequest;
import com.eventpulse.payment.entity.PaymentStatus;
import com.eventpulse.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import({SecurityConfig.class, KeycloakJwtAuthenticationConverter.class})
class PaymentSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("POST /api/v1/payments/orders without token returns 401 Unauthorized")
    void createOrder_unauthenticated_returns401() throws Exception {
        CreatePaymentOrderRequest request = new CreatePaymentOrderRequest(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/payments/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /api/v1/payments/orders with CUSTOMER role returns 201 Created")
    void createOrder_withCustomerRole_returns201() throws Exception {
        UUID bookingId = UUID.randomUUID();
        CreatePaymentOrderRequest request = new CreatePaymentOrderRequest(bookingId);

        PaymentOrderResponse response = PaymentOrderResponse.builder()
                .paymentId(UUID.randomUUID())
                .bookingId(bookingId)
                .razorpayOrderId("order_TEST123")
                .amount(new BigDecimal("999.00"))
                .currency("INR")
                .keyId("rzp_test_sample")
                .status(PaymentStatus.ORDER_CREATED)
                .build();

        when(paymentService.createPaymentOrder(any(), eq("cust-1"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments/orders")
                        .with(jwt().jwt(builder -> builder.subject("cust-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.razorpayOrderId").value("order_TEST123"))
                .andExpect(jsonPath("$.amount").value(999.00));
    }

    @Test
    @DisplayName("POST /api/v1/payments/verify with CUSTOMER role returns 200 OK")
    void verifyPayment_withCustomerRole_returns200() throws Exception {
        VerifyPaymentRequest request = new VerifyPaymentRequest(
                "order_123", "pay_456", "signature_789"
        );

        PaymentResponse response = PaymentResponse.builder()
                .id(UUID.randomUUID())
                .bookingId(UUID.randomUUID())
                .customerSubject("cust-1")
                .amount(new BigDecimal("999.00"))
                .currency("INR")
                .status(PaymentStatus.CAPTURED)
                .razorpayOrderId("order_123")
                .razorpayPaymentId("pay_456")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(paymentService.verifyPayment(any(), eq("cust-1"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments/verify")
                        .with(jwt().jwt(builder -> builder.subject("cust-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CAPTURED"));
    }

    @Test
    @DisplayName("POST /api/v1/payments/webhook should be publicly accessible without JWT")
    void webhook_publicAccess_returns200() throws Exception {
        String payload = "{\"event\":\"order.paid\"}";

        mockMvc.perform(post("/api/v1/payments/webhook")
                        .header("X-Razorpay-Signature", "sample-signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        verify(paymentService).handleWebhook(eq(payload), eq("sample-signature"));
    }
}
