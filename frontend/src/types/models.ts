import type { Role } from './user';

export interface ClubSummary {
  id: number;
  name: string;
  description: string;
  logoUrl?: string | null;
  category?: string | null;
  memberCount: number;
  activityCount: number;
  tags: string[];
  recommendationScore?: number;
}

export interface ClubDetail extends ClubSummary {
  promoVideoUrl?: string | null;
  contactEmail?: string | null;
  contactPhone?: string | null;
  foundedDate?: string | null;
  createdAt: string;
  pendingApplicants: number;
  activityCountLast30Days: number;
  managerName?: string | null;
  recentActivities: ActivitySummary[];
}

export interface ActivitySummary {
  id: number;
  title: string;
  clubName: string;
  description: string;
  startTime: string | null;
  endTime: string | null;
  location?: string | null;
  attendeeCount: number;
  capacity?: number | null;
  requiresApproval?: boolean;
}

export interface ActivityDetail extends ActivitySummary {
  clubId: number;
  clubName: string;
  description: string;
  capacity?: number | null;
  bannerUrl?: string | null;
  requiresApproval: boolean;
}

export interface ActivityRegistrationResponse {
  id: number;
  activityId: number;
  activityTitle: string;
  status: 'PENDING' | 'APPROVED' | 'DECLINED' | 'CANCELLED' | 'ATTENDED';
  note?: string | null;
  createdAt: string;
}

export interface MembershipRecord {
  id: number;
  clubId: number;
  clubName: string;
  memberId?: number;
  memberName?: string;
  memberEmail?: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'WITHDRAWN';
  membershipRole: 'MEMBER' | 'LEADER' | 'STAFF' | 'ADVISOR';
  applicationReason?: string | null;
  createdAt: string;
  respondedAt?: string | null;
}

export interface MembershipAdminResponse extends MembershipRecord {
  memberId: number;
  memberName: string;
  memberEmail: string;
}

export interface MessageRecord {
  id: number;
  type: 'MEMBERSHIP' | 'ACTIVITY' | 'ANNOUNCEMENT' | 'TASK' | 'SYSTEM';
  title: string;
  content: string;
  read: boolean;
  createdAt: string;
  readAt?: string | null;
  referenceType?: string | null;
  referenceId?: number | null;
}

export interface AnnouncementRecord {
  id: number;
  title: string;
  content: string;
  authorId?: number | null;
  authorName?: string | null;
  createdAt: string;
}

export interface SharedResource {
  id: number;
  clubId: number;
  clubName: string;
  name: string;
  resourceType: string;
  description?: string | null;
  availableFrom?: string | null;
  availableUntil?: string | null;
  contact?: string | null;
}

export interface DashboardMetrics {
  totalClubs: number;
  newClubsThisSemester: number;
  totalMembers: number;
  activeMembersLast30Days: number;
  totalActivitiesThisMonth: number;
  upcomingActivities: number;
  topActiveClubs: Array<{
    clubId: number;
    clubName: string;
    activityCount: number;
    memberCount: number;
  }>;
  activityTrend: Array<{
    month: string;
    activityCount: number;
  }>;
  activityCategoryDistribution: Array<{
    category: string;
    value: number;
  }>;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface AuthResponse {
  token: string;
  expiresIn: number;
  user: {
    id: number;
    fullName: string;
    email: string;
    avatarUrl?: string | null;
    roles: Role[];
  };
}

export interface ResourceApplicationResponse {
  id: number;
  resourceId: number;
  resourceName: string;
  applicantId: number;
  applicantName: string;
  purpose: string;
  requestedFrom?: string | null;
  requestedUntil?: string | null;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
  replyMessage?: string | null;
  createdAt: string;
  respondedAt?: string | null;
}

export interface UserAdmin {
  id: number;
  fullName: string;
  email: string;
  studentNumber?: string | null;
  enabled: boolean;
  roles: Role[] | string[];
  createdAt: string;
  lastLoginAt?: string | null;
}
