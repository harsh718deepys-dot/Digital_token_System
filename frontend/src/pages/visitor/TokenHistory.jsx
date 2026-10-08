import { useState, useEffect } from 'react';
import { Container, Row, Col, Table } from 'react-bootstrap';
import { History, Clock, MapPin } from 'lucide-react';
import { tokenAPI } from '../../services/api';

export default function TokenHistory() {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    tokenAPI.getHistory()
      .then(res => setHistory(res.data))
      .catch(() => setHistory([]))
      .finally(() => setLoading(false));
  }, []);

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleString();
  };

  return (
    <Container className="py-4">
      <Row className="justify-content-center">
        <Col md={10} lg={8}>
          <div className="d-flex align-items-center mb-4">
            <History size={24} className="me-2 text-primary" />
            <h4 className="fw-bold mb-0">Token History</h4>
          </div>

          <div className="card-clean">
            <div className="card-body p-0">
              {loading ? (
                <div className="p-5 text-center">
                  <div className="spinner-border text-primary" />
                </div>
              ) : history.length === 0 ? (
                <div className="p-5 text-center text-muted">
                  <p className="mb-0">No token history found.</p>
                </div>
              ) : (
                <div className="table-responsive">
                  <Table className="table-clean mb-0" hover>
                    <thead>
                      <tr>
                        <th>Token No.</th>
                        <th>Location & Service</th>
                        <th>Category</th>
                        <th>Date & Time</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {history.map((t) => (
                        <tr key={t.id}>
                          <td className="fw-semibold text-primary">{t.tokenNumber}</td>
                          <td>
                            <div className="d-flex align-items-center mb-1">
                              <MapPin size={12} className="me-1 text-muted" />
                              <span style={{ fontSize: '0.85rem' }}>{t.locationName}</span>
                            </div>
                            <span className="text-muted" style={{ fontSize: '0.75rem' }}>{t.serviceName}</span>
                          </td>
                          <td style={{ fontSize: '0.85rem' }}>{t.category.replace('_', ' ')}</td>
                          <td>
                            <div className="d-flex align-items-center text-muted" style={{ fontSize: '0.8rem' }}>
                              <Clock size={12} className="me-1" />
                              {formatDate(t.createdAt)}
                            </div>
                          </td>
                          <td>
                            <span className={`badge-status badge-${t.status.toLowerCase()}`}>
                              {t.status}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </Table>
                </div>
              )}
            </div>
          </div>
        </Col>
      </Row>
    </Container>
  );
}
