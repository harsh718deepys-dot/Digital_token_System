import { useState, useEffect } from 'react';
import { Row, Col, Form, Button, Modal, Table } from 'react-bootstrap';
import { Volume2, Play, Users, MapPin } from 'lucide-react';
import { adminAPI, locationAPI } from '../../services/api';

export default function AdminLiveQueue() {
  const [locations, setLocations] = useState([]);
  const [services, setServices] = useState([]);
  const [counters, setCounters] = useState([]);
  
  const [selLocation, setSelLocation] = useState('');
  const [selService, setSelService] = useState('');
  const [selCounter, setSelCounter] = useState('');
  
  const [tokens, setTokens] = useState([]);
  const [calledToken, setCalledToken] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    locationAPI.getAll().then(res => setLocations(res.data)).catch(() => {});
    adminAPI.getCounters().then(res => setCounters(res.data)).catch(() => {});
  }, []);

  useEffect(() => {
    if (selLocation) {
      locationAPI.getServices(selLocation).then(res => setServices(res.data)).catch(() => {});
    } else {
      setServices([]);
      setSelService('');
    }
  }, [selLocation]);

  const fetchQueue = () => {
    if (!selService) return;
    setLoading(true);
    adminAPI.getAllTokens()
      .then(res => {
        const filtered = res.data.filter(t => 
          t.serviceId == selService && 
          (t.status === 'WAITING' || t.status === 'CALLED' || t.status === 'SERVING')
        ).sort((a, b) => {
          if (a.priority !== b.priority) return a.priority - b.priority;
          return new Date(a.createdAt) - new Date(b.createdAt);
        });
        setTokens(filtered);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchQueue();
    const int = setInterval(fetchQueue, 5000);
    return () => clearInterval(int);
  }, [selService]);

  const handleCallNext = () => {
    if (!selService || !selCounter) return;
    adminAPI.callNext({ serviceId: selService, counterId: selCounter })
      .then(res => {
        setCalledToken(res.data);
        fetchQueue();
      })
      .catch(err => alert(err.response?.data?.message || 'Failed to call next token'));
  };

  const handleComplete = (tokenId) => {
    adminAPI.completeToken(tokenId).then(fetchQueue);
  };

  const availableCounters = counters.filter(c => c.locationId == selLocation);

  return (
    <div>
      <div className="admin-header">
        <h4>Live Queue Operator</h4>
      </div>

      <div className="card-clean mb-4">
        <div className="card-body">
          <Row className="g-3">
            <Col md={3}>
              <Form.Label className="fw-semibold" style={{ fontSize: '0.85rem' }}>Location</Form.Label>
              <Form.Select value={selLocation} onChange={e => setSelLocation(e.target.value)}>
                <option value="">Select Location</option>
                {locations.map(l => <option key={l.id} value={l.id}>{l.name}</option>)}
              </Form.Select>
            </Col>
            <Col md={3}>
              <Form.Label className="fw-semibold" style={{ fontSize: '0.85rem' }}>Service Queue</Form.Label>
              <Form.Select value={selService} onChange={e => setSelService(e.target.value)} disabled={!selLocation}>
                <option value="">Select Service</option>
                {services.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
              </Form.Select>
            </Col>
            <Col md={3}>
              <Form.Label className="fw-semibold" style={{ fontSize: '0.85rem' }}>Operating Counter</Form.Label>
              <Form.Select value={selCounter} onChange={e => setSelCounter(e.target.value)} disabled={!selLocation}>
                <option value="">Select Counter</option>
                {counters.map(c => <option key={c.id} value={c.id}>Counter {c.counterNumber}</option>)}
              </Form.Select>
            </Col>
            <Col md={3} className="d-flex align-items-end">
              <Button 
                variant="primary" 
                className="w-100 d-flex justify-content-center align-items-center gap-2 py-2"
                disabled={!selService || !selCounter}
                onClick={handleCallNext}
              >
                <Volume2 size={18} /> Call Next Token
              </Button>
            </Col>
          </Row>
        </div>
      </div>

      {selService && (
        <div className="card-clean">
          <div className="card-header d-flex justify-content-between align-items-center">
            <span>Current Queue</span>
            <span className="badge bg-primary">{tokens.filter(t => t.status === 'WAITING').length} Waiting</span>
          </div>
          <div className="table-responsive">
            <Table className="table-clean mb-0" hover>
              <thead>
                <tr>
                  <th>Pos</th>
                  <th>Token No.</th>
                  <th>Visitor</th>
                  <th>Category (Priority)</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {tokens.length === 0 ? (
                  <tr><td colSpan="6" className="text-center py-4 text-muted">No tokens in queue.</td></tr>
                ) : (
                  tokens.map((t, index) => (
                    <tr key={t.id} style={{ background: t.status === 'CALLED' ? '#eff6ff' : '' }}>
                      <td className="fw-semibold">{t.status === 'WAITING' ? index + 1 : '-'}</td>
                      <td className="fw-bold text-primary">{t.tokenNumber}</td>
                      <td>{t.visitorName || 'Guest'}</td>
                      <td>
                        {t.category.replace('_', ' ')}
                        {t.priority < 5 && <span className="ms-2 badge bg-warning text-dark">Priority</span>}
                      </td>
                      <td>
                        <span className={`badge-status badge-${t.status.toLowerCase()}`}>
                          {t.status === 'CALLED' ? `CALLED (C${t.counterNumber})` : t.status}
                        </span>
                      </td>
                      <td>
                        {(t.status === 'CALLED' || t.status === 'SERVING') && (
                          <Button variant="success" size="sm" onClick={() => handleComplete(t.id)}>
                            Mark Complete
                          </Button>
                        )}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </Table>
          </div>
        </div>
      )}

      {/* Call Next Modal */}
      <Modal show={!!calledToken} onHide={() => setCalledToken(null)} centered>
        <Modal.Body className="text-center p-5 call-next-modal">
          <div className="mb-3"><Volume2 size={48} className="text-primary" /></div>
          <h4 className="fw-bold mb-4">Token Called</h4>
          <div className="token-big mb-3">{calledToken?.token?.tokenNumber}</div>
          <p className="text-muted fs-5 mb-0">Please proceed to</p>
          <div className="fw-bold fs-3 mt-1">Counter {calledToken?.counterNumber}</div>
          <Button variant="primary" className="mt-4 px-4" onClick={() => setCalledToken(null)}>
            Dismiss
          </Button>
        </Modal.Body>
      </Modal>
    </div>
  );
}
