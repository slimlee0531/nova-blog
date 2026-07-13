<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const articles = ref<any[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const fetchArticles = async () => {
  loading.value = true
  try {
    const res: any = await articleApi.getArticles({
      page: page.value,
      size: pageSize.value
    })
    if (res.code === 200) {
      articles.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  fetchArticles()
}

const getExcerpt = (content: string) => {
  const text = content.replace(/[#*`]/g, '').substring(0, 150)
  return text + (content.length > 150 ? '...' : '')
}

const goToWrite = () => {
  if (userStore.token) {
    router.push('/write')
  } else {
    router.push('/login')
  }
}

onMounted(() => {
  fetchArticles()
})
</script>

<template>
  <div class="home-container">
    <!-- 顶部Banner -->
    <div class="banner">
      <div class="banner-content">
        <h1>NovaBlog</h1>
        <p>面向AI时代的个人博客</p>
        <el-button type="primary" size="large" @click="goToWrite">
          <el-icon><Edit /></el-icon> 写文章
        </el-button>
      </div>
    </div>

    <div class="main-content">
      <!-- 左侧文章列表 -->
      <div class="article-section">
        <h2 class="section-title">
          <el-icon><Document /></el-icon> 最新文章
        </h2>
        <div v-loading="loading">
          <div v-for="article in articles" :key="article.id" class="article-card" @click="router.push(`/article/${article.slug}`)">
            <div class="article-header">
              <h3 class="article-title">{{ article.title }}</h3>
              <el-tag v-if="article.categoryName" size="small" type="info">{{ article.categoryName }}</el-tag>
            </div>
            <div class="article-excerpt">
              {{ article.summary || getExcerpt(article.content) }}
            </div>
            <div class="article-footer">
              <div class="article-meta">
                <span class="meta-item">
                  <el-icon><User /></el-icon> {{ article.authorName || '匿名' }}
                </span>
                <span class="meta-item">
                  <el-icon><Clock /></el-icon> {{ article.publishedAt?.substring(0, 10) }}
                </span>
              </div>
              <div class="article-stats">
                <span class="stat-item">
                  <el-icon><View /></el-icon> {{ article.viewCount || 0 }}
                </span>
                <span class="stat-item">
                  <el-icon><ChatDotRound /></el-icon> {{ article.commentCount || 0 }}
                </span>
                <span class="stat-item">
                  <el-icon><Star /></el-icon> {{ article.likeCount || 0 }}
                </span>
              </div>
            </div>
          </div>

          <el-empty v-if="!loading && articles.length === 0" description="暂无文章，快来写第一篇吧！" />

          <div class="pagination" v-if="total > pageSize">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="total"
              :page-size="pageSize"
              :current-page="page"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </div>

      <!-- 右侧边栏 -->
      <aside class="sidebar">
        <!-- 用户信息 -->
        <el-card class="sidebar-card">
          <template #header>
            <span class="card-title">关于博主</span>
          </template>
          <div class="user-info">
            <el-avatar :size="60" :src="userStore.userInfo?.avatarUrl">
              {{ userStore.userInfo?.username?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="user-detail">
              <div class="user-name">{{ userStore.userInfo?.displayName || 'NovaBlog' }}</div>
              <div class="user-bio">面向AI时代的博客系统</div>
            </div>
          </div>
        </el-card>

        <!-- 热门标签 -->
        <el-card class="sidebar-card">
          <template #header>
            <span class="card-title">热门标签</span>
          </template>
          <div class="tag-cloud">
            <el-tag v-for="i in 5" :key="i" class="tag-item" type="info">标签{{ i }}</el-tag>
          </div>
        </el-card>

        <!-- 快捷入口 -->
        <el-card class="sidebar-card">
          <template #header>
            <span class="card-title">快捷入口</span>
          </template>
          <div class="quick-links">
            <el-button text @click="router.push('/')">
              <el-icon><HomeFilled /></el-icon> 首页
            </el-button>
            <el-button text @click="goToWrite">
              <el-icon><Edit /></el-icon> 写文章
            </el-button>
            <el-button text v-if="userStore.token" @click="router.push('/admin')">
              <el-icon><Setting /></el-icon> 后台管理
            </el-button>
          </div>
        </el-card>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  min-height: calc(100vh - 60px);
}

/* Banner */
.banner {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  padding: 60px 20px;
  text-align: center;
}

.banner-content h1 {
  font-size: 42px;
  margin-bottom: 10px;
}

.banner-content p {
  font-size: 18px;
  margin-bottom: 30px;
  opacity: 0.9;
}

/* 主内容区 */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
  display: flex;
  gap: 30px;
}

/* 文章区域 */
.article-section {
  flex: 1;
  min-width: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
}

/* 文章卡片 */
.article-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #eee;
}

.article-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.article-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
  flex: 1;
}

.article-excerpt {
  color: #666;
  line-height: 1.6;
  margin-bottom: 16px;
}

.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.article-meta {
  display: flex;
  gap: 16px;
  color: #999;
  font-size: 13px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-stats {
  display: flex;
  gap: 12px;
  color: #999;
  font-size: 13px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 侧边栏 */
.sidebar {
  width: 300px;
  flex-shrink: 0;
}

.sidebar-card {
  margin-bottom: 20px;
}

.card-title {
  font-weight: 600;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-detail {
  flex: 1;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.user-bio {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  cursor: pointer;
}

.quick-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quick-links .el-button {
  justify-content: flex-start;
}

/* 分页 */
.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .banner {
    padding: 40px 20px;
  }

  .banner-content h1 {
    font-size: 28px;
  }

  .banner-content p {
    font-size: 14px;
  }

  .main-content {
    flex-direction: column;
    padding: 20px 15px;
  }

  .sidebar {
    width: 100%;
  }

  .article-header {
    flex-direction: column;
    gap: 8px;
  }

  .article-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
