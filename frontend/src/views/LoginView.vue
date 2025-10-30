<template>
  <div class="auth">
    <el-card class="auth__card">
      <h2>登录校园社团平台</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="学号 / 邮箱" prop="identifier">
          <el-input v-model="form.identifier" placeholder="请输入学号或邮箱" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="auth.loading" @click="handleSubmit" block>
            登录
          </el-button>
        </el-form-item>
        <div class="auth__hint">
          还没有账号？
          <el-link type="primary" @click="goRegister">立即注册</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../store/auth';

const auth = useAuthStore();
const router = useRouter();
const route = useRoute();

const form = reactive({
  identifier: '',
  password: '',
});

const rules: FormRules<typeof form> = {
  identifier: [{ required: true, message: '请输入学号或邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

const formRef = ref<FormInstance>();

const handleSubmit = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return;
    try {
      await auth.login(form.identifier, form.password);
      const redirect = (route.query.redirect as string) ?? '/dashboard';
      router.replace(redirect);
      ElMessage.success('登录成功');
    } catch (error: any) {
      ElMessage.error(error?.response?.data?.message ?? '登录失败，请检查账号密码');
    }
  });
};

const goRegister = () => {
  router.push('/register');
};
</script>

<style scoped>
.auth {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2563eb 0%, #38bdf8 100%);
}

.auth__card {
  width: 360px;
}

.auth__hint {
  text-align: center;
  color: #6b7280;
}
</style>
