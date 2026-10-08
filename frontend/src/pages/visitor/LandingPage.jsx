import { useState, useEffect } from 'react';
import { Container, Row, Col, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { Clock, Shield, Bell, CheckCircle, ChevronRight, MapPin } from 'lucide-react';
import { locationAPI } from '../../services/api';

const typeIcons = {
  Temple: '🛕', Bank: '🏦', 'Government Office': '🏛️',
  'Railway Station': '🚆', Hospital: '🏥', Event: '🎪'
};

const DEFAULT_LOCATIONS = [
  { id: 1, name: 'Shree Mahalakshmi Temple', type: 'Temple', description: 'Darshan / Prasad', imageUrl: 'https://images.unsplash.com/photo-1548013146-72479768bada?w=400' },
  { id: 2, name: 'SBI Kothrud Branch', type: 'Bank', description: 'Banking Services', imageUrl: 'https://images.unsplash.com/photo-1541354329998-f4d9a9f9297f?w=400' },
  { id: 3, name: 'Pune Railway Station', type: 'Railway Station', description: 'Ticketing', imageUrl: 'https://images.unsplash.com/photo-1474487548417-781cb71495f3?w=400' },
  { id: 4, name: 'Pune RTO', type: 'Government Office', description: 'Document Services', imageUrl: 'https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=400' },
  { id: 5, name: 'CityCare Diagnostic Centre', type: 'Hospital', description: 'OPD / Billing', imageUrl: 'https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=400' },
  { id: 6, name: 'Government Office', type: 'Government Office', description: 'Document Verification', imageUrl: 'https://images.unsplash.com/photo-1541888946425-d81bb19240f5?w=400' },
];

export default function LandingPage() {
  const [locations, setLocations] = useState(DEFAULT_LOCATIONS);

  useEffect(() => {
    locationAPI.getAll()
      .then(res => {
        if (Array.isArray(res.data) && res.data.length > 0) {
          setLocations(res.data);
        }
      })
      .catch(() => {
        // Fallback already in place
      });
  }, []);

  return (
    <div className="page-wrapper">
      {/* Hero Section */}
      <section className="hero-section">
        <Container>
          <Row className="align-items-center">
            <Col lg={7}>
              <h1>Digital Token System<br />for Crowded Places</h1>
              <p>Skip the long queues. Get your digital token, check live status and receive notifications.</p>
              <Button as={Link} to="/generate-token" variant="warning" size="lg" className="fw-semibold">
                Get Token Now <ChevronRight size={18} />
              </Button>
              <div className="hero-features mt-4">
                <div className="hero-feature">
                  <div className="hero-feature-icon"><Clock size={18} /></div>
                  <span>Real-time<br/>Wait Time</span>
                </div>
                <div className="hero-feature">
                  <div className="hero-feature-icon"><Shield size={18} /></div>
                  <span>Priority<br/>Access</span>
                </div>
                <div className="hero-feature">
                  <div className="hero-feature-icon"><Bell size={18} /></div>
                  <span>Live<br/>Notifications</span>
                </div>
                <div className="hero-feature">
                  <div className="hero-feature-icon"><CheckCircle size={18} /></div>
                  <span>Safer &<br/>Organized</span>
                </div>
              </div>
            </Col>
            <Col lg={5} className="d-none d-lg-block text-center">
              <img
                src="https://images.unsplash.com/photo-1548013146-72479768bada?w=500"
                alt="Temple Queue"
                style={{ borderRadius: '12px', maxWidth: '100%', boxShadow: '0 8px 30px rgba(0,0,0,0.3)' }}
              />
            </Col>
          </Row>
        </Container>
      </section>

      {/* Popular Locations */}
      <section style={{ padding: '3rem 0' }}>
        <Container>
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h5 className="fw-bold mb-0">Popular Locations</h5>
            <Link to="/generate-token" className="text-decoration-none" style={{ color: 'var(--primary)', fontWeight: 500, fontSize: '0.9rem' }}>
              View All <ChevronRight size={16} />
            </Link>
          </div>
          <Row className="g-3">
            {Array.isArray(locations) && locations.slice(0, 6).map((loc) => (
              <Col key={loc.id} xs={6} sm={4} md={3} lg={2}>
                <Link to="/generate-token" className="text-decoration-none">
                  <div className="location-card">
                    <img
                      src={loc.imageUrl || `https://images.unsplash.com/photo-1548013146-72479768bada?w=400`}
                      alt={loc.name}
                      onError={(e) => { e.target.src = 'https://images.unsplash.com/photo-1548013146-72479768bada?w=400'; }}
                    />
                    <div className="card-body">
                      <div className="card-title">{loc.name.length > 20 ? loc.name.substring(0, 20) + '...' : loc.name}</div>
                      <div className="card-text">
                        {typeIcons[loc.type] || '📍'} {loc.description || loc.type}
                      </div>
                    </div>
                  </div>
                </Link>
              </Col>
            ))}
          </Row>
        </Container>
      </section>

      {/* How It Works */}
      <section style={{ padding: '3rem 0', background: '#fff' }}>
        <Container>
          <h5 className="fw-bold text-center mb-4">How It Works</h5>
          <Row className="g-4">
            {[
              { step: '1', title: 'Select Location & Service', desc: 'Choose your destination and the service you need.' },
              { step: '2', title: 'Get Your Digital Token', desc: 'Receive a unique token number with estimated wait time.' },
              { step: '3', title: 'Track Live Queue', desc: 'Monitor your position in real-time from anywhere.' },
              { step: '4', title: 'Get Notified', desc: 'Receive a notification when your turn is approaching.' },
            ].map((item) => (
              <Col key={item.step} sm={6} lg={3}>
                <div className="text-center">
                  <div style={{
                    width: 48, height: 48, borderRadius: '50%', background: 'var(--primary)',
                    color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontWeight: 700, fontSize: '1.1rem', margin: '0 auto 0.75rem'
                  }}>{item.step}</div>
                  <h6 className="fw-semibold">{item.title}</h6>
                  <p className="text-muted" style={{ fontSize: '0.85rem' }}>{item.desc}</p>
                </div>
              </Col>
            ))}
          </Row>
        </Container>
      </section>
    </div>
  );
}
