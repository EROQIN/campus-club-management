<template>
  <div class="auth">
    <div class="auth__background" aria-hidden="true">
      <span class="auth__grid"></span>
      <span class="auth__orb auth__orb--primary"></span>
      <span class="auth__orb auth__orb--secondary"></span>
    </div>
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
  padding: 48px 16px;
  background: radial-gradient(120% 120% at 10% 10%, #0f172a 0%, #111827 30%, #020617 70%);
  position: relative;
  overflow: hidden;
}

.auth__card {
  width: 360px;
  max-width: 100%;
  border-radius: 24px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  box-shadow: 0 40px 70px rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(18px);
  background: rgba(15, 23, 42, 0.72);
  color: #e2e8f0;
}

.auth__card h2 {
  margin-bottom: 24px;
  font-weight: 600;
  color: #f8fafc;
  text-align: center;
}

.auth__card :deep(.el-input__wrapper) {
  background-color: rgba(15, 23, 42, 0.6);
  border-color: rgba(148, 163, 184, 0.35);
}

.auth__card :deep(.el-input__inner) {
  color: #f8fafc;
}

.auth__card :deep(.el-input__inner::placeholder) {
  color: rgba(148, 163, 184, 0.7);
}

.auth__card :deep(.el-form-item__label) {
  color: rgba(226, 232, 240, 0.85);
  font-weight: 500;
}

.auth__hint {
  text-align: center;
  color: rgba(148, 163, 184, 0.9);
}

.auth__background {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.auth__grid {
  position: absolute;
  inset: 12% 4% 8% 4%;
  background-image: linear-gradient(rgba(30, 64, 175, 0.18) 1px, transparent 1px),
    linear-gradient(90deg, rgba(14, 116, 144, 0.18) 1px, transparent 1px);
  background-size: 46px 46px;
  opacity: 0.6;
}

.auth__orb {
  position: absolute;
  filter: blur(0);
  border-radius: 999px;
  opacity: 0.65;
}

.auth__orb--primary {
  width: 520px;
  height: 520px;
  top: -180px;
  right: -220px;
  background: radial-gradient(circle at 30% 30%, rgba(59, 130, 246, 0.9), transparent 70%);
  animation: drift 26s ease-in-out infinite alternate;
}

.auth__orb--secondary {
  width: 380px;
  height: 380px;
  bottom: -140px;
  left: -80px;
  background: radial-gradient(circle at 60% 60%, rgba(45, 212, 191, 0.8), transparent 65%);
  animation: drift 22s ease-in-out infinite alternate-reverse;
}

@keyframes drift {
  from {
    transform: translate3d(0, 0, 0) rotate(0deg);
  }
  to {
    transform: translate3d(3%, -3%, 0) rotate(10deg);
  }
}

@media (max-width: 640px) {
  .auth {
    padding: 32px 12px;
  }

  .auth__card {
    border-radius: 20px;
    box-shadow: 0 28px 48px rgba(15, 23, 42, 0.45);
  }
}

@media (prefers-reduced-motion: reduce) {
  .auth__orb {
    animation: none;
  }
}
</style>
