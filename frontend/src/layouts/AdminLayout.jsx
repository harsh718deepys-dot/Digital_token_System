import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard, Ticket, List, Monitor, Users, BarChart3,
  History, Settings, LogOut, LayoutGrid
} from 'lucide-react';

const sidebarItems = [
  { path: '/admin', label: 'Dashboard', icon: LayoutDashboard, exact: true },
  { path: '/admin/tokens', label: 'Token Management', icon: Ticket },
  { path: '/admin/queue', label: 'Live Queue', icon: List },
  { path: '/admin/counters', label: 'Counters', icon: Monitor },
  { path: '/admin/statistics', label: 'Statistics', icon: BarChart3 },
  { path: '/admin/history', label: 'History', icon: History },
];

export default function AdminLayout() {
  const { logout, user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const isActive = (item) => {
    if (item.exact) return location.pathname === item.path;
    return location.pathname.startsWith(item.path);
  };

  return (
    <div className="admin-layout">
      <aside className="admin-sidebar">
        <div className="sidebar-brand">
          <LayoutGrid size={22} /> TokenQueue
        </div>
        <nav className="sidebar-nav">
          {sidebarItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={`nav-link ${isActive(item) ? 'active' : ''}`}
            >
              <item.icon size={18} />
              {item.label}
            </Link>
          ))}
          <hr style={{ margin: '0.5rem 1rem', borderColor: '#e5e7eb' }} />
          <Link to="/" className="nav-link">
            <LayoutGrid size={18} /> Visitor View
          </Link>
          <span className="nav-link" style={{ cursor: 'pointer', color: '#dc2626' }} onClick={handleLogout}>
            <LogOut size={18} /> Logout
          </span>
        </nav>
        {user && (
          <div style={{ padding: '1rem 1.25rem', borderTop: '1px solid #e5e7eb', marginTop: 'auto', position: 'absolute', bottom: 0, width: '100%' }}>
            <small className="text-muted">Logged in as</small>
            <div style={{ fontWeight: 600, fontSize: '0.85rem' }}>{user.fullName}</div>
          </div>
        )}
      </aside>
      <main className="admin-content">
        <Outlet />
      </main>
    </div>
  );
}
