import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import BrandLogo from './BrandLogo';

export default function Navbar({
  selectedCity = 'Mumbai',
  onSelectCity,
  searchQuery = '',
  onSearchChange,
  onOpenSignIn,
  onOpenAiAssistant,
  currentUser,
  onSignOut
}) {
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const activeCategory = searchParams.get('category') || 'ALL';
  const [isProfileMenuOpen, setIsProfileMenuOpen] = React.useState(false);

  return (
    <header style={{
      position: 'sticky',
      top: 0,
      zIndex: 50,
      backgroundColor: '#FFFFFF',
      borderBottom: '1px solid var(--color-border)',
      boxShadow: 'var(--shadow-subtle)'
    }}>
      {/* 1. Top Primary Navigation Bar */}
      <div className="site-container" style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        height: '68px',
        gap: '24px'
      }}>
        {/* Left: Brand Logo & Desktop Search */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '28px', flex: 1 }}>
          <BrandLogo height={38} />

          {/* Desktop Search Bar */}
          <div style={{
            display: 'none',
            maxWidth: '520px',
            width: '100%',
            position: 'relative',
            alignItems: 'center'
          }} className="desktop-search-wrapper">
            <span
              className="material-symbols-outlined"
              style={{
                position: 'absolute',
                left: '14px',
                color: 'var(--color-text-secondary)',
                fontSize: '20px',
                pointerEvents: 'none'
              }}
            >
              search
            </span>
            <input
              type="text"
              placeholder="Search for Events, Plays, Sports and Activities"
              value={searchQuery}
              onChange={(e) => onSearchChange && onSearchChange(e.target.value)}
              style={{
                width: '100%',
                height: '42px',
                paddingLeft: '44px',
                paddingRight: '16px',
                backgroundColor: 'var(--color-surface)',
                border: '1px solid var(--color-border)',
                borderRadius: 'var(--radius-full)',
                fontSize: '13px',
                color: 'var(--color-text-main)',
                outline: 'none',
                transition: 'all 150ms ease'
              }}
              onFocus={(e) => {
                e.target.style.backgroundColor = '#FFFFFF';
                e.target.style.borderColor = 'var(--color-text-main)';
              }}
              onBlur={(e) => {
                e.target.style.backgroundColor = 'var(--color-surface)';
                e.target.style.borderColor = 'var(--color-border)';
              }}
            />
          </div>
        </div>

        {/* Right: Location Selector, AI Assistant & Sign In */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
          {/* Location Selector */}
          <button
            onClick={() => onSelectCity && onSelectCity()}
            className="btn btn-ghost btn-sm"
            style={{
              padding: '6px 12px',
              borderRadius: 'var(--radius-full)',
              color: 'var(--color-text-main)',
              fontWeight: 500
            }}
          >
            <span className="material-symbols-outlined" style={{ color: 'var(--color-primary)', fontSize: '18px' }}>
              location_on
            </span>
            <span>{selectedCity}</span>
            <span className="material-symbols-outlined" style={{ fontSize: '16px', color: 'var(--color-text-secondary)' }}>
              keyboard_arrow_down
            </span>
          </button>

          {/* AI Event Assistant Launcher Pill */}
          <button
            onClick={onOpenAiAssistant}
            className="btn btn-outline-primary btn-sm"
            style={{ borderRadius: 'var(--radius-full)' }}
            title="Ask EventPulse Assistant"
          >
            <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>
              smart_toy
            </span>
            <span className="hide-on-mobile">Ask AI</span>
          </button>

          {/* User Profile or Sign In Button */}
          {currentUser ? (
            <div style={{ position: 'relative' }}>
              <button
                onClick={() => setIsProfileMenuOpen(!isProfileMenuOpen)}
                className="btn btn-secondary btn-sm"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  padding: '4px 12px',
                  borderRadius: 'var(--radius-full)',
                  border: '1px solid var(--color-border)'
                }}
              >
                {currentUser.avatarUrl ? (
                  <img src={currentUser.avatarUrl} alt="Avatar" style={{ width: '24px', height: '24px', borderRadius: '50%', objectFit: 'cover' }} />
                ) : (
                  <div style={{
                    width: '24px',
                    height: '24px',
                    borderRadius: '50%',
                    backgroundColor: 'var(--color-primary)',
                    color: '#FFFFFF',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '11px',
                    fontWeight: 800
                  }}>
                    {currentUser.name ? currentUser.name.charAt(0).toUpperCase() : 'U'}
                  </div>
                )}
                <span style={{ fontWeight: 700, fontSize: '13px', color: 'var(--color-text-main)' }}>
                  {currentUser.name}
                </span>
                <span className="material-symbols-outlined" style={{ fontSize: '18px', color: 'var(--color-text-secondary)' }}>
                  keyboard_arrow_down
                </span>
              </button>

              {isProfileMenuOpen && (
                <div style={{
                  position: 'absolute',
                  top: 'calc(100% + 8px)',
                  right: 0,
                  backgroundColor: '#FFFFFF',
                  borderRadius: 'var(--radius-lg)',
                  border: '1px solid var(--color-border)',
                  boxShadow: 'var(--shadow-modal)',
                  width: '240px',
                  zIndex: 1000,
                  padding: '12px',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '6px'
                }}>
                  <div style={{ paddingBottom: '8px', borderBottom: '1px solid var(--color-border-subtle)' }}>
                    <div style={{ fontSize: '13px', fontWeight: 800, color: 'var(--color-text-main)' }}>{currentUser.name}</div>
                    <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)', wordBreak: 'break-all', marginTop: '2px' }}>{currentUser.email}</div>
                  </div>
                  <Link
                    to="/my-tickets"
                    onClick={() => setIsProfileMenuOpen(false)}
                    style={{ textDecoration: 'none', color: 'var(--color-text-main)', fontSize: '12px', padding: '6px 8px', borderRadius: '4px', display: 'flex', alignItems: 'center', gap: '6px' }}
                  >
                    <span className="material-symbols-outlined" style={{ fontSize: '16px', color: 'var(--color-primary)' }}>confirmation_number</span>
                    <span>My Booking Wallet</span>
                  </Link>
                  <button
                    onClick={() => {
                      setIsProfileMenuOpen(false);
                      if (onSignOut) onSignOut();
                    }}
                    style={{ background: 'none', border: 'none', color: '#DC2626', fontSize: '12px', fontWeight: 600, padding: '6px 8px', textAlign: 'left', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px' }}
                  >
                    <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>logout</span>
                    <span>Sign Out</span>
                  </button>
                </div>
              )}
            </div>
          ) : (
            <button
              onClick={onOpenSignIn}
              className="btn btn-primary btn-sm"
              style={{ fontWeight: 700 }}
            >
              Sign In
            </button>
          )}
        </div>
      </div>

      {/* 2. Secondary Category Sub-Nav Bar (Perfect Alignment with site-container) */}
      <div style={{
        backgroundColor: 'var(--color-surface)',
        borderTop: '1px solid var(--color-border-subtle)'
      }}>
        <div className="site-container no-scrollbar" style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          overflowX: 'auto',
          height: '42px',
          paddingTop: '0',
          paddingBottom: '0'
        }}>
          <nav style={{ display: 'flex', alignItems: 'center', gap: '22px', whiteSpace: 'nowrap' }}>
            <Link
              to="/"
              style={{
                fontSize: '13px',
                fontWeight: activeCategory === 'ALL' && location.pathname === '/' ? 700 : 500,
                color: activeCategory === 'ALL' && location.pathname === '/' ? 'var(--color-primary)' : 'var(--color-text-secondary)',
                textDecoration: 'none'
              }}
            >
              All Events
            </Link>
            <Link
              to="/?category=CONCERT"
              style={{
                fontSize: '13px',
                fontWeight: activeCategory === 'CONCERT' ? 700 : 500,
                color: activeCategory === 'CONCERT' ? 'var(--color-primary)' : 'var(--color-text-secondary)',
                textDecoration: 'none'
              }}
            >
              Concerts
            </Link>
            <Link
              to="/?category=THEATRE"
              style={{
                fontSize: '13px',
                fontWeight: activeCategory === 'THEATRE' ? 700 : 500,
                color: activeCategory === 'THEATRE' ? 'var(--color-primary)' : 'var(--color-text-secondary)',
                textDecoration: 'none'
              }}
            >
              Plays
            </Link>
            <Link
              to="/?category=COMEDY"
              style={{
                fontSize: '13px',
                fontWeight: activeCategory === 'COMEDY' ? 700 : 500,
                color: activeCategory === 'COMEDY' ? 'var(--color-primary)' : 'var(--color-text-secondary)',
                textDecoration: 'none'
              }}
            >
              Comedy
            </Link>
            <Link
              to="/my-tickets"
              style={{
                fontSize: '13px',
                fontWeight: location.pathname === '/my-tickets' ? 700 : 500,
                color: location.pathname === '/my-tickets' ? 'var(--color-primary)' : 'var(--color-text-secondary)',
                textDecoration: 'none'
              }}
            >
              My Tickets
            </Link>
          </nav>

          <div style={{ display: 'flex', alignItems: 'center', gap: '14px', whiteSpace: 'nowrap' }}>
            <Link
              to="/organizer/create"
              style={{
                fontSize: '12px',
                fontWeight: 600,
                color: 'var(--color-primary)',
                textDecoration: 'none',
                display: 'flex',
                alignItems: 'center',
                gap: '4px'
              }}
            >
              <span className="material-symbols-outlined" style={{ fontSize: '15px' }}>add_circle</span>
              <span>List Your Show</span>
            </Link>
          </div>
        </div>
      </div>

      <style>{`
        @media (min-width: 768px) {
          .desktop-search-wrapper {
            display: flex !important;
          }
        }
        @media (max-width: 640px) {
          .hide-on-mobile {
            display: none !important;
          }
        }
      `}</style>
    </header>
  );
}
