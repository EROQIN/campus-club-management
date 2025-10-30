import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '../store/auth';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { public: true },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/RegisterView.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    children: [
      { path: '', redirect: '/overview' },
      {
        path: 'overview',
        name: 'overview',
        component: () => import('../views/FeatureOverviewView.vue'),
      },
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('../views/DashboardView.vue'),
      },
      {
        path: 'clubs',
        name: 'clubs',
        component: () => import('../views/ClubsView.vue'),
      },
      {
        path: 'clubs/:id',
        name: 'club-detail',
        component: () => import('../views/ClubDetailView.vue'),
        props: true,
      },
      {
        path: 'activities',
        name: 'activities',
        component: () => import('../views/ActivitiesView.vue'),
      },
      {
        path: 'messages',
        name: 'messages',
        component: () => import('../views/MessagesView.vue'),
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('../views/ProfileView.vue'),
      },
      {
        path: 'resources',
        name: 'resources',
        component: () => import('../views/ResourcesView.vue'),
      },
      {
        path: 'club/manage',
        name: 'club-manage',
        component: () => import('../views/club/ClubManagementView.vue'),
        meta: { roles: ['CLUB_MANAGER', 'UNION_STAFF', 'SYSTEM_ADMIN'] },
      },
      {
        path: 'admin/users',
        name: 'admin-users',
        component: () => import('../views/AdminUsersView.vue'),
        meta: { roles: ['SYSTEM_ADMIN'] },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, _from, next) => {
  const auth = useAuthStore();
  if (!auth.initialised) {
    await auth.bootstrap();
  }
  if (to.meta.public) {
    if (auth.isAuthenticated && to.name === 'login') {
      next({ name: 'dashboard' });
    } else {
      next();
    }
    return;
  }
  if (!auth.isAuthenticated) {
    next({ name: 'login', query: { redirect: to.fullPath } });
    return;
  }
  const requiredRoles = (to.meta.roles as string[] | undefined) ?? [];
  if (requiredRoles.length > 0) {
    const hasRole = requiredRoles.some((role) => auth.roles.includes(role as any));
    if (!hasRole) {
      next({ name: 'dashboard' });
      return;
    }
  }
  next();
});

export default router;
