import React, { useState } from 'react';
import './AdminPanel.css';

export default function AdminPanel() {
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState('');

  const triggerSimulation = async () => {
    setLoading(true);
    setStatus('');
    try {
      // For mock UI, we send the request without a valid JWT, 
      // which succeeds because we configured the mock security profile to permitAll()
      const res = await fetch('/api/admin/simulation/trigger', {
        method: 'POST',
      });
      if (res.ok) {
        setStatus('Simulation triggered successfully.');
      } else {
        setStatus('Failed to trigger simulation. Unauthorized.');
      }
    } catch(err) {
      setStatus('Error triggering simulation.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="admin-panel card" aria-label="Admin Controls">
      <div className="card-header">
        <h3 className="card-title">System Control (RBAC Mock)</h3>
      </div>
      <button 
        onClick={triggerSimulation} 
        disabled={loading}
        className="admin-btn"
      >
        {loading ? 'Triggering...' : 'Trigger Simulation Tick'}
      </button>
      {status && <p className="admin-status">{status}</p>}
    </div>
  );
}
