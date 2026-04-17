import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import ZoneCard from './ZoneCard';

describe('ZoneCard Component', () => {
  const mockZone = {
    displayName: 'Gate A',
    currentCount: 450,
    capacity: 500,
    occupancyRate: 0.9,
    densityLevel: 'HIGH',
  };

  it('renders zone details correctly', () => {
    render(<ZoneCard zone={mockZone} />);
    
    expect(screen.getByText('Gate A')).toBeDefined();
    expect(screen.getByText('450')).toBeDefined();
    expect(screen.getByText('/ 500')).toBeDefined();
    expect(screen.getByText('HIGH')).toBeDefined();
  });

  it('sets accessibility attributes correctly', () => {
    render(<ZoneCard zone={mockZone} />);
    
    const article = screen.getByRole('article');
    expect(article.getAttribute('aria-label')).toContain('Gate A');
    expect(article.getAttribute('aria-label')).toContain('HIGH density');

    const progressBar = screen.getByRole('progressbar');
    expect(progressBar.getAttribute('aria-valuenow')).toBe('90');
    expect(progressBar.getAttribute('aria-label')).toBe('90% occupied');
  });

  it('applies density level data attributes for styling', () => {
    render(<ZoneCard zone={mockZone} />);
    
    const article = screen.getByRole('article');
    expect(article.getAttribute('data-density')).toBe('HIGH');
  });
});
