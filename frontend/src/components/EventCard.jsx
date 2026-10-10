import React from 'react';

export default function EventCard({ event, onSelect }) {
  const formattedDate = new Date(event.startTime).toLocaleDateString('en-US', {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });

  return (
    <div style={{
      backgroundColor: 'var(--bg-card)',
      border: '1px solid var(--border-subtle)',
      borderRadius: 'var(--radius-md)',
      overflow: 'hidden',
      display: 'flex',
      flexDirection: 'column',
      transition: 'border-color var(--transition-fast), transform var(--transition-fast)',
      position: 'relative'
    }}
    onMouseEnter={(e) => {
      e.currentTarget.style.borderColor = 'var(--border-medium)';
      e.currentTarget.style.transform = 'translateY(-2px)';
    }}
    onMouseLeave={(e) => {
      e.currentTarget.style.borderColor = 'var(--border-subtle)';
      e.currentTarget.style.transform = 'translateY(0)';
    }}
    >
      {/* Event Poster / Banner Container */}
      <div style={{
        position: 'relative',
        height: '210px',
        width: '100%',
        backgroundColor: '#0F172A',
        overflow: 'hidden'
      }}>
        <img 
          src={event.posterUrl || event.bannerUrl} 
          alt={event.title}
          style={{
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            filter: 'contrast(1.05) brightness(0.92)'
          }}
          loading="lazy"
        />
        <div style={{
          position: 'absolute',
          top: '12px',
          left: '12px',
          display: 'flex',
          gap: '8px'
        }}>
          <span className="badge badge-amber">{event.category}</span>
          <span className="badge">{event.city}</span>
        </div>
      </div>

      {/* Content Body */}
      <div style={{ padding: '20px', display: 'flex', flexDirection: 'column', flex: 1 }}>
        {/* Date and Venue */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-secondary)', fontSize: '0.8rem', marginBottom: '8px' }}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
            <line x1="16" y1="2" x2="16" y2="6" />
            <line x1="8" y1="2" x2="8" y2="6" />
            <line x1="3" y1="10" x2="21" y2="10" />
          </svg>
          <span>{formattedDate}</span>
        </div>

        <h3 style={{ fontSize: '1.15rem', marginBottom: '10px', lineHeight: 1.35 }}>
          {event.title}
        </h3>

        <p style={{
          fontSize: '0.85rem',
          color: 'var(--text-muted)',
          marginBottom: '16px',
          display: '-webkit-box',
          WebkitLineClamp: 2,
          WebkitBoxOrient: 'vertical',
          overflow: 'hidden'
        }}>
          {event.description}
        </p>

        {/* Performer / Cast Row */}
        {event.performers && event.performers.length > 0 && (
          <div style={{
            borderTop: '1px solid var(--border-subtle)',
            paddingTop: '12px',
            marginBottom: '16px'
          }}>
            <div style={{ fontSize: '0.72rem', textTransform: 'uppercase', letterSpacing: '0.06em', color: 'var(--text-muted)', marginBottom: '8px' }}>
              Featured Cast & Soloists
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <div style={{ display: 'flex', marginLeft: '4px' }}>
                {event.performers.slice(0, 3).map((performer, idx) => (
                  <img
                    key={performer.id || idx}
                    src={performer.imageUrl}
                    alt={performer.name}
                    title={`${performer.name} (${performer.role})`}
                    style={{
                      width: '30px',
                      height: '30px',
                      borderRadius: '50%',
                      border: '2px solid var(--bg-card)',
                      marginLeft: idx > 0 ? '-10px' : '0',
                      objectFit: 'cover'
                    }}
                  />
                ))}
              </div>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                {event.performers.map(p => p.name).slice(0, 2).join(', ')}
                {event.performers.length > 2 && ` +${event.performers.length - 2} more`}
              </span>
            </div>
          </div>
        )}

        {/* Footer info: Venue & Price & Action */}
        <div style={{
          marginTop: 'auto',
          paddingTop: '14px',
          borderTop: '1px solid var(--border-subtle)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}>
          <div>
            <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>From</div>
            <div style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--text-main)', fontFamily: 'var(--font-heading)' }}>
              ₹{event.basePrice}
            </div>
          </div>

          <button 
            className="btn btn-sm btn-primary"
            onClick={() => onSelect(event)}
          >
            <span>View & Reserve Seats</span>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <line x1="5" y1="12" x2="19" y2="12" />
              <polyline points="12 5 19 12 12 19" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  );
}
