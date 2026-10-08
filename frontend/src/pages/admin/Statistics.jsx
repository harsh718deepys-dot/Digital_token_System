import { useState, useEffect } from 'react';
import { Row, Col, Card } from 'react-bootstrap';
import { adminAPI } from '../../services/api';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';

export default function Statistics() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminAPI.getStatistics()
      .then(res => setStats(res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading || !stats) return <div className="p-5 text-center"><div className="spinner-border text-primary" /></div>;

  const pieData = Object.entries(stats.tokensByStatus).map(([name, value]) => ({ name, value }));
  const COLORS = ['#f59e0b', '#3b82f6', '#10b981', '#ef4444'];

  return (
    <div>
      <div className="admin-header">
        <h4>System Statistics</h4>
      </div>

      <Row className="g-4 mb-4">
        <Col md={4}>
          <div className="card-clean h-100">
            <div className="card-body text-center py-5">
              <div className="text-muted mb-2">Total Tokens Today</div>
              <div className="fw-bold fs-1 text-primary">{stats.totalTokensToday}</div>
            </div>
          </div>
        </Col>
        <Col md={4}>
          <div className="card-clean h-100">
            <div className="card-body text-center py-5">
              <div className="text-muted mb-2">Avg. Wait Time</div>
              <div className="fw-bold fs-1 text-warning">~{stats.avgWaitTime} <span className="fs-5 text-muted">min</span></div>
            </div>
          </div>
        </Col>
        <Col md={4}>
          <div className="card-clean h-100">
            <div className="card-body text-center py-5">
              <div className="text-muted mb-2">Completion Rate</div>
              <div className="fw-bold fs-1 text-success">{Math.round(stats.completionRate)}<span className="fs-5">%</span></div>
            </div>
          </div>
        </Col>
      </Row>

      <Row>
        <Col md={6}>
          <div className="card-clean">
            <div className="card-header">Token Status Distribution</div>
            <div className="card-body" style={{ height: '300px' }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={pieData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={80}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {pieData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>
        </Col>
      </Row>
    </div>
  );
}
