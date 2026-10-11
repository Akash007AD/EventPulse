import React from 'react';
import BrandLogo from './BrandLogo';

export default function Footer() {
  return (
    <footer style={{
      backgroundColor: '#FFFFFF',
      borderTop: '1px solid var(--color-border)',
      paddingTop: '40px',
      paddingBottom: '30px'
    }}>
      {/* 1. Customer Trust & Care Commitments */}
      <div className="site-container" style={{ paddingBottom: '30px', borderBottom: '1px solid var(--color-border)' }}>
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
          gap: '24px'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
            <div style={{
              width: '42px',
              height: '42px',
              borderRadius: '50%',
              backgroundColor: '#FFF1F2',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--color-primary)'
            }}>
              <span className="material-symbols-outlined" style={{ fontSize: '22px' }}>verified</span>
            </div>
            <div>
              <div style={{ fontWeight: 700, fontSize: '14px', color: 'var(--color-text-main)' }}>100% Genuine Admission</div>
              <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>Direct partner venue ticketing guarantee</div>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
            <div style={{
              width: '42px',
              height: '42px',
              borderRadius: '50%',
              backgroundColor: '#F0FDF4',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#16A34A'
            }}>
              <span className="material-symbols-outlined" style={{ fontSize: '22px' }}>support_agent</span>
            </div>
            <div>
              <div style={{ fontWeight: 700, fontSize: '14px', color: 'var(--color-text-main)' }}>24/7 Dedicated Care</div>
              <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>care@eventpulse.io for booking queries</div>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
            <div style={{
              width: '42px',
              height: '42px',
              borderRadius: '50%',
              backgroundColor: '#EFF6FF',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#2563EB'
            }}>
              <span className="material-symbols-outlined" style={{ fontSize: '22px' }}>lock</span>
            </div>
            <div>
              <div style={{ fontWeight: 700, fontSize: '14px', color: 'var(--color-text-main)' }}>Secure Payment Processing</div>
              <div style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>256-Bit encrypted transactions</div>
            </div>
          </div>
        </div>
      </div>

      {/* 2. Corporate Information (About Company Only — Zero Feature Buttons) */}
      <div className="site-container" style={{
        paddingTop: '36px',
        paddingBottom: '30px',
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
        gap: '40px'
      }}>
        {/* About Company & Brand Story */}
        <div>
          <div style={{ marginBottom: '14px' }}>
            <BrandLogo height={32} />
          </div>
          <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', lineHeight: 1.7, maxWidth: '340px' }}>
            EventPulse is a consumer live entertainment platform built to connect audiences with authentic concerts, theatrical productions, and comedy specials. We provide transparent seat reservations and verified digital admissions.
          </p>
        </div>

        {/* Corporate Headquarters */}
        <div>
          <h4 style={{ fontSize: '13px', fontWeight: 700, color: 'var(--color-text-main)', textTransform: 'uppercase', marginBottom: '12px', letterSpacing: '0.04em' }}>
            Corporate Information
          </h4>
          <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', lineHeight: 1.7 }}>
            EventPulse Entertainment Technologies India Pvt. Ltd.<br />
            Level 5, Bandra Kurla Complex (BKC), Mumbai, Maharashtra 400051<br />
            Corporate Identity: CIN U72900MH2026PTC108920
          </p>
        </div>

        {/* Support & Contact */}
        <div>
          <h4 style={{ fontSize: '13px', fontWeight: 700, color: 'var(--color-text-main)', textTransform: 'uppercase', marginBottom: '12px', letterSpacing: '0.04em' }}>
            Helpdesk &amp; Press
          </h4>
          <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', lineHeight: 1.7 }}>
            General Inquiries: contact@eventpulse.io<br />
            Customer Grievance Redressal: grievance@eventpulse.io<br />
            Press &amp; Media: press@eventpulse.io
          </p>
        </div>
      </div>

      {/* 3. Bottom Legal Sub-Bar */}
      <div className="site-container" style={{
        paddingTop: '20px',
        borderTop: '1px solid var(--color-border)',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        flexWrap: 'wrap',
        gap: '12px',
        fontSize: '12px',
        color: 'var(--color-text-muted)'
      }}>
        <div>© 2026 EventPulse Entertainment Technologies Pvt. Ltd. All rights reserved.</div>
        <div style={{ display: 'flex', gap: '16px' }}>
          <span>Terms &amp; Conditions</span>
          <span>Privacy Policy</span>
          <span>Security Practices</span>
        </div>
      </div>
    </footer>
  );
}
