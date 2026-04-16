package com.smartstadium.controller;

import com.smartstadium.dto.RouteDto;
import com.smartstadium.model.Zone;
import com.smartstadium.service.RoutingService;
import com.smartstadium.validation.ZoneValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

/**
 * REST controller for route calculation between stadium zones.
 *
 * <p>Provides an endpoint to find the optimal path between two zones,
 * considering current crowd density for congestion-aware routing.</p>
 */
@RestController
@RequestMapping("/api/route")
@Tag(name = "Routing", description = "Congestion-aware pathfinding between stadium zones")
public class RouteController {

    private final RoutingService routingService;
    private final ZoneValidator zoneValidator;

    public RouteController(RoutingService routingService, ZoneValidator zoneValidator) {
        this.routingService = routingService;
        this.zoneValidator = zoneValidator;
    }

    /**
     * Finds the optimal route between two zones.
     *
     * @param from the starting zone identifier
     * @param to   the destination zone identifier
     * @return the route with path and estimated travel time
     */
    @GetMapping
    @Operation(summary = "Find optimal route", description = "Calculates the fastest path between two zones, avoiding congested areas")
    public ResponseEntity<RouteDto> getRoute(
            @Parameter(description = "Starting zone (e.g., GATE_A)", example = "GATE_A") @RequestParam String from,
            @Parameter(description = "Destination zone (e.g., SEATING_NORTH)", example = "SEATING_NORTH") @RequestParam String to) {
        Zone fromZone = zoneValidator.parseZone(from);
        Zone toZone = zoneValidator.parseZone(to);
        RouteDto route = routingService.findRoute(fromZone, toZone);
        return ResponseEntity.ok(route);
    }
}
