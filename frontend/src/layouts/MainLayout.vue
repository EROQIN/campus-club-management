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
          <el-tooltip :content="isDarkMode ? '切换到明亮模式' : '切换到暗色模式'" placement="bottom">
            <el-button class="layout__theme-toggle" circle text @click="toggleTheme">
              <el-icon :size="18">
                <component :is="isDarkMode ? Moon : Sunny" />
              </el-icon>
            </el-button>
          </el-tooltip>
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
import { useThemeStore } from '../store/theme';
import { Collection, House, TrendCharts, Tickets, ChatLineSquare, Box, Setting, Sunny, Moon, Calendar, Finished, VideoCamera } from '@element-plus/icons-vue';
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
const themeStore = useThemeStore();
const breadcrumbs = ref<string[]>([]);

const menu: MenuItem[] = [
  { label: '功能总览', route: '/overview', icon: Collection },
  { label: '数据总览', route: '/dashboard', icon: TrendCharts },
  { label: '社团广场', route: '/clubs', icon: House },
  { label: '活动日历', route: '/activities', icon: Tickets },
  { label: '资源共享', route: '/resources', icon: Box },
  { label: '协作广场', route: '/collaborations', icon: ChatLineSquare },
  { label: '社团管理', route: '/club/manage', icon: Collection, roles: ['CLUB_MANAGER', 'UNION_STAFF', 'SYSTEM_ADMIN'] },
  { label: '活动管理', route: '/club/manage/activities', icon: Calendar, roles: ['CLUB_MANAGER', 'UNION_STAFF', 'SYSTEM_ADMIN'] },
  { label: '视频字幕', route: '/club/manage/video', icon: VideoCamera, roles: ['CLUB_MANAGER', 'UNION_STAFF', 'SYSTEM_ADMIN'] },
  { label: '签到管理', route: '/club/check-in-manager', icon: Finished, roles: ['CLUB_MANAGER', 'UNION_STAFF', 'SYSTEM_ADMIN'] },
  { label: '账号管理', route: '/admin/users', icon: Setting, roles: ['SYSTEM_ADMIN'] },
];

const filteredMenu = computed(() => {
  const roles = auth.roles;
  return menu.filter((item) => {
    if (!item.roles) return true;
    return item.roles.some((role) => roles.includes(role as any));
  });
});

const activeMenu = computed(() => {
  const currentPath = route.path;
  let matchedRoute = currentPath;
  let maxLength = -1;
  for (const item of filteredMenu.value) {
    const itemRoute = item.route;
    if (currentPath === itemRoute || currentPath.startsWith(`${itemRoute}/`) || currentPath.startsWith(itemRoute)) {
      if (itemRoute.length > maxLength) {
        maxLength = itemRoute.length;
        matchedRoute = itemRoute;
      }
    }
  }
  return matchedRoute;
});

const initials = computed(() => auth.user?.fullName?.slice(0, 1) ?? '访');
const isDarkMode = computed(() => themeStore.mode === 'dark');

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

const toggleTheme = () => {
  themeStore.toggleTheme();
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
  background: var(--ccm-sidebar-bg);
  color: var(--ccm-sidebar-text);
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--ccm-sidebar-border);
  box-shadow: var(--ccm-card-shadow);
}

.layout__brand {
  font-size: 18px;
  font-weight: 600;
  padding: 20px;
  text-align: center;
  background: var(--ccm-surface-gradient);
  color: var(--ccm-sidebar-text);
  border-bottom: 1px solid var(--ccm-sidebar-border);
}

.layout__menu {
  flex: 1;
  border-right: none;
  background-color: transparent;
  --el-menu-text-color: var(--ccm-sidebar-text);
  --el-menu-active-color: var(--ccm-primary);
  --el-menu-hover-bg-color: rgba(148, 163, 184, 0.16);
  --el-menu-bg-color: transparent;
}

.layout__menu :deep(.el-menu-item) {
  border-radius: 10px;
  margin: 0 12px;
  font-weight: 500;
  color: var(--ccm-sidebar-text);
}

.layout__menu :deep(.el-menu-item.is-active) {
  background-color: var(--ccm-primary-soft);
  color: var(--ccm-primary);
}

.layout__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--ccm-surface);
  border-bottom: 1px solid var(--ccm-border);
  padding: 0 20px;
  transition: background 0.35s ease, border-color 0.35s ease;
}

.layout__header-left {
  display: flex;
  align-items: center;
  color: var(--ccm-text-secondary);
}

.layout__header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.layout__theme-toggle {
  color: var(--ccm-text-secondary);
}

.layout__user {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--ccm-text-primary);
}

.layout__user-name {
  font-size: 14px;
  color: inherit;
}

.layout__main {
  background-color: var(--ccm-bg-page);
  padding: 24px;
  transition: background-color 0.35s ease;
}

.layout__header :deep(.el-breadcrumb__inner) {
  color: var(--ccm-text-secondary);
}

.layout__header :deep(.el-breadcrumb__separator) {
  color: var(--ccm-text-muted);
}
</style>
