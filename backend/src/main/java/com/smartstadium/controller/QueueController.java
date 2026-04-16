package com.smartstadium.controller;

import com.smartstadium.dto.QueueWaitTimeDto;
import com.smartstadium.model.Zone;
import com.smartstadium.service.QueueService;
import com.smartstadium.validation.ZoneValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

/**
 * REST controller for queue wait time predictions.
 *
 * <p>Provides endpoints to query estimated wait times
 * at stadium zones such as food courts, restrooms, and gates.</p>
 */
@RestController
@RequestMapping("/api/wait-time")
@Tag(name = "Queue Times", description = "Estimated wait times for stadium zones")
public class QueueController {

    private final QueueService queueService;
    private final ZoneValidator zoneValidator;

    public QueueController(QueueService queueService, ZoneValidator zoneValidator) {
        this.queueService = queueService;
        this.zoneValidator = zoneValidator;
    }

    /**
     * Returns estimated wait times for all zones.
     *
     * @return list of wait time DTOs
     */
    @GetMapping
    @Operation(summary = "Get all queue wait times", description = "Returns estimated wait times for all zones with queues")
    public ResponseEntity<List<QueueWaitTimeDto>> getAllWaitTimes() {
        List<QueueWaitTimeDto> waitTimes = queueService.getAllWaitTimes();
        return ResponseEntity.ok(waitTimes);
    }

    /**
     * Returns the estimated wait time for a specific zone.
     *
     * @param zone the zone identifier (case-insensitive)
     * @return the wait time DTO for the specified zone
     */
    @GetMapping(params = "zone")
    @Operation(summary = "Get queue wait time by zone", description = "Returns the estimated wait time for a specific zone")
    public ResponseEntity<QueueWaitTimeDto> getWaitTime(
            @Parameter(description = "Zone identifier (e.g., FOOD_COURT_EAST)", example = "FOOD_COURT_EAST") 
            @RequestParam String zone) {
        Zone parsedZone = zoneValidator.parseZone(zone);
        QueueWaitTimeDto waitTime = queueService.getWaitTime(parsedZone);
        return ResponseEntity.ok(waitTime);
    }
}
