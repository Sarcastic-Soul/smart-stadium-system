package com.smartstadium.validation;

import com.smartstadium.model.Zone;
import org.springframework.stereotype.Component;

/**
 * Validator for zone-related input parameters.
 *
 * <p>Provides validation and parsing of zone identifiers from API requests,
 * with user-friendly error messages.</p>
 */
@Component
public class ZoneValidator {

    /**
     * Parses and validates a zone string into a {@link Zone} enum value.
     *
     * @param zoneStr the zone identifier string (case-insensitive)
     * @return the corresponding Zone enum value
     * @throws IllegalArgumentException if the zone string is invalid
     */
    public Zone parseZone(String zoneStr) {
        if (zoneStr == null || zoneStr.isBlank()) {
            throw new IllegalArgumentException("Zone parameter is required and cannot be empty");
        }

        String normalized = zoneStr.trim().toUpperCase();

        try {
            return Zone.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid zone: '" + zoneStr + "'. Valid zones: " +
                    String.join(", ", getValidZoneNames()));
        }
    }

    /**
     * Returns the list of valid zone names.
     */
    public String[] getValidZoneNames() {
        Zone[] zones = Zone.values();
        String[] names = new String[zones.length];
        for (int i = 0; i < zones.length; i++) {
            names[i] = zones[i].name();
        }
        return names;
    }
}
