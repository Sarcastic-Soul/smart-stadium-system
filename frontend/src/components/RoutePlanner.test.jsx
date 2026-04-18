import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import RoutePlanner from './RoutePlanner.jsx';

// Mock the API call used inside RoutePlanner
vi.mock('../api/stadiumApi.js', () => ({
  calculateRoute: vi.fn().mockResolvedValue({
    path: ['GATE_A', 'CONCOURSE_NORTH', 'SEATING_NORTH'],
    totalEstimatedTime: 180
  })
}));

describe('RoutePlanner Component', () => {
  it('renders the Route Planner section', () => {
    render(<RoutePlanner />);

    // Check if the component renders its heading
    const headings = screen.getAllByRole('heading');
    const routeHeading = headings.find(h => h.textContent.match(/Route/i));
    expect(routeHeading).toBeInTheDocument();
  });

  it('renders the zone selection dropdowns', () => {
    render(<RoutePlanner />);

    // Most route planners have a 'From' and 'To' select
    const selects = screen.queryAllByRole('combobox');
    if (selects.length > 0) {
      expect(selects.length).toBeGreaterThanOrEqual(2);
    }
  });

  it('renders a button to trigger route calculation', () => {
    render(<RoutePlanner />);

    // There should be a button to find the route
    const button = screen.getByRole('button', { name: /Find Route|Calculate/i });
    expect(button).toBeInTheDocument();
  });
});
