import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import CrowdHeatmap from './CrowdHeatmap.jsx';

// Mock ZoneCard since we just want to test CrowdHeatmap's structural rendering
vi.mock('./ZoneCard.jsx', () => ({
  default: ({ zone }) => <div data-testid={`zone-card-${zone.zone}`}>{zone.displayName}</div>
}));

describe('CrowdHeatmap Component', () => {
  it('renders loading state correctly', () => {
    render(<CrowdHeatmap loading={true} />);

    // The component uses role="status" for the loading container
    expect(screen.getByRole('status')).toBeInTheDocument();
    expect(screen.getByText(/Loading crowd density data/i)).toBeInTheDocument();
  });

  it('renders error state correctly', () => {
    const errorMessage = "Network timeout";
    render(<CrowdHeatmap error={errorMessage} />);

    // The component uses role="alert" for errors
    expect(screen.getByRole('alert')).toBeInTheDocument();
    expect(screen.getByText(new RegExp(errorMessage, 'i'))).toBeInTheDocument();
  });

  it('renders crowd data and accessible table correctly', () => {
    const mockData = [
      {
        zone: 'GATE_A',
        displayName: 'Gate A',
        peopleCount: 150,
        densityLevel: 'LOW',
        estimatedWaitSeconds: 120
      },
      {
        zone: 'FOOD_COURT_EAST',
        displayName: 'Food Court East',
        peopleCount: 300,
        densityLevel: 'HIGH',
        estimatedWaitSeconds: 600
      }
    ];

    render(<CrowdHeatmap data={mockData} loading={false} error={null} />);

    // Ensure the fallback semantic table is rendered for screen readers
    const table = screen.getByRole('table', { name: /Crowd density details per zone/i });
    expect(table).toBeInTheDocument();

    // Check if table cells are populated
    expect(screen.getByText('Gate A')).toBeInTheDocument();
    expect(screen.getByText('150')).toBeInTheDocument();
    expect(screen.getByText('Food Court East')).toBeInTheDocument();

    // Check if the ZoneCard representations (mocked) are rendered in the visual grid
    expect(screen.getByTestId('zone-card-GATE_A')).toBeInTheDocument();
    expect(screen.getByTestId('zone-card-FOOD_COURT_EAST')).toBeInTheDocument();

    // Check if the total zone count is correct in the header
    expect(screen.getByText(/2 zones/i)).toBeInTheDocument();
  });

  it('handles empty data gracefully', () => {
    render(<CrowdHeatmap data={[]} loading={false} error={null} />);

    // Header should show 0 zones
    expect(screen.getByText(/0 zones/i)).toBeInTheDocument();

    // Table should still exist but have no body rows
    const table = screen.getByRole('table', { name: /Crowd density details per zone/i });
    expect(table).toBeInTheDocument();
  });
});
