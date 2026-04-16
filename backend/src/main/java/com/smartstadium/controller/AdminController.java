package com.smartstadium.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.smartstadium.service.EventSimulationService;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*") // In production, restrict this to specific origins
@Tag(name = "Admin Utilities", description = "Endpoints requiring administrative privileges")
public class AdminController {

    private final EventSimulationService eventSimulationService;

    public AdminController(EventSimulationService eventSimulationService) {
        this.eventSimulationService = eventSimulationService;
    }

    @PostMapping("/simulation/trigger")
    @Operation(summary = "Trigger a simulation tick manually", security = @SecurityRequirement(name = "bearerAuth"))
    // @PreAuthorize("hasAuthority('SCOPE_admin')") // In real RBAC, use scopes
    public ResponseEntity<Map<String, String>> triggerSimulation() {
        eventSimulationService.simulateCrowdMovements();
        return ResponseEntity.ok(Map.of("status", "success", "message", "Simulation manually triggered."));
    }
}
