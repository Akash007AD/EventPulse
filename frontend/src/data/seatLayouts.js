// Dynamic Seat Layout & Pricing Engine for EventPulse
// Dynamically generates seat rows, tiers, pricing, and visual layout per event & venue.
// Manages per-event booked seats isolation and storage.

export const BOOKED_SEATS_KEY = 'eventpulse_booked_seats_by_event';

// Retrieve user-booked seats strictly for a specific event
export function getBookedSeatsForEvent(eventId) {
  if (!eventId) return [];
  try {
    const raw = localStorage.getItem(BOOKED_SEATS_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed[eventId]) ? parsed[eventId] : [];
  } catch {
    return [];
  }
}

// Persist booked seats strictly for a specific event
export function recordBookedSeatsForEvent(eventId, seatIds = []) {
  if (!eventId || !seatIds.length) return;
  try {
    const raw = localStorage.getItem(BOOKED_SEATS_KEY);
    const parsed = raw ? JSON.parse(raw) : {};
    const existing = new Set(parsed[eventId] || []);
    seatIds.forEach(id => existing.add(id));
    parsed[eventId] = Array.from(existing);
    localStorage.setItem(BOOKED_SEATS_KEY, JSON.stringify(parsed));
  } catch (err) {
    console.error('Error saving booked seats:', err);
  }
}

