import React, { useState, useEffect } from 'react';
import { Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import LandingPage from './pages/LandingPage';
import EventDetailsPage from './pages/EventDetailsPage';
import SeatSelectionPage from './pages/SeatSelectionPage';
import CheckoutPage from './pages/CheckoutPage';
import ConfirmedTicketPage from './pages/ConfirmedTicketPage';
import OrganizerWizardPage from './pages/OrganizerWizardPage';
import NotFoundPage from './pages/NotFoundPage';
import OfflinePage from './pages/OfflinePage';
import AiAssistantWidget from './components/AiAssistantWidget';
import SignInModal from './components/SignInModal';
import { ApiService } from './services/api';
import { UserService } from './services/userService';
import { MOCK_EVENTS } from './data/mockEvents';

export default function App() {
  const [events, setEvents] = useState(MOCK_EVENTS);
  const [allOrganizerEvents, setAllOrganizerEvents] = useState([]);
  const [selectedCity, setSelectedCity] = useState('Mumbai');
  const [searchQuery, setSearchQuery] = useState('');
  
  // Checkout & Ticket State
  const [checkoutData, setCheckoutData] = useState(null);
  const [confirmedPass, setConfirmedPass] = useState(null);

  // Modals & Assistant Drawers
  const [isSignInOpen, setIsSignInOpen] = useState(false);
  const [isAiOpen, setIsAiOpen] = useState(false);
  const [currentUser, setCurrentUser] = useState(() => {
    try {
      const saved = localStorage.getItem('eventpulse_user');
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });

  // Fetch real events from backend Event Service via Spring Cloud Gateway
  useEffect(() => {
    async function loadBackendEvents() {
      try {
        // Fetch active events for public consumer pages (excludes expired)
        const publicEvents = await ApiService.getEvents({ includeExpired: false });
        if (publicEvents && publicEvents.length > 0) {
          setEvents(publicEvents);
        }

        // Fetch all events (including past expired events for organizer analytics)
        const allEvents = await ApiService.getEvents({ includeExpired: true });
        if (allEvents && allEvents.length > 0) {
          setAllOrganizerEvents(allEvents);
        }
      } catch (err) {
        console.warn('Backend load notice:', err.message);
      }
    }
    loadBackendEvents();
  }, []);

  const handleAddNewEvent = (newEvent) => {
    setEvents(prev => [newEvent, ...prev]);
    setAllOrganizerEvents(prev => [newEvent, ...prev]);
  };

  const handleConfirmSeats = (data) => {
    setCheckoutData(data);
  };

  const handlePaymentComplete = (pass) => {
    setConfirmedPass(pass);
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Top BookMyShow Style Navbar with Official Logo */}
      <Navbar
        selectedCity={selectedCity}
        onSelectCity={() => {
          const cities = ['Mumbai', 'Delhi NCR', 'Bengaluru', 'Hyderabad', 'Pune'];
          const next = cities[(cities.indexOf(selectedCity) + 1) % cities.length];
          setSelectedCity(next);
        }}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        onOpenSignIn={() => setIsSignInOpen(true)}
        onOpenAiAssistant={() => setIsAiOpen(true)}
        currentUser={currentUser}
        onSignOut={() => {
          UserService.logout();
          setCurrentUser(null);
        }}
      />

      {/* Main Routed Content */}
      <main style={{ flex: 1 }}>
        <Routes>
          <Route path="/" element={<LandingPage events={events} />} />
          <Route path="/events/:id" element={<EventDetailsPage events={events} />} />
          <Route path="/events/:id/seats" element={<SeatSelectionPage events={events} onConfirmSeats={handleConfirmSeats} />} />
          <Route path="/checkout" element={<CheckoutPage checkoutData={checkoutData} onPaymentComplete={handlePaymentComplete} />} />
          <Route path="/confirmed" element={<ConfirmedTicketPage confirmedPass={confirmedPass} />} />
          <Route path="/my-tickets" element={<ConfirmedTicketPage confirmedPass={confirmedPass} />} />
          <Route path="/organizer/create" element={<OrganizerWizardPage onEventCreated={handleAddNewEvent} allOrganizerEvents={allOrganizerEvents} />} />
          <Route path="/offline" element={<OfflinePage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>

      {/* Clean Light Matte Footer */}
      <Footer />

      {/* Floating Corner AI Assistant Widget */}
      <AiAssistantWidget
        isOpen={isAiOpen}
        onToggle={() => setIsAiOpen(!isAiOpen)}
        onClose={() => setIsAiOpen(false)}
      />

      {/* Clean Sign In Modal */}
      <SignInModal
        isOpen={isSignInOpen}
        onClose={() => setIsSignInOpen(false)}
        onLoginSuccess={(user) => setCurrentUser(user)}
      />
    </div>
  );
}
