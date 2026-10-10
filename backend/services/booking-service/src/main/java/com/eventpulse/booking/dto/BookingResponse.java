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
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;

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
                .expiresAt(booking.getExpiresAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
