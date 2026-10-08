import { useState, useEffect } from 'react';
import { Table, Form, InputGroup } from 'react-bootstrap';
import { Search, Clock } from 'lucide-react';
import { adminAPI } from '../../services/api';

export default function AdminHistory() {
  const [history, setHistory] = useState([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminAPI.getHistory()
      .then(res => setHistory(res.data))
      .catch(() => setHistory([]))
      .finally(() => setLoading(false));
  }, []);

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleString();
  };

  const filtered = history.filter(t => {
    if (!search) return true;
    const q = search.toLowerCase();
    return t.tokenNumber.toLowerCase().includes(q) || 
           (t.visitorName && t.visitorName.toLowerCase().includes(q));
  });

  return (
    <div>
      <div className="admin-header">
        <h4>System History</h4>
      </div>

      <div className="card-clean mb-4">
        <div className="card-body">
          <InputGroup style={{ maxWidth: '300px' }}>
            <InputGroup.Text className="bg-white"><Search size={16} /></InputGroup.Text>
            <Form.Control 
              placeholder="Search token number or name..." 
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </InputGroup>
        </div>
      </div>

      <div className="card-clean">
        <div className="table-responsive">
          <Table className="table-clean mb-0" hover>
            <thead>
              <tr>
                <th>Date & Time</th>
                <th>Token No.</th>
                <th>Visitor</th>
                <th>Service & Location</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan="5" className="text-center py-4"><div className="spinner-border text-primary" /></td></tr>
              ) : filtered.length === 0 ? (
                <tr><td colSpan="5" className="text-center py-4 text-muted">No history found.</td></tr>
              ) : (
                filtered.map(t => (
                  <tr key={t.id}>
                    <td>
                      <div className="text-muted d-flex align-items-center" style={{ fontSize: '0.8rem' }}>
                        <Clock size={12} className="me-1" />
                        {formatDate(t.createdAt)}
                      </div>
                    </td>
                    <td className="fw-semibold">{t.tokenNumber}</td>
                    <td>{t.visitorName || 'Guest'}</td>
                    <td>
                      <div>{t.serviceName}</div>
                      <div className="text-muted" style={{ fontSize: '0.75rem' }}>{t.locationName}</div>
                    </td>
                    <td>
                      <span className={`badge-status badge-${t.status.toLowerCase()}`}>
                        {t.status}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </Table>
        </div>
      </div>
    </div>
  );
}
