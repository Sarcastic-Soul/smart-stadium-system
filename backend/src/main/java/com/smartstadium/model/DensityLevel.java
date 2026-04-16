package com.smartstadium.model;

/**
 * Density classification levels based on zone occupancy percentage.
 *
 * <p>Thresholds: LOW (< 30%), MEDIUM (< 60%), HIGH (< 85%), CRITICAL (>= 85%)</p>
 */
public enum DensityLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    /**
     * Determines the density level based on the occupancy ratio.
     *
     * @param ratio the ratio of current occupancy to capacity (0.0 - 1.0+)
     * @return the corresponding density level
     */
    public static DensityLevel fromRatio(double ratio) {
        if (ratio < 0.30) return LOW;
        if (ratio < 0.60) return MEDIUM;
        if (ratio < 0.85) return HIGH;
        return CRITICAL;
    }
}
