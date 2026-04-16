package com.smartstadium.repository;

import com.smartstadium.model.CrowdData;
import com.smartstadium.model.QueueData;
import com.smartstadium.model.Zone;

import java.util.Map;
import java.util.Optional;

/**
 * Interface for stadium data persistence operations.
 *
 * <p>Implementations include Firestore (cloud profile) and
 * in-memory (local/default profile) data stores.</p>
 */
public interface StadiumRepository {

    /**
     * Saves crowd data for a specific zone.
     */
    void saveCrowdData(Zone zone, CrowdData data);

    /**
     * Retrieves crowd data for a specific zone.
     */
    Optional<CrowdData> getCrowdData(Zone zone);

    /**
     * Retrieves crowd data for all zones.
     */
    Map<Zone, CrowdData> getAllCrowdData();

    /**
     * Saves queue data for a specific zone.
     */
    void saveQueueData(Zone zone, QueueData data);

    /**
     * Retrieves queue data for a specific zone.
     */
    Optional<QueueData> getQueueData(Zone zone);

    /**
     * Retrieves queue data for all zones.
     */
    Map<Zone, QueueData> getAllQueueData();
}
