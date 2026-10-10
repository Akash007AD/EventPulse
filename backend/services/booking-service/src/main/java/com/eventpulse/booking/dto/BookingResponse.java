package com.eventpulse.booking.dto;

import com.eventpulse.booking.entity.Booking;
import com.eventpulse.booking.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private UUID id;
    private String customerSubject;
    private UUID eventId;
    private Integer quantity;
    private BigDecimal amount;
    private String currency;
    private BookingStatus status;
    private String seatNumbers;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;

    public BookingResponse(UUID id, String customerSubject, UUID eventId, Integer quantity,
                           BigDecimal amount, String currency, BookingStatus status,
                           Instant expiresAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.customerSubject = customerSubject;
        this.eventId = eventId;
        this.quantity = quantity;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static BookingResponse fromEntity(Booking booking) {
        if (booking == null) {
            return null;
        }
        return BookingResponse.builder()
                .id(booking.getId())
                .customerSubject(booking.getCustomerSubject())
                .eventId(booking.getEventId())
                .quantity(booking.getQuantity())
                .amount(booking.getAmount())
                .currency(booking.getCurrency())
                .status(booking.getStatus())
                .seatNumbers(booking.getSeatNumbers())
                .expiresAt(booking.getExpiresAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
