<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { commentApi } from '@/api/comment'
import { articleApi } from '@/api/article'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate } from '@/utils/format'
import type { Comment, Article } from '@/types'
import { Check, Close, Delete, ChatDotRound } from '@element-plus/icons-vue'

const comments = ref<Comment[]>([])
const articles = ref<Map<number, Article>>(new Map())
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const statusFilter = ref<string>('')

const fetchComments = async () => {
  loading.value = true
  try {
    const res = await commentApi.adminGetComments({
      page: page.value,
      size: pageSize.value,
      status: statusFilter.value || undefined
    })
    if (res.code === 200) {
      comments.value = res.data.records
      total.value = res.data.total

      // 获取文章标题
      for (const comment of comments.value) {
        if (comment.articleId && !articles.value.has(comment.articleId)) {
          try {
            const articleRes = await articleApi.getArticleById(comment.articleId)
            if (articleRes.code === 200) {
              articles.value.set(comment.articleId, articleRes.data)
            }
          } catch (e) {
            console.error(e)
          }
        }
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleApprove = async (comment: Comment) => {
  try {
    const res = await commentApi.adminApproveComment(comment.id)
    if (res.code === 200) {
      ElMessage.success('已通过')
      fetchComments()
    }
  } catch (e) {
    console.error(e)
  }
}

const handleReject = async (comment: Comment) => {
  try {
    const res = await commentApi.adminRejectComment(comment.id)
    if (res.code === 200) {
      ElMessage.success('已拒绝')
      fetchComments()
    }
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = async (comment: Comment) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条评论吗？此操作不可恢复。',
      '删除评论',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res = await commentApi.adminDeleteComment(comment.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchComments()
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  fetchComments()
}

const handleFilterChange = () => {
  page.value = 1
  fetchComments()
}

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    APPROVED: '#10b981',
    PENDING: '#f59e0b',
    REJECTED: '#ef4444',
    SPAM: '#6b7280'
  }
  return colors[status] || '#6b7280'
}

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    APPROVED: '已通过',
    PENDING: '待审核',
    REJECTED: '已拒绝',
    SPAM: '垃圾'
  }
  return labels[status] || status
}

const getArticleTitle = (articleId: number) => {
  return articles.value.get(articleId)?.title || `文章 #${articleId}`
}

const getCommentAuthor = (comment: Comment) => {
  if (comment.authorName) return comment.authorName
  if (comment.guestName) return comment.guestName
  if (comment.guestEmail) return comment.guestEmail
  return '匿名'
}

onMounted(() => {
  fetchComments()
})
</script>

<template>
  <div class="comment-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>评论管理</h1>
        <p>共 {{ total }} 条评论</p>
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
          class="filter-tab pending"
          :class="{ active: statusFilter === 'PENDING' }"
          @click="statusFilter = 'PENDING'; handleFilterChange()"
        >
          待审核
        </button>
        <button
          class="filter-tab"
          :class="{ active: statusFilter === 'APPROVED' }"
          @click="statusFilter = 'APPROVED'; handleFilterChange()"
        >
          已通过
        </button>
        <button
          class="filter-tab"
          :class="{ active: statusFilter === 'REJECTED' }"
          @click="statusFilter = 'REJECTED'; handleFilterChange()"
        >
          已拒绝
        </button>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="comments-container" v-loading="loading">
      <div
        v-for="comment in comments"
        :key="comment.id"
        class="comment-card"
      >
        <!-- 头部 -->
        <div class="comment-header">
          <div class="comment-author">
            <div class="author-avatar">
              {{ getCommentAuthor(comment).charAt(0) }}
            </div>
            <div class="author-info">
              <span class="author-name">{{ getCommentAuthor(comment) }}</span>
              <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
            </div>
          </div>
          <span
            class="status-badge"
            :style="{ background: `${getStatusColor(comment.status)}15`, color: getStatusColor(comment.status) }"
          >
            {{ getStatusLabel(comment.status) }}
          </span>
        </div>

        <!-- 内容 -->
        <div class="comment-body">
          <p class="comment-content">{{ comment.content }}</p>
          <div class="comment-article">
            <ChatDotRound :size="14" />
            <span>评论于 {{ getArticleTitle(comment.articleId) }}</span>
          </div>
        </div>

        <!-- 操作 -->
        <div class="comment-actions">
          <button
            v-if="comment.status !== 'APPROVED'"
            class="action-btn approve"
            @click="handleApprove(comment)"
          >
            <el-icon><Check /></el-icon>
            <span>通过</span>
          </button>
          <button
            v-if="comment.status !== 'REJECTED'"
            class="action-btn reject"
            @click="handleReject(comment)"
          >
            <el-icon><Close /></el-icon>
            <span>拒绝</span>
          </button>
          <button
            class="action-btn delete"
            @click="handleDelete(comment)"
          >
            <el-icon><Delete /></el-icon>
            <span>删除</span>
          </button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && comments.length === 0" class="empty-state">
        <div class="empty-icon">💬</div>
        <h3>暂无评论</h3>
        <p>还没有收到任何评论</p>
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
.comment-list {
  max-width: 1200px;
  margin: 0 auto;
}

/* 页面头部 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
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

/* 筛选标签 */
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

.filter-tab.pending {
  position: relative;
}

.filter-tab.pending::after {
  content: '';
  width: 6px;
  height: 6px;
  background: #f59e0b;
  border-radius: 50%;
  position: absolute;
  top: 6px;
  right: 6px;
}

/* 评论容器 */
.comments-container {
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 16px;
  overflow: hidden;
}

[data-theme="dark"] .comments-container {
  background: #1a1a1a;
  border-color: #333;
}

/* 评论卡片 */
.comment-card {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.2s ease;
}

[data-theme="dark"] .comment-card {
  border-bottom-color: #2a2a2a;
}

.comment-card:last-child {
  border-bottom: none;
}

.comment-card:hover {
  background: #f8f9fa;
}

[data-theme="dark"] .comment-card:hover {
  background: #2a2a2a;
}

/* 评论头部 */
.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.comment-author {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
  color: #667eea;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
}

[data-theme="dark"] .author-name {
  color: #fff;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

/* 评论内容 */
.comment-body {
  margin-bottom: 16px;
}

.comment-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin: 0 0 12px;
}

[data-theme="dark"] .comment-content {
  color: #ccc;
}

.comment-article {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #667eea;
}

/* 评论操作 */
.comment-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 8px;
  border: 1px solid #e5e5e5;
  background: #fff;
  color: #666;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
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

.action-btn.approve:hover {
  border-color: #10b981;
  color: #10b981;
  background: rgba(16, 185, 129, 0.05);
}

.action-btn.reject:hover {
  border-color: #f59e0b;
  color: #f59e0b;
  background: rgba(245, 158, 11, 0.05);
}

.action-btn.delete:hover {
  border-color: #ef4444;
  color: #ef4444;
  background: rgba(239, 68, 68, 0.05);
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
  margin: 0;
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
  }

  .filter-tabs {
    width: 100%;
    justify-content: center;
  }

  .comment-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .comment-actions {
    flex-wrap: wrap;
  }

  .action-btn span {
    display: none;
  }
}
</style>
