package com.eventpulse.booking.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookingNotFoundException.class)
    public ProblemDetail handleBookingNotFound(BookingNotFoundException ex, HttpServletRequest request) {
        log.warn("Booking not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Booking Not Found");
        problem.setType(URI.create("https://eventpulse.io/errors/booking-not-found"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "BOOKING_NOT_FOUND");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(InvalidBookingStateException.class)
    public ProblemDetail handleInvalidState(InvalidBookingStateException ex, HttpServletRequest request) {
        log.warn("Invalid booking state transition: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Invalid Booking State");
        problem.setType(URI.create("https://eventpulse.io/errors/conflict"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "INVALID_BOOKING_STATE");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(ConcurrentBookingException.class)
    public ProblemDetail handleConcurrentBooking(ConcurrentBookingException ex, HttpServletRequest request) {
        log.warn("Concurrent lock contention on booking request: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
        problem.setTitle("High Demand Contention");
        problem.setType(URI.create("https://eventpulse.io/errors/high-demand"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "HIGH_DEMAND_CONCURRENCY");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(EventValidationException.class)
    public ProblemDetail handleEventValidation(EventValidationException ex, HttpServletRequest request) {
        log.warn("Event validation failed: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Event Validation Failed");
        problem.setType(URI.create("https://eventpulse.io/errors/event-validation-failed"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "EVENT_VALIDATION_FAILED");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failed for request [{}]: {} error(s)", request.getRequestURI(), ex.getErrorCount());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "One or more validation constraints failed.");
        problem.setTitle("Validation Failed");
        problem.setType(URI.create("https://eventpulse.io/errors/validation-failed"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "VALIDATION_ERROR");
        problem.setProperty("timestamp", Instant.now());

        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problem.setProperty("invalidParams", errors);
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid Request Parameter");
        problem.setType(URI.create("https://eventpulse.io/errors/bad-request"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "INVALID_PARAMETER");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied for request [{}]: {}", request.getRequestURI(), ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setTitle("Forbidden");
        problem.setType(URI.create("about:blank"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "FORBIDDEN");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected internal error occurred."
        );
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://eventpulse.io/errors/internal-error"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "INTERNAL_SERVER_ERROR");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
