import api from './http';
import type { ClubTaskItem, PageResponse } from '../types/models';

export const createTask = async (clubId: number, payload: Record<string, unknown>) => {
  const { data } = await api.post<ClubTaskItem>(`/api/clubs/${clubId}/tasks`, payload);
  return data;
};

export const updateTask = async (clubId: number, taskId: number, payload: Record<string, unknown>) => {
  const { data } = await api.put<ClubTaskItem>(`/api/clubs/${clubId}/tasks/${taskId}`, payload);
  return data;
};

export const listTasks = async (
  clubId: number,
  params: { page?: number; size?: number } = {},
) => {
  const { data } = await api.get<PageResponse<ClubTaskItem>>(`/api/clubs/${clubId}/tasks`, { params });
  return data;
};

export const getTask = async (clubId: number, taskId: number) => {
  const { data } = await api.get<ClubTaskItem>(`/api/clubs/${clubId}/tasks/${taskId}`);
  return data;
};

export const updateAssignmentStatus = async (
  clubId: number,
  taskId: number,
  assignmentId: number,
  payload: { status: string; remark?: string },
) => {
  const { data } = await api.put<ClubTaskItem>(
    `/api/clubs/${clubId}/tasks/${taskId}/assignments/${assignmentId}`,
    payload,
  );
  return data;
};
