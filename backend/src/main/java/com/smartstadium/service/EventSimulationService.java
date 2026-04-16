package com.smartstadium.service;

import com.smartstadium.model.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulates real-time crowd movements and queue changes in the stadium.
 *
 * <p>This service runs on a scheduled interval, generating realistic
 * crowd fluctuations using bounded random walks. In a production environment,
 * this would be replaced by real sensor data published via Google Cloud Pub/Sub.</p>
 *
 * <p>When the {@code cloud} profile is active, updates are published to
 * the Pub/Sub topic {@code crowd-updates}. Otherwise, data is updated
 * directly in the repository via the service layer.</p>
 */
@Service
public class EventSimulationService {

    private static final Logger logger = LoggerFactory.getLogger(EventSimulationService.class);

    private final CrowdService crowdService;
    private final QueueService queueService;
    private final SimpMessagingTemplate messagingTemplate;

    /** Tracks the simulated crowd count per zone for bounded random walks. */
    private final Map<Zone, Integer> simulatedCrowdCounts = new EnumMap<>(Zone.class);

    /** Tracks the simulated queue length per zone. */
    private final Map<Zone, Integer> simulatedQueueLengths = new EnumMap<>(Zone.class);

    public EventSimulationService(CrowdService crowdService, QueueService queueService, SimpMessagingTemplate messagingTemplate) {
        this.crowdService = crowdService;
        this.queueService = queueService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Seeds the initial crowd and queue data for all zones on startup.
     */
    @PostConstruct
    public void initializeData() {
        logger.info("Seeding initial stadium data for all {} zones", Zone.values().length);

        for (Zone zone : Zone.values()) {
            int initialCount = generateInitialCount(zone);
            simulatedCrowdCounts.put(zone, initialCount);
            crowdService.updateCrowdData(zone, initialCount);

            int initialQueue = generateInitialQueueLength(zone);
            simulatedQueueLengths.put(zone, initialQueue);
            double serviceTime = queueService.getDefaultServiceTime(zone);
            queueService.updateQueueData(zone, initialQueue, serviceTime);
        }

        logger.info("Stadium data initialization complete");
    }

    /**
     * Periodically updates crowd and queue data to simulate real-time changes.
     * Runs every 10 seconds.
     */
    @Scheduled(fixedRate = 10_000, initialDelay = 15_000)
    public void simulateCrowdMovements() {
        logger.debug("Simulating crowd movement update");

        for (Zone zone : Zone.values()) {
            updateCrowdForZone(zone);
            updateQueueForZone(zone);
        }
        
        // Broadcast an update event map to WebSocket clients telling them to refresh data
        messagingTemplate.convertAndSend("/topic/telemetry", "REFRESH");
    }

    /**
     * Updates crowd count using a bounded random walk.
     *
     * <p>The change is constrained to ±(5% of capacity) per tick,
     * and the result is clamped between 0 and capacity.</p>
     */
    private void updateCrowdForZone(Zone zone) {
        int current = simulatedCrowdCounts.getOrDefault(zone, 0);
        int maxDelta = Math.max(1, (int) (zone.getCapacity() * 0.05));
        int delta = ThreadLocalRandom.current().nextInt(-maxDelta, maxDelta + 1);

        int newCount = Math.max(0, Math.min(zone.getCapacity(), current + delta));
        simulatedCrowdCounts.put(zone, newCount);
        crowdService.updateCrowdData(zone, newCount);
    }

    /**
     * Updates queue length using a bounded random walk.
     */
    private void updateQueueForZone(Zone zone) {
        int currentQueue = simulatedQueueLengths.getOrDefault(zone, 0);
        int maxQueueDelta = Math.max(1, currentQueue / 5 + 1);
        int delta = ThreadLocalRandom.current().nextInt(-maxQueueDelta, maxQueueDelta + 1);

        int maxQueue = getMaxQueueForZone(zone);
        int newQueue = Math.max(0, Math.min(maxQueue, currentQueue + delta));
        simulatedQueueLengths.put(zone, newQueue);

        double serviceTime = queueService.getDefaultServiceTime(zone);
        queueService.updateQueueData(zone, newQueue, serviceTime);
    }

    /**
     * Generates a realistic initial crowd count based on zone type.
     */
    private int generateInitialCount(Zone zone) {
        double minRatio = 0.2;
        double maxRatio = 0.6;
        int min = (int) (zone.getCapacity() * minRatio);
        int max = (int) (zone.getCapacity() * maxRatio);
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Generates a realistic initial queue length based on zone type.
     */
    private int generateInitialQueueLength(Zone zone) {
        return switch (zone) {
            case FOOD_COURT_EAST, FOOD_COURT_WEST ->
                    ThreadLocalRandom.current().nextInt(5, 25);
            case RESTROOM_NORTH, RESTROOM_SOUTH ->
                    ThreadLocalRandom.current().nextInt(3, 15);
            case GATE_A, GATE_B, GATE_C ->
                    ThreadLocalRandom.current().nextInt(10, 40);
            case VIP_LOUNGE ->
                    ThreadLocalRandom.current().nextInt(0, 8);
            default ->
                    ThreadLocalRandom.current().nextInt(0, 5);
        };
    }

    /**
     * Returns the maximum realistic queue length for a zone.
     */
    private int getMaxQueueForZone(Zone zone) {
        return switch (zone) {
            case FOOD_COURT_EAST, FOOD_COURT_WEST -> 50;
            case RESTROOM_NORTH, RESTROOM_SOUTH -> 30;
            case GATE_A, GATE_B, GATE_C -> 80;
            case VIP_LOUNGE -> 15;
            default -> 10;
        };
    }
}
