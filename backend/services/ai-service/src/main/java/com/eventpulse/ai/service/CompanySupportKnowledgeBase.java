package com.eventpulse.ai.service;

import com.eventpulse.ai.dto.FaqItemDto;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Authoritative ground-truth knowledge base for platform FAQ and customer support.
 * Ensures the assistant gives reliable, safe answers without hallucinating policies or
 * making unauthorized commitments that could harm the company.
 */
@Component
public class CompanySupportKnowledgeBase {

    private static final List<FaqItemDto> FAQ_ITEMS = Arrays.asList(
            FaqItemDto.builder()
                    .topic("TICKET_HOLD")
                    .question("How long is my ticket reserved during checkout?")
                    .answer("When you initiate a booking on EventPulse, your tickets are held exclusively for you for 15 minutes. If payment is not completed within 15 minutes, the reservation expires automatically and tickets return to the event pool.")
                    .build(),
            FaqItemDto.builder()
                    .topic("REFUNDS_CANCELLATIONS")
                    .question("What is the cancellation and refund policy?")
                    .answer("You can cancel your booking up to 48 hours prior to the event start time for a 100% full refund. Once approved, refunds are credited back to your original payment method via Razorpay within 5-7 business days. Cancellations within 48 hours of the event are non-refundable.")
                    .build(),
            FaqItemDto.builder()
                    .topic("PAYMENT_METHODS")
                    .question("What payment methods are supported?")
                    .answer("EventPulse supports all major Indian and international payment methods through Razorpay: UPI (Google Pay, PhonePe, Paytm, BHIM), Credit & Debit Cards (Visa, MasterCard, RuPay), NetBanking from 50+ banks, and supported digital wallets.")
                    .build(),
            FaqItemDto.builder()
                    .topic("FAILED_PAYMENT")
                    .question("What should I do if money was deducted from my bank but the booking failed?")
                    .answer("If your bank account was debited but the booking was not confirmed, Razorpay's auto-reconciliation engine will either confirm the booking within 15 minutes or automatically trigger a 100% refund to your source account within 3-5 business days. You can also email your Razorpay Payment ID to support@eventpulse.com for rapid tracing.")
                    .build(),
            FaqItemDto.builder()
                    .topic("TICKET_DELIVERY")
                    .question("How do I receive my tickets?")
                    .answer("Immediately after your payment is verified, your confirmed tickets with QR codes and booking summary are sent to your registered email and mobile number via SMS and email notifications. You can also view and download all your active tickets under 'My Bookings' in your account.")
                    .build(),
            FaqItemDto.builder()
                    .topic("BOOKING_STATUS")
                    .question("How do I check my booking status or view my tickets?")
                    .answer("You can check the real-time status of your booking at any time by logging into your EventPulse account and navigating to 'My Bookings'. You will see your booking reference, status (CONFIRMED, PENDING, CANCELLED), and downloadable QR tickets.")
                    .build(),
            FaqItemDto.builder()
                    .topic("EVENT_RESCHEDULE")
                    .question("What happens if an event is rescheduled or postponed?")
                    .answer("If an organizer reschedules an event, your existing tickets remain valid for the new date automatically. If you cannot attend the revised schedule, you are entitled to claim a full refund within 72 hours of the postponement announcement.")
                    .build(),
            FaqItemDto.builder()
                    .topic("DISCOUNT_COUPONS")
                    .question("Can the chatbot give me a discount coupon or free pass?")
                    .answer("Our automated assistant cannot generate custom promo codes, override ticket prices, or grant free tickets. Valid promotional discounts provided by organizers can be applied during checkout in the 'Promo Code' field prior to payment.")
                    .build(),
            FaqItemDto.builder()
                    .topic("ORGANIZER_APPLICATION")
                    .question("How can I become an Event Organizer on EventPulse?")
                    .answer("To organize events, submit an Organizer Application from your profile. Our administrator team reviews submissions within 24-48 business hours. Upon approval, your account role is upgraded to ORGANIZER, enabling you to publish events and track real-time analytics.")
                    .build(),
            FaqItemDto.builder()
                    .topic("CUSTOMER_SUPPORT_ESCALATION")
                    .question("How can I escalate to a human customer support agent?")
                    .answer("For urgent queries, payment disputes, or complex requests, email support@eventpulse.com or call our toll-free helpline. Please include your registered email address and Booking ID or Razorpay Payment ID, and our human support team will respond within 24 hours.")
                    .build()
    );

    public List<FaqItemDto> getAllFaqs() {
        return Collections.unmodifiableList(FAQ_ITEMS);
    }

    public Optional<FaqItemDto> findFaqMatch(String userQuery) {
        if (userQuery == null) {
            return Optional.empty();
        }
        String lower = userQuery.toLowerCase();

        // 1. High priority: payment deduction / failure
        if (lower.contains("failed payment") || lower.contains("money deducted") || lower.contains("amount deducted")
                || lower.contains("debited") || lower.contains("payment failed") || lower.contains("transaction failed")) {
            return findByTopic("FAILED_PAYMENT");
        }

        // 2. Cancellation and refunds
        if (lower.contains("cancel") || lower.contains("refund") || lower.contains("money back")) {
            return findByTopic("REFUNDS_CANCELLATIONS");
        }

        // 3. Ticket holds
        if (lower.contains("hold") || lower.contains("held") || lower.contains("reserved") || lower.contains("expire") || lower.contains("15 min")) {
            return findByTopic("TICKET_HOLD");
        }

        // 4. Booking status / lookup
        if (lower.contains("booking status") || lower.contains("check booking") || lower.contains("find my booking")
                || lower.contains("my bookings") || lower.contains("booking reference")) {
            return findByTopic("BOOKING_STATUS");
        }

        // 5. Ticket delivery
        if (lower.contains("delivery") || lower.contains("receive") || lower.contains("ticket email")
                || lower.contains("sms") || lower.contains("where is my ticket") || lower.contains("download ticket")) {
            return findByTopic("TICKET_DELIVERY");
        }

        // 6. Rescheduling / Postponement
        if (lower.contains("reschedule") || lower.contains("postpone") || lower.contains("postponed")
                || lower.contains("date changed") || lower.contains("event cancelled by organizer")) {
            return findByTopic("EVENT_RESCHEDULE");
        }

        // 7. Discount / Promo codes
        if (lower.contains("discount") || lower.contains("coupon") || lower.contains("promo")
                || lower.contains("voucher") || lower.contains("free pass") || lower.contains("cheap ticket")) {
            return findByTopic("DISCOUNT_COUPONS");
        }

        // 8. Payment methods
        if (lower.contains("payment") || lower.contains("upi") || lower.contains("card")
                || lower.contains("paytm") || lower.contains("razorpay") || lower.contains("netbanking") || lower.contains("gpay")) {
            return findByTopic("PAYMENT_METHODS");
        }

        // 9. Organizer application
        if (lower.contains("organizer") || lower.contains("host event") || lower.contains("publish event") || lower.contains("apply")) {
            return findByTopic("ORGANIZER_APPLICATION");
        }

        // 10. Human escalation / customer support
        if (lower.contains("contact") || lower.contains("helpline") || lower.contains("support email")
                || lower.contains("human") || lower.contains("agent") || lower.contains("representative") || lower.contains("escalat")) {
            return findByTopic("CUSTOMER_SUPPORT_ESCALATION");
        }

        return Optional.empty();
    }

    private Optional<FaqItemDto> findByTopic(String topic) {
        return FAQ_ITEMS.stream().filter(f -> f.getTopic().equalsIgnoreCase(topic)).findFirst();
    }
}
