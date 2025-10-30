export type Role = 'STUDENT' | 'CLUB_MANAGER' | 'UNION_STAFF' | 'SYSTEM_ADMIN';

export interface UserSummary {
  id: number;
  fullName: string;
  email: string;
  avatarUrl?: string | null;
  roles: Role[];
}

export interface UserProfile extends UserSummary {
  studentNumber?: string | null;
  bio?: string | null;
  enabled: boolean;
  interests: string[];
  createdAt: string;
}
