<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="card in summaryCards" :key="card.title">
        <el-card class="dashboard__card" shadow="hover">
          <div class="dashboard__card-title">{{ card.title }}</div>
          <div class="dashboard__card-value">{{ card.value }}</div>
          <div class="dashboard__card-sub">{{ card.subtitle }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="dashboard__section">
      <el-col :span="12">
        <el-card shadow="never" class="dashboard__panel">
          <template #header>
            <div class="dashboard__panel-title">活跃社团 TOP5</div>
          </template>
          <el-table :data="metrics?.topActiveClubs ?? []" border empty-text="暂无数据">
            <el-table-column prop="clubName" label="社团" min-width="160" />
            <el-table-column prop="activityCount" label="近30天活动" width="120" />
            <el-table-column prop="memberCount" label="成员数" width="100" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="dashboard__panel">
          <template #header>
            <div class="dashboard__panel-title">社团类别分布</div>
          </template>
          <el-empty v-if="!metrics?.activityCategoryDistribution?.length" description="暂无数据" />
          <el-progress
            v-else
            v-for="item in metrics?.activityCategoryDistribution"
            :key="item.category"
            :percentage="categoryPercentage(item.value)"
            :format="() => `${item.category} (${item.value})`"
            :stroke-width="18"
            striped
            class="dashboard__progress"
          />
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="dashboard__panel">
      <template #header>
        <div class="dashboard__panel-title">活动趋势（月度）</div>
      </template>
      <el-empty v-if="!metrics?.activityTrend?.length" description="暂无数据" />
      <el-table v-else :data="metrics?.activityTrend" border style="width: 100%">
        <el-table-column prop="month" label="月份" width="160" />
        <el-table-column prop="activityCount" label="活动数量" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { fetchDashboardMetrics } from '../api/dashboard';
import type { DashboardMetrics } from '../types/models';
import { ElMessage } from 'element-plus';

const metrics = ref<DashboardMetrics>();

const summaryCards = computed(() => {
  if (!metrics.value) {
    return [] as Array<{ title: string; value: string; subtitle: string }>;
  }
  const data = metrics.value;
  return [
    { title: '社团总数', value: String(data.totalClubs), subtitle: `本学期新增 ${data.newClubsThisSemester}` },
    { title: '成员总数', value: String(data.totalMembers), subtitle: `近30天活跃 ${data.activeMembersLast30Days}` },
    { title: '本月活动', value: String(data.totalActivitiesThisMonth), subtitle: `即将开展 ${data.upcomingActivities}` },
    { title: '活跃社团', value: `${data.topActiveClubs.length}`, subtitle: '统计近30天数据' },
  ];
});

const categoryPercentage = (value: number) => {
  if (!metrics.value?.activityCategoryDistribution?.length) return 0;
  const total = metrics.value.activityCategoryDistribution.reduce((sum, item) => sum + item.value, 0);
  return Math.round((value / total) * 100);
};

const loadMetrics = async () => {
  try {
    metrics.value = await fetchDashboardMetrics();
  } catch (error) {
    ElMessage.error('数据加载失败，请稍后重试');
  }
};

onMounted(() => {
  loadMetrics();
});
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.dashboard__card {
  border-radius: 12px;
  background: linear-gradient(135deg, var(--ccm-primary-strong) 0%, var(--ccm-accent) 100%);
  color: #ffffff;
}

.dashboard__card-title {
  font-size: 16px;
  opacity: 0.85;
}

.dashboard__card-value {
  font-size: 32px;
  font-weight: 600;
  margin-top: 8px;
}

.dashboard__card-sub {
  margin-top: 4px;
  font-size: 13px;
  opacity: 0.85;
}

.dashboard__panel {
  border-radius: 12px;
}

.dashboard__panel-title {
  font-weight: 600;
  color: var(--ccm-text-primary);
}

.dashboard__section {
  margin-top: 10px;
}

.dashboard__progress {
  margin-bottom: 12px;
}
</style>
