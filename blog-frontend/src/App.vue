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
          <div class="logo-icon">
            <span>N</span>
          </div>
          <span class="logo-text">NovaBlog</span>
        </router-link>

        <nav class="nav" :class="{ open: mobileMenuOpen }">
          <router-link to="/" class="nav-item" @click="mobileMenuOpen = false">首页</router-link>
          <router-link v-if="userStore.token" to="/bookmarks" class="nav-item" @click="mobileMenuOpen = false">收藏</router-link>
          <a class="nav-item" href="https://github.com/slimlee0531/novaBlog" target="_blank">GitHub</a>
        </nav>

        <div class="header-actions">
          <ThemeToggle />
          <template v-if="userStore.token">
            <router-link to="/write" class="write-btn">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M11 1L15 5L6 14H2V10L11 1Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span>写文章</span>
            </router-link>
            <el-dropdown trigger="click">
              <button class="avatar-btn">
                <div class="avatar">
                  {{ userStore.userInfo?.username?.charAt(0) || 'U' }}
                </div>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/bookmarks')">
                    ⭐ 我的收藏
                  </el-dropdown-item>
                  <el-dropdown-item @click="router.push('/admin')">
                    ⚙️ 管理后台
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    🚪 退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="login-btn">登录</router-link>
            <router-link to="/register" class="register-btn">注册</router-link>
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
            <div class="footer-logo">
              <span>N</span>
            </div>
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
  background: #fff;
}

[data-theme="dark"] .app-layout {
  background: #111;
}

/* ==================== Header ==================== */
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 1px solid #f0f0f0;
}

[data-theme="dark"] .header {
  background: rgba(17, 17, 17, 0.9);
  border-bottom-color: #222;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  height: 72px;
  padding: 0 24px;
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
  color: #1a1a1a;
}

[data-theme="dark"] .logo {
  color: #fff;
}

.logo:hover {
  opacity: 0.8;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 18px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

/* ==================== Navigation ==================== */
.nav {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-item {
  padding: 10px 18px;
  font-size: 15px;
  font-weight: 500;
  color: #666;
  text-decoration: none;
  border-radius: 10px;
  transition: all 0.2s ease;
}

[data-theme="dark"] .nav-item {
  color: #999;
}

.nav-item:hover {
  color: #1a1a1a;
  background: #f5f5f5;
}

[data-theme="dark"] .nav-item:hover {
  color: #fff;
  background: #222;
}

.nav-item.router-link-active {
  color: #667eea;
}

/* ==================== Actions ==================== */
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.write-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  text-decoration: none;
  border-radius: 10px;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.write-btn:hover {
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transform: translateY(-1px);
}

.login-btn {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  text-decoration: none;
  border-radius: 10px;
  transition: all 0.2s ease;
}

[data-theme="dark"] .login-btn {
  color: #fff;
}

.login-btn:hover {
  background: #f5f5f5;
}

[data-theme="dark"] .login-btn:hover {
  background: #222;
}

.register-btn {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  background: #1a1a1a;
  text-decoration: none;
  border-radius: 10px;
  transition: all 0.2s ease;
}

[data-theme="dark"] .register-btn {
  background: #fff;
  color: #111;
}

.register-btn:hover {
  opacity: 0.9;
}

.avatar-btn {
  padding: 0;
  background: none;
  border: none;
  cursor: pointer;
}

.avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  transition: all 0.2s ease;
}

.avatar-btn:hover .avatar {
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

/* ==================== Mobile Menu ==================== */
.menu-btn {
  display: none;
  padding: 8px;
  background: none;
  border: none;
  cursor: pointer;
  border-radius: 8px;
}

.menu-btn:hover {
  background: #f5f5f5;
}

[data-theme="dark"] .menu-btn:hover {
  background: #222;
}

.menu-icon {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 22px;
}

.menu-icon span {
  display: block;
  height: 2px;
  background: #1a1a1a;
  border-radius: 1px;
  transition: all 0.3s ease;
}

[data-theme="dark"] .menu-icon span {
  background: #fff;
}

.menu-icon.active span:first-child {
  transform: translateY(4px) rotate(45deg);
}

.menu-icon.active span:last-child {
  transform: translateY(-4px) rotate(-45deg);
}

/* ==================== Main ==================== */
.main {
  flex: 1;
}

/* ==================== Footer ==================== */
.footer {
  border-top: 1px solid #f0f0f0;
  padding: 40px 0;
  margin-top: auto;
}

[data-theme="dark"] .footer {
  border-color: #222;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.footer-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

[data-theme="dark"] .footer-brand {
  color: #fff;
}

.footer-logo {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 14px;
}

.footer-copy {
  font-size: 14px;
  color: #999;
}

/* ==================== Responsive ==================== */
@media (max-width: 768px) {
  .nav {
    display: none;
    position: fixed;
    top: 72px;
    left: 0;
    right: 0;
    background: #fff;
    border-bottom: 1px solid #f0f0f0;
    padding: 16px;
    flex-direction: column;
    gap: 4px;
    z-index: 99;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }

  [data-theme="dark"] .nav {
    background: #111;
    border-color: #222;
  }

  .nav.open {
    display: flex;
  }

  .nav-item {
    width: 100%;
    padding: 14px 16px;
    border-radius: 10px;
    text-align: center;
  }

  .menu-btn {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .write-btn,
  .login-btn,
  .register-btn {
    display: none;
  }

  .footer-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
}

/* ==================== Page Transition ==================== */
.page-enter-active,
.page-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
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
