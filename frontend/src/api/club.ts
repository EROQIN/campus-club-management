import api from './http';
import type { ClubDetail, ClubSummary, PageResponse } from '../types/models';

export interface ClubQueryParams {
  page?: number;
  size?: number;
  keywords?: string;
  category?: string;
  tags?: string[];
}

export const fetchClubs = async (params: ClubQueryParams) => {
  const { data } = await api.get<PageResponse<ClubSummary>>('/api/clubs', {
    params,
    paramsSerializer: (parameters) => {
      const searchParams = new URLSearchParams();
      if (typeof parameters.page !== 'undefined') {
        searchParams.append('page', String(parameters.page));
      }
      if (typeof parameters.size !== 'undefined') {
        searchParams.append('size', String(parameters.size));
      }
      if (parameters.keywords) {
        searchParams.append('keywords', parameters.keywords);
      }
      if (parameters.category) {
        searchParams.append('category', parameters.category);
      }
      if (parameters.tags && parameters.tags.length) {
        parameters.tags
          .filter(
            (tag: string | null | undefined): tag is string =>
              Boolean(tag && tag.trim().length),
          )
          .forEach((tag: string) => searchParams.append('tags', tag));
      }
      return searchParams.toString();
    },
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
