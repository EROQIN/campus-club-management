<template>
  <el-card shadow="never" class="profile">
    <template #header>
      <h3>个人资料</h3>
    </template>
    <el-form :model="form" label-width="88px" :rules="rules" ref="formRef">
      <el-form-item label="姓名">
        <el-input v-model="profile.fullName" disabled />
      </el-form-item>
      <el-form-item label="学号">
        <el-input v-model="profile.studentNumber" disabled />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="profile.email" disabled />
      </el-form-item>
      <el-form-item label="头像地址" prop="avatarUrl">
        <el-input v-model="form.avatarUrl" placeholder="http(s)://" />
      </el-form-item>
      <el-form-item label="个人简介" prop="bio">
        <el-input
          type="textarea"
          :rows="4"
          v-model="form.bio"
          placeholder="介绍你的兴趣、技能或希望参与的活动"
        />
      </el-form-item>
      <el-form-item label="兴趣标签" prop="interestTags">
        <el-select v-model="form.interestTags" multiple placeholder="选择兴趣" filterable>
          <el-option v-for="tag in tags" :key="tag" :label="tag" :value="tag" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="submit">保存修改</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../store/auth';
import api from '../api/http';
import { updateProfile } from '../api/user';

const auth = useAuthStore();
const tags = ref<string[]>([]);
const saving = ref(false);
const formRef = ref<FormInstance>();

const profile = computed(() => auth.profile ?? {
  fullName: '',
  studentNumber: '',
  email: '',
  bio: '',
  avatarUrl: '',
  interests: [] as string[],
});

const form = reactive({
  bio: '',
  avatarUrl: '',
  interestTags: [] as string[],
});

const rules: FormRules<typeof form> = {
  bio: [{ max: 500, message: '个人简介不超过500字', trigger: 'blur' }],
  avatarUrl: [
    {
      type: 'url',
      message: '请输入合法的链接',
      trigger: 'blur',
    },
  ],
  interestTags: [{ type: 'array', max: 5, message: '最多选择5个兴趣', trigger: 'change' }],
};

const submit = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return;
    saving.value = true;
    try {
      const updated = await updateProfile({
        bio: form.bio,
        avatarUrl: form.avatarUrl,
        interestTags: form.interestTags,
      });
      auth.profile = updated;
      ElMessage.success('资料更新成功');
    } catch (error: any) {
      ElMessage.error(error?.response?.data?.message ?? '保存失败');
    } finally {
      saving.value = false;
    }
  });
};

const loadTags = async () => {
  const { data } = await api.get<string[]>('/api/auth/tags');
  tags.value = data;
};

onMounted(async () => {
  await auth.bootstrap();
  if (!auth.profile) {
    await auth.fetchProfile();
  }
  form.bio = auth.profile?.bio ?? '';
  form.avatarUrl = auth.profile?.avatarUrl ?? '';
  form.interestTags = [...(auth.profile?.interests ?? [])];
  await loadTags();
});
</script>

<style scoped>
.profile {
  max-width: 640px;
  border-radius: 12px;
}
</style>
