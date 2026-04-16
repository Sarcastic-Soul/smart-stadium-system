import React from 'react';
import ZoneCard from './ZoneCard.jsx';

/**
 * Crowd heatmap displayed as a responsive grid of zone cards.
 */
export default function CrowdHeatmap({ data, loading, error }) {
  if (loading) {
    return (
      <section className="card section-full" aria-label="Crowd Density Heatmap">
        <div className="card-header">
          <h2 className="card-title"><span className="icon" aria-hidden="true">🔥</span> Crowd Density</h2>
        </div>
        <div className="loading-container" role="status">
          <div className="spinner" aria-label="Loading crowd data"></div>
          <span className="visually-hidden">Loading crowd density data...</span>
        </div>
      </section>
    );
  }

  if (error) {
    return (
      <section className="card section-full" aria-label="Crowd Density Heatmap">
        <div className="card-header">
          <h2 className="card-title"><span className="icon" aria-hidden="true">🔥</span> Crowd Density</h2>
        </div>
        <p className="error-message" role="alert">Failed to load crowd data: {error}</p>
      </section>
    );
  }

  return (
    <section className="card section-full" aria-label="Crowd Density Heatmap">
      <div className="card-header">
        <h2 className="card-title"><span className="icon" aria-hidden="true">🔥</span> Crowd Density</h2>
        <span className="last-updated">{data?.length || 0} zones</span>
      </div>
      <div className="zone-grid" role="list" aria-label="Stadium zones density overview">
        {data?.map((zone) => (
          <div role="listitem" key={zone.zone}>
            <ZoneCard zone={zone} />
          </div>
        ))}
      </div>
    </section>
  );
}
