import api from './http';
import type { MembershipAdminResponse, MembershipRecord } from '../types/models';

export const fetchClubMembers = async (clubId: number) => {
  const { data } = await api.get<MembershipAdminResponse[]>(`/api/memberships/clubs/${clubId}/members`);
  return data;
};

export const fetchClubApplicants = async (clubId: number) => {
  const { data } = await api.get<MembershipRecord[]>(`/api/memberships/clubs/${clubId}`);
  return data;
};

export const approveMembership = async (membershipId: number, approve: boolean) => {
  await api.post(`/api/memberships/${membershipId}/decision`, { approve });
};
