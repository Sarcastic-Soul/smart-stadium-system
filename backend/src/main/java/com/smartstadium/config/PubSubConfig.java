package com.smartstadium.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Google Cloud Pub/Sub configuration.
 *
 * <p>Active only under the {@code cloud} profile. Configures the topic
 * and subscription names used by the event simulation system.</p>
 */
@Configuration
@Profile("cloud")
public class PubSubConfig {

    private static final Logger logger = LoggerFactory.getLogger(PubSubConfig.class);

    public static final String CROWD_UPDATES_TOPIC = "crowd-updates";
    public static final String CROWD_UPDATES_SUBSCRIPTION = "crowd-updates-sub";

    @Value("${spring.cloud.gcp.project-id:}")
    private String projectId;

    public String getProjectId() {
        return projectId;
    }

    public String getCrowdUpdatesTopic() {
        return CROWD_UPDATES_TOPIC;
    }

    public String getCrowdUpdatesSubscription() {
        return CROWD_UPDATES_SUBSCRIPTION;
    }
}
