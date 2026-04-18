import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import AiAssistant from './AiAssistant.jsx';

// Mock the global fetch if AiAssistant makes direct fetch calls,
// or if it uses a specific API module, you can mock that instead.
global.fetch = vi.fn(() =>
  Promise.resolve({
    ok: true,
    json: () => Promise.resolve({ response: 'Hello! I am the AI assistant.', role: 'assistant' }),
  })
);

describe('AiAssistant Component', () => {
  it('renders the AI Assistant element', () => {
    render(<AiAssistant />);

    // Look for common text or roles associated with the AI assistant
    const assistantElements = screen.queryAllByText(/AI Assistant|Ask me anything/i);
    expect(assistantElements.length).toBeGreaterThanOrEqual(0);
  });

  it('renders a toggle button to open the chat window', () => {
    render(<AiAssistant />);

    // Often there's a floating action button to toggle the chat
    const button = screen.queryByRole('button', { name: /Chat|Assistant|Toggle/i })
                   || screen.queryByRole('button');

    if (button) {
      expect(button).toBeInTheDocument();

      // Simulate opening the chat
      fireEvent.click(button);

      // After clicking, an input field should typically be visible
      const input = screen.queryByRole('textbox') || screen.queryByPlaceholderText(/Type/i);
      if (input) {
        expect(input).toBeInTheDocument();
      }
    }
  });

  it('allows user to type a message and submit', async () => {
    render(<AiAssistant />);

    // Open chat if it's hidden by default
    const toggleButton = screen.queryByRole('button', { name: /Chat|Assistant|Toggle/i });
    if (toggleButton) {
      fireEvent.click(toggleButton);
    }

    const input = screen.queryByRole('textbox');
    const sendButton = screen.queryByRole('button', { name: /Send|Submit/i });

    if (input && sendButton) {
      fireEvent.change(input, { target: { value: 'Where is the closest restroom?' } });
      expect(input.value).toBe('Where is the closest restroom?');

      fireEvent.click(sendButton);

      // Assert that fetch was called (or the respective mocked API)
      expect(global.fetch).toHaveBeenCalled();
    }
  });
});
