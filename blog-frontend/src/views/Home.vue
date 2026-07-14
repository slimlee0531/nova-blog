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
    <!-- Hero区域 (Premium Style) -->
    <section class="hero">
      <div class="hero-bg">
        <div class="hero-orb hero-orb-1"></div>
        <div class="hero-orb hero-orb-2"></div>
      </div>
      <div class="hero-content">
        <div class="hero-badge">✦ 面向AI时代的博客</div>
        <h1 class="hero-title">
          <span class="hero-title-line">用文字连接</span>
          <span class="hero-title-line gradient-text">人类与AI</span>
        </h1>
        <p class="hero-subtitle">为人类与AI Agent提供结构化、可机器读取的优质内容</p>
        <div class="hero-actions">
          <button class="hero-btn primary" @click="goToWrite">
            <span>✍️</span> 开始创作
          </button>
          <button class="hero-btn secondary" @click="scrollToContent">
            <span>↗</span> 浏览文章
          </button>
        </div>
        <div class="hero-stats">
          <div class="stat-item">
            <span class="stat-value">{{ total }}</span>
            <span class="stat-label">篇文章</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">{{ tags.length }}</span>
            <span class="stat-label">个标签</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">∞</span>
            <span class="stat-label">种可能</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 主内容区 -->
    <section class="content-section" id="content">
      <div class="content-grid">
        <!-- 左侧文章列表 -->
        <main class="article-list">
          <div class="section-header">
            <h2 class="section-title">最新文章</h2>
            <span class="section-count" v-if="total">共 {{ total }} 篇</span>
          </div>

          <div v-loading="loading" class="articles-wrapper">
            <TransitionGroup name="list" tag="div" class="articles">
              <article
                v-for="(article, index) in articles"
                :key="article.id"
                class="article-card"
                :style="{ animationDelay: `${index * 50}ms` }"
                @click="router.push(`/article/${article.slug}`)"
              >
                <div class="article-card-inner">
                  <div class="article-content">
                    <div class="article-meta-top">
                      <span class="category" v-if="article.categoryName">{{ article.categoryName }}</span>
                      <span class="meta-dot">·</span>
                      <span class="date">{{ formatDate(article.publishedAt || article.createdAt) }}</span>
                    </div>
                    <h3 class="article-title">{{ article.title }}</h3>
                    <p class="article-excerpt">{{ article.summary || truncate(article.content, 100) }}</p>
                    <div class="article-footer">
                      <div class="author">
                        <el-avatar :size="24" class="author-avatar">
                          {{ article.authorName?.charAt(0) || 'A' }}
                        </el-avatar>
                        <span class="author-name">{{ article.authorName || '匿名' }}</span>
                      </div>
                      <div class="article-meta-right">
                        <span class="read-time">{{ Math.ceil((article.content?.length || 0) / 500) }} 分钟</span>
                      </div>
                    </div>
                  </div>
                  <div class="article-arrow">
                    <span>→</span>
                  </div>
                </div>
              </article>
            </TransitionGroup>

            <div v-if="!loading && articles.length === 0" class="empty-state">
              <div class="empty-icon">📝</div>
              <h3>暂无文章</h3>
              <p>开始创作你的第一篇文章吧</p>
              <button class="hero-btn primary small" @click="goToWrite">开始创作</button>
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
              <el-avatar :size="56" class="author-avatar">
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
              <span class="widget-icon">🏷️</span>
              热门标签
            </h3>
            <div class="tags-cloud">
              <span
                v-for="tag in tags"
                :key="tag.id"
                class="tag-item"
              >
                #{{ tag.name }}
              </span>
            </div>
          </div>

          <!-- 快捷入口 -->
          <div class="sidebar-widget">
            <h3 class="widget-title">
              <span class="widget-icon">⚡</span>
              快捷入口
            </h3>
            <div class="links">
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
              <button class="link-item" v-if="userStore.token" @click="router.push('/admin')">
                <span class="link-icon">⚙️</span>
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
/* ==================== Hero区域 (Premium Style) ==================== */
.hero {
  position: relative;
  padding: 100px var(--spacing-lg) 80px;
  text-align: center;
  overflow: hidden;
  background: linear-gradient(180deg, #f8f9ff 0%, #ffffff 100%);
}

[data-theme="dark"] .hero {
  background: linear-gradient(180deg, #0a0a0c 0%, #000000 100%);
}

.hero-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.hero-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
}

.hero-orb-1 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.15) 0%, transparent 70%);
  top: -200px;
  right: -100px;
  animation: float 20s ease-in-out infinite;
}

.hero-orb-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(118, 75, 162, 0.12) 0%, transparent 70%);
  bottom: -200px;
  left: -100px;
  animation: float 25s ease-in-out infinite reverse;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(30px, -30px) scale(1.05); }
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 700px;
  margin: 0 auto;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: var(--radius-full);
  font-size: 13px;
  font-weight: 500;
  color: var(--color-primary);
  margin-bottom: 32px;
}

.hero-title {
  font-size: 64px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 20px;
  letter-spacing: -2px;
  line-height: 1.1;
}

.hero-title-line {
  display: block;
}

.gradient-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 18px;
  color: var(--color-text-secondary);
  margin-bottom: 40px;
  font-weight: 400;
  line-height: 1.6;
  max-width: 500px;
  margin-left: auto;
  margin-right: auto;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 60px;
}

.hero-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  font-size: 16px;
  font-weight: 500;
  border-radius: var(--radius-full);
  transition: all var(--transition-base);
  border: none;
  cursor: pointer;
}

