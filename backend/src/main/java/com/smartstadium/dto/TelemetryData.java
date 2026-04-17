package com.smartstadium.dto;

import java.util.List;

/**
 * Data Transfer Object for real-time telemetry updates.
 *
 * <p>Bundles crowd density and queue wait times into a single payload
 * to minimize WebSocket message overhead and eliminate redundant API calls.</p>
 *
 * @param crowdDensities list of current densities for all zones
 * @param queueWaitTimes list of current wait times for all zones
 */
public record TelemetryData(
        List<CrowdDensityDto> crowdDensities,
        List<QueueWaitTimeDto> queueWaitTimes
) {
}
