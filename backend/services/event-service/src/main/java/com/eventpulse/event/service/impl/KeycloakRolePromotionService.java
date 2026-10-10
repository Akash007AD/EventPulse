package com.eventpulse.event.service.impl;

import com.eventpulse.event.service.RolePromotionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class KeycloakRolePromotionService implements RolePromotionService {

    private final RestClient restClient;
    private final String keycloakServerUrl;
    private final String realm;
    private final String adminUsername;
    private final String adminPassword;
    private final String adminClientId;

    public KeycloakRolePromotionService(
            @Value("${keycloak.server-url:${KEYCLOAK_SERVER_URL:http://localhost:8180}}") String keycloakServerUrl,
            @Value("${keycloak.realm:${KEYCLOAK_REALM:eventpulse}}") String realm,
            @Value("${keycloak.admin.username:${KEYCLOAK_ADMIN:admin}}") String adminUsername,
            @Value("${keycloak.admin.password:${KEYCLOAK_ADMIN_PASSWORD:admin_password}}") String adminPassword,
            @Value("${keycloak.admin.client-id:admin-cli}") String adminClientId
    ) {
        this.restClient = RestClient.builder().build();
        this.keycloakServerUrl = keycloakServerUrl;
        this.realm = realm;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.adminClientId = adminClientId;
    }

    @Override
    public void promoteToOrganizer(String userSubject) {
        log.info("Initiating Keycloak role promotion to ORGANIZER for user: {}", userSubject);
        try {
            // 1. Obtain Admin Access Token
            String adminToken = obtainAdminToken();
            if (adminToken == null) {
                log.warn("Could not obtain Keycloak admin token; skipping direct role assignment for: {}", userSubject);
                return;
            }

            // 2. Fetch ORGANIZER role representation
            Map<String, Object> roleRepresentation = restClient.get()
                    .uri(keycloakServerUrl + "/admin/realms/" + realm + "/roles/ORGANIZER")
                    .headers(headers -> headers.setBearerAuth(adminToken))
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (roleRepresentation == null) {
                log.warn("Role 'ORGANIZER' not found in Keycloak realm '{}'", realm);
                return;
            }

            // 3. Assign role to user
            restClient.post()
                    .uri(keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userSubject + "/role-mappings/realm")
                    .headers(headers -> {
                        headers.setBearerAuth(adminToken);
                        headers.setContentType(MediaType.APPLICATION_JSON);
                    })
                    .body(List.of(roleRepresentation))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Successfully assigned ORGANIZER role in Keycloak for user: {}", userSubject);
        } catch (Exception ex) {
            log.warn("Gracefully handled Keycloak Admin API role promotion for user '{}': {}. The user application is approved in EventPulse DB.",
                    userSubject, ex.getMessage());
        }
    }

    private String obtainAdminToken() {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", adminClientId);
            formData.add("username", adminUsername);
            formData.add("password", adminPassword);
            formData.add("grant_type", "password");

            Map<String, Object> tokenResponse = restClient.post()
                    .uri(keycloakServerUrl + "/realms/master/protocol/openid-connect/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (tokenResponse != null && tokenResponse.containsKey("access_token")) {
                return (String) tokenResponse.get("access_token");
            }
        } catch (Exception ex) {
            log.debug("Keycloak Admin token retrieval bypassed or offline: {}", ex.getMessage());
        }
        return null;
    }
}
