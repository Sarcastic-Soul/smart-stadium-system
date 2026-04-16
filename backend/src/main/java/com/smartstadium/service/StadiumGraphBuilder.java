package com.smartstadium.service;

import com.smartstadium.model.Zone;

import java.util.*;

/**
 * Builds and maintains the stadium zone connectivity graph.
 *
 * <p>The graph represents physical connections between stadium zones,
 * with edge weights based on estimated walking distance (in meters).
 * This graph is used by {@link RoutingService} for pathfinding.</p>
 */
public final class StadiumGraphBuilder {

    private StadiumGraphBuilder() {
        // Utility class — prevent instantiation
    }

    /**
     * Represents a weighted edge in the stadium graph.
     *
     * @param destination the destination zone
     * @param weight      the base travel weight (roughly meters of walking distance)
     */
    public record Edge(Zone destination, double weight) {
    }

    /**
     * Builds the stadium zone graph with realistic connections.
     *
     * <p>Layout concept:
     * <pre>
     *   GATE_A ── MAIN_CONCOURSE ── GATE_B
     *                  │
     *            ┌─────┼─────┐
     *            │     │     │
     *     FOOD_COURT_WEST  GATE_C  FOOD_COURT_EAST
     *            │                    │
     *     RESTROOM_SOUTH        RESTROOM_NORTH
     *            │     │     │
     *            └─────┼─────┘
     *                  │
     *         SEATING_SOUTH  SEATING_NORTH
     *                  │
     *             VIP_LOUNGE
     * </pre></p>
     *
     * @return adjacency list representation of the stadium graph
     */
    public static Map<Zone, List<Edge>> buildGraph() {
        Map<Zone, List<Edge>> graph = new EnumMap<>(Zone.class);

        for (Zone zone : Zone.values()) {
            graph.put(zone, new ArrayList<>());
        }

        // Gate connections to Main Concourse
        addBidirectionalEdge(graph, Zone.GATE_A, Zone.MAIN_CONCOURSE, 50);
        addBidirectionalEdge(graph, Zone.GATE_B, Zone.MAIN_CONCOURSE, 50);
        addBidirectionalEdge(graph, Zone.GATE_C, Zone.MAIN_CONCOURSE, 60);

        // Main Concourse to Food Courts
        addBidirectionalEdge(graph, Zone.MAIN_CONCOURSE, Zone.FOOD_COURT_EAST, 80);
        addBidirectionalEdge(graph, Zone.MAIN_CONCOURSE, Zone.FOOD_COURT_WEST, 80);

        // Food Courts to Restrooms
        addBidirectionalEdge(graph, Zone.FOOD_COURT_EAST, Zone.RESTROOM_NORTH, 30);
        addBidirectionalEdge(graph, Zone.FOOD_COURT_WEST, Zone.RESTROOM_SOUTH, 30);

        // Main Concourse to Seating
        addBidirectionalEdge(graph, Zone.MAIN_CONCOURSE, Zone.SEATING_NORTH, 120);
        addBidirectionalEdge(graph, Zone.MAIN_CONCOURSE, Zone.SEATING_SOUTH, 120);

        // Seating to VIP
        addBidirectionalEdge(graph, Zone.SEATING_NORTH, Zone.VIP_LOUNGE, 40);
        addBidirectionalEdge(graph, Zone.SEATING_SOUTH, Zone.VIP_LOUNGE, 60);

        // Cross connections for alternative routes
        addBidirectionalEdge(graph, Zone.SEATING_NORTH, Zone.FOOD_COURT_EAST, 100);
        addBidirectionalEdge(graph, Zone.SEATING_SOUTH, Zone.FOOD_COURT_WEST, 100);
        addBidirectionalEdge(graph, Zone.RESTROOM_NORTH, Zone.RESTROOM_SOUTH, 150);

        return Collections.unmodifiableMap(graph);
    }

    private static void addBidirectionalEdge(Map<Zone, List<Edge>> graph,
                                              Zone from, Zone to, double weight) {
        graph.get(from).add(new Edge(to, weight));
        graph.get(to).add(new Edge(from, weight));
    }
}
