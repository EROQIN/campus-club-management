import api from './http';
import type {
  ActivityDetail,
  ActivityRegistrationResponse,
  ActivitySummary,
  PageResponse,
  CheckInQrResponse,
  ActivityCheckInRecord,
  ActivityArchive,
  ActivityArchiveSummary,
} from '../types/models';

export const fetchClubActivities = async (clubId: number, params: { page?: number; size?: number }) => {
  const { data } = await api.get<PageResponse<ActivitySummary>>(`/api/clubs/${clubId}/activities`, {
    params,
  });
  return data;
};

export const fetchUpcomingActivities = async (params: { page?: number; size?: number }) => {
  const { data } = await api.get<PageResponse<ActivitySummary>>('/api/activities/upcoming', {
    params,
  });
  return data;
};

export const fetchActivityDetail = async (id: number) => {
  const { data } = await api.get<ActivityDetail>(`/api/activities/${id}`);
  return data;
};

export const createActivity = async (clubId: number, payload: Record<string, unknown>) => {
  const { data } = await api.post<ActivityDetail>(`/api/clubs/${clubId}/activities`, payload);
  return data;
};

export const updateActivity = async (id: number, payload: Record<string, unknown>) => {
  const { data } = await api.put<ActivityDetail>(`/api/activities/${id}`, payload);
  return data;
};

export const deleteActivity = async (id: number) => {
  await api.delete(`/api/activities/${id}`);
};

export const registerActivity = async (id: number, payload: { note?: string }) => {
  const { data } = await api.post<ActivityRegistrationResponse>(
    `/api/activities/${id}/register`,
    payload,
  );
  return data;
};

export const reviewRegistration = async (id: number, approve: boolean) => {
  const { data } = await api.post<ActivityRegistrationResponse>(
    `/api/registrations/${id}/decision`,
    null,
    { params: { approve } },
  );
  return data;
};

export const fetchRegistrations = async (
  activityId: number,
  params: { page?: number; size?: number },
) => {
  const { data } = await api.get<PageResponse<ActivityRegistrationResponse>>(
    `/api/activities/${activityId}/registrations`,
    {
      params,
    },
  );
  return data;
};

export const generateCheckInQr = async (activityId: number) => {
  const { data } = await api.post<CheckInQrResponse>(
    `/api/activities/${activityId}/check-in/qr`,
  );
  return data;
};

export const fetchActivityCheckIns = async (
  activityId: number,
  params: { page?: number; size?: number },
) => {
  const { data } = await api.get<PageResponse<ActivityCheckInRecord>>(
    `/api/activities/${activityId}/check-ins`,
    { params },
  );
  return data;
};

export const submitActivityCheckIn = async (
  activityId: number,
  payload: { token: string; method?: string },
) => {
  const { data } = await api.post<ActivityCheckInRecord>(
    `/api/activities/${activityId}/check-in`,
    payload,
  );
  return data;
};

export const manualCheckIn = async (
  activityId: number,
  payload: { attendeeIds: number[] },
  params: { page?: number; size?: number } = {},
) => {
  const { data } = await api.post<PageResponse<ActivityCheckInRecord>>(
    `/api/activities/${activityId}/check-in/manual`,
    payload,
    { params },
  );
  return data;
};

export const exportAttendance = async (activityId: number) => {
  const { data } = await api.get<ArrayBuffer>(`/api/activities/${activityId}/attendance/export`, {
    responseType: 'arraybuffer',
  });
  return data;
};

export const archiveActivity = async (activityId: number, payload: { summary: string; photoUrls: string[] }) => {
  const { data } = await api.post<ActivityArchive>(`/api/activities/${activityId}/archive`, payload);
  return data;
};

export const fetchActivityArchive = async (activityId: number) => {
  const { data } = await api.get<ActivityArchive>(`/api/activities/${activityId}/archive`);
  return data;
};

export const fetchClubActivityArchives = async (
  clubId: number,
  params: { keywords?: string; start?: string; end?: string; page?: number; size?: number } = {},
) => {
  const { data } = await api.get<PageResponse<ActivityArchiveSummary>>(`/api/clubs/${clubId}/archives`, {
    params,
  });
  return data;
};

export const exportActivityArchivePdf = async (activityId: number) => {
  const { data } = await api.get<ArrayBuffer>(`/api/activities/${activityId}/archive/pdf`, {
    responseType: 'arraybuffer',
  });
  return data;
};
