<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { tagApi } from '@/api/tag'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const articles = ref<any[]>([])
const tags = ref<any[]>([])
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

const fetchTags = async () => {
  try {
    const res: any = await tagApi.getTagList()
    if (res.code === 200) {
      tags.value = res.data.slice(0, 20)
    }
  } catch (e) {
    console.error(e)
  }
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  window.scrollTo({ top: 0, behavior: 'smooth' })
  fetchArticles()
}

const getExcerpt = (content: string) => {
  const text = content.replace(/[#*`>\[\]()]/g, '').substring(0, 120)
  return text + (content.length > 120 ? '...' : '')
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return dateStr.substring(0, 10)
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
  fetchTags()
})
</script>

<template>
  <div class="home">
    <!-- Hero区域 -->
    <section class="hero">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <h1 class="hero-title">
          <span class="gradient-text">NovaBlog</span>
        </h1>
        <p class="hero-subtitle">面向AI时代的个人博客 · 为人类与AI Agent提供结构化内容</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round @click="goToWrite" class="action-btn">
            <el-icon><EditPen /></el-icon> 开始创作
          </el-button>
          <el-button size="large" round @click="router.push('/')" class="action-btn secondary">
            <el-icon><Compass /></el-icon> 浏览文章
          </el-button>
        </div>
        <div class="hero-stats">
          <div class="stat">
            <span class="stat-num">{{ total }}</span>
            <span class="stat-label">篇文章</span>
          </div>
          <div class="stat">
            <span class="stat-num">{{ tags.length }}</span>
            <span class="stat-label">个标签</span>
          </div>
          <div class="stat">
            <span class="stat-num">∞</span>
            <span class="stat-label">种可能</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 主内容区 -->
    <section class="content-section">
      <div class="container">
        <div class="content-grid">
          <!-- 左侧文章列表 -->
          <main class="article-list">
            <div class="list-header">
              <h2 class="section-title">
                <span class="title-icon">📝</span> 最新文章
              </h2>
            </div>

            <div v-loading="loading" class="articles-wrapper">
              <TransitionGroup name="list" tag="div" class="articles">
                <article
                  v-for="article in articles"
                  :key="article.id"
                  class="article-card"
                  @click="router.push(`/article/${article.slug}`)"
                >
                  <div class="article-content">
                    <div class="article-meta-top">
                      <span class="category" v-if="article.categoryName">{{ article.categoryName }}</span>
                      <span class="date">{{ formatDate(article.publishedAt) }}</span>
                    </div>
                    <h3 class="article-title">{{ article.title }}</h3>
                    <p class="article-excerpt">{{ article.summary || getExcerpt(article.content) }}</p>
                    <div class="article-footer">
                      <div class="author">
                        <el-avatar :size="24" :src="article.authorAvatar">
                          {{ article.authorName?.charAt(0) || 'A' }}
                        </el-avatar>
                        <span>{{ article.authorName || '匿名' }}</span>
                      </div>
                      <div class="stats">
                        <span><el-icon><View /></el-icon> {{ article.viewCount || 0 }}</span>
                        <span><el-icon><ChatDotRound /></el-icon> {{ article.commentCount || 0 }}</span>
                        <span><el-icon><Star /></el-icon> {{ article.likeCount || 0 }}</span>
                      </div>
                    </div>
                  </div>
                  <div class="article-arrow">
                    <el-icon><ArrowRight /></el-icon>
                  </div>
                </article>
              </TransitionGroup>

              <el-empty v-if="!loading && articles.length === 0" description="暂无文章">
                <el-button type="primary" @click="goToWrite">写第一篇文章</el-button>
              </el-empty>

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
          </main>

          <!-- 右侧边栏 -->
          <aside class="sidebar">
            <!-- 博主信息 -->
            <div class="sidebar-widget author-widget">
              <div class="author-cover"></div>
              <div class="author-info">
                <el-avatar :size="72" :src="userStore.userInfo?.avatarUrl" class="author-avatar">
                  {{ userStore.userInfo?.username?.charAt(0) || 'N' }}
                </el-avatar>
                <h3 class="author-name">{{ userStore.userInfo?.displayName || 'NovaBlog' }}</h3>
                <p class="author-bio">面向AI时代的博客系统</p>
                <div class="author-stats">
                  <div class="author-stat">
                    <span class="num">{{ total }}</span>
                    <span class="label">文章</span>
                  </div>
                  <div class="author-stat">
                    <span class="num">{{ tags.length }}</span>
                    <span class="label">标签</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 热门标签 -->
            <div class="sidebar-widget tags-widget">
              <h3 class="widget-title">
                <span>🏷️</span> 热门标签
              </h3>
              <div class="tags-cloud">
                <span
                  v-for="tag in tags"
                  :key="tag.id"
                  class="tag-item"
                >
                  {{ tag.name }}
                </span>
              </div>
            </div>

            <!-- 快捷入口 -->
            <div class="sidebar-widget links-widget">
              <h3 class="widget-title">
                <span>🔗</span> 快捷入口
              </h3>
              <div class="links">
                <a class="link-item" @click="router.push('/')">
                  <span class="link-icon">🏠</span>
                  <span>首页</span>
                </a>
                <a class="link-item" @click="goToWrite">
                  <span class="link-icon">✍️</span>
                  <span>写文章</span>
                </a>
                <a class="link-item" v-if="userStore.token" @click="router.push('/admin')">
                  <span class="link-icon">⚙️</span>
                  <span>后台管理</span>
                </a>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* 变量 */
:root {
  --primary: #6366f1;
  --primary-light: #818cf8;
  --gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 全局样式 */
.home {
  min-height: 100vh;
  background: #f8fafc;
}

/* Hero区域 */
.hero {
  position: relative;
  padding: 100px 20px 60px;
  text-align: center;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: 0;
}

.hero-bg::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(circle at 20% 80%, rgba(255,255,255,0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255,255,255,0.15) 0%, transparent 50%);
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 56px;
  font-weight: 800;
  margin-bottom: 16px;
  letter-spacing: -1px;
}

.gradient-text {
  background: linear-gradient(135deg, #fff 0%, #e0e7ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 40px;
  font-weight: 300;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 50px;
}

.action-btn {
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 30px;
  transition: all 0.3s ease;
}

.action-btn:not(.secondary) {
  background: white;
  color: #667eea;
  border: none;
}

.action-btn:not(.secondary):hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.action-btn.secondary {
  background: transparent;
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.5);
}

.action-btn.secondary:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: white;
}

