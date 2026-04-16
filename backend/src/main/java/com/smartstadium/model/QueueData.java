package com.smartstadium.model;

import java.time.Instant;

/**
 * Represents queue data for a specific stadium zone at a point in time.
 */
public class QueueData {

    private Zone zone;
    private int queueLength;
    private double avgServiceTimeSeconds;
    private Instant timestamp;

    public QueueData() {
    }

    public QueueData(Zone zone, int queueLength, double avgServiceTimeSeconds) {
        this.zone = zone;
        this.queueLength = Math.max(0, queueLength);
        this.avgServiceTimeSeconds = Math.max(0.0, avgServiceTimeSeconds);
        this.timestamp = Instant.now();
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public int getQueueLength() {
        return queueLength;
    }

    public void setQueueLength(int queueLength) {
        this.queueLength = Math.max(0, queueLength);
    }

    public double getAvgServiceTimeSeconds() {
        return avgServiceTimeSeconds;
    }

    public void setAvgServiceTimeSeconds(double avgServiceTimeSeconds) {
        this.avgServiceTimeSeconds = Math.max(0.0, avgServiceTimeSeconds);
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
