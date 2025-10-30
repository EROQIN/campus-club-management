<template>
  <el-container class="layout">
    <el-aside width="220px" class="layout__sidebar">
      <div class="layout__brand">校园社团中心</div>
      <el-menu
        :default-active="activeMenu"
        class="layout__menu"
        @select="handleMenuSelect"
        router
      >
        <el-menu-item
          v-for="item in filteredMenu"
          :key="item.route"
          :index="item.route"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout__header">
        <div class="layout__header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="(breadcrumb, index) in breadcrumbs" :key="index">
              {{ breadcrumb }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="layout__header-right">
          <el-badge :value="unreadCount" :hidden="!unreadCount">
            <el-button text @click="navigateMessages">消息</el-button>
          </el-badge>
          <el-dropdown>
            <span class="layout__user">
              <el-avatar :size="32">
                {{ initials }}
              </el-avatar>
              <span class="layout__user-name">{{ auth.user?.fullName }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goProfile">个人资料</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="layout__main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter, RouterView } from 'vue-router';
import { useAuthStore } from '../store/auth';
import { House, TrendCharts, Tickets, ChatDotSquare, User, Box, Setting } from '@element-plus/icons-vue';
import { fetchUnreadCount } from '../api/message';

interface MenuItem {
  label: string;
  route: string;
  icon: any;
  roles?: string[];
}

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();
const unreadCount = ref<number>(0);
const breadcrumbs = ref<string[]>([]);

const menu: MenuItem[] = [
  { label: '数据总览', route: '/dashboard', icon: TrendCharts },
  { label: '社团广场', route: '/clubs', icon: House },
  { label: '活动日历', route: '/activities', icon: Tickets },
  { label: '消息中心', route: '/messages', icon: ChatDotSquare },
  { label: '资源共享', route: '/resources', icon: Box },
  { label: '账号管理', route: '/admin/users', icon: Setting, roles: ['SYSTEM_ADMIN'] },
  { label: '个人资料', route: '/profile', icon: User },
];

const filteredMenu = computed(() => {
  const roles = auth.roles;
  return menu.filter((item) => {
    if (!item.roles) return true;
    return item.roles.some((role) => roles.includes(role as any));
  });
});

const activeMenu = computed(() => {
  const match = filteredMenu.value.find((item) => route.path.startsWith(item.route));
  return match ? match.route : route.path;
});

const initials = computed(() => auth.user?.fullName?.slice(0, 1) ?? '访');

const handleMenuSelect = (index: string) => {
  router.push(index);
};

const navigateMessages = () => {
  router.push('/messages');
};

const goProfile = () => {
  router.push('/profile');
};

const logout = () => {
  auth.logout();
  router.replace('/login');
};

const buildBreadcrumbs = () => {
  const crumbs: string[] = [];
  const current = menu.find((item) => route.path.startsWith(item.route));
  if (current) {
    crumbs.push(current.label);
  }
  breadcrumbs.value = crumbs;
};

const loadUnread = async () => {
  if (!auth.isAuthenticated) return;
  try {
    unreadCount.value = await fetchUnreadCount();
  } catch (error) {
    console.error('加载未读消息失败', error);
  }
};

onMounted(async () => {
  await auth.bootstrap();
  buildBreadcrumbs();
  await loadUnread();
});

watch(
  () => route.path,
  () => {
    buildBreadcrumbs();
  },
);
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.layout__sidebar {
  background-color: #0f172a;
  color: #eef2ff;
  display: flex;
  flex-direction: column;
}

.layout__brand {
  font-size: 18px;
  font-weight: 600;
  padding: 20px;
  text-align: center;
}

.layout__menu {
  flex: 1;
  border-right: none;
  background-color: transparent;
}

.layout__menu :deep(.el-menu-item.is-active) {
  background-color: rgba(255, 255, 255, 0.12);
}

.layout__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
}

.layout__header-left {
  display: flex;
  align-items: center;
}

.layout__header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.layout__user {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.layout__user-name {
  font-size: 14px;
  color: #1f2937;
}

.layout__main {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>
