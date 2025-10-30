<template>
  <div class="activities">
    <div class="activities__header">
      <h2>社团活动日程</h2>
      <el-button v-if="isManager" type="primary" @click="dialogVisible = true">发起活动</el-button>
    </div>

    <el-card shadow="never">
      <el-skeleton v-if="loading" :rows="4" animated />
      <el-empty v-else-if="activities.length === 0" description="近期暂无活动" />
      <div v-else class="activities__list">
        <el-card
          v-for="activity in activities"
          :key="activity.id"
          shadow="hover"
          class="activities__card"
        >
          <div class="activities__card-header">
            <div>
              <div class="activities__title">{{ activity.title }}</div>
              <div class="activities__meta">
                <span>{{ activity.clubName }}</span>
                <span>时间：{{ formatDate(activity.startTime) }}</span>
                <span>地点：{{ activity.location || '待定' }}</span>
              </div>
            </div>
            <el-tag type="success">已报名 {{ activity.attendeeCount }} 人</el-tag>
          </div>
          <p class="activities__description">{{ activity.description }}</p>
          <div class="activities__actions">
            <el-button
              size="small"
              type="primary"
              :disabled="activityRegistering === activity.id"
              @click="register(activity.id)"
            >
              报名参加
            </el-button>
          </div>
        </el-card>
      </div>
      <div class="activities__pagination" v-if="page">
        <el-pagination
          layout="prev, pager, next"
          background
          :current-page="page.number + 1"
          :page-size="page.size"
          :total="page.totalElements"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog title="新建活动" v-model="dialogVisible" width="560px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="所属社团" prop="clubId">
          <el-select v-model="form.clubId" placeholder="请选择社团">
            <el-option
              v-for="club in managerClubs"
              :key="club.id"
              :label="club.name"
              :value="club.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="活动简介" prop="description">
          <el-input type="textarea" :rows="3" v-model="form.description" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择时间" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择时间" />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="人数上限">
          <el-input-number v-model="form.capacity" :min="1" />
        </el-form-item>
        <el-form-item label="无需审核">
          <el-switch v-model="form.requiresApproval" inline-prompt active-text="否" inactive-text="是" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submit">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import dayjs from 'dayjs';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { fetchUpcomingActivities, registerActivity, createActivity } from '../api/activity';
import { fetchClubs } from '../api/club';
import type { ActivitySummary, PageResponse, ClubSummary } from '../types/models';
import { useAuthStore } from '../store/auth';

const auth = useAuthStore();

const query = reactive({ page: 0, size: 6 });
const page = ref<PageResponse<ActivitySummary>>();
const activities = ref<ActivitySummary[]>([]);
const loading = ref(false);
const activityRegistering = ref<number | null>(null);

const dialogVisible = ref(false);
const formRef = ref<FormInstance>();
const creating = ref(false);
const managerClubs = ref<ClubSummary[]>([]);

const form = reactive({
  clubId: undefined as number | undefined,
  title: '',
  description: '',
  startTime: '',
  endTime: '',
  location: '',
  capacity: undefined as number | undefined,
  requiresApproval: true,
});

const rules: FormRules<typeof form> = {
  clubId: [{ required: true, message: '请选择社团', trigger: 'change' }],
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入活动简介', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
};

const isManager = computed(() => auth.roles.some((role) => role === 'CLUB_MANAGER' || role === 'SYSTEM_ADMIN' || role === 'UNION_STAFF'));

const loadActivities = async () => {
  loading.value = true;
  try {
    const response = await fetchUpcomingActivities({ page: query.page, size: query.size });
    page.value = response;
    activities.value = response.content;
  } finally {
    loading.value = false;
  }
};

const loadManagerClubs = async () => {
  if (!isManager.value) return;
  const response = await fetchClubs({ page: 0, size: 100 });
  managerClubs.value = response.content;
};

const handlePageChange = async (pageIndex: number) => {
  query.page = pageIndex - 1;
  await loadActivities();
};

const formatDate = (value?: string | null) => {
  if (!value) return '时间待定';
  return dayjs(value).format('MM月DD日 HH:mm');
};

const register = async (activityId: number) => {
  activityRegistering.value = activityId;
  try {
    await registerActivity(activityId, {});
    ElMessage.success('报名成功，等待审核');
    await loadActivities();
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '报名失败');
  } finally {
    activityRegistering.value = null;
  }
};

const resetForm = () => {
  form.clubId = undefined;
  form.title = '';
  form.description = '';
  form.startTime = '';
  form.endTime = '';
  form.location = '';
  form.capacity = undefined;
  form.requiresApproval = true;
};

const submit = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return;
    creating.value = true;
    try {
      await createActivity(form.clubId!, {
        title: form.title,
        description: form.description,
        startTime: dayjs(form.startTime).toISOString(),
        endTime: dayjs(form.endTime).toISOString(),
        location: form.location,
        capacity: form.capacity,
        requiresApproval: form.requiresApproval,
      });
      ElMessage.success('活动发布成功');
      dialogVisible.value = false;
      resetForm();
      await loadActivities();
    } catch (error: any) {
      ElMessage.error(error?.response?.data?.message ?? '发布失败');
    } finally {
      creating.value = false;
    }
  });
};

onMounted(async () => {
  await auth.bootstrap();
  await Promise.all([loadActivities(), loadManagerClubs()]);
});
</script>

<style scoped>
.activities {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.activities__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activities__list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 12px;
}

.activities__card {
  border-radius: 12px;
}

.activities__card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activities__title {
  font-size: 18px;
  font-weight: 600;
}

.activities__meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

.activities__description {
  color: #4b5563;
  margin: 12px 0;
}

.activities__actions {
  display: flex;
  justify-content: flex-end;
}

.activities__pagination {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
