import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
  timeout: 15000,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('ccm_token');
  if (token) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('ccm_token');
      localStorage.removeItem('ccm_user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  },
);

export default api;
