package com.smartstadium.service;

import com.smartstadium.config.RoutingProperties;
import com.smartstadium.dto.RouteDto;
import com.smartstadium.model.CrowdData;
import com.smartstadium.model.DensityLevel;
import com.smartstadium.model.Zone;
import com.smartstadium.service.StadiumGraphBuilder.Edge;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for calculating optimal routes between stadium zones using A* algorithm.
 *
 * <p>Uses A* pathfinding on a weighted graph where edge weights are adjusted by
 * the crowd density of destination zones. A heuristic function (Euclidean distance)
 * is used to prioritize paths heading towards the target destination.</p>
 */
@Service
public class RoutingService {

    private static final Logger logger = LoggerFactory.getLogger(
        RoutingService.class
    );

    private final Map<Zone, List<Edge>> graph;
    private final CrowdService crowdService;
    private final RoutingProperties routingProperties;
    private final Optional<RemoteConfigService> remoteConfigService;

    public RoutingService(
        CrowdService crowdService,
        RoutingProperties routingProperties,
        Optional<RemoteConfigService> remoteConfigService
    ) {
        this.crowdService = crowdService;
        this.routingProperties = routingProperties;
        this.remoteConfigService = remoteConfigService;
        this.graph = StadiumGraphBuilder.buildGraph();
        logger.info(
            "A* Routing service initialized. Base walking speed: {} m/s",
            routingProperties.getWalkingSpeed()
        );
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
                from.name(),
                to.name(),
                List.of(from.name()),
                List.of(from.getDisplayName()),
                0,
                0.0
            );
        }

        // A* algorithm implementation
        Map<Zone, Double> gScore = new EnumMap<>(Zone.class); // Cost from start to current
        Map<Zone, Double> fScore = new EnumMap<>(Zone.class); // Estimated total cost (g + h)
        Map<Zone, Zone> predecessors = new EnumMap<>(Zone.class);

        // Priority Queue ordered by fScore
        PriorityQueue<State> openSet = new PriorityQueue<>();

        for (Zone zone : Zone.values()) {
            gScore.put(zone, Double.MAX_VALUE);
            fScore.put(zone, Double.MAX_VALUE);
        }

        gScore.put(from, 0.0);
        fScore.put(from, calculateHeuristic(from, to));
        openSet.add(new State(from, fScore.get(from)));

        while (!openSet.isEmpty()) {
            State currentState = openSet.poll();
            Zone current = currentState.zone();

            // Lazy deletion: if we found a shorter path to this zone before popping this state, ignore
            if (currentState.fScore() > fScore.get(current)) {
                continue;
            }

            if (current == to) {
                break;
            }

            evaluateNeighbors(
                current,
                to,
                gScore,
                fScore,
                predecessors,
                openSet
            );
        }

        if (gScore.get(to) == Double.MAX_VALUE) {
            throw new IllegalArgumentException(
                "No route found from " +
                    from.getDisplayName() +
                    " to " +
                    to.getDisplayName()
            );
        }

        List<Zone> path = reconstructPath(predecessors, from, to);
        double totalWeight = gScore.get(to);
        int estimatedTimeSeconds = (int) Math.ceil(
            totalWeight / routingProperties.getWalkingSpeed()
        );

        return new RouteDto(
            from.name(),
            to.name(),
            path.stream().map(Zone::name).toList(),
            path.stream().map(Zone::getDisplayName).toList(),
            estimatedTimeSeconds,
            Math.round(totalWeight * 100.0) / 100.0
        );
    }

    private record State(
        Zone zone,
        double fScore
    ) implements Comparable<State> {
        @Override
        public int compareTo(State o) {
            return Double.compare(this.fScore, o.fScore);
        }
    }

    private void evaluateNeighbors(
        Zone current,
        Zone to,
        Map<Zone, Double> gScore,
        Map<Zone, Double> fScore,
        Map<Zone, Zone> predecessors,
        PriorityQueue<State> openSet
    ) {
        List<Edge> edges = graph.get(current);
        if (edges == null) return;

        for (Edge edge : edges) {
            double tentativeGScore =
                gScore.get(current) + calculateAdjustedWeight(edge);

            if (tentativeGScore < gScore.get(edge.destination())) {
                predecessors.put(edge.destination(), current);
                gScore.put(edge.destination(), tentativeGScore);
                double newFScore =
                    tentativeGScore +
                    calculateHeuristic(edge.destination(), to);
                fScore.put(edge.destination(), newFScore);

                // Add state without checking contains (lazy deletion handles duplicates)
                openSet.add(new State(edge.destination(), newFScore));
            }
        }
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
            Optional<Double> override = remoteConfigService
                .get()
                .getMultiplierOverride(level);
            if (override.isPresent()) {
                return override.get();
            }
        }

        // Fallback to configured properties
        return routingProperties.getMultipliers().getOrDefault(level, 1.0);
    }

    private List<Zone> reconstructPath(
        Map<Zone, Zone> predecessors,
        Zone from,
        Zone to
    ) {
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
