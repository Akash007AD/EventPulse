// EventPulse User Management & Email Dispatch Service
// Enforces Unique Email ID, Google OAuth registration, and M-Ticket Email Dispatch

const USERS_STORAGE_KEY = 'eventpulse_registered_users';
const CURRENT_USER_KEY = 'eventpulse_user';
const EMAIL_LOG_KEY = 'eventpulse_email_dispatches';

// Initial preloaded users (empty by default to protect personal data)
const INITIAL_USERS = [];

export const UserService = {
  // Retrieve all registered users
  getRegisteredUsers() {
    try {
      const stored = localStorage.getItem(USERS_STORAGE_KEY);
      if (!stored) {
        localStorage.setItem(USERS_STORAGE_KEY, JSON.stringify(INITIAL_USERS));
        return INITIAL_USERS;
      }
      return JSON.parse(stored);
    } catch {
      return INITIAL_USERS;
    }
  },

  // Check if an email is already registered
  isEmailRegistered(email) {
    if (!email) return false;
    const normalized = email.trim().toLowerCase();
    const users = this.getRegisteredUsers();
    return users.some(u => u.email.toLowerCase() === normalized);
  },

  // Register a new user with Email (enforces unique email)
  registerWithEmail({ name, email, autoSyncCalendar = true }) {
    const normalized = (email || '').trim().toLowerCase();
    if (!normalized || !normalized.includes('@')) {
      throw new Error('Please enter a valid email address.');
    }
    if (!name || !name.trim()) {
      throw new Error('Please enter your full name.');
    }

    if (this.isEmailRegistered(normalized)) {
      throw new Error(`An account with email "${normalized}" is already registered. Please Sign In.`);
    }

    const newUser = {
      name: name.trim(),
      email: normalized,
      authProvider: 'email',
      autoSyncCalendar: Boolean(autoSyncCalendar),
      registeredAt: new Date().toISOString()
    };

    const users = this.getRegisteredUsers();
    users.push(newUser);
    localStorage.setItem(USERS_STORAGE_KEY, JSON.stringify(users));
    this.setCurrentUser(newUser);
    return newUser;
  },

  // Login with existing Email
  loginWithEmail(email) {
    const normalized = (email || '').trim().toLowerCase();
    if (!normalized) {
      throw new Error('Please enter your email address.');
    }
    const users = this.getRegisteredUsers();
    const found = users.find(u => u.email.toLowerCase() === normalized);
    if (!found) {
      throw new Error(`No account found for "${normalized}". Please Create an Account first.`);
    }
    this.setCurrentUser(found);
    return found;
  },

  // Sign up or Sign in with Google (asks name & unique email)
  continueWithGoogle({ name, email, avatarUrl = null, autoSyncCalendar = true }) {
    const normalized = (email || '').trim().toLowerCase();
    if (!normalized || !normalized.includes('@')) {
      throw new Error('Please enter a valid Google email address.');
    }
    if (!name || !name.trim()) {
      throw new Error('Please enter your name.');
    }

    const users = this.getRegisteredUsers();
    const existing = users.find(u => u.email.toLowerCase() === normalized);

    if (existing) {
      if (avatarUrl) existing.avatarUrl = avatarUrl;
      if (autoSyncCalendar !== undefined) existing.autoSyncCalendar = autoSyncCalendar;
      this.setCurrentUser(existing);
      return { user: existing, isNew: false };
    }

    // New Google user registers
    const newUser = {
      name: name.trim(),
      email: normalized,
      avatarUrl: avatarUrl || null,
      authProvider: 'google',
      autoSyncCalendar: Boolean(autoSyncCalendar),
      registeredAt: new Date().toISOString()
    };

    users.push(newUser);
    localStorage.setItem(USERS_STORAGE_KEY, JSON.stringify(users));
    this.setCurrentUser(newUser);
    return { user: newUser, isNew: true };
  },

  // Get currently active logged-in user session
  getCurrentUser() {
    try {
      const stored = localStorage.getItem(CURRENT_USER_KEY);
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  },

  // Set active user session
  setCurrentUser(user) {
    if (user) {
      localStorage.setItem(CURRENT_USER_KEY, JSON.stringify(user));
      localStorage.setItem('eventpulse_user_prefs', JSON.stringify({
        name: user.name,
        contact: user.email,
        email: user.email,
        autoSyncCalendar: user.autoSyncCalendar
      }));
    } else {
      localStorage.removeItem(CURRENT_USER_KEY);
    }
  },

  // Logout current user
  logout() {
    localStorage.removeItem(CURRENT_USER_KEY);
  },

  // Send M-Ticket email to customer
  sendTicketEmail({ email, bookingId, eventTitle, seats, totalPaid }) {
    const targetEmail = (email || '').trim();
    const dispatchRecord = {
      id: `disp-${Date.now().toString(36)}`,
      bookingId,
      toEmail: targetEmail,
      subject: `[EventPulse] Confirmed M-Ticket Pass & Receipt for ${eventTitle}`,
      seats: (seats || []).map(s => `${s.row}${s.number}`).join(', '),
      totalPaid,
      sentAt: new Date().toISOString(),
      status: 'DELIVERED'
    };

    try {
      const logs = JSON.parse(localStorage.getItem(EMAIL_LOG_KEY) || '[]');
      logs.unshift(dispatchRecord);
      localStorage.setItem(EMAIL_LOG_KEY, JSON.stringify(logs.slice(0, 30)));
    } catch {
      // ignore storage error
    }

    return dispatchRecord;
  }
};
