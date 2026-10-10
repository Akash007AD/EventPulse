import React from 'react';
import { ApiService } from '../services/api';

export default function TicketModal({ event, booking, onClose, onTestAtGate }) {
  if (!event || !booking) return null;

  const handleGoogleCalendar = () => {
    const url = ApiService.getGoogleCalendarUrl(event);
    window.open(url, '_blank', 'noopener,noreferrer');
  };

  const handleDownloadIcs = () => {
    ApiService.downloadIcs(event, booking.seatNumbers);
  };

  const firstTicket = booking.tickets && booking.tickets[0];
  const signedToken = firstTicket ? firstTicket.signedToken : `EP.tkt-1001.${event.id}.6700af.99c2d1`;

  const formattedDate = new Date(event.startTime).toLocaleDateString('en-US', {
    weekday: 'long',
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div 
        className="modal-card" 
        style={{ maxWidth: '640px', backgroundColor: '#0B0F19' }} 
        onClick={e => e.stopPropagation()}
      >
        {/* Ticket Header Banner */}
        <div style={{
          backgroundColor: '#1E293B',
          borderBottom: '1px dashed var(--border-medium)',
          padding: '24px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start'
        }}>
          <div>
            <span className="badge badge-success" style={{ marginBottom: '8px' }}>
              RESERVATION CONFIRMED
            </span>
            <h2 style={{ fontSize: '1.45rem', marginTop: '6px' }}>{event.title}</h2>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
              {event.venueName} • {event.city}
            </div>
          </div>

          <button className="btn btn-sm btn-secondary" onClick={onClose} style={{ padding: '6px 10px' }}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        {/* Perforated Divider Bar */}
        <div style={{
          height: '16px',
          backgroundColor: '#070A10',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: '0 8px',
          position: 'relative'
        }}>
          <div style={{ width: '16px', height: '16px', borderRadius: '50%', backgroundColor: 'var(--bg-canvas)', marginLeft: '-16px' }} />
          <div style={{ flex: 1, borderBottom: '2px dashed var(--border-medium)', margin: '0 8px' }} />
          <div style={{ width: '16px', height: '16px', borderRadius: '50%', backgroundColor: 'var(--bg-canvas)', marginRight: '-16px' }} />
        </div>

        {/* Ticket Body with Details and QR Pass */}
        <div style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {/* Metadata Grid */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))',
            gap: '16px',
            backgroundColor: '#131B2E',
            padding: '16px',
            borderRadius: 'var(--radius-sm)',
            border: '1px solid var(--border-subtle)'
          }}>
            <div>
              <div style={{ fontSize: '0.7rem', textTransform: 'uppercase', color: 'var(--text-muted)' }}>Date & Time</div>
              <div style={{ fontWeight: 600, fontSize: '0.85rem', color: 'var(--text-main)', marginTop: '2px' }}>
                {formattedDate}
              </div>
            </div>
            <div>
              <div style={{ fontSize: '0.7rem', textTransform: 'uppercase', color: 'var(--text-muted)' }}>Assigned Seats</div>
              <div style={{ fontWeight: 700, fontSize: '0.95rem', color: 'var(--accent-primary)', marginTop: '2px' }}>
                {booking.seatNumbers ? booking.seatNumbers.join(', ') : 'Allocated Pass'}
              </div>
            </div>
            <div>
              <div style={{ fontSize: '0.7rem', textTransform: 'uppercase', color: 'var(--text-muted)' }}>Booking Reference</div>
              <div style={{ fontFamily: 'var(--font-mono)', fontSize: '0.82rem', color: 'var(--text-main)', marginTop: '2px' }}>
                {booking.id || 'BK-789234'}
              </div>
            </div>
          </div>

          {/* Cryptographically Signed Gate Pass Area */}
          <div style={{
            backgroundColor: '#111827',
            border: '1px solid var(--border-medium)',
            borderRadius: 'var(--radius-sm)',
            padding: '20px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '20px',
            flexWrap: 'wrap'
          }}>
            <div style={{ flex: 1, minWidth: '220px' }}>
              <div style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: 'var(--text-muted)', letterSpacing: '0.08em' }}>
                HMAC-SHA256 SIGNED GATE TOKEN
              </div>
              <div style={{
                fontFamily: 'var(--font-mono)',
                fontSize: '0.78rem',
                color: 'var(--accent-primary)',
                wordBreak: 'break-all',
                marginTop: '6px',
                lineHeight: 1.5,
                backgroundColor: '#070A10',
                padding: '8px 10px',
                borderRadius: 'var(--radius-xs)',
                border: '1px solid var(--border-subtle)'
              }}>
                {signedToken}
              </div>
              <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginTop: '6px' }}>
                Protected against duplicate entry via venue anti-passback guardrails.
              </div>
            </div>

            {/* Stylized QR Matrix simulation */}
            <div style={{
              width: '100px',
              height: '100px',
              backgroundColor: '#FFFFFF',
              padding: '6px',
              borderRadius: 'var(--radius-xs)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0
            }}>
              <svg width="88" height="88" viewBox="0 0 24 24" fill="none" stroke="#000000" strokeWidth="1.8">
                <rect x="2" y="2" width="7" height="7" />
                <rect x="15" y="2" width="7" height="7" />
                <rect x="2" y="15" width="7" height="7" />
                <rect x="5" y="5" width="1" height="1" fill="#000" />
                <rect x="18" y="5" width="1" height="1" fill="#000" />
                <rect x="5" y="18" width="1" height="1" fill="#000" />
                <rect x="13" y="13" width="3" height="3" />
                <rect x="18" y="15" width="4" height="2" />
                <rect x="15" y="19" width="3" height="3" />
              </svg>
            </div>
          </div>

          {/* Calendar Synchronization Buttons (Direct Google Calendar + RFC 5545 .ics) */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            <div style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: 'var(--text-muted)' }}>
              Calendar Integration & Exports
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '12px' }}>
              <button 
                className="btn btn-primary"
                onClick={handleGoogleCalendar}
                style={{ width: '100%' }}
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
                  <line x1="16" y1="2" x2="16" y2="6" />
                  <line x1="8" y1="2" x2="8" y2="6" />
                  <line x1="3" y1="10" x2="21" y2="10" />
                </svg>
                Sync with Google Calendar
              </button>

              <button 
                className="btn btn-secondary"
                onClick={handleDownloadIcs}
                style={{ width: '100%' }}
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                  <polyline points="7 10 12 15 17 10" />
                  <line x1="12" y1="15" x2="12" y2="3" />
                </svg>
                Download .ICS File
              </button>
            </div>
          </div>

          {/* Test at Gate Button */}
          <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: '16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
              Simulate admission verification
            </span>
            <button 
              className="btn btn-sm btn-outline"
              onClick={() => onTestAtGate(signedToken)}
            >
              Test This Token at Gate Scanner
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
