<template>
  <div class="checkin-page">
    <el-card class="checkin-page__card" shadow="never">
      <template v-if="status === 'loading'">
        <el-result icon="info" title="正在签到" sub-title="正在验证二维码信息，请稍候" />
      </template>
      <template v-else-if="status === 'success'">
        <el-result icon="success" title="签到成功" :sub-title="successMessage">
          <template #extra>
            <el-button type="primary" @click="goActivities">查看活动</el-button>
          </template>
        </el-result>
      </template>
      <template v-else>
        <el-result icon="error" title="签到失败" :sub-title="errorMessage">
          <template #extra>
            <el-button type="primary" @click="retry" v-if="canRetry">重新签到</el-button>
            <el-button @click="goActivities">返回活动列表</el-button>
          </template>
        </el-result>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { submitActivityCheckIn } from '../api/activity';

const route = useRoute();
const router = useRouter();

const status = ref<'loading' | 'success' | 'error'>('loading');
const errorMessage = ref('二维码无效，请联系活动负责人。');
const successMessage = ref('感谢参与，祝您活动愉快！');

const activityId = computed(() => {
  const raw = route.query.activity;
  const value = Array.isArray(raw) ? raw[0] : raw;
  return value ? Number(value) : NaN;
});

const token = computed(() => {
  const raw = route.query.token;
  return Array.isArray(raw) ? raw[0] : raw;
});

const canRetry = computed(() => status.value === 'error' && Boolean(token.value) && !Number.isNaN(activityId.value));

const submitCheckIn = async () => {
  if (Number.isNaN(activityId.value) || !token.value) {
    status.value = 'error';
    errorMessage.value = '二维码参数缺失，请重新扫描。';
    return;
  }
  status.value = 'loading';
  try {
    await submitActivityCheckIn(activityId.value, { token: token.value });
    status.value = 'success';
  } catch (error: any) {
    status.value = 'error';
    errorMessage.value = error?.response?.data?.message ?? '签到失败，请稍后再试。';
  }
};

const retry = async () => {
  await submitCheckIn();
};

const goActivities = () => {
  router.replace({ name: 'activities' });
};

onMounted(async () => {
  await submitCheckIn();
  if (status.value === 'success') {
    ElMessage.success('签到成功');
  }
});
</script>

<style scoped>
.checkin-page {
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 16px;
}

.checkin-page__card {
  width: 520px;
  max-width: 100%;
}
</style>
