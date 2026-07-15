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

const getReadTime = (content: string) => {
  return Math.ceil((content?.length || 0) / 500)
}

onMounted(() => {
  fetchArticles()
  fetchTags()
})
</script>

<template>
  <div class="home">
    <!-- Hero Section -->
    <section class="hero">
      <div class="hero-container">
        <div class="hero-content">
          <div class="hero-badge">✦ 面向AI时代的博客</div>
          <h1 class="hero-title">
            用文字连接<br/>
            <span class="gradient-text">人类与AI</span>
          </h1>
          <p class="hero-desc">
            为人类与AI Agent提供结构化、可机器读取的优质内容
          </p>
          <div class="hero-actions">
            <button class="btn-primary" @click="goToWrite">
              <span>开始创作</span>
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M6 12L10 8L6 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
            <button class="btn-ghost" @click="scrollToContent">浏览文章</button>
          </div>
        </div>
        <div class="hero-decoration">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
          <div class="decoration-circle circle-3"></div>
        </div>
      </div>
    </section>

    <!-- Stats Bar -->
    <section class="stats-bar">
      <div class="stats-container">
        <div class="stat-item">
          <span class="stat-number">{{ total }}</span>
          <span class="stat-label">篇文章</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-number">{{ tags.length }}</span>
          <span class="stat-label">个标签</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-number">∞</span>
          <span class="stat-label">种可能</span>
        </div>
      </div>
    </section>

    <!-- Content Section -->
    <section class="content" id="content">
      <div class="content-container">
        <!-- Main Content -->
        <main class="main-content">
          <div class="section-header">
            <h2>最新文章</h2>
            <div class="section-line"></div>
          </div>

          <div v-loading="loading" class="articles-loading">
            <div class="articles" v-if="articles.length">
              <article
                v-for="(article, index) in articles"
                :key="article.id"
                class="article-card"
                :class="{ featured: index === 0 }"
                @click="router.push(`/article/${article.slug}`)"
              >
                <div class="article-image" v-if="article.featuredImage && index === 0">
                  <img :src="article.featuredImage" :alt="article.title" />
                </div>
                <div class="article-content">
                  <div class="article-meta">
                    <span class="article-category" v-if="article.categoryName">{{ article.categoryName }}</span>
                    <span class="article-date">{{ formatDate(article.publishedAt || article.createdAt) }}</span>
                    <span class="article-read">{{ getReadTime(article.content) }} min read</span>
                  </div>
                  <h3 class="article-title">{{ article.title }}</h3>
                  <p class="article-excerpt">{{ article.summary || truncate(article.content, 150) }}</p>
                  <div class="article-tags" v-if="article.tagInfos?.length || article.tags?.length">
                    <template v-if="article.tagInfos?.length">
                      <span
                        v-for="tag in article.tagInfos.slice(0, 4)"
                        :key="tag.id"
                        class="article-tag"
                        :style="tag.color ? { background: `${tag.color}14`, color: tag.color, borderColor: `${tag.color}36` } : {}"
                      >#{{ tag.name }}</span>
                    </template>
                    <template v-else>
                      <span v-for="tag in article.tags?.slice(0, 4)" :key="tag" class="article-tag">#{{ tag }}</span>
                    </template>
                  </div>
                  <div class="article-footer">
                    <div class="article-author">
                      <div class="author-avatar">{{ article.authorName?.charAt(0) || 'A' }}</div>
                      <span class="author-name">{{ article.authorName || '匿名' }}</span>
                    </div>
                    <div class="article-stats">
                      <span class="stat">👁 {{ article.viewCount || 0 }}</span>
                      <span class="stat">💬 {{ article.commentCount || 0 }}</span>
                    </div>
                  </div>
                </div>
              </article>
            </div>

            <div v-else-if="!loading" class="empty-state">
              <div class="empty-icon">📝</div>
              <h3>暂无文章</h3>
              <p>开始创作你的第一篇文章吧</p>
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
              <div class="author-avatar-large">
                {{ userStore.userInfo?.username?.charAt(0) || 'N' }}
              </div>
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
              <div class="tag-cloud">
                <span
                  v-for="tag in tags"
                  :key="tag.id"
                  class="tag"
                  :style="tag.color ? { background: `${tag.color}15`, color: tag.color, borderColor: `${tag.color}40` } : {}"
                >
                  #{{ tag.name }}
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
                  <span class="link-icon">🏠</span>
                  <span>首页</span>
                  <span class="link-arrow">→</span>
                </button>
                <button class="link-item" @click="goToWrite">
                  <span class="link-icon">✍️</span>
                  <span>写文章</span>
                  <span class="link-arrow">→</span>
                </button>
                <button class="link-item" v-if="userStore.token" @click="router.push('/bookmarks')">
                  <span class="link-icon">⭐</span>
                  <span>我的收藏</span>
                  <span class="link-arrow">→</span>
                </button>
                <button class="link-item" v-if="userStore.token" @click="router.push('/admin')">
                  <span class="link-icon">⚙️</span>
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
  position: relative;
  padding: 140px 24px 80px;
  text-align: center;
  background: linear-gradient(180deg, #fafbfc 0%, #fff 100%);
  overflow: hidden;
}

