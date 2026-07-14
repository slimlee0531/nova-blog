<script setup lang="ts">
import { useUserStore } from '@/store/user'
import { useRouter, useRoute } from 'vue-router'
import { ref, computed } from 'vue'
import ThemeToggle from '@/components/ThemeToggle.vue'
import {
  Odometer,
  Document,
  Folder,
  Collection,
  ChatDotRound,
  ArrowLeft,
  Fold,
  Expand,
  SwitchButton
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
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

const navItems = [
  { path: '/admin', icon: Odometer, label: '仪表盘', exact: true },
  { path: '/admin/articles', icon: Document, label: '文章管理' },
  { path: '/admin/categories', icon: Folder, label: '分类管理' },
  { path: '/admin/tags', icon: Collection, label: '标签管理' },
  { path: '/admin/comments', icon: ChatDotRound, label: '评论管理' }
]

const isActive = (item: any) => {
  if (item.exact) {
    return route.path === item.path
  }
  return route.path.startsWith(item.path)
}

const pageTitle = computed(() => {
  const item = navItems.find(i => isActive(i))
  return item?.label || '管理后台'
})
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
      <!-- Logo -->
      <div class="sidebar-header">
        <router-link to="/" class="sidebar-logo" v-if="!sidebarCollapsed">
          <div class="logo-icon">
            <span>N</span>
          </div>
          <span class="logo-text">NovaBlog</span>
        </router-link>
        <div class="logo-icon mini" v-else>
          <span>N</span>
        </div>
      </div>

      <!-- 导航 -->
      <nav class="sidebar-nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item) }"
          @click="closeMobileMenu"
        >
          <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
          <span class="nav-text" v-if="!sidebarCollapsed">{{ item.label }}</span>
        </router-link>
      </nav>

      <!-- 用户信息 -->
      <div class="sidebar-user">
        <div class="user-avatar">
          {{ userStore.userInfo?.username?.charAt(0) || 'U' }}
        </div>
        <div class="user-info" v-if="!sidebarCollapsed">
          <div class="user-name">{{ userStore.userInfo?.displayName || userStore.userInfo?.username }}</div>
          <div class="user-role">管理员</div>
        </div>
      </div>

      <!-- 退出按钮 -->
      <button class="logout-btn" @click="handleLogout">
        <el-icon><SwitchButton /></el-icon>
        <span v-if="!sidebarCollapsed">退出登录</span>
      </button>
    </aside>

    <!-- 主内容区 -->
    <div class="admin-main">
      <!-- 顶栏 -->
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
            <el-icon><Fold v-if="!sidebarCollapsed" /><Expand v-else /></el-icon>
          </button>

          <!-- 面包屑 -->
          <div class="breadcrumb">
            <span class="breadcrumb-item">后台</span>
            <span class="breadcrumb-separator">/</span>
            <span class="breadcrumb-item current">{{ pageTitle }}</span>
          </div>
        </div>

        <div class="header-right">
          <ThemeToggle />
          <button class="back-btn" @click="router.push('/')">
            <el-icon><ArrowLeft /></el-icon>
            <span>返回前台</span>
          </button>
        </div>
      </header>

      <!-- 内容区 -->
      <main class="admin-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--color-bg, #f5f5f7);
}

/* ==================== 侧边栏 ==================== */
.admin-sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid #e5e5e5;
  display: flex;
  flex-direction: column;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
}

[data-theme="dark"] .admin-sidebar {
  background: #1a1a1a;
  border-right-color: #333;
}

.admin-sidebar.collapsed {
  width: 72px;
}

/* Logo */
.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #e5e5e5;
  padding: 0 20px;
}

[data-theme="dark"] .sidebar-header {
  border-bottom-color: #333;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
}

.logo-icon.mini {
  width: 32px;
  height: 32px;
  font-size: 14px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: -0.5px;
}

[data-theme="dark"] .logo-text {
  color: #fff;
}

/* 导航 */
.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 10px;
  color: #666;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  width: 100%;
}