export function getEventSeatLayout(event) {
  if (!event) {
    return {
      stageLabel: 'MAIN STAGE',
      stageType: 'curved',
      rows: getDefaultRows(999)
    };
  }

  const title = (event.title || '').toLowerCase();
  const base = event.basePrice || 999;
  const stage = event.stageLabel || 'STAGE THIS WAY';
  const eventId = event.id || 'default-event';

  let rawLayout;

  // 1. Arijit Singh Live — Stadium Concert (DY Patil Stadium)
  if (title.includes('arijit')) {
    rawLayout = {
      stageLabel: '✦ ACOUSTIC GRAND STAGE ✦',
      stageType: 'curved',
      rows: [
        { row: 'A', tier: 'DIAMOND VIP FRONT', price: 4999, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], sold: [2, 3, 8] },
        { row: 'B', tier: 'DIAMOND VIP FRONT', price: 4999, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], sold: [1, 5, 9] },
        { row: 'C', tier: 'PLATINUM LOUNGE', price: 3499, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12], sold: [4, 7, 11] },
        { row: 'D', tier: 'PLATINUM LOUNGE', price: 3499, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12], sold: [2, 3, 10] },
        { row: 'E', tier: 'GOLD GROUND ARENA', price: 2499, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14], sold: [6, 8, 12] },
        { row: 'F', tier: 'GOLD GROUND ARENA', price: 2499, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14], sold: [1, 9] },
        { row: 'G', tier: 'SILVER STADIUM STAND', price: 1999, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14], sold: [5, 11] },
        { row: 'H', tier: 'SILVER STADIUM STAND', price: 1999, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14], sold: [3, 4] }
      ]
    };
  }
  // 2. Sunburn Arena ft. Alan Walker — Festival EDM Pyro Stage (Jio World Garden)
  else if (title.includes('walker') || title.includes('sunburn')) {
    rawLayout = {
      stageLabel: '⚡ ARENA PYROTECHNICS STAGE ⚡',
      stageType: 'wide',
      rows: [
        { row: 'A', tier: 'FAN PIT VIP (STAGE BARRIER)', price: 3999, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], sold: [1, 2, 7, 8] },
        { row: 'B', tier: 'FAN PIT VIP (STAGE BARRIER)', price: 3999, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], sold: [3, 4, 9] },
        { row: 'C', tier: 'VIP ELEVATED DECK', price: 2999, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12], sold: [5, 6] },
        { row: 'D', tier: 'VIP ELEVATED DECK', price: 2999, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12], sold: [2, 11] },
        { row: 'E', tier: 'PHASE 1 GENERAL ACCESS', price: 1999, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14], sold: [4, 8, 12] },
        { row: 'F', tier: 'EARLY BIRD GA', price: 1499, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14], sold: [1, 14] }
      ]
    };
  }
  // 3. Zakir Khan Stand-up — Intimate Comedy Club (The Habitat)
  else if (title.includes('zakir') || title.includes('comedy')) {
    rawLayout = {
      stageLabel: '🎤 INTIMATE COMEDY ROOM STAGE 🎤',
      stageType: 'compact',
      rows: [
        { row: 'A', tier: 'FRONT ROW (CROWD WORK ZONE)', price: 1499, seats: [1, 2, 3, 4, 5, 6, 7, 8], sold: [3, 4] },
        { row: 'B', tier: 'PREMIUM COMEDY SEATS', price: 1199, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], sold: [1, 2, 7] },
        { row: 'C', tier: 'PREMIUM COMEDY SEATS', price: 1199, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], sold: [5, 9] },
        { row: 'D', tier: 'STANDARD CLUB SEATS', price: 999, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], sold: [4, 6] },
        { row: 'E', tier: 'STANDARD CLUB SEATS', price: 999, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], sold: [8, 10] }
      ]
    };
  }
  // 4. Symphony of Lights: The Grand Philharmonic — NCPA Jamshed Bhabha Theatre
  else if (title.includes('philharmonic') || title.includes('symphony')) {
    rawLayout = {
      stageLabel: '🎻 GRAND ACOUSTIC ORCHESTRA STAGE 🎻',
      stageType: 'theatre',
      rows: [
        { row: 'A', tier: 'ROYAL BOX / VIP STALLS', price: 3200, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], sold: [2, 5, 6] },
        { row: 'B', tier: 'CENTRAL ORCHESTRA', price: 2400, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12], sold: [1, 8, 11] },
        { row: 'C', tier: 'CENTRAL ORCHESTRA', price: 2400, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12], sold: [3, 7] },
        { row: 'D', tier: 'FIRST BALCONY', price: 1800, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13], sold: [4, 9, 10] },
        { row: 'E', tier: 'UPPER BALCONY', price: 1200, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14], sold: [2, 13] },
        { row: 'F', tier: 'UPPER BALCONY', price: 1200, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14], sold: [6, 7] }
      ]
    };
  }
  // 5. Hamlet: A Modernist Noir Staging — Prithvi Theatre
  else if (title.includes('hamlet') || title.includes('prithvi') || title.includes('theatre')) {
    rawLayout = {
      stageLabel: '🎭 PRITHVI THRUST DRAMA STAGE 🎭',
      stageType: 'intimate',
      rows: [
        { row: 'A', tier: 'FRONT STAGE CLOSE-UP', price: 1200, seats: [1, 2, 3, 4, 5, 6, 7, 8], sold: [1, 4] },
        { row: 'B', tier: 'FRONT STAGE CLOSE-UP', price: 1200, seats: [1, 2, 3, 4, 5, 6, 7, 8], sold: [2, 7] },
        { row: 'C', tier: 'ELEVATED TIER 1', price: 950, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], sold: [3, 8] },
        { row: 'D', tier: 'ELEVATED TIER 1', price: 950, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], sold: [5, 6] },
        { row: 'E', tier: 'GALLERY REAR', price: 800, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], sold: [9, 10] }
      ]
    };
  }
  // 6. Custom Organizer Event or Generic Show
  else if (event.tiers && Array.isArray(event.tiers) && event.tiers.length > 0) {
    const customRows = [];
    const letters = ['A', 'B', 'C', 'D', 'E', 'F'];
    event.tiers.forEach((t, idx) => {
      const rowLetter = letters[idx * 2] || String.fromCharCode(65 + idx);
      const rowLetter2 = letters[idx * 2 + 1];
      customRows.push({
        row: rowLetter,
        tier: t.name,
        price: t.price,
        seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
        sold: [2, 7]
      });
      if (rowLetter2) {
        customRows.push({
          row: rowLetter2,
          tier: t.name,
          price: t.price,
          seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
          sold: [4, 9]
        });
      }
    });
    rawLayout = {
      stageLabel: stage || 'MAIN STAGE',
      stageType: 'curved',
      rows: customRows
    };
  } else {
    rawLayout = {
      stageLabel: stage,
      stageType: 'curved',
      rows: getDefaultRows(base)
    };
  }

  // Merge strictly this event's persistent user-booked seats
  const userBookedSeatIds = getBookedSeatsForEvent(eventId);
  const isolatedRows = rawLayout.rows.map(rowObj => {
    const bookedInRow = userBookedSeatIds
      .filter(id => id.startsWith(rowObj.row))
      .map(id => parseInt(id.replace(rowObj.row, ''), 10))
      .filter(num => !isNaN(num));

    const mergedSold = Array.from(new Set([...(rowObj.sold || []), ...bookedInRow]));
    return {
      ...rowObj,
      sold: mergedSold
    };
  });

  return {
    ...rawLayout,
    rows: isolatedRows
  };
}

function getDefaultRows(basePrice) {
  const vipPrice = Math.round(basePrice * 2.2);
  const execPrice = Math.round(basePrice * 1.5);
  const stdPrice = Math.round(basePrice);

  return [
    { row: 'A', tier: 'VIP PREFERRED FRONT', price: vipPrice, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], sold: [3, 8] },
    { row: 'B', tier: 'VIP PREFERRED FRONT', price: vipPrice, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], sold: [1, 6] },
    { row: 'C', tier: 'EXECUTIVE MIDDLE', price: execPrice, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12], sold: [4, 7] },
    { row: 'D', tier: 'EXECUTIVE MIDDLE', price: execPrice, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12], sold: [2, 10] },
    { row: 'E', tier: 'STANDARD GENERAL', price: stdPrice, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12], sold: [5, 11] },
    { row: 'F', tier: 'STANDARD GENERAL', price: stdPrice, seats: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12], sold: [1, 12] }
  ];
}
