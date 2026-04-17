package com.smartstadium.model;

/**
 * Enumeration of all zones within the stadium.
 *
 * <p>Each zone has a defined capacity representing the maximum number
 * of people the zone can comfortably hold.</p>
 */
public enum Zone {
    GATE_A("Gate A", 500, 0, 0),
    GATE_B("Gate B", 500, 100, 0),
    GATE_C("Gate C", 400, 50, 25),
    SEATING_NORTH("Seating North", 5000, 60, 70),
    SEATING_SOUTH("Seating South", 5000, 40, 70),
    FOOD_COURT_EAST("Food Court East", 300, 80, 30),
    FOOD_COURT_WEST("Food Court West", 300, 20, 30),
    RESTROOM_NORTH("Restroom North", 100, 80, 50),
    RESTROOM_SOUTH("Restroom South", 100, 20, 50),
    MAIN_CONCOURSE("Main Concourse", 2000, 50, 0),
    VIP_LOUNGE("VIP Lounge", 200, 50, 90);

    private final String displayName;
    private final int capacity;
    private final double x;
    private final double y;

    Zone(String displayName, int capacity, double x, double y) {
        this.displayName = displayName;
        this.capacity = capacity;
        this.x = x;
        this.y = y;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
