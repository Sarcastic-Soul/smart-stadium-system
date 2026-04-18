package com.smartstadium.service;

import static org.junit.jupiter.api.Assertions.*;

import com.smartstadium.config.RoutingProperties;
import com.smartstadium.dto.RouteDto;
import com.smartstadium.model.Zone;
import com.smartstadium.repository.InMemoryStadiumRepository;
import com.smartstadium.repository.StadiumRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RoutingService}.
 * Tests A* pathfinding, edge cases, and route properties.
 */
class RoutingServiceTest {

    private RoutingService routingService;
    private RoutingProperties routingProperties;

    @BeforeEach
    void setUp() {
        StadiumRepository repository = new InMemoryStadiumRepository();
        CrowdService crowdService = new CrowdService(repository);
        routingProperties = new RoutingProperties();
        routingService = new RoutingService(
            crowdService,
            routingProperties,
            Optional.empty()
        );
    }

    @Test
    @DisplayName("Should return same-zone route with zero time")
    void shouldReturnSameZoneRoute() {
        RouteDto route = routingService.findRoute(Zone.GATE_A, Zone.GATE_A);

        assertEquals(1, route.path().size());
        assertEquals("GATE_A", route.path().get(0));
        assertEquals(0, route.estimatedTimeSeconds());
        assertEquals(0.0, route.totalWeight());
    }

    @Test
    @DisplayName("Should find direct route between adjacent zones")
    void shouldFindDirectRoute() {
        RouteDto route = routingService.findRoute(
            Zone.GATE_A,
            Zone.MAIN_CONCOURSE
        );

        assertEquals(2, route.path().size());
        assertEquals("GATE_A", route.path().get(0));
        assertEquals(
            "MAIN_CONCOURSE",
            route.path().get(route.path().size() - 1)
        );
        assertTrue(route.estimatedTimeSeconds() > 0);
    }

    @Test
    @DisplayName("Should find multi-hop route between distant zones")
    void shouldFindMultiHopRoute() {
        RouteDto route = routingService.findRoute(Zone.GATE_A, Zone.VIP_LOUNGE);

        assertTrue(route.path().size() >= 3);
        assertEquals("GATE_A", route.path().get(0));
        assertEquals("VIP_LOUNGE", route.path().get(route.path().size() - 1));
        assertTrue(route.estimatedTimeSeconds() > 0);
        assertTrue(route.totalWeight() > 0);
    }

    @Test
    @DisplayName("Should find route between all zone pairs")
    void shouldFindRouteBetweenAllZones() {
        for (Zone from : Zone.values()) {
            for (Zone to : Zone.values()) {
                RouteDto route = routingService.findRoute(from, to);
                assertNotNull(
                    route,
                    "Route from " + from + " to " + to + " should exist"
                );
                assertFalse(route.path().isEmpty());
            }
        }
    }

    @Test
    @DisplayName("Should have consistent path and display name lengths")
    void shouldHaveConsistentPathNames() {
        RouteDto route = routingService.findRoute(
            Zone.GATE_B,
            Zone.FOOD_COURT_EAST
        );

        assertEquals(route.path().size(), route.pathDisplayNames().size());
        assertEquals("Gate B", route.pathDisplayNames().get(0));
    }

    @Test
    @DisplayName(
        "Should return route with positive travel time for different zones"
    )
    void shouldReturnPositiveTravelTime() {
        RouteDto route = routingService.findRoute(
            Zone.GATE_A,
            Zone.FOOD_COURT_EAST
        );

        assertTrue(
            route.estimatedTimeSeconds() > 0,
            "Travel time should be positive for different zones"
        );
    }

    @Test
    @DisplayName("Should handle zero walking speed gracefully")
    void shouldHandleZeroWalkingSpeed() {
        routingProperties.setWalkingSpeed(0.0);
        RouteDto route = routingService.findRoute(
            Zone.GATE_A,
            Zone.FOOD_COURT_EAST
        );

        // IEEE 754 division by 0.0 results in Infinity, which casts to Integer.MAX_VALUE
        assertEquals(
            Integer.MAX_VALUE,
            route.estimatedTimeSeconds(),
            "Zero walking speed should result in MAX_VALUE estimated time"
        );
    }
}
