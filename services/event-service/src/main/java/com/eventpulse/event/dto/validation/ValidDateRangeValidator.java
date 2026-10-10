package com.eventpulse.event.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Instant;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, DateRanged> {

    @Override
    public boolean isValid(DateRanged value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        Instant startsAt = value.getStartsAt();
        Instant endsAt = value.getEndsAt();

        // If either is null, let @NotNull annotations handle field presence
        if (startsAt == null || endsAt == null) {
            return true;
        }

        return endsAt.isAfter(startsAt);
    }
}
