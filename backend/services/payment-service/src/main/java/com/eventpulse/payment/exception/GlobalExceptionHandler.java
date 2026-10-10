package com.eventpulse.payment.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ProblemDetail handlePaymentNotFound(PaymentNotFoundException ex) {
        log.warn("Payment not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Payment Not Found");
        problem.setType(URI.create("https://api.eventpulse.com/errors/payment-not-found"));
        return problem;
    }

    @ExceptionHandler(InvalidPaymentSignatureException.class)
    public ProblemDetail handleInvalidSignature(InvalidPaymentSignatureException ex) {
        log.warn("Payment signature verification failed: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid Payment Signature");
        problem.setType(URI.create("https://api.eventpulse.com/errors/invalid-signature"));
        return problem;
    }

    @ExceptionHandler(PaymentAlreadyProcessedException.class)
    public ProblemDetail handlePaymentAlreadyProcessed(PaymentAlreadyProcessedException ex) {
        log.warn("Payment already processed: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Payment Conflict");
        problem.setType(URI.create("https://api.eventpulse.com/errors/payment-conflict"));
        return problem;
    }

    @ExceptionHandler(BookingValidationException.class)
    public ProblemDetail handleBookingValidation(BookingValidationException ex) {
        log.warn("Booking validation error: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Booking Validation Error");
        problem.setType(URI.create("https://api.eventpulse.com/errors/booking-validation-error"));
        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setTitle("Access Denied");
        problem.setType(URI.create("https://api.eventpulse.com/errors/forbidden"));
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed for one or more fields");
        problem.setTitle("Validation Failure");
        problem.setType(URI.create("https://api.eventpulse.com/errors/validation-failure"));
        problem.setProperty("invalidParams", errors);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unhandled exception encountered: ", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected internal error occurred. Please contact support."
        );
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://api.eventpulse.com/errors/internal-server-error"));
        return problem;
    }
}
