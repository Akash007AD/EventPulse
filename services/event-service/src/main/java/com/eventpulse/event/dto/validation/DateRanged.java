package com.eventpulse.event.dto.validation;

import java.time.Instant;

public interface DateRanged {
    Instant getStartsAt();
    Instant getEndsAt();
}
