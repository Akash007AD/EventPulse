package com.eventpulse.event.controller;

import com.eventpulse.event.config.KeycloakJwtAuthenticationConverter;
import com.eventpulse.event.config.SecurityConfig;
import com.eventpulse.event.dto.ApplyOrganizerRequest;
import com.eventpulse.event.dto.OrganizerApplicationResponse;
import com.eventpulse.event.dto.PagedResponse;
import com.eventpulse.event.entity.OrganizerApplicationStatus;
import com.eventpulse.event.service.OrganizerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrganizerController.class)
@Import({SecurityConfig.class, KeycloakJwtAuthenticationConverter.class})
class OrganizerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrganizerService organizerService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("POST /api/v1/organizers/apply without authentication returns 401 ProblemDetail")
    void apply_unauthenticated_returns401() throws Exception {
        ApplyOrganizerRequest request = ApplyOrganizerRequest.builder()
                .organizationName("Mega Live")
                .contactEmail("admin@megalive.com")
                .build();

        mockMvc.perform(post("/api/v1/organizers/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("GET /api/v1/organizers/applications by CUSTOMER role returns 403 Forbidden ProblemDetail")
    void listApplications_byCustomer_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/organizers/applications")
                        .with(jwt().jwt(jwt -> jwt.subject("cust-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("POST /api/v1/organizers/applications/{id}/approve by CUSTOMER role returns 403 Forbidden")
    void approveApplication_byCustomer_returns403() throws Exception {
        UUID appId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/organizers/applications/{id}/approve", appId)
                        .with(jwt().jwt(jwt -> jwt.subject("cust-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /api/v1/organizers/applications by ADMIN role returns 200 OK")
    void listApplications_byAdmin_returns200() throws Exception {
        when(organizerService.listApplications(any(), any()))
                .thenReturn(PagedResponse.<OrganizerApplicationResponse>builder()
                        .content(List.of())
                        .page(0)
                        .size(10)
                        .totalElements(0)
                        .totalPages(0)
                        .isFirst(true)
                        .isLast(true)
                        .build());

        mockMvc.perform(get("/api/v1/organizers/applications")
                        .with(jwt().jwt(jwt -> jwt.subject("admin-user"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/organizers/apply with CUSTOMER role returns 201 Created")
    void apply_withCustomerRole_returns201() throws Exception {
        ApplyOrganizerRequest request = ApplyOrganizerRequest.builder()
                .organizationName("Mega Live")
                .contactEmail("admin@megalive.com")
                .build();

        UUID appId = UUID.randomUUID();
        when(organizerService.apply(any(), eq("cust-1")))
                .thenReturn(OrganizerApplicationResponse.builder()
                        .id(appId)
                        .userSubject("cust-1")
                        .organizationName("Mega Live")
                        .status(OrganizerApplicationStatus.PENDING)
                        .build());

        mockMvc.perform(post("/api/v1/organizers/apply")
                        .with(jwt().jwt(jwt -> jwt.subject("cust-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(appId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
