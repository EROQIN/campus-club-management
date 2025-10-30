<template>
  <div class="clubs">
    <el-card shadow="never" class="clubs__filters">
      <el-form :inline="true" :model="query">
        <el-form-item>
          <el-input
            v-model="query.keywords"
            placeholder="搜索社团名称或关键词"
            @keyup.enter.native="refresh"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-select v-model="query.category" placeholder="选择类别" clearable style="width: 160px">
            <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="refresh">搜索</el-button>
        </el-form-item>
        <el-form-item v-if="isManager">
          <el-button type="success" @click="openDialog">新建社团</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card
      v-if="showRecommendations"
      shadow="never"
      class="clubs__recommend"
    >
      <template #header>
        <div class="clubs__recommend-header">为你推荐的社团</div>
      </template>
      <el-skeleton :loading="recommendationsLoading" animated :count="1">
        <el-row :gutter="20">
          <el-col :span="8" v-for="club in recommendations" :key="club.id">
            <el-card class="clubs__card" shadow="hover">
              <div class="clubs__card-header">
                <h3>{{ club.name }}</h3>
                <el-tag type="success" size="small">匹配度 {{ club.recommendationScore }}</el-tag>
              </div>
              <p class="clubs__description">{{ club.description }}</p>
              <div class="clubs__meta">类别：{{ club.category || '未分类' }}</div>
              <div class="clubs__tags">
                <el-tag v-for="tag in club.tags" :key="tag" effect="light" size="small">{{ tag }}</el-tag>
              </div>
              <div class="clubs__actions">
                <el-button link type="primary" @click="goDetail(club.id)">查看详情</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-skeleton>
    </el-card>

    <el-row :gutter="20" class="clubs__list">
      <el-col :span="8" v-for="club in clubs" :key="club.id">
        <el-card shadow="hover" class="clubs__card">
          <div class="clubs__card-header">
            <img v-if="club.logoUrl" :src="club.logoUrl" alt="logo" class="clubs__logo" />
            <div>
              <h3>{{ club.name }}</h3>
              <div class="clubs__meta">类别：{{ club.category || '未分类' }}</div>
              <div class="clubs__meta">成员：{{ club.memberCount }} 人</div>
            </div>
          </div>
          <p class="clubs__description">{{ club.description }}</p>
          <div class="clubs__tags">
            <el-tag v-for="tag in club.tags" :key="tag" effect="dark" size="small">{{ tag }}</el-tag>
          </div>
          <div class="clubs__actions">
            <el-button link type="primary" @click="goDetail(club.id)">查看详情</el-button>
            <el-button
              v-if="!isManager"
              size="small"
              type="primary"
              :loading="joiningClubId === club.id"
              :disabled="joinedStatus(club.id)"
              @click="applyClub(club.id)"
            >
              {{ joinedStatusText(club.id) }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="clubs__pagination">
      <el-pagination
        v-if="page"
        background
        layout="prev, pager, next"
        :current-page="page.number + 1"
        :page-size="page.size"
        :total="page.totalElements"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog v-model="dialogVisible" title="新建社团" width="520px">
      <el-form :model="clubForm" :rules="clubRules" ref="clubFormRef" label-width="88px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="clubForm.name" />
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input v-model="clubForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-select v-model="clubForm.category" placeholder="请选择">
            <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="Logo 链接">
          <el-input v-model="clubForm.logoUrl" placeholder="http(s)://" />
        </el-form-item>
        <el-form-item label="特色视频">
          <el-input v-model="clubForm.promoVideoUrl" placeholder="http(s)://" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="clubForm.tags" multiple placeholder="可选择多项" filterable>
            <el-option v-for="tag in allTags" :key="tag" :label="tag" :value="tag" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitClub">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { fetchClubs, createClub, fetchClubRecommendations } from '../api/club';
import { applyMembership, fetchMyMemberships } from '../api/membership';
import api from '../api/http';
import type { ClubSummary, MembershipRecord, PageResponse } from '../types/models';
import { useAuthStore } from '../store/auth';

const auth = useAuthStore();
const router = useRouter();

const categories = ['文艺', '体育', '学术', '公益', '科技', '创新', '职业发展'];
const query = reactive({ keywords: '', category: '', page: 0, size: 9 });

const page = ref<PageResponse<ClubSummary>>();
const clubs = computed(() => page.value?.content ?? []);
const memberships = ref<MembershipRecord[]>([]);
const joiningClubId = ref<number | null>(null);

const dialogVisible = ref(false);
const creating = ref(false);
const clubFormRef = ref<FormInstance>();
const allTags = ref<string[]>([]);

const clubForm = reactive({
  name: '',
  description: '',
  category: '',
  logoUrl: '',
  promoVideoUrl: '',
  tags: [] as string[],
});

const recommendations = ref<ClubSummary[]>([]);
const recommendationsLoading = ref(false);

const showRecommendations = computed(() => recommendations.value.length > 0);

const clubRules: FormRules<typeof clubForm> = {
  name: [{ required: true, message: '请输入社团名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入社团简介', trigger: 'blur' }],
  category: [{ required: true, message: '请选择社团类别', trigger: 'change' }],
};

const isManager = computed(() => auth.roles.some((role) => role === 'CLUB_MANAGER' || role === 'SYSTEM_ADMIN' || role === 'UNION_STAFF'));

const loadClubs = async () => {
  page.value = await fetchClubs({
    page: query.page,
    size: query.size,
    keywords: query.keywords || undefined,
    category: query.category || undefined,
  });
};

const loadMemberships = async () => {
  memberships.value = await fetchMyMemberships();
};

const loadTags = async () => {
  const { data } = await api.get<string[]>('/api/auth/tags');
  allTags.value = data;
};

const loadRecommendations = async () => {
  recommendationsLoading.value = true;
  try {
    const response = await fetchClubRecommendations({ size: 6 });
    recommendations.value = response.content;
  } finally {
    recommendationsLoading.value = false;
  }
};

const refresh = async () => {
  query.page = 0;
  await loadClubs();
};

const handlePageChange = async (pageIndex: number) => {
  query.page = pageIndex - 1;
  await loadClubs();
};

const joinedStatus = (clubId: number) => {
  const record = memberships.value.find((m) => m.clubId === clubId);
  if (!record) return false;
  return record.status === 'PENDING' || record.status === 'APPROVED';
};

const joinedStatusText = (clubId: number) => {
  const record = memberships.value.find((m) => m.clubId === clubId);
  if (!record) return '申请加入';
  if (record.status === 'PENDING') return '待审核';
  if (record.status === 'APPROVED') return '已加入';
  return '重新申请';
};

const applyClub = async (clubId: number) => {
  joiningClubId.value = clubId;
  try {
    await applyMembership({ clubId, applicationReason: '希望加入社团，期待贡献力量。' });
    ElMessage.success('已提交申请，请等待审核');
    await loadMemberships();
    await loadRecommendations();
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '申请失败，请稍后再试');
  } finally {
    joiningClubId.value = null;
  }
};

