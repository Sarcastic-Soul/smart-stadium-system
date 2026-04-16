package com.smartstadium.model;

/**
 * Enumeration of all zones within the stadium.
 *
 * <p>Each zone has a defined capacity representing the maximum number
 * of people the zone can comfortably hold.</p>
 */
public enum Zone {
    GATE_A("Gate A", 500),
    GATE_B("Gate B", 500),
    GATE_C("Gate C", 400),
    SEATING_NORTH("Seating North", 5000),
    SEATING_SOUTH("Seating South", 5000),
    FOOD_COURT_EAST("Food Court East", 300),
    FOOD_COURT_WEST("Food Court West", 300),
    RESTROOM_NORTH("Restroom North", 100),
    RESTROOM_SOUTH("Restroom South", 100),
    MAIN_CONCOURSE("Main Concourse", 2000),
    VIP_LOUNGE("VIP Lounge", 200);

    private final String displayName;
    private final int capacity;

    Zone(String displayName, int capacity) {
        this.displayName = displayName;
        this.capacity = capacity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCapacity() {
        return capacity;
    }
}
