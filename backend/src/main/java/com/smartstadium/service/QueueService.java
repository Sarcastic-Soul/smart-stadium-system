package com.smartstadium.service;

import com.smartstadium.dto.QueueWaitTimeDto;
import com.smartstadium.model.CrowdData;
import com.smartstadium.model.DensityLevel;
import com.smartstadium.model.QueueData;
import com.smartstadium.model.Zone;
import com.smartstadium.repository.StadiumRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.smartstadium.config.CacheConfig.QUEUE_WAIT_TIME_CACHE;

/**
 * Service for predicting queue wait times at stadium zones.
 *
 * <p>Wait time is calculated using the formula:
 * {@code waitTime = queueLength × avgServiceTime × congestionFactor}
 * where the congestion factor is derived from the zone's crowd density.</p>
 *
 * <p>The formula is modular — override {@link #calculateWaitTimeSeconds}
 * to implement custom prediction logic (e.g., ML-based models).</p>
 */
@Service
public class QueueService {

    private static final Logger logger = LoggerFactory.getLogger(QueueService.class);

    private final StadiumRepository repository;
    private final CrowdService crowdService;

    public QueueService(StadiumRepository repository, CrowdService crowdService) {
        this.repository = repository;
        this.crowdService = crowdService;
    }

    /**
     * Retrieves estimated wait times for all zones.
     *
     * @return list of wait time DTOs for every zone
     */
    @Cacheable(value = QUEUE_WAIT_TIME_CACHE, key = "'all'")
    public List<QueueWaitTimeDto> getAllWaitTimes() {
        logger.debug("Fetching wait times for all zones");
        Map<Zone, QueueData> allQueues = repository.getAllQueueData();

        return Arrays.stream(Zone.values())
                .map(zone -> {
                    QueueData queueData = allQueues.getOrDefault(
                            zone, new QueueData(zone, 0, getDefaultServiceTime(zone)));
                    return toDto(zone, queueData);
                })
                .toList();
    }

    /**
     * Retrieves the estimated wait time for a specific zone.
     *
     * @param zone the zone to query
     * @return the wait time DTO
     */
    @Cacheable(value = QUEUE_WAIT_TIME_CACHE, key = "#zone.name()")
    public QueueWaitTimeDto getWaitTime(Zone zone) {
        logger.debug("Fetching wait time for zone: {}", zone);
        QueueData queueData = repository.getQueueData(zone)
                .orElse(new QueueData(zone, 0, getDefaultServiceTime(zone)));
        return toDto(zone, queueData);
    }

    /**
     * Updates queue data for a zone and evicts related caches.
     */
    @CacheEvict(value = QUEUE_WAIT_TIME_CACHE, allEntries = true)
    public void updateQueueData(Zone zone, int queueLength, double avgServiceTime) {
        logger.info("Updating queue data for zone {}: length={}, avgService={}s",
                zone, queueLength, avgServiceTime);
        QueueData data = new QueueData(zone, queueLength, avgServiceTime);
        repository.saveQueueData(zone, data);
    }

    /**
     * Calculates the estimated wait time in seconds.
     *
     * <p>Formula: {@code queueLength × avgServiceTime × congestionFactor}</p>
     *
     * <p>This method is intentionally modular to allow future replacement
     * with ML-based prediction models or more sophisticated formulas.</p>
     *
     * @param queueLength    number of people in the queue
     * @param avgServiceTime average time to serve one person (seconds)
     * @param densityLevel   current crowd density level of the zone
     * @return estimated wait time in seconds
     */
    int calculateWaitTimeSeconds(int queueLength, double avgServiceTime,
                                         DensityLevel densityLevel) {
        double congestionFactor = getCongestionFactor(densityLevel);
        double rawWaitTime = queueLength * avgServiceTime * congestionFactor;
        return (int) Math.ceil(rawWaitTime);
    }

    /**
     * Returns the congestion factor based on crowd density.
     *
     * <p>Higher density means slower service due to crowding effects.</p>
     */
    double getCongestionFactor(DensityLevel level) {
        return switch (level) {
            case LOW -> 1.0;
            case MEDIUM -> 1.2;
            case HIGH -> 1.5;
            case CRITICAL -> 2.0;
        };
    }

    /**
     * Returns default average service times per zone type.
     */
    double getDefaultServiceTime(Zone zone) {
        return switch (zone) {
            case FOOD_COURT_EAST, FOOD_COURT_WEST -> 45.0;  // ~45s per food order
            case RESTROOM_NORTH, RESTROOM_SOUTH -> 90.0;    // ~90s restroom usage
            case GATE_A, GATE_B, GATE_C -> 15.0;            // ~15s gate entry
            case VIP_LOUNGE -> 30.0;                         // ~30s VIP check-in
            default -> 10.0;                                 // ~10s general transit
        };
    }

    private QueueWaitTimeDto toDto(Zone zone, QueueData queueData) {
        CrowdData crowdData = crowdService.getCrowdData(zone);
        DensityLevel density = crowdData.getDensityLevel();

        int waitSeconds = calculateWaitTimeSeconds(
                queueData.getQueueLength(),
                queueData.getAvgServiceTimeSeconds(),
                density
        );

        return new QueueWaitTimeDto(
                zone.name(),
                zone.getDisplayName(),
                queueData.getQueueLength(),
                waitSeconds,
                density,
                queueData.getTimestamp()
        );
    }
}