[data-theme="dark"] .hero {
  background: linear-gradient(180deg, #0a0a0a 0%, #111 100%);
}

.hero-container {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-badge {
  display: inline-block;
  padding: 8px 20px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  font-size: 14px;
  font-weight: 600;
  border-radius: 9999px;
  margin-bottom: 32px;
}

[data-theme="dark"] .hero-badge {
  background: rgba(102, 126, 234, 0.15);
}

.hero-title {
  font-size: 64px;
  font-weight: 800;
  color: #1a1a1a;
  line-height: 1.1;
  letter-spacing: -2px;
  margin: 0 0 24px;
}

[data-theme="dark"] .hero-title {
  color: #fff;
}

.gradient-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-desc {
  font-size: 20px;
  color: #666;
  line-height: 1.6;
  margin: 0 0 40px;
  max-width: 500px;
  margin-left: auto;
  margin-right: auto;
}

[data-theme="dark"] .hero-desc {
  color: #999;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

/* 装饰元素 */
.hero-decoration {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 600px;
  height: 600px;
  pointer-events: none;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.5;
}

.circle-1 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.08) 0%, transparent 70%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: float 20s ease-in-out infinite;
}

.circle-2 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(118, 75, 162, 0.06) 0%, transparent 70%);
  top: 20%;
  right: 10%;
  animation: float 15s ease-in-out infinite reverse;
}

.circle-3 {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(236, 72, 153, 0.05) 0%, transparent 70%);
  bottom: 20%;
  left: 10%;
  animation: float 18s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(20px, -20px); }
}

/* ==================== Buttons ==================== */
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  background: #fff;
  border: 2px solid #e5e5e5;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

[data-theme="dark"] .btn-ghost {
  color: #fff;
  background: #1a1a1a;
  border-color: #333;
}

.btn-ghost:hover {
  border-color: #667eea;
  color: #667eea;
}

/* ==================== Stats Bar ==================== */
.stats-bar {
  background: #fff;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
  padding: 24px 0;
}

[data-theme="dark"] .stats-bar {
  background: #111;
  border-color: #222;
}

.stats-container {
  max-width: 600px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 48px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 36px;
  font-weight: 800;
  color: #1a1a1a;
  letter-spacing: -1px;
}

[data-theme="dark"] .stat-number {
  color: #fff;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: #e5e5e5;
}

[data-theme="dark"] .stat-divider {
  background: #333;
}

/* ==================== Content ==================== */
.content {
  padding: 64px 24px 80px;
  max-width: 1360px;
  margin: 0 auto;
}

.content-container {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 40px;
  max-width: 1280px;
  margin: 0 auto;
}

/* ==================== Section Header ==================== */
.section-header {
  margin-bottom: 32px;
}

.section-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 12px;
}

[data-theme="dark"] .section-header h2 {
  color: #fff;
}

.section-line {
  width: 48px;
  height: 3px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
}

