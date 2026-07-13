<script setup lang="ts">
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'
import { ref } from 'vue'
import ThemeToggle from '@/components/ThemeToggle.vue'

const userStore = useUserStore()
const router = useRouter()
const sidebarCollapsed = ref(false)
const mobileMenuOpen = ref(false)

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const closeMobileMenu = () => {
  mobileMenuOpen.value = false
}
</script>

<template>
  <div class="admin-layout" :class="{ collapsed: sidebarCollapsed }">
    <!-- 移动端遮罩 -->
    <div
      class="sidebar-overlay"
      :class="{ visible: mobileMenuOpen }"
      @click="closeMobileMenu"
    ></div>

    <!-- 侧边栏 -->
    <aside class="admin-sidebar" :class="{ collapsed: sidebarCollapsed, open: mobileMenuOpen }">
      <div class="sidebar-header">
        <router-link to="/" class="sidebar-logo" v-if="!sidebarCollapsed">
          <span class="logo-icon">✦</span>
          <span>NovaBlog</span>
        </router-link>
        <span class="logo-icon" v-else>✦</span>
      </div>

      <nav class="sidebar-nav">
        <router-link
          to="/admin"
          class="nav-item"
          :class="{ active: $route.path === '/admin' }"
          @click="closeMobileMenu"
        >
          <span class="nav-icon">📊</span>
          <span class="nav-text" v-if="!sidebarCollapsed">仪表盘</span>
        </router-link>
        <router-link
          to="/admin/articles"
          class="nav-item"
          :class="{ active: $route.path === '/admin/articles' }"
          @click="closeMobileMenu"
        >
          <span class="nav-icon">📝</span>
          <span class="nav-text" v-if="!sidebarCollapsed">文章管理</span>
        </router-link>
        <router-link
          to="/admin/categories"
          class="nav-item"
          :class="{ active: $route.path === '/admin/categories' }"
          @click="closeMobileMenu"
        >
          <span class="nav-icon">📁</span>
          <span class="nav-text" v-if="!sidebarCollapsed">分类管理</span>
        </router-link>
        <router-link
          to="/admin/tags"
          class="nav-item"
          :class="{ active: $route.path === '/admin/tags' }"
          @click="closeMobileMenu"
        >
          <span class="nav-icon">🏷️</span>
          <span class="nav-text" v-if="!sidebarCollapsed">标签管理</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <button class="nav-item" @click="handleLogout">
          <span class="nav-icon">🚪</span>
          <span class="nav-text" v-if="!sidebarCollapsed">退出登录</span>
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="admin-main">
      <header class="admin-header">
        <div class="header-left">
          <!-- 移动端菜单按钮 -->
          <button class="mobile-menu-btn" @click="mobileMenuOpen = !mobileMenuOpen">
            <span class="hamburger" :class="{ active: mobileMenuOpen }">
              <span></span>
              <span></span>
              <span></span>
            </span>
          </button>

          <!-- 折叠按钮 -->
          <button class="collapse-btn" @click="toggleSidebar">
            {{ sidebarCollapsed ? '→' : '←' }}
          </button>

          <span class="header-title">管理后台</span>
        </div>

        <div class="header-right">
          <ThemeToggle />
          <span class="user-name">{{ userStore.userInfo?.displayName || userStore.userInfo?.username }}</span>
          <button class="back-btn" @click="router.push('/')">返回前台</button>
        </div>
      </header>

      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--color-bg);
}

/* ==================== 侧边栏 ==================== */
.admin-sidebar {
  width: var(--admin-sidebar-width);
  background: var(--color-bg-card);
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  transition: width var(--transition-base);
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: var(--z-header);
}

.admin-sidebar.collapsed {
  width: var(--admin-sidebar-collapsed-width);
}

.sidebar-header {
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--color-divider);
  padding: 0 var(--spacing-md);
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  text-decoration: none;
  color: var(--color-text);
  font-weight: var(--font-bold);
  font-size: var(--text-lg);
}

.logo-icon {
  font-size: 20px;
  background: var(--color-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* ==================== 导航 ==================== */
.sidebar-nav {
  flex: 1;
  padding: var(--spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  text-decoration: none;
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  transition: all var(--transition-fast);
  width: 100%;
  text-align: left;
}

.nav-item:hover {
  background: var(--color-bg-subtle);
  color: var(--color-primary);
}

.nav-item.active {
  background: var(--color-primary-bg);
  color: var(--color-primary);
}

.nav-icon {
  font-size: var(--text-lg);
  width: 24px;
  text-align: center;
  flex-shrink: 0;
}

.sidebar-footer {
  padding: var(--spacing-md);
  border-top: 1px solid var(--color-divider);
}

/* ==================== 主内容区 ==================== */
.admin-main {
  flex: 1;
  margin-left: var(--admin-sidebar-width);
  transition: margin-left var(--transition-base);
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.admin-layout.collapsed .admin-main {
  margin-left: var(--admin-sidebar-collapsed-width);
}

/* ==================== Header ==================== */
.admin-header {
  height: var(--header-height);
  background: var(--color-bg-card);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-lg);
  position: sticky;
  top: 0;
  z-index: calc(var(--z-header) - 1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.header-title {
  font-weight: var(--font-semibold);
  color: var(--color-text);
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.user-name {
  color: var(--color-text-secondary);
  font-size: var(--text-sm);
}

.back-btn {
  padding: var(--spacing-xs) var(--spacing-md);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.back-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.collapse-btn {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  background: var(--color-bg-subtle);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.collapse-btn:hover {
  background: var(--color-border);
  color: var(--color-text);
}

/* ==================== 内容 ==================== */
.admin-content {
  flex: 1;
  padding: var(--spacing-lg);
}

/* ==================== 移动端 ==================== */
.mobile-menu-btn {
  display: none;
  padding: var(--spacing-sm);
}

.hamburger {
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 18px;
}

.hamburger span {
  display: block;
  height: 2px;
  background: var(--color-text);
  border-radius: 1px;
  transition: all var(--transition-base);
}

.hamburger.active span:nth-child(1) {
  transform: translateY(6px) rotate(45deg);
}

.hamburger.active span:nth-child(2) {
  opacity: 0;
}

.hamburger.active span:nth-child(3) {
  transform: translateY(-6px) rotate(-45deg);
}

.sidebar-overlay {
  display: none;
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: calc(var(--z-header) - 1);
}

/* ==================== 响应式 ==================== */
@media (max-width: 768px) {
  .admin-sidebar {
    transform: translateX(-100%);
    z-index: var(--z-header);
  }

  .admin-sidebar.open {
    transform: translateX(0);
  }

  .admin-main {
    margin-left: 0;
  }

  .admin-layout.collapsed .admin-main {
    margin-left: 0;
  }

  .mobile-menu-btn {
    display: flex;
  }

  .collapse-btn {
    display: none;
  }

  .sidebar-overlay.visible {
    display: block;
  }

  .admin-content {
    padding: var(--spacing-md);
  }
}
</style>
