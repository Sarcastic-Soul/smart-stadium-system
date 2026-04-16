import React from 'react';

/**
 * Application header with logo, title, live indicator, and last-updated time.
 */
export default function Header({ lastUpdated }) {
  const formatTime = (date) => {
    if (!date) return '—';
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  };

  return (
    <header className="header" role="banner">
      <div className="header-left">
        <div className="header-logo" aria-hidden="true">🏟️</div>
        <div>
          <h1 className="header-title">Smart Stadium</h1>
          <p className="header-subtitle">AI-Powered Crowd Intelligence</p>
        </div>
      </div>
      <div className="header-right">
        <div className="live-indicator" aria-label="System is live and receiving data">
          <span className="live-dot" aria-hidden="true"></span>
          <span>Live</span>
        </div>
        <time className="last-updated" dateTime={lastUpdated?.toISOString()}>
          Updated: {formatTime(lastUpdated)}
        </time>
      </div>
    </header>
  );
}
