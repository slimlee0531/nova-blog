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
          <router-link to="/admin" class="nav-link">后台管理</router-link>
          <el-button type="text" @click="handleLogout">退出</el-button>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-link">登录</router-link>
          <router-link to="/register" class="nav-link">注册</router-link>
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
  background-color: #409eff;
  color: white;
  padding: 0 20px;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: white;
  text-decoration: none;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-link {
  color: white;
  text-decoration: none;
}

.nav-link:hover {
  text-decoration: underline;
}

.el-main {
  min-height: calc(100vh - 60px);
  background-color: #f5f7fa;
}
</style>
