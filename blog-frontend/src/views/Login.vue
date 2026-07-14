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
  <div class="login-page">
    <!-- Left: Branding -->
    <div class="brand-side">
      <div class="brand-content">
        <div class="brand-logo">
          <div class="logo-mark">N</div>
          <span class="logo-text">NovaBlog</span>
        </div>
        <h1 class="brand-title">用文字连接<br>人类与AI</h1>
        <p class="brand-desc">面向AI时代的博客系统，为人类与AI Agent提供结构化内容</p>
        <div class="brand-features">
          <div class="feature">
            <div class="feature-icon">✦</div>
            <div class="feature-text">
              <h4>AI增强</h4>
              <p>智能摘要与标签</p>
            </div>
          </div>
          <div class="feature">
            <div class="feature-icon">⚡</div>
            <div class="feature-text">
              <h4>极速体验</h4>
              <p>毫秒级响应</p>
            </div>
          </div>
          <div class="feature">
            <div class="feature-icon">🔒</div>
            <div class="feature-text">
              <h4>安全可靠</h4>
              <p>企业级数据保护</p>
            </div>
          </div>
        </div>
      </div>
      <div class="brand-footer">
        <p>© 2026 NovaBlog. Crafted with care.</p>
      </div>
    </div>

    <!-- Right: Form -->
    <div class="form-side">
      <div class="form-container">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>登录你的账号继续</p>
        </div>

        <form class="form" @submit.prevent="handleLogin">
          <div class="form-group">
            <label class="form-label">用户名</label>
            <input
              v-model="form.username"
              type="text"
              class="form-input"
              placeholder="请输入用户名"
              autocomplete="username"
            />
          </div>

          <div class="form-group">
            <label class="form-label">密码</label>
            <input
              v-model="form.password"
              type="password"
              class="form-input"
              placeholder="请输入密码"
              autocomplete="current-password"
              @keyup.enter="handleLogin"
            />
          </div>

          <button
            type="submit"
            class="submit-btn"
            :class="{ loading }"
            :disabled="loading"
          >
            <span v-if="!loading">登录</span>
            <span v-else class="loading-text">
              <span class="spinner"></span>
              登录中
            </span>
          </button>
        </form>

        <div class="form-divider">
          <span>或者</span>
        </div>

        <button class="social-btn" @click="router.push('/')">
          <span>先逛逛</span>
        </button>

        <p class="form-footer">
          还没有账号？
          <router-link to="/register">立即注册</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1fr;
  background: var(--color-surface);
}

/* ==================== Brand Side ==================== */
.brand-side {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px 80px;
  position: relative;
  overflow: hidden;
}

.brand-side::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(26, 115, 232, 0.15) 0%, transparent 60%);
  pointer-events: none;
}

.brand-side::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -30%;
  width: 80%;
  height: 80%;
  background: radial-gradient(circle, rgba(138, 180, 248, 0.1) 0%, transparent 60%);
  pointer-events: none;
}

.brand-content {
  position: relative;
  z-index: 1;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 60px;
}

.brand-logo .logo-mark {
  width: 40px;
  height: 40px;
  background: white;
  color: #1a1a2e;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 20px;
}

.brand-logo .logo-text {
  font-size: 22px;
  font-weight: 600;
  letter-spacing: -0.3px;
}

.brand-title {
  font-size: 48px;
  font-weight: 700;
  line-height: 1.15;
  letter-spacing: -1.5px;
  margin: 0 0 20px;
}

.brand-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.6;
  margin: 0 0 60px;
  max-width: 400px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.feature {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.feature-icon {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.feature-text h4 {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 4px;
}

.feature-text p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.brand-footer {
  position: absolute;
  bottom: 40px;
  left: 80px;
  z-index: 1;
}

.brand-footer p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
  margin: 0;
}

/* ==================== Form Side ==================== */
.form-side {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.form-container {
  width: 100%;
  max-width: 400px;
}

.form-header {
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text);
  margin: 0 0 8px;
  letter-spacing: -0.5px;
}

.form-header p {
  font-size: 15px;
  color: var(--color-text-tertiary);
  margin: 0;
}

/* ==================== Form ==================== */
.form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
}

.form-input {
  height: 48px;
  padding: 0 16px;
  font-size: 15px;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  outline: none;
  transition: all var(--duration-fast) var(--ease-out);
}

.form-input::placeholder {
  color: var(--color-text-tertiary);
}

.form-input:hover {
  border-color: var(--color-text-tertiary);
}

.form-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-light);
}

[data-theme="dark"] .form-input {
  background: var(--color-surface-dim);
  border-color: var(--color-border);
}

[data-theme="dark"] .form-input:hover {
  border-color: var(--color-text-tertiary);
}

.submit-btn {
  height: 48px;
  margin-top: 8px;
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-on-primary);
  background: var(--color-primary);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}

.submit-btn:hover:not(.loading) {
  background: var(--color-primary-hover);
  box-shadow: var(--shadow-md);
}

.submit-btn.loading {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-text {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ==================== Divider ==================== */
.form-divider {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 24px 0;
}

.form-divider::before,
.form-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--color-border);
}

.form-divider span {
  font-size: 13px;
  color: var(--color-text-tertiary);
}

/* ==================== Social Button ==================== */
.social-btn {
  width: 100%;
  height: 44px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}

.social-btn:hover {
  background: var(--color-surface-dim);
  border-color: var(--color-text-tertiary);
}

/* ==================== Footer ==================== */
.form-footer {
  text-align: center;
  font-size: 14px;
  color: var(--color-text-tertiary);
  margin: 24px 0 0;
}

.form-footer a {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}

.form-footer a:hover {
  text-decoration: underline;
}

/* ==================== Responsive ==================== */
@media (max-width: 1024px) {
  .brand-side {
    display: none;
  }

  .login-page {
    grid-template-columns: 1fr;
  }

  .form-side {
    padding: 40px 24px;
  }
}

@media (max-width: 480px) {
  .form-side {
    padding: 32px 20px;
    align-items: flex-start;
    padding-top: 80px;
  }

  .form-header h2 {
    font-size: 24px;
  }
}
</style>