[data-theme="dark"] .nav-item {
  color: #999;
}

.nav-item:hover {
  background: #f5f5f5;
  color: #1a1a1a;
}

[data-theme="dark"] .nav-item:hover {
  background: #2a2a2a;
  color: #fff;
}

.nav-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.nav-icon {
  font-size: 20px;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

/* 用户信息 */
.sidebar-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  margin: 0 12px;
  background: #f5f5f5;
  border-radius: 12px;
}

[data-theme="dark"] .sidebar-user {
  background: #2a2a2a;
}

.user-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 16px;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

[data-theme="dark"] .user-name {
  color: #fff;
}

.user-role {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

/* 退出按钮 */
.logout-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 12px;
  padding: 12px 16px;
  background: none;
  border: 1px solid #e5e5e5;
  border-radius: 10px;
  color: #666;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  width: calc(100% - 24px);
}

[data-theme="dark"] .logout-btn {
  border-color: #333;
  color: #999;
}

.logout-btn:hover {
  background: #fef2f2;
  border-color: #fecaca;
  color: #dc2626;
}

[data-theme="dark"] .logout-btn:hover {
  background: rgba(220, 38, 38, 0.1);
  border-color: rgba(220, 38, 38, 0.3);
  color: #ef4444;
}

/* ==================== 主内容区 ==================== */
.admin-main {
  flex: 1;
  margin-left: 260px;
  transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.admin-layout.collapsed .admin-main {
  margin-left: 72px;
}

/* ==================== Header ==================== */
.admin-header {
  height: 64px;
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 50;
}

[data-theme="dark"] .admin-header {
  background: #1a1a1a;
  border-bottom-color: #333;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #f5f5f5;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 18px;
}

[data-theme="dark"] .collapse-btn {
  background: #2a2a2a;
  color: #999;
}

.collapse-btn:hover {
  background: #e5e5e5;
  color: #1a1a1a;
}

[data-theme="dark"] .collapse-btn:hover {
  background: #333;
  color: #fff;
}

/* 面包屑 */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.breadcrumb-item {
  color: #999;
}

.breadcrumb-item.current {
  color: #1a1a1a;
  font-weight: 500;
}

[data-theme="dark"] .breadcrumb-item {
  color: #666;
}

[data-theme="dark"] .breadcrumb-item.current {
  color: #fff;
}

.breadcrumb-separator {
  color: #ccc;
}

[data-theme="dark"] .breadcrumb-separator {
  color: #444;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid #e5e5e5;
  background: #fff;
  color: #666;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

[data-theme="dark"] .back-btn {
  border-color: #333;
  color: #999;
  background: #1a1a1a;
}

.back-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

/* ==================== 内容 ==================== */
.admin-content {
  flex: 1;
  padding: 24px;
}

/* ==================== 移动端 ==================== */
.mobile-menu-btn {
  display: none;
  padding: 8px;
  background: none;
  border: none;
  cursor: pointer;
}

.hamburger {
  display: flex;
  flex-direction: column;
  gap: 5px;
  width: 20px;
}

.hamburger span {
  display: block;
  height: 2px;
  background: #1a1a1a;
  border-radius: 1px;
  transition: all 0.3s ease;
}

[data-theme="dark"] .hamburger span {
  background: #fff;
}

.hamburger.active span:nth-child(1) {
  transform: translateY(7px) rotate(45deg);
}

.hamburger.active span:nth-child(2) {
  opacity: 0;
}

.hamburger.active span:nth-child(3) {
  transform: translateY(-7px) rotate(-45deg);
}

.sidebar-overlay {
  display: none;
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 90;
}

/* ==================== 动画 ==================== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ==================== 响应式 ==================== */
@media (max-width: 768px) {
  .admin-sidebar {
    transform: translateX(-100%);
    z-index: 100;
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

  .breadcrumb {
    display: none;
  }

  .sidebar-overlay.visible {
    display: block;
  }

  .admin-content {
    padding: 16px;
  }
}
</style>
