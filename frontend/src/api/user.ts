import api from './http';
import type { UserProfile } from '../types/user';

export const updateProfile = async (payload: Partial<UserProfile> & { interestTags?: string[] }) => {
  const { data } = await api.put<UserProfile>('/api/users/me', payload);
  return data;
};
