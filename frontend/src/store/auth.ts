import { defineStore } from 'pinia';
import api from '../api/http';
import type { AuthResponse } from '../types/models';
import type { UserProfile, UserSummary } from '../types/user';

interface AuthState {
  token: string | null;
  user: UserSummary | null;
  profile: UserProfile | null;
  initialised: boolean;
  loading: boolean;
}

const TOKEN_KEY = 'ccm_token';
const USER_KEY = 'ccm_user';

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: null,
    user: null,
    profile: null,
    initialised: false,
    loading: false,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    roles: (state) => state.user?.roles ?? [],
  },
  actions: {
    async bootstrap() {
      if (this.initialised) return;
      const token = localStorage.getItem(TOKEN_KEY);
      const userRaw = localStorage.getItem(USER_KEY);
      if (token && userRaw) {
        this.token = token;
        this.user = JSON.parse(userRaw) as UserSummary;
        try {
          await this.fetchProfile();
        } catch (error) {
          this.logout();
        }
      }
      this.initialised = true;
    },
    async login(identifier: string, password: string) {
      this.loading = true;
      try {
        const { data } = await api.post<AuthResponse>('/api/auth/login', {
          identifier,
          password,
        });
        this.handleAuthSuccess(data);
        await this.fetchProfile();
      } finally {
        this.loading = false;
      }
    },
    async register(payload: {
      studentNumber?: string | null;
      fullName: string;
      email: string;
      password: string;
      interestTags: string[];
      registeringAsManager: boolean;
    }) {
      this.loading = true;
      try {
        const { data } = await api.post<AuthResponse>('/api/auth/register', payload);
        this.handleAuthSuccess(data);
        await this.fetchProfile();
      } finally {
        this.loading = false;
      }
    },
    handleAuthSuccess(response: AuthResponse) {
      this.token = response.token;
      this.user = response.user;
      localStorage.setItem(TOKEN_KEY, response.token);
      localStorage.setItem(USER_KEY, JSON.stringify(response.user));
    },
    logout() {
      this.token = null;
      this.user = null;
      this.profile = null;
      this.initialised = true;
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    },
    async fetchProfile() {
      const { data } = await api.get<UserProfile>('/api/users/me');
      this.profile = data;
      if (this.user) {
        this.user = {
          ...this.user,
          fullName: data.fullName,
          avatarUrl: data.avatarUrl,
        };
        localStorage.setItem(USER_KEY, JSON.stringify(this.user));
      }
    },
  },
});
