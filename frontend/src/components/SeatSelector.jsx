import React, { useState } from 'react';

export default function SeatSelector({ event, onBack, onProceedToCheckout }) {
  const [selectedSeats, setSelectedSeats] = useState([]); // array of { rowLetter, number, price, tier, label }

  if (!event || !event.seatingSchema) {
    return (
      <div className="container" style={{ padding: '40px 20px', textAlign: 'center' }}>
        <p>No seating schema available for this event.</p>
        <button className="btn btn-outline" onClick={onBack} style={{ marginTop: '16px' }}>Back</button>
      </div>
    );
  }

  const { rows, stageLabel } = event.seatingSchema;

  const toggleSeat = (row, seat) => {
    if (seat.status === 'RESERVED') return;

    const seatLabel = `Row ${row.rowLetter}, Seat ${seat.number}`;
    const exists = selectedSeats.some(s => s.label === seatLabel);

    if (exists) {
      setSelectedSeats(selectedSeats.filter(s => s.label !== seatLabel));
    } else {
      setSelectedSeats([
        ...selectedSeats,
        {
          rowLetter: row.rowLetter,
          number: seat.number,
          price: row.price,
          tier: row.tier,
          label: seatLabel
        }
      ]);
    }
  };

  const isSeatSelected = (rowLetter, seatNumber) => {
    return selectedSeats.some(s => s.rowLetter === rowLetter && s.number === seatNumber);
  };

  const totalPrice = selectedSeats.reduce((acc, curr) => acc + curr.price, 0);

  return (
    <div style={{ backgroundColor: 'var(--bg-canvas)', minHeight: 'calc(100vh - 68px)', padding: '24px 0 100px 0' }}>
      <div className="container">
        {/* Top Breadcrumb & Event Bar */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          marginBottom: '20px',
          borderBottom: '1px solid var(--border-subtle)',
          paddingBottom: '16px',
          flexWrap: 'wrap',
          gap: '12px'
        }}>
          <div>
            <button 
              className="btn btn-sm btn-outline" 
              onClick={onBack}
              style={{ marginBottom: '8px' }}
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <line x1="19" y1="12" x2="5" y2="12" />
                <polyline points="12 19 5 12 12 5" />
              </svg>
              Back to Event Details
            </button>
            <h2 style={{ fontSize: '1.45rem' }}>{event.title}</h2>
            <div style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
              {event.venueName} • {event.city}
            </div>
          </div>

          {/* Seat Status Legend */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '16px',
            backgroundColor: 'var(--bg-card)',
            padding: '10px 16px',
            borderRadius: 'var(--radius-sm)',
            border: '1px solid var(--border-subtle)'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.78rem' }}>
              <div style={{ width: '14px', height: '14px', backgroundColor: '#334155', borderRadius: '2px' }} />
              <span>Available</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.78rem' }}>
              <div style={{ width: '14px', height: '14px', backgroundColor: 'var(--accent-primary)', borderRadius: '2px' }} />
              <span>Selected</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.78rem' }}>
              <div style={{ width: '14px', height: '14px', backgroundColor: '#1E293B', border: '1px solid #334155', borderRadius: '2px' }} />
              <span style={{ color: 'var(--text-muted)' }}>Reserved</span>
            </div>
          </div>
        </div>

        {/* Layout Container: Seating Map + Sticky Summary */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'minmax(0, 1fr) 340px',
          gap: '24px',
          alignItems: 'start'
        }}>
          {/* Main Seating Canvas */}
          <div style={{
            backgroundColor: 'var(--bg-card)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 'var(--radius-md)',
            padding: '24px',
            overflowX: 'auto',
            textAlign: 'center'
          }}>
            {/* Stage Indicator Bar */}
            <div style={{
              maxWidth: '480px',
              margin: '0 auto 40px auto',
              padding: '10px 20px',
              backgroundColor: '#1E293B',
              border: '1px solid var(--border-medium)',
              borderRadius: 'var(--radius-xs)',
              color: 'var(--text-secondary)',
              fontSize: '0.78rem',
              letterSpacing: '0.15em',
              fontWeight: 700,
              boxShadow: '0 4px 12px rgba(0, 0, 0, 0.4)'
            }}>
              {stageLabel || "STAGE / PERFORMANCE AREA"}
            </div>

            {/* Seating Grid */}
            <div style={{
              display: 'inline-flex',
              flexDirection: 'column',
              gap: '12px',
              padding: '10px',
              minWidth: '320px'
            }}>
              {rows.map((row) => (
                <div key={row.rowLetter} style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                  {/* Row Letter Identifier */}
                  <div style={{
                    width: '32px',
                    fontFamily: 'var(--font-heading)',
                    fontWeight: 700,
                    fontSize: '0.9rem',
                    color: 'var(--text-secondary)',
                    textAlign: 'right'
                  }}>
                    {row.rowLetter}
                  </div>

                  {/* Seat buttons */}
                  <div style={{ display: 'flex', gap: '8px' }}>
                    {row.seats.map((seat) => {
                      const selected = isSeatSelected(row.rowLetter, seat.number);
                      const isReserved = seat.status === 'RESERVED';

                      let bgColor = '#334155';
                      let textColor = '#F8FAFC';
                      let borderColor = '#475569';

                      if (isReserved) {
                        bgColor = '#111827';
                        borderColor = '#1E293B';
                        textColor = '#475569';
                      } else if (selected) {
                        bgColor = 'var(--accent-primary)';
                        borderColor = 'var(--accent-primary)';
                        textColor = '#070A10';
                      }

                      return (
                        <button
                          key={seat.number}
                          disabled={isReserved}
                          onClick={() => toggleSeat(row, seat)}
                          title={`Row ${row.rowLetter}, Seat ${seat.number} • ${row.tier} (₹${row.price})`}
                          style={{
                            width: '38px',
                            height: '38px',
                            backgroundColor: bgColor,
                            border: `1px solid ${borderColor}`,
                            borderRadius: 'var(--radius-xs)',
                            color: textColor,
                            fontSize: '0.75rem',
                            fontWeight: 700,
                            cursor: isReserved ? 'not-allowed' : 'pointer',
                            transition: 'all 0.15s ease',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            userSelect: 'none'
                          }}
                        >
                          {seat.number}
                        </button>
                      );
                    })}
                  </div>

                  {/* Row Tier Badge */}
                  <div style={{ marginLeft: '12px', width: '80px', textAlign: 'left' }}>
                    <span className="badge" style={{ fontSize: '0.68rem' }}>
                      ₹{row.price}
                    </span>
                  </div>
                </div>
              ))}
            </div>

            <div style={{ marginTop: '30px', fontSize: '0.78rem', color: 'var(--text-muted)' }}>
              Scroll horizontally if viewing on smaller mobile devices. Click any seat to reserve.
            </div>
          </div>

          {/* Desktop Summary Sidebar */}
          <div style={{
            backgroundColor: 'var(--bg-card)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 'var(--radius-md)',
            padding: '24px',
            position: 'sticky',
            top: '88px'
          }}>
            <h3 style={{ fontSize: '1.15rem', marginBottom: '16px' }}>
              Selected Seats ({selectedSeats.length})
            </h3>

            {selectedSeats.length === 0 ? (
              <div style={{
                padding: '28px 16px',
                textAlign: 'center',
                backgroundColor: 'var(--bg-canvas)',
                border: '1px dashed var(--border-medium)',
                borderRadius: 'var(--radius-sm)',
                color: 'var(--text-muted)',
                fontSize: '0.85rem'
              }}>
                No seats selected yet.<br/>Click seats on the layout to begin.
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginBottom: '20px' }}>
                {selectedSeats.map(seat => (
                  <div key={seat.label} style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    backgroundColor: 'var(--bg-canvas)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: 'var(--radius-xs)',
                    padding: '8px 12px'
                  }}>
                    <div>
                      <div style={{ fontWeight: 600, fontSize: '0.85rem', color: 'var(--text-main)' }}>
                        {seat.label}
                      </div>
                      <div style={{ fontSize: '0.7rem', color: 'var(--accent-primary)' }}>
                        {seat.tier} Tier
                      </div>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span style={{ fontWeight: 600, fontSize: '0.9rem' }}>₹{seat.price}</span>
                      <button 
                        onClick={() => setSelectedSeats(selectedSeats.filter(s => s.label !== seat.label))}
                        style={{
                          background: 'none',
                          border: 'none',
                          color: 'var(--text-muted)',
                          cursor: 'pointer',
                          padding: '2px',
                          display: 'flex'
                        }}
                        title="Remove seat"
                      >
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <line x1="18" y1="6" x2="6" y2="18" />
                          <line x1="6" y1="6" x2="18" y2="18" />
                        </svg>
                      </button>
                    </div>
                  </div>
                ))}

                {/* Subtotal Calculation */}
                <div style={{
                  borderTop: '1px solid var(--border-subtle)',
                  paddingTop: '14px',
                  marginTop: '10px',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center'
                }}>
                  <span style={{ color: 'var(--text-secondary)' }}>Total Amount</span>
                  <span style={{ fontSize: '1.4rem', fontWeight: 800, fontFamily: 'var(--font-heading)', color: 'var(--text-main)' }}>
                    ₹{totalPrice}
                  </span>
                </div>
              </div>
            )}

            <button
              className="btn btn-primary btn-lg"
              disabled={selectedSeats.length === 0}
              onClick={() => onProceedToCheckout({ selectedSeats, totalPrice })}
              style={{ width: '100%' }}
            >
              <span>Confirm Seats & Proceed</span>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <line x1="5" y1="12" x2="19" y2="12" />
                <polyline points="12 5 19 12 12 19" />
              </svg>
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Sticky Bottom Sheet (Appears when seats are selected on mobile) */}
      {selectedSeats.length > 0 && (
        <div style={{
          position: 'fixed',
          bottom: 0,
          left: 0,
          right: 0,
          backgroundColor: 'var(--bg-card)',
          borderTop: '1px solid var(--border-medium)',
          padding: '14px 20px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          zIndex: 90,
          boxShadow: '0 -4px 12px rgba(0,0,0,0.5)'
        }}>
          <div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
              {selectedSeats.length} {selectedSeats.length === 1 ? 'seat' : 'seats'} selected
            </div>
            <div style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--text-main)', fontFamily: 'var(--font-heading)' }}>
              ₹{totalPrice}
            </div>
          </div>
          <button
            className="btn btn-primary"
            onClick={() => onProceedToCheckout({ selectedSeats, totalPrice })}
          >
            Checkout ({selectedSeats.length})
          </button>
        </div>
      )}
    </div>
  );
}
