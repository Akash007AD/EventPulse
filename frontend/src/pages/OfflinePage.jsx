import React from 'react';
import { Link } from 'react-router-dom';

export default function OfflinePage() {
  const handleRetry = () => {
    window.location.reload();
  };

  return (
    <div style={{ backgroundColor: 'var(--color-surface)', minHeight: '80vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '40px 20px' }}>
      <div style={{
        maxWidth: '520px',
        width: '100%',
        backgroundColor: '#FFFFFF',
        borderRadius: 'var(--radius-xl)',
        border: '1px solid var(--color-border)',
        boxShadow: 'var(--shadow-card)',
        padding: '48px 32px',
        textAlign: 'center'
      }}>
        {/* Offline Signal Icon */}
        <div style={{
          width: '80px',
          height: '80px',
          borderRadius: '50%',
          backgroundColor: '#FEE2E2',
          color: 'var(--color-primary)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          margin: '0 auto 20px'
        }}>
          <span className="material-symbols-outlined" style={{ fontSize: '42px' }}>wifi_off</span>
        </div>

        <h1 style={{ fontSize: '26px', fontWeight: 800, color: 'var(--color-text-main)', marginBottom: '8px' }}>
          You're Offline
        </h1>

        <p style={{ fontSize: '14px', color: 'var(--color-text-secondary)', lineHeight: 1.6, marginBottom: '28px' }}>
          It looks like you've lost internet connection. Please check your Wi-Fi or cellular data network and try again.
        </p>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '24px' }}>
          <button
            onClick={handleRetry}
            className="btn btn-primary btn-lg"
            style={{ width: '100%', justifyContent: 'center' }}
          >
            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>refresh</span>
            <span>Retry Connection</span>
          </button>

          <Link
            to="/my-tickets"
            className="btn btn-secondary"
            style={{ width: '100%', justifyContent: 'center' }}
          >
            View Saved Offline M-Tickets
          </Link>
        </div>

        <div style={{
          padding: '12px 16px',
          backgroundColor: 'var(--color-surface)',
          borderRadius: 'var(--radius-md)',
          fontSize: '12px',
          color: 'var(--color-text-secondary)',
          textAlign: 'left'
        }}>
          <strong>Tip for Event Day:</strong> Your previously confirmed M-Ticket QR passes remain accessible for gate turnstile admission even without active internet.
        </div>
      </div>
    </div>
  );
}
