// EventPulse Razorpay Payment Gateway Service
// Uses real merchant key from .env (rzp_test_TmH1RHV9SNUGpB)

const RAZORPAY_KEY = import.meta.env.VITE_RAZORPAY_KEY_ID || 'rzp_test_TmH1RHV9SNUGpB';

export function loadRazorpayScript() {
  return new Promise((resolve) => {
    if (window.Razorpay) {
      resolve(true);
      return;
    }
    const script = document.createElement('script');
    script.src = 'https://checkout.razorpay.com/v1/checkout.js';
    script.onload = () => resolve(true);
    script.onerror = () => resolve(false);
    document.body.appendChild(script);
  });
}

export async function openRazorpayCheckout({
  amountInRupees,
  eventTitle,
  bookingId,
  customerName = 'Akash Debnath',
  customerEmail = 'debnathakash2004@gmail.com',
  customerPhone = '9876543210',
  onSuccess,
  onDismiss,
  onError
}) {
  const isLoaded = await loadRazorpayScript();
  if (!isLoaded || !window.Razorpay) {
    if (onError) onError(new Error('Razorpay SDK failed to load. Please check your internet connection.'));
    return;
  }

  // Amount in subunits (paise): 1 INR = 100 paise
  const amountInPaise = Math.round(amountInRupees * 100);

  const options = {
    key: RAZORPAY_KEY,
    amount: amountInPaise,
    currency: 'INR',
    name: 'EventPulse',
    description: `Booking for ${eventTitle}`,
    image: 'https://cdn-icons-png.flaticon.com/512/3845/3845829.png',
    prefill: {
      name: customerName,
      email: customerEmail,
      contact: customerPhone
    },
    notes: {
      bookingId: bookingId,
      platform: 'EventPulse Ticketing'
    },
    theme: {
      color: '#F84464' // Official TicketPulse Crimson Brand Color
    },
    handler: function (response) {
      // response: { razorpay_payment_id, razorpay_order_id, razorpay_signature }
      if (onSuccess) {
        onSuccess({
          paymentId: response.razorpay_payment_id,
          orderId: response.razorpay_order_id || `order_sim_${Date.now()}`,
          signature: response.razorpay_signature || 'sig_verified'
        });
      }
    },
    modal: {
      ondismiss: function () {
        if (onDismiss) onDismiss();
      }
    }
  };

  try {
    const razorpayInstance = new window.Razorpay(options);
    razorpayInstance.on('payment.failed', function (response) {
      if (onError) {
        onError(response.error);
      }
    });
    razorpayInstance.open();
  } catch (err) {
    console.error('Failed to open Razorpay modal:', err);
    if (onError) onError(err);
  }
}
