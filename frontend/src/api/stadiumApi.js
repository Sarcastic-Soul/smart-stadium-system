/**
 * Centralized API client for the Smart Stadium backend.
 * All API calls go through this module for consistent error handling.
 */

const API_BASE = '/api';

/**
 * Performs a fetch request with error handling.
 * @param {string} endpoint - The API endpoint path
 * @param {object} options - Fetch options
 * @returns {Promise<any>} Parsed JSON response
 */
async function fetchApi(endpoint, options = {}) {
  const url = `${API_BASE}${endpoint}`;
  const response = await fetch(url, {
    headers: {
      'Accept': 'application/json',
      ...options.headers,
    },
    ...options,
  });

  if (!response.ok) {
    const errorBody = await response.json().catch(() => null);
    const message = errorBody?.message || `Request failed with status ${response.status}`;
    throw new Error(message);
  }

  return response.json();
}

/**
 * Fetches crowd density data for all zones.
 * @returns {Promise<Array>} Array of crowd density objects
 */
export function fetchCrowdDensity() {
  return fetchApi('/crowd-density');
}

/**
 * Fetches the optimal route between two zones.
 * @param {string} from - Source zone identifier
 * @param {string} to - Destination zone identifier
 * @returns {Promise<object>} Route object with path and estimated time
 */
export function fetchRoute(from, to) {
  return fetchApi(`/route?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`);
}

/**
 * Fetches wait time data for all zones.
 * @returns {Promise<Array>} Array of wait time objects
 */
export function fetchWaitTimes() {
  return fetchApi('/wait-time');
}
/**
 * Fetches the complete stadium telemetry state in one call.
 * @returns {Promise<object>} Bundled crowd and queue data
 */
export function fetchTelemetry() {
  return fetchApi('/telemetry');
}
