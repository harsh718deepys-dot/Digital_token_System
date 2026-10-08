import { useState, useEffect } from 'react';
import { Table, Button, Form, InputGroup } from 'react-bootstrap';
import { Search, MapPin } from 'lucide-react';
import { adminAPI } from '../../services/api';

export default function TokenManagement() {
  const [tokens, setTokens] = useState([]);
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchTokens();
  }, []);

  const fetchTokens = () => {
    setLoading(true);
    adminAPI.getAllTokens()
      .then(res => setTokens(res.data))
      .catch(() => setTokens([]))
      .finally(() => setLoading(false));
  };

  const handleComplete = (id) => {
    adminAPI.completeToken(id).then(fetchTokens);
  };

  const filteredTokens = tokens.filter(t => {
    if (filter !== 'ALL' && t.status !== filter) return false;
    if (search) {
      const q = search.toLowerCase();
      return t.tokenNumber.toLowerCase().includes(q) || 
             (t.visitorName && t.visitorName.toLowerCase().includes(q));
    }
    return true;
  });

  return (
    <div>
      <div className="admin-header">
        <h4>Token Management</h4>
        <Button variant="outline-primary" size="sm" onClick={fetchTokens}>Refresh</Button>
      </div>

      <div className="card-clean mb-4">
        <div className="card-body">
          <div className="d-flex gap-3 flex-wrap">
            <InputGroup style={{ maxWidth: '300px' }}>
              <InputGroup.Text className="bg-white"><Search size={16} /></InputGroup.Text>
              <Form.Control 
                placeholder="Search token number or name..." 
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </InputGroup>
            <Form.Select 
              style={{ maxWidth: '200px' }}
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
            >
              <option value="ALL">All Statuses</option>
              <option value="WAITING">Waiting</option>
              <option value="CALLED">Called</option>
              <option value="SERVING">Serving</option>
              <option value="COMPLETED">Completed</option>
              <option value="CANCELLED">Cancelled</option>
            </Form.Select>
          </div>
        </div>
      </div>

      <div className="card-clean">
        <div className="table-responsive">
          <Table className="table-clean mb-0" hover>
            <thead>
              <tr>
                <th>Token No.</th>
                <th>Visitor Details</th>
                <th>Service & Location</th>
                <th>Category (Priority)</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan="6" className="text-center py-4"><div className="spinner-border text-primary" /></td></tr>
              ) : filteredTokens.length === 0 ? (
                <tr><td colSpan="6" className="text-center py-4 text-muted">No tokens found.</td></tr>
              ) : (
                filteredTokens.map(t => (
                  <tr key={t.id}>
                    <td className="fw-semibold text-primary">{t.tokenNumber}</td>
                    <td>
                      <div>{t.visitorName || 'Guest'}</div>
                    </td>
                    <td>
                      <div className="mb-1">{t.serviceName}</div>
                      <div className="text-muted d-flex align-items-center" style={{ fontSize: '0.75rem' }}>
                        <MapPin size={12} className="me-1" /> {t.locationName}
                      </div>
                    </td>
                    <td>
                      <div>{t.category.replace('_', ' ')}</div>
                      <small className="text-muted">Prio: {t.priority}</small>
                    </td>
                    <td>
                      <span className={`badge-status badge-${t.status.toLowerCase()}`}>
                        {t.status}
                      </span>
                    </td>
                    <td>
                      {(t.status === 'CALLED' || t.status === 'SERVING') && (
                        <Button variant="success" size="sm" onClick={() => handleComplete(t.id)}>
                          Complete
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
    </div>
  );
}
