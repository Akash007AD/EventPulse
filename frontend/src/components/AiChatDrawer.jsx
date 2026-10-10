import React, { useState } from 'react';
import { ApiService } from '../services/api';

export default function AiChatDrawer({ isOpen, onClose }) {
  const [messages, setMessages] = useState([
    {
      sender: 'assistant',
      text: "Welcome to EventPulse Concierge. I can answer questions about scheduled performances, our 10-minute seat hold window, 48-hour cancellation policies, and calendar sync."
    }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const handleSend = async (userText) => {
    const textToSend = userText || input;
    if (!textToSend.trim()) return;

    const newMessages = [...messages, { sender: 'user', text: textToSend }];
    setMessages(newMessages);
    setInput('');
    setLoading(true);

    const reply = await ApiService.askAiAssistant(textToSend);
    setMessages([...newMessages, { sender: 'assistant', text: reply }]);
    setLoading(false);
  };

  return (
    <div style={{
      position: 'fixed',
      bottom: '24px',
      right: '24px',
      width: '380px',
      maxWidth: 'calc(100vw - 48px)',
      height: '520px',
      backgroundColor: 'var(--bg-card)',
      border: '1px solid var(--border-medium)',
      borderRadius: 'var(--radius-md)',
      boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.7), 0 8px 10px -6px rgba(0, 0, 0, 0.5)',
      display: 'flex',
      flexDirection: 'column',
      zIndex: 200,
      overflow: 'hidden'
    }}>
      {/* Drawer Header */}
      <div style={{
        padding: '14px 18px',
        backgroundColor: '#1E293B',
        borderBottom: '1px solid var(--border-subtle)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <div style={{
            width: '8px',
            height: '8px',
            borderRadius: '50%',
            backgroundColor: '#38BDF8'
          }} />
          <span style={{ fontWeight: 700, fontSize: '0.9rem', color: 'var(--text-main)', letterSpacing: '0.02em' }}>
            EventPulse AI Concierge
          </span>
        </div>
        <button 
          onClick={onClose}
          style={{
            background: 'none',
            border: 'none',
            color: 'var(--text-secondary)',
            cursor: 'pointer',
            padding: '4px'
          }}
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>
      </div>

      {/* Suggested Questions */}
      <div style={{
        padding: '10px 14px',
        backgroundColor: 'var(--bg-canvas)',
        borderBottom: '1px solid var(--border-subtle)',
        display: 'flex',
        gap: '6px',
        overflowX: 'auto',
        whiteSpace: 'nowrap'
      }}>
        {[
          "Cancellation policy?",
          "How long are seats held?",
          "How does calendar sync work?"
        ].map(q => (
          <button
            key={q}
            onClick={() => handleSend(q)}
            style={{
              backgroundColor: 'var(--bg-surface)',
              border: '1px solid var(--border-subtle)',
              borderRadius: 'var(--radius-xs)',
              padding: '4px 8px',
              fontSize: '0.72rem',
              color: 'var(--text-secondary)',
              cursor: 'pointer'
            }}
          >
            {q}
          </button>
        ))}
      </div>

      {/* Messages Scroll Area */}
      <div style={{
        flex: 1,
        padding: '16px',
        overflowY: 'auto',
        display: 'flex',
        flexDirection: 'column',
        gap: '12px'
      }}>
        {messages.map((m, idx) => (
          <div 
            key={idx}
            style={{
              alignSelf: m.sender === 'user' ? 'flex-end' : 'flex-start',
              maxWidth: '85%',
              backgroundColor: m.sender === 'user' ? 'var(--accent-primary)' : 'var(--bg-surface)',
              color: m.sender === 'user' ? '#070A10' : 'var(--text-main)',
              borderRadius: 'var(--radius-sm)',
              padding: '10px 14px',
              fontSize: '0.82rem',
              lineHeight: 1.5,
              fontWeight: m.sender === 'user' ? 600 : 400
            }}
          >
            {m.text}
          </div>
        ))}
        {loading && (
          <div style={{
            alignSelf: 'flex-start',
            backgroundColor: 'var(--bg-surface)',
            color: 'var(--text-secondary)',
            borderRadius: 'var(--radius-sm)',
            padding: '8px 12px',
            fontSize: '0.78rem'
          }}>
            Consulting policy knowledge base...
          </div>
        )}
      </div>

      {/* Input Field */}
      <form 
        onSubmit={e => { e.preventDefault(); handleSend(); }}
        style={{
          padding: '12px',
          borderTop: '1px solid var(--border-subtle)',
          backgroundColor: 'var(--bg-canvas)',
          display: 'flex',
          gap: '8px'
        }}
      >
        <input
          type="text"
          value={input}
          onChange={e => setInput(e.target.value)}
          placeholder="Ask a question..."
          style={{
            flex: 1,
            backgroundColor: 'var(--bg-card)',
            border: '1px solid var(--border-medium)',
            borderRadius: 'var(--radius-xs)',
            padding: '8px 12px',
            fontSize: '0.82rem',
            color: 'var(--text-main)',
            outline: 'none'
          }}
        />
        <button className="btn btn-sm btn-primary" type="submit" disabled={!input.trim() || loading}>
          Send
        </button>
      </form>
    </div>
  );
}
