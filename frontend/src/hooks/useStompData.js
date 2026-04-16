import { useState, useEffect, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';

/**
 * Custom hook for fetching data on mount and whenever a WebSocket EVENT fires.
 *
 * @param {Function} fetchFn - Async function to call for getting data
 * @param {string} endpoint - WebSocket endpoint to listen to
 */
export function useStompData(fetchFn) {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [lastUpdated, setLastUpdated] = useState(null);
  const mountedRef = useRef(true);

  const execute = useCallback(async () => {
    try {
      const result = await fetchFn();
      if (mountedRef.current) {
        setData(result);
        setError(null);
        setLastUpdated(new Date());
        setLoading(false);
      }
    } catch (err) {
      if (mountedRef.current) {
        setError(err.message);
        setLoading(false);
      }
    }
  }, [fetchFn]);

  useEffect(() => {
    mountedRef.current = true;

    // Initial fetch
    execute();

    // WebSocket setup - point to the /ws proxy endpoint
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws`;

    const client = new Client({
      brokerURL: wsUrl,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        client.subscribe('/topic/telemetry', (message) => {
          if (message.body === 'REFRESH') {
            execute();
          }
        });
      },
      onStompError: (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
      },
    });

    client.activate();

    return () => {
      mountedRef.current = false;
      client.deactivate();
    };
  }, [execute]);

  return { data, error, loading, lastUpdated };
}
