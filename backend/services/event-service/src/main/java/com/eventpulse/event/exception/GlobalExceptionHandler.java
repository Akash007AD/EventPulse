package com.eventpulse.event.exception;

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

    @ExceptionHandler(EventNotFoundException.class)
    public ProblemDetail handleEventNotFoundException(EventNotFoundException ex, HttpServletRequest request) {
        log.warn("Event not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Event Not Found");
        problem.setType(URI.create("https://eventpulse.io/errors/not-found"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "EVENT_NOT_FOUND");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(InvalidEventStateException.class)
    public ProblemDetail handleInvalidEventStateException(InvalidEventStateException ex, HttpServletRequest request) {
        log.warn("Invalid event state transition: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Invalid Event State");
        problem.setType(URI.create("https://eventpulse.io/errors/conflict"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "INVALID_EVENT_STATE");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(OrganizerApplicationNotFoundException.class)
    public ProblemDetail handleOrganizerApplicationNotFoundException(OrganizerApplicationNotFoundException ex, HttpServletRequest request) {
        log.warn("Organizer application not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Organizer Application Not Found");
        problem.setType(URI.create("https://eventpulse.io/errors/not-found"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "ORGANIZER_APPLICATION_NOT_FOUND");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(DuplicateOrganizerApplicationException.class)
    public ProblemDetail handleDuplicateOrganizerApplicationException(DuplicateOrganizerApplicationException ex, HttpServletRequest request) {
        log.warn("Duplicate organizer application: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Duplicate Application");
        problem.setType(URI.create("https://eventpulse.io/errors/conflict"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "DUPLICATE_APPLICATION");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(InvalidOrganizerApplicationStateException.class)
    public ProblemDetail handleInvalidOrganizerApplicationStateException(InvalidOrganizerApplicationStateException ex, HttpServletRequest request) {
        log.warn("Invalid organizer application state: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Invalid Application State");
        problem.setType(URI.create("https://eventpulse.io/errors/conflict"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "INVALID_APPLICATION_STATE");
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
        // Capture global/class-level errors (such as @ValidDateRange)
        ex.getBindingResult().getGlobalErrors().forEach(globalError -> {
            errors.put(globalError.getObjectName(), globalError.getDefaultMessage());
        });

        problem.setProperty("invalidParams", errors);
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument in request [{}]: {}", request.getRequestURI(), ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid Parameter");
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
        log.error("Unhandled server exception at [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected internal error occurred. Please contact support if the issue persists."
        );
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://eventpulse.io/errors/internal-error"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "INTERNAL_SERVER_ERROR");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
