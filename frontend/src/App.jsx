import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import EventCard from './components/EventCard';
import EventDetailModal from './components/EventDetailModal';
import SeatSelector from './components/SeatSelector';
import CheckoutModal from './components/CheckoutModal';
import TicketModal from './components/TicketModal';
import GateScannerModal from './components/GateScannerModal';
import AiChatDrawer from './components/AiChatDrawer';
import LegalModal from './components/LegalModal';
import Footer from './components/Footer';
import { ApiService } from './services/api';

export default function App() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  // Navigation & View State
  const [currentView, setCurrentView] = useState('events'); // 'events' | 'seat-selector'
  const [selectedEvent, setSelectedEvent] = useState(null);
  
  // Modals
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [checkoutModalOpen, setCheckoutModalOpen] = useState(false);
  const [checkoutData, setCheckoutData] = useState(null);
  const [ticketModalOpen, setTicketModalOpen] = useState(false);
  const [confirmedBooking, setConfirmedBooking] = useState(null);
  const [gateModalOpen, setGateModalOpen] = useState(false);
  const [testGateToken, setTestGateToken] = useState('');
  const [legalModalOpen, setLegalModalOpen] = useState(false);
  const [aiDrawerOpen, setAiDrawerOpen] = useState(false);

  useEffect(() => {
    async function loadEvents() {
      setLoading(true);
      const data = await ApiService.getEvents();
      setEvents(data);
      setLoading(false);
    }
    loadEvents();
  }, []);

  // Filter events
  const filteredEvents = events.filter(e => {
    const matchesCat = selectedCategory === 'ALL' || e.category === selectedCategory;
    const matchesSearch = !searchQuery.trim() || 
      e.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      e.city.toLowerCase().includes(searchQuery.toLowerCase()) ||
      e.venueName.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesCat && matchesSearch;
  });

  const handleOpenEventDetail = (event) => {
    setSelectedEvent(event);
    setDetailModalOpen(true);
  };

  const handleStartSeatSelection = (event) => {
    setSelectedEvent(event);
    setDetailModalOpen(false);
    setCurrentView('seat-selector');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleProceedToCheckout = (data) => {
    setCheckoutData(data);
    setCheckoutModalOpen(true);
  };

  const handlePaymentSuccess = async (paymentDetails) => {
    setCheckoutModalOpen(false);

    // Call API / Simulation
    const booking = await ApiService.createBooking({
      eventId: selectedEvent.id,
      seatNumbers: paymentDetails.seatNumbers,
      totalAmount: paymentDetails.totalAmount,
      userId: 'usr-customer-akash'
    });

    setConfirmedBooking(booking);
    setTicketModalOpen(true);
  };

  const handleTestAtGate = (token) => {
    setTicketModalOpen(false);
    setTestGateToken(token);
    setGateModalOpen(true);
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Header Navigation */}
      <Navbar
        activeTab={currentView}
        onNavigate={(tab) => {
          setCurrentView(tab);
          if (tab === 'events') setSelectedEvent(null);
        }}
        onOpenAi={() => setAiDrawerOpen(!aiDrawerOpen)}
        onOpenGate={() => setGateModalOpen(true)}
      />

      {/* Main Content Area */}
      <main style={{ flex: 1 }}>
        {currentView === 'events' ? (
          <div>
            {/* Editorial Hero Section */}
            <section style={{
              backgroundColor: '#070A10',
              borderBottom: '1px solid var(--border-subtle)',
              padding: '64px 0 48px 0',
              position: 'relative'
            }}>
              <div className="container">
                <div style={{ maxWidth: '780px' }}>
                  <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
                    <span className="badge badge-amber">EDITORIAL SEATING PLATFORM</span>
                    <span className="badge">10-MIN DISTRIBUTED HOLDS</span>
                  </div>

                  <h1 style={{
                    fontSize: 'clamp(2rem, 5vw, 3.25rem)',
                    lineHeight: 1.15,
                    marginBottom: '18px',
                    fontWeight: 800,
                    letterSpacing: '-0.03em'
                  }}>
                    Live Performances, Curated Rosters & Reserved Seating.
                  </h1>

                  <p style={{
                    fontSize: '1.1rem',
                    color: 'var(--text-secondary)',
                    lineHeight: 1.6,
                    marginBottom: '32px'
                  }}>
                    Select exact venue seats with real-time distributed hold locks. Tickets are issued as cryptographically signed passes with seamless Google Calendar synchronization.
                  </p>

                  {/* Search and Filters Bar */}
                  <div style={{
                    display: 'flex',
                    flexWrap: 'wrap',
                    gap: '12px',
                    backgroundColor: 'var(--bg-card)',
                    border: '1px solid var(--border-medium)',
                    borderRadius: 'var(--radius-sm)',
                    padding: '8px 12px'
                  }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flex: '1 1 280px' }}>
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--text-muted)" strokeWidth="2">
                        <circle cx="11" cy="11" r="8" />
                        <line x1="21" y1="21" x2="16.65" y2="16.65" />
                      </svg>
                      <input
                        type="text"
                        value={searchQuery}
                        onChange={e => setSearchQuery(e.target.value)}
                        placeholder="Search by event title, hall, or city..."
                        style={{
                          width: '100%',
                          background: 'none',
                          border: 'none',
                          color: 'var(--text-main)',
                          fontSize: '0.9rem',
                          outline: 'none'
                        }}
                      />
                    </div>

                    {/* Category Filter Chips */}
                    <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                      {['ALL', 'CONCERT', 'THEATRE', 'TECH', 'COMEDY'].map(cat => (
                        <button
                          key={cat}
                          onClick={() => setSelectedCategory(cat)}
                          className={`btn btn-sm ${selectedCategory === cat ? 'btn-primary' : 'btn-outline'}`}
                          style={{
                            padding: '4px 10px',
                            fontSize: '0.75rem',
                            borderRadius: 'var(--radius-xs)'
                          }}
                        >
                          {cat}
                        </button>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
            </section>

            {/* Events Grid Section */}
            <section style={{ padding: '48px 0', backgroundColor: 'var(--bg-canvas)' }}>
              <div className="container">
                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  marginBottom: '28px',
                  borderBottom: '1px solid var(--border-subtle)',
                  paddingBottom: '14px'
                }}>
                  <div>
                    <h2 style={{ fontSize: '1.4rem' }}>Scheduled Events & Residencies</h2>
                    <div style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                      Showing {filteredEvents.length} live productions with active seat maps
                    </div>
                  </div>
                </div>

                {loading ? (
                  <div style={{ padding: '60px 0', textAlign: 'center', color: 'var(--text-muted)' }}>
                    Loading performance listings...
                  </div>
                ) : filteredEvents.length === 0 ? (
                  <div style={{
                    padding: '60px 20px',
                    textAlign: 'center',
                    backgroundColor: 'var(--bg-card)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: 'var(--radius-md)'
                  }}>
                    <p style={{ color: 'var(--text-secondary)', fontSize: '1rem' }}>
                      No events match your search query or filter.
                    </p>
                    <button
                      className="btn btn-outline"
                      onClick={() => { setSelectedCategory('ALL'); setSearchQuery(''); }}
                      style={{ marginTop: '16px' }}
                    >
                      Clear All Filters
                    </button>
                  </div>
                ) : (
                  <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))',
                    gap: '24px'
                  }}>
                    {filteredEvents.map(event => (
                      <EventCard
                        key={event.id}
                        event={event}
                        onSelect={handleOpenEventDetail}
                      />
                    ))}
                  </div>
                )}
              </div>
            </section>
          </div>
        ) : (
          /* Interactive Seat Selector View */
          <SeatSelector
            event={selectedEvent}
            onBack={() => {
              setCurrentView('events');
              setDetailModalOpen(true);
            }}
            onProceedToCheckout={handleProceedToCheckout}
          />
        )}
      </main>

      {/* Modals & Drawers */}
      {detailModalOpen && (
        <EventDetailModal
          event={selectedEvent}
          onClose={() => setDetailModalOpen(false)}
          onSelectSeats={handleStartSeatSelection}
        />
      )}

      {checkoutModalOpen && (
        <CheckoutModal
          event={selectedEvent}
          checkoutData={checkoutData}
          onClose={() => setCheckoutModalOpen(false)}
          onPaymentSuccess={handlePaymentSuccess}
        />
      )}

      {ticketModalOpen && (
        <TicketModal
          event={selectedEvent}
          booking={confirmedBooking}
          onClose={() => setTicketModalOpen(false)}
          onTestAtGate={handleTestAtGate}
        />
      )}

      {gateModalOpen && (
        <GateScannerModal
          initialToken={testGateToken}
          onClose={() => {
            setGateModalOpen(false);
            setTestGateToken('');
          }}
        />
      )}

      {legalModalOpen && (
        <LegalModal
          onClose={() => setLegalModalOpen(false)}
        />
      )}

      {/* Floating AI Support Drawer */}
      <AiChatDrawer
        isOpen={aiDrawerOpen}
        onClose={() => setAiDrawerOpen(false)}
      />

      {/* Floating AI Trigger Pill if closed */}
      {!aiDrawerOpen && (
        <button
          onClick={() => setAiDrawerOpen(true)}
          style={{
            position: 'fixed',
            bottom: '24px',
            right: '24px',
            backgroundColor: 'var(--bg-card)',
            border: '1px solid var(--border-medium)',
            borderRadius: 'var(--radius-sm)',
            padding: '10px 16px',
            color: 'var(--text-main)',
            boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.6)',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            zIndex: 100
          }}
        >
          <div style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: '#38BDF8' }} />
          <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>AI Concierge</span>
        </button>
      )}

      {/* Footer */}
      <Footer
        onOpenLegal={() => setLegalModalOpen(true)}
        onOpenGate={() => setGateModalOpen(true)}
      />
    </div>
  );
}
