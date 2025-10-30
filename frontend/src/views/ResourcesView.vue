<template>
  <div class="resources">
    <div class="resources__header">
      <h2>资源共享池</h2>
      <el-button v-if="isManager" type="primary" @click="openModal()">发布资源</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="资源列表" name="list">
        <el-card shadow="never">
          <el-table :data="resources" v-loading="loading" empty-text="暂无资源">
            <el-table-column prop="name" label="资源名称" min-width="160" />
            <el-table-column prop="resourceType" label="类型" width="120" />
            <el-table-column prop="clubName" label="所属社团" min-width="160" />
            <el-table-column prop="contact" label="联系方式" width="160" />
            <el-table-column label="可用时间" min-width="200">
              <template #default="{ row }">
                {{ formatRange(row.availableFrom, row.availableUntil) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <template v-if="isManager && isOwnedResource(row)">
                  <el-button link type="primary" @click="openModal(row)">编辑</el-button>
                  <el-button link type="danger" @click="remove(row.id)">删除</el-button>
                </template>
                <template v-else>
                  <el-button
                    size="small"
                    type="primary"
                    @click="openApplyDialog(row)"
                  >申请使用</el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane v-if="!isManager" label="我的申请" name="my">
        <el-card shadow="never">
          <el-table
            :data="myApplications"
            v-loading="myApplicationsLoading"
            empty-text="暂无申请记录"
          >
            <el-table-column prop="resourceName" label="资源名称" min-width="160" />
            <el-table-column prop="purpose" label="申请用途" min-width="200" />
            <el-table-column label="使用时间" min-width="200">
              <template #default="{ row }">
                {{ formatRange(row.requestedFrom, row.requestedUntil) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)">{{ formatStatus(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="回复" min-width="160" prop="replyMessage" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'PENDING'"
                  link
                  type="danger"
                  @click="cancelApplication(row.id)"
                >撤回</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane v-if="isManager" label="申请处理" name="manage">
        <el-card shadow="never">
          <el-table
            :data="managerApplications"
            v-loading="applicationsLoading"
            empty-text="暂无待处理申请"
          >
            <el-table-column prop="resourceName" label="资源名称" min-width="160" />
            <el-table-column prop="applicantName" label="申请人" width="140" />
            <el-table-column prop="purpose" label="用途" min-width="220" />
            <el-table-column label="使用时间" min-width="200">
              <template #default="{ row }">
                {{ formatRange(row.requestedFrom, row.requestedUntil) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)">{{ formatStatus(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="replyMessage" label="回复" min-width="160" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <template v-if="row.status === 'PENDING'">
                  <el-button link type="success" @click="handleDecision(row, true)">通过</el-button>
                  <el-button link type="danger" @click="handleDecision(row, false)">拒绝</el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑资源' : '新增资源'" width="520px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="所属社团" prop="clubId">
          <el-select v-model="form.clubId" placeholder="请选择社团">
            <el-option
              v-for="club in myClubs"
              :key="club.id"
              :label="club.name"
              :value="club.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="资源名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="资源类型" prop="resourceType">
          <el-select v-model="form.resourceType" placeholder="请选择">
            <el-option v-for="type in types" :key="type" :label="type" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源描述">
          <el-input type="textarea" :rows="3" v-model="form.description" />
        </el-form-item>
        <el-form-item label="可用时间">
          <el-date-picker
            v-model="form.range"
            type="datetimerange"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="form.contact" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="applyDialogVisible" title="申请使用资源" width="520px">
      <el-form :model="applyForm" :rules="applyRules" ref="applyFormRef" label-width="90px">
        <el-form-item label="资源" prop="resourceId">
          <el-input v-model="applyResourceName" disabled />
        </el-form-item>
        <el-form-item label="申请用途" prop="purpose">
          <el-input type="textarea" :rows="3" v-model="applyForm.purpose" />
        </el-form-item>
        <el-form-item label="使用时间">
          <el-date-picker
            v-model="applyForm.range"
            type="datetimerange"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="applying" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import dayjs from 'dayjs';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  fetchResources,
  createResource,
  updateResource,
  deleteResource,
  applyResource,
  listMyResourceApplications,
  listResourceApplications,
  decideResourceApplication,
  cancelResourceApplication,
} from '../api/resource';
import { fetchMyClubs } from '../api/club';
import type {
  SharedResource,
  ClubSummary,
  ResourceApplicationResponse,
} from '../types/models';
import { useAuthStore } from '../store/auth';

const auth = useAuthStore();
const loading = ref(false);
const applicationsLoading = ref(false);
const myApplicationsLoading = ref(false);
const resources = ref<SharedResource[]>([]);
const dialogVisible = ref(false);
const applyDialogVisible = ref(false);
const saving = ref(false);
const applying = ref(false);
const formRef = ref<FormInstance>();
const applyFormRef = ref<FormInstance>();
const activeTab = ref('list');
const myClubs = ref<ClubSummary[]>([]);
const myApplications = ref<ResourceApplicationResponse[]>([]);
const managerApplications = ref<ResourceApplicationResponse[]>([]);

const form = reactive({
  id: undefined as number | undefined,
  clubId: undefined as number | undefined,
  name: '',
  resourceType: '',
  description: '',
  range: [] as [Date, Date] | [],
  contact: '',
});

const applyForm = reactive({
  resourceId: 0,
  purpose: '',
  range: [] as [Date, Date] | [],
});

const applyResourceName = ref('');

const types = ['场地', '设备', '技能培训', '宣传渠道', '人力支持'];

const rules: FormRules<typeof form> = {
  clubId: [{ required: true, message: '请选择社团', trigger: 'change' }],
  name: [{ required: true, message: '请输入资源名称', trigger: 'blur' }],
  resourceType: [{ required: true, message: '请选择资源类型', trigger: 'change' }],
};

const applyRules: FormRules<typeof applyForm> = {
  purpose: [
    { required: true, message: '请填写申请用途', trigger: 'blur' },
    { min: 5, message: '用途至少包含 5 个字符', trigger: 'blur' },
  ],
};

const isManager = computed(() =>
  auth.roles.some((role) => ['CLUB_MANAGER', 'SYSTEM_ADMIN', 'UNION_STAFF'].includes(role)),
);

const ownedClubIds = computed(() => new Set(myClubs.value.map((club) => club.id)));

const hasGlobalResourcePermission = computed(() =>
  auth.roles.includes('UNION_STAFF') || auth.roles.includes('SYSTEM_ADMIN'),
);

const isOwnedResource = (resource: SharedResource) =>
  hasGlobalResourcePermission.value || ownedClubIds.value.has(resource.clubId);

const formatRange = (start?: string | null, end?: string | null) => {
  if (!start && !end) return '长期可用';
  return `${start ? dayjs(start).format('MM-DD HH:mm') : '立即'} ~ ${end ? dayjs(end).format('MM-DD HH:mm') : '长期'}`;
};

const statusTagType = (status: ResourceApplicationResponse['status']) => {
  switch (status) {
    case 'APPROVED':
      return 'success';
    case 'REJECTED':
      return 'danger';
    case 'PENDING':
      return 'warning';
    default:
      return 'info';
  }
};

const formatStatus = (status: ResourceApplicationResponse['status']) => {
  switch (status) {
    case 'PENDING':
      return '待处理';
    case 'APPROVED':
      return '已通过';
    case 'REJECTED':
      return '已拒绝';
    case 'CANCELLED':
      return '已撤回';
    default:
      return status;
  }
};

const loadResources = async () => {
  loading.value = true;
  try {
    resources.value = await fetchResources();
  } finally {
    loading.value = false;
  }
};

const loadMyClubs = async () => {
  if (!isManager.value) {
    myClubs.value = [];
    return;
  }
  myClubs.value = await fetchMyClubs();
};

const openModal = (resource?: SharedResource) => {
  if (resource) {
    form.id = resource.id;
    form.clubId = resource.clubId;
    form.name = resource.name;
    form.resourceType = resource.resourceType;
    form.description = resource.description ?? '';
    form.contact = resource.contact ?? '';
    form.range = resource.availableFrom && resource.availableUntil
      ? [new Date(resource.availableFrom), new Date(resource.availableUntil)]
      : [];
  } else {
    form.id = undefined;
    form.clubId = myClubs.value[0]?.id;
    form.name = '';
    form.resourceType = '';
    form.description = '';
    form.contact = '';
    form.range = [];
  }
  dialogVisible.value = true;
};

const submit = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return;
    saving.value = true;
    try {
      const payload = {
        name: form.name,
        resourceType: form.resourceType,
        description: form.description,
        contact: form.contact,
        availableFrom: form.range[0] ? dayjs(form.range[0]).toISOString() : undefined,
        availableUntil: form.range[1] ? dayjs(form.range[1]).toISOString() : undefined,
      };
      if (form.id) {
        await updateResource(form.id, payload);
      } else {
        await createResource(form.clubId!, payload);
      }
      ElMessage.success('保存成功');
      dialogVisible.value = false;
      await loadResources();
      if (isManager.value && activeTab.value === 'manage') {
        await loadManagerApplications();
      }
    } catch (error: any) {
      ElMessage.error(error?.response?.data?.message ?? '保存失败');
    } finally {
      saving.value = false;
    }
  });
};

