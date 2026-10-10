package com.eventpulse.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectOrganizerRequest {

    @NotBlank(message = "Rejection reason is required")
    @Size(max = 500, message = "Rejection reason cannot exceed 500 characters")
    private String reason;
}
