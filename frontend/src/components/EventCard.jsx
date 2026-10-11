import React from 'react';
import { Link } from 'react-router-dom';

export default function EventCard({ event }) {
  return (
    <Link
      to={`/events/${event.id}`}
      style={{
        display: 'flex',
        flexDirection: 'column',
        textDecoration: 'none',
        color: 'inherit',
        cursor: 'pointer',
        transition: 'transform 200ms ease'
      }}
      className="event-card-root"
    >
      {/* 2:3 Vertical Poster Container */}
      <div style={{
        position: 'relative',
        width: '100%',
        aspectRatio: '2 / 3',
        borderRadius: 'var(--radius-md)',
        overflow: 'hidden',
        backgroundColor: 'var(--color-surface-container)',
        boxShadow: 'var(--shadow-subtle)'
      }}>
        <img
          src={event.posterUrl}
          alt={event.title}
          style={{
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            display: 'block',
            transition: 'transform 350ms cubic-bezier(0.16, 1, 0.3, 1)'
          }}
          className="poster-image"
        />

        {/* Bottom Scrim Overlay with Tag & Badge */}
        <div style={{
          position: 'absolute',
          inset: 'auto 0 0 0',
          height: '35%',
          background: 'linear-gradient(to top, rgba(26, 26, 36, 0.9) 0%, transparent 100%)',
          display: 'flex',
          alignItems: 'flex-end',
          padding: '8px 10px'
        }}>
          {event.formatBadge && (
            <span style={{
              fontSize: '10px',
              fontWeight: 700,
              letterSpacing: '0.04em',
              color: '#FFFFFF',
              backgroundColor: 'var(--color-primary)',
              padding: '2px 6px',
              borderRadius: 'var(--radius-xs)',
              textTransform: 'uppercase'
            }}>
              {event.formatBadge}
            </span>
          )}
        </div>

        {/* Urgent/Selling Fast Status Tag (Top Left) */}
        {event.tag && (
          <div style={{
            position: 'absolute',
            top: '8px',
            left: '8px',
            backgroundColor: 'rgba(26, 26, 36, 0.75)',
            backdropFilter: 'blur(4px)',
            color: '#FFFFFF',
            fontSize: '9px',
            fontWeight: 700,
            padding: '2px 6px',
            borderRadius: 'var(--radius-xs)',
            letterSpacing: '0.05em',
            textTransform: 'uppercase'
          }}>
            {event.tag}
          </div>
        )}
      </div>

      {/* Event Details Content Stack */}
      <div style={{ marginTop: '10px', display: 'flex', flexDirection: 'column', gap: '3px' }}>
        <h3 style={{
          fontSize: '15px',
          fontWeight: 700,
          color: 'var(--color-text-main)',
          lineHeight: 1.3,
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap'
        }}>
          {event.title}
        </h3>

        <div style={{
          fontSize: '12px',
          color: 'var(--color-text-secondary)',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap'
        }}>
          {event.genre || `${event.categoryLabel} | ${event.language}`}
        </div>

        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '3px',
          fontSize: '12px',
          color: 'var(--color-text-secondary)',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap'
        }}>
          <span className="material-symbols-outlined" style={{ fontSize: '14px', color: 'var(--color-primary)' }}>
            location_on
          </span>
          <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {event.venueName}
          </span>
        </div>

        <div style={{
          fontSize: '13px',
          fontWeight: 700,
          color: 'var(--color-text-main)',
          marginTop: '2px'
        }}>
          ₹{event.basePrice?.toLocaleString()} <span style={{ fontWeight: 400, fontSize: '11px', color: 'var(--color-text-secondary)' }}>onwards</span>
        </div>
      </div>

      <style>{`
        .event-card-root:hover .poster-image {
          transform: scale(1.05);
        }
      `}</style>
    </Link>
  );
}
