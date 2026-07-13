<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { commentApi } from '@/api/comment'
import { useUserStore } from '@/store/user'
import { marked } from 'marked'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const article = ref<any>(null)
const comments = ref<any[]>([])
const loading = ref(false)
const commentContent = ref('')
const submitting = ref(false)

const fetchArticle = async () => {
  loading.value = true
  try {
    const slug = route.params.slug as string
    const res: any = await articleApi.getArticleBySlug(slug)
    if (res.code === 200) {
      article.value = res.data
      fetchComments()
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  if (!article.value) return
  try {
    const res: any = await commentApi.getByArticle(article.value.id)
    if (res.code === 200) {
      comments.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    const res: any = await commentApi.create({
      articleId: article.value.id,
      content: commentContent.value
    })

    if (res.code === 200) {
      // 立即添加到评论列表
      const newComment = {
        id: res.data?.id || Date.now(),
        content: commentContent.value,
        userName: userStore.userInfo?.displayName || userStore.userInfo?.username || '我',
        createdAt: new Date().toISOString().substring(0, 16),
        status: 'PENDING'
      }
      comments.value.unshift(newComment)
      commentContent.value = ''
      ElMessage.success('评论发表成功')
    }
  } catch (e) {
    ElMessage.error('评论失败')
  } finally {
    submitting.value = false
  }
}

const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  return marked(article.value.content)
})

onMounted(() => {
  fetchArticle()
})
</script>

<template>
  <div class="article-page">
    <div v-loading="loading">
      <template v-if="article">
        <!-- 文章头部 -->
        <div class="article-header">
          <div class="header-meta">
            <el-tag v-if="article.categoryName" effect="dark">{{ article.categoryName }}</el-tag>
          </div>
          <h1 class="article-title">{{ article.title }}</h1>
          <div class="header-info">
            <div class="author-info">
              <el-avatar :size="40" class="author-avatar">
                {{ article.authorName?.charAt(0) || 'A' }}
              </el-avatar>
              <div class="author-detail">
                <div class="author-name">{{ article.authorName || '匿名作者' }}</div>
                <div class="publish-time">
                  {{ article.publishedAt?.substring(0, 10) }} · 阅读 {{ article.viewCount || 0 }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 文章内容 -->
        <div class="article-content">
          <div class="markdown-body" v-html="renderedContent"></div>
        </div>

        <!-- 文章底部互动 -->
        <div class="article-actions">
          <div class="action-item">
            <el-button circle size="large">
              <el-icon size="20"><Star /></el-icon>
            </el-button>
            <span>{{ article.likeCount || 0 }}</span>
          </div>
          <div class="action-item">
            <el-button circle size="large">
              <el-icon size="20"><ChatDotRound /></el-icon>
            </el-button>
            <span>{{ comments.length }}</span>
          </div>
          <div class="action-item">
            <el-button circle size="large">
              <el-icon size="20"><Share /></el-icon>
            </el-button>
            <span>分享</span>
          </div>
        </div>

        <!-- 评论区 -->
        <div class="comment-section">
          <div class="section-header">
            <h3>评论</h3>
            <span class="comment-count">共 {{ comments.length }} 条评论</span>
          </div>

          <!-- 评论输入框 -->
          <div class="comment-form">
            <div class="form-avatar">
              <el-avatar :size="44">
                {{ userStore.userInfo?.username?.charAt(0) || '?' }}
              </el-avatar>
            </div>
            <div class="form-content">
              <el-input
                v-model="commentContent"
                type="textarea"
                :rows="3"
                :placeholder="userStore.token ? '写下你的评论...' : '请先登录后再评论'"
                :disabled="!userStore.token"
                resize="none"
              />
              <div class="form-footer">
                <span v-if="!userStore.token" class="login-tip">
                  <router-link to="/login">登录</router-link> 后参与讨论
                </span>
                <el-button
                  v-else
                  type="primary"
                  :loading="submitting"
                  :disabled="!commentContent.trim()"
                  @click="submitComment"
                >
                  发布评论
                </el-button>
              </div>
            </div>
          </div>

          <!-- 评论列表 -->
          <div class="comment-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-avatar">
                <el-avatar :size="40">
                  {{ (comment.userName || comment.guestName || '匿')?.charAt(0) }}
                </el-avatar>
              </div>
              <div class="comment-body">
                <div class="comment-header">
                  <span class="comment-author">{{ comment.userName || comment.guestName || '匿名用户' }}</span>
                  <span v-if="comment.status === 'PENDING'" class="comment-pending">审核中</span>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
                <div class="comment-footer">
                  <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                  <div class="comment-actions">
                    <el-button text size="small">
                      <el-icon><Star /></el-icon> 赞
                    </el-button>
                    <el-button text size="small">
                      <el-icon><ChatRound /></el-icon> 回复
                    </el-button>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="comments.length === 0" class="empty-comments">
              <el-icon size="48" color="#ddd"><ChatLineSquare /></el-icon>
              <p>还没有评论，来发表第一条评论吧</p>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script lang="ts">
export default {
  methods: {
    formatTime(time: string) {
      if (!time) return ''
      const date = new Date(time)
      const now = new Date()
      const diff = now.getTime() - date.getTime()
      const minutes = Math.floor(diff / 60000)
      const hours = Math.floor(diff / 3600000)
      const days = Math.floor(diff / 86400000)

      if (minutes < 1) return '刚刚'
      if (minutes < 60) return `${minutes}分钟前`
      if (hours < 24) return `${hours}小时前`
      if (days < 7) return `${days}天前`
      return time.substring(0, 10)
    }
  }
}
</script>

<style scoped>
.article-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 40px 20px;
  background: white;
  min-height: calc(100vh - 60px);
}

/* 文章头部 */
.article-header {
  margin-bottom: 40px;
}

.header-meta {
  margin-bottom: 16px;
}

.article-title {
  font-size: 36px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 24px;
  line-height: 1.3;
}

.header-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
}

