import React, { useState, useEffect, useCallback, useRef } from "react";
import { fetchRoute } from "../api/stadiumApi.js";

/**
 * Formats seconds into a human-readable string (e.g., "2m 30s").
 */
function formatTime(seconds) {
    if (seconds < 60) return `${seconds}s`;
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return secs > 0 ? `${mins}m ${secs}s` : `${mins}m`;
}

/**
 * Route planner component with zone selection and path display.
 */
export default function RoutePlanner() {
    const [zones, setZones] = useState([]);
    const [from, setFrom] = useState("");
    const [to, setTo] = useState("");
    const [route, setRoute] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const resultRef = useRef(null);

    useEffect(() => {
        fetch("/api/zones")
            .then((res) => res.json())
            .then((data) => setZones(data))
            .catch(() => {
                // Fallback if endpoint is unavailable
                setZones([
                    { id: "GATE_A", name: "Gate A" },
                    { id: "GATE_B", name: "Gate B" },
                    { id: "GATE_C", name: "Gate C" },
                    { id: "SEATING_NORTH", name: "Seating North" },
                    { id: "SEATING_SOUTH", name: "Seating South" },
                    { id: "FOOD_COURT_EAST", name: "Food Court East" },
                    { id: "FOOD_COURT_WEST", name: "Food Court West" },
                    { id: "RESTROOM_NORTH", name: "Restroom North" },
                    { id: "RESTROOM_SOUTH", name: "Restroom South" },
                    { id: "MAIN_CONCOURSE", name: "Main Concourse" },
                    { id: "VIP_LOUNGE", name: "VIP Lounge" },
                ]);
            });
    }, []);

    const handleFindRoute = useCallback(async () => {
        if (!from || !to) return;
        setLoading(true);
        setError(null);
        setRoute(null);

        try {
            const result = await fetchRoute(from, to);
            setRoute(result);
            setTimeout(() => {
                if (resultRef.current) {
                    resultRef.current.focus();
                }
            }, 0);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }, [from, to]);

    const canSearch = from && to && !loading;

    return (
        <section className="card" aria-label="Route Planner">
            <div className="card-header">
                <h2 className="card-title">
                    <span className="icon" aria-hidden="true">
                        🧭
                    </span>{" "}
                    Route Planner
                </h2>
            </div>

            <div
                className="route-form"
                role="search"
                aria-label="Find optimal route"
                aria-busy={loading}
            >
                <div className="select-group">
                    <label htmlFor="route-from">From</label>
                    <select
                        id="route-from"
                        value={from}
                        onChange={(e) => setFrom(e.target.value)}
                        aria-required="true"
                        aria-invalid={!!error}
                    >
                        <option value="">Select origin</option>
                        {zones.map((z) => (
                            <option key={z.id} value={z.id}>
                                {z.name}
                            </option>
                        ))}
                    </select>
                </div>

                <div className="select-group">
                    <label htmlFor="route-to">To</label>
                    <select
                        id="route-to"
                        value={to}
                        onChange={(e) => setTo(e.target.value)}
                        aria-required="true"
                        aria-invalid={!!error}
                    >
                        <option value="">Select destination</option>
                        {zones.map((z) => (
                            <option key={z.id} value={z.id}>
                                {z.name}
                            </option>
                        ))}
                    </select>
                </div>

                <button
                    className="route-btn"
                    onClick={handleFindRoute}
                    disabled={!canSearch}
                    aria-label="Find optimal route"
                >
                    {loading ? "Finding..." : "Find Route"}
                </button>
            </div>

            {error && (
                <p className="error-message" role="alert">
                    {error}
                </p>
            )}

            {route && (
                <div
                    className="route-result"
                    aria-live="polite"
                    aria-label="Route result"
                    tabIndex="-1"
                    ref={resultRef}
                >
                    <div className="route-info">
                        <div className="route-stat">
                            <div className="route-stat-value">
                                {formatTime(route.estimatedTimeSeconds)}
                            </div>
                            <div className="route-stat-label">
                                Est. Travel Time
                            </div>
                        </div>
                        <div className="route-stat">
                            <div className="route-stat-value">
                                {route.path.length - 1}
                            </div>
                            <div className="route-stat-label">Steps</div>
                        </div>
                        <div className="route-stat">
                            <div className="route-stat-value">
                                {route.totalWeight}m
                            </div>
                            <div className="route-stat-label">Distance</div>
                        </div>
                    </div>

                    <nav className="route-path" aria-label="Route steps">
                        {route.pathDisplayNames.map((name, i) => (
                            <div className="route-step" key={i}>
                                {i > 0 && (
                                    <span
                                        className="route-arrow"
                                        aria-hidden="true"
                                    >
                                        →
                                    </span>
                                )}
                                <span className="route-node">{name}</span>
                            </div>
                        ))}
                    </nav>
                </div>
            )}

            {!route && !error && !loading && (
                <p className="empty-state">
                    Select origin and destination to find the best route
                </p>
            )}
        </section>
    );
}
