import React from 'react';

export default function LegalModal({ onClose, defaultTab = 'terms' }) {
  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div 
        className="modal-card" 
        style={{ maxWidth: '720px' }} 
        onClick={e => e.stopPropagation()}
      >
        {/* Header */}
        <div style={{
          padding: '20px 24px',
          borderBottom: '1px solid var(--border-subtle)',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <div>
            <h3 style={{ fontSize: '1.25rem' }}>Platform Governance & Terms of Service</h3>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
              EventPulse Legal, Ticketing, and Privacy Architecture
            </div>
          </div>
          <button className="btn btn-sm btn-secondary" onClick={onClose}>
            Close
          </button>
        </div>

        {/* Legal Text Body */}
        <div style={{
          padding: '24px',
          display: 'flex',
          flexDirection: 'column',
          gap: '20px',
          fontSize: '0.88rem',
          lineHeight: 1.7,
          color: 'var(--text-secondary)'
        }}>
          <div>
            <h4 style={{ color: 'var(--text-main)', marginBottom: '6px' }}>1. Seat Reservation & Distributed Lock Policy</h4>
            <p>
              When a user selects seats and proceeds to checkout, EventPulse locks the requested inventory via distributed Redis/Redisson locks for a maximum duration of ten (10) minutes. If the payment flow is not finalized within this window, the lock automatically expires and the seats revert to public availability.
            </p>
          </div>

          <div>
            <h4 style={{ color: 'var(--text-main)', marginBottom: '6px' }}>2. Cancellation & Refund Policy</h4>
            <p>
              Tickets cancelled at least forty-eight (48) hours prior to the scheduled event start time are eligible for a 100% refund back to the original Razorpay payment source. Refund transactions settle within 3 to 5 business days. Cancellations inside 48 hours are non-refundable unless the organizer cancels or reschedules the event.
            </p>
          </div>

          <div>
            <h4 style={{ color: 'var(--text-main)', marginBottom: '6px' }}>3. Cryptographic Gate Admission & Anti-Passback</h4>
            <p>
              Every EventPulse ticket pass contains a cryptographically signed HMAC-SHA256 token. Entry turnstiles enforce strict anti-passback rules: once a ticket has been scanned, any duplicate presentation of the same token will trigger an immediate breach alert and deny admission.
            </p>
          </div>

          <div>
            <h4 style={{ color: 'var(--text-main)', marginBottom: '6px' }}>4. Google Calendar OAuth2 Scope Privacy Notice</h4>
            <p>
              When using the "Sync with Google Calendar" feature, EventPulse requests access to the <code>https://www.googleapis.com/auth/calendar.events</code> scope solely to insert the confirmed event details into the user's personal calendar. EventPulse does not read, modify, or store any existing user calendar data.
            </p>
          </div>

          <div>
            <h4 style={{ color: 'var(--text-main)', marginBottom: '6px' }}>5. Authentic Review Policy</h4>
            <p>
              EventPulse strictly prohibits synthetic, automated, or commercially incentivized reviews. Only verified attendees who completed gate turnstile check-in are eligible to submit post-event feedback.
            </p>
          </div>
        </div>

        {/* Footer */}
        <div style={{ padding: '16px 24px', borderTop: '1px solid var(--border-subtle)', textAlign: 'right' }}>
          <button className="btn btn-primary" onClick={onClose}>
            Acknowledge & Close
          </button>
        </div>
      </div>
    </div>
  );
}