const remove = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除该资源吗？', '提示', { type: 'warning' });
    await deleteResource(id);
    ElMessage.success('删除成功');
    await loadResources();
    if (isManager.value && activeTab.value === 'manage') {
      await loadManagerApplications();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败，请稍后再试');
    }
  }
};

const openApplyDialog = (resource: SharedResource) => {
  applyForm.resourceId = resource.id;
  applyForm.purpose = '';
  applyForm.range = [];
  applyResourceName.value = resource.name;
  applyDialogVisible.value = true;
};

const submitApply = () => {
  applyFormRef.value?.validate(async (valid) => {
    if (!valid) return;
    applying.value = true;
    try {
      const payload = {
        purpose: applyForm.purpose,
        requestedFrom: applyForm.range[0] ? dayjs(applyForm.range[0]).toISOString() : undefined,
        requestedUntil: applyForm.range[1] ? dayjs(applyForm.range[1]).toISOString() : undefined,
      };
      await applyResource(applyForm.resourceId, payload);
      ElMessage.success('已提交申请');
      applyDialogVisible.value = false;
      if (activeTab.value === 'my') {
        await loadMyApplications();
      }
    } catch (error: any) {
      ElMessage.error(error?.response?.data?.message ?? '申请失败，请稍后再试');
    } finally {
      applying.value = false;
    }
  });
};

