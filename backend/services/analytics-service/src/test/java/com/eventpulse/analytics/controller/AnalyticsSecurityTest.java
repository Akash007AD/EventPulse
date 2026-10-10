package com.eventpulse.analytics.controller;

import com.eventpulse.analytics.config.KeycloakJwtAuthenticationConverter;
import com.eventpulse.analytics.config.SecurityConfig;
import com.eventpulse.analytics.dto.EventAnalyticsResponse;
import com.eventpulse.analytics.dto.PlatformOverviewResponse;
import com.eventpulse.analytics.service.AnalyticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
@Import({SecurityConfig.class, KeycloakJwtAuthenticationConverter.class})
class AnalyticsSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("GET /api/v1/analytics/events/{id} without token returns 401 Unauthorized")
    void getEventAnalytics_unauthenticated_returns401() throws Exception {
        UUID eventId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/analytics/events/" + eventId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("GET /api/v1/analytics/events/{id} with CUSTOMER role returns 403 Forbidden")
    void getEventAnalytics_customerRole_returns403() throws Exception {
        UUID eventId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/analytics/events/" + eventId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /api/v1/analytics/events/{id} with ORGANIZER role returns 200 OK")
    void getEventAnalytics_organizerRole_returns200() throws Exception {
        UUID eventId = UUID.randomUUID();
        EventAnalyticsResponse response = EventAnalyticsResponse.builder()
                .eventId(eventId)
                .eventTitle("AI Summit")
                .totalRevenue(new BigDecimal("50000.00"))
                .currency("INR")
                .ticketsSold(50)
                .confirmedBookings(25)
                .lastUpdated(Instant.now())
                .build();

        when(analyticsService.getEventAnalytics(eventId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/analytics/events/" + eventId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ORGANIZER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(50000.00))
                .andExpect(jsonPath("$.ticketsSold").value(50));
    }

    @Test
    @DisplayName("GET /api/v1/analytics/overview with ORGANIZER role returns 403 Forbidden (Admin only)")
    void getOverview_organizerRole_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/overview")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ORGANIZER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/analytics/overview with ADMIN role returns 200 OK")
    void getOverview_adminRole_returns200() throws Exception {
        PlatformOverviewResponse response = PlatformOverviewResponse.builder()
                .totalGrossRevenue(new BigDecimal("100000.00"))
                .currency("INR")
                .totalTicketsSold(100L)
                .totalConfirmedBookings(50L)
                .totalTrackedEvents(5L)
                .build();

        when(analyticsService.getPlatformOverview()).thenReturn(response);

        mockMvc.perform(get("/api/v1/analytics/overview")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGrossRevenue").value(100000.00))
                .andExpect(jsonPath("$.totalTrackedEvents").value(5));
    }
}
