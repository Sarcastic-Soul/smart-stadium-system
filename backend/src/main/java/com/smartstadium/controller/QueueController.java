package com.smartstadium.controller;

import com.smartstadium.dto.QueueWaitTimeDto;
import com.smartstadium.model.Zone;
import com.smartstadium.service.QueueService;
import com.smartstadium.validation.ZoneValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for queue wait time predictions.
 *
 * <p>Provides endpoints to query estimated wait times
 * at stadium zones such as food courts, restrooms, and gates.</p>
 */
@RestController
@RequestMapping("/api/wait-time")
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
    public ResponseEntity<QueueWaitTimeDto> getWaitTime(@RequestParam String zone) {
        Zone parsedZone = zoneValidator.parseZone(zone);
        QueueWaitTimeDto waitTime = queueService.getWaitTime(parsedZone);
        return ResponseEntity.ok(waitTime);
    }
}
