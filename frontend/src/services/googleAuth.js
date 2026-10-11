// Official Google Identity Services & OAuth2 Client for EventPulse
// Uses Google Client ID and requests email, profile, and calendar access

export const GOOGLE_CLIENT_ID =
  import.meta.env.VITE_GOOGLE_CLIENT_ID ||
  '158914712095-2v1d0f1j3bvkhst9tdcebm9hujhhlr11.apps.googleusercontent.com';

export const GoogleAuthService = {
  getClientId() {
    return GOOGLE_CLIENT_ID;
  },

  isGoogleScriptReady() {
    return Boolean(window.google?.accounts?.oauth2);
  },

  // Launch official Google OAuth2 Token Client requesting Profile, Email & Google Calendar Access
  launchGoogleOAuth({ onSuccess, onError, onFallback }) {
    if (!window.google?.accounts?.oauth2) {
      console.warn('[EventPulse Google Auth] Google SDK not yet ready, using interactive modal.');
      if (onFallback) onFallback();
      return;
    }

    try {
      const tokenClient = window.google.accounts.oauth2.initTokenClient({
        client_id: GOOGLE_CLIENT_ID,
        scope: 'email profile https://www.googleapis.com/auth/calendar.events',
        prompt: 'consent',
        callback: async (response) => {
          if (response.error) {
            console.warn('[EventPulse Google Auth Error]:', response.error);
            if (onError) onError(new Error(response.error_description || response.error));
            return;
          }

          if (response.access_token) {
            try {
              // Fetch verified user profile directly from Google
              const res = await fetch('https://www.googleapis.com/oauth2/v3/userinfo', {
                headers: {
                  Authorization: `Bearer ${response.access_token}`
                }
              });

              if (!res.ok) throw new Error('Failed to retrieve user profile from Google.');
              const profile = await res.json();

              onSuccess({
                name: profile.name || 'Google User',
                email: profile.email,
                avatarUrl: profile.picture || null,
                accessToken: response.access_token,
                autoSyncCalendar: true
              });
            } catch (fetchErr) {
              console.warn('[EventPulse Google Profile Fetch Warning]:', fetchErr);
              if (onError) onError(fetchErr);
            }
          }
        }
      });

      tokenClient.requestAccessToken({ prompt: 'consent' });
    } catch (initErr) {
      console.warn('[EventPulse Google Init Notice]:', initErr);
      if (onFallback) onFallback();
      else if (onError) onError(initErr);
    }
  }
};
