<template>
  <div class="collab">
    <h2>跨社团协作</h2>

    <el-card shadow="never" class="collab__card">
      <template #header>
        <div class="collab__header">
          <span>发起协作提案</span>
          <el-select v-model="selectedClubId" placeholder="选择我的社团" style="width: 260px" @change="loadMyProposals">
            <el-option v-for="club in myClubs" :key="club.id" :label="club.name" :value="club.id" />
          </el-select>
        </div>
      </template>
      <el-form :model="proposalForm" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="proposalForm.title" maxlength="150" show-word-limit />
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model="proposalForm.collaborationType" placeholder="联合活动 / 资源共享等" />
        </el-form-item>
        <el-form-item label="资源需求">
          <el-input v-model="proposalForm.requiredResources" placeholder="所需支持" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input type="textarea" :rows="4" v-model="proposalForm.description" maxlength="1000" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="creating" :disabled="!selectedClubId" @click="submitProposal">
            发布提案
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="collab__content">
      <el-card shadow="never" class="collab__card">
        <template #header>
          <span>我发起的提案</span>
        </template>
        <el-table :data="myProposals" v-loading="loadingMy" empty-text="暂无数据">
          <el-table-column prop="title" label="标题" min-width="160" />
          <el-table-column prop="collaborationType" label="类型" width="120" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDatetime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="响应" min-width="200">
            <template #default="{ row }">
              <div class="collab__responses" v-if="row.responses?.length">
                <el-tag
                  v-for="response in row.responses"
                  :key="response.id"
                  :type="response.status === 'ACCEPTED' ? 'success' : response.status === 'DECLINED' ? 'danger' : 'info'"
                  size="small"
                >
                  {{ response.responderClubName }} ({{ response.status }})
                </el-tag>
              </div>
              <span v-else>—</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card shadow="never" class="collab__card">
        <template #header>
          <span>协作广场</span>
        </template>
        <el-table :data="allProposals" v-loading="loadingAll" empty-text="暂无提案">
          <el-table-column prop="title" label="标题" min-width="160" />
          <el-table-column prop="initiatorClubName" label="发起社团" min-width="140" />
          <el-table-column prop="collaborationType" label="类型" width="120" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button
                type="primary"
                text
                size="small"
                @click="openRespondDialog(row)"
              >
                响应
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-dialog v-model="respondDialogVisible" title="回应提案" width="520px">
      <div class="collab__respond-meta" v-if="activeProposal">
        <p><strong>{{ activeProposal.title }}</strong></p>
        <p class="muted">发起：{{ activeProposal.initiatorClubName }}</p>
      </div>
      <el-form :model="responseForm" label-width="90px">
        <el-form-item label="选择社团">
          <el-select v-model="responseClubId" placeholder="选择我的社团">
            <el-option v-for="club in myClubs" :key="club.id" :label="club.name" :value="club.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="回复">
          <el-input type="textarea" :rows="3" v-model="responseForm.message" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="responseForm.status">
            <el-radio label="ACCEPTED">接受</el-radio>
            <el-radio label="DECLINED">拒绝</el-radio>
            <el-radio label="PENDING">待讨论</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="respondDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="responding" @click="submitResponse">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import dayjs from 'dayjs';
import { ElMessage } from 'element-plus';
import { fetchMyClubs } from '../api/club';
import {
  createCollaborationProposal,
  fetchMyCollaborationProposals,
  fetchCollaborationProposals,
  respondCollaborationProposal,
} from '../api/collaboration';
import type { ClubSummary, CollaborationProposalItem } from '../types/models';

const myClubs = ref<ClubSummary[]>([]);
const selectedClubId = ref<number | null>(null);
const myProposals = ref<CollaborationProposalItem[]>([]);
const allProposals = ref<CollaborationProposalItem[]>([]);
const loadingMy = ref(false);
const loadingAll = ref(false);
const creating = ref(false);
const responding = ref(false);
const respondDialogVisible = ref(false);
const activeProposal = ref<CollaborationProposalItem | null>(null);
const responseClubId = ref<number | null>(null);

const proposalForm = reactive({
  title: '',
  description: '',
  collaborationType: '',
  requiredResources: '',
});

const responseForm = reactive({
  message: '',
  status: 'PENDING',
});

const formatDatetime = (value?: string | null) => {
  if (!value) return '--';
  return dayjs(value).format('YYYY-MM-DD HH:mm');
};

const resetProposalForm = () => {
  proposalForm.title = '';
  proposalForm.description = '';
  proposalForm.collaborationType = '';
  proposalForm.requiredResources = '';
};

const submitProposal = async () => {
  if (!selectedClubId.value) {
    ElMessage.warning('请选择社团');
    return;
  }
  if (!proposalForm.title || !proposalForm.description) {
    ElMessage.warning('请完善提案内容');
    return;
  }
  creating.value = true;
  try {
    await createCollaborationProposal(selectedClubId.value, proposalForm);
    ElMessage.success('提案已发布');
    resetProposalForm();
    await loadMyProposals();
    await loadAllProposals();
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '发布失败，请稍后再试');
  } finally {
    creating.value = false;
  }
};

const loadMyProposals = async () => {
  if (!selectedClubId.value) {
    myProposals.value = [];
    return;
  }
  loadingMy.value = true;
  try {
    const result = await fetchMyCollaborationProposals(selectedClubId.value, { page: 0, size: 20 });
    myProposals.value = result.content;
  } finally {
    loadingMy.value = false;
  }
};

const loadAllProposals = async () => {
  loadingAll.value = true;
  try {
    const result = await fetchCollaborationProposals({ page: 0, size: 20 });
    allProposals.value = result.content;
  } finally {
    loadingAll.value = false;
  }
};

const openRespondDialog = (proposal: CollaborationProposalItem) => {
  activeProposal.value = proposal;
  respondDialogVisible.value = true;
  responseForm.message = '';
  responseForm.status = 'PENDING';
  responseClubId.value = selectedClubId.value ?? myClubs.value[0]?.id ?? null;
};

const submitResponse = async () => {
  if (!activeProposal.value || !responseClubId.value) {
    ElMessage.warning('请选择社团');
    return;
  }
  responding.value = true;
  try {
    await respondCollaborationProposal(activeProposal.value.id, responseClubId.value, {
      message: responseForm.message,
      status: responseForm.status,
    });
    ElMessage.success('已提交回应');
    respondDialogVisible.value = false;
    await loadAllProposals();
    await loadMyProposals();
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '提交失败，请稍后再试');
  } finally {
    responding.value = false;
  }
};

onMounted(async () => {
  myClubs.value = await fetchMyClubs();
  const firstClub = myClubs.value[0];
  if (firstClub) {
    selectedClubId.value = firstClub.id;
  }
  await loadMyProposals();
  await loadAllProposals();
});
 </script>
 
 <style scoped>
 .collab {
   display: flex;
   flex-direction: column;
   gap: 20px;
 }
 
 .collab__card {
   border-radius: 12px;
 }
 
 .collab__header {
   display: flex;
   align-items: center;
   justify-content: space-between;
   gap: 16px;
 }
 
 .collab__content {
   display: flex;
   flex-direction: column;
   gap: 20px;
 }
 
 .collab__responses {
   display: flex;
   flex-wrap: wrap;
   gap: 6px;
 }
 
 .collab__respond-meta {
   margin-bottom: 12px;
 }
 
 .collab__respond-meta .muted {
   color: #94a3b8;
   font-size: 13px;
 }
 </style>
