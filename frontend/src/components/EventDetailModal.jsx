import React from 'react';

export default function EventDetailModal({ event, onClose, onSelectSeats }) {
  if (!event) return null;

  const formattedDate = new Date(event.startTime).toLocaleDateString('en-US', {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div 
        className="modal-card" 
        style={{ maxWidth: '820px' }} 
        onClick={e => e.stopPropagation()}
      >
        {/* Header Hero Banner with Organizer Poster Slot */}
        <div style={{
          position: 'relative',
          height: '280px',
          backgroundColor: '#0F172A',
          backgroundImage: `linear-gradient(to top, rgba(17, 24, 39, 0.95), rgba(17, 24, 39, 0.2)), url(${event.bannerUrl || event.posterUrl})`,
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          display: 'flex',
          alignItems: 'flex-end',
          padding: '24px'
        }}>
          {/* Close button */}
          <button 
            onClick={onClose}
            className="btn btn-sm btn-secondary"
            style={{ position: 'absolute', top: '16px', right: '16px', padding: '6px 10px' }}
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>

          <div style={{ display: 'flex', gap: '20px', alignItems: 'flex-end', width: '100%' }}>
            {/* Dedicated Organizer Poster Frame */}
            <div style={{
              width: '110px',
              height: '150px',
              backgroundColor: '#1E293B',
              border: '2px solid var(--border-medium)',
              borderRadius: 'var(--radius-sm)',
              overflow: 'hidden',
              flexShrink: 0,
              boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.7)'
            }}>
              <img 
                src={event.posterUrl || event.bannerUrl} 
                alt="Event Official Poster" 
                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
              />
            </div>

            {/* Title & Metadata */}
            <div>
              <div style={{ display: 'flex', gap: '8px', marginBottom: '8px' }}>
                <span className="badge badge-amber">{event.category}</span>
                <span className="badge">{event.city}</span>
              </div>
              <h2 style={{ fontSize: '1.75rem', lineHeight: 1.25, marginBottom: '4px' }}>
                {event.title}
              </h2>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                Organized by {event.organizerName || 'Verified Cultural Producer'}
              </div>
            </div>
          </div>
        </div>

        {/* Modal Body */}
        <div style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '24px' }}>
          {/* Venue & Date Summary Box */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
            gap: '16px',
            backgroundColor: 'var(--bg-canvas)',
            padding: '16px',
            borderRadius: 'var(--radius-sm)',
            border: '1px solid var(--border-subtle)'
          }}>
            <div>
              <div style={{ fontSize: '0.72rem', textTransform: 'uppercase', color: 'var(--text-muted)' }}>Date & Time</div>
              <div style={{ fontWeight: 600, color: 'var(--text-main)', marginTop: '2px' }}>{formattedDate}</div>
            </div>
            <div>
              <div style={{ fontSize: '0.72rem', textTransform: 'uppercase', color: 'var(--text-muted)' }}>Venue & Hall</div>
              <div style={{ fontWeight: 600, color: 'var(--text-main)', marginTop: '2px' }}>{event.venueName}</div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>{event.address}, {event.city}</div>
            </div>
          </div>

          {/* Synopsis */}
          <div>
            <h4 style={{ fontSize: '1rem', marginBottom: '8px', color: 'var(--text-main)' }}>Overview & Synopsis</h4>
            <p style={{ lineHeight: 1.7, fontSize: '0.92rem' }}>
              {event.description}
            </p>
          </div>

          {/* Cast & Performers Roster Section */}
          {event.performers && event.performers.length > 0 && (
            <div>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
                <h4 style={{ fontSize: '1rem', color: 'var(--text-main)' }}>Performers & Principal Cast</h4>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{event.performers.length} artists</span>
              </div>

              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
                gap: '14px'
              }}>
                {event.performers.map(performer => (
                  <div key={performer.id} style={{
                    backgroundColor: 'var(--bg-canvas)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: 'var(--radius-sm)',
                    padding: '12px',
                    display: 'flex',
                    gap: '12px'
                  }}>
                    <img 
                      src={performer.imageUrl} 
                      alt={performer.name} 
                      style={{
                        width: '52px',
                        height: '52px',
                        borderRadius: 'var(--radius-xs)',
                        objectFit: 'cover',
                        border: '1px solid var(--border-medium)',
                        flexShrink: 0
                      }}
                    />
                    <div style={{ overflow: 'hidden' }}>
                      <div style={{ fontWeight: 600, fontSize: '0.9rem', color: 'var(--text-main)' }}>
                        {performer.name}
                      </div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--accent-primary)', fontWeight: 500, marginTop: '1px' }}>
                        {performer.role}
                      </div>
                      <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '4px', lineHeight: 1.35, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                        {performer.bio}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Seating & Booking CTA Bar */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            paddingTop: '16px',
            borderTop: '1px solid var(--border-subtle)'
          }}>
            <div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Pricing from</div>
              <div style={{ fontSize: '1.4rem', fontWeight: 700, fontFamily: 'var(--font-heading)' }}>
                ₹{event.basePrice} <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 400 }}>/ seat</span>
              </div>
            </div>

            <div style={{ display: 'flex', gap: '12px' }}>
              <button className="btn btn-outline" onClick={onClose}>
                Back to Catalog
              </button>
              <button 
                className="btn btn-primary btn-lg" 
                onClick={() => onSelectSeats(event)}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M7 13h10" />
                  <path d="M5 21h14" />
                  <path d="M6 3h12a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z" />
                </svg>
                Select Seats & View Layout
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