.hero-stats {
  display: flex;
  justify-content: center;
  gap: 60px;
}

.stat {
  text-align: center;
  color: white;
}

.stat-num {
  display: block;
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.8;
}

/* 内容区域 */
.content-section {
  padding: 40px 20px 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 40px;
}

/* 文章列表 */
.list-header {
  margin-bottom: 24px;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
}

.title-icon {
  font-size: 28px;
}

.articles {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.article-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e2e8f0;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
  border-color: #667eea;
}

.article-content {
  flex: 1;
  min-width: 0;
}

.article-meta-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.category {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.date {
  color: #94a3b8;
  font-size: 13px;
}

.article-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 12px 0;
  line-height: 1.4;
  transition: color 0.2s;
}

.article-card:hover .article-title {
  color: #667eea;
}

.article-excerpt {
  color: #64748b;
  font-size: 15px;
  line-height: 1.6;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #475569;
}

.stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #94a3b8;
}

.stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-arrow {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #667eea;
  transition: all 0.3s ease;
}

.article-card:hover .article-arrow {
  background: #667eea;
  color: white;
  transform: translateX(4px);
}

/* 列表动画 */
.list-enter-active,
.list-leave-active {
  transition: all 0.4s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

/* 侧边栏 */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.sidebar-widget {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.widget-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  padding: 20px 24px;
  margin: 0;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 作者卡片 */
.author-cover {
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.author-info {
  padding: 0 24px 24px;
  text-align: center;
  margin-top: -36px;
}

.author-avatar {
  border: 4px solid white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.author-name {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 12px 0 8px;
}

.author-bio {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 16px;
}

.author-stats {
  display: flex;
  justify-content: center;
  gap: 40px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}

.author-stat {
  text-align: center;
}

.author-stat .num {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: #667eea;
}

.author-stat .label {
  font-size: 13px;
  color: #94a3b8;
}

/* 标签云 */
.tags-cloud {
  padding: 20px 24px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  background: #f1f5f9;
  color: #475569;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tag-item:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px);
}

/* 快捷链接 */
.links {
  padding: 16px 24px;
}

.link-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #475569;
  text-decoration: none;
}

.link-item:hover {
  background: #f1f5f9;
  color: #667eea;
  transform: translateX(4px);
}

.link-icon {
  font-size: 18px;
}

/* 分页 */
.pagination {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: #667eea;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .sidebar {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }

  .author-widget {
    grid-column: span 2;
  }
}

@media (max-width: 768px) {
  .hero {
    padding: 60px 20px 40px;
  }

  .hero-title {
    font-size: 36px;
  }

  .hero-subtitle {
    font-size: 15px;
    margin-bottom: 30px;
  }

  .hero-actions {
    flex-direction: column;
    gap: 12px;
  }

  .action-btn {
    width: 100%;
  }

  .hero-stats {
    gap: 30px;
  }

  .stat-num {
    font-size: 28px;
  }

  .content-section {
    padding: 30px 16px 40px;
  }

  .section-title {
    font-size: 20px;
  }

  .article-card {
    padding: 20px;
    flex-direction: column;
    align-items: flex-start;
  }

  .article-arrow {
    display: none;
  }

  .article-title {
    font-size: 18px;
  }

  .article-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .sidebar {
    grid-template-columns: 1fr;
  }

  .author-widget {
    grid-column: span 1;
  }

  .author-stats {
    gap: 30px;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 28px;
  }

  .hero-stats {
    flex-direction: column;
    gap: 16px;
  }

  .article-meta-top {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
