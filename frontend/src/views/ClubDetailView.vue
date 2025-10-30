<template>
  <div v-if="club" class="club-detail">
    <el-card shadow="never" class="club-detail__info">
      <div class="club-detail__header">
        <img v-if="club.logoUrl" :src="club.logoUrl" class="club-detail__logo" alt="logo" />
        <div>
          <h2>{{ club.name }}</h2>
          <div class="club-detail__meta">
            <span>类别：{{ club.category || '未分类' }}</span>
            <span>成员：{{ club.memberCount }}</span>
            <span>负责人：{{ club.managerName || '未设置' }}</span>
          </div>
          <div class="club-detail__contact">
            <span v-if="club.contactEmail">邮箱：{{ club.contactEmail }}</span>
            <span v-if="club.contactPhone">电话：{{ club.contactPhone }}</span>
          </div>
        </div>
      </div>
      <p class="club-detail__description">{{ club.description }}</p>
      <div class="club-detail__tags">
        <el-tag v-for="tag in club.tags" :key="tag" size="small">{{ tag }}</el-tag>
      </div>
      <div class="club-detail__actions" v-if="!isManager">
        <el-button type="primary" @click="apply" :loading="applying" :disabled="joined">
          {{ joined ? '已提交申请' : '申请加入' }}
        </el-button>
      </div>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="never" class="club-detail__panel">
          <template #header>
            <div class="club-detail__panel-title">近期活动</div>
          </template>
          <el-empty v-if="!activities.length" description="暂无活动" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="activity in activities"
              :key="activity.id"
              :timestamp="formatDate(activity.startTime)"
              placement="top"
            >
              <h4>{{ activity.title }}</h4>
              <p>地点：{{ activity.location || '待定' }}</p>
              <el-button type="primary" link @click="goActivity(activity.id)">查看详情</el-button>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
      <el-col :span="8" v-if="isManager">
        <el-card shadow="never" class="club-detail__panel">
          <template #header>
            <div class="club-detail__panel-title">待审核成员 ({{ applicants.length }})</div>
          </template>
          <el-empty v-if="!applicants.length" description="暂无待处理" />
          <el-scrollbar v-else class="club-detail__scroll">
            <div v-for="member in applicants" :key="member.id" class="club-detail__applicant">
              <div>
                <div class="club-detail__applicant-name">{{ member.clubName }} 申请人</div>
                <div class="club-detail__applicant-reason">{{ member.applicationReason || '无申请理由' }}</div>
              </div>
              <div class="club-detail__applicant-actions">
                <el-button size="small" type="success" @click="decide(member.id, true)">通过</el-button>
                <el-button size="small" type="danger" @click="decide(member.id, false)">拒绝</el-button>
              </div>
            </div>
          </el-scrollbar>
        </el-card>
      </el-col>
    </el-row>
  </div>
  <el-skeleton v-else animated :rows="6" />
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import dayjs from 'dayjs';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchClub } from '../api/club';
import { fetchClubActivities } from '../api/activity';
import { applyMembership, decideMembership, fetchClubApplicants, fetchMyMemberships } from '../api/membership';
import type { ActivitySummary, ClubDetail, MembershipRecord } from '../types/models';
import { useAuthStore } from '../store/auth';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const club = ref<ClubDetail>();
const activities = ref<ActivitySummary[]>([]);
const myMemberships = ref<MembershipRecord[]>([]);
const applicants = ref<MembershipRecord[]>([]);
const applying = ref(false);

const clubId = Number(route.params.id);

const isManager = computed(() => auth.roles.some((role) => role === 'CLUB_MANAGER' || role === 'SYSTEM_ADMIN' || role === 'UNION_STAFF'));

const joined = computed(() => myMemberships.value.some((m) => m.clubId === clubId && (m.status === 'PENDING' || m.status === 'APPROVED')));

const formatDate = (value?: string | null) => {
  if (!value) return '时间待定';
  return dayjs(value).format('YYYY年MM月DD日 HH:mm');
};

const loadClub = async () => {
  club.value = await fetchClub(clubId);
};

const loadActivities = async () => {
  const response = await fetchClubActivities(clubId, { page: 0, size: 5 });
  activities.value = response.content;
};

const loadMemberships = async () => {
  myMemberships.value = await fetchMyMemberships();
  if (isManager.value) {
    applicants.value = await fetchClubApplicants(clubId);
  }
};

const apply = async () => {
  applying.value = true;
  try {
    await applyMembership({ clubId, applicationReason: '热爱社团活动，期待加入！' });
    ElMessage.success('申请已提交');
    await loadMemberships();
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '申请失败');
  } finally {
    applying.value = false;
  }
};

const decide = async (membershipId: number, approve: boolean) => {
  const action = approve ? '通过' : '拒绝';
  try {
    await ElMessageBox.confirm(`确认${action}该申请吗？`, '提示', { type: 'warning' });
    await decideMembership(membershipId, { approve, message: approve ? '欢迎加入社团！' : '感谢关注，我们将保持联系' });
    ElMessage.success(`${action}成功`);
    await loadMemberships();
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message ?? `${action}失败`);
    }
  }
};

const goActivity = (id: number) => {
  router.push(`/activities?id=${id}`);
};

onMounted(async () => {
  await auth.bootstrap();
  await Promise.all([loadClub(), loadActivities(), loadMemberships()]);
});
</script>

<style scoped>
.club-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.club-detail__info {
  border-radius: 12px;
}

.club-detail__header {
  display: flex;
  gap: 16px;
}

.club-detail__logo {
  width: 96px;
  height: 96px;
  border-radius: 16px;
  object-fit: cover;
}

.club-detail__meta,
.club-detail__contact {
  display: flex;
  gap: 16px;
  color: #6b7280;
}

.club-detail__description {
  margin-top: 12px;
  color: #374151;
  line-height: 1.6;
}

.club-detail__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.club-detail__actions {
  margin-top: 16px;
}

.club-detail__panel {
  border-radius: 12px;
}

.club-detail__panel-title {
  font-weight: 600;
}

.club-detail__scroll {
  max-height: 320px;
}

.club-detail__applicant {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 4px;
  border-bottom: 1px solid #f1f5f9;
}

.club-detail__applicant:last-child {
  border-bottom: none;
}

.club-detail__applicant-name {
  font-weight: 600;
}

.club-detail__applicant-reason {
  font-size: 13px;
  color: #64748b;
}

.club-detail__applicant-actions {
  display: flex;
  gap: 8px;
}
</style>
