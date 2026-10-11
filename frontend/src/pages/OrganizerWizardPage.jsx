import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ImageDatabase } from '../services/imageDb';

export default function OrganizerWizardPage({ onEventCreated, allOrganizerEvents = [] }) {
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState('wizard'); // 'wizard' | 'imagedb' | 'analytics'
  const [filterType, setFilterType] = useState('all'); // 'all' | 'active' | 'expired'
  const [currentStep, setCurrentStep] = useState(1);

  // Stored Images from Database
  const [storedImages, setStoredImages] = useState([]);
  const [isUploadingToDb, setIsUploadingToDb] = useState(false);
  const [uploadSuccessMsg, setUploadSuccessMsg] = useState(null);

  // Form State for listing new show
  const [formData, setFormData] = useState({
    title: '',
    category: 'CONCERT',
    categoryLabel: 'Concerts',
    genre: 'Live Music | Acoustic',
    language: 'Hindi',
    duration: '2h 30m',
    ageRestriction: 'All Ages',
    city: 'Mumbai',
    venueName: '',
    venueAddress: '',
    startTime: '2026-12-20T19:00:00Z',
    posterUrl: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=800&q=80',
    bannerUrl: 'https://images.unsplash.com/photo-1465847899084-d164df4dedc6?auto=format&fit=crop&w=1200&q=80',
    basePrice: 999,
    description: '',
    tiers: [
      { name: 'VIP FRONT ROW', price: 2999 },
      { name: 'EXECUTIVE SEATS', price: 1499 },
      { name: 'STANDARD ENTRY', price: 999 }
    ]
  });

  const [imagePreview, setImagePreview] = useState(formData.posterUrl);

  // Load stored images from Image Database on mount
  useEffect(() => {
    async function loadDbImages() {
      const imgs = await ImageDatabase.getAllImages();
      setStoredImages(imgs);
    }
    loadDbImages();
  }, []);

  // Handle local image file upload directly into Database
  const handleImageFileChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setIsUploadingToDb(true);
    setUploadSuccessMsg(null);

    const reader = new FileReader();
    reader.onloadend = async () => {
      const dataUrl = reader.result;
      setImagePreview(dataUrl);

      // Persist directly into Image Database
      const savedRecord = await ImageDatabase.saveImage({
        file,
        dataUrl,
        fileName: file.name,
        contentType: file.type
      });

      setFormData(prev => ({
        ...prev,
        posterUrl: dataUrl,
        bannerUrl: dataUrl
      }));

      // Refresh DB list
      const updatedList = await ImageDatabase.getAllImages();
      setStoredImages(updatedList);
      setIsUploadingToDb(false);
      setUploadSuccessMsg(`Saved to Database: ${file.name} (${Math.round(file.size / 1024)} KB)`);
      setTimeout(() => setUploadSuccessMsg(null), 4000);
    };
    reader.readAsDataURL(file);
  };

  const handleSelectFromDb = (img) => {
    setImagePreview(img.dataUrl);
    setFormData(prev => ({
      ...prev,
      posterUrl: img.dataUrl,
      bannerUrl: img.dataUrl
    }));
    setActiveTab('wizard');
    setUploadSuccessMsg(`Selected poster from DB: ${img.fileName}`);
    setTimeout(() => setUploadSuccessMsg(null), 3000);
  };

  const handleDeleteFromDb = async (id) => {
    await ImageDatabase.deleteImage(id);
    const updated = await ImageDatabase.getAllImages();
    setStoredImages(updated);
  };

  const handlePublish = (e) => {
    e.preventDefault();
    const newEvent = {
      ...formData,
      id: `evt-${Date.now().toString(36)}`,
      rating: '100% (New Show)',
      tag: 'NEW RELEASE',
      formatBadge: 'EXCLUSIVE',
      isExpired: false,
      performers: [
        {
          id: 'perf-new',
          name: 'Featured Artist',
          role: 'Lead Performer',
          imageUrl: formData.posterUrl
        }
      ]
    };

    if (onEventCreated) {
      onEventCreated(newEvent);
    }
    navigate(`/events/${newEvent.id}`);
  };

  // Filter organizer events
  const displayedEvents = allOrganizerEvents.filter(event => {
    if (filterType === 'active') return !event.isExpired;
    if (filterType === 'expired') return event.isExpired;
    return true;
  });

  return (
    <div style={{ backgroundColor: 'var(--color-surface)', minHeight: '100vh', padding: '36px 0 80px' }}>
      <div className="site-container" style={{ maxWidth: '980px', margin: '0 auto' }}>
        
        {/* Top Header */}
        <div style={{ marginBottom: '24px', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <span className="badge-pill badge-crimson" style={{ marginBottom: '8px' }}>
              ORGANIZER PORTAL
            </span>
            <h1 style={{ fontSize: '28px', fontWeight: 800, color: 'var(--color-text-main)', margin: '4px 0' }}>
              Organizer Dashboard &amp; Events
            </h1>
            <p style={{ fontSize: '14px', color: 'var(--color-text-secondary)' }}>
              Manage your live productions, upload event posters to the Image DB, and analyze past show metrics.
            </p>
          </div>

          {/* 3-Tab Switcher */}
          <div style={{
            display: 'flex',
            backgroundColor: '#FFFFFF',
            padding: '4px',
            borderRadius: 'var(--radius-lg)',
            border: '1px solid var(--color-border)',
            boxShadow: 'var(--shadow-subtle)',
            gap: '4px'
          }}>
            <button
              onClick={() => setActiveTab('wizard')}
              style={{
                padding: '8px 16px',
                borderRadius: 'var(--radius-md)',
                border: 'none',
                backgroundColor: activeTab === 'wizard' ? 'var(--color-primary)' : 'transparent',
                color: activeTab === 'wizard' ? '#FFFFFF' : 'var(--color-text-secondary)',
                fontWeight: 700,
                fontSize: '13px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '6px'
              }}
            >
              <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>add_circle</span>
              <span>List New Show</span>
            </button>

            <button
              onClick={() => setActiveTab('imagedb')}
              style={{
                padding: '8px 16px',
                borderRadius: 'var(--radius-md)',
                border: 'none',
                backgroundColor: activeTab === 'imagedb' ? 'var(--color-primary)' : 'transparent',
                color: activeTab === 'imagedb' ? '#FFFFFF' : 'var(--color-text-secondary)',
                fontWeight: 700,
                fontSize: '13px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '6px'
              }}
            >
              <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>perm_media</span>
              <span>Poster Image DB ({storedImages.length})</span>
            </button>

            <button
              onClick={() => setActiveTab('analytics')}
              style={{
                padding: '8px 16px',
                borderRadius: 'var(--radius-md)',
                border: 'none',
                backgroundColor: activeTab === 'analytics' ? 'var(--color-primary)' : 'transparent',
                color: activeTab === 'analytics' ? '#FFFFFF' : 'var(--color-text-secondary)',
                fontWeight: 700,
                fontSize: '13px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '6px'
              }}
            >
              <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>analytics</span>
              <span>Hosted Shows &amp; Reviews</span>
            </button>
          </div>
        </div>

        {/* Global Notification Banner */}
        {uploadSuccessMsg && (
          <div style={{
            padding: '12px 18px',
            backgroundColor: '#F0FDF4',
            border: '1px solid #BBF7D0',
            borderRadius: 'var(--radius-md)',
            color: '#166534',
            fontSize: '13px',
            fontWeight: 600,
            marginBottom: '20px',
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}>
            <span className="material-symbols-outlined" style={{ fontSize: '20px', color: '#16A34A' }}>cloud_done</span>
            <span>{uploadSuccessMsg}</span>
          </div>
        )}

        {/* TAB 1: LIST NEW SHOW WIZARD */}
        {activeTab === 'wizard' && (
          <div>
            {/* 4-Step Progress Indicator */}
            <div style={{
              backgroundColor: '#FFFFFF',
              borderRadius: 'var(--radius-xl)',
              border: '1px solid var(--color-border)',
              padding: '16px 24px',
              marginBottom: '28px',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              boxShadow: 'var(--shadow-subtle)',
              overflowX: 'auto'
            }} className="no-scrollbar">
              {[
                { step: 1, label: '1. Event Info & Poster' },
                { step: 2, label: '2. Date & Venue' },
                { step: 3, label: '3. Ticket Pricing' },
                { step: 4, label: '4. Publish Live' }
              ].map(s => (
                <div
                  key={s.step}
                  onClick={() => setCurrentStep(s.step)}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    cursor: 'pointer',
                    color: currentStep === s.step ? 'var(--color-primary)' : 'var(--color-text-secondary)',
                    fontWeight: currentStep === s.step ? 800 : 500,
                    fontSize: '13px',
                    whiteSpace: 'nowrap'
                  }}
                >
                  <span style={{
                    width: '24px',
                    height: '24px',
                    borderRadius: '50%',
                    backgroundColor: currentStep === s.step ? 'var(--color-primary)' : 'var(--color-surface-container)',
                    color: currentStep === s.step ? '#FFFFFF' : 'var(--color-text-secondary)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '11px',
                    fontWeight: 700
                  }}>
                    {s.step}
                  </span>
                  <span>{s.label}</span>
                </div>
              ))}
            </div>

            {/* Form Step Container */}
            <div style={{
              backgroundColor: '#FFFFFF',
              borderRadius: 'var(--radius-xl)',
              border: '1px solid var(--color-border)',
              boxShadow: 'var(--shadow-card)',
              padding: '32px'
            }}>
              {/* Step 1: Event Info & Poster Image DB */}
              {currentStep === 1 && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                  <h2 style={{ fontSize: '18px', fontWeight: 800, color: 'var(--color-text-main)' }}>
                    Step 1: Event Information &amp; Poster Image
                  </h2>

                  <div>
                    <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: 'var(--color-text-secondary)', marginBottom: '6px', textTransform: 'uppercase' }}>
                      Event Title
                    </label>
                    <input
                      type="text"
                      placeholder="e.g. Royal Philharmonic: Night of Symphonies"
                      value={formData.title}
                      onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                      style={{ width: '100%', padding: '10px 14px', borderRadius: 'var(--radius-md)', border: '1px solid var(--color-border-strong)', fontSize: '14px' }}
                    />
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                    <div>
                      <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: 'var(--color-text-secondary)', marginBottom: '6px', textTransform: 'uppercase' }}>
                        Category
                      </label>
                      <select
                        value={formData.category}
                        onChange={(e) => {
                          const catLabels = { CONCERT: 'Concerts', THEATRE: 'Plays', COMEDY: 'Comedy' };
                          setFormData({ ...formData, category: e.target.value, categoryLabel: catLabels[e.target.value] || 'Events' });
                        }}
                        style={{ width: '100%', padding: '10px 14px', borderRadius: 'var(--radius-md)', border: '1px solid var(--color-border-strong)', fontSize: '14px' }}
                      >
                        <option value="CONCERT">Live Concert</option>
                        <option value="THEATRE">Theatrical Play</option>
                        <option value="COMEDY">Stand-up Comedy</option>
                      </select>
                    </div>

                    <div>
                      <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: 'var(--color-text-secondary)', marginBottom: '6px', textTransform: 'uppercase' }}>
                        Language &amp; Genre
                      </label>
                      <input
                        type="text"
                        value={formData.genre}
                        onChange={(e) => setFormData({ ...formData, genre: e.target.value })}
                        style={{ width: '100%', padding: '10px 14px', borderRadius: 'var(--radius-md)', border: '1px solid var(--color-border-strong)', fontSize: '14px' }}
                      />
                    </div>
                  </div>

                  {/* Dedicated Image DB Poster Upload */}
                  <div style={{
                    padding: '20px',
                    borderRadius: 'var(--radius-lg)',
                    border: '1px solid var(--color-border)',
                    backgroundColor: 'var(--color-surface)'
                  }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                      <label style={{ fontSize: '12px', fontWeight: 800, color: 'var(--color-text-main)', textTransform: 'uppercase' }}>
                        Event Poster (Stored in Database)
                      </label>
                      <button
                        type="button"
                        onClick={() => setActiveTab('imagedb')}
                        className="btn btn-secondary"
                        style={{ fontSize: '11px', padding: '4px 10px' }}
                      >
                        <span className="material-symbols-outlined" style={{ fontSize: '14px' }}>photo_library</span>
                        <span>Browse Image DB Vault ({storedImages.length})</span>
                      </button>
                    </div>

                    <div style={{
                      display: 'flex',
                      gap: '20px',
                      alignItems: 'center',
                      flexWrap: 'wrap'
                    }}>
                      {/* Thumbnail Preview */}
                      <div style={{
                        width: '100px',
                        height: '140px',
                        borderRadius: 'var(--radius-md)',
                        backgroundColor: '#FFFFFF',
                        border: '2px solid var(--color-border)',
                        overflow: 'hidden',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        boxShadow: 'var(--shadow-subtle)'
                      }}>
                        {imagePreview ? (
                          <img src={imagePreview} alt="Poster" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                        ) : (
                          <span className="material-symbols-outlined" style={{ fontSize: '32px', color: 'var(--color-text-muted)' }}>image</span>
                        )}
                      </div>

                      {/* Upload Controls */}
                      <div style={{ flex: 1, minWidth: '240px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
                        <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--color-text-main)' }}>
                          Upload New Poster Image File
                        </div>
                        <p style={{ fontSize: '12px', color: 'var(--color-text-secondary)', margin: 0 }}>
                          File is stored directly in the persistent Image Database (MySQL &amp; IndexedDB). Supports PNG, JPG, WEBP.
                        </p>
                        
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginTop: '4px' }}>
                          <label className="btn btn-primary" style={{ cursor: 'pointer', fontSize: '12px', padding: '8px 14px' }}>
                            <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>upload_file</span>
                            <span>{isUploadingToDb ? 'Saving to DB...' : 'Choose Poster File'}</span>
                            <input
                              type="file"
                              accept="image/*"
                              onChange={handleImageFileChange}
                              style={{ display: 'none' }}
                            />
                          </label>
                          <span style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>or paste URL below:</span>
                        </div>

                        <input
                          type="text"
                          placeholder="Image URL (e.g. https://...)"
                          value={formData.posterUrl}
                          onChange={(e) => {
                            setFormData({ ...formData, posterUrl: e.target.value, bannerUrl: e.target.value });
                            setImagePreview(e.target.value);
                          }}
                          style={{ width: '100%', padding: '8px 12px', borderRadius: '4px', border: '1px solid var(--color-border)', fontSize: '12px' }}
                        />
                      </div>
                    </div>
                  </div>

                  <div>
                    <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: 'var(--color-text-secondary)', marginBottom: '6px', textTransform: 'uppercase' }}>
                      Synopsis &amp; Description
                    </label>
                    <textarea
                      rows={4}
                      placeholder="Describe your performance, lineup, and audience experience..."
                      value={formData.description}
                      onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                      style={{ width: '100%', padding: '10px 14px', borderRadius: 'var(--radius-md)', border: '1px solid var(--color-border-strong)', fontSize: '14px' }}
                    />
                  </div>

                  <button onClick={() => setCurrentStep(2)} className="btn btn-primary" style={{ alignSelf: 'flex-end', marginTop: '10px' }}>
                    Next: Date &amp; Venue <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>arrow_forward</span>
                  </button>
                </div>
              )}

              {/* Step 2: Date & Venue */}
              {currentStep === 2 && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                  <h2 style={{ fontSize: '18px', fontWeight: 800, color: 'var(--color-text-main)' }}>
                    Step 2: Date, Time &amp; Venue
                  </h2>

                  <div>
                    <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: 'var(--color-text-secondary)', marginBottom: '6px', textTransform: 'uppercase' }}>
                      City
                    </label>
                    <input
                      type="text"
                      value={formData.city}
                      onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                      style={{ width: '100%', padding: '10px 14px', borderRadius: 'var(--radius-md)', border: '1px solid var(--color-border-strong)', fontSize: '14px' }}
                    />
                  </div>

                  <div>
                    <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: 'var(--color-text-secondary)', marginBottom: '6px', textTransform: 'uppercase' }}>
                      Auditorium / Venue Name
                    </label>
                    <input
                      type="text"
                      placeholder="e.g. NCPA Tata Theatre, Mumbai"
                      value={formData.venueName}
                      onChange={(e) => setFormData({ ...formData, venueName: e.target.value })}
                      style={{ width: '100%', padding: '10px 14px', borderRadius: 'var(--radius-md)', border: '1px solid var(--color-border-strong)', fontSize: '14px' }}
                    />
                  </div>

                  <div>
                    <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: 'var(--color-text-secondary)', marginBottom: '6px', textTransform: 'uppercase' }}>
                      Full Street Address
                    </label>
                    <input
                      type="text"
                      placeholder="e.g. NCPA Marg, Nariman Point, South Mumbai"
                      value={formData.venueAddress}
                      onChange={(e) => setFormData({ ...formData, venueAddress: e.target.value })}
                      style={{ width: '100%', padding: '10px 14px', borderRadius: 'var(--radius-md)', border: '1px solid var(--color-border-strong)', fontSize: '14px' }}
                    />
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '10px' }}>
                    <button onClick={() => setCurrentStep(1)} className="btn btn-secondary">
                      Back
                    </button>
                    <button onClick={() => setCurrentStep(3)} className="btn btn-primary">
                      Next: Ticket Pricing <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>arrow_forward</span>
                    </button>
                  </div>
                </div>
              )}

              {/* Step 3: Ticket Pricing */}
              {currentStep === 3 && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                  <h2 style={{ fontSize: '18px', fontWeight: 800, color: 'var(--color-text-main)' }}>
                    Step 3: Seating Tiers &amp; Pricing
                  </h2>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    {formData.tiers.map((tier, idx) => (
                      <div key={idx} style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        padding: '14px 18px',
                        backgroundColor: 'var(--color-surface)',
                        borderRadius: 'var(--radius-md)',
                        border: '1px solid var(--color-border)'
                      }}>
                        <span style={{ fontWeight: 700, fontSize: '14px' }}>{tier.name}</span>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                          <span style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>Price: ₹</span>
                          <input
                            type="number"
                            value={tier.price}
                            onChange={(e) => {
                              const updated = [...formData.tiers];
                              updated[idx].price = Number(e.target.value);
                              setFormData({ ...formData, tiers: updated });
                            }}
                            style={{ width: '100px', padding: '6px 10px', borderRadius: '4px', border: '1px solid var(--color-border-strong)', fontWeight: 700 }}
                          />
                        </div>
                      </div>
                    ))}
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '10px' }}>
                    <button onClick={() => setCurrentStep(2)} className="btn btn-secondary">
                      Back
                    </button>
                    <button onClick={() => setCurrentStep(4)} className="btn btn-primary">
                      Review &amp; Publish <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>arrow_forward</span>
                    </button>
                  </div>
                </div>
              )}

              {/* Step 4: Review & Publish */}
              {currentStep === 4 && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                  <h2 style={{ fontSize: '18px', fontWeight: 800, color: 'var(--color-text-main)' }}>
                    Step 4: Review &amp; Launch Event
                  </h2>

                  <div style={{
                    padding: '20px',
                    backgroundColor: 'var(--color-surface)',
                    borderRadius: 'var(--radius-lg)',
                    border: '1px solid var(--color-border)',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '10px',
                    fontSize: '14px'
                  }}>
                    <div><strong>Event Title:</strong> {formData.title || 'Untitled Concert'}</div>
                    <div><strong>Category:</strong> {formData.categoryLabel} ({formData.genre})</div>
                    <div><strong>Venue:</strong> {formData.venueName || 'To Be Announced'}, {formData.city}</div>
                    <div><strong>Starting Price:</strong> ₹{formData.tiers[formData.tiers.length - 1].price} onwards</div>
                    <div><strong>Database Poster:</strong> Saved to MySQL &amp; Image DB</div>
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '10px' }}>
                    <button onClick={() => setCurrentStep(3)} className="btn btn-secondary">
                      Back
                    </button>
                    <button onClick={handlePublish} className="btn btn-primary btn-lg" style={{ fontWeight: 800 }}>
                      <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>publish</span>
                      <span>Publish Show Live</span>
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}

        {/* TAB 2: POSTER IMAGE DB VAULT */}
        {activeTab === 'imagedb' && (
          <div>
            {/* Header Box */}
            <div style={{
              backgroundColor: '#FFFFFF',
              borderRadius: 'var(--radius-xl)',
              border: '1px solid var(--color-border)',
              padding: '24px 28px',
              marginBottom: '24px',
              boxShadow: 'var(--shadow-card)',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              flexWrap: 'wrap',
              gap: '16px'
            }}>
              <div>
                <h2 style={{ fontSize: '20px', fontWeight: 800, color: 'var(--color-text-main)', margin: '0 0 4px' }}>
                  Poster Image Database Vault
                </h2>
                <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', margin: 0 }}>
                  Stored in <code style={{ backgroundColor: '#F1F5F9', padding: '2px 6px', borderRadius: '4px' }}>eventpulse_event.event_images</code> &amp; IndexedDB.
                </p>
              </div>

              {/* Upload New Button */}
              <label className="btn btn-primary" style={{ cursor: 'pointer', padding: '10px 18px' }}>
                <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>add_photo_alternate</span>
                <span>{isUploadingToDb ? 'Uploading to DB...' : 'Upload New Poster to DB'}</span>
                <input
                  type="file"
                  accept="image/*"
                  onChange={handleImageFileChange}
                  style={{ display: 'none' }}
                />
              </label>
            </div>

            {/* Grid of Images in DB */}
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
              gap: '20px'
            }}>
              {storedImages.map(img => (
                <div
                  key={img.id}
                  style={{
                    backgroundColor: '#FFFFFF',
                    borderRadius: 'var(--radius-lg)',
                    border: '1px solid var(--color-border)',
                    overflow: 'hidden',
                    boxShadow: 'var(--shadow-subtle)',
                    display: 'flex',
                    flexDirection: 'column'
                  }}
                >
                  <div style={{ height: '240px', backgroundColor: '#F1F5F9', position: 'relative' }}>
                    <img
                      src={img.dataUrl}
                      alt={img.fileName}
                      style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                    />
                    <div style={{
                      position: 'absolute',
                      top: '8px',
                      right: '8px',
                      backgroundColor: 'rgba(26, 26, 36, 0.8)',
                      backdropFilter: 'blur(4px)',
                      color: '#FFFFFF',
                      fontSize: '10px',
                      fontWeight: 700,
                      padding: '3px 8px',
                      borderRadius: '12px'
                    }}>
                      {Math.round(img.fileSize / 1024)} KB
                    </div>
                  </div>

                  <div style={{ padding: '14px', flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'space-between', gap: '10px' }}>
                    <div>
                      <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--color-text-main)', wordBreak: 'break-all', lineHeight: 1.3 }}>
                        {img.fileName}
                      </div>
                      <div style={{ fontSize: '11px', color: 'var(--color-text-muted)', marginTop: '4px' }}>
                        ID: {img.id}
                      </div>
                    </div>

                    <div style={{ display: 'flex', gap: '8px' }}>
                      <button
                        onClick={() => handleSelectFromDb(img)}
                        className="btn btn-primary"
                        style={{ flex: 1, fontSize: '12px', padding: '6px 8px', justifyContent: 'center' }}
                      >
                        Use for Show
                      </button>
                      <button
                        onClick={() => handleDeleteFromDb(img.id)}
                        className="btn btn-secondary"
                        style={{ padding: '6px 8px', color: '#DC2626' }}
                        title="Delete from DB"
                      >
                        <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>delete</span>
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* TAB 3: HOSTED SHOWS & HISTORICAL ANALYTICS */}
        {activeTab === 'analytics' && (
          <div>
            {/* Filter Pills */}
            <div style={{ display: 'flex', gap: '10px', marginBottom: '20px', alignItems: 'center' }}>
              <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--color-text-secondary)' }}>Filter Shows:</span>
              {[
                { id: 'all', label: `All Shows (${allOrganizerEvents.length})` },
                { id: 'active', label: `Active / Live (${allOrganizerEvents.filter(e => !e.isExpired).length})` },
                { id: 'expired', label: `Past Concluded Shows (${allOrganizerEvents.filter(e => e.isExpired).length})` }
              ].map(f => (
                <button
                  key={f.id}
                  onClick={() => setFilterType(f.id)}
                  style={{
                    padding: '6px 14px',
                    borderRadius: 'var(--radius-full)',
                    border: '1px solid',
                    borderColor: filterType === f.id ? 'var(--color-primary)' : 'var(--color-border)',
                    backgroundColor: filterType === f.id ? 'var(--color-primary)' : '#FFFFFF',
                    color: filterType === f.id ? '#FFFFFF' : 'var(--color-text-main)',
                    fontSize: '12px',
                    fontWeight: 700,
                    cursor: 'pointer'
                  }}
                >
                  {f.label}
                </button>
              ))}
            </div>

            {/* List of Organizer Events with Detailed Metrics */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              {displayedEvents.map(evt => (
                <div
                  key={evt.id}
                  style={{
                    backgroundColor: '#FFFFFF',
                    borderRadius: 'var(--radius-xl)',
                    border: evt.isExpired ? '1px dashed #CBD5E1' : '1px solid var(--color-border)',
                    boxShadow: 'var(--shadow-card)',
                    padding: '24px',
                    opacity: evt.isExpired ? 0.95 : 1
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '12px' }}>
                    <div style={{ display: 'flex', gap: '16px' }}>
                      <img
                        src={evt.posterUrl}
                        alt={evt.title}
                        style={{ width: '90px', height: '120px', borderRadius: 'var(--radius-md)', objectFit: 'cover' }}
                      />
                      <div>
                        <div style={{ display: 'flex', gap: '8px', alignItems: 'center', marginBottom: '6px' }}>
                          <span className={`badge-pill ${evt.isExpired ? 'badge-blue' : 'badge-crimson'}`}>
                            {evt.isExpired ? 'CONCLUDED / PAST EVENT' : 'ACTIVE LIVE SHOW'}
                          </span>
                          <span style={{ fontSize: '12px', color: 'var(--color-text-muted)' }}>ID: {evt.id.substring(0, 8)}...</span>
                        </div>
                        <h3 style={{ fontSize: '18px', fontWeight: 800, color: 'var(--color-text-main)', margin: '0 0 4px' }}>
                          {evt.title}
                        </h3>
                        <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', margin: '0 0 6px' }}>
                          {evt.venueName}, {evt.city}
                        </p>
                        {evt.isExpired && (
                          <div style={{
                            padding: '6px 10px',
                            backgroundColor: '#F8FAFC',
                            borderRadius: 'var(--radius-sm)',
                            border: '1px solid #E2E8F0',
                            fontSize: '12px',
                            color: '#475569',
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '6px'
                          }}>
                            <span className="material-symbols-outlined" style={{ fontSize: '16px', color: '#64748B' }}>inventory_2</span>
                            <span>Removed from public discovery after show expiry • Retained permanently in database for historical analytics.</span>
                          </div>
                        )}
                      </div>
                    </div>

                    <div style={{ textAlign: 'right' }}>
                      <Link
                        to={`/events/${evt.id}`}
                        className="btn btn-secondary"
                        style={{ fontSize: '12px', padding: '6px 12px' }}
                      >
                        View Show Details
                      </Link>
                    </div>
                  </div>

                  {/* Historical & Performance Analytics Cards */}
                  <div style={{
                    marginTop: '20px',
                    paddingTop: '16px',
                    borderTop: '1px solid var(--color-border)',
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
                    gap: '14px'
                  }}>
                    <div style={{ backgroundColor: 'var(--color-surface)', padding: '12px 16px', borderRadius: 'var(--radius-md)' }}>
                      <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--color-text-secondary)', textTransform: 'uppercase' }}>
                        Tickets Sold
                      </span>
                      <div style={{ fontSize: '18px', fontWeight: 800, color: 'var(--color-text-main)', marginTop: '2px' }}>
                        {evt.isExpired ? '1,420 / 1,500' : '842 / 1,000'}
                      </div>
                      <span style={{ fontSize: '11px', color: '#16A34A', fontWeight: 600 }}>
                        {evt.isExpired ? '94.6% Occupancy' : '84.2% Booked'}
                      </span>
                    </div>

                    <div style={{ backgroundColor: 'var(--color-surface)', padding: '12px 16px', borderRadius: 'var(--radius-md)' }}>
                      <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--color-text-secondary)', textTransform: 'uppercase' }}>
                        Gross Ticket Sales
                      </span>
                      <div style={{ fontSize: '18px', fontWeight: 800, color: 'var(--color-text-main)', marginTop: '2px' }}>
                        ₹{evt.isExpired ? '18,44,580' : '10,93,758'}
                      </div>
                      <span style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>
                        Net organizer payout
                      </span>
                    </div>

                    <div style={{ backgroundColor: 'var(--color-surface)', padding: '12px 16px', borderRadius: 'var(--radius-md)' }}>
                      <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--color-text-secondary)', textTransform: 'uppercase' }}>
                        Platform Convenience Fee
                      </span>
                      <div style={{ fontSize: '18px', fontWeight: 800, color: 'var(--color-primary)', marginTop: '2px' }}>
                        ₹{evt.isExpired ? '35,500' : '21,050'}
                      </div>
                      <span style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>
                        ₹25/ticket + 2.5% CGST + 2.5% SGST
                      </span>
                    </div>

                    <div style={{ backgroundColor: 'var(--color-surface)', padding: '12px 16px', borderRadius: 'var(--radius-md)' }}>
                      <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--color-text-secondary)', textTransform: 'uppercase' }}>
                        Audience Satisfaction
                      </span>
                      <div style={{ fontSize: '18px', fontWeight: 800, color: '#F59E0B', marginTop: '2px', display: 'flex', alignItems: 'center', gap: '4px' }}>
                        <span>★</span>
                        <span style={{ color: 'var(--color-text-main)' }}>{evt.isExpired ? '4.9 / 5' : '4.8 / 5'}</span>
                      </div>
                      <span style={{ fontSize: '11px', color: '#16A34A', fontWeight: 600 }}>
                        96% audience positive reviews
                      </span>
                    </div>
                  </div>

                  {/* Audience Review Sample for Analysis */}
                  <div style={{ marginTop: '16px', backgroundColor: '#F8F9FA', padding: '12px 16px', borderRadius: 'var(--radius-md)' }}>
                    <div style={{ fontSize: '12px', fontWeight: 700, color: 'var(--color-text-secondary)', marginBottom: '6px' }}>
                      Latest Audience Review Snippet:
                    </div>
                    <p style={{ fontSize: '13px', color: 'var(--color-text-main)', margin: 0, fontStyle: 'italic' }}>
                      "{evt.isExpired ? 'Electric ambiance and unforgettable setlist! The crowd organization and seat visibility were world-class.' : 'Sound acoustics in the auditorium were crystal clear. Effortless entry at the gate!'}"
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

      </div>
    </div>
  );
}
