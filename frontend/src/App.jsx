import React, { useCallback } from 'react';
import Header from './components/Header.jsx';
import CrowdHeatmap from './components/CrowdHeatmap.jsx';
import RoutePlanner from './components/RoutePlanner.jsx';
import QueueTimes from './components/QueueTimes.jsx';
import AiAssistant from './components/AiAssistant.jsx';
import AdminPanel from './components/AdminPanel.jsx';
import { fetchTelemetry } from './api/stadiumApi.js';
import { useStompData } from './hooks/useStompData.js';

/**
 * Main application component assembling the dashboard.
 */
export default function App() {
  const telemetryFetcher = useCallback(() => fetchTelemetry(), []);
  const { data: telemetry, loading, error, lastUpdated } = useStompData(telemetryFetcher);

  const crowdData = telemetry?.crowdDensities || null;
  const queueData = telemetry?.queueWaitTimes || null;

  return (
    <>
      <a href="#main-content" className="skip-link">Skip to main content</a>
      <div className="app-container">
        <Header lastUpdated={lastUpdated} />

        <main id="main-content" className="dashboard-grid" role="main" aria-label="Stadium Dashboard">
          <CrowdHeatmap
            data={crowdData}
            loading={loading}
            error={error}
          />

          <RoutePlanner />

          <QueueTimes
            data={queueData}
            loading={loading}
            error={error}
          />
          <AdminPanel />
        </main>
        
        <AiAssistant />
      </div>
    </>
  );
}
