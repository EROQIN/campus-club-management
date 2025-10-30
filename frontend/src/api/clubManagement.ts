import api from './http';
import type { ActivityDetail, ActivitySummary, PageResponse, ClubSummary } from '../types/models';

export const fetchMyClubs = async () => {
  const { data } = await api.get<ClubSummary[]>('/api/clubs/my');
  return data;
};

export const fetchClubActivities = async (clubId: number, params: { page?: number; size?: number } = {}) => {
  const { data } = await api.get<PageResponse<ActivitySummary>>(`/api/clubs/${clubId}/activities`, {
    params,
  });
  return data;
};

export const createActivity = async (clubId: number, payload: Record<string, unknown>) => {
  const { data } = await api.post<ActivityDetail>(`/api/clubs/${clubId}/activities`, payload);
  return data;
};

export const updateActivity = async (activityId: number, payload: Record<string, unknown>) => {
  const { data } = await api.put<ActivityDetail>(`/api/activities/${activityId}`, payload);
  return data;
};

export const deleteActivity = async (activityId: number) => {
  await api.delete(`/api/activities/${activityId}`);
};
