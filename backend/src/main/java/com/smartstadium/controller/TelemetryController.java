package com.smartstadium.controller;

import com.smartstadium.dto.TelemetryData;
import com.smartstadium.service.CrowdService;
import com.smartstadium.service.QueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Unified controller for real-time stadium telemetry.
 *
 * <p>Provides a single endpoint to fetch the complete state of the stadium,
 * including crowd density and queue wait times for all zones.</p>
 */
@RestController
@RequestMapping("/api/telemetry")
@Tag(name = "Telemetry", description = "Unified real-time stadium data")
public class TelemetryController {

    private final CrowdService crowdService;
    private final QueueService queueService;

    public TelemetryController(CrowdService crowdService, QueueService queueService) {
        this.crowdService = crowdService;
        this.queueService = queueService;
    }

    /**
     * Returns the complete stadium state (telemetry).
     *
     * @return bundled telemetry data
     */
    @GetMapping
    @Operation(summary = "Get full stadium telemetry", description = "Returns combined crowd and queue data for all zones")
    public ResponseEntity<TelemetryData> getTelemetry() {
        return ResponseEntity.ok(new TelemetryData(
                crowdService.getAllDensities(),
                queueService.getAllWaitTimes()
        ));
    }
}
