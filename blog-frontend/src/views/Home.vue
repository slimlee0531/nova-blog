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

const scrollToContent = () => {
  document.getElementById('content')?.scrollIntoView({ behavior: 'smooth' })
}

onMounted(() => {
  fetchArticles()
  fetchTags()
})
</script>

<template>
  <div class="home">
    <!-- Hero区域 (Apple Style) -->
    <section class="hero">
      <div class="hero-content">
        <h1 class="hero-title">NovaBlog</h1>
        <p class="hero-subtitle">面向AI时代的个人博客</p>
        <div class="hero-actions">
          <button class="hero-btn primary" @click="goToWrite">开始创作</button>
          <button class="hero-btn secondary" @click="scrollToContent">浏览文章</button>
        </div>
      </div>
    </section>

    <!-- 主内容区 -->
    <section class="content-section" id="content">
      <div class="content-grid">
        <!-- 左侧文章列表 -->
        <main class="article-list">
          <h2 class="section-title">最新文章</h2>

          <div v-loading="loading" class="articles-wrapper">
            <TransitionGroup name="list" tag="div" class="articles">
              <article
                v-for="article in articles"
                :key="article.id"
                class="article-card"
                @click="router.push(`/article/${article.slug}`)"
              >
                <div class="article-meta-top">
                  <span class="category" v-if="article.categoryName">{{ article.categoryName }}</span>
                  <span class="date">{{ formatDate(article.publishedAt || article.createdAt) }}</span>
                </div>
                <h3 class="article-title">{{ article.title }}</h3>
                <p class="article-excerpt">{{ article.summary || truncate(article.content, 120) }}</p>
                <div class="article-footer">
                  <div class="author">
                    <el-avatar :size="20">
                      {{ article.authorName?.charAt(0) || 'A' }}
                    </el-avatar>
                    <span>{{ article.authorName || '匿名' }}</span>
                  </div>
                  <div class="stats">
                    <span>{{ article.viewCount || 0 }} 次阅读</span>
                  </div>
                </div>
              </article>
            </TransitionGroup>

            <div v-if="!loading && articles.length === 0" class="empty-state">
              <p>暂无文章</p>
              <button class="hero-btn primary small" @click="goToWrite">写第一篇文章</button>
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
            <div class="author-info">
              <el-avatar :size="48" class="author-avatar">
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
            <h3 class="widget-title">热门标签</h3>
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
            <h3 class="widget-title">快捷入口</h3>
            <div class="links">
              <button class="link-item" @click="router.push('/')">
                <span>首页</span>
                <span class="link-arrow">→</span>
              </button>
              <button class="link-item" @click="goToWrite">
                <span>写文章</span>
                <span class="link-arrow">→</span>
              </button>
              <button class="link-item" v-if="userStore.token" @click="router.push('/admin')">
                <span>后台管理</span>
                <span class="link-arrow">→</span>
              </button>
            </div>
          </div>
        </aside>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* ==================== Hero区域 (Apple Style) ==================== */
.hero {
  padding: var(--spacing-4xl) var(--spacing-lg) var(--spacing-3xl);
  text-align: center;
  background: var(--color-bg);
}

.hero-content {
  max-width: 600px;
  margin: 0 auto;
}

.hero-title {
  font-size: 56px;
  font-weight: var(--font-bold);
  color: var(--color-text);
  margin-bottom: var(--spacing-sm);
  letter-spacing: -1.5px;
  line-height: 1.1;
}

.hero-subtitle {
  font-size: var(--text-xl);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-2xl);
  font-weight: var(--font-normal);
}

.hero-actions {
  display: flex;
  gap: var(--spacing-md);
  justify-content: center;
}

.hero-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 12px 28px;
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  border-radius: var(--radius-full);
  transition: all var(--transition-fast);
  border: none;
  cursor: pointer;
}

.hero-btn.primary {
  background: var(--color-primary);
  color: white;
}

.hero-btn.primary:hover {
  background: var(--color-primary-dark);
}

.hero-btn.primary.small {
  padding: 10px 24px;
  font-size: var(--text-sm);
}

.hero-btn.secondary {
  background: var(--color-bg-subtle);
  color: var(--color-text);
}

.hero-btn.secondary:hover {
  background: var(--color-border-light);
}

/* ==================== 内容区域 ==================== */
.content-section {
  padding: var(--spacing-2xl) var(--spacing-lg) var(--spacing-3xl);
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr var(--sidebar-width);
  gap: var(--spacing-3xl);
}

/* ==================== 文章列表 ==================== */
.section-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--color-text);
  margin-bottom: var(--spacing-xl);
  letter-spacing: -0.5px;
}

.articles {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.article-card {
  padding-bottom: var(--spacing-lg);
  border-bottom: 1px solid var(--color-border-light);
  cursor: pointer;
  transition: opacity var(--transition-fast);
}

.article-card:last-child {
  border-bottom: none;
}

.article-card:hover {
  opacity: 0.7;
}

.article-meta-top {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.category {
  color: var(--color-primary);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  text-transform: uppercase;
  letter-spacing: 0.5px;
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
  letter-spacing: -0.3px;
}

.article-excerpt {
  color: var(--color-text-secondary);
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
  align-items: center;
}

.author {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.stats {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

/* ==================== 空状态 ==================== */
.empty-state {
  text-align: center;
  padding: var(--spacing-4xl);
  color: var(--color-text-muted);
}

.empty-state p {
  margin-bottom: var(--spacing-lg);
  font-size: var(--text-lg);
}

/* ==================== 侧边栏 ==================== */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.sidebar-widget {
  background: var(--color-bg-card);
}

.widget-title {
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin: 0 0 var(--spacing-md) 0;
}

/* ==================== 作者卡片 ==================== */
.author-info {
  text-align: center;
}

.author-avatar {
  margin-bottom: var(--spacing-md);
}

.author-name {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--color-text);
  margin: 0 0 var(--spacing-xs) 0;
}

.author-bio {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  margin: 0 0 var(--spacing-lg) 0;
}

.author-stats {
  display: flex;
  justify-content: center;
  gap: var(--spacing-2xl);
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--color-border-light);
}

.author-stat {
  text-align: center;
}

.author-stat .num {
  display: block;
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--color-text);
}

.author-stat .label {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

/* ==================== 标签云 ==================== */
.tags-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.tag-item {
  color: var(--color-text-secondary);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.tag-item:hover {
  color: var(--color-primary);
}

.tag-item::before {
  content: '#';
  color: var(--color-text-muted);
}

/* ==================== 快捷链接 ==================== */
.links {
  display: flex;
  flex-direction: column;
}

.link-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-sm) 0;
  border-bottom: 1px solid var(--color-border-light);
  cursor: pointer;
  transition: opacity var(--transition-fast);
  color: var(--color-text);
  text-decoration: none;
  width: 100%;
  font-size: var(--text-sm);
}

.link-item:last-child {
  border-bottom: none;
}

.link-item:hover {
  opacity: 0.7;
}

.link-arrow {
  color: var(--color-text-muted);
  font-size: var(--text-sm);
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
    gap: var(--spacing-2xl);
  }

  .sidebar {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: var(--spacing-lg);
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
    font-size: 40px;
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

  .content-section {
    padding: var(--spacing-xl) var(--spacing-md);
  }

  .article-title {
    font-size: var(--text-lg);
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
    font-size: 32px;
  }
}
</style>
