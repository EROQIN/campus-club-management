<template>
  <div class="overview">
    <section class="overview__hero">
      <div class="overview__hero-text">
        <h1>校园社团功能总览</h1>
        <p>
          这里汇集了当前系统已实现的全部能力，帮助不同角色快速了解可以使用的工具与流程。
          选择角色视角，即可查看对应的工作台与可视化看板入口。
        </p>
        <div class="overview__hero-actions">
          <el-button type="primary" size="large" @click="goto('/dashboard')">进入数据总览</el-button>
          <el-button size="large" @click="goto('/clubs')">探索社团生态</el-button>
        </div>
      </div>
      <el-card shadow="never" class="overview__hero-card">
        <template #header>
          <div class="overview__hero-card-title">关键数据即时概览</div>
        </template>
        <el-skeleton :loading="metricsLoading" animated :rows="3">
          <div class="overview__stats">
            <div v-for="card in metricsCards" :key="card.title" class="overview__stat">
              <div class="overview__stat-value">{{ card.value }}</div>
              <div class="overview__stat-label">{{ card.title }}</div>
              <div class="overview__stat-desc">{{ card.subtitle }}</div>
            </div>
          </div>
        </el-skeleton>
      </el-card>
    </section>

    <el-card shadow="never" class="overview__filters">
      <div class="overview__filters-header">
        <div class="overview__filters-text">
          <h2>角色视角</h2>
          <p>系统支持多种角色协同工作，切换视角查看对应功能入口。</p>
        </div>
        <el-radio-group v-model="selectedFilter" size="large">
          <el-radio-button v-for="option in roleFilters" :key="option.value" :label="option.value">
            {{ option.label }}
          </el-radio-button>
        </el-radio-group>
      </div>
    </el-card>

    <div class="overview__features">
      <el-empty v-if="!filteredFeatures.length" description="该视角下暂无功能" />
      <el-row v-else :gutter="20">
        <el-col v-for="feature in filteredFeatures" :key="feature.id" :span="8">
          <el-card
            shadow="hover"
            :class="['overview__feature-card', { 'overview__feature-card--inactive': !isFeatureActive(feature) }]"
          >
            <div class="overview__feature-icon" :style="{ background: feature.color }">
              <span>{{ feature.emoji }}</span>
            </div>
            <div class="overview__feature-content">
              <div class="overview__feature-header">
                <h3>{{ feature.title }}</h3>
                <el-tag
                  size="small"
                  :type="isFeatureActive(feature) ? 'success' : 'info'"
                >
                  {{ featureStatusText(feature) }}
                </el-tag>
              </div>
              <p class="overview__feature-desc">{{ feature.description }}</p>
              <div class="overview__feature-meta">
                <el-tag
                  v-for="role in displayRoles(feature)"
                  :key="role"
                  size="small"
                  effect="plain"
                >
                  {{ role }}
                </el-tag>
              </div>
              <div class="overview__feature-actions">
                <el-button
                  v-if="feature.route"
                  link
                  type="primary"
                  :disabled="!isFeatureActive(feature)"
                  @click="goto(feature.route!)"
                >
                  {{ feature.action ?? '立即前往' }}
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="20" class="overview__journey">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="overview__panel-title">学生用户体验路径</div>
          </template>
          <el-steps direction="vertical" :active="studentJourney.length">
            <el-step
              v-for="(step, index) in studentJourney"
              :key="step.title"
              :title="`${index + 1}. ${step.title}`"
              :description="step.description"
            />
          </el-steps>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="overview__panel-title">社团负责人工作台</div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="item in managerTimeline"
              :key="item.title"
              :type="item.type"
              :timestamp="item.timestamp"
            >
              <h4>{{ item.title }}</h4>
              <p>{{ item.description }}</p>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>
        <div class="overview__panel-title">功能清单与亮点</div>
      </template>
      <el-collapse accordion>
        <el-collapse-item
          v-for="group in capabilityGroups"
          :key="group.title"
          :name="group.title"
        >
          <template #title>
            <span class="overview__collapse-title">{{ group.title }}</span>
            <el-tag size="small" effect="plain">{{ group.subtitle }}</el-tag>
          </template>
          <ul class="overview__capability-list">
            <li v-for="item in group.items" :key="item" class="overview__capability-item">{{ item }}</li>
          </ul>
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../store/auth';
import { fetchDashboardMetrics } from '../api/dashboard';
import type { DashboardMetrics } from '../types/models';
import type { Role } from '../types/user';

