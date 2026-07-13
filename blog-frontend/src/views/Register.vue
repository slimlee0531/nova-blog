<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})
const loading = ref(false)

const handleRegister = async () => {
  if (!form.value.username || !form.value.email || !form.value.password) {
    ElMessage.warning('请填写完整信息')
    return
  }

  if (form.value.password !== form.value.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }

  loading.value = true
  try {
    const success = await userStore.register({
      username: form.value.username,
      email: form.value.email,
      password: form.value.password
    })
    if (success) {
      ElMessage.success('注册成功')
      router.push('/')
    }
  } catch (e) {
    ElMessage.error('注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <h1>创建账号</h1>
        <p>开始你的写作之旅</p>
      </div>

      <el-form :model="form" class="auth-form">
        <el-form-item>
          <el-input
            v-model="form.username"
            placeholder="用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="form.email"
            placeholder="邮箱"
            size="large"
            prefix-icon="Message"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="确认密码"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>

        <button
          class="submit-btn"
          :class="{ loading }"
          :disabled="loading"
          @click="handleRegister"
        >
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </el-form>

      <div class="auth-footer">
        已有账号？
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-gradient);
  padding: var(--spacing-2xl);
  position: relative;
  overflow: hidden;
}

.auth-page::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 80%, rgba(255,255,255,0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255,255,255,0.15) 0%, transparent 50%);
}

.auth-card {
  position: relative;
  width: 100%;
  max-width: 400px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: var(--radius-xl);
  padding: var(--spacing-2xl);
  box-shadow: var(--shadow-xl);
}

[data-theme="dark"] .auth-card {
  background: rgba(30, 41, 59, 0.95);
}

.auth-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.auth-header h1 {
  font-size: var(--text-2xl);
  color: var(--color-text);
  margin-bottom: var(--spacing-sm);
}

.auth-header p {
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.auth-form {
  margin-bottom: var(--spacing-md);
}

.auth-form :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
}

.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: var(--radius-md);
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  background: var(--color-gradient);
  color: white;
  border: none;
  cursor: pointer;
  transition: all var(--transition-base);
}

.submit-btn:hover:not(.loading) {
  box-shadow: var(--shadow-primary);
  transform: translateY(-1px);
}

.submit-btn.loading {
  opacity: 0.7;
  cursor: not-allowed;
}

.auth-footer {
  text-align: center;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.auth-footer a {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: var(--font-medium);
}

@media (max-width: 480px) {
  .auth-card {
    padding: var(--spacing-xl);
  }
}
</style>
