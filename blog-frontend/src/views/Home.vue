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
      tags.value = res.data.slice(0, 15)
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
    <!-- Hero -->
    <section class="hero">
      <div class="hero-container">
        <div class="hero-badge">面向AI时代的博客</div>
        <h1 class="hero-title">
          用文字连接<br>人类与AI
        </h1>
        <p class="hero-desc">
          为人类与AI Agent提供结构化、可机器读取的优质内容
        </p>
        <div class="hero-actions">
          <button class="btn-primary large" @click="goToWrite">开始创作</button>
          <button class="btn-secondary large" @click="scrollToContent">浏览文章</button>
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
        </div>
      </div>
    </section>

    <!-- Content -->
    <section class="content" id="content">
      <div class="content-container">
        <!-- Articles -->
        <main class="main-content">
          <div class="section-header">
            <h2>最新文章</h2>
          </div>

          <div v-loading="loading">
            <div class="articles" v-if="articles.length">
              <article
                v-for="(article, index) in articles"
                :key="article.id"
                class="article-item"
                @click="router.push(`/article/${article.slug}`)"
              >
                <div class="article-meta">
                  <span class="article-category" v-if="article.categoryName">{{ article.categoryName }}</span>
                  <span class="article-date">{{ formatDate(article.publishedAt || article.createdAt) }}</span>
                </div>
                <h3 class="article-title">{{ article.title }}</h3>
                <p class="article-excerpt">{{ article.summary || truncate(article.content, 120) }}</p>
                <div class="article-footer">
                  <div class="article-author">
                    <div class="author-avatar">{{ article.authorName?.charAt(0) || 'A' }}</div>
                    <span>{{ article.authorName || '匿名' }}</span>
                  </div>
                  <span class="article-read">{{ Math.ceil((article.content?.length || 0) / 500) }} min read</span>
                </div>
              </article>
            </div>

            <div v-else-if="!loading" class="empty">
              <p>暂无文章</p>
              <button class="btn-primary" @click="goToWrite">写第一篇</button>
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

        <!-- Sidebar -->
        <aside class="sidebar">
          <!-- Author Card -->
          <div class="sidebar-card author-card">
            <div class="author-header">
              <div class="author-avatar large">{{ userStore.userInfo?.username?.charAt(0) || 'N' }}</div>
            </div>
            <div class="author-body">
              <h3>{{ userStore.userInfo?.displayName || 'NovaBlog' }}</h3>
              <p>面向AI时代的博客系统</p>
              <div class="author-stats">
                <div class="author-stat">
                  <span class="stat-num">{{ total }}</span>
                  <span class="stat-label">文章</span>
                </div>
                <div class="author-stat">
                  <span class="stat-num">{{ tags.length }}</span>
                  <span class="stat-label">标签</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Tags -->
          <div class="sidebar-card">
            <div class="card-header">
              <h3>热门标签</h3>
            </div>
            <div class="card-body">
              <div class="tag-list">
                <span v-for="tag in tags" :key="tag.id" class="tag">
                  {{ tag.name }}
                </span>
              </div>
            </div>
          </div>

          <!-- Quick Links -->
          <div class="sidebar-card">
            <div class="card-header">
              <h3>快捷入口</h3>
            </div>
            <div class="card-body">
              <div class="link-list">
                <button class="link-item" @click="router.push('/')">
                  <span>首页</span>
                  <span class="link-arrow">→</span>
                </button>
                <button class="link-item" @click="goToWrite">
                  <span>写文章</span>
                  <span class="link-arrow">→</span>
                </button>
                <button class="link-item" v-if="userStore.token" @click="router.push('/admin')">
                  <span>管理后台</span>
                  <span class="link-arrow">→</span>
                </button>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* ==================== Hero ==================== */
.hero {
  padding: 120px 24px 80px;
  text-align: center;
  background: linear-gradient(180deg, #f8f9fa 0%, var(--color-surface) 100%);
}

[data-theme="dark"] .hero {
  background: linear-gradient(180deg, #121212 0%, var(--color-surface) 100%);
}

.hero-container {
  max-width: 680px;
  margin: 0 auto;
}

.hero-badge {
  display: inline-block;
  padding: 6px 16px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-size: 13px;
  font-weight: 500;
  border-radius: var(--radius-full);
  margin-bottom: 32px;
}

[data-theme="dark"] .hero-badge {
  background: rgba(138, 180, 248, 0.12);
}

.hero-title {
  font-size: 56px;
  font-weight: 700;
  color: var(--color-text);
  line-height: 1.15;
  letter-spacing: -1.5px;
  margin: 0 0 20px;
}

.hero-desc {
  font-size: 18px;
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin: 0 0 40px;
}

.hero-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 60px;
}

.hero-stats {
  display: flex;
  gap: 48px;
  justify-content: center;
}

.stat {
  text-align: center;
}

.stat-num {
  display: block;
  font-size: 32px;
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -1px;
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-tertiary);
  margin-top: 4px;
}

/* ==================== Buttons ==================== */
.btn-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-on-primary);
  background: var(--color-primary);
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
  text-decoration: none;
}

.btn-primary:hover {
  background: var(--color-primary-hover);
  box-shadow: var(--shadow-md);
}

.btn-primary.large {
  padding: 12px 32px;
  font-size: 15px;
}

.btn-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
  text-decoration: none;
}

