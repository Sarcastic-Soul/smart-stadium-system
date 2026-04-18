package com.smartstadium.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.smartstadium.model.Zone;
import com.smartstadium.service.CrowdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration test for {@link CrowdController}.
 *
 * <p>Tests the full HTTP request/response cycle with the Spring context loaded.
 * Excludes Firestore and Pub/Sub auto-configuration to use in-memory repository.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@EnableAutoConfiguration(
    exclude = {
        com.google.cloud.spring.autoconfigure.firestore
            .GcpFirestoreAutoConfiguration.class,
        com.google.cloud.spring.autoconfigure.firestore
            .FirestoreRepositoriesAutoConfiguration.class,
        com.google.cloud.spring.autoconfigure.firestore
            .FirestoreTransactionManagerAutoConfiguration.class,
        com.google.cloud.spring.autoconfigure.pubsub
            .GcpPubSubAutoConfiguration.class,
        com.google.cloud.spring.autoconfigure.pubsub
            .GcpPubSubReactiveAutoConfiguration.class,
        com.google.cloud.spring.autoconfigure.core
            .GcpContextAutoConfiguration.class,
        com.google.cloud.spring.autoconfigure.storage
            .GcpStorageAutoConfiguration.class,
        org.springframework.boot.actuate.autoconfigure.tracing
            .MicrometerTracingAutoConfiguration.class,
        org.springframework.boot.actuate.autoconfigure.observation
            .ObservationAutoConfiguration.class,
    }
)
class CrowdControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CrowdService crowdService;

    @BeforeEach
    void setUp() {
        // Seed test data
        crowdService.updateCrowdData(Zone.GATE_A, 200);
        crowdService.updateCrowdData(Zone.FOOD_COURT_EAST, 270);
    }

    @Test
    @DisplayName("GET /api/crowd-density should return all zones")
    void shouldReturnAllDensities() throws Exception {
        mockMvc
            .perform(
                get("/api/crowd-density").accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(Zone.values().length)))
            .andExpect(jsonPath("$[0].zone").exists())
            .andExpect(jsonPath("$[0].displayName").exists())
            .andExpect(jsonPath("$[0].currentCount").exists())
            .andExpect(jsonPath("$[0].capacity").exists())
            .andExpect(jsonPath("$[0].densityLevel").exists());
    }

    @Test
    @DisplayName(
        "GET /api/crowd-density/GATE_A should return specific zone data"
    )
    void shouldReturnSpecificZoneDensity() throws Exception {
        mockMvc
            .perform(
                get("/api/crowd-density/GATE_A").accept(
                    MediaType.APPLICATION_JSON
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.zone").value("GATE_A"))
            .andExpect(jsonPath("$.displayName").value("Gate A"))
            .andExpect(jsonPath("$.capacity").value(500));
    }

    @Test
    @DisplayName("GET /api/crowd-density/INVALID should return 400 error")
    void shouldReturn400ForInvalidZone() throws Exception {
        mockMvc
            .perform(
                get("/api/crowd-density/INVALID").accept(
                    MediaType.APPLICATION_JSON
                )
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail", containsString("Invalid zone")));
    }

    @Test
    @DisplayName("GET /api/crowd-density should include security headers")
    void shouldIncludeSecurityHeaders() throws Exception {
        mockMvc
            .perform(get("/api/crowd-density"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    @DisplayName(
        "GET /api/crowd-density/gate_a should handle case-insensitive input"
    )
    void shouldHandleCaseInsensitiveZone() throws Exception {
        mockMvc
            .perform(
                get("/api/crowd-density/gate_a").accept(
                    MediaType.APPLICATION_JSON
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.zone").value("GATE_A"));
    }

    @Test
    @DisplayName("GET /api/route should return valid route")
    void shouldReturnRouteWithValidParams() throws Exception {
        mockMvc
            .perform(
                get("/api/route")
                    .param("from", "GATE_A")
                    .param("to", "FOOD_COURT_EAST")
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.from").value("GATE_A"))
            .andExpect(jsonPath("$.to").value("FOOD_COURT_EAST"))
            .andExpect(jsonPath("$.path").isArray())
            .andExpect(jsonPath("$.estimatedTimeSeconds").isNumber());
    }

    @Test
    @DisplayName("GET /api/route without params should return 400")
    void shouldReturn400ForMissingRouteParams() throws Exception {
        mockMvc
            .perform(get("/api/route").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/wait-time should return all wait times")
    void shouldReturnAllWaitTimes() throws Exception {
        mockMvc
            .perform(get("/api/wait-time").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(Zone.values().length)));
    }

    @Test
    @DisplayName(
        "GET /api/wait-time?zone=FOOD_COURT_EAST should return wait time"
    )
    void shouldReturnWaitTimeForZone() throws Exception {
        mockMvc
            .perform(
                get("/api/wait-time")
                    .param("zone", "FOOD_COURT_EAST")
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.zone").value("FOOD_COURT_EAST"))
            .andExpect(jsonPath("$.displayName").value("Food Court East"))
            .andExpect(jsonPath("$.estimatedWaitSeconds").isNumber());
    }
}
