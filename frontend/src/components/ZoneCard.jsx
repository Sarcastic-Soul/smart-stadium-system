import React from 'react';

/**
 * Reusable zone card displaying density status with color-coded bar.
 */
export default function ZoneCard({ zone }) {
  const {
    displayName,
    currentCount,
    capacity,
    occupancyRate,
    densityLevel,
  } = zone;

  const fillPercent = Math.min(100, Math.round(occupancyRate * 100));

  return (
    <article
      className="zone-card"
      data-density={densityLevel}
      aria-label={`${displayName}: ${densityLevel} density, ${currentCount} of ${capacity} capacity`}
    >
      <div className="zone-card-header">
        <span className="zone-name">{displayName}</span>
        <span
          className="density-badge"
          data-level={densityLevel}
          role="status"
          aria-label={`Density level: ${densityLevel}`}
        >
          {densityLevel}
        </span>
      </div>

      <div className="zone-stats">
        <span className="zone-count">{currentCount.toLocaleString()}</span>
        <span className="zone-capacity">/ {capacity.toLocaleString()}</span>
      </div>

      <div
        className="density-bar-track"
        role="progressbar"
        aria-valuenow={fillPercent}
        aria-valuemin={0}
        aria-valuemax={100}
        aria-label={`${fillPercent}% occupied`}
      >
        <div
          className="density-bar-fill"
          data-level={densityLevel}
          style={{ '--fill-percent': `${fillPercent}%` }}
        />
      </div>
    </article>
  );
}
