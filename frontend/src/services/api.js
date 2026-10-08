import axios from 'axios';

const API_BASE = '/api';

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
});

// Add JWT token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 responses
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// Auth APIs
export const authAPI = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  me: () => api.get('/auth/me'),
};

// Location APIs
export const locationAPI = {
  getAll: () => api.get('/locations'),
  getById: (id) => api.get(`/locations/${id}`),
  getServices: (id) => api.get(`/locations/${id}/services`),
};

// Token APIs
export const tokenAPI = {
  create: (data) => api.post('/tokens', data),
  getById: (id) => api.get(`/tokens/${id}`),
  getByNumber: (num) => api.get(`/tokens/number/${num}`),
  getActive: () => api.get('/tokens/active'),
  cancel: (id) => api.delete(`/tokens/${id}`),
  getHistory: () => api.get('/tokens/history'),
  getNotifications: () => api.get('/tokens/notifications'),
  markNotificationRead: (id) => api.put(`/tokens/notifications/${id}/read`),
};

// Queue APIs
export const queueAPI = {
  getStatus: (serviceId) => api.get(`/queue/${serviceId}`),
};

// Admin APIs
export const adminAPI = {
  getDashboard: () => api.get('/admin/dashboard'),
  getAllTokens: () => api.get('/admin/tokens'),
  callNext: (data) => api.post('/admin/queue/call-next', data),
  completeToken: (id) => api.post(`/admin/tokens/${id}/complete`),
  getCounters: () => api.get('/admin/counters'),
  addCounter: (data) => api.post('/admin/counters', data),
  updateCounter: (id, data) => api.put(`/admin/counters/${id}`, data),
  deleteCounter: (id) => api.delete(`/admin/counters/${id}`),
  getStatistics: () => api.get('/admin/statistics'),
  getHistory: () => api.get('/admin/history'),
  optimize: (locationId) => api.post(`/admin/optimize/${locationId}`),
};

export default api;