type RoleFilter = 'MY' | 'ALL' | Role;

interface FeatureCard {
  id: string;
  title: string;
  description: string;
  roles: Role[];
  emoji: string;
  color: string;
  route?: string;
  action?: string;
}

interface TimelineItem {
  title: string;
  timestamp: string;
  description: string;
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info';
}

const router = useRouter();
const auth = useAuthStore();

const metrics = ref<DashboardMetrics | null>(null);
const metricsLoading = ref(false);
const selectedFilter = ref<RoleFilter>('MY');

const roleFilters: Array<{ label: string; value: RoleFilter }> = [
  { label: '我的视角', value: 'MY' },
  { label: '学生', value: 'STUDENT' },
  { label: '社团负责人', value: 'CLUB_MANAGER' },
  { label: '团委', value: 'UNION_STAFF' },
  { label: '系统管理员', value: 'SYSTEM_ADMIN' },
  { label: '全部功能', value: 'ALL' },
];

const featureCards: FeatureCard[] = [
  {
    id: 'recommend',
    title: '智能社团推荐',
    description: '基于兴趣标签与社团活跃度计算匹配度，推荐最契合的新生社团，并支持查看详情与申请。',
    roles: ['STUDENT'],
    emoji: '✨',
    color: 'linear-gradient(135deg, #818cf8 0%, #6366f1 100%)',
    route: '/clubs',
    action: '查看推荐列表',
  },
  {
    id: 'club-manage',
    title: '社团主页管理',
    description: '负责人可维护社团简介、活动亮点与标签，结合成员与活动数据打造吸引力展示页。',
    roles: ['CLUB_MANAGER'],
    emoji: '🏛️',
    color: 'linear-gradient(135deg, #f97316 0%, #f59e0b 100%)',
    route: '/clubs',
    action: '前往社团广场',
  },
  {
    id: 'activity',
    title: '活动发布与报名',
    description: '支持活动发布、报名审批与签到反馈，学生可在线报名并在消息中心跟进状态。',
    roles: ['CLUB_MANAGER', 'STUDENT'],
    emoji: '📅',
    color: 'linear-gradient(135deg, #22d3ee 0%, #0ea5e9 100%)',
    route: '/activities',
    action: '查看活动日历',
  },
  {
    id: 'resources',
    title: '资源共享与申请',
    description: '社团共享设备/场地，学生提交申请，负责人与团委可在线审批并自动推送通知。',
    roles: ['CLUB_MANAGER', 'UNION_STAFF', 'STUDENT', 'SYSTEM_ADMIN'],
    emoji: '🧰',
    color: 'linear-gradient(135deg, #34d399 0%, #10b981 100%)',
    route: '/resources',
    action: '进入资源工作台',
  },
  {
    id: 'message',
    title: '站内通知中心',
    description: '报名、审批、资源处理等事件都会生成消息提醒，支持未读标记、引用定位和快速跳转。',
    roles: ['STUDENT', 'CLUB_MANAGER', 'UNION_STAFF', 'SYSTEM_ADMIN'],
    emoji: '🔔',
    color: 'linear-gradient(135deg, #f87171 0%, #ef4444 100%)',
    route: '/messages',
    action: '查看消息',
  },
  {
    id: 'dashboard',
    title: '数据看板',
    description: '动态指标卡、活跃社团榜单与活动趋势，为团委及管理员提供决策参考。',
    roles: ['UNION_STAFF', 'SYSTEM_ADMIN'],
    emoji: '📊',
    color: 'linear-gradient(135deg, #a855f7 0%, #8b5cf6 100%)',
    route: '/dashboard',
    action: '打开数据看板',
  },
  {
    id: 'admin',
    title: '账号与权限管理',
    description: '系统管理员可以搜索用户、调整角色与启用状态，保障权限安全与合规。',
    roles: ['SYSTEM_ADMIN'],
    emoji: '🛡️',
    color: 'linear-gradient(135deg, #f472b6 0%, #ec4899 100%)',
    route: '/admin/users',
    action: '管理账号',
  },
  {
    id: 'profile',
    title: '个人画像与兴趣标签',
    description: '所有用户均可维护个人资料、兴趣标签与联系方式，提升推荐准确度与沟通效率。',
    roles: ['STUDENT', 'CLUB_MANAGER', 'UNION_STAFF', 'SYSTEM_ADMIN'],
    emoji: '🧑🏻‍🎓',
    color: 'linear-gradient(135deg, #38bdf8 0%, #0ea5e9 100%)',
    route: '/profile',
    action: '完善资料',
  },
];

