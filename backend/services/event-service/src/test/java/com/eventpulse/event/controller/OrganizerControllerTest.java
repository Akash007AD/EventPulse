package com.eventpulse.event.controller;

import com.eventpulse.event.dto.ApplyOrganizerRequest;
import com.eventpulse.event.dto.OrganizerApplicationResponse;
import com.eventpulse.event.dto.PagedResponse;
import com.eventpulse.event.dto.RejectOrganizerRequest;
import com.eventpulse.event.entity.OrganizerApplicationStatus;
import com.eventpulse.event.service.OrganizerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrganizerController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrganizerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrganizerService organizerService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("POST /api/v1/organizers/apply: returns 201 Created")
    void apply_returns201() throws Exception {
        ApplyOrganizerRequest request = ApplyOrganizerRequest.builder()
                .organizationName("Mega Events Inc")
                .contactEmail("admin@megaevents.com")
                .contactPhone("9988776655")
                .build();

        UUID appId = UUID.randomUUID();
        OrganizerApplicationResponse response = OrganizerApplicationResponse.builder()
                .id(appId)
                .userSubject("user-john")
                .organizationName("Mega Events Inc")
                .contactEmail("admin@megaevents.com")
                .status(OrganizerApplicationStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        when(organizerService.apply(any(ApplyOrganizerRequest.class), eq("user-john")))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/organizers/apply")
                        .header("X-User-Subject", "user-john")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(appId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/v1/organizers/me: returns 200 OK")
    void getMyApplication_returns200() throws Exception {
        UUID appId = UUID.randomUUID();
        OrganizerApplicationResponse response = OrganizerApplicationResponse.builder()
                .id(appId)
                .userSubject("user-john")
                .organizationName("Mega Events Inc")
                .status(OrganizerApplicationStatus.APPROVED)
                .build();

        when(organizerService.getMyApplication("user-john")).thenReturn(response);

        mockMvc.perform(get("/api/v1/organizers/me")
                        .header("X-User-Subject", "user-john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appId.toString()))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("GET /api/v1/organizers/applications: returns 200 OK with paged response")
    void listApplications_returns200() throws Exception {
        OrganizerApplicationResponse response = OrganizerApplicationResponse.builder()
                .id(UUID.randomUUID())
                .userSubject("user-john")
                .organizationName("Mega Events Inc")
                .status(OrganizerApplicationStatus.PENDING)
                .build();

        PagedResponse<OrganizerApplicationResponse> pagedResponse = PagedResponse.<OrganizerApplicationResponse>builder()
                .content(List.of(response))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .isFirst(true)
                .isLast(true)
                .build();

        when(organizerService.listApplications(any(), any())).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/organizers/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/organizers/applications/{id}/approve: returns 200 OK")
    void approveApplication_returns200() throws Exception {
        UUID appId = UUID.randomUUID();
        OrganizerApplicationResponse response = OrganizerApplicationResponse.builder()
                .id(appId)
                .userSubject("user-john")
                .status(OrganizerApplicationStatus.APPROVED)
                .reviewedBy("admin-alex")
                .build();

        when(organizerService.approveApplication(eq(appId), eq("admin-alex"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/organizers/applications/{id}/approve", appId)
                        .header("X-Admin-Subject", "admin-alex"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reviewedBy").value("admin-alex"));
    }

    @Test
    @DisplayName("POST /api/v1/organizers/applications/{id}/reject: returns 200 OK")
    void rejectApplication_returns200() throws Exception {
        UUID appId = UUID.randomUUID();
        RejectOrganizerRequest request = RejectOrganizerRequest.builder()
                .reason("Incomplete business documentation.")
                .build();

        OrganizerApplicationResponse response = OrganizerApplicationResponse.builder()
                .id(appId)
                .userSubject("user-john")
                .status(OrganizerApplicationStatus.REJECTED)
                .rejectionReason("Incomplete business documentation.")
                .reviewedBy("admin-alex")
                .build();

        when(organizerService.rejectApplication(eq(appId), eq("admin-alex"), eq("Incomplete business documentation.")))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/organizers/applications/{id}/reject", appId)
                        .header("X-Admin-Subject", "admin-alex")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value("Incomplete business documentation."));
    }
}
