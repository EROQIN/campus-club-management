import api from './http';
import type { ResourceApplicationResponse, SharedResource } from '../types/models';

export const fetchResources = async () => {
  const { data } = await api.get<SharedResource[]>('/api/resources');
  return data;
};

export const fetchResourcesByClub = async (clubId: number) => {
  const { data } = await api.get<SharedResource[]>(`/api/clubs/${clubId}/resources`);
  return data;
};

export const createResource = async (clubId: number, payload: Record<string, unknown>) => {
  const { data } = await api.post<SharedResource>(`/api/clubs/${clubId}/resources`, payload);
  return data;
};

export const updateResource = async (id: number, payload: Record<string, unknown>) => {
  const { data } = await api.put<SharedResource>(`/api/resources/${id}`, payload);
  return data;
};

export const deleteResource = async (id: number) => {
  await api.delete(`/api/resources/${id}`);
};

export const applyResource = async (
  resourceId: number,
  payload: { purpose: string; requestedFrom?: string; requestedUntil?: string },
) => {
  const { data } = await api.post<ResourceApplicationResponse>(
    `/api/resources/${resourceId}/applications`,
    payload,
  );
  return data;
};

export const listMyResourceApplications = async () => {
  const { data } = await api.get<ResourceApplicationResponse[]>(`/api/resources/applications/my`);
  return data;
};

export const listResourceApplications = async (resourceId: number) => {
  const { data } = await api.get<ResourceApplicationResponse[]>(
    `/api/resources/${resourceId}/applications`,
  );
  return data;
};

export const decideResourceApplication = async (
  applicationId: number,
  payload: { approve: boolean; replyMessage?: string },
) => {
  const { data } = await api.post<ResourceApplicationResponse>(
    `/api/resources/applications/${applicationId}/decision`,
    payload,
  );
  return data;
};

export const cancelResourceApplication = async (applicationId: number) => {
  await api.delete(`/api/resources/applications/${applicationId}`);
};
