import React, { useState } from 'react';
import { ApiService } from '../services/api';

export default function GateScannerModal({ initialToken = '', onClose }) {
  const [token, setToken] = useState(initialToken);
  const [gateId, setGateId] = useState('GATE-NORTH-01');
  const [scanResult, setScanResult] = useState(null);
  const [isScanning, setIsScanning] = useState(false);

  const handleScan = async (overrideToken) => {
    const tokenToScan = overrideToken || token;
    if (!tokenToScan) return;

    setIsScanning(true);
    setScanResult(null);

    const result = await ApiService.scanTicket(tokenToScan, gateId);
    setScanResult(result);
    setIsScanning(false);
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div 
        className="modal-card" 
        style={{ maxWidth: '580px' }} 
        onClick={e => e.stopPropagation()}
      >
        {/* Header */}
        <div style={{
          padding: '20px 24px',
          borderBottom: '1px solid var(--border-subtle)',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <div>
            <h3 style={{ fontSize: '1.25rem' }}>Gate Check-In & Turnstile Scanner</h3>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
              HMAC-SHA256 Anti-Passback Admission Console
            </div>
          </div>
          <button className="btn btn-sm btn-secondary" onClick={onClose}>
            Close
          </button>
        </div>

        {/* Body */}
        <div style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {/* Gate Selector */}
          <div>
            <label style={{ fontSize: '0.78rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
              Current Turnstile / Checkpoint Gate
            </label>
            <select
              value={gateId}
              onChange={e => setGateId(e.target.value)}
              style={{
                width: '100%',
                backgroundColor: 'var(--bg-canvas)',
                border: '1px solid var(--border-medium)',
                borderRadius: 'var(--radius-xs)',
                padding: '9px 12px',
                color: 'var(--text-main)',
                fontSize: '0.88rem',
                outline: 'none'
              }}
            >
              <option value="GATE-NORTH-01">Turnstile 01 — North Grand Concourse</option>
              <option value="GATE-SOUTH-02">Turnstile 02 — South Orchestra Entry</option>
              <option value="GATE-VIP-03">Turnstile 03 — VIP & Royal Circle Express</option>
            </select>
          </div>

          {/* Token Input */}
          <div>
            <label style={{ fontSize: '0.78rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
              Ticket HMAC Token
            </label>
            <input
              type="text"
              value={token}
              onChange={e => setToken(e.target.value)}
              placeholder="EP.tkt-1001.evt-001.196d.a82b..."
              style={{
                width: '100%',
                backgroundColor: 'var(--bg-canvas)',
                border: '1px solid var(--border-medium)',
                borderRadius: 'var(--radius-xs)',
                padding: '9px 12px',
                color: 'var(--text-main)',
                fontSize: '0.88rem',
                fontFamily: 'var(--font-mono)',
                outline: 'none'
              }}
            />
          </div>

          {/* Preset Test Buttons */}
          <div>
            <div style={{ fontSize: '0.72rem', textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: '8px' }}>
              Quick Simulation Tokens
            </div>
            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
              <button 
                className="btn btn-sm btn-outline"
                onClick={() => {
                  const validToken = "EP.tkt-1001.evt-001.6700af.99c2d1";
                  setToken(validToken);
                  handleScan(validToken);
                }}
              >
                Simulate Valid Pass
              </button>
              <button 
                className="btn btn-sm btn-outline"
                onClick={() => {
                  const usedToken = "EP.tkt-1001.evt-001.6700af.used";
                  setToken(usedToken);
                  handleScan(usedToken);
                }}
                style={{ color: '#F87171' }}
              >
                Simulate Anti-Passback Duplicate
              </button>
              <button 
                className="btn btn-sm btn-outline"
                onClick={() => {
                  const invalidToken = "INVALID_CORRUPT_TOKEN";
                  setToken(invalidToken);
                  handleScan(invalidToken);
                }}
              >
                Simulate Corrupted Pass
              </button>
            </div>
          </div>

          {/* Scan Action */}
          <button 
            className="btn btn-primary btn-lg"
            onClick={() => handleScan()}
            disabled={!token || isScanning}
          >
            {isScanning ? "Verifying Signature..." : "Verify & Admit Attendee"}
          </button>

          {/* Result Card */}
          {scanResult && (
            <div style={{
              backgroundColor: scanResult.success ? 'var(--status-success-bg)' : 'var(--status-danger-bg)',
              border: `1px solid ${scanResult.success ? 'var(--status-success)' : 'var(--status-danger)'}`,
              borderRadius: 'var(--radius-sm)',
              padding: '16px',
              marginTop: '4px'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                <span className={`badge ${scanResult.success ? 'badge-success' : 'badge-amber'}`} style={{ color: scanResult.success ? '#34D399' : '#EF4444', borderColor: scanResult.success ? '#059669' : '#DC2626' }}>
                  {scanResult.code}
                </span>
                <span style={{ fontWeight: 700, fontSize: '0.95rem', color: scanResult.success ? '#34D399' : '#FCA5A5' }}>
                  {scanResult.success ? "ENTRY APPROVED" : "ENTRY REJECTED"}
                </span>
              </div>
              <p style={{ fontSize: '0.85rem', color: scanResult.success ? '#D1FAE5' : '#FECACA', margin: 0 }}>
                {scanResult.message}
              </p>
              {scanResult.ticketId && (
                <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '8px' }}>
                  Logged at {gateId} • Reference Ticket: {scanResult.ticketId}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
