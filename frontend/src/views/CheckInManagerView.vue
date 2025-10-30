<template>
  <div class="checkin-manager">
    <el-card shadow="never" class="checkin-manager__card">
      <template #header>
        <div class="checkin-manager__header">
          <span>签到二维码管理</span>
          <el-button
            type="primary"
            :loading="refreshing"
            :disabled="!selectedActivityId"
            @click="refreshQr(true)"
          >
            生成 / 刷新二维码
          </el-button>
        </div>
      </template>
      <div class="checkin-manager__controls">
        <el-form label-width="88px" class="checkin-manager__form" :inline="false">
          <el-form-item label="社团">
            <el-select
              v-model="selectedClubId"
              placeholder="选择社团"
              style="width: 260px"
            >
              <el-option v-for="club in myClubs" :key="club.id" :label="club.name" :value="club.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="活动">
            <el-select
              v-model="selectedActivityId"
              placeholder="选择活动"
              :disabled="activities.length === 0"
              style="width: 300px"
            >
              <el-option
                v-for="item in activities"
                :key="item.id"
                :label="formatActivityLabel(item)"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <div class="checkin-manager__qr" v-loading="refreshing">
          <template v-if="qrInfo.qrUrl">
            <el-image :src="qrInfo.qrUrl" fit="contain" :preview-src-list="[qrInfo.qrUrl]" />
            <div class="checkin-manager__meta">
              <span>生成时间：{{ formatDatetime(qrInfo.generatedAt) }}</span>
              <span>
                有效期至：
                <el-tag v-if="qrInfo.expiresAt" :type="qrExpired ? 'danger' : 'success'" size="small">
                  {{ formatDatetime(qrInfo.expiresAt) }}
                </el-tag>
                <span v-else>--</span>
              </span>
            </div>
          </template>
          <el-empty v-else description="选择活动后生成二维码" />
        </div>
      </div>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="checkin-manager__header">
          <span>签到记录</span>
          <el-tag v-if="records.length" type="success" size="small">
            累计 {{ page.total }} 人次
          </el-tag>
        </div>
      </template>
      <el-table :data="records" v-loading="loadingRecords" empty-text="暂无签到数据">
        <el-table-column prop="attendeeName" label="姓名" min-width="160" />
        <el-table-column prop="method" label="方式" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="row.method === 'MANUAL' ? 'warning' : 'success'">
              {{ row.method === 'MANUAL' ? '手动' : '扫码' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="签到时间" width="180">
          <template #default="{ row }">
            {{ formatDatetime(row.checkedInAt) }}
          </template>
        </el-table-column>
      </el-table>
      <div class="checkin-manager__pagination" v-if="page.total > page.size">
        <el-pagination
          layout="prev, pager, next"
          :current-page="page.current"
          :page-size="page.size"
          :total="page.total"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import dayjs from 'dayjs';
import { ElMessage } from 'element-plus';
import { useRoute } from 'vue-router';
import { fetchMyClubs } from '../api/club';
import { fetchClubActivities } from '../api/clubManagement';
import { generateCheckInQr, fetchActivityCheckIns } from '../api/activity';
import type { ClubSummary, ActivitySummary, ActivityCheckInRecord } from '../types/models';

const myClubs = ref<ClubSummary[]>([]);
const activities = ref<ActivitySummary[]>([]);
const selectedClubId = ref<number | null>(null);
const selectedActivityId = ref<number | null>(null);

const qrInfo = reactive({
  qrUrl: '',
  generatedAt: '',
  expiresAt: '',
});

const refreshing = ref(false);
const loadingRecords = ref(false);
const records = ref<ActivityCheckInRecord[]>([]);
const page = reactive({
  current: 1,
  size: 10,
  total: 0,
});

const initializing = ref(true);

const route = useRoute();

const qrExpired = computed(() => {
  if (!qrInfo.expiresAt) return false;
  return dayjs(qrInfo.expiresAt).isBefore(dayjs());
});

const formatDatetime = (value?: string | null) => {
  if (!value) return '--';
  return dayjs(value).format('YYYY-MM-DD HH:mm');
};

const formatActivityLabel = (activity: ActivitySummary) => {
  const start = activity.startTime ? dayjs(activity.startTime).format('MM-DD HH:mm') : '待定';
  return `${activity.title}（${start}）`;
};

const loadClubs = async () => {
  myClubs.value = await fetchMyClubs();
  if (!myClubs.value.length) {
    selectedClubId.value = null;
    return;
  }
  if (!selectedClubId.value || !myClubs.value.some((club) => club.id === selectedClubId.value)) {
    selectedClubId.value = myClubs.value[0]?.id ?? null;
  }
};

const loadActivities = async () => {
  if (!selectedClubId.value) {
    activities.value = [];
    selectedActivityId.value = null;
    return;
  }
  try {
    const response = await fetchClubActivities(selectedClubId.value, { size: 50 });
    activities.value = response.content;
    if (!activities.value.length) {
      selectedActivityId.value = null;
      qrInfo.qrUrl = '';
      qrInfo.expiresAt = '';
      qrInfo.generatedAt = '';
      records.value = [];
      page.current = 1;
      page.total = 0;
      return;
    }
    if (!selectedActivityId.value || !activities.value.some((item) => item.id === selectedActivityId.value)) {
      const first = activities.value[0];
      selectedActivityId.value = first ? first.id : null;
    }
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '加载活动列表失败');
  }
};

const refreshQr = async (showMessage = false) => {
  if (!selectedActivityId.value) return;
  try {
    refreshing.value = true;
    const data = await generateCheckInQr(selectedActivityId.value);
    qrInfo.qrUrl = data.qrUrl;
    qrInfo.generatedAt = data.generatedAt;
    qrInfo.expiresAt = data.expiresAt;
    if (showMessage) {
      ElMessage.success('二维码已刷新');
    }
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '生成二维码失败');
  } finally {
    refreshing.value = false;
  }
};