const goDetail = (clubId: number) => {
  router.push(`/clubs/${clubId}`);
};

const openDialog = () => {
  dialogVisible.value = true;
};

const resetForm = () => {
  clubForm.name = '';
  clubForm.description = '';
  clubForm.category = '';
  clubForm.logoUrl = '';
  clubForm.promoVideoUrl = '';
  clubForm.tags = [];
};

const submitClub = () => {
  clubFormRef.value?.validate(async (valid) => {
    if (!valid) return;
    creating.value = true;
    try {
      await createClub({ ...clubForm });
      ElMessage.success('社团创建成功');
      dialogVisible.value = false;
      resetForm();
      await loadClubs();
    } catch (error: any) {
      ElMessage.error(error?.response?.data?.message ?? '创建失败，请稍后再试');
    } finally {
      creating.value = false;
    }
  });
};

onMounted(async () => {
  await auth.bootstrap();
  await Promise.all([loadClubs(), loadMemberships(), loadTags(), loadRecommendations()]);
});
</script>

<style scoped>
.clubs {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.clubs__filters {
  border-radius: 12px;
}

.clubs__list {
  min-height: 200px;
}

.clubs__card {
  border-radius: 12px;
  min-height: 240px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.clubs__card-header {
  display: flex;
  gap: 16px;
  align-items: center;
}

.clubs__logo {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  object-fit: cover;
}

.clubs__meta {
  font-size: 13px;
  color: #6b7280;
}

.clubs__description {
  color: #4b5563;
  min-height: 60px;
}

.clubs__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.clubs__actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.clubs__pagination {
  display: flex;
  justify-content: center;
}

.clubs__recommend {
  border-radius: 12px;
}

.clubs__recommend-header {
  font-weight: 600;
}
</style>
