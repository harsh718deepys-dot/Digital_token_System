import { useState, useEffect } from 'react';
import { Container, Row, Col, Table, Button } from 'react-bootstrap';
import { RefreshCw, MapPin } from 'lucide-react';
import { tokenAPI } from '../../services/api';

export default function LiveQueue() {
  const [activeToken, setActiveToken] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchActive = () => {
    setLoading(true);
    tokenAPI.getActive()
      .then(res => {
        if (res.data && res.data.tokenNumber) {
          setActiveToken(res.data);
        } else {
          setActiveToken(null);
        }
      })
      .catch(() => setActiveToken(null))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchActive();
    const interval = setInterval(fetchActive, 10000);
    return () => clearInterval(interval);
  }, []);

  if (loading) {
    return (
      <Container className="py-5 text-center">
        <div className="spinner-border text-primary" />
      </Container>
    );
  }

  if (!activeToken) {
    return (
      <Container className="py-5">
        <Row className="justify-content-center">
          <Col md={6} className="text-center">
            <div className="card-clean p-5">
              <h5 className="fw-bold mb-2">No Active Token</h5>
              <p className="text-muted">You don't have any active tokens. Generate one to get started.</p>
              <Button href="/generate-token" className="btn-primary-custom">Get a Token</Button>
            </div>
          </Col>
        </Row>
      </Container>
    );
  }

  const statusSteps = [
    { label: 'Token\nGenerated', done: true },
    { label: `Waiting\n(${activeToken.queuePosition || 0} ahead)`, done: activeToken.status !== 'WAITING', current: activeToken.status === 'WAITING' },
    { label: 'Your Turn\nSoon', done: activeToken.status === 'SERVING' || activeToken.status === 'COMPLETED', current: activeToken.status === 'CALLED' },
    { label: 'Completed', done: activeToken.status === 'COMPLETED' },
  ];

  return (
    <Container className="py-4">
      <Row className="justify-content-center">
        <Col md={7} lg={6}>
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h5 className="fw-bold mb-0">Live Queue Status</h5>
            <Button variant="outline-secondary" size="sm" onClick={fetchActive}>
              <RefreshCw size={14} /> Refresh
            </Button>
          </div>

          {/* Location Header */}
          <div className="card-clean mb-3">
            <div className="card-body d-flex justify-content-between align-items-center">
              <div>
                <div className="fw-semibold">{activeToken.locationName}</div>
                <div className="text-muted" style={{ fontSize: '0.8rem' }}>{activeToken.serviceName}</div>
              </div>
              <span className="d-flex align-items-center gap-1" style={{ fontSize: '0.8rem', color: 'var(--success)' }}>
                <span className="live-dot"></span> Live
              </span>
            </div>
          </div>

          {/* Token Stats */}
          <div className="queue-status-bar">
            <div className="queue-stat">
              <span className="label">Your Token</span>
              <span className="value primary">{activeToken.tokenNumber}</span>
            </div>
            <div className="queue-stat">
              <span className="label">Your Position</span>
              <span className="value">{activeToken.queuePosition || '-'}</span>
            </div>
            <div className="queue-stat">
              <span className="label">Est. Wait Time</span>
              <span className="value">~{activeToken.estimatedWaitTime || 0} min</span>
            </div>
          </div>

          {/* Progress Steps */}
          <div className="card-clean mb-3">
            <div className="card-body">
              <div className="queue-progress">
                {statusSteps.map((s, i) => (
                  <div key={i} className="progress-step">
                    <div className={`progress-dot ${s.done ? 'completed' : s.current ? 'current' : 'pending'}`}>
                      {s.done ? '✓' : i + 1}
                    </div>
                    <span style={{ whiteSpace: 'pre-line' }}>{s.label}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Status Badge */}
          <div className="card-clean">
            <div className="card-body text-center">
              <span className={`badge-status badge-${activeToken.status.toLowerCase()}`}>
                {activeToken.status}
              </span>
              {activeToken.counterNumber && (
                <p className="mt-2 mb-0 text-muted" style={{ fontSize: '0.85rem' }}>
                  Assigned to <strong>Counter {activeToken.counterNumber}</strong>
                </p>
              )}
            </div>
          </div>
        </Col>
      </Row>
    </Container>
  );
}
