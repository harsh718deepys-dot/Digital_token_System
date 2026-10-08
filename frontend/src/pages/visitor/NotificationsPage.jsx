import { useState, useEffect } from 'react';
import { Container, Row, Col, Button } from 'react-bootstrap';
import { Bell, CheckCircle2, Info, AlertTriangle } from 'lucide-react';
import { tokenAPI } from '../../services/api';

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchNotifications = () => {
    tokenAPI.getNotifications()
      .then(res => setNotifications(res.data))
      .catch(() => setNotifications([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchNotifications();
  }, []);

  const markAsRead = (id) => {
    tokenAPI.markNotificationRead(id).then(() => {
      setNotifications(notifications.map(n => n.id === id ? { ...n, read: true } : n));
    });
  };

  const getIcon = (type) => {
    switch (type) {
      case 'TOKEN_GENERATED': return <CheckCircle2 size={18} />;
      case 'TOKEN_CALLED': return <Bell size={18} />;
      case 'TOKEN_NEAR': return <AlertTriangle size={18} />;
      default: return <Info size={18} />;
    }
  };

  const getIconClass = (type) => {
    switch (type) {
      case 'TOKEN_GENERATED': return 'success';
      case 'TOKEN_CALLED': return 'info';
      case 'TOKEN_NEAR': return 'warning';
      default: return 'info';
    }
  };

  const timeAgo = (dateStr) => {
    const min = Math.round((new Date() - new Date(dateStr)) / 60000);
    if (min < 60) return `${min}m ago`;
    if (min < 1440) return `${Math.round(min / 60)}h ago`;
    return `${Math.round(min / 1440)}d ago`;
  };

  return (
    <Container className="py-4">
      <Row className="justify-content-center">
        <Col md={8} lg={6}>
          <div className="d-flex justify-content-between align-items-center mb-4">
            <h4 className="fw-bold mb-0">Notifications</h4>
            {notifications.some(n => !n.read) && (
              <Button variant="link" size="sm" className="text-decoration-none p-0"
                onClick={() => {
                  notifications.filter(n => !n.read).forEach(n => markAsRead(n.id));
                }}>
                Mark all as read
              </Button>
            )}
          </div>

          <div className="card-clean">
            <div className="card-body p-0">
              {loading ? (
                <div className="p-5 text-center">
                  <div className="spinner-border text-primary" />
                </div>
              ) : notifications.length === 0 ? (
                <div className="p-5 text-center text-muted">
                  <Bell size={32} className="mb-2 opacity-50" />
                  <p className="mb-0">No notifications yet.</p>
                </div>
              ) : (
                notifications.map((n) => (
                  <div key={n.id} className={`notification-item ${!n.read ? 'unread' : ''}`}
                    onClick={() => !n.read && markAsRead(n.id)}
                    style={{ cursor: !n.read ? 'pointer' : 'default' }}>
                    <div className={`notification-icon ${getIconClass(n.type)}`}>
                      {getIcon(n.type)}
                    </div>
                    <div className="notification-content flex-grow-1">
                      <div className="d-flex justify-content-between align-items-start">
                        <div className="title">{n.title}</div>
                        {!n.read && <div style={{ width: 8, height: 8, background: 'var(--primary)', borderRadius: '50%' }} />}
                      </div>
                      <div className="message">{n.message}</div>
                      <div className="time">{timeAgo(n.createdAt)}</div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </Col>
      </Row>
    </Container>
  );
}
