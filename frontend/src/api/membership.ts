import api from './http';
import type { MembershipRecord } from '../types/models';

export const applyMembership = async (payload: { clubId: number; applicationReason?: string }) => {
  const { data } = await api.post<MembershipRecord>('/api/memberships', payload);
  return data;
};

export const decideMembership = async (
  membershipId: number,
  payload: { approve: boolean; membershipRole?: string; message?: string },
) => {
  const { data } = await api.post<MembershipRecord>(
    `/api/memberships/${membershipId}/decision`,
    payload,
  );
  return data;
};

export const withdrawMembership = async (membershipId: number) => {
  await api.delete(`/api/memberships/${membershipId}`);
};

export const fetchMyMemberships = async () => {
  const { data } = await api.get<MembershipRecord[]>('/api/memberships/my');
  return data;
};

export const fetchClubApplicants = async (clubId: number) => {
  const { data } = await api.get<MembershipRecord[]>(`/api/memberships/clubs/${clubId}`);
  return data;
};
