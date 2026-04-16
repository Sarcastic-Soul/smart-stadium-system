package com.smartstadium.service;

import com.smartstadium.dto.RouteDto;
import com.smartstadium.model.CrowdData;
import com.smartstadium.model.DensityLevel;
import com.smartstadium.model.Zone;
import com.smartstadium.service.StadiumGraphBuilder.Edge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for calculating optimal routes between stadium zones.
 *
 * <p>Uses Dijkstra's algorithm on a weighted graph where edge weights
 * are adjusted by the crowd density of destination zones. This naturally
 * routes people away from congested areas.</p>
 */
@Service
public class RoutingService {

    private static final Logger logger = LoggerFactory.getLogger(RoutingService.class);

    /**
     * Average walking speed in meters per second (approx 1.2 m/s for crowded venues).
     */
    private static final double WALKING_SPEED_MPS = 1.2;

    private final Map<Zone, List<Edge>> graph;
    private final CrowdService crowdService;

    public RoutingService(CrowdService crowdService) {
        this.crowdService = crowdService;
        this.graph = StadiumGraphBuilder.buildGraph();
        logger.info("Routing service initialized with {} zones", graph.size());
    }

    /**
     * Finds the optimal route between two zones using Dijkstra's algorithm.
     *
     * <p>Edge weights are dynamically adjusted based on crowd density at
     * the destination zone. High-density zones incur a penalty, making
     * the algorithm prefer less congested paths.</p>
     *
     * @param from the starting zone
     * @param to   the destination zone
     * @return the route DTO with path and estimated travel time
     * @throws IllegalArgumentException if no route exists between the zones
     */
    public RouteDto findRoute(Zone from, Zone to) {
        logger.debug("Finding route from {} to {}", from, to);

        if (from == to) {
            return new RouteDto(
                    from.name(), to.name(),
                    List.of(from.name()),
                    List.of(from.getDisplayName()),
                    0, 0.0
            );
        }

        // Dijkstra's algorithm
        Map<Zone, Double> distances = new EnumMap<>(Zone.class);
        Map<Zone, Zone> predecessors = new EnumMap<>(Zone.class);
        PriorityQueue<ZoneDistance> queue = new PriorityQueue<>(
                Comparator.comparingDouble(ZoneDistance::distance));

        for (Zone zone : Zone.values()) {
            distances.put(zone, Double.MAX_VALUE);
        }
        distances.put(from, 0.0);
        queue.add(new ZoneDistance(from, 0.0));

        Set<Zone> visited = EnumSet.noneOf(Zone.class);

        while (!queue.isEmpty()) {
            ZoneDistance current = queue.poll();
            Zone currentZone = current.zone();

            if (visited.contains(currentZone)) {
                continue;
            }
            visited.add(currentZone);

            if (currentZone == to) {
                break;
            }

            List<Edge> edges = graph.get(currentZone);
            if (edges == null) continue;

            for (Edge edge : edges) {
                if (visited.contains(edge.destination())) continue;

                double adjustedWeight = calculateAdjustedWeight(edge);
                double newDist = distances.get(currentZone) + adjustedWeight;

                if (newDist < distances.get(edge.destination())) {
                    distances.put(edge.destination(), newDist);
                    predecessors.put(edge.destination(), currentZone);
                    queue.add(new ZoneDistance(edge.destination(), newDist));
                }
            }
        }

        if (distances.get(to) == Double.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "No route found from " + from.getDisplayName() + " to " + to.getDisplayName());
        }

        // Reconstruct path
        List<Zone> path = reconstructPath(predecessors, from, to);
        double totalWeight = distances.get(to);
        int estimatedTimeSeconds = (int) Math.ceil(totalWeight / WALKING_SPEED_MPS);

        logger.debug("Route found: {} -> {}, {} steps, ~{}s",
                from, to, path.size(), estimatedTimeSeconds);

        return new RouteDto(
                from.name(),
                to.name(),
                path.stream().map(Zone::name).toList(),
                path.stream().map(Zone::getDisplayName).toList(),
                estimatedTimeSeconds,
                Math.round(totalWeight * 100.0) / 100.0
        );
    }

    /**
     * Calculates the crowd-adjusted weight for an edge.
     *
     * <p>Density multiplier: LOW=1.0, MEDIUM=1.3, HIGH=1.8, CRITICAL=2.5</p>
     */
    double calculateAdjustedWeight(Edge edge) {
        CrowdData crowdData = crowdService.getCrowdData(edge.destination());
        double densityMultiplier = getDensityMultiplier(crowdData.getDensityLevel());
        return edge.weight() * densityMultiplier;
    }

    /**
     * Returns the congestion multiplier for a given density level.
     */
    static double getDensityMultiplier(DensityLevel level) {
        return switch (level) {
            case LOW -> 1.0;
            case MEDIUM -> 1.3;
            case HIGH -> 1.8;
            case CRITICAL -> 2.5;
        };
    }

    private List<Zone> reconstructPath(Map<Zone, Zone> predecessors, Zone from, Zone to) {
        LinkedList<Zone> path = new LinkedList<>();
        Zone current = to;

        while (current != null) {
            path.addFirst(current);
            if (current == from) break;
            current = predecessors.get(current);
        }

        return Collections.unmodifiableList(path);
    }

    private record ZoneDistance(Zone zone, double distance) {
    }
}
