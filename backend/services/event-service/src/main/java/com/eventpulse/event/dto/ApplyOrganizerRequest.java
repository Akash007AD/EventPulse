package com.eventpulse.event.dto;

import jakarta.validation.constraints.Email;
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
public class ApplyOrganizerRequest {

    @NotBlank(message = "Organization name is required")
    @Size(max = 128, message = "Organization name cannot exceed 128 characters")
    private String organizationName;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Contact email must be valid")
    @Size(max = 128, message = "Contact email cannot exceed 128 characters")
    private String contactEmail;

    @Size(max = 32, message = "Contact phone cannot exceed 32 characters")
    private String contactPhone;

    @Size(max = 64, message = "Business registration number cannot exceed 64 characters")
    private String businessRegistrationNumber;

    @Size(max = 255, message = "Website URL cannot exceed 255 characters")
    private String websiteUrl;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
}
