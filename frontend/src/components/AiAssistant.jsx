import React, { useState, useRef, useEffect } from 'react';
import './AiAssistant.css';

export default function AiAssistant() {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([
    { role: 'assistant', text: 'Hi! I am your AI Stadium Assistant. How can I help you today?' }
  ]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const toggleChat = () => setIsOpen(!isOpen);

  const sendMessage = async (e) => {
    e.preventDefault();
    if (!input.trim()) return;

    const userMessage = input.trim();
    setMessages(prev => [...prev, { role: 'user', text: userMessage }]);
    setInput('');
    setIsLoading(true);

    try {
      const response = await fetch('/api/ai/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: userMessage })
      });
      
      const data = await response.json();
      setMessages(prev => [...prev, { role: 'assistant', text: data.response }]);
    } catch (error) {
      console.error("Chat error:", error);
      setMessages(prev => [...prev, { role: 'assistant', text: "Sorry, I'm having trouble connecting to the servers right now." }]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="ai-assistant-wrapper">
      {isOpen && (
        <div className="ai-chat-window">
          <div className="ai-chat-header">
            <h3>AI Assistant (Gemini)</h3>
            <button className="ai-close-btn" onClick={toggleChat} aria-label="Close Chat">×</button>
          </div>
          
          <div className="ai-chat-messages">
            {messages.map((msg, idx) => (
              <div key={idx} className={`ai-message ${msg.role}`}>
                {msg.text}
              </div>
            ))}
            {isLoading && <div className="ai-message assistant typing">typing...</div>}
            <div ref={messagesEndRef} />
          </div>

          <form className="ai-chat-input" onSubmit={sendMessage}>
            <input 
              type="text" 
              value={input} 
              onChange={e => setInput(e.target.value)} 
              placeholder="Ask about restrooms, food..."
              aria-label="Message to AI Assistant"
            />
            <button type="submit" disabled={isLoading}>Send</button>
          </form>
        </div>
      )}
      
      <button 
        className={`ai-fab ${isOpen ? 'active' : ''}`} 
        onClick={toggleChat}
        aria-label="Toggle AI Assistant"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path strokeLinecap="round" strokeLinejoin="round" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"></path></svg>
      </button>
    </div>
  );
}
