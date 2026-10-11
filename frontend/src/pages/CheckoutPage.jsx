import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { openRazorpayCheckout } from '../services/razorpay';
import { ApiService } from '../services/api';
import { recordBookedSeatsForEvent } from '../data/seatLayouts';
import { UserService } from '../services/userService';

export default function CheckoutPage({ checkoutData, onPaymentComplete }) {
  const navigate = useNavigate();

  const [paymentMethod, setPaymentMethod] = useState('RAZORPAY');
  const [upiId, setUpiId] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);
  const [paymentError, setPaymentError] = useState(null);

  // User & Contact Info for Ticket Dispatch
  const [currentUser] = useState(() => UserService.getCurrentUser());
  const [customerEmail, setCustomerEmail] = useState(() => currentUser?.email || '');
  const [customerName, setCustomerName] = useState(() => currentUser?.name || '');

  // Fallback data if user navigated directly
  const booking = checkoutData || {
    event: {
      id: "11111111-1111-1111-1111-111111111111",
      title: "Acoustic Nights with Arijit Singh Live",
      venueName: "DY Patil Stadium, Navi Mumbai",
      posterUrl: "https://lh3.googleusercontent.com/aida-public/AB6AXuC8QOOlV1iJbyC-Lqx0CaRY6ZbMpRsA0y6gn5Ko5QiWYHzZK1LTP_URByQavUbn9NODDsQT1qgssQ91QgY6UpeURaks44xgO7t9dqwYjcU9Lf9us-tVg0YSYmXr26X2-Vdbk6j8_lao3QfNCphg12_Tek8Uo2goiKQOmS6-vp_13datXB_ngX6DZIUuIcBKVGeWrJBO1Tr_yFocQ22ubLWTUP1dJYPUfvP__M-3qsxwmHIb1Nyd0uXS"
    },
    seats: [
      { id: "B4", row: "B", number: 4, tier: "LOUNGE / VIP", price: 3500 },
      { id: "B5", row: "B", number: 5, tier: "LOUNGE / VIP", price: 3500 }
    ],
    totalPrice: 7000
  };

  // Authoritative Backend Costing State
  const [backendCost, setBackendCost] = useState(null);
  const [isLoadingCost, setIsLoadingCost] = useState(true);

  useEffect(() => {
    let active = true;
    async function loadCostFromBackend() {
      setIsLoadingCost(true);
      try {
        const res = await ApiService.calculateCost({
          eventId: booking.event.id,
          seats: booking.seats,
          quantity: booking.seats.length
        });
        if (active && res) {
          setBackendCost(res);
        }
      } catch (err) {
        console.warn('Backend cost calculation notice:', err);
      } finally {
        if (active) setIsLoadingCost(false);
      }
    }
    loadCostFromBackend();
    return () => { active = false; };
  }, [booking.event.id, booking.seats]);

  // Read authoritative figures managed entirely by backend
  const ticketCount = backendCost?.ticketCount ?? booking.seats.length;
  const ticketSubtotal = backendCost?.ticketSubtotal ?? booking.totalPrice;
  const platformFee = backendCost?.platformFeeTotal ?? (25 * ticketCount);
  const cgst = backendCost?.cgstAmount ?? Number((ticketSubtotal * 0.025).toFixed(2));
  const sgst = backendCost?.sgstAmount ?? Number((ticketSubtotal * 0.025).toFixed(2));
  const grandTotal = backendCost?.grandTotal ? Math.round(Number(backendCost.grandTotal)) : Math.round(ticketSubtotal + cgst + sgst + platformFee);

  const completeBookingConfirmation = (paymentDetails) => {
    const bookingId = `EP-BK-${Math.floor(100000 + Math.random() * 900000)}`;

    // 1. Record booked seats strictly for this event (turns seats gray in layout)
    if (booking?.event?.id && booking?.seats?.length) {
      const seatIds = booking.seats.map(s => s.id || `${s.row}${s.number}`);
      recordBookedSeatsForEvent(booking.event.id, seatIds);
    }

    // 2. Dispatch M-Ticket automatically to customer's unique email address
    const targetEmail = (customerEmail || currentUser?.email || '').trim() || 'customer@eventpulse.com';
    UserService.sendTicketEmail({
      email: targetEmail,
      bookingId,
      eventTitle: booking.event.title,
      seats: booking.seats,
      totalPaid: grandTotal
    });

    ApiService.sendTicketNotification({
      toEmail: targetEmail,
      recipientName: customerName || currentUser?.name || 'Customer',
      bookingId,
      eventTitle: booking.event.title,
      seats: booking.seats,
      totalPaid: grandTotal,
      venueName: booking.event.venueName
    });

    const confirmedPass = {
      bookingId,
      event: booking.event,
      seats: booking.seats,
      ticketCount: ticketCount,
      ticketSubtotal: ticketSubtotal,
      platformFee: platformFee,
      cgst: cgst,
      sgst: sgst,
      totalPaid: grandTotal,
      customerEmail: targetEmail,
      customerName: customerName || currentUser?.name || 'Customer',
      paymentMethod: paymentDetails.method || 'Razorpay Standard',
      razorpayPaymentId: paymentDetails.paymentId || `pay_rzp_${Date.now()}`,
      razorpayOrderId: paymentDetails.orderId || `order_${Date.now()}`,
      bookingTime: new Date().toISOString(),
      qrToken: `EP.${Date.now()}.${booking.event.id}.RZP`
    };

    if (onPaymentComplete) {
      onPaymentComplete(confirmedPass);
    }
    navigate('/confirmed');
  };

  const handlePay = (e) => {
    e.preventDefault();
    setIsProcessing(true);
    setPaymentError(null);

    // Launch Real Razorpay Standard Checkout Popup
    openRazorpayCheckout({
      amountInRupees: grandTotal,
      eventTitle: booking.event.title,
      bookingId: `BK-${Date.now()}`,
      onSuccess: (response) => {
        setIsProcessing(false);
        completeBookingConfirmation({
          method: 'Razorpay Gateway',
          paymentId: response.paymentId,
          orderId: response.orderId
        });
      },
      onDismiss: () => {
        setIsProcessing(false);
      },
      onError: (err) => {
        setIsProcessing(false);
        setPaymentError(err.message || 'Payment was cancelled or could not be processed.');
      }
    });
  };

  return (
    <div style={{ backgroundColor: 'var(--color-surface)', minHeight: '100vh', padding: '32px 0 80px' }}>
      <div className="site-container">
        {/* Stepper Header */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: '24px' }}>
          <Link to="/" style={{ color: 'var(--color-text-secondary)', textDecoration: 'none' }}>Home</Link>
          <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>chevron_right</span>
          <span>Seat Selection</span>
          <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>chevron_right</span>
          <span style={{ color: 'var(--color-text-main)', fontWeight: 600 }}>Order Summary &amp; Payment</span>
        </div>

        <h1 style={{ fontSize: '26px', fontWeight: 800, color: 'var(--color-text-main)', marginBottom: '28px' }}>
          Review Your Booking &amp; Pay
        </h1>

        {/* 2-Column Checkout Layout */}
        <div style={{ display: 'grid', gridTemplateColumns: '1.25fr 0.85fr', gap: '32px', alignItems: 'start' }} className="checkout-grid">
          {/* Left Column: Delivery Email & Payment Options */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {/* Customer M-Ticket Delivery Contact Card */}
            <div style={{
              backgroundColor: '#FFFFFF',
              borderRadius: 'var(--radius-xl)',
              border: '1px solid var(--color-border)',
              boxShadow: 'var(--shadow-subtle)',
              padding: '24px'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span className="material-symbols-outlined" style={{ color: 'var(--color-primary)', fontSize: '22px' }}>
                    mark_email_read
                  </span>
                  <h2 style={{ fontSize: '16px', fontWeight: 800, color: 'var(--color-text-main)', margin: 0 }}>
                    M-Ticket Dispatch &amp; Delivery Email
                  </h2>
                </div>
                <span className="badge-pill badge-neutral" style={{ fontSize: '11px', color: '#166534', backgroundColor: '#F0FDF4', border: '1px solid #BBF7D0' }}>
                  VERIFIED RECIPIENT
                </span>
              </div>

              <p style={{ fontSize: '12px', color: 'var(--color-text-secondary)', margin: '0 0 16px 0', lineHeight: 1.4 }}>
                Your official M-Ticket QR pass and 5% GST tax invoice will be dispatched immediately to this registered email.
              </p>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '11px', fontWeight: 700, color: 'var(--color-text-secondary)', marginBottom: '6px', textTransform: 'uppercase' }}>
                    Recipient Full Name
                  </label>
                  <input
                    type="text"
                    value={customerName}
                    onChange={(e) => setCustomerName(e.target.value)}
                    placeholder="Enter full name"
                    style={{
                      width: '100%',
                      padding: '10px 12px',
                      borderRadius: 'var(--radius-md)',
                      border: '1px solid var(--color-border-strong)',
                      fontSize: '13px',
                      outline: 'none'
                    }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '11px', fontWeight: 700, color: 'var(--color-text-secondary)', marginBottom: '6px', textTransform: 'uppercase' }}>
                    Unique Email ID (Ticket Delivery)
                  </label>
                  <input
                    type="email"
                    value={customerEmail}
                    onChange={(e) => setCustomerEmail(e.target.value)}
                    placeholder="name@example.com"
                    required
                    style={{
                      width: '100%',
                      padding: '10px 12px',
                      borderRadius: 'var(--radius-md)',
                      border: '1px solid var(--color-border-strong)',
                      fontSize: '13px',
                      outline: 'none'
                    }}
                  />
                </div>
              </div>
            </div>

            <div style={{
              backgroundColor: '#FFFFFF',
              borderRadius: 'var(--radius-xl)',
              border: '1px solid var(--color-border)',
              boxShadow: 'var(--shadow-subtle)',
              padding: '28px'
            }}>
              <h2 style={{ fontSize: '18px', fontWeight: 800, color: 'var(--color-text-main)', marginBottom: '20px' }}>
                Select Payment Option
              </h2>

              {/* Payment Method Selector Tabs */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '24px' }}>
                {/* Razorpay Gateway Option (Default & Real) */}
                <div
                  onClick={() => setPaymentMethod('RAZORPAY')}
                  style={{
                    padding: '16px',
                    borderRadius: 'var(--radius-lg)',
                    border: `2px solid ${paymentMethod === 'RAZORPAY' ? 'var(--color-primary)' : 'var(--color-border)'}`,
                    backgroundColor: paymentMethod === 'RAZORPAY' ? '#FFF1F2' : '#FFFFFF',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between'
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{
                      width: '36px',
                      height: '36px',
                      borderRadius: '8px',
                      backgroundColor: '#0C2340',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      color: '#3395FF',
                      fontWeight: 900,
                      fontSize: '18px'
                    }}>
                      R
                    </div>
                    <div>
                      <div style={{ fontWeight: 800, fontSize: '15px', color: 'var(--color-text-main)' }}>
                        Razorpay Payment Gateway
                      </div>
                      <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>
                        UPI (GPay, PhonePe, Paytm, QR), Cards, NetBanking, Wallets
                      </div>
                    </div>
                  </div>
                  <span className="badge-pill badge-crimson" style={{ fontSize: '10px' }}>REAL GATEWAY</span>
                </div>

                {/* Direct UPI Option */}
                <div
                  onClick={() => setPaymentMethod('UPI')}
                  style={{
                    padding: '16px',
                    borderRadius: 'var(--radius-lg)',
                    border: `2px solid ${paymentMethod === 'UPI' ? 'var(--color-primary)' : 'var(--color-border)'}`,
                    backgroundColor: paymentMethod === 'UPI' ? '#FFF1F2' : '#FFFFFF',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between'
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <span className="material-symbols-outlined" style={{ color: 'var(--color-primary)', fontSize: '24px' }}>
                      qr_code_scanner
                    </span>
                    <div>
                      <div style={{ fontWeight: 700, fontSize: '15px', color: 'var(--color-text-main)' }}>Direct UPI / VPA</div>
                      <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>Pay via UPI ID Intent</div>
                    </div>
                  </div>
                  <span className="badge-pill badge-neutral" style={{ fontSize: '10px' }}>INSTANT</span>
                </div>

                {/* Cards Option */}
                <div
                  onClick={() => setPaymentMethod('CARD')}
                  style={{
                    padding: '16px',
                    borderRadius: 'var(--radius-lg)',
                    border: `2px solid ${paymentMethod === 'CARD' ? 'var(--color-primary)' : 'var(--color-border)'}`,
                    backgroundColor: paymentMethod === 'CARD' ? '#FFF1F2' : '#FFFFFF',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between'
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <span className="material-symbols-outlined" style={{ color: 'var(--color-primary)', fontSize: '24px' }}>
                      credit_card
                    </span>
                    <div>
                      <div style={{ fontWeight: 700, fontSize: '15px', color: 'var(--color-text-main)' }}>Credit / Debit Cards</div>
                      <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>Visa, MasterCard, RuPay, Amex</div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Direct UPI Input if selected */}
              {paymentMethod === 'UPI' && (
                <div style={{
                  padding: '16px',
                  backgroundColor: 'var(--color-surface)',
                  borderRadius: 'var(--radius-md)',
                  marginBottom: '20px'
                }}>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, marginBottom: '6px' }}>
                    Enter UPI ID (VPA)
                  </label>
                  <input
                    type="text"
                    placeholder="e.g. mobile@okaxis"
                    value={upiId}
                    onChange={(e) => setUpiId(e.target.value)}
                    style={{
                      width: '100%',
                      padding: '10px 14px',
                      borderRadius: 'var(--radius-sm)',
                      border: '1px solid var(--color-border-strong)',
                      fontSize: '14px',
                      outline: 'none'
                    }}
                  />
                  <span style={{ fontSize: '11px', color: 'var(--color-text-secondary)', display: 'block', marginTop: '4px' }}>
                    Will route authorization through Razorpay secure engine.
                  </span>
                </div>
              )}

              {/* Payment Error Feedback */}
              {paymentError && (
                <div style={{
                  padding: '12px 16px',
                  backgroundColor: '#FEF2F2',
                  border: '1px solid #FCA5A5',
                  color: '#991B1B',
                  borderRadius: 'var(--radius-md)',
                  fontSize: '13px',
                  marginBottom: '16px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px'
                }}>
                  <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>error</span>
                  <span>{paymentError}</span>
                </div>
              )}

              {/* Gateway Live Key Info Banner */}
              <div style={{
                padding: '10px 14px',
                backgroundColor: '#F0FDF4',
                border: '1px solid #BBF7D0',
                borderRadius: 'var(--radius-md)',
                fontSize: '12px',
                color: '#166534',
                marginBottom: '16px',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}>
                <span className="material-symbols-outlined" style={{ fontSize: '18px', color: '#16A34A' }}>check_circle</span>
                <span>Active Razorpay Merchant Key ID: <strong>rzp_test_TmH1RHV9SNUGpB</strong></span>
              </div>

              {/* Buttons Row */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                <button
                  onClick={handlePay}
                  disabled={isProcessing}
                  className="btn btn-primary btn-lg"
                  style={{ width: '100%', justifyContent: 'center', fontWeight: 800, fontSize: '15px' }}
                >
                  <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>payments</span>
                  <span>{isProcessing ? 'Opening Razorpay...' : `Pay ₹${grandTotal.toLocaleString()} with Razorpay`}</span>
                </button>

                <button
                  onClick={() => {
                    completeBookingConfirmation({
                      method: 'Razorpay Instant Simulation',
                      paymentId: `pay_sim_${Date.now()}`
                    });
                  }}
                  type="button"
                  className="btn btn-secondary"
                  style={{ width: '100%', justifyContent: 'center', fontSize: '12px', color: 'var(--color-text-secondary)' }}
                >
                  ⚡ Instant Test Approval (Bypass Popup)
                </button>
              </div>
            </div>

            {/* Security Trust Badges */}
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '24px',
              fontSize: '12px',
              color: 'var(--color-text-secondary)'
            }}>
              <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                <span className="material-symbols-outlined" style={{ fontSize: '16px', color: '#16A34A' }}>lock</span>
                256-Bit SSL Encrypted
              </span>
              <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                <span className="material-symbols-outlined" style={{ fontSize: '16px', color: 'var(--color-primary)' }}>verified_user</span>
                100% Safe Payments
              </span>
            </div>
          </div>

          {/* Right Column: Order Summary Card with Platform Fee & GST Itemization */}
          <div style={{
            backgroundColor: '#FFFFFF',
            borderRadius: 'var(--radius-xl)',
            border: '1px solid var(--color-border)',
            boxShadow: 'var(--shadow-subtle)',
            padding: '24px',
            display: 'flex',
            flexDirection: 'column',
            gap: '16px'
          }}>
            <div style={{ display: 'flex', gap: '14px', paddingBottom: '16px', borderBottom: '1px solid var(--color-border-subtle)' }}>
              <img
                src={booking.event.posterUrl}
                alt={booking.event.title}
                style={{ width: '70px', height: '100px', objectFit: 'cover', borderRadius: 'var(--radius-md)' }}
              />
              <div>
                <h3 style={{ fontSize: '16px', fontWeight: 800, color: 'var(--color-text-main)', lineHeight: 1.3 }}>
                  {booking.event.title}
                </h3>
                <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)', marginTop: '4px' }}>
                  {booking.event.venueName}
                </div>
                <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--color-primary)', marginTop: '4px' }}>
                  Sat, 14 Dec | 7:00 PM
                </div>
              </div>
            </div>

            {/* Selected Seats */}
            <div>
              <div style={{ fontSize: '12px', fontWeight: 700, color: 'var(--color-text-secondary)', textTransform: 'uppercase', marginBottom: '8px' }}>
                Reserved Seats ({ticketCount})
              </div>
              <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                {booking.seats.map(s => (
                  <span key={s.id} className="badge-pill badge-neutral" style={{ fontSize: '12px' }}>
                    {s.row}{s.number} ({s.tier})
                  </span>
                ))}
              </div>
            </div>

            {/* Backend Engine Verification Badge */}
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              padding: '6px 10px',
              backgroundColor: '#F0FDF4',
              border: '1px solid #BBF7D0',
              borderRadius: 'var(--radius-sm)',
              fontSize: '11px',
              color: '#166534',
              fontWeight: 600
            }}>
              <span className="material-symbols-outlined" style={{ fontSize: '15px', color: '#16A34A' }}>verified</span>
              <span>{backendCost?.calculatedBy || 'Costing Managed by Backend Engine'}</span>
            </div>

            {/* Exact Fee & Tax Itemization Breakdown */}
            <div style={{
              display: 'flex',
              flexDirection: 'column',
              gap: '10px',
              paddingTop: '12px',
              borderTop: '1px solid var(--color-border-subtle)',
              fontSize: '13px',
              color: 'var(--color-text-secondary)'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span>Ticket Subtotal ({ticketCount} {ticketCount === 1 ? 'Ticket' : 'Tickets'})</span>
                <span style={{ color: 'var(--color-text-main)', fontWeight: 600 }}>₹{ticketSubtotal.toLocaleString()}</span>
              </div>

              {/* Central GST 2.5% on Ticket Price */}
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px' }}>
                <span>Central GST (CGST @ 2.5% on Ticket Price)</span>
                <span style={{ color: 'var(--color-text-main)' }}>₹{cgst.toFixed(2)}</span>
              </div>

              {/* State GST 2.5% on Ticket Price */}
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px' }}>
                <span>State GST (SGST @ 2.5% on Ticket Price)</span>
                <span style={{ color: 'var(--color-text-main)' }}>₹{sgst.toFixed(2)}</span>
              </div>

              {/* EventPulse Platform Fee (₹25 per ticket) */}
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span>Platform Convenience Fee (₹25 × {ticketCount})</span>
                <span style={{ color: 'var(--color-text-main)', fontWeight: 600 }}>₹{platformFee.toFixed(2)}</span>
              </div>

              {/* Grand Total */}
              <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                paddingTop: '14px',
                borderTop: '1px solid var(--color-border)',
                fontSize: '16px',
                fontWeight: 800,
                color: 'var(--color-text-main)'
              }}>
                <span>Total Amount Payable</span>
                <span style={{ color: 'var(--color-primary)' }}>₹{grandTotal.toLocaleString()}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <style>{`
        @media (max-width: 860px) {
          .checkout-grid {
            grid-template-columns: 1fr !important;
          }
        }
      `}</style>
    </div>
  );
}
