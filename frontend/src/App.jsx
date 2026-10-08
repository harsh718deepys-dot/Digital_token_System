import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';

// Layouts
import VisitorLayout from './layouts/VisitorLayout';
import AdminLayout from './layouts/AdminLayout';

// Visitor Pages
import LandingPage from './pages/visitor/LandingPage';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import GenerateToken from './pages/visitor/GenerateToken';
import LiveQueue from './pages/visitor/LiveQueue';
import TokenHistory from './pages/visitor/TokenHistory';
import NotificationsPage from './pages/visitor/NotificationsPage';

// Admin Pages
import Dashboard from './pages/admin/Dashboard';
import TokenManagement from './pages/admin/TokenManagement';
import AdminLiveQueue from './pages/admin/AdminLiveQueue';
import Counters from './pages/admin/Counters';
import Statistics from './pages/admin/Statistics';
import AdminHistory from './pages/admin/AdminHistory';

function ProtectedRoute({ children, requireAdmin = false }) {
  const { user, loading } = useAuth();
  if (loading) return <div className="d-flex justify-content-center p-5"><div className="spinner-border text-primary" /></div>;
  if (!user) return <Navigate to="/login" />;
  if (requireAdmin && user.role !== 'ADMIN') return <Navigate to="/" />;
  return children;
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public / Visitor Routes */}
          <Route element={<VisitorLayout />}>
            <Route path="/" element={<LandingPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/generate-token" element={
              <ProtectedRoute><GenerateToken /></ProtectedRoute>
            } />
            <Route path="/live-queue" element={
              <ProtectedRoute><LiveQueue /></ProtectedRoute>
            } />
            <Route path="/history" element={
              <ProtectedRoute><TokenHistory /></ProtectedRoute>
            } />
            <Route path="/notifications" element={
              <ProtectedRoute><NotificationsPage /></ProtectedRoute>
            } />
          </Route>

          {/* Admin Routes */}
          <Route path="/admin" element={
            <ProtectedRoute requireAdmin><AdminLayout /></ProtectedRoute>
          }>
            <Route index element={<Dashboard />} />
            <Route path="tokens" element={<TokenManagement />} />
            <Route path="queue" element={<AdminLiveQueue />} />
            <Route path="counters" element={<Counters />} />
            <Route path="statistics" element={<Statistics />} />
            <Route path="history" element={<AdminHistory />} />
          </Route>

          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
