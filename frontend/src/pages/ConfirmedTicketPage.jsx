import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { UserService } from '../services/userService';

export default function ConfirmedTicketPage({ confirmedPass }) {
  const [isEmailResent, setIsEmailResent] = useState(false);

  const pass = confirmedPass || {
    bookingId: "EP-BK-894210",
    event: {
      title: "Acoustic Nights with Arijit Singh Live",
      venueName: "DY Patil Stadium",
      venueAddress: "Sector 7, Nerul, Navi Mumbai",
      startTime: "2026-12-14T19:00:00Z"
    },
    seats: [
      { id: "B4", row: "B", number: 4, tier: "LOUNGE / VIP" },
      { id: "B5", row: "B", number: 5, tier: "LOUNGE / VIP" }
    ],
    totalPaid: 7560,
    customerEmail: "customer@example.com"
  };

  const targetEmail = pass.customerEmail || 'customer@example.com';

  const handleResendEmail = () => {
    UserService.sendTicketEmail({
      email: targetEmail,
      bookingId: pass.bookingId,
      eventTitle: pass.event.title,
      seats: pass.seats,
      totalPaid: pass.totalPaid
    });
    setIsEmailResent(true);
    setTimeout(() => setIsEmailResent(false), 3000);
  };

  const getGoogleCalendarUrl = () => {
    const title = encodeURIComponent(pass.event.title);
    const details = encodeURIComponent(`Booking ID: ${pass.bookingId}\nSeats: ${pass.seats.map(s => s.id).join(', ')}\nVenue: ${pass.event.venueName}`);
    const location = encodeURIComponent(`${pass.event.venueName}, ${pass.event.venueAddress}`);
    const startIso = "20261214T190000Z";
    const endIso = "20261214T221500Z";
    return `https://calendar.google.com/calendar/render?action=TEMPLATE&text=${title}&dates=${startIso}/${endIso}&details=${details}&location=${location}`;
  };

  return (
    <div style={{ backgroundColor: 'var(--color-surface)', minHeight: '100vh', padding: '40px 0 80px' }}>
      <div className="site-container" style={{ maxWidth: '640px', margin: '0 auto' }}>
        {/* Success Confirmation Header */}
        <div style={{ textAlign: 'center', marginBottom: '28px' }}>
          <div style={{
            width: '64px',
            height: '64px',
            borderRadius: '50%',
            backgroundColor: '#DCFCE7',
            color: '#16A34A',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            margin: '0 auto 16px',
            boxShadow: '0 4px 12px rgba(22, 163, 74, 0.2)'
          }}>
            <span className="material-symbols-outlined" style={{ fontSize: '36px' }}>check_circle</span>
          </div>
          <h1 style={{ fontSize: '26px', fontWeight: 800, color: 'var(--color-text-main)', marginBottom: '4px' }}>
            Booking Confirmed!
          </h1>
          <p style={{ fontSize: '14px', color: 'var(--color-text-secondary)', margin: '4px 0' }}>
            Booking ID: <strong style={{ color: 'var(--color-text-main)' }}>{pass.bookingId}</strong>
          </p>
          {pass.razorpayPaymentId && (
            <div style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', padding: '4px 10px', backgroundColor: '#F0FDF4', border: '1px solid #BBF7D0', borderRadius: '12px', fontSize: '12px', color: '#166534', marginTop: '6px' }}>
              <span className="material-symbols-outlined" style={{ fontSize: '14px', color: '#16A34A' }}>verified</span>
              <span>Razorpay Ref: <strong>{pass.razorpayPaymentId}</strong></span>
            </div>
          )}

          {/* Email Delivery Confirmation Card */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            backgroundColor: '#F0FDF4',
            border: '1px solid #BBF7D0',
            borderRadius: 'var(--radius-lg)',
            padding: '12px 18px',
            marginTop: '14px',
            textAlign: 'left'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <span className="material-symbols-outlined" style={{ fontSize: '24px', color: '#16A34A' }}>mark_email_read</span>
              <div>
                <div style={{ fontSize: '13px', fontWeight: 800, color: '#166534' }}>
                  M-Ticket Dispatched to Registered Email
                </div>
                <div style={{ fontSize: '12px', color: '#15803D' }}>
                  Sent to: <strong>{targetEmail}</strong> • Official QR Pass &amp; Tax Receipt
                </div>
              </div>
            </div>
            <button
              onClick={handleResendEmail}
              className="btn btn-ghost"
              style={{ fontSize: '11px', color: '#15803D', fontWeight: 700, border: '1px solid #86EFAC', padding: '4px 10px', borderRadius: '4px', backgroundColor: '#FFFFFF' }}
            >
              {isEmailResent ? '✓ Resent!' : 'Resend Email'}
            </button>
          </div>
        </div>

        {/* M-Ticket Card (Ticket Cutout Style) */}
        <div style={{
          backgroundColor: '#FFFFFF',
          borderRadius: 'var(--radius-xl)',
          border: '1px solid var(--color-border)',
          boxShadow: 'var(--shadow-card)',
          overflow: 'hidden',
          marginBottom: '24px'
        }}>
          {/* Top Ticket Header */}
          <div style={{ padding: '24px 28px', borderBottom: '2px dashed var(--color-border)', position: 'relative' }}>
            <span className="badge-pill badge-crimson" style={{ marginBottom: '10px' }}>
              OFFICIAL M-TICKET PASS
            </span>
            <h2 style={{ fontSize: '22px', fontWeight: 800, color: 'var(--color-text-main)', marginBottom: '8px' }}>
              {pass.event.title}
            </h2>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: 'var(--color-text-secondary)' }}>
              <span className="material-symbols-outlined" style={{ fontSize: '16px', color: 'var(--color-primary)' }}>calendar_month</span>
              <span>Sat, 14 Dec, 2026 | 07:00 PM</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: 'var(--color-text-secondary)', marginTop: '4px' }}>
              <span className="material-symbols-outlined" style={{ fontSize: '16px', color: 'var(--color-primary)' }}>location_on</span>
              <span>{pass.event.venueName}, Gate 3</span>
            </div>
          </div>

          {/* Middle Seating & QR Block */}
          <div style={{ padding: '24px 28px', display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center' }}>
            {/* Seats Assigned */}
            <div style={{ marginBottom: '20px' }}>
              <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--color-text-secondary)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
                RESERVED SEAT ASSIGNMENT
              </span>
              <div style={{ display: 'flex', gap: '8px', justifyContent: 'center', marginTop: '6px' }}>
                {pass.seats.map(s => (
                  <span key={s.id} style={{
                    padding: '8px 16px',
                    borderRadius: 'var(--radius-md)',
                    backgroundColor: 'var(--color-surface)',
                    border: '1px solid var(--color-border)',
                    fontSize: '15px',
                    fontWeight: 800,
                    color: 'var(--color-primary)'
                  }}>
                    {s.row}{s.number} ({s.tier})
                  </span>
                ))}
              </div>
            </div>

            {/* Entry QR Code */}
            <div style={{
              padding: '16px',
              backgroundColor: '#FFFFFF',
              borderRadius: 'var(--radius-lg)',
              border: '2px solid var(--color-border)',
              marginBottom: '12px',
              boxShadow: 'var(--shadow-subtle)'
            }}>
              <svg width="180" height="180" viewBox="0 0 100 100" fill="#1A1A24">
                {/* Clean QR code simulation matrix */}
                <rect width="100" height="100" fill="#FFFFFF" />
                <rect x="10" y="10" width="24" height="24" fill="#1A1A24" />
                <rect x="14" y="14" width="16" height="16" fill="#FFFFFF" />
                <rect x="18" y="18" width="8" height="8" fill="#1A1A24" />
                <rect x="66" y="10" width="24" height="24" fill="#1A1A24" />
                <rect x="70" y="14" width="16" height="16" fill="#FFFFFF" />
                <rect x="74" y="18" width="8" height="8" fill="#1A1A24" />
                <rect x="10" y="66" width="24" height="24" fill="#1A1A24" />
                <rect x="14" y="70" width="16" height="16" fill="#FFFFFF" />
                <rect x="18" y="74" width="8" height="8" fill="#1A1A24" />
                {/* Inner Data Cells */}
                <rect x="42" y="12" width="6" height="6" fill="#F84464" />
                <rect x="52" y="12" width="6" height="6" fill="#1A1A24" />
                <rect x="42" y="24" width="8" height="8" fill="#1A1A24" />
                <rect x="12" y="44" width="8" height="8" fill="#1A1A24" />
                <rect x="26" y="44" width="8" height="8" fill="#1A1A24" />
                <rect x="44" y="44" width="12" height="12" fill="#F84464" />
                <rect x="64" y="44" width="8" height="8" fill="#1A1A24" />
                <rect x="80" y="44" width="8" height="8" fill="#1A1A24" />
                <rect x="42" y="66" width="8" height="8" fill="#1A1A24" />
                <rect x="66" y="66" width="6" height="6" fill="#1A1A24" />
                <rect x="80" y="74" width="8" height="8" fill="#1A1A24" />
                <rect x="44" y="80" width="8" height="8" fill="#1A1A24" />
                <rect x="64" y="82" width="10" height="10" fill="#F84464" />
              </svg>
            </div>

            <p style={{ fontSize: '12px', color: 'var(--color-text-secondary)', maxWidth: '320px', margin: '0 0 10px' }}>
              Present this verified digital QR pass at the entrance gate turnstile for admission.
            </p>

            {/* Payment Summary with GST on Tickets */}
            <div style={{
              width: '100%',
              padding: '12px 16px',
              backgroundColor: 'var(--color-surface)',
              borderRadius: 'var(--radius-md)',
              border: '1px solid var(--color-border)',
              fontSize: '12px',
              textAlign: 'left'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
                <span style={{ color: 'var(--color-text-secondary)' }}>Ticket Subtotal:</span>
                <span style={{ fontWeight: 600 }}>₹{(pass.ticketSubtotal || pass.totalPaid).toLocaleString()}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
                <span style={{ color: 'var(--color-text-secondary)' }}>GST (2.5% CGST + 2.5% SGST on Ticket Price):</span>
                <span style={{ fontWeight: 600 }}>₹{((pass.cgst || 0) + (pass.sgst || 0)).toFixed(2)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
                <span style={{ color: 'var(--color-text-secondary)' }}>Platform Convenience Fee:</span>
                <span style={{ fontWeight: 600 }}>₹{(pass.platformFee || 25).toFixed(2)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '1px solid var(--color-border)', paddingTop: '6px', fontWeight: 800, fontSize: '13px' }}>
                <span>Total Paid:</span>
                <span style={{ color: 'var(--color-primary)' }}>₹{pass.totalPaid.toLocaleString()}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Calendar Sync Status & Actions */}
        {(() => {
          let hasCalendarPermission = true;
          try {
            const prefs = JSON.parse(localStorage.getItem('eventpulse_user_prefs') || '{}');
            if (prefs.autoSyncCalendar !== undefined) {
              hasCalendarPermission = prefs.autoSyncCalendar;
            }
          } catch {
            hasCalendarPermission = true;
          }

          return (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {hasCalendarPermission ? (
                <div style={{
                  padding: '12px 16px',
                  backgroundColor: '#EFF6FF',
                  border: '1px solid #BFDBFE',
                  borderRadius: 'var(--radius-md)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  gap: '12px'
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span className="material-symbols-outlined" style={{ color: '#2563EB', fontSize: '20px' }}>event_available</span>
                    <span style={{ fontSize: '13px', color: '#1E40AF', fontWeight: 600 }}>
                      Direct Google Calendar Sync Enabled (Permission granted at signup)
                    </span>
                  </div>
                  <a
                    href={getGoogleCalendarUrl()}
                    target="_blank"
                    rel="noreferrer"
                    style={{ fontSize: '12px', fontWeight: 700, color: '#2563EB', textDecoration: 'underline', whiteSpace: 'nowrap' }}
                  >
                    Open in Calendar &rarr;
                  </a>
                </div>
              ) : (
                <a
                  href={getGoogleCalendarUrl()}
                  target="_blank"
                  rel="noreferrer"
                  className="btn btn-secondary"
                  style={{ width: '100%', justifyContent: 'center' }}
                >
                  <span className="material-symbols-outlined" style={{ color: '#2563EB', fontSize: '18px' }}>calendar_add_on</span>
                  <span>Sync Directly to Google Calendar</span>
                </a>
              )}

              <Link
                to="/"
                className="btn btn-primary"
                style={{ width: '100%', justifyContent: 'center' }}
              >
                Explore More Live Events
              </Link>
            </div>
          );
        })()}
      </div>
    </div>
  );
}
