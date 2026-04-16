package com.smartstadium.config;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Firestore client configuration for Google Cloud.
 *
 * <p>Active only under the {@code cloud} profile. For local development,
 * the application uses an in-memory data store instead.</p>
 */
@Configuration
@Profile("cloud")
public class FirestoreConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirestoreConfig.class);

    @Value("${spring.cloud.gcp.project-id:}")
    private String projectId;

    @Bean
    public Firestore firestore() {
        logger.info("Initializing Firestore client for project: {}", projectId);
        FirestoreOptions.Builder builder = FirestoreOptions.getDefaultInstance().toBuilder();
        if (projectId != null && !projectId.isBlank()) {
            builder.setProjectId(projectId);
        }
        return builder.build().getService();
    }
}
