<template>
  <div class="panorama">
    <section class="panorama__hero" role="region" aria-label="功能全景">
      <div class="panorama__hero-canvas" aria-hidden="true">
        <span class="panorama__hero-contour panorama__hero-contour--one"></span>
        <span class="panorama__hero-contour panorama__hero-contour--two"></span>
        <span class="panorama__hero-contour panorama__hero-contour--three"></span>
      </div>
      <div class="panorama__hero-body">
        <div class="panorama__hero-copy">
          <span class="panorama__hero-eyebrow">Campus Flow Matrix</span>
          <h1>校园社团体验再设计</h1>
          <p>
            在流线型界面中浏览社团生态，快速识别对你开放的核心工具与数据看板。
            通过角色切换进入专属流程，保持团队协同与信息透明。
          </p>
          <div class="panorama__hero-actions">
            <el-button type="primary" size="large" @click="goto('/dashboard')">进入数据总览</el-button>
            <el-button size="large" text @click="goto('/clubs')">探索社团生态</el-button>
          </div>
        </div>
        <el-card shadow="never" class="panorama__hero-card">
          <template #header>
            <div class="panorama__hero-card-title">核心指标即刻感知</div>
          </template>
          <el-skeleton :loading="metricsLoading" animated :rows="3">
            <div class="panorama__metrics">
              <div v-for="card in metricsCards" :key="card.title" class="panorama__metric">
                <div class="panorama__metric-label">{{ card.title }}</div>
                <div class="panorama__metric-value">{{ card.value }}</div>
                <div class="panorama__metric-desc">{{ card.subtitle }}</div>
              </div>
            </div>
          </el-skeleton>
        </el-card>
      </div>
    </section>

    <section class="panorama__panel panorama__panel--filters">
      <header class="panorama__panel-header">
        <div>
          <span class="panorama__panel-eyebrow">多角色编排</span>
          <h2>选择你的工作视角</h2>
          <p>从学生到管理者，按角色切换功能入口与推荐流程。</p>
        </div>
        <el-radio-group v-model="selectedFilter" size="large" class="panorama__filter-group">
          <el-radio-button v-for="option in roleFilters" :key="option.value" :label="option.value">
            {{ option.label }}
          </el-radio-button>
        </el-radio-group>
      </header>
    </section>

    <section class="panorama__features">
      <el-empty
        v-if="!filteredFeatures.length"
        description="该视角下暂无功能"
        class="panorama__empty"
      />
      <div v-else class="panorama__feature-grid">
        <article
          v-for="feature in filteredFeatures"
          :key="feature.id"
          class="panorama__feature-card"
          :data-state="isFeatureActive(feature) ? 'active' : 'pending'"
        >
          <div
            class="panorama__feature-icon"
            :style="{ background: feature.color }"
            aria-hidden="true"
          >
            <span>{{ feature.emoji }}</span>
          </div>
          <div class="panorama__feature-content">
            <div class="panorama__feature-header">
              <h3>{{ feature.title }}</h3>
              <el-tag
                size="small"
                :type="isFeatureActive(feature) ? 'success' : 'info'"
              >
                {{ featureStatusText(feature) }}
              </el-tag>
            </div>
            <p class="panorama__feature-desc">{{ feature.description }}</p>
            <div class="panorama__feature-meta">
              <el-tag
                v-for="role in displayRoles(feature)"
                :key="role"
                size="small"
                effect="plain"
              >
                {{ role }}
              </el-tag>
            </div>
            <div class="panorama__feature-actions">
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
        </article>
      </div>
    </section>

    <section class="panorama__journey">
      <el-row :gutter="24">
        <el-col :span="12">
          <div class="panorama__journey-card">
            <header>
              <span class="panorama__panel-eyebrow">学生流</span>
              <div class="panorama__panel-title">学生用户体验路径</div>
            </header>
            <el-steps direction="vertical" :active="studentJourney.length" finish-status="success">
              <el-step
                v-for="(step, index) in studentJourney"
                :key="step.title"
                :title="`${index + 1}. ${step.title}`"
                :description="step.description"
              />
            </el-steps>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="panorama__journey-card">
            <header>
              <span class="panorama__panel-eyebrow">运营控台</span>
              <div class="panorama__panel-title">社团负责人工作台</div>
            </header>
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
          </div>
        </el-col>
      </el-row>
    </section>

    <section class="panorama__panel panorama__panel--capabilities">
      <header class="panorama__panel-header">
        <div>
          <span class="panorama__panel-eyebrow">功能矩阵</span>
          <h2>能力清单与亮点</h2>
          <p>聚焦协作、资源与数据层面的高频操作。</p>
        </div>
      </header>
      <el-collapse accordion class="panorama__collapse">
        <el-collapse-item
          v-for="group in capabilityGroups"
          :key="group.title"
          :name="group.title"
        >
          <template #title>
            <span class="panorama__collapse-title">{{ group.title }}</span>
            <el-tag size="small" effect="plain">{{ group.subtitle }}</el-tag>
          </template>
          <ul class="panorama__capability-list">
            <li v-for="item in group.items" :key="item" class="panorama__capability-item">
              {{ item }}
            </li>
          </ul>
        </el-collapse-item>
      </el-collapse>
    </section>
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
.panorama {
  display: flex;
  flex-direction: column;
  gap: 40px;
  padding-bottom: 24px;
}

