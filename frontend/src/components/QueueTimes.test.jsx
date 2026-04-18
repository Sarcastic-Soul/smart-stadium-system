import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import QueueTimes from './QueueTimes.jsx';

describe('QueueTimes Component', () => {
  it('renders loading state correctly', () => {
    render(<QueueTimes loading={true} />);

    expect(screen.getByRole('status')).toBeInTheDocument();
    expect(screen.getByText(/Loading queue times/i)).toBeInTheDocument();
  });

  it('renders error state correctly', () => {
    const errorMessage = "Failed to fetch queues";
    render(<QueueTimes error={errorMessage} />);

    expect(screen.getByRole('alert')).toBeInTheDocument();
    expect(screen.getByText(new RegExp(errorMessage, 'i'))).toBeInTheDocument();
  });

  it('renders queue data correctly and filters non-queue zones', () => {
    const mockData = [
      { zone: 'GATE_A', displayName: 'Gate A', queueLength: 50, estimatedWaitSeconds: 300 },
      { zone: 'FOOD_COURT_EAST', displayName: 'Food Court East', queueLength: 10, estimatedWaitSeconds: 60 },
      // SEATING_NORTH is typically filtered out by the component's internal logic
      { zone: 'SEATING_NORTH', displayName: 'Seating North', queueLength: 0, estimatedWaitSeconds: 0 }
    ];

    render(<QueueTimes data={mockData} loading={false} error={null} />);

    // Since SEATING_NORTH is not a queue zone, only 2 should render
    expect(screen.getByText(/2 queues/i)).toBeInTheDocument();

    // Check if valid zones are rendered
    expect(screen.getByText('Gate A')).toBeInTheDocument();
    expect(screen.getByText('Food Court East')).toBeInTheDocument();

    // Check wait times formatting (300s -> 5 min, 60s -> 1 min)
    expect(screen.getByText('5')).toBeInTheDocument();
    expect(screen.getByText('1')).toBeInTheDocument();

    // Verify aria-live polite region exists for accessibility
    const list = screen.getByRole('list', { name: /Queue wait times by zone/i });
    expect(list).toHaveAttribute('aria-live', 'polite');
  });

  it('handles empty data gracefully', () => {
    render(<QueueTimes data={[]} loading={false} error={null} />);

    expect(screen.getByText(/0 queues/i)).toBeInTheDocument();
    expect(screen.getByRole('list', { name: /Queue wait times by zone/i })).toBeEmptyDOMElement();
  });
});
