import React, { useState, useEffect } from 'react';

export default function CheckoutModal({ event, checkoutData, onClose, onPaymentSuccess }) {
  const [secondsRemaining, setSecondsRemaining] = useState(600); // 10 minutes hold
  const [paymentMethod, setPaymentMethod] = useState('UPI'); // 'UPI' | 'CARD' | 'NETBANKING'
  const [isProcessing, setIsProcessing] = useState(false);

  useEffect(() => {
    const timer = setInterval(() => {
      setSecondsRemaining(prev => {
        if (prev <= 1) {
          clearInterval(timer);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  const minutes = Math.floor(secondsRemaining / 60);
  const seconds = secondsRemaining % 60;
  const timeFormatted = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;

  const handleSimulatePayment = () => {
    setIsProcessing(true);
    setTimeout(() => {
      setIsProcessing(false);
      onPaymentSuccess({
        orderId: `order_${Math.random().toString(36).substring(2, 10)}`,
        paymentId: `pay_${Math.random().toString(36).substring(2, 12)}`,
        signature: `hmac_sha256_${Math.random().toString(36).substring(2, 16)}`,
        seatNumbers: checkoutData.selectedSeats.map(s => s.label),
        totalAmount: checkoutData.totalPrice,
        currency: 'INR'
      });
    }, 1200);
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div 
        className="modal-card" 
        style={{ maxWidth: '580px' }} 
        onClick={e => e.stopPropagation()}
      >
        {/* Top Header */}
        <div style={{
          padding: '20px 24px',
          borderBottom: '1px solid var(--border-subtle)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}>
          <div>
            <h3 style={{ fontSize: '1.25rem' }}>Secure Checkout & Seat Reservation</h3>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
              Razorpay Verified Order Gateway
            </div>
          </div>

          {/* 10-Minute Hold Window Countdown */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            backgroundColor: secondsRemaining < 120 ? 'rgba(239, 68, 68, 0.15)' : 'var(--bg-canvas)',
            border: `1px solid ${secondsRemaining < 120 ? 'var(--status-danger)' : 'var(--border-medium)'}`,
            padding: '6px 12px',
            borderRadius: 'var(--radius-xs)'
          }}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke={secondsRemaining < 120 ? 'var(--status-danger)' : 'var(--accent-primary)'} strokeWidth="2">
              <circle cx="12" cy="12" r="10" />
              <polyline points="12 6 12 12 16 14" />
            </svg>
            <div style={{ fontFamily: 'var(--font-mono)', fontSize: '0.9rem', fontWeight: 700, color: secondsRemaining < 120 ? 'var(--status-danger)' : 'var(--text-main)' }}>
              {timeFormatted}
            </div>
          </div>
        </div>

        {/* Content Body */}
        <div style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {/* Reservation Warning Bar */}
          <div style={{
            backgroundColor: 'var(--bg-canvas)',
            borderLeft: '3px solid var(--accent-primary)',
            padding: '12px 16px',
            fontSize: '0.82rem',
            color: 'var(--text-secondary)'
          }}>
            Your selected seats are held under temporary lock for 10 minutes. If checkout is not completed, they will be released to public inventory.
          </div>

          {/* Order Details */}
          <div>
            <div style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: '8px' }}>
              Reserved Seat Numbers
            </div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
              {checkoutData.selectedSeats.map(s => (
                <span key={s.label} className="badge badge-amber" style={{ padding: '6px 12px', fontSize: '0.82rem' }}>
                  {s.label} ({s.tier})
                </span>
              ))}
            </div>
          </div>

          {/* Payment Method Selector */}
          <div>
            <div style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: '10px' }}>
              Select Payment Method
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px' }}>
              {[
                { id: 'UPI', label: 'UPI / QR', desc: 'Google Pay, PhonePe, Paytm' },
                { id: 'CARD', label: 'Credit / Debit', desc: 'Visa, Mastercard, RuPay' },
                { id: 'NETBANKING', label: 'Net Banking', desc: 'All Major Indian Banks' }
              ].map(method => (
                <button
                  key={method.id}
                  type="button"
                  onClick={() => setPaymentMethod(method.id)}
                  style={{
                    backgroundColor: paymentMethod === method.id ? 'var(--accent-subtle)' : 'var(--bg-canvas)',
                    border: `1px solid ${paymentMethod === method.id ? 'var(--accent-primary)' : 'var(--border-subtle)'}`,
                    borderRadius: 'var(--radius-sm)',
                    padding: '12px 10px',
                    textAlign: 'left',
                    cursor: 'pointer',
                    transition: 'all 0.15s ease'
                  }}
                >
                  <div style={{ fontWeight: 600, fontSize: '0.85rem', color: paymentMethod === method.id ? 'var(--accent-primary)' : 'var(--text-main)' }}>
                    {method.label}
                  </div>
                  <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', marginTop: '2px' }}>
                    {method.desc}
                  </div>
                </button>
              ))}
            </div>
          </div>

          {/* Payment Form Details */}
          <div style={{
            backgroundColor: 'var(--bg-canvas)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 'var(--radius-sm)',
            padding: '14px 16px'
          }}>
            {paymentMethod === 'UPI' && (
              <div>
                <label style={{ fontSize: '0.78rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
                  Enter UPI ID (VPA)
                </label>
                <input
                  type="text"
                  defaultValue="attendee@okhdfcbank"
                  style={{
                    width: '100%',
                    backgroundColor: 'var(--bg-card)',
                    border: '1px solid var(--border-medium)',
                    borderRadius: 'var(--radius-xs)',
                    padding: '9px 12px',
                    color: 'var(--text-main)',
                    fontSize: '0.88rem',
                    fontFamily: 'var(--font-mono)',
                    outline: 'none'
                  }}
                />
              </div>
            )}
            {paymentMethod === 'CARD' && (
              <div>
                <label style={{ fontSize: '0.78rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
                  Card Number (Test Simulation)
                </label>
                <input
                  type="text"
                  defaultValue="4111 •••• •••• 1111"
                  disabled
                  style={{
                    width: '100%',
                    backgroundColor: 'var(--bg-card)',
                    border: '1px solid var(--border-medium)',
                    borderRadius: 'var(--radius-xs)',
                    padding: '9px 12px',
                    color: 'var(--text-main)',
                    fontSize: '0.88rem',
                    fontFamily: 'var(--font-mono)'
                  }}
                />
              </div>
            )}
            {paymentMethod === 'NETBANKING' && (
              <div style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                Simulating direct redirect to Razorpay NetBanking authentication gateway.
              </div>
            )}
          </div>

          {/* Pricing Breakdown */}
          <div style={{
            borderTop: '1px solid var(--border-subtle)',
            paddingTop: '16px',
            display: 'flex',
            flexDirection: 'column',
            gap: '6px'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
              <span>Base Seat Price ({checkoutData.selectedSeats.length} seats)</span>
              <span>₹{checkoutData.totalPrice}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
              <span>Booking Convenience Fee (Zero Markup)</span>
              <span style={{ color: 'var(--status-success)' }}>FREE</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '1.25rem', fontWeight: 700, color: 'var(--text-main)', marginTop: '8px' }}>
              <span>Total Payable</span>
              <span style={{ fontFamily: 'var(--font-heading)' }}>₹{checkoutData.totalPrice}</span>
            </div>
          </div>

          {/* Action Buttons */}
          <div style={{ display: 'flex', gap: '12px', marginTop: '10px' }}>
            <button className="btn btn-outline" onClick={onClose} disabled={isProcessing}>
              Cancel Hold
            </button>
            <button
              className="btn btn-primary btn-lg"
              onClick={handleSimulatePayment}
              disabled={isProcessing || secondsRemaining === 0}
              style={{ flex: 1 }}
            >
              {isProcessing ? (
                <span>Verifying Razorpay Order...</span>
              ) : (
                <>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <rect x="2" y="5" width="20" height="14" rx="2" />
                    <line x1="2" y1="10" x2="22" y2="10" />
                  </svg>
                  <span>Pay ₹{checkoutData.totalPrice} via Razorpay</span>
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