const loadCheckIns = async (pageIndex = page.current) => {
  if (!selectedActivityId.value) {
    records.value = [];
    page.total = 0;
    return;
  }
  loadingRecords.value = true;
  try {
    const result = await fetchActivityCheckIns(selectedActivityId.value, {
      page: pageIndex - 1,
      size: page.size,
    });
    records.value = result.content;
    page.current = result.number + 1;
    page.total = result.totalElements;
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '加载签到记录失败');
  } finally {
    loadingRecords.value = false;
  }
};

const handlePageChange = async (nextPage: number) => {
  await loadCheckIns(nextPage);
};

watch(selectedClubId, async () => {
  await loadActivities();
});

watch(selectedActivityId, async () => {
  if (!selectedActivityId.value) {
    records.value = [];
    qrInfo.qrUrl = '';
    qrInfo.expiresAt = '';
    qrInfo.generatedAt = '';
    return;
  }
  if (!initializing.value) {
    await refreshQr();
    await loadCheckIns(1);
  }
});

onMounted(async () => {
  const initialClub = Number(
    Array.isArray(route.query.clubId) ? route.query.clubId[0] : route.query.clubId,
  );
  if (!Number.isNaN(initialClub)) {
    selectedClubId.value = initialClub;
  }

  await loadClubs();
  await loadActivities();

  const initialActivity = Number(
    Array.isArray(route.query.activityId) ? route.query.activityId[0] : route.query.activityId,
  );
  if (
    !Number.isNaN(initialActivity) &&
    activities.value.some((item) => item.id === initialActivity)
  ) {
    selectedActivityId.value = initialActivity;
  } else if (!selectedActivityId.value && activities.value.length) {
    selectedActivityId.value = activities.value[0]?.id ?? null;
  }

  if (selectedActivityId.value) {
    await refreshQr();
    await loadCheckIns(1);
  }

  initializing.value = false;
});
</script>

<style scoped>
.checkin-manager {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.checkin-manager__card {
  border-radius: 12px;
}

.checkin-manager__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.checkin-manager__controls {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  align-items: flex-start;
}

.checkin-manager__qr {
  width: 240px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.checkin-manager__qr :deep(.el-image) {
  width: 220px;
  height: 220px;
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.35);
  padding: 8px;
  background: #fff;
}

.checkin-manager__meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: #475569;
  text-align: center;
}

.checkin-manager__pagination {
  margin-top: 12px;
  display: flex;
  justify-content: center;
}
</style>
