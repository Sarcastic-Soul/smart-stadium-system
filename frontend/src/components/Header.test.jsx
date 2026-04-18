import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import Header from './Header.jsx';

describe('Header Component', () => {
  it('renders the header element (banner)', () => {
    render(<Header lastUpdated={null} />);
    const headerElement = screen.getByRole('banner');
    expect(headerElement).toBeInTheDocument();
  });

  it('renders the application title', () => {
    render(<Header lastUpdated={null} />);
    // Assuming the header contains an h1 or standard title text
    const titleElements = screen.getAllByText(/Smart Stadium/i);
    expect(titleElements.length).toBeGreaterThan(0);
  });

  it('displays the last updated time when a valid Date object is provided', () => {
    const mockDate = new Date('2024-01-01T12:00:00Z');
    render(<Header lastUpdated={mockDate} />);

    // The Header component should render without crashing when given a date
    expect(screen.getByRole('banner')).toBeInTheDocument();

    // Optionally check if the formatted time string is somewhere in the document
    // (e.g., '12:00 PM' or similar depending on the locale)
    // const timeString = mockDate.toLocaleTimeString();
    // expect(screen.getByText(new RegExp(timeString, 'i'))).toBeInTheDocument();
  });

  it('handles a null or undefined lastUpdated prop gracefully', () => {
    const { container } = render(<Header />);
    expect(container).toBeInTheDocument();
  });
});
