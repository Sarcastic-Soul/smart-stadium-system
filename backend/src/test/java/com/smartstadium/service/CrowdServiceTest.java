package com.smartstadium.service;

import com.smartstadium.dto.CrowdDensityDto;
import com.smartstadium.model.CrowdData;
import com.smartstadium.model.DensityLevel;
import com.smartstadium.model.Zone;
import com.smartstadium.repository.InMemoryStadiumRepository;
import com.smartstadium.repository.StadiumRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CrowdService}.
 * Tests density calculation logic, DTO conversion, and boundary cases.
 */
class CrowdServiceTest {

    private StadiumRepository repository;
    private CrowdService crowdService;

    @BeforeEach
    void setUp() {
        repository = new InMemoryStadiumRepository();
        crowdService = new CrowdService(repository);
    }

    @Test
    @DisplayName("Should return LOW density when zone is below 30% capacity")
    void shouldReturnLowDensity() {
        // GATE_A capacity = 500, 29% = 145
        crowdService.updateCrowdData(Zone.GATE_A, 145);

        CrowdDensityDto dto = crowdService.getDensity(Zone.GATE_A);

        assertEquals(DensityLevel.LOW, dto.densityLevel());
        assertEquals(145, dto.currentCount());
        assertEquals(500, dto.capacity());
        assertTrue(dto.occupancyRate() < 0.30);
    }

    @Test
    @DisplayName("Should return MEDIUM density when zone is between 30-60% capacity")
    void shouldReturnMediumDensity() {
        // GATE_A capacity = 500, 50% = 250
        crowdService.updateCrowdData(Zone.GATE_A, 250);

        CrowdDensityDto dto = crowdService.getDensity(Zone.GATE_A);

        assertEquals(DensityLevel.MEDIUM, dto.densityLevel());
        assertEquals(0.5, dto.occupancyRate());
    }

    @Test
    @DisplayName("Should return HIGH density when zone is between 60-85% capacity")
    void shouldReturnHighDensity() {
        // SEATING_NORTH capacity = 5000, 70% = 3500
        crowdService.updateCrowdData(Zone.SEATING_NORTH, 3500);

        CrowdDensityDto dto = crowdService.getDensity(Zone.SEATING_NORTH);

        assertEquals(DensityLevel.HIGH, dto.densityLevel());
        assertEquals(3500, dto.currentCount());
    }

    @Test
    @DisplayName("Should return CRITICAL density when zone is at or above 85% capacity")
    void shouldReturnCriticalDensity() {
        // FOOD_COURT_EAST capacity = 300, 90% = 270
        crowdService.updateCrowdData(Zone.FOOD_COURT_EAST, 270);

        CrowdDensityDto dto = crowdService.getDensity(Zone.FOOD_COURT_EAST);

        assertEquals(DensityLevel.CRITICAL, dto.densityLevel());
        assertTrue(dto.occupancyRate() >= 0.85);
    }

    @Test
    @DisplayName("Should return zero count for zone with no data")
    void shouldReturnZeroForEmptyZone() {
        CrowdDensityDto dto = crowdService.getDensity(Zone.VIP_LOUNGE);

        assertEquals(0, dto.currentCount());
        assertEquals(DensityLevel.LOW, dto.densityLevel());
        assertEquals(0.0, dto.occupancyRate());
    }

    @Test
    @DisplayName("Should clamp negative count to zero")
    void shouldClampNegativeCount() {
        crowdService.updateCrowdData(Zone.GATE_A, -50);

        CrowdDensityDto dto = crowdService.getDensity(Zone.GATE_A);

        assertEquals(0, dto.currentCount());
        assertEquals(DensityLevel.LOW, dto.densityLevel());
    }

    @Test
    @DisplayName("Should return data for all zones")
    void shouldReturnAllZones() {
        // Seed some data
        crowdService.updateCrowdData(Zone.GATE_A, 100);
        crowdService.updateCrowdData(Zone.GATE_B, 200);

        List<CrowdDensityDto> all = crowdService.getAllDensities();

        assertEquals(Zone.values().length, all.size());
    }

    @ParameterizedTest
    @DisplayName("Should correctly classify density at boundary values")
    @CsvSource({
            "0,    LOW",
            "149,  LOW",       // 29.8% of 500
            "150,  MEDIUM",    // 30% of 500
            "299,  MEDIUM",    // 59.8% of 500
            "300,  HIGH",      // 60% of 500
            "424,  HIGH",      // 84.8% of 500
            "425,  CRITICAL",  // 85% of 500
            "500,  CRITICAL",  // 100% of 500
    })
    void shouldClassifyDensityAtBoundaries(int count, DensityLevel expected) {
        crowdService.updateCrowdData(Zone.GATE_A, count);

        CrowdDensityDto dto = crowdService.getDensity(Zone.GATE_A);

        assertEquals(expected, dto.densityLevel());
    }

    @Test
    @DisplayName("Should include correct zone metadata in DTO")
    void shouldIncludeZoneMetadata() {
        crowdService.updateCrowdData(Zone.FOOD_COURT_EAST, 150);

        CrowdDensityDto dto = crowdService.getDensity(Zone.FOOD_COURT_EAST);

        assertEquals("FOOD_COURT_EAST", dto.zone());
        assertEquals("Food Court East", dto.displayName());
        assertEquals(300, dto.capacity());
        assertNotNull(dto.timestamp());
    }
}
