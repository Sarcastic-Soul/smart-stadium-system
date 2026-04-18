package com.smartstadium;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the AI-Powered Smart Stadium System.
 *
 * <p>This application provides real-time crowd management, route optimization,
 * and queue prediction for large-scale sporting venues. It integrates with
 * Google Cloud services (Firestore, Pub/Sub) and is deployable on Cloud Run.</p>
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableRetry
public class SmartStadiumApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartStadiumApplication.class, args);
    }
}
