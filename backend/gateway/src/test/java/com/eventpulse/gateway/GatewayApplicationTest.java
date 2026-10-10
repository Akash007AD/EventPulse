package com.eventpulse.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GatewayApplicationTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    @DisplayName("Context loads and gateway routes are defined")
    void contextLoads_andRoutesAreConfigured() {
        assertThat(routeLocator).isNotNull();
        var routes = routeLocator.getRoutes().collectList().block();
        assertThat(routes).isNotNull();
        assertThat(routes).anyMatch(r -> r.getId().equals("event-service-route"));
        assertThat(routes).anyMatch(r -> r.getId().equals("booking-service-route"));
    }
}
