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

    <el-card shadow="never" class="clubs__hero">
      <div class="clubs__hero-content">
        <div>
          <h2>社团广场</h2>
          <p class="clubs__hero-desc">
            发现校园里的精彩组织，与志同道合的伙伴一起成长与探索。
          </p>
          <el-space wrap>
            <el-button type="primary" @click="refresh" :loading="clubsLoading">刷新推荐</el-button>
            <el-button
              v-if="!isManager"
              text
              type="info"
              @click="loadRecommendations"
              :loading="recommendationsLoading"
            >
              重新匹配兴趣
            </el-button>
          </el-space>
        </div>
        <el-image
          class="clubs__hero-illustration"
          src="https://img.alicdn.com/imgextra/i4/O1CN01uGhOQe1pRecX90uzx_!!6000000005337-2-tps-800-600.png"
          fit="contain"
        >
          <template #error>
            <el-empty description="精彩社团在等你" :image-size="120" />
          </template>
        </el-image>
      </div>
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

    <el-alert
      v-if="loadError"
      type="error"
      :closable="false"
      class="clubs__error"
      show-icon
      :title="loadErrorMessage"
    />

    <el-skeleton :loading="clubsLoading" animated>
      <template #template>
        <el-row :gutter="20" class="clubs__list">
          <el-col :span="8" v-for="n in 6" :key="n">
            <el-card shadow="never" class="clubs__card clubs__card--placeholder">
              <el-skeleton-item variant="image" class="clubs__logo-skeleton" />
              <el-skeleton-item variant="h3" style="width: 60%" />
              <el-skeleton-item variant="text" />
              <el-skeleton-item variant="text" />
              <el-skeleton-item variant="text" style="width: 80%" />
            </el-card>
          </el-col>
        </el-row>
      </template>
      <template #default>
        <template v-if="clubs.length">
          <el-row :gutter="20" class="clubs__list">
            <el-col :span="8" v-for="club in clubs" :key="club.id">
              <el-card shadow="hover" class="clubs__card">
                <div class="clubs__card-header">
                  <el-avatar
                    v-if="club.logoUrl"
                    :src="club.logoUrl"
                    :size="56"
                    shape="square"
                    fit="cover"
                  />
                  <div>
                    <h3>{{ club.name }}</h3>
                    <div class="clubs__meta">类别：{{ club.category || '未分类' }}</div>
                    <div class="clubs__meta clubs__meta--members">
                      <el-icon><UserFilled /></el-icon>
                      <span>{{ club.memberCount }} 人</span>
                    </div>
                  </div>
                </div>
                <p class="clubs__description">{{ club.description }}</p>
                <div class="clubs__tags" v-if="(club.tags ?? []).length">
                  <el-tag
                    v-for="tag in club.tags ?? []"
                    :key="tag"
                    effect="light"
                    size="small"
                    round
                  >
                    {{ tag }}
                  </el-tag>
                </div>
                <div class="clubs__actions">
                  <el-button text type="primary" @click="goDetail(club.id)">查看详情</el-button>
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
        </template>
        <el-empty
          v-else-if="!clubsLoading && !loadError"
          description="暂无符合条件的社团"
          :image-size="160"
        >
          <el-button type="primary" @click="refresh">清空筛选条件</el-button>
        </el-empty>
      </template>
    </el-skeleton>

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
import { UserFilled } from '@element-plus/icons-vue';

const auth = useAuthStore();
const router = useRouter();

const categories = ['文艺', '体育', '学术', '公益', '科技', '创新', '职业发展'];
const query = reactive({ keywords: '', category: '', page: 0, size: 9 });

const page = ref<PageResponse<ClubSummary>>();
const clubs = computed(() => page.value?.content ?? []);
const memberships = ref<MembershipRecord[]>([]);
const joiningClubId = ref<number | null>(null);
const clubsLoading = ref(false);
const loadError = ref<string | null>(null);
const loadErrorMessage = computed(() => loadError.value ?? '');

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
  clubsLoading.value = true;
  loadError.value = null;
  try {
    page.value = await fetchClubs({
      page: query.page,
      size: query.size,
      keywords: query.keywords || undefined,
      category: query.category || undefined,
    });
  } catch (error: any) {
    loadError.value = error?.response?.data?.message ?? '社团信息加载失败，请稍后再试';
    page.value = undefined;
    ElMessage.error({ message: loadErrorMessage.value });
  } finally {
    clubsLoading.value = false;
  }
};

const loadMemberships = async () => {
  try {
    memberships.value = await fetchMyMemberships();
  } catch (error) {
    console.error('加载已加入社团失败', error);
  }
};

const loadTags = async () => {
  try {
    const { data } = await api.get<string[]>('/api/auth/tags');
    allTags.value = data;
  } catch (error) {
    console.error('加载标签失败', error);
  }
};

const loadRecommendations = async () => {
  recommendationsLoading.value = true;
  try {
    const response = await fetchClubRecommendations({ size: 6 });
    recommendations.value = response.content;
  } catch (error: any) {
    recommendations.value = [];
    if (error?.response?.status !== 404) {
      ElMessage.warning({
        message: error?.response?.data?.message ?? '推荐社团加载失败',
      });
    }
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
  await Promise.allSettled([loadClubs(), loadMemberships(), loadTags(), loadRecommendations()]);
});
</script>

<style scoped>
.clubs {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-bottom: 20px;
}

.clubs__filters {
  border-radius: 12px;
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 80%);
  border: 1px solid #e2e8f0;
}

.clubs__hero {
  border-radius: 16px;
  background: radial-gradient(circle at 20% 20%, #f0f9ff, #ffffff 65%);
  border: 1px solid #dbeafe;
}

.clubs__hero-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
}

.clubs__hero-content h2 {
  margin-bottom: 6px;
}

.clubs__hero-desc {
  color: #475569;
  margin-bottom: 16px;
  max-width: 420px;
}

.clubs__hero-illustration {
  max-width: 240px;
}

.clubs__error {
  border-radius: 12px;
  overflow: hidden;
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
  border: 1px solid #e2e8f0;
  box-shadow: 0 12px 24px -16px rgba(15, 23, 42, 0.35);
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

.clubs__meta--members {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
  color: #1f2937;
}

.clubs__description {
  color: #4b5563;
  min-height: 60px;
  line-height: 1.5;
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
  margin-top: 12px;
}

.clubs__pagination {
  display: flex;
  justify-content: center;
}

.clubs__recommend {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.clubs__recommend-header {
  font-weight: 600;
}

.clubs__card--placeholder {
  gap: 12px;
  padding: 20px;
}

.clubs__logo-skeleton {
  width: 56px;
  height: 56px;
  border-radius: 12px;
}
</style>
