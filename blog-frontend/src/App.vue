<script setup lang="ts">
import { useUserStore } from '@/store/user'
import { onMounted, ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import ThemeToggle from '@/components/ThemeToggle.vue'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const mobileMenuOpen = ref(false)

const isAdminRoute = computed(() => route.path.startsWith('/admin'))

onMounted(() => {
  if (userStore.token) {
    userStore.fetchUserInfo()
  }
})

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}

const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value
}
</script>

<template>
  <!-- 前台布局 -->
  <div v-if="!isAdminRoute" class="app-layout">
    <header class="app-header">
      <div class="header-inner">
        <div class="header-left">
          <router-link to="/" class="logo">
            <span class="logo-icon">✦</span>
            <span class="logo-text">NovaBlog</span>
          </router-link>
        </div>

        <nav class="header-nav" :class="{ open: mobileMenuOpen }">
          <router-link to="/" class="nav-link" @click="mobileMenuOpen = false">首页</router-link>
          <a class="nav-link" href="https://github.com" target="_blank">GitHub</a>
        </nav>

        <div class="header-right">
          <ThemeToggle />
          <template v-if="userStore.token">
            <router-link to="/write" class="nav-link write-link">
              <span>✍️</span> 写文章
            </router-link>
            <el-dropdown trigger="click">
              <button class="user-avatar-btn">
                <el-avatar :size="32">
                  {{ userStore.userInfo?.username?.charAt(0) || 'U' }}
                </el-avatar>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/admin')">
                    ⚙️ 后台管理
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    🚪 退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="nav-link">登录</router-link>
            <router-link to="/register" class="register-btn">注册</router-link>
          </template>

          <!-- 移动端菜单按钮 -->
          <button class="mobile-menu-btn" @click="toggleMobileMenu">
            <span class="hamburger" :class="{ active: mobileMenuOpen }">
              <span></span>
              <span></span>
              <span></span>
            </span>
          </button>
        </div>
      </div>
    </header>

    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <footer class="app-footer">
      <div class="footer-inner">
        <p>© 2026 NovaBlog · 面向AI时代的博客系统</p>
      </div>
    </footer>
  </div>

  <!-- 后台布局 -->
  <router-view v-else />
</template>

<style scoped>
/* ==================== 前台布局 ==================== */
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* ==================== Header (Premium Style) ==================== */
.app-header {
  position: sticky;
  top: 0;
  z-index: var(--z-header);
  background: rgba(251, 251, 253, 0.72);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

[data-theme="dark"] .app-header {
  background: rgba(0, 0, 0, 0.72);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.header-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  height: var(--header-height);
  padding: 0 var(--spacing-lg);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--color-text);
  font-weight: var(--font-semibold);
  font-size: 18px;
  letter-spacing: -0.4px;
  transition: opacity var(--transition-fast);
}

.logo:hover {
  opacity: 0.7;
}

.logo-icon {
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  font-weight: bold;
}

.logo-text {
  letter-spacing: -0.5px;
}

/* ==================== 导航 ==================== */
.header-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  color: var(--color-text-secondary);
  text-decoration: none;
  font-size: 14px;
  font-weight: var(--font-medium);
  border-radius: var(--radius-full);
  transition: all var(--transition-fast);
}

.nav-link:hover {
  color: var(--color-text);
  background: rgba(0, 0, 0, 0.04);
}

[data-theme="dark"] .nav-link:hover {
  background: rgba(255, 255, 255, 0.06);
}

.write-link {
  color: var(--color-primary);
}

.write-link:hover {
  background: var(--color-primary-bg);
}

.register-btn {
  display: inline-flex;
  align-items: center;
  padding: 8px 20px;
  background: var(--color-primary);
  color: var(--color-text-inverse);
  font-size: 14px;
  font-weight: var(--font-medium);
  border-radius: var(--radius-full);
  text-decoration: none;
  transition: all var(--transition-fast);
  box-shadow: 0 1px 3px rgba(10, 132, 255, 0.3);
}

.register-btn:hover {
  background: var(--color-primary-dark);
  box-shadow: 0 4px 12px rgba(10, 132, 255, 0.4);
  transform: translateY(-1px);
}

/* ==================== Header 右侧 ==================== */
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar-btn {
  padding: 0;
  border-radius: var(--radius-full);
  overflow: hidden;
  transition: all var(--transition-fast);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.user-avatar-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

/* ==================== 移动端菜单 ==================== */
.mobile-menu-btn {
  display: none;
  padding: 8px;
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}

.mobile-menu-btn:hover {
  background: rgba(0, 0, 0, 0.04);
}

.hamburger {
  display: flex;
  flex-direction: column;
  gap: 5px;
  width: 18px;
}

.hamburger span {
  display: block;
  height: 1.5px;
  background: var(--color-text);
  border-radius: 1px;
  transition: all var(--transition-base);
}

.hamburger.active span:nth-child(1) {
  transform: translateY(6.5px) rotate(45deg);
}

.hamburger.active span:nth-child(2) {
  opacity: 0;
}

.hamburger.active span:nth-child(3) {
  transform: translateY(-6.5px) rotate(-45deg);
}

/* ==================== Main ==================== */
.app-main {
  flex: 1;
}

/* ==================== Footer (Premium Style) ==================== */
.app-footer {
  background: var(--color-bg-subtle);
  border-top: 1px solid var(--color-border-light);
  padding: var(--spacing-2xl) 0;
  margin-top: var(--spacing-4xl);
}

.footer-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
  text-align: center;
  color: var(--color-text-muted);
  font-size: 13px;
  letter-spacing: 0.2px;
}

/* ==================== 响应式 ==================== */
@media (max-width: 768px) {
  .header-nav {
    display: none;
    position: fixed;
    top: var(--header-height);
    left: 0;
    right: 0;
    background: rgba(251, 251, 253, 0.95);
    backdrop-filter: saturate(180%) blur(20px);
    -webkit-backdrop-filter: saturate(180%) blur(20px);
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
    padding: var(--spacing-sm);
    flex-direction: column;
    gap: 4px;
    z-index: calc(var(--z-header) - 1);
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
  }

  [data-theme="dark"] .header-nav {
    background: rgba(0, 0, 0, 0.95);
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.4);
  }

  .header-nav.open {
    display: flex;
  }

  .header-nav .nav-link {
    width: 100%;
    padding: 14px;
    justify-content: center;
    border-radius: var(--radius-md);
  }

  .mobile-menu-btn {
    display: flex;
  }

  .write-link {
    display: none;
  }

  .register-btn {
    display: none;
  }

  .header-inner {
    padding: 0 var(--spacing-md);
  }
}
</style>
