<script setup lang="ts">
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}
</script>

<template>
  <el-container class="admin-container">
    <el-aside width="200px" class="admin-aside">
      <div class="admin-logo">NovaBlog 管理后台</div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/admin">
          <el-icon><HomeFilled /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/articles">
          <el-icon><Document /></el-icon>
          <span>文章管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><Folder /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/tags">
          <el-icon><PriceTag /></el-icon>
          <span>标签管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <div class="header-right">
          <span class="user-name">{{ userStore.userInfo?.displayName || userStore.userInfo?.username }}</span>
          <el-button type="text" @click="handleLogout">退出</el-button>
          <el-button type="text" @click="router.push('/')">返回前台</el-button>
        </div>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-container {
  min-height: 100vh;
}

.admin-aside {
  background-color: #304156;
}

.admin-logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: white;
  font-size: 16px;
  font-weight: bold;
}

.admin-header {
  background-color: #fff;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-name {
  color: #333;
}

.admin-main {
  background-color: #f5f7fa;
}
</style>
