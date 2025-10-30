<template>
  <div class="auth">
    <el-card class="auth__card">
      <h2>新生注册</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="姓名" prop="fullName">
          <el-input v-model="form.fullName" />
        </el-form-item>
        <el-form-item label="学号" prop="studentNumber">
          <el-input v-model="form.studentNumber" placeholder="请输入11位学号" maxlength="11" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请使用校园邮箱" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="至少8位，包含字母和数字" />
        </el-form-item>
        <el-form-item label="兴趣标签" prop="interestTags">
          <el-select v-model="form.interestTags" multiple placeholder="选择3-5个兴趣" filterable>
            <el-option
              v-for="tag in tags"
              :key="tag"
              :value="tag"
              :label="tag"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.registeringAsManager">
            我是社团负责人 / 希望创建社团
          </el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="auth.loading" @click="handleSubmit" block>
            完成注册
          </el-button>
        </el-form-item>
        <div class="auth__hint">
          已有账号？
          <el-link type="primary" @click="goLogin">返回登录</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import api from '../api/http';
import { useAuthStore } from '../store/auth';

const auth = useAuthStore();
const router = useRouter();
const tags = ref<string[]>([]);

const form = reactive({
  fullName: '',
  studentNumber: '',
  email: '',
  password: '',
  interestTags: [] as string[],
  registeringAsManager: false,
});

const rules: FormRules<typeof form> = {
  fullName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  studentNumber: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    {
      pattern: /^\d{11}$/,
      message: '学号需为11位数字',
      trigger: 'blur',
    },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [
    {
      required: true,
      message: '请输入密码',
      trigger: 'blur',
    },
    {
      pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$/,
      message: '密码至少8位，需包含字母与数字',
      trigger: 'blur',
    },
  ],
  interestTags: [
    { required: true, message: '请选择兴趣标签', trigger: 'change' },
    {
      validator: (_rule, value, callback) => {
        if (!value || value.length < 3 || value.length > 5) {
          callback(new Error('请至少选择3个且不超过5个标签'));
        } else {
          callback();
        }
      },
      trigger: 'change',
    },
  ],
};

const formRef = ref<FormInstance>();

const handleSubmit = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return;
    try {
      await auth.register({
        fullName: form.fullName,
        studentNumber: form.studentNumber,
        email: form.email,
        password: form.password,
        interestTags: form.interestTags,
        registeringAsManager: form.registeringAsManager,
      });
      ElMessage.success('注册成功，欢迎加入！');
      router.replace('/dashboard');
    } catch (error: any) {
      ElMessage.error(error?.response?.data?.message ?? '注册失败，请稍后再试');
    }
  });
};

const goLogin = () => {
  router.push('/login');
};

onMounted(async () => {
  try {
    const { data } = await api.get<string[]>('/api/auth/tags');
    tags.value = data;
  } catch (error) {
    console.error('加载兴趣标签失败', error);
  }
});
</script>

<style scoped>
.auth {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4f46e5 0%, #38bdf8 100%);
}

.auth__card {
  width: 420px;
}

.auth__hint {
  text-align: center;
  color: #6b7280;
}
</style>