.panorama__hero {
  position: relative;
  overflow: hidden;
  border-radius: 32px;
  padding: 56px 64px;
  background: radial-gradient(120% 140% at 10% 20%, #1f2937 0%, #111827 35%, #0f172a 65%);
  color: #f8fafc;
}

.panorama__hero-body {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 36px;
  align-items: center;
}

.panorama__hero-copy {
  grid-column: span 3;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.panorama__hero-eyebrow {
  letter-spacing: 0.2em;
  font-size: 12px;
  text-transform: uppercase;
  color: rgba(148, 163, 184, 0.8);
}

.panorama__hero-copy h1 {
  margin: 0;
  font-size: 40px;
  font-weight: 600;
  line-height: 1.1;
}

.panorama__hero-copy p {
  margin: 0;
  font-size: 16px;
  line-height: 1.7;
  color: rgba(226, 232, 240, 0.92);
}

.panorama__hero-actions {
  display: flex;
  gap: 14px;
}

.panorama__hero-card {
  grid-column: span 2;
  border-radius: 24px;
  backdrop-filter: blur(16px);
  background: rgba(15, 23, 42, 0.55);
  border: 1px solid rgba(148, 163, 184, 0.2);
  color: #e2e8f0;
}

.panorama__hero-card-title {
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-size: 12px;
  color: rgba(148, 163, 184, 0.9);
}

.panorama__metrics {
  display: grid;
  gap: 20px;
}

.panorama__metric {
  display: grid;
  gap: 6px;
}

.panorama__metric-label {
  font-size: 13px;
  color: rgba(148, 163, 184, 0.9);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.panorama__metric-value {
  font-size: 32px;
  font-weight: 600;
}

.panorama__metric-desc {
  font-size: 12px;
  color: rgba(226, 232, 240, 0.65);
}

.panorama__hero-canvas {
  pointer-events: none;
}

.panorama__hero-contour {
  position: absolute;
  border-radius: 999px;
  filter: blur(0);
  opacity: 0.55;
}

.panorama__hero-contour--one {
  inset: -40% 30% 40% -20%;
  background: radial-gradient(120% 100% at 0% 50%, rgba(99, 102, 241, 0.85), transparent 65%);
  animation: contour-drift 26s ease-in-out infinite alternate;
}

.panorama__hero-contour--two {
  inset: 42% -30% -30% 35%;
  background: radial-gradient(120% 120% at 50% 50%, rgba(14, 165, 233, 0.7), transparent 75%);
  animation: contour-drift 22s ease-in-out infinite alternate-reverse;
}

.panorama__hero-contour--three {
  inset: 10% 15% -35% -35%;
  background: radial-gradient(120% 120% at 50% 50%, rgba(56, 189, 248, 0.4), transparent 80%);
  animation: contour-drift 30s ease-in-out infinite alternate;
}

.panorama__panel {
  border-radius: 24px;
  padding: 32px 36px;
  background: #ffffff;
  box-shadow: 0 30px 60px rgba(15, 23, 42, 0.08);
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.panorama__panel--filters {
  align-items: stretch;
}

.panorama__panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
}

.panorama__panel-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.16em;
  color: #1e3a8a;
  background: rgba(191, 219, 254, 0.5);
}

.panorama__panel-header h2 {
  margin: 10px 0 8px;
  font-size: 28px;
  font-weight: 600;
  color: #0f172a;
}

.panorama__panel-header p {
  margin: 0;
  color: #475569;
  line-height: 1.6;
}

.panorama__filter-group {
  background: #f8fafc;
  border-radius: 999px;
  padding: 6px;
  border: 1px solid #e2e8f0;
}

.panorama__features {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.panorama__feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}

.panorama__feature-card {
  position: relative;
  border-radius: 24px;
  background: linear-gradient(160deg, #ffffff 0%, #f8fafc 60%, #e2e8f0 120%);
  border: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 24px 48px rgba(15, 23, 42, 0.08);
  padding: 24px 24px 22px;
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 20px;
  transition: transform 220ms ease, box-shadow 220ms ease;
}

.panorama__feature-card[data-state='active']:hover {
  transform: translateY(-6px);
  box-shadow: 0 32px 56px rgba(30, 64, 175, 0.15);
}

.panorama__feature-card[data-state='pending'] {
  opacity: 0.72;
}

.panorama__feature-icon {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #ffffff;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.12);
}

.panorama__feature-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panorama__feature-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.panorama__feature-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
}

.panorama__feature-desc {
  margin: 0;
  color: #1f2937;
  line-height: 1.6;
}

.panorama__feature-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.panorama__feature-actions {
  margin-top: auto;
}

.panorama__journey {
  border-radius: 32px;
  background: linear-gradient(135deg, rgba(241, 245, 249, 0.9), rgba(224, 231, 255, 0.95));
  padding: 36px 40px;
  box-shadow: inset 0 1px 0 rgba(148, 163, 184, 0.15), 0 40px 70px rgba(30, 64, 175, 0.12);
}

.panorama__journey-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  padding: 24px 28px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  backdrop-filter: blur(12px);
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 100%;
}

.panorama__panel-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 6px 0 0;
}

