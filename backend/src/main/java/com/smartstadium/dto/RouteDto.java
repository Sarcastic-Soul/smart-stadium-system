package com.smartstadium.dto;

import java.util.List;

/**
 * Data Transfer Object for route calculation API responses.
 *
 * @param from               the starting zone identifier
 * @param to                 the destination zone identifier
 * @param path               the ordered list of zones in the route
 * @param pathDisplayNames   the ordered list of zone display names
 * @param estimatedTimeSeconds the estimated travel time in seconds
 * @param totalWeight        the total graph weight of the path
 */
public record RouteDto(
        String from,
        String to,
        List<String> path,
        List<String> pathDisplayNames,
        int estimatedTimeSeconds,
        double totalWeight
) {
}
