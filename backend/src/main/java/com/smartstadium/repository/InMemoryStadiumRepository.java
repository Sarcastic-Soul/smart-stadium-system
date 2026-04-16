package com.smartstadium.repository;

import com.smartstadium.model.CrowdData;
import com.smartstadium.model.QueueData;
import com.smartstadium.model.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of {@link StadiumRepository} for local development.
 *
 * <p>Uses {@link ConcurrentHashMap} for thread-safe access. Active when
 * the {@code cloud} profile is NOT active (i.e., local development).</p>
 */
@Repository
@Profile("!cloud")
public class InMemoryStadiumRepository implements StadiumRepository {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryStadiumRepository.class);

    private final Map<Zone, CrowdData> crowdDataStore = new ConcurrentHashMap<>();
    private final Map<Zone, QueueData> queueDataStore = new ConcurrentHashMap<>();

    public InMemoryStadiumRepository() {
        logger.info("Initialized in-memory stadium repository (local profile)");
    }

    @Override
    public void saveCrowdData(Zone zone, CrowdData data) {
        crowdDataStore.put(zone, data);
    }

    @Override
    public Optional<CrowdData> getCrowdData(Zone zone) {
        return Optional.ofNullable(crowdDataStore.get(zone));
    }

    @Override
    public Map<Zone, CrowdData> getAllCrowdData() {
        return Map.copyOf(crowdDataStore);
    }

    @Override
    public void saveQueueData(Zone zone, QueueData data) {
        queueDataStore.put(zone, data);
    }

    @Override
    public Optional<QueueData> getQueueData(Zone zone) {
        return Optional.ofNullable(queueDataStore.get(zone));
    }

    @Override
    public Map<Zone, QueueData> getAllQueueData() {
        return Map.copyOf(queueDataStore);
    }
}
