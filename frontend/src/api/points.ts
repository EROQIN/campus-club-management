import api from './http';
import type { PageResponse } from '../types/models';
import type { PointRecord, PointLeaderboardEntry } from '../types/models';

export const addPointRecord = async (clubId: number, payload: { memberId: number; points: number; reason: string }) => {
  const { data } = await api.post<PointRecord>(`/api/clubs/${clubId}/points`, payload);
  return data;
};

export const fetchPointRecords = async (
  clubId: number,
  params: { page?: number; size?: number } = {},
) => {
  const { data } = await api.get<PageResponse<PointRecord>>(`/api/clubs/${clubId}/points`, { params });
  return data;
};

export const fetchPointLeaderboard = async (clubId: number, limit = 20) => {
  const { data } = await api.get<PointLeaderboardEntry[]>(`/api/clubs/${clubId}/points/leaderboard`, {
    params: { limit },
  });
  return data;
};
