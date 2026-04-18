package com.smartstadium.controller;

import com.smartstadium.model.Zone;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/zones")
@Tag(name = "Zones", description = "Endpoints for discovering stadium zones")
public class ZoneController {

    @GetMapping
    @Operation(
        summary = "Get all zones",
        description = "Returns a list of all valid stadium zones"
    )
    public ResponseEntity<List<Map<String, String>>> getAllZones() {
        List<Map<String, String>> zones = Arrays
            .stream(Zone.values())
            .map(zone ->
                Map.of("id", zone.name(), "name", zone.getDisplayName())
            )
            .collect(Collectors.toList());
        return ResponseEntity.ok(zones);
    }
}
