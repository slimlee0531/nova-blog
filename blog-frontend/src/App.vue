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
  <div v-if="!isAdminRoute" class="app-layout">
    <!-- Header -->
    <header class="header">
      <div class="header-container">
        <router-link to="/" class="logo">
          <div class="logo-mark">N</div>
          <span class="logo-text">NovaBlog</span>
        </router-link>

        <nav class="nav" :class="{ open: mobileMenuOpen }">
          <router-link to="/" class="nav-item" @click="mobileMenuOpen = false">首页</router-link>
          <a class="nav-item" href="https://github.com" target="_blank">GitHub</a>
        </nav>

        <div class="header-actions">
          <ThemeToggle />
          <template v-if="userStore.token">
            <router-link to="/write" class="btn-text">写文章</router-link>
            <el-dropdown trigger="click">
              <button class="avatar-btn">
                <div class="avatar">
                  {{ userStore.userInfo?.username?.charAt(0) || 'U' }}
                </div>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/admin')">
                    管理后台
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="btn-text">登录</router-link>
            <router-link to="/register" class="btn-primary">注册</router-link>
          </template>

          <button class="menu-btn" @click="toggleMobileMenu">
            <span class="menu-icon" :class="{ active: mobileMenuOpen }">
              <span></span>
              <span></span>
            </span>
          </button>
        </div>
      </div>
    </header>

    <!-- Main -->
    <main class="main">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- Footer -->
    <footer class="footer">
      <div class="footer-container">
        <div class="footer-content">
          <div class="footer-brand">
            <div class="logo-mark small">N</div>
            <span>NovaBlog</span>
          </div>
          <p class="footer-copy">© 2026 NovaBlog. Crafted with care.</p>
        </div>
      </div>
    </footer>
  </div>

  <router-view v-else />
</template>

<style scoped>
/* ==================== Layout ==================== */
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-surface);
}

/* ==================== Header ==================== */
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 1px solid var(--color-border-subtle);
}

[data-theme="dark"] .header {
  background: rgba(30, 30, 30, 0.85);
  border-bottom-color: var(--color-border);
}

.header-container {
  max-width: var(--content-max);
  margin: 0 auto;
  height: var(--header-height);
  padding: 0 var(--space-6);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* ==================== Logo ==================== */
.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: var(--color-text);
}

.logo:hover {
  opacity: 0.8;
}

.logo-mark {
  width: 32px;
  height: 32px;
  background: var(--color-text);
  color: var(--color-surface);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
}

.logo-mark.small {
  width: 24px;
  height: 24px;
  font-size: 12px;
  border-radius: 6px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: -0.3px;
}

/* ==================== Navigation ==================== */
.nav {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-item {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary);
  text-decoration: none;
  border-radius: var(--radius-full);
  transition: all var(--duration-fast) var(--ease-out);
}

.nav-item:hover {
  color: var(--color-text);
  background: var(--color-surface-container);
}

.nav-item.router-link-active {
  color: var(--color-primary);
}

/* ==================== Actions ==================== */
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-text {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary);
  text-decoration: none;
  border-radius: var(--radius-full);
  transition: all var(--duration-fast) var(--ease-out);
}

.btn-text:hover {
  color: var(--color-text);
  background: var(--color-surface-container);
}

.btn-primary {
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-on-primary);
  background: var(--color-primary);
  text-decoration: none;
  border-radius: var(--radius-full);
  transition: all var(--duration-fast) var(--ease-out);
}

.btn-primary:hover {
  background: var(--color-primary-hover);
  box-shadow: var(--shadow-md);
}

.avatar-btn {
  padding: 0;
  background: none;
  border: none;
  cursor: pointer;
}

.avatar {
  width: 32px;
  height: 32px;
  background: var(--color-surface-container);
  color: var(--color-text);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  transition: all var(--duration-fast) var(--ease-out);
}

.avatar-btn:hover .avatar {
  background: var(--color-surface-hover);
  box-shadow: 0 0 0 2px var(--color-surface), 0 0 0 4px var(--color-primary);
}

/* ==================== Mobile Menu ==================== */
.menu-btn {
  display: none;
  padding: 8px;
  background: none;
  border: none;
  cursor: pointer;
  border-radius: var(--radius-sm);
}

.menu-btn:hover {
  background: var(--color-surface-container);
}

.menu-icon {
  display: flex;
  flex-direction: column;
  gap: 5px;
  width: 20px;
}

.menu-icon span {
  display: block;
  height: 2px;
  background: var(--color-text);
  border-radius: 1px;
  transition: all var(--duration-normal) var(--ease-out);
  transform-origin: center;
}

.menu-icon.active span:first-child {
  transform: translateY(3.5px) rotate(45deg);
}

.menu-icon.active span:last-child {
  transform: translateY(-3.5px) rotate(-45deg);
}

/* ==================== Main ==================== */
.main {
  flex: 1;
}

/* ==================== Footer ==================== */
.footer {
  border-top: 1px solid var(--color-border-subtle);
  padding: var(--space-8) 0;
  margin-top: var(--space-20);
}

.footer-container {
  max-width: var(--content-max);
  margin: 0 auto;
  padding: 0 var(--space-6);
}

.footer-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
}

.footer-copy {
  font-size: 13px;
  color: var(--color-text-tertiary);
}

/* ==================== Responsive ==================== */
@media (max-width: 768px) {
  .nav {
    display: none;
    position: fixed;
    top: var(--header-height);
    left: 0;
    right: 0;
    background: var(--color-surface);
    border-bottom: 1px solid var(--color-border-subtle);
    padding: var(--space-2);
    flex-direction: column;
    gap: 2px;
    z-index: 99;
    box-shadow: var(--shadow-lg);
  }

  .nav.open {
    display: flex;
  }

  .nav-item {
    width: 100%;
    padding: 12px 16px;
    border-radius: var(--radius-md);
    text-align: center;
  }

  .menu-btn {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .btn-text,
  .btn-primary {
    display: none;
  }

  .footer-content {
    flex-direction: column;
    gap: var(--space-4);
    text-align: center;
  }
}

/* ==================== Page Transition ==================== */
.page-enter-active,
.page-leave-active {
  transition: opacity 200ms ease, transform 200ms ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
