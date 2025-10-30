<template>
  <div class="admin-users">
    <div class="admin-users__header">
      <h2>用户与权限管理</h2>
      <el-input
        v-model="keyword"
        placeholder="按姓名/邮箱/学号搜索"
        clearable
        @clear="loadUsers"
        @keyup.enter.native="loadUsers"
        style="width: 260px"
      >
        <template #append>
          <el-button @click="loadUsers">搜索</el-button>
        </template>
      </el-input>
    </div>

    <el-card shadow="never">
      <el-table :data="users" v-loading="loading" empty-text="暂无用户">
        <el-table-column prop="fullName" label="姓名" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column prop="studentNumber" label="学号" width="140" />
        <el-table-column label="角色" min-width="220">
          <template #default="{ row }">
            <el-select
              v-model="row.roles"
              multiple
              collapse-tags
              placeholder="请选择"
              style="width: 200px"
              @change="updateRoles(row)"
            >
              <el-option
                v-for="role in roleOptions"
                :key="role.value"
                :label="role.label"
                :value="role.value"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              inline-prompt
              active-text="启用"
              inactive-text="禁用"
              @change="updateStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="最近登录" width="180">
          <template #default="{ row }">
            {{ formatDatetime(row.lastLoginAt) }}
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDatetime(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import dayjs from 'dayjs';
import { ElMessage } from 'element-plus';
import { fetchUsers, updateUserRoles, updateUserStatus } from '../api/admin';
import type { UserAdmin } from '../types/models';

const users = ref<UserAdmin[]>([]);
const loading = ref(false);
const keyword = ref('');

const roleOptions = [
  { value: 'STUDENT', label: '学生' },
  { value: 'CLUB_MANAGER', label: '社团负责人' },
  { value: 'UNION_STAFF', label: '团委' },
  { value: 'SYSTEM_ADMIN', label: '系统管理员' },
];

const loadUsers = async () => {
  loading.value = true;
  try {
    users.value = await fetchUsers(keyword.value.trim() || undefined);
  } finally {
    loading.value = false;
  }
};

const formatDatetime = (value?: string | null) => {
  if (!value) return '-';
  return dayjs(value).format('YYYY-MM-DD HH:mm');
};

const updateRoles = async (user: UserAdmin) => {
  try {
    await updateUserRoles(user.id, user.roles as string[]);
    ElMessage.success('角色更新成功');
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '更新角色失败');
    await loadUsers();
  }
};

const updateStatus = async (user: UserAdmin) => {
  try {
    await updateUserStatus(user.id, user.enabled);
    ElMessage.success('状态已更新');
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '更新状态失败');
    await loadUsers();
  }
};

onMounted(() => {
  loadUsers();
});
</script>

<style scoped>
.admin-users {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.admin-users__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
