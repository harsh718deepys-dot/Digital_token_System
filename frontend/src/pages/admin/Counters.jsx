import { useState, useEffect } from 'react';
import { Row, Col, Table, Button, Badge } from 'react-bootstrap';
import { Monitor, Zap } from 'lucide-react';
import { adminAPI } from '../../services/api';

export default function Counters() {
  const [counters, setCounters] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchCounters = () => {
    setLoading(true);
    adminAPI.getCounters()
      .then(res => setCounters(res.data))
      .catch(() => setCounters([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchCounters();
    const interval = setInterval(fetchCounters, 15000);
    return () => clearInterval(interval);
  }, []);

  const handleOptimize = () => {
    alert("Running Load Balancing (Greedy + DP) Optimization...");
    // Since location is hardcoded to 1 for demo purposes
    adminAPI.optimize(1)
      .then(res => {
        alert(`Optimization Result:\nTotal Wait Time Reduced: ${res.data.totalWaitTime}\n\n${res.data.comparison}`);
        fetchCounters();
      })
      .catch(err => alert('Optimization failed'));
  };

  return (
    <div>
      <div className="admin-header d-flex justify-content-between align-items-center">
        <h4>Counter Management</h4>
        <div className="d-flex gap-2">
          <Button variant="outline-primary" size="sm" onClick={fetchCounters}>Refresh</Button>
          <Button variant="warning" size="sm" onClick={handleOptimize} className="d-flex align-items-center gap-1">
            <Zap size={14} /> Run Load Balancer
          </Button>
        </div>
      </div>

      <Row className="g-4">
        {loading && counters.length === 0 ? (
          <Col className="text-center py-5"><div className="spinner-border text-primary" /></Col>
        ) : (
          counters.map(c => (
            <Col md={4} lg={3} key={c.id}>
              <div className="card-clean h-100" style={{ borderTop: `4px solid ${c.status === 'SERVING' ? 'var(--success)' : 'var(--secondary)'}` }}>
                <div className="card-body">
                  <div className="d-flex justify-content-between align-items-start mb-3">
                    <div className="d-flex align-items-center">
                      <Monitor size={20} className="me-2 text-muted" />
                      <h5 className="fw-bold mb-0">Counter {c.counterNumber}</h5>
                    </div>
                    <Badge bg={c.status === 'SERVING' ? 'success' : 'secondary'}>{c.status}</Badge>
                  </div>
                  
                  <div className="mb-3">
                    <div className="text-muted" style={{ fontSize: '0.75rem' }}>Currently Serving</div>
                    <div className="fw-bold fs-4 text-primary">{c.currentTokenNumber || '--'}</div>
                  </div>

                  <div className="d-flex justify-content-between text-muted" style={{ fontSize: '0.8rem' }}>
                    <span>Tokens in Queue: {c.tokensInQueue}</span>
                    <span>Cap: {c.capacity}</span>
                  </div>
                  
                  {/* Load bar */}
                  <div className="mt-2" style={{ height: '4px', background: '#e5e7eb', borderRadius: '2px', overflow: 'hidden' }}>
                    <div 
                      style={{ 
                        height: '100%', 
                        width: `${Math.min(100, (c.tokensInQueue / c.capacity) * 100)}%`,
                        background: (c.tokensInQueue / c.capacity) > 0.8 ? 'var(--danger)' : 'var(--primary)'
                      }} 
                    />
                  </div>
                </div>
              </div>
            </Col>
          ))
        )}
      </Row>
    </div>
  );
}
