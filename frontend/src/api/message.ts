import api from './http';
import type { MessageRecord } from '../types/models';

export const fetchMessages = async () => {
  const { data } = await api.get<MessageRecord[]>('/api/messages');
  return data;
};

export const markMessage = async (id: number, read: boolean) => {
  const { data } = await api.post<MessageRecord>(`/api/messages/${id}/read`, { read });
  return data;
};

export const fetchUnreadCount = async () => {
  const { data } = await api.get<number>('/api/messages/unread-count');
  return data;
};
