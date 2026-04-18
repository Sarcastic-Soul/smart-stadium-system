import React from "react";
import ZoneCard from "./ZoneCard.jsx";

/**
 * Crowd heatmap displayed as a responsive grid of zone cards.
 */
export default function CrowdHeatmap({ data, loading, error }) {
    if (loading) {
        return (
            <section
                className="card section-full"
                aria-label="Crowd Density Heatmap"
            >
                <div className="card-header">
                    <h2 className="card-title">
                        <span className="icon" aria-hidden="true">
                            🔥
                        </span>{" "}
                        Crowd Density
                    </h2>
                </div>
                <div className="loading-container" role="status">
                    <div
                        className="spinner"
                        aria-label="Loading crowd data"
                    ></div>
                    <span className="visually-hidden">
                        Loading crowd density data...
                    </span>
                </div>
            </section>
        );
    }

    if (error) {
        return (
            <section
                className="card section-full"
                aria-label="Crowd Density Heatmap"
            >
                <div className="card-header">
                    <h2 className="card-title">
                        <span className="icon" aria-hidden="true">
                            🔥
                        </span>{" "}
                        Crowd Density
                    </h2>
                </div>
                <p className="error-message" role="alert">
                    Failed to load crowd data: {error}
                </p>
            </section>
        );
    }

    return (
        <section
            className="card section-full"
            aria-label="Crowd Density Heatmap"
        >
            <div className="card-header">
                <h2 className="card-title">
                    <span className="icon" aria-hidden="true">
                        🔥
                    </span>{" "}
                    Crowd Density
                </h2>
                <span className="last-updated">{data?.length || 0} zones</span>
            </div>
            {/* Accessible semantic table fallback */}
            <div className="visually-hidden">
                <table aria-label="Crowd density details per zone">
                    <thead>
                        <tr>
                            <th scope="col">Zone Name</th>
                            <th scope="col">People Count</th>
                            <th scope="col">Density Level</th>
                            <th scope="col">Wait Time</th>
                        </tr>
                    </thead>
                    <tbody>
                        {data?.map((zone) => (
                            <tr key={`table-${zone.zone}`}>
                                <td>{zone.displayName}</td>
                                <td>{zone.peopleCount}</td>
                                <td>{zone.densityLevel}</td>
                                <td>{zone.estimatedWaitSeconds} seconds</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            <div
                className="zone-grid"
                role="list"
                aria-label="Stadium zones density overview"
                aria-hidden="true"
            >
                {data?.map((zone) => (
                    <div role="listitem" key={zone.zone}>
                        <ZoneCard zone={zone} />
                    </div>
                ))}
            </div>
        </section>
    );
}