.author-name {
  font-weight: 600;
  color: #333;
}

.publish-time {
  font-size: 13px;
  color: #999;
  margin-top: 2px;
}

/* 文章内容 */
.article-content {
  margin-bottom: 40px;
}

.markdown-body {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  margin-top: 32px;
  margin-bottom: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.markdown-body :deep(h1) { font-size: 28px; }
.markdown-body :deep(h2) { font-size: 24px; }
.markdown-body :deep(h3) { font-size: 20px; }

.markdown-body :deep(p) {
  margin-bottom: 16px;
}

.markdown-body :deep(code) {
  background: #f7f8fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'SF Mono', Monaco, monospace;
  font-size: 14px;
  color: #e83e8c;
}

.markdown-body :deep(pre) {
  background: #282c34;
  padding: 20px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 24px 0;
}

.markdown-body :deep(pre code) {
  background: none;
  padding: 0;
  color: #abb2bf;
}

.markdown-body :deep(blockquote) {
  margin: 24px 0;
  padding: 16px 20px;
  border-left: 4px solid #667eea;
  background: #f8f9fa;
  border-radius: 0 8px 8px 0;
  color: #555;
}

.markdown-body :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 20px 0;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 24px;
  margin: 16px 0;
}

.markdown-body :deep(li) {
  margin: 8px 0;
}

/* 文章底部互动 */
.article-actions {
  display: flex;
  justify-content: center;
  gap: 48px;
  padding: 32px 0;
  margin-bottom: 40px;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 13px;
}

.action-item .el-button {
  border-color: #eee;
}

.action-item .el-button:hover {
  color: #667eea;
  border-color: #667eea;
}

/* 评论区 */
.comment-section {
  padding-top: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h3 {
  font-size: 20px;
  margin: 0;
}

.comment-count {
  color: #999;
  font-size: 14px;
}

/* 评论表单 */
.comment-form {
  display: flex;
  gap: 16px;
  margin-bottom: 40px;
  padding-bottom: 32px;
  border-bottom: 1px solid #eee;
}

.form-avatar {
  flex-shrink: 0;
}

.form-avatar .el-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.form-content {
  flex: 1;
}

.form-content :deep(.el-textarea__inner) {
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 14px;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.login-tip {
  color: #999;
  font-size: 14px;
}

.login-tip a {
  color: #667eea;
  text-decoration: none;
}

/* 评论列表 */
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.comment-item {
  display: flex;
  gap: 16px;
}

.comment-avatar .el-avatar {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 600;
  color: #333;
}

.comment-pending {
  font-size: 12px;
  color: #ff9500;
  background: #fff8e1;
  padding: 2px 8px;
  border-radius: 4px;
}

.comment-content {
  color: #333;
  line-height: 1.6;
  margin-bottom: 12px;
}

.comment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comment-time {
  font-size: 13px;
  color: #999;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

/* 空评论 */
.empty-comments {
  text-align: center;
  padding: 60px 0;
  color: #999;
}

.empty-comments p {
  margin-top: 16px;
}

/* 响应式 */
@media (max-width: 768px) {
  .article-page {
    padding: 24px 16px;
  }

  .article-title {
    font-size: 26px;
  }

  .article-actions {
    gap: 32px;
  }
}
</style>
