import { useState, useEffect, useRef, useCallback } from 'react';

/**
 * Custom hook for polling an async function at a regular interval.
 *
 * @param {Function} fetchFn - Async function to call
 * @param {number} intervalMs - Polling interval in milliseconds
 * @param {boolean} enabled - Whether polling is active
 * @returns {{ data: any, error: string|null, loading: boolean, lastUpdated: Date|null }}
 */
export function usePolling(fetchFn, intervalMs = 5000, enabled = true) {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [lastUpdated, setLastUpdated] = useState(null);
  const intervalRef = useRef(null);
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

    if (enabled) {
      execute();
      intervalRef.current = setInterval(execute, intervalMs);
    }

    return () => {
      mountedRef.current = false;
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, [execute, intervalMs, enabled]);

  return { data, error, loading, lastUpdated };
}
