import React from 'react';

/**
 * Returns an icon for the zone type.
 */
function getZoneIcon(zoneName) {
  if (zoneName.includes('FOOD')) return '🍔';
  if (zoneName.includes('RESTROOM')) return '🚻';
  if (zoneName.includes('GATE')) return '🚪';
  if (zoneName.includes('VIP')) return '⭐';
  if (zoneName.includes('SEATING')) return '💺';
  return '📍';
}

/**
 * Returns urgency level for wait time display.
 */
function getUrgency(seconds) {
  if (seconds < 120) return 'low';
  if (seconds < 300) return 'medium';
  if (seconds < 600) return 'high';
  return 'critical';
}

/**
 * Formats wait time seconds to display string.
 */
function formatWait(seconds) {
  if (seconds === 0) return '0';
  if (seconds < 60) return `${seconds}`;
  const mins = Math.floor(seconds / 60);
  return `${mins}`;
}

/**
 * Returns the unit label for the wait time.
 */
function getWaitUnit(seconds) {
  if (seconds < 60) return 'sec';
  return 'min';
}

/**
 * Queue times component showing estimated wait per zone.
 */
export default function QueueTimes({ data, loading, error }) {
  // Filter to only show zones that typically have queues
  const queueZones = data?.filter((item) =>
    ['FOOD_COURT_EAST', 'FOOD_COURT_WEST', 'RESTROOM_NORTH', 'RESTROOM_SOUTH',
     'GATE_A', 'GATE_B', 'GATE_C', 'VIP_LOUNGE'].includes(item.zone)
  ) || [];

  // Sort by wait time descending
  const sorted = [...queueZones].sort(
    (a, b) => b.estimatedWaitSeconds - a.estimatedWaitSeconds
  );

  if (loading) {
    return (
      <section className="card" aria-label="Queue Wait Times">
        <div className="card-header">
          <h2 className="card-title"><span className="icon" aria-hidden="true">⏱️</span> Queue Times</h2>
        </div>
        <div className="loading-container" role="status">
          <div className="spinner" aria-label="Loading queue data"></div>
          <span className="visually-hidden">Loading queue times...</span>
        </div>
      </section>
    );
  }

  if (error) {
    return (
      <section className="card" aria-label="Queue Wait Times">
        <div className="card-header">
          <h2 className="card-title"><span className="icon" aria-hidden="true">⏱️</span> Queue Times</h2>
        </div>
        <p className="error-message" role="alert">Failed to load queue data: {error}</p>
      </section>
    );
  }

  return (
    <section className="card" aria-label="Queue Wait Times">
      <div className="card-header">
        <h2 className="card-title"><span className="icon" aria-hidden="true">⏱️</span> Queue Times</h2>
        <span className="last-updated">{sorted.length} queues</span>
      </div>
      <div className="queue-list" role="list" aria-label="Queue wait times by zone">
        {sorted.map((item) => {
          const urgency = getUrgency(item.estimatedWaitSeconds);
          return (
            <div
              className="queue-item"
              key={item.zone}
              role="listitem"
              aria-label={`${item.displayName}: estimated wait ${formatWait(item.estimatedWaitSeconds)} ${getWaitUnit(item.estimatedWaitSeconds)}, ${item.queueLength} people in queue`}
            >
              <div className="queue-item-left">
                <span className="queue-icon" aria-hidden="true">{getZoneIcon(item.zone)}</span>
                <div>
                  <div className="queue-zone-name">{item.displayName}</div>
                  <div className="queue-length">{item.queueLength} in queue</div>
                </div>
              </div>
              <div>
                <span className="queue-wait" data-urgency={urgency}>
                  {formatWait(item.estimatedWaitSeconds)}
                </span>
                <span className="queue-unit">{getWaitUnit(item.estimatedWaitSeconds)}</span>
              </div>
            </div>
          );
        })}
      </div>
    </section>
  );
}
