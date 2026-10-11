import React, { useState } from 'react';
import { UserService } from '../services/userService';
import { GoogleAuthService, GOOGLE_CLIENT_ID } from '../services/googleAuth';

export default function SignInModal({ isOpen, onClose, onLoginSuccess }) {
  // Modes: 'EMAIL_LOGIN' | 'EMAIL_SIGNUP' | 'GOOGLE_PROMPT' | 'CALENDAR_ACCESS_POPUP'
  const [authMode, setAuthMode] = useState('EMAIL_LOGIN');

  // Clean Form Fields (No hardcoded personal info)
  const [nameInput, setNameInput] = useState('');
  const [emailInput, setEmailInput] = useState('');
  const [googleName, setGoogleName] = useState('');
  const [googleEmail, setGoogleEmail] = useState('');

  // Pending user while asking for calendar permission
  const [pendingUserData, setPendingUserData] = useState(null);

  // Error & Status Feedback
  const [errorMessage, setErrorMessage] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  if (!isOpen) return null;

  const handleEmailSubmit = (e) => {
    e.preventDefault();
    setErrorMessage(null);

    try {
      if (authMode === 'EMAIL_SIGNUP') {
        const normalized = (emailInput || '').trim().toLowerCase();
        if (!normalized || !normalized.includes('@')) {
          throw new Error('Please enter a valid email address.');
        }
        if (!nameInput || !nameInput.trim()) {
          throw new Error('Please enter your full name.');
        }

        // Validate unique email before proceeding to calendar popup
        if (UserService.isEmailRegistered(normalized)) {
          throw new Error(`An account with email "${normalized}" is already registered. Please Sign In.`);
        }

        // Stage user and show dedicated Calendar Access Popup
        setPendingUserData({
          type: 'EMAIL',
          name: nameInput.trim(),
          email: normalized
        });
        setAuthMode('CALENDAR_ACCESS_POPUP');
      } else {
        // Email Login
        const user = UserService.loginWithEmail(emailInput);
        setSuccessMessage(`Welcome back, ${user.name}!`);
        setTimeout(() => {
          if (onLoginSuccess) onLoginSuccess(user);
          onClose();
        }, 700);
      }
    } catch (err) {
      setErrorMessage(err.message);
    }
  };

  const handleGoogleClick = () => {
    setErrorMessage(null);

    // Launch official Google OAuth2 Token Client requesting Profile, Email & Calendar access
    GoogleAuthService.launchGoogleOAuth({
      onSuccess: (googleProfile) => {
        try {
          const { user, isNew } = UserService.continueWithGoogle({
            name: googleProfile.name,
            email: googleProfile.email,
            avatarUrl: googleProfile.avatarUrl,
            autoSyncCalendar: true
          });

          // Stage user and show Calendar Access confirmation popup
          setPendingUserData({
            type: 'GOOGLE',
            user,
            isNew
          });
          setAuthMode('CALENDAR_ACCESS_POPUP');
        } catch (err) {
          setErrorMessage(err.message);
        }
      },
      onError: (err) => {
        console.warn('Google OAuth popup not active, switching to manual verification:', err.message);
        setAuthMode('GOOGLE_PROMPT');
      },
      onFallback: () => {
        setAuthMode('GOOGLE_PROMPT');
      }
    });
  };

  const handleGoogleManualSubmit = (e) => {
    e.preventDefault();
    setErrorMessage(null);

    try {
      const normalized = (googleEmail || '').trim().toLowerCase();
      if (!normalized || !normalized.includes('@')) {
        throw new Error('Please enter a valid Google email address.');
      }
      if (!googleName || !googleName.trim()) {
        throw new Error('Please enter your full name.');
      }

      setPendingUserData({
        type: 'GOOGLE_MANUAL',
        name: googleName.trim(),
        email: normalized
      });
      setAuthMode('CALENDAR_ACCESS_POPUP');
    } catch (err) {
      setErrorMessage(err.message);
    }
  };

  const handleFinalizeCalendarPermission = (grantAccess) => {
    try {
      let finalUser;
      if (!pendingUserData) return;

      if (pendingUserData.type === 'EMAIL') {
        finalUser = UserService.registerWithEmail({
          name: pendingUserData.name,
          email: pendingUserData.email,
          autoSyncCalendar: grantAccess
        });
      } else if (pendingUserData.type === 'GOOGLE_MANUAL') {
        const { user } = UserService.continueWithGoogle({
          name: pendingUserData.name,
          email: pendingUserData.email,
          autoSyncCalendar: grantAccess
        });
        finalUser = user;
      } else if (pendingUserData.type === 'GOOGLE') {
        finalUser = pendingUserData.user;
        if (finalUser) {
          finalUser.autoSyncCalendar = grantAccess;
          UserService.setCurrentUser(finalUser);
        }
      }

      setSuccessMessage(
        grantAccess
          ? `Welcome to EventPulse, ${finalUser?.name || 'User'}! Google Calendar sync enabled.`
          : `Welcome to EventPulse, ${finalUser?.name || 'User'}!`
      );

      setTimeout(() => {
        if (onLoginSuccess && finalUser) onLoginSuccess(finalUser);
        onClose();
      }, 700);
    } catch (err) {
      setErrorMessage(err.message);
      setAuthMode('EMAIL_SIGNUP');
    }
  };

  return (
    <div style={{
      position: 'fixed',
      inset: 0,
      backgroundColor: 'rgba(26, 26, 36, 0.65)',
      backdropFilter: 'blur(4px)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 1050,
      padding: '20px'
    }}>
      <div style={{
        maxWidth: '460px',
        width: '100%',
        backgroundColor: '#FFFFFF',
        borderRadius: 'var(--radius-xl)',
        boxShadow: 'var(--shadow-modal)',
        padding: '32px',
        position: 'relative'
      }}>
        {/* Close Button */}
        <button
          onClick={onClose}
          style={{
            position: 'absolute',
            top: '20px',
            right: '20px',
            background: 'none',
            border: 'none',
            cursor: 'pointer',
            color: 'var(--color-text-secondary)'
          }}
        >
          <span className="material-symbols-outlined" style={{ fontSize: '22px' }}>close</span>
        </button>

        {/* Modal Header */}
        <div style={{ textAlign: 'center', marginBottom: '20px' }}>
          <h2 style={{ fontSize: '22px', fontWeight: 800, color: 'var(--color-text-main)', marginBottom: '4px' }}>
            {authMode === 'CALENDAR_ACCESS_POPUP'
              ? 'Google Calendar Sync'
              : authMode === 'GOOGLE_PROMPT'
              ? 'Google Sign In / Sign Up'
              : authMode === 'EMAIL_SIGNUP'
              ? 'Create Your Account'
              : 'Welcome to EventPulse'}
          </h2>
          <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', lineHeight: 1.4 }}>
            {authMode === 'CALENDAR_ACCESS_POPUP'
              ? 'Allow EventPulse to automatically add show dates, venue gates, and calendar alerts.'
              : authMode === 'GOOGLE_PROMPT'
              ? 'Connect using your Google account to receive instant verified M-Tickets.'
              : 'Sign in with Email or Google to unlock instant M-Tickets and event access.'}
          </p>
        </div>

        {/* Error Feedback Banner */}
        {errorMessage && (
          <div style={{
            padding: '10px 14px',
            backgroundColor: '#FEF2F2',
            border: '1px solid #FCA5A5',
            borderRadius: 'var(--radius-md)',
            color: '#991B1B',
            fontSize: '12px',
            marginBottom: '16px',
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}>
            <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>error</span>
            <span>{errorMessage}</span>
          </div>
        )}

        {/* Success Feedback Banner */}
        {successMessage && (
          <div style={{
            padding: '10px 14px',
            backgroundColor: '#F0FDF4',
            border: '1px solid #BBF7D0',
            borderRadius: 'var(--radius-md)',
            color: '#166534',
            fontSize: '12px',
            marginBottom: '16px',
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}>
            <span className="material-symbols-outlined" style={{ fontSize: '16px', color: '#16A34A' }}>check_circle</span>
            <span>{successMessage}</span>
          </div>
        )}

        {/* 1. DEDICATED CALENDAR ACCESS POPUP / STEP */}
        {authMode === 'CALENDAR_ACCESS_POPUP' ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
            <div style={{
              padding: '16px',
              backgroundColor: '#EFF6FF',
              borderRadius: 'var(--radius-lg)',
              border: '1px solid #BFDBFE',
              textAlign: 'center'
            }}>
              <div style={{
                width: '48px',
                height: '48px',
                borderRadius: '50%',
                backgroundColor: '#DBEAFE',
                color: '#2563EB',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                margin: '0 auto 10px'
              }}>
                <span className="material-symbols-outlined" style={{ fontSize: '28px' }}>calendar_month</span>
              </div>
              <div style={{ fontSize: '15px', fontWeight: 800, color: '#1E40AF', marginBottom: '6px' }}>
                Sync with Google Calendar?
              </div>
              <p style={{ fontSize: '12px', color: '#1E3A8A', lineHeight: 1.4, margin: 0 }}>
                Automatically schedule booked concerts, venue entry gates, and reminder alerts directly into your personal Google Calendar.
              </p>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '12px', color: 'var(--color-text-secondary)', padding: '0 8px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span className="material-symbols-outlined" style={{ fontSize: '16px', color: '#16A34A' }}>check</span>
                <span>Automatic show reminders 2 hours before gate opening</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span className="material-symbols-outlined" style={{ fontSize: '16px', color: '#16A34A' }}>check</span>
                <span>Direct link to M-Ticket pass inside your calendar event</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span className="material-symbols-outlined" style={{ fontSize: '16px', color: '#16A34A' }}>check</span>
                <span>Venue map navigation coordinates synced seamlessly</span>
              </div>
            </div>

            <button
              onClick={() => handleFinalizeCalendarPermission(true)}
              className="btn btn-primary"
              style={{ width: '100%', justifyContent: 'center', padding: '12px', fontWeight: 800 }}
            >
              <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>event_available</span>
              <span>Allow Google Calendar Access</span>
            </button>

            <button
              onClick={() => handleFinalizeCalendarPermission(false)}
              className="btn btn-ghost"
              style={{ width: '100%', justifyContent: 'center', fontSize: '12px', color: 'var(--color-text-secondary)' }}
            >
              Skip for Now
            </button>
          </div>
        ) : authMode === 'GOOGLE_PROMPT' ? (
          /* 2. GOOGLE VERIFICATION FALLBACK (CLEAN INPUTS, NO INFO DISCLOSURE) */
          <form onSubmit={handleGoogleManualSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{
              padding: '12px',
              backgroundColor: '#F8FAFC',
              borderRadius: 'var(--radius-lg)',
              border: '1px solid #E2E8F0',
              display: 'flex',
              alignItems: 'center',
              gap: '12px'
            }}>
              <div style={{
                width: '36px',
                height: '36px',
                borderRadius: '50%',
                backgroundColor: '#FFFFFF',
                border: '1px solid #CBD5E1',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '16px',
                fontWeight: 800,
                color: '#4285F4'
              }}>
                G
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--color-text-main)' }}>
                  Google Client Authentication
                </div>
                <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)' }}>
                  Client ID: {GOOGLE_CLIENT_ID ? `${GOOGLE_CLIENT_ID.slice(0, 16)}...` : 'Configured'}
                </div>
              </div>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: 'var(--color-text-secondary)', marginBottom: '6px', textTransform: 'uppercase' }}>
                Full Name
              </label>
              <input
                type="text"
                placeholder="Enter your full name"
                value={googleName}
                onChange={(e) => setGoogleName(e.target.value)}
                required
                style={{
                  width: '100%',
                  padding: '10px 14px',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid var(--color-border-strong)',
                  fontSize: '14px',
                  outline: 'none'
                }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: 'var(--color-text-secondary)', marginBottom: '6px', textTransform: 'uppercase' }}>
                Google Email Address (Unique)
              </label>
              <input
                type="email"
                placeholder="name@gmail.com"
                value={googleEmail}
                onChange={(e) => setGoogleEmail(e.target.value)}
                required
                style={{
                  width: '100%',
                  padding: '10px 14px',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid var(--color-border-strong)',
                  fontSize: '14px',
                  outline: 'none'
                }}
              />
            </div>

            <button
              type="submit"
              className="btn btn-primary"
              style={{ width: '100%', justifyContent: 'center', padding: '12px', fontWeight: 800 }}
            >
              Continue with Google Account
            </button>

            <button
              type="button"
              onClick={() => { setAuthMode('EMAIL_LOGIN'); setErrorMessage(null); }}
              className="btn btn-ghost"
              style={{ width: '100%', justifyContent: 'center', fontSize: '12px', color: 'var(--color-text-secondary)' }}
            >
              &larr; Back to Email Sign In
            </button>
          </form>
        ) : (
          /* 3. EMAIL SIGN IN & SIGN UP */
          <div>
            {/* Mode Switcher Tabs */}
            <div style={{
              display: 'flex',
              borderBottom: '1px solid var(--color-border)',
              marginBottom: '20px'
            }}>
              <button
                type="button"
                onClick={() => { setAuthMode('EMAIL_LOGIN'); setErrorMessage(null); }}
                style={{
                  flex: 1,
                  padding: '10px',
                  border: 'none',
                  background: 'none',
                  fontSize: '14px',
                  fontWeight: 700,
                  cursor: 'pointer',
                  color: authMode === 'EMAIL_LOGIN' ? 'var(--color-primary)' : 'var(--color-text-secondary)',
                  borderBottom: `2px solid ${authMode === 'EMAIL_LOGIN' ? 'var(--color-primary)' : 'transparent'}`
                }}
              >
                Sign In
              </button>
              <button
                type="button"
                onClick={() => { setAuthMode('EMAIL_SIGNUP'); setErrorMessage(null); }}
                style={{
                  flex: 1,
                  padding: '10px',
                  border: 'none',
                  background: 'none',
                  fontSize: '14px',
                  fontWeight: 700,
                  cursor: 'pointer',
                  color: authMode === 'EMAIL_SIGNUP' ? 'var(--color-primary)' : 'var(--color-text-secondary)',
                  borderBottom: `2px solid ${authMode === 'EMAIL_SIGNUP' ? 'var(--color-primary)' : 'transparent'}`
                }}
              >
                Create Account (Sign Up)
              </button>
            </div>

            {/* Email Form */}
            <form onSubmit={handleEmailSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              {authMode === 'EMAIL_SIGNUP' && (
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: 'var(--color-text-secondary)', marginBottom: '6px', textTransform: 'uppercase' }}>
                    Full Name
                  </label>
                  <input
                    type="text"
                    placeholder="Enter your full name"
                    value={nameInput}
                    onChange={(e) => setNameInput(e.target.value)}
                    required
                    style={{
                      width: '100%',
                      padding: '10px 14px',
                      borderRadius: 'var(--radius-md)',
                      border: '1px solid var(--color-border-strong)',
                      fontSize: '14px',
                      outline: 'none'
                    }}
                  />
                </div>
              )}

              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: 'var(--color-text-secondary)', marginBottom: '6px', textTransform: 'uppercase' }}>
                  Email Address {authMode === 'EMAIL_SIGNUP' && '(Unique)'}
                </label>
                <input
                  type="email"
                  placeholder="name@example.com"
                  value={emailInput}
                  onChange={(e) => setEmailInput(e.target.value)}
                  required
                  style={{
                    width: '100%',
                    padding: '10px 14px',
                    borderRadius: 'var(--radius-md)',
                    border: '1px solid var(--color-border-strong)',
                    fontSize: '14px',
                    outline: 'none'
                  }}
                />
              </div>

              <button
                type="submit"
                className="btn btn-primary"
                style={{ width: '100%', justifyContent: 'center', padding: '12px', fontWeight: 800 }}
              >
                {authMode === 'EMAIL_SIGNUP' ? 'Continue with Email' : 'Sign In with Email'}
              </button>
            </form>

            {/* Divider */}
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '12px',
              margin: '20px 0',
              color: 'var(--color-text-muted)',
              fontSize: '12px'
            }}>
              <span style={{ flex: 1, height: '1px', backgroundColor: 'var(--color-border)' }} />
              <span>OR</span>
              <span style={{ flex: 1, height: '1px', backgroundColor: 'var(--color-border)' }} />
            </div>

            {/* Google Authentication Trigger Button */}
            <button
              type="button"
              onClick={handleGoogleClick}
              className="btn btn-secondary"
              style={{
                width: '100%',
                justifyContent: 'center',
                fontSize: '13px',
                borderRadius: 'var(--radius-md)',
                padding: '11px',
                fontWeight: 700,
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}
            >
              <svg width="18" height="18" viewBox="0 0 24 24">
                <path fill="#4285F4" d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v4.51h6.6c-.29 1.52-1.14 2.82-2.4 3.68v3.05h3.88c2.27-2.09 3.66-5.17 3.66-9.17z"/>
                <path fill="#34A853" d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.88-3.05c-1.08.72-2.45 1.16-4.05 1.16-3.12 0-5.77-2.1-6.72-4.93H1.25v3.15C3.26 21.36 7.33 24 12 24z"/>
                <path fill="#FBBC05" d="M5.28 14.27c-.25-.72-.38-1.49-.38-2.27s.13-1.55.38-2.27V6.58H1.25C.45 8.18 0 9.99 0 12s.45 3.82 1.25 5.42l4.03-3.15z"/>
                <path fill="#EA4335" d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.33 0 3.26 2.64 1.25 6.58l4.03 3.15c.95-2.83 3.6-4.98 6.72-4.98z"/>
              </svg>
              <span>Continue with Google</span>
            </button>
          </div>
        )}

        <div style={{ textAlign: 'center', fontSize: '11px', color: 'var(--color-text-muted)', marginTop: '20px', lineHeight: 1.5 }}>
          Official tickets &amp; QR access codes will be delivered automatically to your registered email.
        </div>
      </div>
    </div>
  );
}