const loadMyApplications = async () => {
  myApplicationsLoading.value = true;
  try {
    myApplications.value = await listMyResourceApplications();
  } finally {
    myApplicationsLoading.value = false;
  }
};

const cancelApplication = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认撤回该申请吗？', '提示', { type: 'warning' });
    await cancelResourceApplication(id);
    ElMessage.success('已撤回申请');
    await loadMyApplications();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('撤回失败，请稍后再试');
    }
  }
};

const loadManagerApplications = async () => {
  if (!isManager.value) return;
  applicationsLoading.value = true;
  try {
    const owned = hasGlobalResourcePermission.value
      ? resources.value
      : resources.value.filter((resource) => isOwnedResource(resource));
    const lists = await Promise.all(
      owned.map(async (resource) => {
        const rows = await listResourceApplications(resource.id);
        return rows.map((row) => ({ ...row, resourceName: `${resource.name}` }));
      }),
    );
    managerApplications.value = lists.flat().sort((a, b) => dayjs(b.createdAt).valueOf() - dayjs(a.createdAt).valueOf());
  } finally {
    applicationsLoading.value = false;
  }
};

const handleDecision = async (application: ResourceApplicationResponse, approve: boolean) => {
  let replyMessage = '';
  if (!approve) {
    const result = await ElMessageBox.prompt('请给申请人一些说明（可选）', '拒绝申请', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      inputPlaceholder: '可填写拒绝原因',
      inputType: 'textarea',
      distinguishCancelAndClose: true,
    }).catch((error) => ({ action: error } as { action: string }));
    if (!result || (result as any).action === 'cancel') {
      return;
    }
    replyMessage = (result as any).value ?? '';
  }
  try {
    await decideResourceApplication(application.id, { approve, replyMessage });
    ElMessage.success('已更新申请状态');
    await loadManagerApplications();
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '操作失败，请稍后再试');
  }
};

const handleTabChange = async (name: string | number) => {
  if (name === 'my') {
    await loadMyApplications();
  }
  if (name === 'manage') {
    await loadManagerApplications();
  }
};

onMounted(async () => {
  await auth.bootstrap();
  activeTab.value = isManager.value ? 'manage' : 'list';
  await Promise.all([loadResources(), loadMyClubs()]);
  if (!isManager.value) {
    await loadMyApplications();
  } else {
    await loadManagerApplications();
  }
});

watch(
  () => isManager.value,
  async (value) => {
    activeTab.value = value ? 'manage' : 'list';
    await loadMyClubs();
  },
);
</script>

<style scoped>
.resources {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.resources__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
