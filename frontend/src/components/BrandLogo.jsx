import React from 'react';
import { Link } from 'react-router-dom';

export default function BrandLogo({ mode = 'full', height = 36 }) {
  if (mode === 'icon') {
    return (
      <svg
        viewBox="0 0 90 90"
        width={height}
        height={height}
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        style={{ display: 'block' }}
      >
        {/* Ticket Badge with Edge Notches */}
        <path
          d="M 16 0 H 74 A 16 16 0 0 1 90 16 V 33 A 12 12 0 0 0 78 45 A 12 12 0 0 0 90 57 V 74 A 16 16 0 0 1 74 90 H 16 A 16 16 0 0 1 0 74 V 57 A 12 12 0 0 0 12 45 A 12 12 0 0 0 0 33 V 16 A 16 16 0 0 1 16 0 Z"
          fill="#F84464"
        />
        {/* Sound/Heartbeat Pulse Line */}
        <path
          d="M 14 45 L 29 45 L 35 34 L 42 62 L 50 22 L 58 58 L 64 45 L 76 45"
          stroke="#FFFFFF"
          strokeWidth="5.5"
          strokeLinecap="round"
          strokeLinejoin="round"
          fill="none"
        />
        <circle cx="50" cy="14" r="3.2" fill="#FFFFFF" opacity="0.95" />
      </svg>
    );
  }

  return (
    <Link to="/" style={{ display: 'inline-flex', alignItems: 'center', gap: '10px', textDecoration: 'none' }}>
      <svg
        viewBox="0 0 90 90"
        width={height}
        height={height}
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        style={{ display: 'block', flexShrink: 0 }}
      >
        <path
          d="M 16 0 H 74 A 16 16 0 0 1 90 16 V 33 A 12 12 0 0 0 78 45 A 12 12 0 0 0 90 57 V 74 A 16 16 0 0 1 74 90 H 16 A 16 16 0 0 1 0 74 V 57 A 12 12 0 0 0 12 45 A 12 12 0 0 0 0 33 V 16 A 16 16 0 0 1 16 0 Z"
          fill="#F84464"
        />
        <path
          d="M 14 45 L 29 45 L 35 34 L 42 62 L 50 22 L 58 58 L 64 45 L 76 45"
          stroke="#FFFFFF"
          strokeWidth="5.5"
          strokeLinecap="round"
          strokeLinejoin="round"
          fill="none"
        />
        <circle cx="50" cy="14" r="3.2" fill="#FFFFFF" opacity="0.95" />
      </svg>
      <div style={{ display: 'flex', flexDirection: 'column', lineHeight: 1 }}>
        <span style={{
          fontFamily: 'var(--font-inter)',
          fontSize: '22px',
          fontWeight: 900,
          letterSpacing: '-0.03em',
          color: '#1A1A24'
        }}>
          Event<span style={{ color: '#F84464', fontWeight: 800 }}>Pulse</span>
        </span>
      </div>
    </Link>
  );
}
