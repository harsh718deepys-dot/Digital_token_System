import { useState } from 'react';
import { Container, Row, Col, Form, Button, Alert } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function RegisterPage() {
  const [form, setForm] = useState({ username: '', email: '', password: '', fullName: '', phone: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register(form);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    }
    setLoading(false);
  };

  return (
    <Container className="py-5">
      <Row className="justify-content-center">
        <Col md={5}>
          <div className="card-clean">
            <div className="card-body p-4">
              <h4 className="fw-bold mb-1 text-center">Create Account</h4>
              <p className="text-muted text-center mb-4" style={{ fontSize: '0.9rem' }}>Join TokenQueue today</p>

              {error && <Alert variant="danger" className="py-2" style={{ fontSize: '0.85rem' }}>{error}</Alert>}

              <Form onSubmit={handleSubmit}>
                <Form.Group className="mb-3">
                  <Form.Label className="fw-semibold" style={{ fontSize: '0.85rem' }}>Full Name</Form.Label>
                  <Form.Control type="text" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} placeholder="Enter full name" required />
                </Form.Group>
                <Form.Group className="mb-3">
                  <Form.Label className="fw-semibold" style={{ fontSize: '0.85rem' }}>Username</Form.Label>
                  <Form.Control type="text" value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} placeholder="Choose a username" required />
                </Form.Group>
                <Form.Group className="mb-3">
                  <Form.Label className="fw-semibold" style={{ fontSize: '0.85rem' }}>Email</Form.Label>
                  <Form.Control type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} placeholder="Enter email" required />
                </Form.Group>
                <Form.Group className="mb-3">
                  <Form.Label className="fw-semibold" style={{ fontSize: '0.85rem' }}>Phone</Form.Label>
                  <Form.Control type="tel" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} placeholder="Enter phone number" />
                </Form.Group>
                <Form.Group className="mb-3">
                  <Form.Label className="fw-semibold" style={{ fontSize: '0.85rem' }}>Password</Form.Label>
                  <Form.Control type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} placeholder="Create password" required />
                </Form.Group>
                <Button type="submit" className="w-100 btn-primary-custom" disabled={loading}>
                  {loading ? 'Creating...' : 'Create Account'}
                </Button>
              </Form>

              <p className="text-center mt-3 mb-0" style={{ fontSize: '0.85rem' }}>
                Already have an account? <Link to="/login" className="fw-semibold">Sign In</Link>
              </p>
            </div>
          </div>
        </Col>
      </Row>
    </Container>
  );
}
