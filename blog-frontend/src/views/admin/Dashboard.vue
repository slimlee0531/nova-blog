<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { commentApi } from '@/api/comment'
import { bookmarkApi } from '@/api/bookmark'
import { formatDate } from '@/utils/format'
import type { Article } from '@/types'
import {
  Document,
  View,
  ChatDotRound,
  Star,
  EditPen,
  ArrowRight
} from '@element-plus/icons-vue'

const router = useRouter()

const stats = ref({
  totalArticles: 0,
  publishedArticles: 0,
  draftArticles: 0,
  totalViews: 0,
  totalComments: 0,
  totalBookmarks: 0
})

const recentArticles = ref<Article[]>([])
const loading = ref(false)

const fetchStats = async () => {
  loading.value = true
  try {
    // 获取文章统计
    const allRes = await articleApi.adminGetArticles({ page: 1, size: 1 })
    if (allRes.code === 200) {
      stats.value.totalArticles = allRes.data.total
    }

    const publishedRes = await articleApi.adminGetArticles({ page: 1, size: 1, status: 'PUBLISHED' })
    if (publishedRes.code === 200) {
      stats.value.publishedArticles = publishedRes.data.total
    }

    const draftRes = await articleApi.adminGetArticles({ page: 1, size: 1, status: 'DRAFT' })
    if (draftRes.code === 200) {
      stats.value.draftArticles = draftRes.data.total
    }

    // 获取最近文章
    const recentRes = await articleApi.adminGetArticles({ page: 1, size: 5 })
    if (recentRes.code === 200) {
      recentArticles.value = recentRes.data.records
    }

    // 获取评论统计（简化处理）
    stats.value.totalComments = 0
    stats.value.totalViews = 0
    stats.value.totalBookmarks = 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const statCards = [
  { key: 'totalArticles', label: '文章总数', icon: Document, color: '#667eea' },
  { key: 'publishedArticles', label: '已发布', icon: View, color: '#10b981' },
  { key: 'draftArticles', label: '草稿', icon: EditPen, color: '#f59e0b' },
  { key: 'totalViews', label: '总浏览', icon: View, color: '#8b5cf6' },
  { key: 'totalComments', label: '总评论', icon: ChatDotRound, color: '#ec4899' },
  { key: 'totalBookmarks', label: '总收藏', icon: Star, color: '#f97316' }
]

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    PUBLISHED: '#10b981',
    DRAFT: '#f59e0b',
    ARCHIVED: '#6b7280'
  }
  return colors[status] || '#6b7280'
}

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    PUBLISHED: '已发布',
    DRAFT: '草稿',
    ARCHIVED: '已归档'
  }
  return labels[status] || status
}

onMounted(() => {
  fetchStats()
})
</script>

