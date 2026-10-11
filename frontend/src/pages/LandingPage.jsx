import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import EventCard from '../components/EventCard';

export default function LandingPage({ events = [] }) {
  const navigate = useNavigate();
  const location = useLocation();

  const searchParams = new URLSearchParams(location.search);
  const activeCategory = searchParams.get('category') || 'ALL';

  // Filter events by category and ensure expired events are removed from public consumer discovery
  const publicEvents = events.filter(e => {
    // 1. Expiry check: remove events whose end time has passed
    const now = new Date();
    const eventEnd = new Date(e.endTime || e.startTime);
    if (eventEnd < now) return false;

    // 2. Category check
    if (activeCategory === 'ALL') return true;
    return e.category === activeCategory;
  });

  const featured = publicEvents[0] || events[0] || null;
  const concerts = publicEvents.filter(e => e.category === 'CONCERT');
  const comedyAndPlays = publicEvents.filter(e => e.category === 'COMEDY' || e.category === 'THEATRE');

  return (
    <div style={{ backgroundColor: 'var(--color-surface)', minHeight: '100vh', paddingBottom: '60px' }}>
      {/* 1. Featured Promotional Hero Banner Carousel (BookMyShow Style) */}
      {featured && (
        <section className="site-container" style={{ paddingTop: '24px', paddingBottom: '28px' }}>
          <div style={{
            backgroundColor: '#191C1D',
            borderRadius: 'var(--radius-xl)',
            overflow: 'hidden',
            boxShadow: 'var(--shadow-card)'
          }}>
            {/* Banner Media Frame with Scrim */}
            <div style={{ position: 'relative', width: '100%', height: '240px' }} className="hero-banner-image">
              <img
                src={featured.bannerUrl}
                alt={featured.title}
                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
              />
              <div style={{
                position: 'absolute',
                inset: 0,
                background: 'linear-gradient(to top, #191C1D 0%, rgba(25, 28, 29, 0.4) 40%, transparent 100%)'
              }} />

              {/* Selling Fast Live Badge */}
              <div style={{
                position: 'absolute',
                top: '16px',
                left: '16px',
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                backgroundColor: 'var(--color-primary)',
                color: '#FFFFFF',
                padding: '4px 10px',
                borderRadius: 'var(--radius-full)',
                fontSize: '11px',
                fontWeight: 700,
                letterSpacing: '0.04em'
              }}>
                <span style={{
                  width: '6px',
                  height: '6px',
                  borderRadius: '50%',
                  backgroundColor: '#FFFFFF'
                }} />
                <span>SELLING FAST</span>
              </div>
            </div>

            {/* Banner Details Card */}
            <div style={{
              backgroundColor: '#FFFFFF',
              padding: '16px 20px',
              display: 'flex',
              flexDirection: 'column',
              gap: '6px'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{
                  fontSize: '11px',
                  fontWeight: 700,
                  color: 'var(--color-primary)',
                  letterSpacing: '0.04em',
                  textTransform: 'uppercase'
                }}>
                  Exclusive Live Tour
                </span>
                <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--color-text-main)' }}>
                  ₹{featured.basePrice?.toLocaleString()} onwards
                </span>
              </div>

              <h2 style={{
                fontSize: '22px',
                fontWeight: 800,
                color: 'var(--color-text-main)',
                lineHeight: 1.25,
                margin: '2px 0'
              }}>
                {featured.title}
              </h2>

              <div style={{
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                fontSize: '13px',
                color: 'var(--color-text-secondary)',
                flexWrap: 'wrap'
              }}>
                <span className="material-symbols-outlined" style={{ fontSize: '16px', color: 'var(--color-primary)' }}>
                  calendar_month
                </span>
                <span>Sat, 14 Dec | 7:00 PM</span>
                <span>•</span>
                <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>
                  stadium
                </span>
                <span>{featured.venueName}, {featured.city}</span>
              </div>

              {/* Bottom Carousel Controls & Action Button */}
              <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                marginTop: '12px',
                paddingTop: '10px',
                borderTop: '1px solid var(--color-border-subtle)'
              }}>
                {/* Carousel Dots */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <span style={{ width: '20px', height: '6px', borderRadius: 'var(--radius-full)', backgroundColor: 'var(--color-primary)' }} />
                  <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'var(--color-surface-dim)' }} />
                  <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'var(--color-surface-dim)' }} />
                </div>

                <button
                  onClick={() => navigate(`/events/${featured.id}`)}
                  className="btn btn-primary"
                  style={{ padding: '8px 24px' }}
                >
                  <span>Book Now</span>
                  <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>arrow_forward</span>
                </button>
              </div>
            </div>
          </div>
        </section>
      )}

      {/* 2. Recommended Events Section (Live Upcoming Shows) */}
      <section className="site-container" style={{ paddingBottom: '36px' }}>
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-end',
          marginBottom: '16px'
        }}>
          <div>
            <h2 style={{ fontSize: '20px', fontWeight: 800, color: 'var(--color-text-main)', marginBottom: '2px' }}>
              Recommended Events
            </h2>
            <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>
              Discover trending live sensations in Mumbai
            </p>
          </div>
          <button
            onClick={() => navigate('/')}
            style={{
              background: 'none',
              border: 'none',
              color: 'var(--color-primary)',
              fontWeight: 700,
              fontSize: '13px',
              cursor: 'pointer',
              display: 'inline-flex',
              alignItems: 'center',
              gap: '2px'
            }}
          >
            <span>See All</span>
            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>chevron_right</span>
          </button>
        </div>

        {/* Responsive Grid */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
          gap: '20px'
        }}>
          {publicEvents.map(event => (
            <EventCard key={event.id} event={event} />
          ))}
        </div>
      </section>

      {/* 3. Live Comedy & Theatre Spotlight Section */}
      {comedyAndPlays.length > 0 && (
        <section className="site-container" style={{ paddingBottom: '36px' }}>
          <div style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'flex-end',
            marginBottom: '16px'
          }}>
            <div>
              <h2 style={{ fontSize: '20px', fontWeight: 800, color: 'var(--color-text-main)', marginBottom: '2px' }}>
                The Best of Live Comedy &amp; Plays
              </h2>
              <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>
                Stand-up specials and intimate theatre stages
              </p>
            </div>
          </div>

          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
            gap: '20px'
          }}>
            {comedyAndPlays.map(event => (
              <EventCard key={event.id} event={event} />
            ))}
          </div>
        </section>
      )}

      <style>{`
        @media (min-width: 768px) {
          .hero-banner-image {
            height: 320px !important;
          }
        }
      `}</style>
    </div>
  );
}
