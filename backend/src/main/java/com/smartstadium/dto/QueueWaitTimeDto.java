package com.smartstadium.dto;

import com.smartstadium.model.DensityLevel;
import java.time.Instant;

/**
 * Data Transfer Object for queue wait time API responses.
 *
 * @param zone                  the zone identifier
 * @param displayName           the human-readable zone name
 * @param queueLength           the current number of people in the queue
 * @param estimatedWaitSeconds  the estimated wait time in seconds
 * @param densityLevel          the current density level of the zone
 * @param timestamp             when this data was recorded
 */
public record QueueWaitTimeDto(
        String zone,
        String displayName,
        int queueLength,
        int estimatedWaitSeconds,
        DensityLevel densityLevel,
        Instant timestamp
) {
}
