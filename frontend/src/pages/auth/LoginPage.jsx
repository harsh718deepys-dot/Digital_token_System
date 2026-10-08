import { useState } from 'react';
import { Container, Row, Col, Form, Button, Alert } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function LoginPage() {
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const data = await login(form);
      navigate(data.role === 'ADMIN' ? '/admin' : '/');
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid credentials');
    }
    setLoading(false);
  };

  return (
    <Container className="py-5">
      <Row className="justify-content-center">
        <Col md={5}>
          <div className="card-clean">
            <div className="card-body p-4">
              <h4 className="fw-bold mb-1 text-center">Welcome Back</h4>
              <p className="text-muted text-center mb-4" style={{ fontSize: '0.9rem' }}>Sign in to your TokenQueue account</p>

              {error && <Alert variant="danger" className="py-2" style={{ fontSize: '0.85rem' }}>{error}</Alert>}

              <Form onSubmit={handleSubmit}>
                <Form.Group className="mb-3">
                  <Form.Label className="fw-semibold" style={{ fontSize: '0.85rem' }}>Username</Form.Label>
                  <Form.Control
                    type="text"
                    value={form.username}
                    onChange={(e) => setForm({ ...form, username: e.target.value })}
                    placeholder="Enter username"
                    required
                  />
                </Form.Group>
                <Form.Group className="mb-3">
                  <Form.Label className="fw-semibold" style={{ fontSize: '0.85rem' }}>Password</Form.Label>
                  <Form.Control
                    type="password"
                    value={form.password}
                    onChange={(e) => setForm({ ...form, password: e.target.value })}
                    placeholder="Enter password"
                    required
                  />
                </Form.Group>
                <Button type="submit" className="w-100 btn-primary-custom" disabled={loading}>
                  {loading ? 'Signing in...' : 'Sign In'}
                </Button>
              </Form>

              <p className="text-center mt-3 mb-0" style={{ fontSize: '0.85rem' }}>
                Don't have an account? <Link to="/register" className="fw-semibold">Register</Link>
              </p>

              <hr className="my-3" />
              <div className="text-center">
                <small className="text-muted">Demo Accounts:</small><br />
                <small className="text-muted">Admin: <code>admin / admin123</code></small><br />
                <small className="text-muted">Visitor: <code>visitor1 / visitor123</code></small>
              </div>
            </div>
          </div>
        </Col>
      </Row>
    </Container>
  );
}
