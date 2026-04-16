import React, { useCallback } from 'react';
import Header from './components/Header.jsx';
import CrowdHeatmap from './components/CrowdHeatmap.jsx';
import RoutePlanner from './components/RoutePlanner.jsx';
import QueueTimes from './components/QueueTimes.jsx';
import AiAssistant from './components/AiAssistant.jsx';
import AdminPanel from './components/AdminPanel.jsx';
import { fetchCrowdDensity, fetchWaitTimes } from './api/stadiumApi.js';
import { useStompData } from './hooks/useStompData.js';

/**
 * Main application component assembling the dashboard.
 */
export default function App() {
  const crowdFetcher = useCallback(() => fetchCrowdDensity(), []);
  const queueFetcher = useCallback(() => fetchWaitTimes(), []);

  const crowd = useStompData(crowdFetcher);
  const queue = useStompData(queueFetcher);

  // Use the most recent update time from either data source
  const lastUpdated = crowd.lastUpdated && queue.lastUpdated
    ? new Date(Math.max(crowd.lastUpdated.getTime(), queue.lastUpdated.getTime()))
    : crowd.lastUpdated || queue.lastUpdated;

  return (
    <>
      <a href="#main-content" className="skip-link">Skip to main content</a>
      <div className="app-container">
        <Header lastUpdated={lastUpdated} />

        <main id="main-content" className="dashboard-grid" role="main" aria-label="Stadium Dashboard">
          <CrowdHeatmap
            data={crowd.data}
            loading={crowd.loading}
            error={crowd.error}
          />

          <RoutePlanner />

          <QueueTimes
            data={queue.data}
            loading={queue.loading}
            error={queue.error}
          />
          <AdminPanel />
        </main>
        
        <AiAssistant />
      </div>
    </>
  );
}
