import { useState, useEffect } from 'react';
import { Container, Row, Col, Form, Button, Alert } from 'react-bootstrap';
import { useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, MapPin, Check, ChevronRight, User, Users, Heart, AlertTriangle, Baby, Bell } from 'lucide-react';
import { locationAPI, tokenAPI } from '../../services/api';

const categories = [
  { value: 'NORMAL', label: 'Normal', desc: 'General Visitor', icon: User },
  { value: 'SENIOR_CITIZEN', label: 'Senior Citizen', desc: '60+ years', icon: Users },
  { value: 'DIFFERENTLY_ABLED', label: 'Differently-abled', desc: 'Special needs', icon: Heart },
  { value: 'PREGNANT_WOMAN', label: 'Pregnant Woman', desc: 'Priority access', icon: Baby },
  { value: 'EMERGENCY', label: 'Emergency', desc: 'Medical / Other', icon: AlertTriangle },
];

export default function GenerateToken() {
  const [step, setStep] = useState(1);
  const [locations, setLocations] = useState([]);
  const [services, setServices] = useState([]);
  const [form, setForm] = useState({ locationId: '', serviceId: '', category: 'NORMAL' });
  const [generatedToken, setGeneratedToken] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    locationAPI.getAll()
      .then(res => setLocations(res.data))
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (form.locationId) {
      locationAPI.getServices(form.locationId)
        .then(res => setServices(res.data))
        .catch(() => {});
    }
  }, [form.locationId]);

  const handleSubmit = async () => {
    setError('');
    setLoading(true);
    try {
      const res = await tokenAPI.create({
        locationId: parseInt(form.locationId),
        serviceId: parseInt(form.serviceId),
        category: form.category,
      });
      setGeneratedToken(res.data);
      setStep(4);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to generate token');
    }
    setLoading(false);
  };

  const selectedLocation = locations.find(l => l.id == form.locationId);
  const selectedService = services.find(s => s.id == form.serviceId);

  return (
    <Container className="py-4">
      <Row className="justify-content-center">
        <Col md={7} lg={6}>
          <div className="d-flex align-items-center mb-3">
            <Link to="/" className="text-decoration-none me-2"><ArrowLeft size={20} /></Link>
            <h5 className="fw-bold mb-0">Generate Token</h5>
          </div>

          {/* Stepper */}
          <div className="stepper">
            {['Select Service', 'Fill Details', 'Confirm', 'Token Generated'].map((label, i) => (
              <div key={i} className="stepper-step">
                {i > 0 && <div className={`stepper-line ${step > i ? 'completed' : ''}`} />}
                <div className={`stepper-circle ${step === i + 1 ? 'active' : step > i + 1 ? 'completed' : ''}`}>
                  {step > i + 1 ? <Check size={14} /> : i + 1}
                </div>
                <span className={`stepper-label ${step === i + 1 ? 'active' : ''}`}>{label}</span>
              </div>
            ))}
          </div>

          {error && <Alert variant="danger" className="py-2" style={{ fontSize: '0.85rem' }}>{error}</Alert>}

          {/* Step 1: Select Location & Service */}
          {step === 1 && (
            <div className="card-clean">
              <div className="card-body p-4">
                <Form.Group className="mb-3">
                  <Form.Label className="fw-semibold">
                    <MapPin size={16} className="me-1" /> Select Location
                  </Form.Label>
                  <Form.Select value={form.locationId} onChange={(e) => setForm({ ...form, locationId: e.target.value, serviceId: '' })}>
                    <option value="">Choose a location...</option>
                    {locations.map(loc => (
                      <option key={loc.id} value={loc.id}>{loc.name}, {loc.city}</option>
                    ))}
                  </Form.Select>
                </Form.Group>

                {services.length > 0 && (
                  <Form.Group className="mb-3">
                    <Form.Label className="fw-semibold">Select Service</Form.Label>
                    <Form.Select value={form.serviceId} onChange={(e) => setForm({ ...form, serviceId: e.target.value })}>
                      <option value="">Choose a service...</option>
                      {services.map(svc => (
                        <option key={svc.id} value={svc.id}>{svc.name}</option>
                      ))}
                    </Form.Select>
                  </Form.Group>
                )}

                <h6 className="fw-semibold mt-4 mb-3">Visitor Category</h6>
                <Row className="g-2">
                  {categories.map((cat) => (
                    <Col key={cat.value} xs={6} md={4}>
                      <div
                        className={`category-card ${form.category === cat.value ? 'selected' : ''}`}
                        onClick={() => setForm({ ...form, category: cat.value })}
                      >
                        <div className="category-icon">
                          <cat.icon size={20} />
                        </div>
                        <div className="category-name">{cat.label}</div>
                        <div className="category-desc">{cat.desc}</div>
                      </div>
                    </Col>
                  ))}
                </Row>

                <Button
                  className="w-100 btn-primary-custom mt-4"
                  disabled={!form.locationId || !form.serviceId}
                  onClick={() => setStep(2)}
                >
                  Next <ChevronRight size={16} />
                </Button>
              </div>
            </div>
          )}

          {/* Step 2: Review Details */}
          {step === 2 && (
            <div className="card-clean">
              <div className="card-body p-4">
                <h6 className="fw-semibold mb-3">Review Your Details</h6>
                <div className="mb-2"><strong>Location:</strong> {selectedLocation?.name}</div>
                <div className="mb-2"><strong>Service:</strong> {selectedService?.name}</div>
                <div className="mb-2"><strong>Category:</strong> {categories.find(c => c.value === form.category)?.label}</div>
                <div className="mb-3"><strong>Est. Service Time:</strong> ~{selectedService?.avgServiceTime} min</div>

                <div className="d-flex gap-2">
                  <Button variant="outline-secondary" onClick={() => setStep(1)}>Back</Button>
                  <Button className="flex-grow-1 btn-primary-custom" onClick={() => setStep(3)}>
                    Next <ChevronRight size={16} />
                  </Button>
                </div>
              </div>
            </div>
          )}

          {/* Step 3: Confirm */}
          {step === 3 && (
            <div className="card-clean">
              <div className="card-body p-4 text-center">
                <h6 className="fw-semibold mb-3">Confirm Token Generation</h6>
                <p className="text-muted">You are about to generate a token for:</p>
                <div className="p-3 mb-3" style={{ background: '#f9fafb', borderRadius: '8px' }}>
                  <div className="fw-semibold">{selectedService?.name}</div>
                  <div className="text-muted" style={{ fontSize: '0.85rem' }}>{selectedLocation?.name}</div>
                  <div className="mt-1">
                    <span className="badge-status badge-waiting">
                      {categories.find(c => c.value === form.category)?.label}
                    </span>
                  </div>
                </div>

                <div className="d-flex gap-2">
                  <Button variant="outline-secondary" onClick={() => setStep(2)}>Back</Button>
                  <Button className="flex-grow-1 btn-primary-custom" onClick={handleSubmit} disabled={loading}>
                    {loading ? 'Generating...' : 'Generate Token'}
                  </Button>
                </div>
              </div>
            </div>
          )}

          {/* Step 4: Token Generated */}
          {step === 4 && generatedToken && (
            <div className="card-clean">
              <div className="card-body p-4">
                <div className="token-display">
                  <div className="token-success-icon">
                    <Check size={32} />
                  </div>
                  <h5 className="fw-bold mb-1">Token Generated Successfully!</h5>
                  <p className="text-muted mb-3">Your Token Number</p>
                  <div className="token-number">{generatedToken.tokenNumber}</div>
                  <p className="mt-2 mb-1 fw-semibold">{generatedToken.serviceName}</p>
                  <p className="text-muted" style={{ fontSize: '0.85rem' }}>{generatedToken.locationName}</p>

                  <Row className="g-3 mt-3">
                    <Col xs={6}>
                      <div className="p-3" style={{ background: '#f9fafb', borderRadius: '8px' }}>
                        <div className="text-muted" style={{ fontSize: '0.75rem' }}>Your Position</div>
                        <div className="fw-bold" style={{ fontSize: '1.5rem' }}>{generatedToken.queuePosition || 1}</div>
                      </div>
                    </Col>
                    <Col xs={6}>
                      <div className="p-3" style={{ background: '#f9fafb', borderRadius: '8px' }}>
                        <div className="text-muted" style={{ fontSize: '0.75rem' }}>Estimated Wait Time</div>
                        <div className="fw-bold" style={{ fontSize: '1.5rem' }}>~{generatedToken.estimatedWaitTime || 5} min</div>
                      </div>
                    </Col>
                  </Row>

                  <div className="p-3 mt-3" style={{ background: '#f0f7ff', borderRadius: '8px', fontSize: '0.8rem' }}>
                    <Bell size={14} className="me-1" /> You will receive a notification when your turn is near. Please arrive at the location on time.
                  </div>

                  <div className="d-flex gap-2 mt-4">
                    <Button as={Link} to="/live-queue" className="flex-grow-1 btn-primary-custom">
                      View Live Queue
                    </Button>
                  </div>
                </div>
              </div>
            </div>
          )}
        </Col>
      </Row>
    </Container>
  );
}
