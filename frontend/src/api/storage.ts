import api from './http';
import type { UploadResponse } from '../types/models';

export const uploadImage = async (file: File, directory?: string) => {
  const formData = new FormData();
  formData.append('file', file);
  if (directory) {
    formData.append('directory', directory);
  }
  const { data } = await api.post<UploadResponse>('/api/uploads/images', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data;
};
