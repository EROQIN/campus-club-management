import api from './http';
import type { AnnouncementRecord } from '../types/models';

export const fetchClubMessages = async (clubId: number) => {
  const { data } = await api.get<AnnouncementRecord[]>(`/api/clubs/${clubId}/messages`);
  return data;
};

export const broadcastClubMessage = async (clubId: number, payload: { title: string; content: string }) => {
  await api.post(`/api/clubs/${clubId}/messages`, payload);
};