const studentJourney = [
  { title: '注册 / 登录', description: '使用学号或邮箱注册账户，并完成邮箱验证与首次登录。' },
  { title: '完善兴趣标签', description: '在个人资料中选择兴趣标签，作为推荐算法的重要输入。' },
  { title: '浏览推荐社团', description: '在社团广场获取“为你推荐”列表，了解社团特色与活动。' },
  { title: '提交加入申请', description: '在线填写申请理由并追踪审批状态，重要进展将同步通知。' },
  { title: '参与活动与资源', description: '报名活动、申请资源，积累校园实践经历并记录成长。' },
];

const managerTimeline: TimelineItem[] = [
  {
    title: '建设社团主页',
    timestamp: '阶段 1',
    description: '发布社团介绍、标签与招新亮点，提升曝光度。',
    type: 'primary',
  },
  {
    title: '活动策划与发布',
    timestamp: '阶段 2',
    description: '创建活动、设置报名要求，在线收集与审批报名。',
    type: 'success',
  },
  {
    title: '资源共享管理',
    timestamp: '阶段 3',
    description: '上传社团资源，与团委协同处理申请，沉淀服务能力。',
    type: 'warning',
  },
  {
    title: '沟通与反馈',
    timestamp: '阶段 4',
    description: '通过消息中心和数据看板掌握社团活跃度，持续优化运营。',
    type: 'info',
  },
];

const capabilityGroups = [
  {
    title: '用户与权限体系',
    subtitle: '注册、登录、角色管控',
    items: [
      '支持学生、社团负责人、团委、系统管理员多角色登录',
      '管理员可实时调整角色与启用状态，保证合规安全',
      '个人资料支持兴趣标签与联系方式维护，增强用户画像',
    ],
  },
  {
    title: '社团与招新',
    subtitle: '展示、推荐与加入流程',
    items: [
      '社团卡片展示成员规模、近期活动与标签亮点',
      '兴趣标签匹配算法提供“为你推荐”列表',
      '在线提交加入申请，审批结果推送至消息中心',
    ],
  },
  {
    title: '活动与资源',
    subtitle: '计划执行与资源调配',
    items: [
      '活动支持发布、报名、审批、签到全链路管理',
      '共享资源可配置可用时段与联系方式，学生线上申请',
      '负责人与团委在线审批，系统自动发出审批通知',
    ],
  },
  {
    title: '协作与洞察',
    subtitle: '消息通知与数据分析',
    items: [
      '站内消息自动聚合审批进展与任务提醒，支持标记已读',
      '数据看板提供社团指标、活动趋势、类别分布等可视化',
      '历史审批与资源申请记录可追溯，支撑校内协同',
    ],
  },
];

const roleLabels: Record<Role, string> = {
  STUDENT: '学生',
  CLUB_MANAGER: '社团负责人',
  UNION_STAFF: '团委',
  SYSTEM_ADMIN: '系统管理员',
};

const availableRoles = computed(() => new Set(auth.roles as Role[]));

const metricsCards = computed(() => {
  if (!metrics.value) {
    return [
      { title: '社团总数', value: '—', subtitle: '涵盖学生组织与兴趣社团' },
      { title: '本月活动', value: '—', subtitle: '查看活动趋势与活跃度' },
      { title: '活跃成员', value: '—', subtitle: '近 30 天参与情况' },
    ];
  }
  return [
    {
      title: '社团总数',
      value: String(metrics.value.totalClubs),
      subtitle: `本学期新增 ${metrics.value.newClubsThisSemester}`,
    },
    {
      title: '本月活动',
      value: String(metrics.value.totalActivitiesThisMonth),
      subtitle: `即将开展 ${metrics.value.upcomingActivities}`,
    },
    {
      title: '活跃成员',
      value: String(metrics.value.activeMembersLast30Days),
      subtitle: '近 30 天参与活动的成员',
    },
  ];
});

