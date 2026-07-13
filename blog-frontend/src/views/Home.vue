<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { tagApi } from '@/api/tag'
import { useUserStore } from '@/store/user'
import { formatDate, truncate } from '@/utils/format'
import type { Article, Tag } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const articles = ref<Article[]>([])
const tags = ref<Tag[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const fetchArticles = async () => {
  loading.value = true
  try {
    const res = await articleApi.getArticles({
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
    const res = await tagApi.getTagList()
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
          <span class="text-gradient">NovaBlog</span>
        </h1>
        <p class="hero-subtitle">面向AI时代的个人博客 · 为人类与AI Agent提供结构化内容</p>
        <div class="hero-actions">
          <button class="hero-btn primary" @click="goToWrite">
            <span>✍️</span> 开始创作
          </button>
          <button class="hero-btn secondary" @click="router.push('/')">
            <span>🧭</span> 浏览文章
          </button>
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
    <section class="content-section container">
      <div class="content-grid">
        <!-- 左侧文章列表 -->
        <main class="article-list">
          <div class="list-header">
            <h2 class="section-title">
              <span>📝</span> 最新文章
            </h2>
          </div>

          <div v-loading="loading" class="articles-wrapper">
            <TransitionGroup name="list" tag="div" class="articles">
              <article
                v-for="article in articles"
                :key="article.id"
                class="article-card card-hover"
                @click="router.push(`/article/${article.slug}`)"
              >
                <div class="article-body">
                  <div class="article-meta-top">
                    <span class="category" v-if="article.categoryName">{{ article.categoryName }}</span>
                    <span class="date">{{ formatDate(article.publishedAt || article.createdAt) }}</span>
                  </div>
                  <h3 class="article-title">{{ article.title }}</h3>
                  <p class="article-excerpt">{{ article.summary || truncate(article.content, 120) }}</p>
                  <div class="article-footer">
                    <div class="author">
                      <el-avatar :size="24">
                        {{ article.authorName?.charAt(0) || 'A' }}
                      </el-avatar>
                      <span>{{ article.authorName || '匿名' }}</span>
                    </div>
                    <div class="stats">
                      <span>👁 {{ article.viewCount || 0 }}</span>
                      <span>💬 {{ article.commentCount || 0 }}</span>
                      <span>❤️ {{ article.likeCount || 0 }}</span>
                    </div>
                  </div>
                </div>
                <div class="article-arrow">→</div>
              </article>
            </TransitionGroup>

            <div v-if="!loading && articles.length === 0" class="empty-state">
              <p>📝 暂无文章</p>
              <button class="hero-btn primary" @click="goToWrite">写第一篇文章</button>
            </div>

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
              <el-avatar :size="64" class="author-avatar">
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
          <div class="sidebar-widget">
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
          <div class="sidebar-widget">
            <h3 class="widget-title">
              <span>🔗</span> 快捷入口
            </h3>
            <div class="links">
              <button class="link-item" @click="router.push('/')">
                <span class="link-icon">🏠</span>
                <span>首页</span>
              </button>
              <button class="link-item" @click="goToWrite">
                <span class="link-icon">✍️</span>
                <span>写文章</span>
              </button>
              <button class="link-item" v-if="userStore.token" @click="router.push('/admin')">
                <span class="link-icon">⚙️</span>
                <span>后台管理</span>
              </button>
            </div>
          </div>
        </aside>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* ==================== Hero区域 ==================== */
.hero {
  position: relative;
  padding: var(--spacing-4xl) var(--spacing-lg) var(--spacing-3xl);
  text-align: center;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background: var(--color-gradient);
  z-index: 0;
}

.hero-bg::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 80%, rgba(255,255,255,0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255,255,255,0.15) 0%, transparent 50%);
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 700px;
  margin: 0 auto;
}

.hero-title {
  font-size: var(--text-5xl);
  font-weight: var(--font-bold);
  margin-bottom: var(--spacing-md);
  letter-spacing: -1px;
}

.hero-subtitle {
  font-size: var(--text-lg);
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: var(--spacing-2xl);
  font-weight: var(--font-normal);
}

.hero-actions {
  display: flex;
  gap: var(--spacing-md);
  justify-content: center;
  margin-bottom: var(--spacing-3xl);
}

.hero-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 12px 32px;
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  border-radius: var(--radius-full);
  transition: all var(--transition-base);
  border: none;
  cursor: pointer;
}

.hero-btn.primary {
  background: white;
  color: var(--color-primary);
}

.hero-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.hero-btn.secondary {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.4);
}

.hero-btn.secondary:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.6);
}

.hero-stats {
  display: flex;
  justify-content: center;
  gap: var(--spacing-3xl);
}

.stat {
  text-align: center;
  color: white;
}

.stat-num {
  display: block;
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  margin-bottom: var(--spacing-xs);
}

.stat-label {
  font-size: var(--text-sm);
  opacity: 0.8;
}

