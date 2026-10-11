import React, { useState } from 'react';
import { Link } from 'react-router-dom';

export default function AiAssistantWidget({ isOpen, onToggle, onClose }) {
  const [messages, setMessages] = useState([
    {
      id: 1,
      sender: 'bot',
      text: "Hi! Looking for live concerts, comedy shows, or need help with ticket policies?",
      timestamp: "Just now"
    }
  ]);
  const [inputValue, setInputValue] = useState('');

  const quickPrompts = [
    "Concerts this weekend in Mumbai",
    "Cancellation & refund policy",
    "Stand-up comedy specials",
    "How to access my M-Ticket"
  ];

  const handleSend = (textToSend) => {
    const query = textToSend || inputValue;
    if (!query.trim()) return;

    // Add user message
    const userMsg = {
      id: Date.now(),
      sender: 'user',
      text: query,
      timestamp: "Just now"
    };

    setMessages(prev => [...prev, userMsg]);
    setInputValue('');

    // Simulate smart consumer response
    setTimeout(() => {
      let botResponse = {
        id: Date.now() + 1,
        sender: 'bot',
        text: "Here is what's trending right now for live shows:",
        timestamp: "Just now",
        recommendedEvent: {
          id: "evt-001",
          title: "Acoustic Nights with Arijit Singh Live",
          venue: "DY Patil Stadium, Navi Mumbai",
          price: "₹1,999 onwards"
        }
      };

      if (query.toLowerCase().includes('refund') || query.toLowerCase().includes('cancel')) {
        botResponse = {
          id: Date.now() + 1,
          sender: 'bot',
          text: "Tickets are refundable up to 24 hours before showtime minus standard gateway convenience fees. After 24 hours, bookings are locked for gate admission.",
          timestamp: "Just now"
        };
      }

      setMessages(prev => [...prev, botResponse]);
    }, 600);
  };

  return (
    <>
      {/* 1. Floating Action Button (FAB) in Bottom Right Corner */}
      <div style={{
        position: 'fixed',
        bottom: '24px',
        right: '24px',
        zIndex: 999
      }}>
        {!isOpen && (
          <button
            onClick={onToggle}
            className="btn btn-primary"
            style={{
              padding: '12px 20px',
              boxShadow: 'var(--shadow-modal)',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              borderRadius: 'var(--radius-full)'
            }}
          >
            <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>smart_toy</span>
            <span style={{ fontWeight: 700 }}>Ask Assistant</span>
            <span style={{
              width: '8px',
              height: '8px',
              borderRadius: '50%',
              backgroundColor: '#4ADE80',
              boxShadow: '0 0 6px #4ADE80'
            }} />
          </button>
        )}
      </div>

      {/* 2. Raised Interactive Assistant Window / Drawer */}
      {isOpen && (
        <div style={{
          position: 'fixed',
          bottom: '24px',
          right: '24px',
          width: '380px',
          height: '540px',
          backgroundColor: '#FFFFFF',
          borderRadius: 'var(--radius-xl)',
          border: '1px solid var(--color-border)',
          boxShadow: 'var(--shadow-modal)',
          zIndex: 1000,
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden'
        }} className="ai-widget-card">
          {/* Header */}
          <div style={{
            padding: '16px 20px',
            backgroundColor: '#FFFFFF',
            borderBottom: '1px solid var(--color-border)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <div style={{
                width: '32px',
                height: '32px',
                borderRadius: '50%',
                backgroundColor: '#FFF1F2',
                color: 'var(--color-primary)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}>
                <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>smart_toy</span>
              </div>
              <div>
                <div style={{ fontWeight: 800, fontSize: '14px', color: 'var(--color-text-main)' }}>
                  EventPulse Assistant
                </div>
                <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)' }}>
                  Instant answers for shows &amp; tickets
                </div>
              </div>
            </div>

            <button
              onClick={onClose}
              style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--color-text-secondary)' }}
            >
              <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>close</span>
            </button>
          </div>

          {/* Quick Prompt Chips */}
          <div style={{
            padding: '10px 14px',
            backgroundColor: 'var(--color-surface)',
            borderBottom: '1px solid var(--color-border-subtle)',
            display: 'flex',
            gap: '6px',
            overflowX: 'auto',
            whiteSpace: 'nowrap'
          }} className="no-scrollbar">
            {quickPrompts.map((prompt, idx) => (
              <button
                key={idx}
                onClick={() => handleSend(prompt)}
                style={{
                  fontSize: '11px',
                  fontWeight: 600,
                  backgroundColor: '#FFFFFF',
                  color: 'var(--color-text-secondary)',
                  border: '1px solid var(--color-border)',
                  padding: '4px 10px',
                  borderRadius: 'var(--radius-full)',
                  cursor: 'pointer'
                }}
              >
                {prompt}
              </button>
            ))}
          </div>

          {/* Chat Messages Stream */}
          <div style={{
            flex: 1,
            padding: '16px',
            overflowY: 'auto',
            display: 'flex',
            flexDirection: 'column',
            gap: '12px'
          }}>
            {messages.map(msg => (
              <div
                key={msg.id}
                style={{
                  alignSelf: msg.sender === 'user' ? 'flex-end' : 'flex-start',
                  maxWidth: '85%'
                }}
              >
                <div style={{
                  padding: '10px 14px',
                  borderRadius: 'var(--radius-lg)',
                  fontSize: '13px',
                  lineHeight: 1.5,
                  backgroundColor: msg.sender === 'user' ? 'var(--color-primary)' : 'var(--color-surface)',
                  color: msg.sender === 'user' ? '#FFFFFF' : 'var(--color-text-main)',
                  border: msg.sender === 'user' ? 'none' : '1px solid var(--color-border)'
                }}>
                  {msg.text}
                </div>

                {/* Optional In-Chat Event Recommendation Card */}
                {msg.recommendedEvent && (
                  <div style={{
                    marginTop: '8px',
                    padding: '10px',
                    backgroundColor: '#FFFFFF',
                    borderRadius: 'var(--radius-md)',
                    border: '1px solid var(--color-border)',
                    boxShadow: 'var(--shadow-subtle)',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '4px'
                  }}>
                    <div style={{ fontWeight: 700, fontSize: '13px', color: 'var(--color-text-main)' }}>
                      {msg.recommendedEvent.title}
                    </div>
                    <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)' }}>
                      {msg.recommendedEvent.venue} • {msg.recommendedEvent.price}
                    </div>
                    <Link
                      to={`/events/${msg.recommendedEvent.id}`}
                      onClick={onClose}
                      className="btn btn-outline-primary btn-sm"
                      style={{ marginTop: '6px', alignSelf: 'flex-start', fontSize: '11px', padding: '4px 10px' }}
                    >
                      View Show &amp; Seats
                    </Link>
                  </div>
                )}
              </div>
            ))}
          </div>

          {/* Bottom Message Input Field */}
          <div style={{
            padding: '12px 14px',
            borderTop: '1px solid var(--color-border)',
            backgroundColor: '#FFFFFF',
            display: 'flex',
            gap: '8px'
          }}>
            <input
              type="text"
              placeholder="Ask about live shows or tickets..."
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSend()}
              style={{
                flex: 1,
                padding: '8px 12px',
                borderRadius: 'var(--radius-full)',
                border: '1px solid var(--color-border-strong)',
                fontSize: '13px',
                outline: 'none'
              }}
            />
            <button
              onClick={() => handleSend()}
              className="btn btn-primary"
              style={{ width: '36px', height: '36px', padding: 0, borderRadius: '50%', flexShrink: 0 }}
            >
              <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>send</span>
            </button>
          </div>
        </div>
      )}

      <style>{`
        @media (max-width: 640px) {
          .ai-widget-card {
            inset: 0 0 0 0 !important;
            width: 100vw !important;
            height: 100vh !important;
            border-radius: 0 !important;
          }
        }
      `}</style>
    </>
  );
}