const filteredFeatures = computed(() => {
  if (selectedFilter.value === 'ALL') {
    return featureCards;
  }
  if (selectedFilter.value === 'MY') {
    if (!auth.isAuthenticated || availableRoles.value.size === 0) {
      return featureCards.filter((feature) => feature.roles.includes('STUDENT'));
    }
    return featureCards.filter((feature) => isFeatureActive(feature));
  }
  return featureCards.filter(
    (feature) => feature.roles.length === 0 || feature.roles.includes(selectedFilter.value as Role),
  );
});

const isFeatureActive = (feature: FeatureCard) => {
  if (feature.roles.length === 0) return true;
  if (availableRoles.value.size === 0) return false;
  return feature.roles.some((role) => availableRoles.value.has(role));
};

const featureStatusText = (feature: FeatureCard) => {
  if (isFeatureActive(feature)) {
    return '已开通';
  }
  return '需特定角色';
};

const displayRoles = (feature: FeatureCard) => {
  if (feature.roles.length === 0) {
    return ['全部角色'];
  }
  return feature.roles.map((role) => roleLabels[role]);
};

const loadMetrics = async () => {
  metricsLoading.value = true;
  try {
    metrics.value = await fetchDashboardMetrics();
  } catch (error) {
    ElMessage.error('统计数据加载失败，请稍后再试');
  } finally {
    metricsLoading.value = false;
  }
};

const goto = (path: string) => {
  router.push(path);
};

onMounted(async () => {
  await auth.bootstrap();
  if (auth.isAuthenticated) {
    await loadMetrics();
  }
});
</script>

<style scoped>
.overview {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.overview__hero {
  display: flex;
  gap: 24px;
  align-items: stretch;
}

.overview__hero-text {
  flex: 1;
  padding: 32px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--ccm-accent), var(--ccm-primary));
  color: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-shadow: var(--ccm-card-shadow);
}

.overview__hero-text h1 {
  font-size: 32px;
  font-weight: 600;
  margin: 0;
}

.overview__hero-text p {
  margin: 0;
  font-size: 16px;
  line-height: 1.6;
  opacity: 0.92;
}

.overview__hero-actions {
  display: flex;
  gap: 12px;
}

.overview__hero-card {
  width: 360px;
  border-radius: 16px;
}

.overview__hero-card-title {
  font-weight: 600;
}

.overview__stats {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.overview__stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.overview__stat-value {
  font-size: 26px;
  font-weight: 600;
  color: var(--ccm-text-primary);
}

.overview__stat-label {
  font-size: 14px;
  color: var(--ccm-text-secondary);
}

.overview__stat-desc {
  font-size: 12px;
  color: var(--ccm-text-muted);
}

.overview__filters {
  border-radius: 16px;
  background: var(--ccm-surface);
  border: 1px solid var(--ccm-border);
  padding: 24px;
}

.overview__filters-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.overview__filters-text h2 {
  margin: 0 0 6px;
}

.overview__features {
  border-radius: 12px;
}

.overview__feature-card {
  border-radius: 16px;
  min-height: 220px;
  display: flex;
  padding: 20px;
  gap: 16px;
  position: relative;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  background: var(--ccm-surface);
  border: 1px solid var(--ccm-border);
  box-shadow: var(--ccm-card-shadow);
}

.overview__feature-card:hover {
  transform: translateY(-4px);
}

.overview__feature-card--inactive {
  opacity: 0.65;
}

.overview__feature-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #ffffff;
}

.overview__feature-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.overview__feature-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.overview__feature-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.overview__feature-desc {
  margin: 0;
  color: var(--ccm-text-secondary);
  line-height: 1.6;
}

.overview__feature-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.overview__feature-actions {
  margin-top: auto;
}

.overview__journey {
  margin-top: 8px;
}

.overview__panel-title {
  font-weight: 600;
  color: var(--ccm-text-primary);
}

.overview__collapse-title {
  font-weight: 600;
  margin-right: 8px;
  color: var(--ccm-text-primary);
}

.overview__capability-list {
  margin: 0;
  padding-left: 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.overview__capability-item {
  color: var(--ccm-text-secondary);
  line-height: 1.6;
}

@media (max-width: 1200px) {
  .overview__hero {
    flex-direction: column;
  }

  .overview__hero-card {
    width: 100%;
  }

  .overview__hero-text {
    padding: 24px;
  }

  .overview__feature-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .overview__feature-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
  }
}
</style>
