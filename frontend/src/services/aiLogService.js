// EventPulse AI Log Service — Tracks and Audits AI Assistant Interactions
// Persisted locally with full administrative inspection & export capabilities

const AI_LOGS_STORAGE_KEY = 'eventpulse_ai_interaction_logs';

const INITIAL_SEEDED_LOGS = [
  {
    id: 'ai-log-101',
    timestamp: new Date(Date.now() - 3600000 * 2).toISOString(),
    query: 'What is the cancellation and refund policy?',
    response: 'You can cancel your booking up to 48 hours prior to the event start time for a 100% full refund via Razorpay (5-7 business days). Cancellations within 48 hours are non-refundable.',
    intent: 'REFUNDS_CANCELLATIONS',
    status: 'RESOLVED',
    latencyMs: 142,
    recommendedEvent: null
  },
  {
    id: 'ai-log-102',
    timestamp: new Date(Date.now() - 3600000 * 5).toISOString(),
    query: 'How long is my ticket reserved during checkout?',
    response: 'When you initiate a booking on EventPulse, your tickets are held exclusively for you for 15 minutes. If payment is not completed within 15 minutes, the reservation expires automatically.',
    intent: 'TICKET_HOLD',
    status: 'RESOLVED',
    latencyMs: 118,
    recommendedEvent: null
  },
  {
    id: 'ai-log-103',
    timestamp: new Date(Date.now() - 3600000 * 9).toISOString(),
    query: 'Concerts this weekend in Mumbai',
    response: 'Found live concert: Acoustic Nights with Arijit Singh Live at DY Patil Stadium, Navi Mumbai starting from ₹1,999.',
    intent: 'EVENT_DISCOVERY',
    status: 'RESOLVED',
    latencyMs: 235,
    recommendedEvent: {
      id: 'evt-001',
      title: 'Acoustic Nights with Arijit Singh Live',
      venue: 'DY Patil Stadium, Navi Mumbai',
      price: '₹1,999 onwards'
    }
  },
  {
    id: 'ai-log-104',
    timestamp: new Date(Date.now() - 3600000 * 18).toISOString(),
    query: 'How do I receive my tickets after payment?',
    response: 'Immediately after payment verification, confirmed tickets with QR codes and booking summary are sent to your registered email and phone. You can also view them under My Bookings.',
    intent: 'TICKET_DELIVERY',
    status: 'RESOLVED',
    latencyMs: 98,
    recommendedEvent: null
  }
];

export const aiLogService = {
  // Retrieve all AI interaction logs
  getAiLogs() {
    try {
      const stored = localStorage.getItem(AI_LOGS_STORAGE_KEY);
      if (stored) {
        return JSON.parse(stored);
      }
      // Seed with initial realistic interaction audit logs
      localStorage.setItem(AI_LOGS_STORAGE_KEY, JSON.stringify(INITIAL_SEEDED_LOGS));
      return INITIAL_SEEDED_LOGS;
    } catch (e) {
      console.warn('[aiLogService] Could not read logs from localStorage:', e);
      return INITIAL_SEEDED_LOGS;
    }
  },

  // Record a new AI query-response interaction log
  recordAiLog({ query, response, intent = 'GENERAL', recommendedEvent = null, latencyMs = 150, status = 'RESOLVED' }) {
    if (!query || !response) return null;

    const newLog = {
      id: `ai-log-${Date.now()}-${Math.random().toString(36).substr(2, 5)}`,
      timestamp: new Date().toISOString(),
      query: query.trim(),
      response: response.trim(),
      intent: intent || 'GENERAL',
      status: status || 'RESOLVED',
      latencyMs: Math.max(30, latencyMs || Math.floor(Math.random() * 120 + 80)),
      recommendedEvent: recommendedEvent || null
    };

    try {
      const logs = this.getAiLogs();
      const updated = [newLog, ...logs];
      localStorage.setItem(AI_LOGS_STORAGE_KEY, JSON.stringify(updated));
      return newLog;
    } catch (e) {
      console.warn('[aiLogService] Could not save log:', e);
      return newLog;
    }
  },

  // Clear interaction logs
  clearAiLogs() {
    try {
      localStorage.setItem(AI_LOGS_STORAGE_KEY, JSON.stringify([]));
    } catch (e) {
      console.warn('[aiLogService] Could not clear logs:', e);
    }
  },

  // Reset to default sample logs
  resetToDefaultLogs() {
    try {
      localStorage.setItem(AI_LOGS_STORAGE_KEY, JSON.stringify(INITIAL_SEEDED_LOGS));
      return INITIAL_SEEDED_LOGS;
    } catch (e) {
      console.warn('[aiLogService] Could not reset logs:', e);
      return INITIAL_SEEDED_LOGS;
    }
  },

  // Export logs as a downloadable JSON file
  exportAiLogs() {
    const logs = this.getAiLogs();
    const dataStr = 'data:text/json;charset=utf-8,' + encodeURIComponent(JSON.stringify(logs, null, 2));
    const downloadAnchor = document.createElement('a');
    downloadAnchor.setAttribute('href', dataStr);
    downloadAnchor.setAttribute('download', `eventpulse_ai_logs_${new Date().toISOString().slice(0, 10)}.json`);
    document.body.appendChild(downloadAnchor);
    downloadAnchor.click();
    downloadAnchor.remove();
  },

  // Aggregate stats for dashboard inspection
  getAiStats() {
    const logs = this.getAiLogs();
    const totalQueries = logs.length;
    const intentCounts = {};
    let totalLatency = 0;

    logs.forEach(log => {
      intentCounts[log.intent] = (intentCounts[log.intent] || 0) + 1;
      totalLatency += log.latencyMs || 100;
    });

    const avgLatency = totalQueries > 0 ? Math.round(totalLatency / totalQueries) : 0;

    return {
      totalQueries,
      avgLatency,
      intentCounts,
      resolvedPercentage: 100
    };
  }
};
