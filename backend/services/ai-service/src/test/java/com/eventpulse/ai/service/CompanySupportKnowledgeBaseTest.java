package com.eventpulse.ai.service;

import com.eventpulse.ai.dto.FaqItemDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CompanySupportKnowledgeBaseTest {

    private CompanySupportKnowledgeBase knowledgeBase;

    @BeforeEach
    void setUp() {
        knowledgeBase = new CompanySupportKnowledgeBase();
    }

    @Test
    @DisplayName("Cancellation and refund queries match REFUNDS_CANCELLATIONS topic")
    void cancellationQuery_matchesRefundTopic() {
        Optional<FaqItemDto> match = knowledgeBase.findFaqMatch("What is your cancellation and refund policy?");
        assertThat(match).isPresent();
        assertThat(match.get().getTopic()).isEqualTo("REFUNDS_CANCELLATIONS");
        assertThat(match.get().getAnswer()).contains("48 hours");
    }

    @Test
    @DisplayName("Ticket hold queries match TICKET_HOLD topic")
    void holdQuery_matchesTicketHoldTopic() {
        Optional<FaqItemDto> match = knowledgeBase.findFaqMatch("How long is my seat held during checkout?");
        assertThat(match).isPresent();
        assertThat(match.get().getTopic()).isEqualTo("TICKET_HOLD");
        assertThat(match.get().getAnswer()).contains("15 minutes");
    }

    @Test
    @DisplayName("Payment method queries match PAYMENT_METHODS topic")
    void paymentQuery_matchesPaymentMethodsTopic() {
        Optional<FaqItemDto> match = knowledgeBase.findFaqMatch("Can I pay using UPI Google Pay or credit card?");
        assertThat(match).isPresent();
        assertThat(match.get().getTopic()).isEqualTo("PAYMENT_METHODS");
        assertThat(match.get().getAnswer()).contains("Razorpay");
    }

    @Test
    @DisplayName("Organizer registration queries match ORGANIZER_APPLICATION topic")
    void organizerQuery_matchesOrganizerTopic() {
        Optional<FaqItemDto> match = knowledgeBase.findFaqMatch("How can I apply to become an organizer and publish events?");
        assertThat(match).isPresent();
        assertThat(match.get().getTopic()).isEqualTo("ORGANIZER_APPLICATION");
    }

    @Test
    @DisplayName("Non-FAQ general search queries return empty match")
    void searchEventsQuery_returnsEmptyMatch() {
        Optional<FaqItemDto> match = knowledgeBase.findFaqMatch("Find technology conferences in Bangalore");
        assertThat(match).isEmpty();
    }
}