.btn-secondary:hover {
  background: var(--color-surface-dim);
  border-color: var(--color-text-tertiary);
}

.btn-secondary.large {
  padding: 12px 32px;
  font-size: 15px;
}

/* ==================== Content ==================== */
.content {
  padding: 60px 24px 80px;
  max-width: var(--content-max);
  margin: 0 auto;
}

.content-container {
  display: grid;
  grid-template-columns: 1fr var(--sidebar-width);
  gap: 64px;
}

/* ==================== Section Header ==================== */
.section-header {
  margin-bottom: 32px;
}

.section-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
  letter-spacing: -0.3px;
}

/* ==================== Articles ==================== */
.articles {
  display: flex;
  flex-direction: column;
}

.article-item {
  padding: 28px 0;
  border-bottom: 1px solid var(--color-border-subtle);
  cursor: pointer;
  transition: opacity var(--duration-fast) var(--ease-out);
}

.article-item:first-child {
  padding-top: 0;
}

.article-item:last-child {
  border-bottom: none;
}

.article-item:hover {
  opacity: 0.7;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.article-category {
  color: var(--color-primary);
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.article-date {
  color: var(--color-text-tertiary);
  font-size: 13px;
}

.article-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 8px;
  line-height: 1.3;
  letter-spacing: -0.2px;
}

.article-excerpt {
  font-size: 15px;
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin: 0 0 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.article-author {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.author-avatar {
  width: 24px;
  height: 24px;
  background: var(--color-surface-container);
  color: var(--color-text);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
}

.author-avatar.large {
  width: 48px;
  height: 48px;
  font-size: 18px;
}

.article-read {
  font-size: 13px;
  color: var(--color-text-tertiary);
}

/* ==================== Empty ==================== */
.empty {
  text-align: center;
  padding: 80px 24px;
  color: var(--color-text-tertiary);
}

.empty p {
  margin: 0 0 20px;
  font-size: 16px;
}

/* ==================== Pagination ==================== */
.pagination {
  margin-top: 48px;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: var(--color-primary);
}

/* ==================== Sidebar ==================== */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.sidebar-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border-subtle);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: box-shadow var(--duration-normal) var(--ease-out);
}

.sidebar-card:hover {
  box-shadow: var(--shadow-lg);
}

.card-header {
  padding: 20px 24px 0;
}

.card-header h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
}

.card-body {
  padding: 16px 24px 20px;
}

/* ==================== Author Card ==================== */
.author-card {
  text-align: center;
}

.author-header {
  padding: 24px 24px 0;
}

.author-header .author-avatar {
  margin: 0 auto;
  background: var(--color-primary);
  color: var(--color-text-on-primary);
  font-size: 18px;
}

.author-body {
  padding: 16px 24px 24px;
}

.author-body h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 4px;
}

.author-body p {
  font-size: 14px;
  color: var(--color-text-tertiary);
  margin: 0 0 20px;
}

.author-stats {
  display: flex;
  justify-content: center;
  gap: 48px;
  padding-top: 20px;
  border-top: 1px solid var(--color-border-subtle);
}

.author-stat {
  text-align: center;
}

.author-stat .stat-num {
  font-size: 20px;
}

/* ==================== Tags ==================== */
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  padding: 6px 14px;
  background: var(--color-surface-dim);
  color: var(--color-text-secondary);
  font-size: 13px;
  font-weight: 500;
  border-radius: var(--radius-full);
  transition: all var(--duration-fast) var(--ease-out);
}

.tag:hover {
  background: var(--color-primary);
  color: var(--color-text-on-primary);
  cursor: pointer;
}

[data-theme="dark"] .tag:hover {
  background: var(--color-primary);
  color: var(--color-text-on-primary);
}

/* ==================== Links ==================== */
.link-list {
  display: flex;
  flex-direction: column;
}

.link-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary);
  background: none;
  border: none;
  border-bottom: 1px solid var(--color-border-subtle);
  cursor: pointer;
  transition: color var(--duration-fast) var(--ease-out);
  width: 100%;
  text-align: left;
}

.link-item:last-child {
  border-bottom: none;
}

.link-item:hover {
  color: var(--color-primary);
}

.link-arrow {
  color: var(--color-text-tertiary);
  transition: transform var(--duration-fast) var(--ease-out);
}

.link-item:hover .link-arrow {
  transform: translateX(4px);
  color: var(--color-primary);
}

/* ==================== Responsive ==================== */
@media (max-width: 1024px) {
  .content-container {
    grid-template-columns: 1fr;
    gap: 40px;
  }

  .sidebar {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }

  .sidebar-card:first-child {
    grid-column: span 2;
  }
}

@media (max-width: 768px) {
  .hero {
    padding: 80px 16px 60px;
  }

  .hero-title {
    font-size: 36px;
    letter-spacing: -1px;
  }

  .hero-desc {
    font-size: 16px;
  }

  .hero-actions {
    flex-direction: column;
    gap: 10px;
  }

  .btn-primary.large,
  .btn-secondary.large {
    width: 100%;
    padding: 14px 24px;
  }

  .hero-stats {
    gap: 32px;
  }

  .stat-num {
    font-size: 28px;
  }

  .content {
    padding: 40px 16px 60px;
  }

  .sidebar {
    grid-template-columns: 1fr;
  }

  .sidebar-card:first-child {
    grid-column: span 1;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 32px;
  }
}
</style>
