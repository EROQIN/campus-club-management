import api from './http';
import type { MembershipRecord } from '../types/models';

export interface ApplyMembershipPayload {
  clubId: number;
  applicationReason?: string;
}

export interface MembershipDecisionPayload {
  approve: boolean;
  message?: string;
}

export const applyMembership = async (payload: ApplyMembershipPayload) => {
  const { data } = await api.post<MembershipRecord>('/api/memberships', payload);
  return data;
};

export const decideMembership = async (membershipId: number, payload: MembershipDecisionPayload) => {
  const { data } = await api.post<MembershipRecord>(`/api/memberships/${membershipId}/decision`, payload);
  return data;
};

export const fetchMyMemberships = async () => {
  const { data } = await api.get<MembershipRecord[]>('/api/memberships/my');
  return data;
};

export const fetchClubApplicants = async (clubId: number) => {
  const { data } = await api.get<MembershipRecord[]>(`/api/memberships/clubs/${clubId}`);
  return data;
};
