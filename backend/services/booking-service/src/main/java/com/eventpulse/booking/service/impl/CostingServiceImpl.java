package com.eventpulse.booking.service.impl;

import com.eventpulse.booking.dto.CostCalculationRequest;
import com.eventpulse.booking.dto.CostCalculationResponse;
import com.eventpulse.booking.service.CostingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Slf4j
@Service
public class CostingServiceImpl implements CostingService {

    private static final BigDecimal PLATFORM_FEE_PER_TICKET = new BigDecimal("25.00");
    private static final BigDecimal CGST_RATE = new BigDecimal("0.025"); // 2.5% CGST
    private static final BigDecimal SGST_RATE = new BigDecimal("0.025"); // 2.5% SGST

    @Override
    public CostCalculationResponse calculateCost(CostCalculationRequest request) {
        log.info("Backend calculating authoritative cost for eventId: {}", request.getEventId());

        int count = 1;
        BigDecimal subtotal = BigDecimal.ZERO;

        if (request.getSeats() != null && !request.getSeats().isEmpty()) {
            count = request.getSeats().size();
            for (CostCalculationRequest.SeatCostItem seat : request.getSeats()) {
                if (seat.getPrice() != null) {
                    subtotal = subtotal.add(seat.getPrice());
                }
            }
        } else if (request.getQuantity() != null && request.getQuantity() > 0) {
            count = request.getQuantity();
            subtotal = new BigDecimal("999.00").multiply(new BigDecimal(count));
        }

        if (subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            subtotal = new BigDecimal("999.00").multiply(new BigDecimal(count));
        }

        BigDecimal countBd = new BigDecimal(count);
        BigDecimal platformFeeTotal = PLATFORM_FEE_PER_TICKET.multiply(countBd).setScale(2, RoundingMode.HALF_UP);

        // GST is calculated strictly on the ticket price (not convenience fee)
        BigDecimal cgstAmount = subtotal.multiply(CGST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal sgstAmount = subtotal.multiply(SGST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalGst = cgstAmount.add(sgstAmount).setScale(2, RoundingMode.HALF_UP);

        BigDecimal grandTotal = subtotal.add(totalGst).add(platformFeeTotal).setScale(2, RoundingMode.HALF_UP);

        return CostCalculationResponse.builder()
                .eventId(request.getEventId())
                .ticketCount(count)
                .ticketSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                .platformFeePerTicket(PLATFORM_FEE_PER_TICKET)
                .platformFeeTotal(platformFeeTotal)
                .cgstRatePercent(new BigDecimal("2.5"))
                .cgstAmount(cgstAmount)
                .sgstRatePercent(new BigDecimal("2.5"))
                .sgstAmount(sgstAmount)
                .totalGstAmount(totalGst)
                .grandTotal(grandTotal)
                .currency("INR")
                .calculatedBy("EventPulse Microservices Costing Engine (Backend Authoritative)")
                .calculatedAt(Instant.now())
                .build();
    }
}
