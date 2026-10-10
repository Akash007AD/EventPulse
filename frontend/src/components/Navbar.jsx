import React from 'react';

export default function Navbar({ onNavigate, activeTab, onOpenAi, onOpenGate }) {
  return (
    <header style={{
      backgroundColor: 'var(--bg-canvas)',
      borderBottom: '1px solid var(--border-subtle)',
      position: 'sticky',
      top: 0,
      zIndex: 100,
      backdropFilter: 'blur(8px)',
      background: 'rgba(7, 10, 16, 0.92)'
    }}>
      <div className="container" style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        height: '68px'
      }}>
        {/* Brand */}
        <div 
          onClick={() => onNavigate('events')} 
          style={{ 
            display: 'flex', 
            alignItems: 'center', 
            gap: '12px', 
            cursor: 'pointer' 
          }}
        >
          <div style={{
            width: '32px',
            height: '32px',
            backgroundColor: 'var(--bg-card)',
            border: '1px solid var(--accent-primary)',
            borderRadius: 'var(--radius-xs)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'var(--accent-primary)'
          }}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
              <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
            </svg>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <span style={{ 
              fontFamily: 'var(--font-heading)', 
              fontWeight: 800, 
              fontSize: '1.2rem', 
              letterSpacing: '-0.03em',
              color: 'var(--text-main)',
              lineHeight: 1
            }}>
              EVENTPULSE
            </span>
            <span style={{ 
              fontSize: '0.65rem', 
              letterSpacing: '0.12em', 
              color: 'var(--accent-primary)', 
              fontWeight: 600,
              marginTop: '3px'
            }}>
              RESERVED TICKETING & GATE
            </span>
          </div>
        </div>

        {/* Navigation Links */}
        <nav style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <button 
            className={`btn btn-sm ${activeTab === 'events' ? 'btn-secondary' : 'btn-outline'}`}
            onClick={() => onNavigate('events')}
            style={{ borderColor: activeTab === 'events' ? 'var(--accent-primary)' : 'var(--border-subtle)' }}
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
              <line x1="16" y1="2" x2="16" y2="6" />
              <line x1="8" y1="2" x2="8" y2="6" />
              <line x1="3" y1="10" x2="21" y2="10" />
            </svg>
            Events Catalog
          </button>

          <button 
            className="btn btn-sm btn-outline"
            onClick={onOpenGate}
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M3 7V5a2 2 0 0 1 2-2h2" />
              <path d="M17 3h2a2 2 0 0 1 2 2v2" />
              <path d="M21 17v2a2 2 0 0 1-2 2h-2" />
              <path d="M7 21H5a2 2 0 0 1-2-2v-2" />
              <rect x="7" y="7" width="10" height="10" rx="1" />
            </svg>
            Gate Scanner
          </button>

          <button 
            className="btn btn-sm btn-outline"
            onClick={onOpenAi}
            style={{ color: '#38BDF8', borderColor: 'rgba(56, 189, 248, 0.35)' }}
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
            </svg>
            AI Concierge
          </button>
        </nav>
      </div>
    </header>
  );
}
