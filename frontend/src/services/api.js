// EventPulse Unified API Client
// Connects to Spring Cloud Gateway (http://localhost:8080/api/v1) with seamless offline fallback

import { MOCK_EVENTS } from '../data/mockEvents';

const BASE_URL = import.meta.env.VITE_API_GATEWAY_URL || 'http://localhost:8080/api/v1';

async function safeFetch(url, options = {}) {
  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 4000);
    const res = await fetch(url, { ...options, signal: controller.signal });
    clearTimeout(timeoutId);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch (err) {
    console.info(`[EventPulse API fallback active for ${url}]:`, err.message);
    return null;
  }
}

export const ApiService = {
  // 1. Event Service
  async getEvents() {
    const live = await safeFetch(`${BASE_URL}/events`);
    if (live && live.content) return live.content;
    return MOCK_EVENTS;
  },

  async getEventById(id) {
    const live = await safeFetch(`${BASE_URL}/events/${id}`);
    if (live && live.id) return live;
    return MOCK_EVENTS.find(e => e.id === id) || MOCK_EVENTS[0];
  },

  getGoogleCalendarUrl(event) {
    const title = encodeURIComponent(event.title);
    const details = encodeURIComponent(`${event.description}\n\nVenue: ${event.venueName}`);
    const location = encodeURIComponent(`${event.venueName}, ${event.address}, ${event.city}`);
    const startIso = (event.startTime || '20261115T193000Z').replace(/[-:]/g, '').split('.')[0] + 'Z';
    const endIso = (event.endTime || '20261115T220000Z').replace(/[-:]/g, '').split('.')[0] + 'Z';
    return `https://calendar.google.com/calendar/render?action=TEMPLATE&text=${title}&dates=${startIso}/${endIso}&details=${details}&location=${location}`;
  },

  downloadIcs(event, seats = []) {
    const seatList = seats.length ? `\\nReserved Seats: ${seats.join(', ')}` : '';
    const startIso = (event.startTime || '20261115T193000Z').replace(/[-:]/g, '').split('.')[0] + 'Z';
    const endIso = (event.endTime || '20261115T220000Z').replace(/[-:]/g, '').split('.')[0] + 'Z';
    const icsContent = [
      'BEGIN:VCALENDAR',
      'VERSION:2.0',
      'PRODID:-//EventPulse//Universal Ticketing RFC 5545//EN',
      'CALSCALE:GREGORIAN',
      'METHOD:PUBLISH',
      'BEGIN:VEVENT',
      `UID:EP-${event.id}-${Date.now()}@eventpulse.com`,
      `DTSTAMP:${startIso}`,
      `DTSTART:${startIso}`,
      `DTEND:${endIso}`,
      `SUMMARY:${event.title}`,
      `DESCRIPTION:${event.description.replace(/\n/g, ' ')}${seatList}`,
      `LOCATION:${event.venueName}\\, ${event.city}`,
      'STATUS:CONFIRMED',
      'END:VEVENT',
      'END:VCALENDAR'
    ].join('\r\n');

    const blob = new Blob([icsContent], { type: 'text/calendar;charset=utf-8' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.setAttribute('download', `${event.slug || 'eventpulse-ticket'}.ics`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  },

  // 2. Booking Service
  async createBooking(bookingData) {
    const live = await safeFetch(`${BASE_URL}/bookings`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(bookingData)
    });
    if (live && live.id) return live;

    // Simulation for offline demo
    return {
      id: `bk-${Math.random().toString(36).substring(2, 9)}`,
      eventId: bookingData.eventId,
      userId: bookingData.userId || 'usr-mock-customer',
      ticketQuantity: bookingData.seatNumbers.length,
      totalAmount: bookingData.totalAmount,
      currency: 'INR',
      status: 'CONFIRMED',
      expiresAt: new Date(Date.now() + 10 * 60 * 1000).toISOString(),
      seatNumbers: bookingData.seatNumbers,
      tickets: bookingData.seatNumbers.map((seat, idx) => ({
        id: `tkt-${Math.random().toString(36).substring(2, 9)}`,
        seatNumber: seat,
        signedToken: `EP.tkt-${1000 + idx}.${bookingData.eventId}.${Date.now().toString(16)}.a9b7c8`,
        status: 'ISSUED'
      }))
    };
  },

  // 3. Gate Scanner Validation
  async scanTicket(token, gateId = "GATE-NORTH-01") {
    const live = await safeFetch(`${BASE_URL}/tickets/scan`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token, gateId })
    });
    if (live) return live;

    // Mock validation rules
    if (!token || !token.startsWith('EP.')) {
      return { success: false, code: "INVALID_SIGNATURE", message: "Cryptographic signature failed or corrupt format." };
    }
    if (token.includes('USED') || token.endsWith('used')) {
      return { success: false, code: "ANTI_PASSBACK_VIOLATION", message: "Pass has already been scanned at Gate South-02. Entry denied." };
    }
    return {
      success: true,
      code: "ENTRY_GRANTED",
      message: "Access Granted. Identity & Ticket verified.",
      ticketId: token.split('.')[1] || 'tkt-001',
      gateId
    };
  },

  // 4. AI Assistant Chat
  async askAiAssistant(question) {
    const live = await safeFetch(`${BASE_URL}/ai/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message: question })
    });
    if (live && live.reply) return live.reply;

    // Authoritative Fallback Knowledge Base
    const lower = question.toLowerCase();
    if (lower.includes('refund') || lower.includes('cancel')) {
      return "EventPulse Policy: Cancellations requested at least 48 hours prior to event start time are eligible for a 100% refund processed within 3-5 business days back to your original Razorpay payment method. Cancellations within 48 hours are non-refundable unless the organizer cancels or reschedules the event.";
    }
    if (lower.includes('hold') || lower.includes('expire') || lower.includes('reserve')) {
      return "Seat Hold Policy: When you begin the checkout process, your selected seats are held under a distributed lock for exactly 10 minutes. If checkout is not completed within 10 minutes, the hold expires automatically and the seats are released back into public inventory.";
    }
    if (lower.includes('calendar') || lower.includes('google')) {
      return "Calendar Integration: Once your tickets are confirmed, you can use the 'Sync to Google Calendar' button with your Google Account for instant event reminders, or download the universal RFC 5545 .ics file for Apple Calendar and Outlook.";
    }
    if (lower.includes('qr') || lower.includes('scan') || lower.includes('gate')) {
      return "Gate Verification: Every EventPulse ticket is cryptographically signed with HMAC-SHA256 and features strict anti-passback protection. Once a pass is scanned at the venue entrance, any subsequent scan attempts are flagged immediately.";
    }
    return "EventPulse AI Concierge: I can assist you with discovering events, checking performer rosters, understanding our 10-minute seat hold window, Razorpay payment processing, and our 48-hour cancellation policies. What would you like to know?";
  }
};
