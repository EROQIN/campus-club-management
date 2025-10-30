import api from './http';
import type { CollaborationProposalItem, PageResponse } from '../types/models';

export const createCollaborationProposal = async (
  clubId: number,
  payload: { title: string; description: string; collaborationType?: string; requiredResources?: string },
) => {
  const { data } = await api.post<CollaborationProposalItem>(`/api/collaborations/clubs/${clubId}`, payload);
  return data;
};

export const fetchMyCollaborationProposals = async (
  clubId: number,
  params: { page?: number; size?: number } = {},
) => {
  const { data } = await api.get<PageResponse<CollaborationProposalItem>>(
    `/api/collaborations/clubs/${clubId}`,
    { params },
  );
  return data;
};

export const fetchCollaborationProposals = async (params: { page?: number; size?: number } = {}) => {
  const { data } = await api.get<PageResponse<CollaborationProposalItem>>(`/api/collaborations`, { params });
  return data;
};

export const respondCollaborationProposal = async (
  proposalId: number,
  clubId: number,
  payload: { message: string; status: string },
) => {
  const { data } = await api.post<CollaborationProposalItem>(
    `/api/collaborations/${proposalId}/respond/${clubId}`,
    payload,
  );
  return data;
};
