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
      <el-form-item label="头像" prop="avatarUrl">
        <div class="avatar-field">
          <el-avatar :size="96" :src="form.avatarUrl">
            {{ profile.fullName?.slice(0, 1) ?? '?' }}
          </el-avatar>
          <div class="avatar-field__actions">
            <el-upload
              :auto-upload="false"
              :show-file-list="false"
              :http-request="handleAvatarUpload"
            >
              <el-button type="primary" :loading="avatarUploading">上传头像</el-button>
            </el-upload>
            <el-button
              v-if="form.avatarUrl"
              text
              type="danger"
              @click="clearAvatar"
            >
              移除
            </el-button>
            <span class="avatar-field__hint">支持 JPG/PNG/WebP，大小 ≤ 5MB，建议正方形图片。</span>
          </div>
        </div>
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
import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../store/auth';
import api from '../api/http';
import { updateProfile } from '../api/user';
import { uploadImage } from '../api/storage';

const auth = useAuthStore();
const tags = ref<string[]>([]);
const saving = ref(false);
const formRef = ref<FormInstance>();
const avatarUploading = ref(false);

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

const handleAvatarUpload = async (options: UploadRequestOptions) => {
  const { file, onError, onSuccess } = options;
  try {
    avatarUploading.value = true;
    const result = await uploadImage(file as File, 'users/avatars');
    form.avatarUrl = result.url;
    onSuccess?.(result);
    ElMessage.success('头像上传成功');
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '头像上传失败，请稍后再试');
    onError?.(error);
  } finally {
    avatarUploading.value = false;
  }
};

const clearAvatar = () => {
  form.avatarUrl = '';
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

.avatar-field {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-field__actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.avatar-field__hint {
  font-size: 12px;
  color: #94a3b8;
}
</style>
