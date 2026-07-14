<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { commentApi } from '@/api/comment'
import { articleApi } from '@/api/article'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Comment, Article } from '@/types'
import { formatDate } from '@/utils/format'

const comments = ref<Comment[]>([])
const articles = ref<Map<number, Article>>(new Map())
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const statusFilter = ref<string>('')
const articleFilter = ref<number | undefined>(undefined)

const fetchComments = async () => {
  loading.value = true
  try {
    const res = await commentApi.adminGetComments({
      page: page.value,
      size: pageSize.value,
      status: statusFilter.value || undefined,
      articleId: articleFilter.value
    })
    if (res.code === 200) {
      comments.value = res.data.records
      total.value = res.data.total
      // 获取文章标题
      for (const comment of comments.value) {
        if (comment.articleId && !articles.value.has(comment.articleId)) {
          try {
            const articleRes = await articleApi.getArticle(comment.articleId)
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

const handleApprove = async (id: number) => {
  try {
    const res = await commentApi.adminApproveComment(id)
    if (res.code === 200) {
      ElMessage.success('已通过')
      fetchComments()
    }
  } catch (e) {
    console.error(e)
  }
}

const handleReject = async (id: number) => {
  try {
    const res = await commentApi.adminRejectComment(id)
    if (res.code === 200) {
      ElMessage.success('已拒绝')
      fetchComments()
    }
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      type: 'warning'
    })
    const res = await commentApi.adminDeleteComment(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchComments()
    }
  } catch (e) {
    console.error(e)
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

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    'APPROVED': 'success',
    'PENDING': 'warning',
    'REJECTED': 'danger',
    'SPAM': 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    'APPROVED': '已通过',
    'PENDING': '待审核',
    'REJECTED': '已拒绝',
    'SPAM': '垃圾'
  }
  return map[status] || status
}

const getArticleTitle = (articleId: number) => {
  return articles.value.get(articleId)?.title || `文章 #${articleId}`
}

const getCommentAuthor = (comment: Comment) => {
  if (comment.userName) return comment.userName
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
    <div class="list-header">
      <h2>评论管理</h2>
      <div class="filters">
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="handleFilterChange" style="width: 120px">
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已拒绝" value="REJECTED" />
          <el-option label="垃圾" value="SPAM" />
        </el-select>
      </div>
    </div>

    <el-table :data="comments" v-loading="loading" style="width: 100%">
      <el-table-column label="评论者" width="150">
        <template #default="{ row }">
          <div class="author-cell">
            <el-avatar :size="28">{{ getCommentAuthor(row).charAt(0) }}</el-avatar>
            <span>{{ getCommentAuthor(row) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" min-width="300">
        <template #default="{ row }">
          <div class="content-cell">{{ row.content }}</div>
        </template>
      </el-table-column>
      <el-table-column label="文章" width="200">
        <template #default="{ row }">
          <span class="article-title">{{ getArticleTitle(row.articleId) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status !== 'APPROVED'" type="success" link @click="handleApprove(row.id)">通过</el-button>
          <el-button v-if="row.status !== 'REJECTED'" type="warning" link @click="handleReject(row.id)">拒绝</el-button>
          <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

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
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.list-header h2 {
  margin: 0;
}

.filters {
  display: flex;
  gap: 12px;
}

.author-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.content-cell {
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--color-text-secondary);
  line-height: 1.5;
}

.article-title {
  color: var(--color-primary);
  font-size: 13px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
