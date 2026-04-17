package com.smartstadium.service;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.smartstadium.model.DensityLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing real-time configuration overrides from Firestore.
 *
 * <p>Allows an administrator to tune algorithm sensitivity (density multipliers)
 * in real-time without restarting the backend service.</p>
 */
@Service
@Profile("cloud")
public class RemoteConfigService {

    private static final Logger logger = LoggerFactory.getLogger(RemoteConfigService.class);
    private static final String CONFIG_COLLECTION = "system_config";
    private static final String ROUTING_DOC = "routing_multipliers";

    private final Firestore firestore;
    private final Map<DensityLevel, Double> multiplierOverrides = new ConcurrentHashMap<>();

    public RemoteConfigService(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Periodically refreshes the configuration from Firestore every 30 seconds.
     */
    @Scheduled(fixedRate = 30_000)
    public void refreshConfig() {
        logger.debug("Refreshing remote configuration from Firestore");
        try {
            DocumentSnapshot doc = firestore.collection(CONFIG_COLLECTION)
                    .document(ROUTING_DOC)
                    .get()
                    .get();

            if (doc.exists()) {
                for (DensityLevel level : DensityLevel.values()) {
                    Double val = doc.getDouble(level.name());
                    if (val != null) {
                        multiplierOverrides.put(level, val);
                    }
                }
                logger.info("Updated routing multipliers from Firestore: {}", multiplierOverrides);
            }
        } catch (Exception e) {
            logger.warn("Failed to refresh remote config: {}", e.getMessage());
        }
    }

    public Optional<Double> getMultiplierOverride(DensityLevel level) {
        return Optional.ofNullable(multiplierOverrides.get(level));
    }
}