<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div
        v-for="card in statCards"
        :key="card.key"
        class="stat-card"
      >
        <div class="stat-icon" :style="{ background: `${card.color}15`, color: card.color }">
          <el-icon :size="24"><component :is="card.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats[card.key as keyof typeof stats] }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </div>
    </div>

    <!-- 最近文章 -->
    <div class="section-card">
      <div class="section-header">
        <h3>最近文章</h3>
        <button class="view-all-btn" @click="router.push('/admin/articles')">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </button>
      </div>

      <div class="articles-table" v-loading="loading">
        <div class="table-header">
          <div class="col-title">标题</div>
          <div class="col-status">状态</div>
          <div class="col-date">发布时间</div>
          <div class="col-stats">数据</div>
        </div>

        <div
          v-for="article in recentArticles"
          :key="article.id"
          class="table-row"
          @click="router.push(`/admin/article/edit/${article.id}`)"
        >
          <div class="col-title">
            <div class="article-title">{{ article.title }}</div>
            <div class="article-category" v-if="article.categoryName">{{ article.categoryName }}</div>
          </div>
          <div class="col-status">
            <span
              class="status-badge"
              :style="{ background: `${getStatusColor(article.status)}15`, color: getStatusColor(article.status) }"
            >
              {{ getStatusLabel(article.status) }}
            </span>
          </div>
          <div class="col-date">
            {{ formatDate(article.publishedAt || article.createdAt) }}
          </div>
          <div class="col-stats">
            <span class="stat-item">👁 {{ article.viewCount || 0 }}</span>
            <span class="stat-item">💬 {{ article.commentCount || 0 }}</span>
          </div>
        </div>

        <div v-if="!loading && recentArticles.length === 0" class="empty-state">
          暂无文章，开始创作吧
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h3>快捷操作</h3>
      <div class="actions-grid">
        <button class="action-card" @click="router.push('/admin/article/edit')">
          <div class="action-icon" style="background: #667eea15; color: #667eea">
            <el-icon :size="24"><EditPen /></el-icon>
          </div>
          <span>新建文章</span>
        </button>
        <button class="action-card" @click="router.push('/admin/articles')">
          <div class="action-icon" style="background: #10b98115; color: #10b981">
            <el-icon :size="24"><Document /></el-icon>
          </div>
          <span>管理文章</span>
        </button>
        <button class="action-card" @click="router.push('/admin/categories')">
          <div class="action-icon" style="background: #f59e0b15; color: #f59e0b">
            <el-icon :size="24"><Folder /></el-icon>
          </div>
          <span>管理分类</span>
        </button>
        <button class="action-card" @click="router.push('/admin/comments')">
          <div class="action-icon" style="background: #ec489915; color: #ec4899">
            <el-icon :size="24"><ChatDotRound /></el-icon>
          </div>
          <span>审核评论</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #e5e5e5;
  transition: all 0.2s ease;
}

[data-theme="dark"] .stat-card {
  background: #1a1a1a;
  border-color: #333;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: -0.5px;
  line-height: 1;
}

[data-theme="dark"] .stat-value {
  color: #fff;
}

.stat-label {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}

/* 区块卡片 */
.section-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e5e5e5;
  overflow: hidden;
  margin-bottom: 24px;
}

[data-theme="dark"] .section-card {
  background: #1a1a1a;
  border-color: #333;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e5e5;
}

[data-theme="dark"] .section-header {
  border-bottom-color: #333;
}

.section-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

[data-theme="dark"] .section-header h3 {
  color: #fff;
}

.view-all-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: none;
  border: none;
  color: #667eea;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.view-all-btn:hover {
  background: #667eea10;
}

/* 表格 */
.articles-table {
  padding: 0 24px 24px;
}

.table-header {
  display: grid;
  grid-template-columns: 1fr 100px 120px 140px;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #e5e5e5;
}

[data-theme="dark"] .table-header {
  border-bottom-color: #333;
}

.table-header span {
  font-size: 12px;
  font-weight: 600;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.table-row {
  display: grid;
  grid-template-columns: 1fr 100px 120px 140px;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.2s ease;
}

[data-theme="dark"] .table-row {
  border-bottom-color: #2a2a2a;
}

.table-row:last-child {
  border-bottom: none;
}

.table-row:hover {
  background: #f8f9fa;
}

[data-theme="dark"] .table-row:hover {
  background: #2a2a2a;
}

.article-title {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 4px;
}

[data-theme="dark"] .article-title {
  color: #fff;
}

.article-category {
  font-size: 12px;
  color: #667eea;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.col-date {
  font-size: 13px;
  color: #666;
}

[data-theme="dark"] .col-date {
  color: #999;
}

.col-stats {
  display: flex;
  gap: 12px;
}

.stat-item {
  font-size: 13px;
  color: #666;
}

[data-theme="dark"] .stat-item {
  color: #999;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 14px;
}

/* 快捷操作 */
.quick-actions h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 16px;
}

[data-theme="dark"] .quick-actions h3 {
  color: #fff;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-card {
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

[data-theme="dark"] .action-card {
  background: #1a1a1a;
  border-color: #333;
}

.action-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-card span {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
}

[data-theme="dark"] .action-card span {
  color: #fff;
}

/* 响应式 */
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .table-header,
  .table-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .table-header {
    display: none;
  }

  .col-date,
  .col-stats {
    font-size: 12px;
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
