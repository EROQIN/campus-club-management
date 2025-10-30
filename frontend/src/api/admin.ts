import api from './http';
import type { UserAdmin } from '../types/models';

export const fetchUsers = async (keyword?: string) => {
  const { data } = await api.get<UserAdmin[]>('/api/admin/users', {
    params: keyword ? { keyword } : undefined,
  });
  return data;
};

export const updateUserRoles = async (id: number, roles: string[]) => {
  const { data } = await api.put<UserAdmin>(`/api/admin/users/${id}/roles`, { roles });
  return data;
};

export const updateUserStatus = async (id: number, enabled: boolean) => {
  const { data } = await api.put<UserAdmin>(`/api/admin/users/${id}/status`, { enabled });
  return data;
};

