import api from './http';
import type { ClubDetail, ClubSummary, PageResponse } from '../types/models';

export interface ClubQueryParams {
  page?: number;
  size?: number;
  keywords?: string;
  category?: string;
}

export const fetchClubs = async (params: ClubQueryParams) => {
  const { data } = await api.get<PageResponse<ClubSummary>>('/api/clubs', {
    params,
  });
  return data;
};

export const fetchClub = async (id: number) => {
  const { data } = await api.get<ClubDetail>(`/api/clubs/${id}`);
  return data;
};

export const fetchMyClubs = async () => {
  const { data } = await api.get<ClubSummary[]>('/api/clubs/my');
  return data;
};

export const fetchClubRecommendations = async (params: { page?: number; size?: number } = {}) => {
  const { data } = await api.get<PageResponse<ClubSummary>>('/api/clubs/recommendations', {
    params,
  });
  return data;
};

export const createClub = async (payload: Record<string, unknown>) => {
  const { data } = await api.post<ClubDetail>('/api/clubs', payload);
  return data;
};

export const updateClub = async (id: number, payload: Record<string, unknown>) => {
  const { data } = await api.put<ClubDetail>(`/api/clubs/${id}`, payload);
  return data;
};

export const deleteClub = async (id: number) => {
  await api.delete(`/api/clubs/${id}`);
};
