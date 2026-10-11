import React, { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getEventSeatLayout } from '../data/seatLayouts';

export default function SeatSelectionPage({ events = [], onConfirmSeats }) {
  const { id } = useParams();
  const navigate = useNavigate();

  const event = events.find(e => e.id === id) || events[0];

  // Dynamic Seat Layout & Pricing specific to this event & venue
  const seatLayout = getEventSeatLayout(event);
  const SEAT_ROWS = seatLayout.rows;

  // Selected seats state: array of { label: 'B4', row: 'B', number: 4, tier: 'VIP', price: 3500 }
  const [selectedSeats, setSelectedSeats] = useState([]);

  const handleToggleSeat = (rowItem, seatNum) => {
    if (rowItem.sold.includes(seatNum)) return; // cannot select sold seat

    const seatId = `${rowItem.row}${seatNum}`;
    const exists = selectedSeats.find(s => s.id === seatId);

    if (exists) {
      setSelectedSeats(selectedSeats.filter(s => s.id !== seatId));
    } else {
      setSelectedSeats([
        ...selectedSeats,
        {
          id: seatId,
          row: rowItem.row,
          number: seatNum,
          tier: rowItem.tier,
          price: rowItem.price,
          label: `Row ${rowItem.row} - Seat ${seatNum}`
        }
      ]);
    }
  };

  const totalPrice = selectedSeats.reduce((sum, s) => sum + s.price, 0);

  const handleProceed = () => {
    if (selectedSeats.length === 0) return;
    if (onConfirmSeats) {
      onConfirmSeats({
        event,
        seats: selectedSeats,
        totalPrice
      });
    }
    navigate('/checkout');
  };

  return (
    <div style={{ backgroundColor: 'var(--color-surface)', minHeight: '100vh', paddingBottom: '120px' }}>
      {/* 1. Header Information Bar */}
      <section style={{ backgroundColor: '#FFFFFF', borderBottom: '1px solid var(--color-border)', padding: '14px 0' }}>
        <div className="site-container" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Link to={`/events/${event.id}`} style={{ color: 'var(--color-text-secondary)', textDecoration: 'none', display: 'flex', alignItems: 'center' }}>
                <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>arrow_back</span>
              </Link>
              <h1 style={{ fontSize: '18px', fontWeight: 800, color: 'var(--color-text-main)', margin: 0 }}>
                {event.title}
              </h1>
            </div>
            <div style={{ fontSize: '13px', color: 'var(--color-text-secondary)', marginLeft: '28px', marginTop: '2px' }}>
              {event.venueName} • Sat, 14 Dec | 7:00 PM
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ fontSize: '12px', fontWeight: 600, color: 'var(--color-primary)' }}>
              10-Minute Hold Window Active
            </span>
          </div>
        </div>
      </section>

      {/* 2. Seating Plan Matrix */}
      <section className="site-container" style={{ paddingTop: '32px' }}>
        <div style={{
          backgroundColor: '#FFFFFF',
          borderRadius: 'var(--radius-xl)',
          border: '1px solid var(--color-border)',
          boxShadow: 'var(--shadow-subtle)',
          padding: '36px 20px',
          maxWidth: '920px',
          margin: '0 auto',
          textAlign: 'center'
        }}>
          {/* Event Capacity & Seating Stats Banner */}
          <div style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '12px',
            backgroundColor: '#F8FAFC',
            border: '1px solid #E2E8F0',
            borderRadius: 'var(--radius-full)',
            padding: '6px 16px',
            fontSize: '12px',
            color: 'var(--color-text-secondary)',
            marginBottom: '24px'
          }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: '#64748B' }} />
              <strong>{SEAT_ROWS.reduce((acc, r) => acc + r.sold.length, 0)} Booked (Gray)</strong>
            </span>
            <span>•</span>
            <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: '#16A34A' }} />
              <strong>{SEAT_ROWS.reduce((acc, r) => acc + (r.seats.length - r.sold.length), 0)} Available</strong>
            </span>
            <span>•</span>
            <span>Total Capacity: {SEAT_ROWS.reduce((acc, r) => acc + r.seats.length, 0)} Seats</span>
          </div>

          {/* Curved Stage Banner (BookMyShow Style) */}
          <div style={{ marginBottom: '40px' }}>
            <div style={{
              width: '75%',
              maxWidth: '480px',
              height: '8px',
              backgroundColor: 'var(--color-primary)',
              borderRadius: 'var(--radius-full)',
              margin: '0 auto 8px',
              boxShadow: '0 2px 8px rgba(248, 68, 100, 0.3)'
            }} />
            <span style={{
              fontSize: '12px',
              fontWeight: 800,
              letterSpacing: '0.12em',
              color: 'var(--color-primary)',
              textTransform: 'uppercase'
            }}>
              {seatLayout.stageLabel || 'STAGE THIS WAY'}
            </span>
          </div>

          {/* Seating Grid by Dynamic Tiers */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '28px', overflowX: 'auto', paddingBottom: '16px' }} className="no-scrollbar">
            {Array.from(new Set(SEAT_ROWS.map(r => r.tier))).map(tierName => {
              const tierRows = SEAT_ROWS.filter(r => r.tier === tierName);
              const tierPrice = tierRows[0]?.price;

              return (
                <div key={tierName} style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                  <div style={{
                    fontSize: '12px',
                    fontWeight: 700,
                    color: 'var(--color-text-secondary)',
                    letterSpacing: '0.04em',
                    textTransform: 'uppercase',
                    textAlign: 'left',
                    borderBottom: '1px solid var(--color-border-subtle)',
                    paddingBottom: '4px'
                  }}>
                    {tierName} — ₹{tierPrice?.toLocaleString()}
                  </div>

                  {tierRows.map(rowItem => (
                    <div key={rowItem.row} style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}>
                      <span style={{ width: '20px', fontSize: '12px', fontWeight: 700, color: 'var(--color-text-secondary)' }}>
                        {rowItem.row}
                      </span>

                      <div style={{ display: 'flex', gap: '6px' }}>
                        {rowItem.seats.map(num => {
                          const seatId = `${rowItem.row}${num}`;
                          const isSold = rowItem.sold.includes(num);
                          const isSelected = selectedSeats.some(s => s.id === seatId);

                          let bgColor = '#FFFFFF';
                          let borderColor = 'var(--color-border-strong)';
                          let textColor = 'var(--color-text-main)';

                          if (isSold) {
                            // Solid slate gray for booked/sold seats
                            bgColor = '#CBD5E1';
                            borderColor = '#94A3B8';
                            textColor = '#64748B';
                          } else if (isSelected) {
                            bgColor = '#16A34A';
                            borderColor = '#16A34A';
                            textColor = '#FFFFFF';
                          }

                          return (
                            <button
                              key={num}
                              disabled={isSold}
                              onClick={() => handleToggleSeat(rowItem, num)}
                              style={{
                                width: '28px',
                                height: '28px',
                                borderRadius: '4px',
                                border: `1px solid ${borderColor}`,
                                backgroundColor: bgColor,
                                color: textColor,
                                fontSize: '11px',
                                fontWeight: 700,
                                cursor: isSold ? 'not-allowed' : 'pointer',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                transition: 'all 120ms ease',
                                opacity: isSold ? 0.85 : 1
                              }}
                              title={isSold ? `Seat ${rowItem.row}${num} — Booked (Unavailable)` : `${rowItem.row}${num} (${tierName} - ₹${rowItem.price})`}
                            >
                              {isSold ? '✕' : (isSelected ? '✓' : num)}
                            </button>
                          );
                        })}
                      </div>
                    </div>
                  ))}
                </div>
              );
            })}
          </div>

          {/* Seat Status Legend */}
          <div style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            gap: '24px',
            marginTop: '32px',
            paddingTop: '20px',
            borderTop: '1px solid var(--color-border-subtle)',
            fontSize: '12px',
            color: 'var(--color-text-secondary)'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ width: '16px', height: '16px', border: '1px solid var(--color-border-strong)', backgroundColor: '#FFFFFF', borderRadius: '3px' }} />
              <span>Available</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ width: '16px', height: '16px', backgroundColor: '#16A34A', borderRadius: '3px', color: '#FFFFFF', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '10px', fontWeight: 800 }}>✓</span>
              <span>Selected</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ width: '16px', height: '16px', backgroundColor: '#CBD5E1', border: '1px solid #94A3B8', borderRadius: '3px', color: '#64748B', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '10px', fontWeight: 800 }}>✕</span>
              <span style={{ fontWeight: 600 }}>Booked / Sold (Gray)</span>
            </div>
          </div>
        </div>
      </section>

      {/* 3. Sticky Bottom Checkout Bar (BookMyShow Style) */}
      <div style={{
        position: 'fixed',
        bottom: 0,
        left: 0,
        right: 0,
        backgroundColor: '#FFFFFF',
        borderTop: '1px solid var(--color-border)',
        boxShadow: 'var(--shadow-float)',
        padding: '16px 0',
        zIndex: 40
      }}>
        <div className="site-container" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <div style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>
              {selectedSeats.length > 0
                ? `${selectedSeats.length} Seat${selectedSeats.length > 1 ? 's' : ''} Selected: ${selectedSeats.map(s => s.id).join(', ')}`
                : 'Select seats to continue'}
            </div>
            <div style={{ fontSize: '20px', fontWeight: 800, color: 'var(--color-text-main)' }}>
              ₹{totalPrice.toLocaleString()}
            </div>
          </div>

          <button
            disabled={selectedSeats.length === 0}
            onClick={handleProceed}
            className="btn btn-primary btn-lg"
            style={{
              opacity: selectedSeats.length === 0 ? 0.5 : 1,
              cursor: selectedSeats.length === 0 ? 'not-allowed' : 'pointer'
            }}
          >
            <span>Proceed to Checkout</span>
            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>arrow_forward</span>
          </button>
        </div>
      </div>
    </div>
  );
}
