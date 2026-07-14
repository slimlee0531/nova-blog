<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate } from '@/utils/format'
import type { Article } from '@/types'
import { Plus, Search, EditPen, Delete, View } from '@element-plus/icons-vue'

const router = useRouter()
const articles = ref<Article[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const searchText = ref('')
const statusFilter = ref('')
const deleteLoading = ref<number | null>(null)

const fetchArticles = async () => {
  loading.value = true
  try {
    const params: any = {
      page: page.value,
      size: pageSize.value
    }
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    const res = await articleApi.adminGetArticles(params)
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

const handleCreate = () => {
  router.push('/admin/article/edit')
}

const handleEdit = (id: number) => {
  router.push(`/admin/article/edit/${id}`)
}

const handleDelete = async (article: Article) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除「${article.title}」吗？此操作不可恢复。`,
      '删除文章',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    deleteLoading.value = article.id
    const res = await articleApi.adminDeleteArticle(article.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchArticles()
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  } finally {
    deleteLoading.value = null
  }
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  fetchArticles()
}

const handleFilterChange = () => {
  page.value = 1
  fetchArticles()
}

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

const filteredArticles = () => {
  if (!searchText.value) return articles.value
  return articles.value.filter(a =>
    a.title.toLowerCase().includes(searchText.value.toLowerCase())
  )
}

onMounted(() => {
  fetchArticles()
})
</script>

<template>
  <div class="article-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>文章管理</h1>
        <p>共 {{ total }} 篇文章</p>
      </div>
      <button class="create-btn" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        <span>新建文章</span>
      </button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="search-box">
        <el-icon class="search-icon"><Search /></el-icon>
        <input
          v-model="searchText"
          type="text"
          placeholder="搜索文章标题..."
          @input="handleFilterChange"
        />
      </div>
      <div class="filter-tabs">
        <button
          class="filter-tab"
          :class="{ active: statusFilter === '' }"
          @click="statusFilter = ''; handleFilterChange()"
        >
          全部
        </button>
        <button
          class="filter-tab"
          :class="{ active: statusFilter === 'PUBLISHED' }"
          @click="statusFilter = 'PUBLISHED'; handleFilterChange()"
        >
          已发布
        </button>
        <button
          class="filter-tab"
          :class="{ active: statusFilter === 'DRAFT' }"
          @click="statusFilter = 'DRAFT'; handleFilterChange()"
        >
          草稿
        </button>
      </div>
    </div>

    <!-- 文章列表 -->
    <div class="articles-card" v-loading="loading">
      <div
        v-for="article in filteredArticles()"
        :key="article.id"
        class="article-item"
      >
        <!-- 缩略图 -->
        <div
          class="article-thumb"
          v-if="article.featuredImage"
          :style="{ backgroundImage: `url(${article.featuredImage})` }"
        ></div>
        <div class="article-thumb placeholder" v-else>
          <span>📝</span>
        </div>

        <!-- 内容 -->
        <div class="article-content">
          <div class="article-header">
            <h3 class="article-title" @click="handleEdit(article.id)">{{ article.title }}</h3>
            <span
              class="status-badge"
              :style="{ background: `${getStatusColor(article.status)}15`, color: getStatusColor(article.status) }"
            >
              {{ getStatusLabel(article.status) }}
            </span>
          </div>

          <div class="article-meta">
            <span class="meta-item" v-if="article.categoryName">
              📁 {{ article.categoryName }}
            </span>
            <span class="meta-item">
              📅 {{ formatDate(article.publishedAt || article.createdAt) }}
            </span>
            <span class="meta-item">
              <el-icon><View /></el-icon>
              {{ article.viewCount || 0 }}
            </span>
          </div>

          <p class="article-summary" v-if="article.summary">
            {{ article.summary }}
          </p>
        </div>

        <!-- 操作 -->
        <div class="article-actions">
          <button
            class="action-btn edit"
            @click="handleEdit(article.id)"
            title="编辑"
          >
            <el-icon><EditPen /></el-icon>
          </button>
          <button
            class="action-btn delete"
            @click="handleDelete(article)"
            :disabled="deleteLoading === article.id"
            title="删除"
          >
            <el-icon><Delete /></el-icon>
          </button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && filteredArticles().length === 0" class="empty-state">
        <div class="empty-icon">📝</div>
        <h3>暂无文章</h3>
        <p>开始创作你的第一篇文章吧</p>
        <button class="create-btn" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          <span>新建文章</span>
        </button>
      </div>
    </div>

    <!-- 分页 -->
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
</template>

<style scoped>
.article-list {
  max-width: 1200px;
  margin: 0 auto;
}

/* 页面头部 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.header-content h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 4px;
}

[data-theme="dark"] .header-content h1 {
  color: #fff;
}

.header-content p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.create-btn:hover {
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transform: translateY(-1px);
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 10px;
  flex: 1;
  max-width: 320px;
}

[data-theme="dark"] .search-box {
  background: #1a1a1a;
  border-color: #333;
}

.search-icon {
  color: #999;
}

.search-box input {
  flex: 1;
  border: none;
  background: none;
  font-size: 14px;
  color: #1a1a1a;
  outline: none;
}

[data-theme="dark"] .search-box input {
  color: #fff;
}

.search-box input::placeholder {
  color: #999;
}

.filter-tabs {
  display: flex;
  gap: 4px;
  background: #f5f5f5;
  padding: 4px;
  border-radius: 8px;
}

[data-theme="dark"] .filter-tabs {
  background: #2a2a2a;
}

.filter-tab {
  padding: 8px 16px;
  background: none;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  transition: all 0.2s ease;
}

[data-theme="dark"] .filter-tab {
  color: #999;
}

.filter-tab:hover {
  color: #1a1a1a;
}

[data-theme="dark"] .filter-tab:hover {
  color: #fff;
}

.filter-tab.active {
  background: #fff;
  color: #1a1a1a;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

[data-theme="dark"] .filter-tab.active {
  background: #1a1a1a;
  color: #fff;
}

/* 文章卡片 */
.articles-card {
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 16px;
  overflow: hidden;
}

[data-theme="dark"] .articles-card {
  background: #1a1a1a;
  border-color: #333;
}

.article-item {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.2s ease;
}

[data-theme="dark"] .article-item {
  border-bottom-color: #2a2a2a;
}

.article-item:last-child {
  border-bottom: none;
}

.article-item:hover {
  background: #f8f9fa;
}

[data-theme="dark"] .article-item:hover {
  background: #2a2a2a;
}

.article-thumb {
  width: 80px;
  height: 80px;
  border-radius: 10px;
  background-size: cover;
  background-position: center;
  flex-shrink: 0;
}

.article-thumb.placeholder {
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

[data-theme="dark"] .article-thumb.placeholder {
  background: #2a2a2a;
}

.article-content {
  flex: 1;
  min-width: 0;
}

.article-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.article-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  cursor: pointer;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

[data-theme="dark"] .article-title {
  color: #fff;
}

.article-title:hover {
  color: #667eea;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  flex-shrink: 0;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #666;
}

[data-theme="dark"] .meta-item {
  color: #999;
}

.article-summary {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

[data-theme="dark"] .article-summary {
  color: #999;
}

/* 操作按钮 */
.article-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.action-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: 1px solid #e5e5e5;
  background: #fff;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 16px;
}

[data-theme="dark"] .action-btn {
  background: #2a2a2a;
  border-color: #333;
  color: #999;
}

.action-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.action-btn.delete:hover {
  border-color: #ef4444;
  color: #ef4444;
  background: rgba(239, 68, 68, 0.05);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}

[data-theme="dark"] .empty-state h3 {
  color: #fff;
}

.empty-state p {
  font-size: 14px;
  color: #999;
  margin: 0 0 24px;
}

/* 分页 */
.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .filter-bar {
    flex-direction: column;
  }

  .search-box {
    max-width: 100%;
    width: 100%;
  }

  .filter-tabs {
    width: 100%;
    justify-content: center;
  }

  .article-item {
    flex-direction: column;
  }

  .article-thumb {
    width: 100%;
    height: 120px;
  }

  .article-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .article-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
