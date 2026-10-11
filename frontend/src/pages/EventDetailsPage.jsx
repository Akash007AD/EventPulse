import React, { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';

export default function EventDetailsPage({ events = [] }) {
  const { id } = useParams();
  const navigate = useNavigate();

  const event = events.find(e => e.id === id) || events[0];

  // User review state
  const [reviews, setReviews] = useState([
    {
      id: 1,
      author: 'Rahul Sharma',
      rating: 5,
      date: '2 days ago',
      comment: 'An absolute masterpiece of acoustic staging! The 45-piece symphony with Arijit created goosebumps. Totally worth the VIP pass!'
    },
    {
      id: 2,
      author: 'Priya Iyer',
      rating: 5,
      date: '1 week ago',
      comment: 'The venue acoustic architecture at DY Patil Stadium was crystal clear. Seamless turnstile entry with the M-Ticket QR code.'
    }
  ]);
  const [newReviewAuthor, setNewReviewAuthor] = useState('');
  const [newRating, setNewRating] = useState(5);
  const [newComment, setNewComment] = useState('');
  const [showReviewForm, setShowReviewForm] = useState(false);

  // Check if user has auto calendar sync enabled
  const userPrefs = JSON.parse(localStorage.getItem('eventpulse_user_prefs') || '{}');
  const hasAutoCalendar = userPrefs.autoSyncCalendar;

  if (!event) {
    return (
      <div className="site-container" style={{ padding: '80px 20px', textAlign: 'center' }}>
        <h2>Event Not Found</h2>
        <button onClick={() => navigate('/')} className="btn btn-primary" style={{ marginTop: '16px' }}>
          Back to Events
        </button>
      </div>
    );
  }

  // Google Calendar URL generator
  const getGoogleCalendarUrl = () => {
    const title = encodeURIComponent(event.title);
    const details = encodeURIComponent(`${event.description}\n\nVenue: ${event.venueName}, ${event.venueAddress}`);
    const location = encodeURIComponent(`${event.venueName}, ${event.venueAddress}, ${event.city}`);
    const startIso = (event.startTime || '20261120T193000Z').replace(/[-:]/g, '').split('.')[0] + 'Z';
    const endIso = (event.endTime || '20261120T220000Z').replace(/[-:]/g, '').split('.')[0] + 'Z';
    return `https://calendar.google.com/calendar/render?action=TEMPLATE&text=${title}&dates=${startIso}/${endIso}&details=${details}&location=${location}`;
  };

  const handleAddReview = (e) => {
    e.preventDefault();
    if (!newReviewAuthor.trim() || !newComment.trim()) return;

    setReviews([
      {
        id: Date.now(),
        author: newReviewAuthor.trim(),
        rating: Number(newRating),
        date: 'Just now',
        comment: newComment.trim()
      },
      ...reviews
    ]);

    setNewReviewAuthor('');
    setNewComment('');
    setShowReviewForm(false);
  };

  return (
    <div style={{ backgroundColor: 'var(--color-surface)', minHeight: '100vh', paddingBottom: '80px' }}>
      {/* 1. Breadcrumb Bar */}
      <section style={{ backgroundColor: '#FFFFFF', borderBottom: '1px solid var(--color-border)', padding: '12px 0' }}>
        <div className="site-container" style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '13px', color: 'var(--color-text-secondary)' }}>
          <Link to="/" style={{ color: 'var(--color-text-secondary)', textDecoration: 'none' }}>Home</Link>
          <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>chevron_right</span>
          <Link to={`/?category=${event.category}`} style={{ color: 'var(--color-text-secondary)', textDecoration: 'none' }}>
            {event.categoryLabel || 'Live Events'}
          </Link>
          <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>chevron_right</span>
          <span style={{ color: 'var(--color-text-main)', fontWeight: 600 }}>{event.title}</span>
        </div>
      </section>

      {/* 2. Top Event Showcase Card */}
      <section className="site-container" style={{ paddingTop: '24px', paddingBottom: '32px' }}>
        <div style={{
          backgroundColor: '#FFFFFF',
          borderRadius: 'var(--radius-xl)',
          border: '1px solid var(--color-border)',
          boxShadow: 'var(--shadow-card)',
          padding: '24px',
          display: 'grid',
          gridTemplateColumns: 'minmax(260px, 320px) 1fr',
          gap: '32px',
          alignItems: 'start'
        }} className="details-hero-grid">
          {/* Left: 2:3 Vertical Event Poster with Rating */}
          <div style={{ width: '100%' }}>
            <div style={{
              position: 'relative',
              width: '100%',
              aspectRatio: '2 / 3',
              borderRadius: 'var(--radius-lg)',
              overflow: 'hidden',
              backgroundColor: 'var(--color-surface-container)',
              boxShadow: 'var(--shadow-card)'
            }}>
              <img
                src={event.posterUrl}
                alt={event.title}
                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
              />
              <div style={{
                position: 'absolute',
                inset: 'auto 0 0 0',
                background: 'linear-gradient(to top, rgba(26, 26, 36, 0.95) 0%, transparent 100%)',
                padding: '16px',
                color: '#FFFFFF'
              }}>
                <div style={{ display: 'flex', gap: '6px', marginBottom: '4px' }}>
                  <span style={{
                    fontSize: '10px',
                    fontWeight: 700,
                    backgroundColor: 'var(--color-primary)',
                    padding: '2px 8px',
                    borderRadius: 'var(--radius-full)'
                  }}>
                    LIVE IN CONCERT
                  </span>
                  <span style={{
                    fontSize: '10px',
                    fontWeight: 700,
                    backgroundColor: 'rgba(255, 255, 255, 0.2)',
                    backdropFilter: 'blur(4px)',
                    padding: '2px 8px',
                    borderRadius: 'var(--radius-full)'
                  }}>
                    SURROUND 7.1
                  </span>
                </div>
                <div style={{ fontSize: '13px', display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <span className="material-symbols-outlined" style={{ fontSize: '16px', color: '#FCD34D' }}>star</span>
                  <span style={{ fontWeight: 700 }}>4.9 / 5</span> ({reviews.length} audience reviews)
                </div>
              </div>
            </div>
          </div>

          {/* Right: Overview & Booking Actions */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {/* Category & Format Chips */}
            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
              <span className="badge-pill badge-crimson-tint">{event.categoryLabel}</span>
              <span className="badge-pill badge-neutral">{event.genre}</span>
              <span className="badge-pill badge-neutral">{event.duration} Duration</span>
              <span className="badge-pill badge-neutral">{event.ageRestriction}</span>
            </div>

            {/* Event Title */}
            <h1 style={{ fontSize: '30px', fontWeight: 800, color: 'var(--color-text-main)', lineHeight: 1.25 }}>
              {event.title}
            </h1>

            {/* Date, Time & Venue Chips */}
            <div style={{
              display: 'flex',
              flexDirection: 'column',
              gap: '10px',
              padding: '16px',
              backgroundColor: 'var(--color-surface)',
              borderRadius: 'var(--radius-lg)',
              border: '1px solid var(--color-border)'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '14px', color: 'var(--color-text-main)' }}>
                <span className="material-symbols-outlined" style={{ color: 'var(--color-primary)' }}>calendar_month</span>
                <span style={{ fontWeight: 600 }}>Sat, 14 Dec, 2026 | 07:00 PM</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '14px', color: 'var(--color-text-main)' }}>
                <span className="material-symbols-outlined" style={{ color: 'var(--color-primary)' }}>location_on</span>
                <div>
                  <span style={{ fontWeight: 600 }}>{event.venueName}</span>
                  <span style={{ color: 'var(--color-text-secondary)', marginLeft: '6px', fontSize: '13px' }}>({event.venueAddress})</span>
                </div>
              </div>
            </div>

            {/* Calendar Auto-Sync Notice */}
            {hasAutoCalendar && (
              <div style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '8px 12px',
                backgroundColor: '#F0FDF4',
                border: '1px solid #BBF7D0',
                borderRadius: 'var(--radius-md)',
                fontSize: '12px',
                color: '#166534'
              }}>
                <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>check_circle</span>
                <span>Auto-Calendar Sync Active: This show will be added automatically to your Google Calendar upon confirmation.</span>
              </div>
            )}

            {/* Price & Primary CTA */}
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              paddingTop: '8px',
              flexWrap: 'wrap',
              gap: '16px'
            }}>
              <div>
                <span style={{ fontSize: '12px', color: 'var(--color-text-secondary)', textTransform: 'uppercase' }}>Tickets From</span>
                <div style={{ fontSize: '24px', fontWeight: 800, color: 'var(--color-text-main)' }}>
                  ₹{event.basePrice?.toLocaleString()} <span style={{ fontSize: '13px', fontWeight: 400, color: 'var(--color-text-secondary)' }}>/ person</span>
                </div>
              </div>

              <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                <a
                  href={getGoogleCalendarUrl()}
                  target="_blank"
                  rel="noreferrer"
                  className="btn btn-secondary"
                  title="Add to Google Calendar"
                  style={{ borderRadius: 'var(--radius-full)' }}
                >
                  <span className="material-symbols-outlined" style={{ fontSize: '18px', color: '#2563EB' }}>calendar_add_on</span>
                  <span>Google Calendar</span>
                </a>

                <button
                  onClick={() => navigate(`/events/${event.id}/seats`)}
                  className="btn btn-primary btn-lg"
                  style={{ borderRadius: 'var(--radius-full)', fontWeight: 700 }}
                >
                  <span>Select Seats</span>
                  <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>arrow_forward</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 3. Event Details Body: About, Artists, Reviews */}
      <section className="site-container">
        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '30px' }} className="details-body-grid">
          {/* Left Column: Synopsis, Performers & Customer Reviews */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            {/* About */}
            <div style={{
              backgroundColor: '#FFFFFF',
              borderRadius: 'var(--radius-lg)',
              border: '1px solid var(--color-border)',
              padding: '24px'
            }}>
              <h2 style={{ fontSize: '18px', fontWeight: 800, marginBottom: '12px', color: 'var(--color-text-main)' }}>
                About The Event
              </h2>
              <p style={{ fontSize: '14px', lineHeight: 1.7, color: 'var(--color-text-secondary)' }}>
                {event.description}
              </p>
            </div>

            {/* Artists & Performers */}
            {event.performers && event.performers.length > 0 && (
              <div style={{
                backgroundColor: '#FFFFFF',
                borderRadius: 'var(--radius-lg)',
                border: '1px solid var(--color-border)',
                padding: '24px'
              }}>
                <h2 style={{ fontSize: '18px', fontWeight: 800, marginBottom: '16px', color: 'var(--color-text-main)' }}>
                  Artist Lineup &amp; Performers
                </h2>
                <div style={{ display: 'flex', gap: '20px', flexWrap: 'wrap' }}>
                  {event.performers.map(p => (
                    <div key={p.id} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', maxWidth: '120px' }}>
                      <img
                        src={p.imageUrl}
                        alt={p.name}
                        style={{
                          width: '74px',
                          height: '74px',
                          borderRadius: '50%',
                          objectFit: 'cover',
                          marginBottom: '8px',
                          border: '2px solid var(--color-surface-dim)'
                        }}
                      />
                      <div style={{ fontWeight: 700, fontSize: '13px', color: 'var(--color-text-main)' }}>{p.name}</div>
                      <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)' }}>{p.role}</div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Audience Reviews & Feedback (User Request) */}
            <div style={{
              backgroundColor: '#FFFFFF',
              borderRadius: 'var(--radius-lg)',
              border: '1px solid var(--color-border)',
              padding: '24px'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                <div>
                  <h2 style={{ fontSize: '18px', fontWeight: 800, color: 'var(--color-text-main)' }}>
                    Audience Reviews &amp; Ratings
                  </h2>
                  <p style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>
                    Verified feedback from attendees who experienced this show
                  </p>
                </div>
                <button
                  onClick={() => setShowReviewForm(!showReviewForm)}
                  className="btn btn-outline-primary btn-sm"
                >
                  <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>rate_review</span>
                  <span>Write a Review</span>
                </button>
              </div>

              {/* Review Submission Form */}
              {showReviewForm && (
                <form onSubmit={handleAddReview} style={{
                  backgroundColor: 'var(--color-surface)',
                  padding: '16px',
                  borderRadius: 'var(--radius-md)',
                  marginBottom: '20px',
                  border: '1px solid var(--color-border)'
                }}>
                  <div style={{ marginBottom: '12px' }}>
                    <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, marginBottom: '4px' }}>Your Name</label>
                    <input
                      type="text"
                      placeholder="e.g. Ananya Das"
                      value={newReviewAuthor}
                      onChange={(e) => setNewReviewAuthor(e.target.value)}
                      style={{ width: '100%', padding: '8px 12px', borderRadius: '4px', border: '1px solid var(--color-border-strong)', fontSize: '13px' }}
                    />
                  </div>
                  <div style={{ marginBottom: '12px' }}>
                    <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, marginBottom: '4px' }}>Rating</label>
                    <select
                      value={newRating}
                      onChange={(e) => setNewRating(e.target.value)}
                      style={{ padding: '8px 12px', borderRadius: '4px', border: '1px solid var(--color-border-strong)', fontSize: '13px' }}
                    >
                      <option value="5">★★★★★ 5 Stars (Extraordinary)</option>
                      <option value="4">★★★★☆ 4 Stars (Very Good)</option>
                      <option value="3">★★★☆☆ 3 Stars (Average)</option>
                    </select>
                  </div>
                  <div style={{ marginBottom: '12px' }}>
                    <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, marginBottom: '4px' }}>Your Feedback</label>
                    <textarea
                      rows={3}
                      placeholder="Share what you liked about the music, seating, and performance..."
                      value={newComment}
                      onChange={(e) => setNewComment(e.target.value)}
                      style={{ width: '100%', padding: '8px 12px', borderRadius: '4px', border: '1px solid var(--color-border-strong)', fontSize: '13px' }}
                    />
                  </div>
                  <button type="submit" className="btn btn-primary btn-sm">
                    Submit Review
                  </button>
                </form>
              )}

              {/* Reviews List */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                {reviews.map(r => (
                  <div key={r.id} style={{
                    padding: '16px',
                    backgroundColor: 'var(--color-surface)',
                    borderRadius: 'var(--radius-md)',
                    border: '1px solid var(--color-border-subtle)'
                  }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                      <span style={{ fontWeight: 700, fontSize: '13px', color: 'var(--color-text-main)' }}>{r.author}</span>
                      <span style={{ fontSize: '12px', color: '#F59E0B' }}>
                        {'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)}
                      </span>
                    </div>
                    <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', lineHeight: 1.5 }}>
                      "{r.comment}"
                    </p>
                    <span style={{ fontSize: '11px', color: 'var(--color-text-muted)', display: 'block', marginTop: '6px' }}>
                      {r.date}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Right Column: Venue Guidelines & Facilities */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            <div style={{
              backgroundColor: '#FFFFFF',
              borderRadius: 'var(--radius-lg)',
              border: '1px solid var(--color-border)',
              padding: '24px'
            }}>
              <h3 style={{ fontSize: '16px', fontWeight: 800, marginBottom: '12px', color: 'var(--color-text-main)' }}>
                Venue Facilities
              </h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '13px', color: 'var(--color-text-secondary)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span className="material-symbols-outlined" style={{ color: 'var(--color-primary)' }}>local_parking</span>
                  <span>Designated Car &amp; Bike Parking</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span className="material-symbols-outlined" style={{ color: 'var(--color-primary)' }}>accessible</span>
                  <span>Wheelchair Accessible Ramp &amp; Elevator</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span className="material-symbols-outlined" style={{ color: 'var(--color-primary)' }}>restaurant</span>
                  <span>F&amp;B Food Court on Premise</span>
                </div>
              </div>
            </div>

            <div style={{
              backgroundColor: '#FFFFFF',
              borderRadius: 'var(--radius-lg)',
              border: '1px solid var(--color-border)',
              padding: '24px'
            }}>
              <h3 style={{ fontSize: '16px', fontWeight: 800, marginBottom: '12px', color: 'var(--color-text-main)' }}>
                Important Guidelines
              </h3>
              <ul style={{ paddingLeft: '18px', fontSize: '13px', color: 'var(--color-text-secondary)', display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <li>Gates open 60 minutes prior to showtime.</li>
                <li>M-Ticket on smartphone with QR code mandatory for admission.</li>
                <li>Outside food, drinks, and professional cameras are prohibited.</li>
              </ul>
            </div>
          </div>
        </div>
      </section>

      <style>{`
        @media (max-width: 860px) {
          .details-hero-grid {
            grid-template-columns: 1fr !important;
          }
          .details-body-grid {
            grid-template-columns: 1fr !important;
          }
        }
      `}</style>
    </div>
  );
}
