package com.smartstadium.config;

import com.smartstadium.model.DensityLevel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.Map;

/**
 * Configuration properties for the stadium routing algorithm.
 *
 * <p>Allows tuning walking speeds and crowd density penalties
 * without changing the source code.</p>
 */
@Configuration
@ConfigurationProperties(prefix = "smartstadium.routing")
public class RoutingProperties {

    /**
     * Average walking speed in meters per second.
     */
    private double walkingSpeed = 1.2;

    /**
     * Multipliers for edge weights based on crowd density levels.
     */
    private Map<DensityLevel, Double> multipliers = new EnumMap<>(DensityLevel.class);

    public RoutingProperties() {
        // Default multipliers
        multipliers.put(DensityLevel.LOW, 1.0);
        multipliers.put(DensityLevel.MEDIUM, 1.3);
        multipliers.put(DensityLevel.HIGH, 1.8);
        multipliers.put(DensityLevel.CRITICAL, 2.5);
    }

    public double getWalkingSpeed() {
        return walkingSpeed;
    }

    public void setWalkingSpeed(double walkingSpeed) {
        this.walkingSpeed = walkingSpeed;
    }

    public Map<DensityLevel, Double> getMultipliers() {
        return multipliers;
    }

    public void setMultipliers(Map<DensityLevel, Double> multipliers) {
        this.multipliers = multipliers;
    }
}
