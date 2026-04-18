package com.smartstadium.service;

import com.smartstadium.dto.CrowdDensityDto;
import com.smartstadium.model.CrowdData;
import com.smartstadium.model.Zone;
import com.smartstadium.repository.StadiumRepository;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CrowdService {

    private static final Logger logger = LoggerFactory.getLogger(
        CrowdService.class
    );
    private final StadiumRepository repository;

    public CrowdService(StadiumRepository repository) {
        this.repository = repository;
    }

    // REMOVED @Cacheable
    public List<CrowdDensityDto> getAllDensities() {
        logger.debug("Fetching crowd density for all zones");
        Map<Zone, CrowdData> allData = repository.getAllCrowdData();

        return java.util.Arrays.stream(Zone.values())
            .map(zone -> {
                CrowdData data = allData.getOrDefault(
                    zone,
                    new CrowdData(zone, 0)
                );
                return toDto(data);
            })
            .toList();
    }

    // REMOVED @Cacheable
    public CrowdDensityDto getDensity(Zone zone) {
        logger.debug("Fetching crowd density for zone: {}", zone);
        CrowdData data = repository
            .getCrowdData(zone)
            .orElse(new CrowdData(zone, 0));
        return toDto(data);
    }

    // REMOVED @CacheEvict
    public void updateCrowdData(Zone zone, int count) {
        logger.info("Updating crowd data for zone {} to count {}", zone, count);
        CrowdData data = new CrowdData(zone, count);
        repository.saveCrowdData(zone, data);
    }

    public CrowdData getCrowdData(Zone zone) {
        return repository.getCrowdData(zone).orElse(new CrowdData(zone, 0));
    }

    CrowdDensityDto toDto(CrowdData data) {
        double occupancyRate =
            data.getCapacity() > 0
                ? (double) data.getCurrentCount() / data.getCapacity()
                : 0.0;
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
