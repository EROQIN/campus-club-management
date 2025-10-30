import api from './http';
import type { DashboardMetrics } from '../types/models';

export const fetchDashboardMetrics = async () => {
  const { data } = await api.get<DashboardMetrics>('/api/dashboard/overview');
  return data;
};
