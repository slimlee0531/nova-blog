<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  username: '',
  password: ''
})
const loading = ref(false)

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const success = await userStore.login(form.value)
    if (success) {
      ElMessage.success('登录成功')
      router.push('/')
    }
  } catch (e) {
    ElMessage.error('登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <h1>欢迎回来</h1>
        <p>登录你的账号继续</p>
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
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <button
          class="submit-btn"
          :class="{ loading }"
          :disabled="loading"
          @click="handleLogin"
        >
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </el-form>

      <div class="auth-footer">
        还没有账号？
        <router-link to="/register">立即注册</router-link>
      </div>

      <div class="auth-divider">
        <span>或者</span>
      </div>

      <button class="guest-btn" @click="router.push('/')">
        先逛逛
      </button>
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

.auth-divider {
  display: flex;
  align-items: center;
  margin: var(--spacing-lg) 0;
  color: var(--color-text-muted);
  font-size: var(--text-xs);
}

.auth-divider::before,
.auth-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--color-border);
}

.auth-divider span {
  padding: 0 var(--spacing-md);
}

.guest-btn {
  width: 100%;
  height: 44px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.guest-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

@media (max-width: 480px) {
  .auth-card {
    padding: var(--spacing-xl);
  }
}
</style>
