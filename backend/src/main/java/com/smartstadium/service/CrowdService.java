package com.smartstadium.service;

import com.smartstadium.dto.CrowdDensityDto;
import com.smartstadium.model.CrowdData;
import com.smartstadium.model.Zone;
import com.smartstadium.repository.StadiumRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.smartstadium.config.CacheConfig.CROWD_DENSITY_CACHE;

/**
 * Service for tracking and managing crowd density across stadium zones.
 *
 * <p>Provides crowd density data with caching to avoid redundant computations.
 * Density is calculated as the ratio of current occupancy to zone capacity,
 * classified into levels: LOW, MEDIUM, HIGH, CRITICAL.</p>
 */
@Service
public class CrowdService {

    private static final Logger logger = LoggerFactory.getLogger(CrowdService.class);

    private final StadiumRepository repository;

    public CrowdService(StadiumRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieves crowd density data for all zones.
     *
     * @return list of density DTOs for every zone
     */
    @Cacheable(value = CROWD_DENSITY_CACHE, key = "'all'")
    public List<CrowdDensityDto> getAllDensities() {
        logger.debug("Fetching crowd density for all zones");
        Map<Zone, CrowdData> allData = repository.getAllCrowdData();

        return java.util.Arrays.stream(Zone.values())
                .map(zone -> {
                    CrowdData data = allData.getOrDefault(zone, new CrowdData(zone, 0));
                    return toDto(data);
                })
                .toList();
    }

    /**
     * Retrieves crowd density data for a specific zone.
     *
     * @param zone the zone to query
     * @return the density DTO for the zone
     */
    @Cacheable(value = CROWD_DENSITY_CACHE, key = "#zone.name()")
    public CrowdDensityDto getDensity(Zone zone) {
        logger.debug("Fetching crowd density for zone: {}", zone);
        CrowdData data = repository.getCrowdData(zone)
                .orElse(new CrowdData(zone, 0));
        return toDto(data);
    }

    /**
     * Updates the crowd count for a specific zone and evicts related caches.
     *
     * @param zone  the zone to update
     * @param count the new crowd count
     */
    @CacheEvict(value = CROWD_DENSITY_CACHE, allEntries = true)
    public void updateCrowdData(Zone zone, int count) {
        logger.info("Updating crowd data for zone {} to count {}", zone, count);
        CrowdData data = new CrowdData(zone, count);
        repository.saveCrowdData(zone, data);
    }

    /**
     * Gets the current crowd data model for a zone (used by other services).
     *
     * @param zone the zone to query
     * @return the crowd data, or a default with count 0
     */
    public CrowdData getCrowdData(Zone zone) {
        return repository.getCrowdData(zone)
                .orElse(new CrowdData(zone, 0));
    }

    /**
     * Converts internal CrowdData to a CrowdDensityDto for API responses.
     */
    CrowdDensityDto toDto(CrowdData data) {
        double occupancyRate = data.getCapacity() > 0
                ? (double) data.getCurrentCount() / data.getCapacity()
                : 0.0;
        // Round to 4 decimal places
        occupancyRate = Math.round(occupancyRate * 10000.0) / 10000.0;

        return new CrowdDensityDto(
                data.getZone().name(),
                data.getZone().getDisplayName(),
                data.getCurrentCount(),
                data.getCapacity(),
                occupancyRate,
                data.getDensityLevel(),
                data.getTimestamp()
        );
    }
}
