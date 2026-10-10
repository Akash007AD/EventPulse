package com.eventpulse.payment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import java.net.URI;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final KeycloakJwtAuthenticationConverter jwtAuthenticationConverter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Health checks and actuator
                        .requestMatchers("/actuator/**").permitAll()
                        // Webhook callback verified cryptographically via X-Razorpay-Signature
                        .requestMatchers("/api/v1/payments/webhook").permitAll()
                        // Customer payment flows
                        .requestMatchers("/api/v1/payments/orders").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers("/api/v1/payments/verify").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers("/api/v1/payments/me").hasAnyRole("CUSTOMER", "ADMIN")
                        // General queries & refunds
                        .requestMatchers("/api/v1/payments/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                                    HttpStatus.UNAUTHORIZED,
                                    "Authentication required or access token invalid: " + authException.getMessage()
                            );
                            problem.setTitle("Unauthorized");
                            problem.setType(URI.create("https://api.eventpulse.com/errors/unauthorized"));
                            response.getWriter().write(objectMapper.writeValueAsString(problem));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                                    HttpStatus.FORBIDDEN,
                                    "You do not possess the required permissions to perform this action."
                            );
                            problem.setTitle("Forbidden");
                            problem.setType(URI.create("https://api.eventpulse.com/errors/forbidden"));
                            response.getWriter().write(objectMapper.writeValueAsString(problem));
                        })
                );

        return http.build();
    }
}