/* ==================== Articles ==================== */
.articles {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.article-card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

[data-theme="dark"] .article-card {
  background: #1a1a1a;
  border-color: #222;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
}

.article-card.featured {
  display: grid;
  grid-template-columns: 1fr 1fr;
}

.article-image {
  aspect-ratio: 16/10;
  overflow: hidden;
}

.article-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.article-card:hover .article-image img {
  transform: scale(1.05);
}

.article-content {
  padding: 24px 28px;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.article-category {
  color: #667eea;
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.article-date,
.article-read {
  color: #999;
  font-size: 13px;
}

.article-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 12px;
  line-height: 1.3;
  letter-spacing: -0.3px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

[data-theme="dark"] .article-title {
  color: #fff;
}

.article-card:hover .article-title {
  color: #667eea;
}

.article-excerpt {
  font-size: 15px;
  color: #666;
  line-height: 1.6;
  margin: 0 0 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

[data-theme="dark"] .article-excerpt {
  color: #999;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.article-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  background: #f5f5f5;
  color: #666;
  font-size: 12px;
  font-weight: 500;
  border-radius: 999px;
  border: 1px solid transparent;
  transition: all 0.2s ease;
  line-height: 1.4;
}

[data-theme="dark"] .article-tag {
  background: #222;
  color: #999;
}

.article-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.article-author {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-avatar {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}

.author-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
}

[data-theme="dark"] .author-name {
  color: #fff;
}

.article-stats {
  display: flex;
  gap: 12px;
}

.article-stats .stat {
  font-size: 13px;
  color: #999;
}

/* ==================== Empty State ==================== */
.empty-state {
  text-align: center;
  padding: 80px 24px;
  background: #fafbfc;
  border-radius: 16px;
}

[data-theme="dark"] .empty-state {
  background: #111;
}

.empty-icon {
  font-size: 56px;
  margin-bottom: 24px;
}

.empty-state h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}

[data-theme="dark"] .empty-state h3 {
  color: #fff;
}

.empty-state p {
  font-size: 15px;
  color: #999;
  margin: 0 0 24px;
}

/* ==================== Pagination ==================== */
.pagination {
  margin-top: 48px;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* ==================== Sidebar ==================== */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.sidebar-card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 16px;
  overflow: hidden;
}

[data-theme="dark"] .sidebar-card {
  background: #1a1a1a;
  border-color: #222;
}

.card-header {
  padding: 20px 24px 0;
}

.card-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

[data-theme="dark"] .card-header h3 {
  color: #fff;
}

.card-body {
  padding: 16px 24px 24px;
}

/* ==================== Author Card ==================== */
.author-card {
  text-align: center;
}

.author-header {
  padding: 32px 24px 0;
}

.author-avatar-large {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  margin: 0 auto;
}

.author-body {
  padding: 20px 24px 24px;
}

.author-body h3 {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px;
}

[data-theme="dark"] .author-body h3 {
  color: #fff;
}

.author-body p {
  font-size: 14px;
  color: #999;
  margin: 0 0 24px;
}

.author-stats {
  display: flex;
  justify-content: center;
  gap: 48px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

[data-theme="dark"] .author-stats {
  border-color: #222;
}

.author-stat {
  text-align: center;
}

.author-stat .stat-num {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
}

[data-theme="dark"] .author-stat .stat-num {
  color: #fff;
}

.author-stat .stat-label {
  font-size: 13px;
  color: #999;
}

/* ==================== Tags ==================== */
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  padding: 8px 16px;
  background: #f5f5f5;
  color: #666;
  font-size: 13px;
  font-weight: 500;
  border-radius: 8px;
  border: 1px solid transparent;
  transition: all 0.2s ease;
  cursor: pointer;
}

[data-theme="dark"] .tag {
  background: #222;
  color: #999;
}

.tag:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  cursor: pointer;
}

/* ==================== Links ==================== */
.link-list {
  display: flex;
  flex-direction: column;
}

.link-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 0;
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  background: none;
  border: none;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.2s ease;
  width: 100%;
  text-align: left;
}

[data-theme="dark"] .link-item {
  color: #fff;
  border-color: #222;
}

.link-item:last-child {
  border-bottom: none;
}

.link-item:hover {
  color: #667eea;
}

.link-icon {
  font-size: 18px;
}

.link-arrow {
  margin-left: auto;
  color: #ccc;
  transition: transform 0.2s ease;
}

[data-theme="dark"] .link-arrow {
  color: #444;
}

.link-item:hover .link-arrow {
  transform: translateX(4px);
  color: #667eea;
}

/* ==================== Responsive ==================== */
@media (max-width: 1024px) {
  .content-container {
    grid-template-columns: 1fr;
    gap: 48px;
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
    padding: 100px 20px 60px;
  }

  .hero-title {
    font-size: 40px;
    letter-spacing: -1px;
  }

  .hero-desc {
    font-size: 16px;
  }

  .hero-actions {
    flex-direction: column;
    gap: 12px;
  }

  .stats-container {
    gap: 32px;
  }

  .stat-number {
    font-size: 28px;
  }

  .content {
    padding: 40px 20px 60px;
  }

  .article-card.featured {
    grid-template-columns: 1fr;
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

  .stats-container {
    flex-direction: column;
    gap: 24px;
  }

  .stat-divider {
    width: 40px;
    height: 1px;
  }
}
</style>
