import React from 'react';

export default function Footer({ onOpenLegal, onOpenGate }) {
  return (
    <footer style={{
      backgroundColor: 'var(--bg-canvas)',
      borderTop: '1px solid var(--border-subtle)',
      padding: '48px 0 32px 0',
      marginTop: 'auto'
    }}>
      <div className="container" style={{ display: 'flex', flexDirection: 'column', gap: '32px' }}>
        {/* Main Grid */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
          gap: '32px'
        }}>
          {/* Brand Col */}
          <div style={{ maxWidth: '320px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '12px' }}>
              <div style={{
                width: '24px',
                height: '24px',
                backgroundColor: 'var(--bg-card)',
                border: '1px solid var(--accent-primary)',
                borderRadius: 'var(--radius-xs)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--accent-primary)'
              }}>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
                  <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
                </svg>
              </div>
              <span style={{ fontFamily: 'var(--font-heading)', fontWeight: 800, fontSize: '1.1rem', letterSpacing: '-0.02em', color: 'var(--text-main)' }}>
                EVENTPULSE
              </span>
            </div>
            <p style={{ fontSize: '0.82rem', lineHeight: 1.6, color: 'var(--text-muted)' }}>
              An editorial ticketing and live event architecture designed for reserved seating venues, authentic cultural performances, and cryptographic turnstile verification.
            </p>
          </div>

          {/* Platform Col */}
          <div>
            <div style={{ fontSize: '0.78rem', textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--text-main)', fontWeight: 700, marginBottom: '14px' }}>
              Ticketing Platform
            </div>
            <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
              <li>Direct Google Calendar Sync</li>
              <li>Universal RFC 5545 .ICS Export</li>
              <li>10-Minute Concurrency Holds</li>
              <li>Razorpay Verified Orders</li>
            </ul>
          </div>

          {/* Operations Col */}
          <div>
            <div style={{ fontSize: '0.78rem', textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--text-main)', fontWeight: 700, marginBottom: '14px' }}>
              Operations & Gates
            </div>
            <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
              <li>
                <button 
                  onClick={onOpenGate}
                  style={{ background: 'none', border: 'none', color: 'inherit', cursor: 'pointer', padding: 0, font: 'inherit' }}
                >
                  Turnstile Scanner Console
                </button>
              </li>
              <li>HMAC Anti-Passback Validation</li>
              <li>Organizer Cast & Schema Editor</li>
            </ul>
          </div>

          {/* Legal Governance Col */}
          <div>
            <div style={{ fontSize: '0.78rem', textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--text-main)', fontWeight: 700, marginBottom: '14px' }}>
              Legal & Policies
            </div>
            <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
              <li>
                <button 
                  onClick={onOpenLegal}
                  style={{ background: 'none', border: 'none', color: 'var(--accent-primary)', cursor: 'pointer', padding: 0, font: 'inherit', fontWeight: 600 }}
                >
                  Terms & Conditions
                </button>
              </li>
              <li>
                <button 
                  onClick={onOpenLegal}
                  style={{ background: 'none', border: 'none', color: 'inherit', cursor: 'pointer', padding: 0, font: 'inherit' }}
                >
                  Privacy Policy & Calendar Scope
                </button>
              </li>
              <li>
                <button 
                  onClick={onOpenLegal}
                  style={{ background: 'none', border: 'none', color: 'inherit', cursor: 'pointer', padding: 0, font: 'inherit' }}
                >
                  48-Hour Refund Protocol
                </button>
              </li>
              <li>Authentic Attendance Standard</li>
            </ul>
          </div>
        </div>

        {/* Bottom Bar */}
        <div style={{
          borderTop: '1px solid var(--border-subtle)',
          paddingTop: '20px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '12px',
          fontSize: '0.78rem',
          color: 'var(--text-muted)'
        }}>
          <div>
            © {new Date().getFullYear()} EventPulse Engineering. Distributed Ticketing & High-Concurreny Event Systems.
          </div>
          <div style={{ display: 'flex', gap: '16px' }}>
            <span>No Synthetically Generated Reviews</span>
            <span>•</span>
            <span>Cryptographically Verified Passports</span>
          </div>
        </div>
      </div>
    </footer>
  );
}
