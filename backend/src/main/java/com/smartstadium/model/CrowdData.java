package com.smartstadium.model;

import java.time.Instant;

/**
 * Represents the crowd density data for a specific stadium zone at a point in time.
 */
public class CrowdData {

    private Zone zone;
    private int currentCount;
    private int capacity;
    private DensityLevel densityLevel;
    private Instant timestamp;

    public CrowdData() {
    }

    public CrowdData(Zone zone, int currentCount) {
        this.zone = zone;
        this.currentCount = Math.max(0, currentCount);
        this.capacity = zone.getCapacity();
        this.densityLevel = DensityLevel.fromRatio((double) this.currentCount / this.capacity);
        this.timestamp = Instant.now();
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(int currentCount) {
        this.currentCount = Math.max(0, currentCount);
        if (this.zone != null) {
            this.densityLevel = DensityLevel.fromRatio((double) this.currentCount / this.capacity);
        }
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public DensityLevel getDensityLevel() {
        return densityLevel;
    }

    public void setDensityLevel(DensityLevel densityLevel) {
        this.densityLevel = densityLevel;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
