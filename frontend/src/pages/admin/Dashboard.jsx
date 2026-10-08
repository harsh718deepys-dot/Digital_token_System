import { useState, useEffect } from 'react';
import { Row, Col, Card } from 'react-bootstrap';
import { Users, UserCheck, Clock, CheckCircle, Monitor } from 'lucide-react';
import { adminAPI } from '../../services/api';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboard = () => {
      adminAPI.getDashboard()
        .then(res => setData(res.data))
        .catch(() => {})
        .finally(() => setLoading(false));
    };
    
    fetchDashboard();
    const interval = setInterval(fetchDashboard, 30000); // 30s refresh
    return () => clearInterval(interval);
  }, []);

  if (loading || !data) return <div className="p-5 text-center"><div className="spinner-border text-primary" /></div>;

  return (
    <div>
      <div className="admin-header">
        <h4>Dashboard Overview</h4>
        <div className="text-muted" style={{ fontSize: '0.85rem' }}>Today's Live Data</div>
      </div>

      <Row className="g-4 mb-4">
        <Col md={3}>
          <div className="stat-card">
            <div className="stat-icon blue"><Users size={24} /></div>
            <div>
              <div className="stat-value">{data.totalTokens}</div>
              <div className="stat-label">Total Tokens</div>
            </div>
          </div>
        </Col>
        <Col md={3}>
          <div className="stat-card">
            <div className="stat-icon orange"><Clock size={24} /></div>
            <div>
              <div className="stat-value">{data.waiting}</div>
              <div className="stat-label">Waiting</div>
            </div>
          </div>
        </Col>
        <Col md={3}>
          <div className="stat-card">
            <div className="stat-icon purple"><UserCheck size={24} /></div>
            <div>
              <div className="stat-value">{data.serving}</div>
              <div className="stat-label">Serving</div>
            </div>
          </div>
        </Col>
        <Col md={3}>
          <div className="stat-card">
            <div className="stat-icon green"><CheckCircle size={24} /></div>
            <div>
              <div className="stat-value">{data.completed}</div>
              <div className="stat-label">Completed</div>
            </div>
          </div>
        </Col>
      </Row>

      <Row className="g-4">
        <Col md={8}>
          <div className="card-clean h-100">
            <div className="card-header">Queue Volume Overview</div>
            <div className="card-body" style={{ height: '300px' }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={data.queueOverview} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis dataKey="time" axisLine={false} tickLine={false} tick={{ fontSize: 12 }} />
                  <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12 }} />
                  <Tooltip cursor={{ fill: '#f3f4f6' }} />
                  <Bar dataKey="normal" name="Normal" fill="var(--primary)" radius={[4, 4, 0, 0]} barSize={30} />
                  <Bar dataKey="priority" name="Priority" fill="var(--warning)" radius={[4, 4, 0, 0]} barSize={30} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </Col>
        <Col md={4}>
          <div className="card-clean h-100">
            <div className="card-header d-flex justify-content-between align-items-center">
              Active Counters
              <span className="badge bg-success">{data.counterStatuses.filter(c => c.status === 'SERVING').length} / {data.counterStatuses.length}</span>
            </div>
            <div className="card-body p-0">
              {data.counterStatuses.map(c => (
                <div key={c.id} className="p-3 border-bottom d-flex justify-content-between align-items-center">
                  <div className="d-flex align-items-center">
                    <Monitor size={18} className="me-2 text-muted" />
                    <span className="fw-semibold">Counter {c.counterNumber}</span>
                  </div>
                  <div>
                    {c.status === 'SERVING' ? (
                      <span className="badge-status badge-serving px-2 py-1">Serving {c.currentTokenNumber}</span>
                    ) : (
                      <span className="badge-status badge-completed px-2 py-1">Idle</span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </Col>
      </Row>
    </div>
  );
}