/* ==================== 内容区域 ==================== */
.content-section {
  padding: var(--spacing-2xl) 0 var(--spacing-3xl);
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr var(--sidebar-width);
  gap: var(--spacing-2xl);
}

/* ==================== 文章列表 ==================== */
.section-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--color-text);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
}

.articles {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.article-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  cursor: pointer;
  border: 1px solid var(--color-border);
  transition: all var(--transition-base);
}

.article-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
  border-color: var(--color-primary);
}

.article-body {
  flex: 1;
  min-width: 0;
}

.article-meta-top {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.category {
  background: var(--color-gradient);
  color: white;
  padding: 2px 12px;
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
}

.date {
  color: var(--color-text-muted);
  font-size: var(--text-xs);
}

.article-title {
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--color-text);
  margin-bottom: var(--spacing-sm);
  line-height: var(--leading-tight);
  transition: color var(--transition-fast);
}

.article-card:hover .article-title {
  color: var(--color-primary);
}

.article-excerpt {
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  line-height: var(--leading-normal);
  margin-bottom: var(--spacing-md);
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
  gap: var(--spacing-sm);
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
}

.stats {
  display: flex;
  gap: var(--spacing-md);
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.article-arrow {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  background: var(--color-bg-subtle);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  font-size: var(--text-lg);
  transition: all var(--transition-base);
  flex-shrink: 0;
}

.article-card:hover .article-arrow {
  background: var(--color-primary);
  color: white;
  transform: translateX(4px);
}

/* ==================== 空状态 ==================== */
.empty-state {
  text-align: center;
  padding: var(--spacing-3xl);
  color: var(--color-text-muted);
}

.empty-state p {
  margin-bottom: var(--spacing-lg);
}

/* ==================== 侧边栏 ==================== */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.sidebar-widget {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--color-border);
}

.widget-title {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--color-text);
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--color-divider);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin: 0;
}

/* ==================== 作者卡片 ==================== */
.author-cover {
  height: 72px;
  background: var(--color-gradient);
}

.author-info {
  padding: 0 var(--spacing-lg) var(--spacing-lg);
  text-align: center;
  margin-top: -32px;
}

.author-avatar {
  border: 3px solid var(--color-bg-card);
  box-shadow: var(--shadow-sm);
}

.author-name {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--color-text);
  margin: var(--spacing-sm) 0 var(--spacing-xs);
}

.author-bio {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  margin-bottom: var(--spacing-md);
}

.author-stats {
  display: flex;
  justify-content: center;
  gap: var(--spacing-2xl);
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--color-divider);
}

.author-stat {
  text-align: center;
}

.author-stat .num {
  display: block;
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--color-primary);
}

.author-stat .label {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

/* ==================== 标签云 ==================== */
.tags-cloud {
  padding: var(--spacing-md) var(--spacing-lg);
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.tag-item {
  background: var(--color-bg-subtle);
  color: var(--color-text-secondary);
  padding: 6px 14px;
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tag-item:hover {
  background: var(--color-gradient);
  color: white;
  transform: translateY(-1px);
}

/* ==================== 快捷链接 ==================== */
.links {
  padding: var(--spacing-sm) var(--spacing-md);
}

.link-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--color-text-secondary);
  text-decoration: none;
  width: 100%;
  text-align: left;
}

.link-item:hover {
  background: var(--color-bg-subtle);
  color: var(--color-primary);
  transform: translateX(4px);
}

.link-icon {
  font-size: var(--text-lg);
}

/* ==================== 分页 ==================== */
.pagination {
  margin-top: var(--spacing-2xl);
  display: flex;
  justify-content: center;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: var(--color-primary);
}

/* ==================== 响应式 ==================== */
@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .sidebar {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: var(--spacing-md);
  }

  .sidebar-widget:first-child {
    grid-column: span 2;
  }
}

@media (max-width: 768px) {
  .hero {
    padding: var(--spacing-3xl) var(--spacing-md) var(--spacing-2xl);
  }

  .hero-title {
    font-size: var(--text-3xl);
  }

  .hero-subtitle {
    font-size: var(--text-base);
    margin-bottom: var(--spacing-xl);
  }

  .hero-actions {
    flex-direction: column;
    gap: var(--spacing-sm);
  }

  .hero-btn {
    width: 100%;
    justify-content: center;
  }

  .hero-stats {
    gap: var(--spacing-xl);
  }

  .stat-num {
    font-size: var(--text-2xl);
  }

  .article-card {
    padding: var(--spacing-md);
    flex-direction: column;
    align-items: flex-start;
  }

  .article-arrow {
    display: none;
  }

  .article-title {
    font-size: var(--text-lg);
  }

  .article-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-sm);
  }

  .sidebar {
    grid-template-columns: 1fr;
  }

  .sidebar-widget:first-child {
    grid-column: span 1;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: var(--text-2xl);
  }

  .hero-stats {
    flex-direction: column;
    gap: var(--spacing-md);
  }

  .article-meta-top {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-xs);
  }
}
</style>
