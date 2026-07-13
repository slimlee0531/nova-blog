<script setup lang="ts">
import { useUserStore } from '@/store/user'
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

onMounted(() => {
  if (userStore.token) {
    userStore.fetchUserInfo()
  }
})

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}
</script>

<template>
  <el-container>
    <el-header class="header">
      <div class="header-left">
        <router-link to="/" class="logo">NovaBlog</router-link>
      </div>
      <div class="header-right">
        <template v-if="userStore.token">
          <el-button type="primary" size="small" @click="router.push('/write')">
            <el-icon><Edit /></el-icon> 写文章
          </el-button>
          <router-link to="/admin" class="nav-link">后台管理</router-link>
          <el-dropdown>
            <span class="user-dropdown">
              <el-avatar :size="32">
                {{ userStore.userInfo?.username?.charAt(0) || 'U' }}
              </el-avatar>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/admin')">后台管理</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" plain size="small" @click="router.push('/login')">登录</el-button>
          <el-button size="small" @click="router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: white;
  border-bottom: 1px solid #eee;
  padding: 0 30px;
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  font-size: 22px;
  font-weight: bold;
  color: #409eff;
  text-decoration: none;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.nav-link {
  color: #333;
  text-decoration: none;
  font-size: 14px;
}

.nav-link:hover {
  color: #409eff;
}

.user-dropdown {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.el-main {
  min-height: calc(100vh - 60px);
  background-color: #f5f7fa;
  padding: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .header {
    padding: 0 15px;
  }

  .header-right {
    gap: 10px;
  }
}
</style>
