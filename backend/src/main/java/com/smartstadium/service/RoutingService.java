package com.smartstadium.service;

import com.smartstadium.config.RoutingProperties;
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
 * Service for calculating optimal routes between stadium zones using A* algorithm.
 *
 * <p>Uses A* pathfinding on a weighted graph where edge weights are adjusted by
 * the crowd density of destination zones. A heuristic function (Euclidean distance)
 * is used to prioritize paths heading towards the target destination.</p>
 */
@Service
public class RoutingService {

    private static final Logger logger = LoggerFactory.getLogger(RoutingService.class);

    private final Map<Zone, List<Edge>> graph;
    private final CrowdService crowdService;
    private final RoutingProperties routingProperties;
    private final Optional<RemoteConfigService> remoteConfigService;

    public RoutingService(CrowdService crowdService, 
                          RoutingProperties routingProperties,
                          Optional<RemoteConfigService> remoteConfigService) {
        this.crowdService = crowdService;
        this.routingProperties = routingProperties;
        this.remoteConfigService = remoteConfigService;
        this.graph = StadiumGraphBuilder.buildGraph();
        logger.info("A* Routing service initialized. Base walking speed: {} m/s", 
                routingProperties.getWalkingSpeed());
    }

    /**
     * Finds the optimal route between two zones using the A* algorithm.
     *
     * @param from the starting zone
     * @param to   the destination zone
     * @return the route DTO with path and estimated travel time
     * @throws IllegalArgumentException if no route exists between the zones
     */
    public RouteDto findRoute(Zone from, Zone to) {
        logger.debug("Finding A* route from {} to {}", from, to);

        if (from == to) {
            return new RouteDto(
                    from.name(), to.name(),
                    List.of(from.name()),
                    List.of(from.getDisplayName()),
                    0, 0.0
            );
        }

        // A* algorithm implementation
        Map<Zone, Double> gScore = new EnumMap<>(Zone.class); // Cost from start to current
        Map<Zone, Double> fScore = new EnumMap<>(Zone.class); // Estimated total cost (g + h)
        Map<Zone, Zone> predecessors = new EnumMap<>(Zone.class);
        
        // Priority Queue ordered by fScore
        PriorityQueue<Zone> openSet = new PriorityQueue<>(Comparator.comparingDouble(z -> fScore.getOrDefault(z, Double.MAX_VALUE)));

        for (Zone zone : Zone.values()) {
            gScore.put(zone, Double.MAX_VALUE);
            fScore.put(zone, Double.MAX_VALUE);
        }

        gScore.put(from, 0.0);
        fScore.put(from, calculateHeuristic(from, to));
        openSet.add(from);

        while (!openSet.isEmpty()) {
            Zone current = openSet.poll();

            if (current == to) {
                break;
            }

            List<Edge> edges = graph.get(current);
            if (edges == null) continue;

            for (Edge edge : edges) {
                double tentativeGScore = gScore.get(current) + calculateAdjustedWeight(edge);

                if (tentativeGScore < gScore.get(edge.destination())) {
                    predecessors.put(edge.destination(), current);
                    gScore.put(edge.destination(), tentativeGScore);
                    fScore.put(edge.destination(), tentativeGScore + calculateHeuristic(edge.destination(), to));
                    
                    if (!openSet.contains(edge.destination())) {
                        openSet.add(edge.destination());
                    }
                }
            }
        }

        if (gScore.get(to) == Double.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "No route found from " + from.getDisplayName() + " to " + to.getDisplayName());
        }

        List<Zone> path = reconstructPath(predecessors, from, to);
        double totalWeight = gScore.get(to);
        int estimatedTimeSeconds = (int) Math.ceil(totalWeight / routingProperties.getWalkingSpeed());

        return new RouteDto(
                from.name(), to.name(),
                path.stream().map(Zone::name).toList(),
                path.stream().map(Zone::getDisplayName).toList(),
                estimatedTimeSeconds,
                Math.round(totalWeight * 100.0) / 100.0
        );
    }

    /**
     * Euclidean distance heuristic for A*.
     */
    private double calculateHeuristic(Zone current, Zone goal) {
        double dx = current.getX() - goal.getX();
        double dy = current.getY() - goal.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    double calculateAdjustedWeight(Edge edge) {
        CrowdData crowdData = crowdService.getCrowdData(edge.destination());
        double multiplier = getDensityMultiplier(crowdData.getDensityLevel());
        return edge.weight() * multiplier;
    }

    private double getDensityMultiplier(DensityLevel level) {
        // Check for real-time overrides from Firestore first
        if (remoteConfigService.isPresent()) {
            Optional<Double> override = remoteConfigService.get().getMultiplierOverride(level);
            if (override.isPresent()) {
                return override.get();
            }
        }
        
        // Fallback to configured properties
        return routingProperties.getMultipliers().getOrDefault(level, 1.0);
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
}
