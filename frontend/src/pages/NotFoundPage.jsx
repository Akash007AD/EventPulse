import React from 'react';
import { Link } from 'react-router-dom';

export default function NotFoundPage() {
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
        {/* Spotlighting Stage Graphic */}
        <div style={{
          width: '80px',
          height: '80px',
          borderRadius: '50%',
          backgroundColor: '#FFF1F2',
          color: 'var(--color-primary)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          margin: '0 auto 20px'
        }}>
          <span className="material-symbols-outlined" style={{ fontSize: '44px' }}>theater_comedy</span>
        </div>

        <span className="badge-pill badge-crimson" style={{ marginBottom: '12px' }}>
          404 — SHOW NOT FOUND
        </span>

        <h1 style={{ fontSize: '26px', fontWeight: 800, color: 'var(--color-text-main)', marginBottom: '8px' }}>
          Oops! This Stage Is Quiet
        </h1>

        <p style={{ fontSize: '14px', color: 'var(--color-text-secondary)', lineHeight: 1.6, marginBottom: '28px' }}>
          The event or page you are looking for might have concluded, been rescheduled, or is no longer available at this address.
        </p>

        <div style={{ display: 'flex', gap: '12px', justifyContent: 'center', flexWrap: 'wrap' }}>
          <Link to="/" className="btn btn-primary btn-lg">
            <span>Back to Live Events</span>
            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>arrow_forward</span>
          </Link>
          <Link to="/?category=CONCERT" className="btn btn-secondary btn-lg">
            Browse Concerts
          </Link>
        </div>
      </div>
    </div>
  );
}
