import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import App from './App.jsx';

// Mock child components to isolate App logic
vi.mock('./components/Header.jsx', () => ({
  default: ({ lastUpdated }) => <header data-testid="mock-header">Mock Header {lastUpdated?.toISOString()}</header>
}));
vi.mock('./components/CrowdHeatmap.jsx', () => ({
  default: () => <div data-testid="mock-heatmap">Mock Heatmap</div>
}));
vi.mock('./components/RoutePlanner.jsx', () => ({
  default: () => <div data-testid="mock-route-planner">Mock Route Planner</div>
}));
vi.mock('./components/QueueTimes.jsx', () => ({
  default: () => <div data-testid="mock-queue-times">Mock Queue Times</div>
}));
vi.mock('./components/AiAssistant.jsx', () => ({
  default: () => <div data-testid="mock-ai-assistant">Mock AI Assistant</div>
}));

// Mock the hook
vi.mock('./hooks/useStompData.js', () => ({
  useStompData: vi.fn(() => ({
    data: [],
    loading: false,
    error: null,
    lastUpdated: new Date('2026-04-16T12:00:00Z')
  }))
}));

describe('App Component', () => {
  it('renders the skip-link and main components', () => {
    render(<App />);
    
    expect(screen.getByText('Skip to main content')).toBeInTheDocument();
    expect(screen.getByTestId('mock-header')).toBeInTheDocument();
    expect(screen.getByTestId('mock-heatmap')).toBeInTheDocument();
    expect(screen.getByTestId('mock-route-planner')).toBeInTheDocument();
    expect(screen.getByTestId('mock-queue-times')).toBeInTheDocument();
    expect(screen.getByTestId('mock-ai-assistant')).toBeInTheDocument();
  });
});
