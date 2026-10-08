import { Outlet, Link, useNavigate } from 'react-router-dom';
import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';
import { LayoutGrid, Bell } from 'lucide-react';

export default function VisitorLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <>
      <Navbar expand="lg" className="navbar-main" variant="dark">
        <Container>
          <Navbar.Brand as={Link} to="/">
            <LayoutGrid size={22} /> TokenQueue
          </Navbar.Brand>
          <Navbar.Toggle aria-controls="main-nav" />
          <Navbar.Collapse id="main-nav">
            <Nav className="me-auto">
              <Nav.Link as={Link} to="/">Home</Nav.Link>
              <Nav.Link as={Link} to="/generate-token">Services</Nav.Link>
              <Nav.Link as={Link} to="/live-queue">Live Queue</Nav.Link>
            </Nav>
            <Nav>
              {user ? (
                <>
                  {user.role === 'ADMIN' && (
                    <Nav.Link as={Link} to="/admin">Admin Panel</Nav.Link>
                  )}
                  <Nav.Link as={Link} to="/notifications">
                    <Bell size={18} />
                  </Nav.Link>
                  <Nav.Link as={Link} to="/history">History</Nav.Link>
                  <Button variant="outline-light" size="sm" className="ms-2" onClick={handleLogout}>
                    Logout
                  </Button>
                </>
              ) : (
                <>
                  <Button variant="outline-light" size="sm" className="me-2" as={Link} to="/login">
                    Login
                  </Button>
                  <Button variant="light" size="sm" as={Link} to="/register">
                    Register
                  </Button>
                </>
              )}
            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>
      <main>
        <Outlet />
      </main>
      <footer className="footer">
        <Container className="text-center">
          <p className="mb-1">&copy; 2024 TokenQueue. Digital Token & Crowd Management System.</p>
          <p className="mb-0" style={{ fontSize: '0.8rem', opacity: 0.7 }}>
            DSA Course Project — Queue, Priority Queue, Hashing, Stack, Array, Greedy, DP
          </p>
        </Container>
      </footer>
    </>
  );
}
