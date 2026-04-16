package com.smartstadium.config;

import com.google.cloud.firestore.Firestore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Provides a no-op Firestore bean for the {@code test} profile.
 * This prevents Spring Boot from attempting to auto-configure the real GCP Firestore client
 * during unit and integration tests where the {@code cloud} profile is not active.
 */
@Configuration
@Profile("test")
public class TestFirestoreConfig {

    @Bean
    public Firestore firestore() {
        // Return a mock or stub implementation. For simplicity, we return null because the
        // repository beans that depend on Firestore are excluded from the test profile.
        // Any accidental injection will fail fast, making the missing bean obvious.
        return null;
    }
}
