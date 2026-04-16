package com.smartstadium.service;

import com.smartstadium.dto.QueueWaitTimeDto;
import com.smartstadium.model.DensityLevel;
import com.smartstadium.model.Zone;
import com.smartstadium.repository.InMemoryStadiumRepository;
import com.smartstadium.repository.StadiumRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link QueueService}.
 * Tests wait time formula, congestion factors, and boundary cases.
 */
class QueueServiceTest {

    private QueueService queueService;
    private CrowdService crowdService;

    @BeforeEach
    void setUp() {
        StadiumRepository repository = new InMemoryStadiumRepository();
        crowdService = new CrowdService(repository);
        queueService = new QueueService(repository, crowdService);
    }

    @Test
    @DisplayName("Should calculate zero wait time for empty queue")
    void shouldReturnZeroForEmptyQueue() {
        queueService.updateQueueData(Zone.FOOD_COURT_EAST, 0, 45.0);

        QueueWaitTimeDto dto = queueService.getWaitTime(Zone.FOOD_COURT_EAST);

        assertEquals(0, dto.estimatedWaitSeconds());
        assertEquals(0, dto.queueLength());
    }

    @Test
    @DisplayName("Should calculate wait time with basic formula")
    void shouldCalculateBasicWaitTime() {
        // queue=5, serviceTime=45s, LOW density (factor=1.0)
        // Expected: 5 * 45 * 1.0 = 225s
        int waitTime = queueService.calculateWaitTimeSeconds(5, 45.0, DensityLevel.LOW);

        assertEquals(225, waitTime);
    }

    @Test
    @DisplayName("Should increase wait time with higher congestion")
    void shouldIncreaseWaitTimeWithCongestion() {
        int lowWait = queueService.calculateWaitTimeSeconds(10, 30.0, DensityLevel.LOW);
        int medWait = queueService.calculateWaitTimeSeconds(10, 30.0, DensityLevel.MEDIUM);
        int highWait = queueService.calculateWaitTimeSeconds(10, 30.0, DensityLevel.HIGH);
        int critWait = queueService.calculateWaitTimeSeconds(10, 30.0, DensityLevel.CRITICAL);

        assertTrue(lowWait < medWait, "Medium wait should exceed low");
        assertTrue(medWait < highWait, "High wait should exceed medium");
        assertTrue(highWait < critWait, "Critical wait should exceed high");
    }

    @Test
    @DisplayName("Congestion factors should be ordered correctly")
    void congestionFactorsShouldBeOrdered() {
        double low = queueService.getCongestionFactor(DensityLevel.LOW);
        double medium = queueService.getCongestionFactor(DensityLevel.MEDIUM);
        double high = queueService.getCongestionFactor(DensityLevel.HIGH);
        double critical = queueService.getCongestionFactor(DensityLevel.CRITICAL);

        assertEquals(1.0, low);
        assertTrue(medium > low);
        assertTrue(high > medium);
        assertTrue(critical > high);
    }

    @Test
    @DisplayName("Should return wait times for all zones")
    void shouldReturnAllWaitTimes() {
        // Seed food court data
        queueService.updateQueueData(Zone.FOOD_COURT_EAST, 10, 45.0);
        queueService.updateQueueData(Zone.FOOD_COURT_WEST, 15, 45.0);

        List<QueueWaitTimeDto> all = queueService.getAllWaitTimes();

        assertEquals(Zone.values().length, all.size());
    }

    @Test
    @DisplayName("Should return appropriate default service times per zone type")
    void shouldHaveDifferentServiceTimesByZoneType() {
        double foodTime = queueService.getDefaultServiceTime(Zone.FOOD_COURT_EAST);
        double restroomTime = queueService.getDefaultServiceTime(Zone.RESTROOM_NORTH);
        double gateTime = queueService.getDefaultServiceTime(Zone.GATE_A);

        // Food service takes longer than gate entry
        assertTrue(foodTime > gateTime, "Food service should be slower than gate entry");
        // Restroom usage takes the longest
        assertTrue(restroomTime > foodTime, "Restroom should be slower than food");
    }

    @Test
    @DisplayName("Should include zone metadata in DTO")
    void shouldIncludeZoneMetadata() {
        queueService.updateQueueData(Zone.RESTROOM_SOUTH, 8, 90.0);

        QueueWaitTimeDto dto = queueService.getWaitTime(Zone.RESTROOM_SOUTH);

        assertEquals("RESTROOM_SOUTH", dto.zone());
        assertEquals("Restroom South", dto.displayName());
        assertEquals(8, dto.queueLength());
        assertNotNull(dto.timestamp());
    }

    @Test
    @DisplayName("Should handle large queue lengths without overflow")
    void shouldHandleLargeQueues() {
        int waitTime = queueService.calculateWaitTimeSeconds(1000, 45.0, DensityLevel.CRITICAL);

        assertTrue(waitTime > 0);
        // 1000 * 45 * 2.0 = 90000
        assertEquals(90000, waitTime);
    }
}