.hero-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
}

.hero-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.5);
}

.hero-btn.primary.small {
  padding: 12px 28px;
  font-size: 14px;
}

.hero-btn.secondary {
  background: var(--color-bg-card);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.hero-btn.secondary:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--color-text-muted);
}

.hero-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 36px;
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -1px;
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-muted);
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: var(--color-border);
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
  gap: 60px;
}

/* ==================== 文章列表 ==================== */
.section-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 32px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text);
  letter-spacing: -0.5px;
}

.section-count {
  font-size: 14px;
  color: var(--color-text-muted);
}

.articles {
  display: flex;
  flex-direction: column;
}

.article-card {
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.article-card-inner {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 28px 0;
  border-bottom: 1px solid var(--color-border-light);
  cursor: pointer;
  transition: all var(--transition-base);
}

.article-card:first-child .article-card-inner {
  padding-top: 0;
}

.article-card:last-child .article-card-inner {
  border-bottom: none;
}

.article-card:hover .article-card-inner {
  opacity: 0.7;
}

.article-content {
  flex: 1;
  min-width: 0;
}

.article-meta-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.category {
  color: var(--color-primary);
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.meta-dot {
  color: var(--color-text-muted);
}

.date {
  color: var(--color-text-muted);
  font-size: 13px;
}

.article-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 10px;
  line-height: 1.3;
  letter-spacing: -0.3px;
  transition: color var(--transition-fast);
}

.article-card:hover .article-title {
  color: var(--color-primary);
}

.article-excerpt {
  color: var(--color-text-secondary);
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 16px;
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

.author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 12px;
  font-weight: 600;
}

.author-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
}

.read-time {
  font-size: 12px;
  color: var(--color-text-muted);
}

.article-arrow {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-bg-subtle);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-muted);
  font-size: 18px;
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
  padding: 80px 40px;
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  border: 1px dashed var(--color-border);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.empty-state h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}

.empty-state p {
  color: var(--color-text-muted);
  margin-bottom: 24px;
}

/* ==================== 侧边栏 ==================== */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.sidebar-widget {
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  overflow: hidden;
  border: 1px solid var(--color-border-light);
  transition: all var(--transition-base);
}

.sidebar-widget:hover {
  box-shadow: var(--shadow-md);
}

.widget-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  padding: 20px 24px;
  border-bottom: 1px solid var(--color-border-light);
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.widget-icon {
  font-size: 16px;
}

/* ==================== 作者卡片 ==================== */
.author-widget {
  text-align: center;
}

.author-cover {
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.author-cover::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 80%, rgba(255,255,255,0.2) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255,255,255,0.3) 0%, transparent 50%);
}

.author-info {
  padding: 0 24px 24px;
  margin-top: -28px;
}

.author-avatar {
  border: 4px solid var(--color-bg-card);
  box-shadow: var(--shadow-md);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
}

.author-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
  margin: 12px 0 6px;
}

.author-bio {
  font-size: 13px;
  color: var(--color-text-muted);
  margin: 0 0 20px;
}

.author-stats {
  display: flex;
  justify-content: center;
  gap: 40px;
  padding-top: 20px;
  border-top: 1px solid var(--color-border-light);
}

.author-stat {
  text-align: center;
}

.author-stat .num {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text);
}

.author-stat .label {
  font-size: 12px;
  color: var(--color-text-muted);
}

/* ==================== 标签云 ==================== */
.tags-cloud {
  padding: 20px 24px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  padding: 6px 14px;
  background: var(--color-bg-subtle);
  color: var(--color-text-secondary);
  border-radius: var(--radius-full);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tag-item:hover {
  background: var(--color-primary);
  color: white;
  transform: translateY(-2px);
  box-shadow: var(--shadow-primary);
}

/* ==================== 快捷链接 ==================== */
.links {
  padding: 12px 16px;
}

.link-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--color-text-secondary);
  text-decoration: none;
  width: 100%;
  font-size: 14px;
  font-weight: 500;
}

.link-item:hover {
  background: var(--color-bg-subtle);
  color: var(--color-text);
}

.link-icon {
  font-size: 16px;
}

.link-arrow {
  margin-left: auto;
  color: var(--color-text-muted);
  transition: transform var(--transition-fast);
}

.link-item:hover .link-arrow {
  transform: translateX(4px);
  color: var(--color-primary);
}

/* ==================== 分页 ==================== */
.pagination {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* ==================== 响应式 ==================== */
@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
    gap: 40px;
  }

  .sidebar {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }

  .sidebar-widget:first-child {
    grid-column: span 2;
  }
}

@media (max-width: 768px) {
  .hero {
    padding: 60px var(--spacing-md) 50px;
  }

  .hero-title {
    font-size: 40px;
    letter-spacing: -1px;
  }

  .hero-subtitle {
    font-size: 16px;
  }

  .hero-actions {
    flex-direction: column;
    gap: 12px;
  }

  .hero-btn {
    width: 100%;
    justify-content: center;
  }

  .hero-stats {
    gap: 24px;
  }

  .stat-value {
    font-size: 28px;
  }

  .content-section {
    padding: var(--spacing-xl) var(--spacing-md);
  }

  .article-card-inner {
    padding: 20px 0;
  }

  .article-title {
    font-size: 18px;
  }

  .article-arrow {
    display: none;
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

  .hero-badge {
    font-size: 12px;
    padding: 6px 12px;
  }
}
</style>
