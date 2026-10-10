package com.eventpulse.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Edge fallback controller invoked when downstream services encounter circuit open
 * states or timeouts.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping(produces = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
    public Mono<ResponseEntity<ProblemDetail>> handleGetFallback() {
        return buildFallbackResponse();
    }

    @PostMapping(produces = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
    public Mono<ResponseEntity<ProblemDetail>> handlePostFallback() {
        return buildFallbackResponse();
    }

    private Mono<ResponseEntity<ProblemDetail>> buildFallbackResponse() {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "The requested downstream service is temporarily unavailable or timed out. Please retry shortly."
        );
        problem.setType(URI.create("https://api.eventpulse.com/errors/service-unavailable"));
        problem.setTitle("Service Unavailable");

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem));
    }
}
