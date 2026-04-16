package com.smartstadium.repository;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.smartstadium.model.CrowdData;
import com.smartstadium.model.DensityLevel;
import com.smartstadium.model.QueueData;
import com.smartstadium.model.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Firestore-backed implementation of {@link StadiumRepository}.
 *
 * <p>Active when smartstadium.repository.type is set to 'firestore'.
 * Stores crowd and queue data in Firestore collections.</p>
 */
@Repository
@ConditionalOnProperty(name = "smartstadium.repository.type", havingValue = "firestore")
public class FirestoreStadiumRepository implements StadiumRepository {

    private static final Logger logger = LoggerFactory.getLogger(FirestoreStadiumRepository.class);
    private static final String CROWD_COLLECTION = "crowd_data";
    private static final String QUEUE_COLLECTION = "queue_data";

    private final Firestore firestore;

    public FirestoreStadiumRepository(Firestore firestore) {
        this.firestore = firestore;
        logger.info("Initialized Firestore stadium repository (cloud profile)");
    }

    @Override
    public void saveCrowdData(Zone zone, CrowdData data) {
        try {
            Map<String, Object> document = new HashMap<>();
            document.put("zone", zone.name());
            document.put("currentCount", data.getCurrentCount());
            document.put("capacity", data.getCapacity());
            document.put("densityLevel", data.getDensityLevel().name());
            document.put("timestamp", data.getTimestamp().toString());

            firestore.collection(CROWD_COLLECTION)
                    .document(zone.name())
                    .set(document)
                    .get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while saving crowd data for zone {}", zone, e);
        } catch (ExecutionException e) {
            logger.error("Failed to save crowd data for zone {}", zone, e);
        }
    }

    @Override
    public Optional<CrowdData> getCrowdData(Zone zone) {
        try {
            DocumentSnapshot doc = firestore.collection(CROWD_COLLECTION)
                    .document(zone.name())
                    .get()
                    .get();

            if (doc.exists()) {
                return Optional.of(documentToCrowdData(doc, zone));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while reading crowd data for zone {}", zone, e);
        } catch (ExecutionException e) {
            logger.error("Failed to read crowd data for zone {}", zone, e);
        }
        return Optional.empty();
    }

    @Override
    public Map<Zone, CrowdData> getAllCrowdData() {
        Map<Zone, CrowdData> result = new HashMap<>();
        try {
            var documents = firestore.collection(CROWD_COLLECTION)
                    .get()
                    .get()
                    .getDocuments();

            for (DocumentSnapshot doc : documents) {
                String zoneName = doc.getString("zone");
                if (zoneName != null) {
                    Zone zone = Zone.valueOf(zoneName);
                    result.put(zone, documentToCrowdData(doc, zone));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while reading all crowd data", e);
        } catch (ExecutionException e) {
            logger.error("Failed to read all crowd data", e);
        }
        return result;
    }

    @Override
    public void saveQueueData(Zone zone, QueueData data) {
        try {
            Map<String, Object> document = new HashMap<>();
            document.put("zone", zone.name());
            document.put("queueLength", data.getQueueLength());
            document.put("avgServiceTimeSeconds", data.getAvgServiceTimeSeconds());
            document.put("timestamp", data.getTimestamp().toString());

            firestore.collection(QUEUE_COLLECTION)
                    .document(zone.name())
                    .set(document)
                    .get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while saving queue data for zone {}", zone, e);
        } catch (ExecutionException e) {
            logger.error("Failed to save queue data for zone {}", zone, e);
        }
    }

    @Override
    public Optional<QueueData> getQueueData(Zone zone) {
        try {
            DocumentSnapshot doc = firestore.collection(QUEUE_COLLECTION)
                    .document(zone.name())
                    .get()
                    .get();

            if (doc.exists()) {
                return Optional.of(documentToQueueData(doc, zone));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while reading queue data for zone {}", zone, e);
        } catch (ExecutionException e) {
            logger.error("Failed to read queue data for zone {}", zone, e);
        }
        return Optional.empty();
    }

    @Override
    public Map<Zone, QueueData> getAllQueueData() {
        Map<Zone, QueueData> result = new HashMap<>();
        try {
            var documents = firestore.collection(QUEUE_COLLECTION)
                    .get()
                    .get()
                    .getDocuments();

            for (DocumentSnapshot doc : documents) {
                String zoneName = doc.getString("zone");
                if (zoneName != null) {
                    Zone zone = Zone.valueOf(zoneName);
                    result.put(zone, documentToQueueData(doc, zone));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while reading all queue data", e);
        } catch (ExecutionException e) {
            logger.error("Failed to read all queue data", e);
        }
        return result;
    }

    private CrowdData documentToCrowdData(DocumentSnapshot doc, Zone zone) {
        CrowdData data = new CrowdData();
        data.setZone(zone);
        data.setCapacity(zone.getCapacity());

        Long currentCount = doc.getLong("currentCount");
        data.setCurrentCount(currentCount != null ? currentCount.intValue() : 0);

        String densityStr = doc.getString("densityLevel");
        if (densityStr != null) {
            data.setDensityLevel(DensityLevel.valueOf(densityStr));
        }

        String timestampStr = doc.getString("timestamp");
        if (timestampStr != null) {
            data.setTimestamp(Instant.parse(timestampStr));
        }

        return data;
    }

    private QueueData documentToQueueData(DocumentSnapshot doc, Zone zone) {
        QueueData data = new QueueData();
        data.setZone(zone);

        Long queueLength = doc.getLong("queueLength");
        data.setQueueLength(queueLength != null ? queueLength.intValue() : 0);

        Double avgServiceTime = doc.getDouble("avgServiceTimeSeconds");
        data.setAvgServiceTimeSeconds(avgServiceTime != null ? avgServiceTime : 0.0);

        String timestampStr = doc.getString("timestamp");
        if (timestampStr != null) {
            data.setTimestamp(Instant.parse(timestampStr));
        }

        return data;
    }
}
