package com.smartstadium.controller;

import com.smartstadium.dto.CrowdDensityDto;
import com.smartstadium.model.Zone;
import com.smartstadium.service.CrowdService;
import com.smartstadium.validation.ZoneValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for crowd density data.
 *
 * <p>Provides endpoints to query real-time crowd density
 * information for stadium zones.</p>
 */
@RestController
@RequestMapping("/api/crowd-density")
public class CrowdController {

    private final CrowdService crowdService;
    private final ZoneValidator zoneValidator;

    public CrowdController(CrowdService crowdService, ZoneValidator zoneValidator) {
        this.crowdService = crowdService;
        this.zoneValidator = zoneValidator;
    }

    /**
     * Returns crowd density data for all stadium zones.
     *
     * @return list of crowd density DTOs
     */
    @GetMapping
    public ResponseEntity<List<CrowdDensityDto>> getAllDensities() {
        List<CrowdDensityDto> densities = crowdService.getAllDensities();
        return ResponseEntity.ok(densities);
    }

    /**
     * Returns crowd density data for a specific zone.
     *
     * @param zone the zone identifier (case-insensitive)
     * @return the crowd density DTO for the specified zone
     */
    @GetMapping("/{zone}")
    public ResponseEntity<CrowdDensityDto> getDensity(@PathVariable String zone) {
        Zone parsedZone = zoneValidator.parseZone(zone);
        CrowdDensityDto density = crowdService.getDensity(parsedZone);
        return ResponseEntity.ok(density);
    }
}
