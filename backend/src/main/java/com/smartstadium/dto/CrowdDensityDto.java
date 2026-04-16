package com.smartstadium.dto;

import com.smartstadium.model.DensityLevel;
import java.time.Instant;

/**
 * Data Transfer Object for crowd density API responses.
 *
 * @param zone           the zone identifier
 * @param displayName    the human-readable zone name
 * @param currentCount   the current number of people in the zone
 * @param capacity       the maximum capacity of the zone
 * @param occupancyRate  the occupancy ratio (0.0 to 1.0)
 * @param densityLevel   the classified density level
 * @param timestamp      when this data was recorded
 */
public record CrowdDensityDto(
        String zone,
        String displayName,
        int currentCount,
        int capacity,
        double occupancyRate,
        DensityLevel densityLevel,
        Instant timestamp
) {
}
