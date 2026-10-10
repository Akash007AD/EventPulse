package com.eventpulse.event.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.net.URI;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final KeycloakJwtAuthenticationConverter jwtAuthenticationConverter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(KeycloakJwtAuthenticationConverter jwtAuthenticationConverter, ObjectMapper objectMapper) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints: event browsing and actuator health
                        .requestMatchers(HttpMethod.GET, "/api/v1/events/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Organizer & Admin endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/events").hasAnyRole("ORGANIZER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/events/**").hasAnyRole("ORGANIZER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/events/**").hasAnyRole("ORGANIZER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/events/**").hasAnyRole("ORGANIZER", "ADMIN")
                        // Organizer Onboarding & Admin review
                        .requestMatchers("/api/v1/organizers/applications/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/organizers/**").authenticated()
                        // Any other request must be authenticated
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(problemDetailAuthenticationEntryPoint())
                        .accessDeniedHandler(problemDetailAccessDeniedHandler())
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(problemDetailAuthenticationEntryPoint())
                        .accessDeniedHandler(problemDetailAccessDeniedHandler())
                );

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint problemDetailAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.UNAUTHORIZED, "Full authentication is required to access this resource."
            );
            problem.setTitle("Unauthorized");
            problem.setType(URI.create("about:blank"));
            problem.setInstance(URI.create(request.getRequestURI()));
            objectMapper.writeValue(response.getOutputStream(), problem);
        };
    }

    @Bean
    public AccessDeniedHandler problemDetailAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.FORBIDDEN, "You do not have permission to access this resource."
            );
            problem.setTitle("Forbidden");
            problem.setType(URI.create("about:blank"));
            problem.setInstance(URI.create(request.getRequestURI()));
            objectMapper.writeValue(response.getOutputStream(), problem);
        };
    }
}