.panorama__panel--capabilities {
  background: #0f172a;
  color: #e2e8f0;
  border: 1px solid rgba(148, 163, 184, 0.3);
}

.panorama__panel--capabilities h2 {
  color: #f8fafc;
}

.panorama__collapse {
  background: transparent;
  border: none;
}

.panorama__collapse :deep(.el-collapse-item__header) {
  font-weight: 600;
  font-size: 16px;
  color: #f8fafc;
}

.panorama__collapse :deep(.el-collapse-item__wrap) {
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.25);
  border-radius: 18px;
  margin-top: 12px;
}

.panorama__collapse-title {
  margin-right: 12px;
}

.panorama__capability-list {
  margin: 0;
  padding: 16px 24px 24px 32px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panorama__capability-item {
  position: relative;
  color: rgba(226, 232, 240, 0.92);
  line-height: 1.6;
  padding-left: 14px;
}

.panorama__capability-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 10px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #38bdf8;
}

.panorama__empty {
  background: rgba(248, 250, 252, 0.6);
  border-radius: 24px;
  padding: 40px;
  border: 1px dashed rgba(148, 163, 184, 0.4);
}

@keyframes contour-drift {
  from {
    transform: translate3d(0, 0, 0) rotate(0deg);
  }
  to {
    transform: translate3d(2%, -4%, 0) rotate(12deg);
  }
}

@media (max-width: 1280px) {
  .panorama__hero {
    padding: 44px 48px;
  }

  .panorama__hero-body {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .panorama__hero-copy {
    grid-column: span 4;
  }

  .panorama__hero-card {
    grid-column: span 4;
  }
}

@media (max-width: 960px) {
  .panorama {
    gap: 32px;
  }

  .panorama__hero {
    padding: 36px 28px;
    border-radius: 28px;
  }

  .panorama__hero-body {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 24px;
  }

  .panorama__hero-copy {
    grid-column: span 2;
  }

  .panorama__hero-card {
    grid-column: span 2;
  }

  .panorama__panel {
    padding: 28px 24px;
  }
}

@media (max-width: 760px) {
  .panorama__hero-copy h1 {
    font-size: 32px;
  }

  .panorama__hero-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .panorama__panel-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .panorama__filter-group {
    width: 100%;
  }

  .panorama__feature-card {
    grid-template-columns: 1fr;
    text-align: left;
  }

  .panorama__feature-icon {
    width: 60px;
    height: 60px;
    border-radius: 18px;
  }

  .panorama__journey {
    padding: 24px 20px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .panorama__hero-contour {
    animation: none;
  }

  .panorama__feature-card {
    transition: none;
  }
}
</style>
