<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { commentApi } from '@/api/comment'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const article = ref<any>(null)
const comments = ref<any[]>([])
const loading = ref(false)
const commentContent = ref('')
const submitting = ref(false)

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return dateStr.substring(0, 10)
}

const fetchArticle = async () => {
  loading.value = true
  try {
    const slug = route.params.slug as string
    const res: any = await articleApi.getArticleBySlug(slug)
    if (res.code === 200) {
      article.value = res.data
      fetchComments()
    } else {
      ElMessage.error('文章不存在')
      router.push('/')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载文章失败')
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  if (!article.value) return
  try {
    const res: any = await commentApi.getCommentsByArticle(article.value.id)
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
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  submitting.value = true
  try {
    const res: any = await commentApi.createComment({
      articleId: article.value.id,
      content: commentContent.value
    })
    if (res.code === 200) {
      ElMessage.success('评论成功，等待审核')
      const newComment = {
        id: res.data?.id || Date.now(),
        content: commentContent.value,
        authorName: userStore.userInfo?.displayName || userStore.userInfo?.username || '我',
        createdAt: new Date().toISOString(),
        status: 'pending'
      }
      comments.value.unshift(newComment)
      commentContent.value = ''
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('评论失败')
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchArticle()
})
</script>

<template>
  <div class="article-page">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <!-- 文章内容 -->
    <template v-else-if="article">
      <!-- 顶部导航 -->
      <header class="page-header">
        <div class="header-content">
          <el-button text @click="goBack" class="back-btn">
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
          <div class="header-actions">
            <el-button text circle>
              <el-icon><Share /></el-icon>
            </el-button>
            <el-button text circle>
              <el-icon><Star /></el-icon>
            </el-button>
          </div>
        </div>
      </header>

      <!-- 文章头部 -->
      <div class="article-hero">
        <div class="hero-content">
          <div class="article-meta">
            <span class="category" v-if="article.categoryName">{{ article.categoryName }}</span>
            <span class="date">{{ formatDate(article.publishedAt) }}</span>
            <span class="reading-time" v-if="article.aiReadingTime">{{ article.aiReadingTime }}分钟阅读</span>
          </div>
          <h1 class="article-title">{{ article.title }}</h1>
          <div class="author-info">
            <el-avatar :size="48" :src="article.authorAvatar">
              {{ article.authorName?.charAt(0) || 'A' }}
            </el-avatar>
            <div class="author-detail">
              <span class="author-name">{{ article.authorName || '匿名' }}</span>
              <span class="author-bio">发布于 {{ formatDate(article.publishedAt) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 文章正文 -->
      <div class="article-body">
        <div class="body-content" v-html="article.content"></div>
      </div>

      <!-- 文章底部 -->
      <div class="article-footer">
        <div class="footer-content">
          <!-- 标签 -->
          <div class="article-tags" v-if="article.tags?.length">
            <span class="tag-label">标签：</span>
            <span v-for="tag in article.tags" :key="tag.id" class="tag">{{ tag.name }}</span>
          </div>

          <!-- 互动按钮 -->
          <div class="interactions">
            <button class="interaction-btn">
              <el-icon><Star /></el-icon>
              <span>收藏</span>
            </button>
            <button class="interaction-btn">
              <el-icon><ChatDotRound /></el-icon>
              <span>评论 {{ comments.length }}</span>
            </button>
            <button class="interaction-btn">
              <el-icon><Share /></el-icon>
              <span>分享</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comments-section">
        <div class="comments-content">
          <h3 class="comments-title">
            <span class="title-icon">💬</span> 评论 ({{ comments.length }})
          </h3>

          <!-- 评论输入框 -->
          <div class="comment-form" v-if="userStore.token">
            <div class="form-header">
              <el-avatar :size="40" :src="userStore.userInfo?.avatarUrl">
                {{ userStore.userInfo?.username?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="form-label">发表评论</span>
            </div>
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="4"
              placeholder="写下你的想法..."
              :disabled="submitting"
            />
            <div class="form-footer">
              <el-button
                type="primary"
                @click="submitComment"
                :loading="submitting"
                round
              >
                发布评论
              </el-button>
            </div>
          </div>
          <div class="login-hint" v-else>
            <p>登录后即可参与评论</p>
            <el-button type="primary" round @click="router.push('/login')">立即登录</el-button>
          </div>

          <!-- 评论列表 -->
          <div class="comments-list">
            <TransitionGroup name="comment" tag="div">
              <div v-for="comment in comments" :key="comment.id" class="comment-item">
                <div class="comment-header">
                  <el-avatar :size="40">
                    {{ comment.authorName?.charAt(0) || 'A' }}
                  </el-avatar>
                  <div class="comment-info">
                    <span class="comment-author">{{ comment.authorName || '匿名' }}</span>
                    <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
                  </div>
                </div>
                <div class="comment-body">
                  {{ comment.content }}
                </div>
                <div class="comment-status" v-if="comment.status === 'pending'">
                  <el-icon><Clock /></el-icon> 等待审核
                </div>
              </div>
            </TransitionGroup>

            <el-empty v-if="comments.length === 0" description="暂无评论，快来发表第一条评论吧！" />
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.article-page {
  min-height: 100vh;
  background: #f8fafc;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  color: #64748b;
}

.loading-state p {
  margin-top: 16px;
}

/* 顶部导航 */
.page-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #e2e8f0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 12px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.back-btn {
  font-size: 16px;
  color: #475569;
}

.back-btn:hover {
  color: #667eea;
}

.header-actions {
  display: flex;
  gap: 8px;
}

/* 文章头部 */
.article-hero {
  background: white;
  border-bottom: 1px solid #e2e8f0;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 60px 24px 40px;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.category {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.date,
.reading-time {
  color: #64748b;
  font-size: 14px;
}

.article-title {
  font-size: 36px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 24px 0;
  line-height: 1.3;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.author-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.author-bio {
  font-size: 14px;
  color: #64748b;
}

/* 文章正文 */
.article-body {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 24px;
}

.body-content {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  line-height: 1.8;
  color: #334155;
  font-size: 16px;
}

.body-content :deep(h1),
.body-content :deep(h2),
.body-content :deep(h3) {
  color: #1e293b;
  margin-top: 32px;
  margin-bottom: 16px;
}

.body-content :deep(h2) {
  font-size: 24px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e2e8f0;
}

.body-content :deep(p) {
  margin-bottom: 20px;
}

.body-content :deep(code) {
  background: #f1f5f9;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 14px;
  color: #e11d48;
}

.body-content :deep(pre) {
  background: #1e293b;
  color: #e2e8f0;
  padding: 20px;
  border-radius: 12px;
  overflow-x: auto;
  margin: 24px 0;
}

.body-content :deep(pre code) {
  background: none;
  color: inherit;
  padding: 0;
}

.body-content :deep(blockquote) {
  border-left: 4px solid #667eea;
  background: #f8fafc;
  padding: 16px 24px;
  margin: 24px 0;
  border-radius: 0 12px 12px 0;
}

.body-content :deep(img) {
  max-width: 100%;
  border-radius: 12px;
  margin: 24px 0;
}

/* 文章底部 */
.article-footer {
  background: white;
  border-top: 1px solid #e2e8f0;
  border-bottom: 1px solid #e2e8f0;
}

.footer-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.article-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
}

.tag-label {
  color: #64748b;
}

.tag {
  background: #f1f5f9;
  color: #475569;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
}

.interactions {
  display: flex;
  justify-content: center;
  gap: 24px;
}

.interaction-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  border: none;
  background: #f8fafc;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #64748b;
}

.interaction-btn:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
}

.interaction-btn .el-icon {
  font-size: 24px;
}

/* 评论区 */
.comments-section {
  background: white;
  margin-top: 24px;
}

.comments-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 24px;
}

.comments-title {
  font-size: 22px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 30px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  font-size: 26px;
}

/* 评论表单 */
.comment-form {
  background: #f8fafc;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 40px;
}

.form-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.form-label {
  font-weight: 500;
  color: #1e293b;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.login-hint {
  text-align: center;
  padding: 40px;
  background: #f8fafc;
  border-radius: 16px;
  margin-bottom: 40px;
}

.login-hint p {
  color: #64748b;
  margin-bottom: 16px;
}

/* 评论列表 */
.comments-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.comment-item {
  background: #f8fafc;
  border-radius: 16px;
  padding: 20px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.comment-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.comment-author {
  font-weight: 500;
  color: #1e293b;
}

.comment-time {
  font-size: 13px;
  color: #94a3b8;
}

.comment-body {
  color: #475569;
  line-height: 1.6;
}

.comment-status {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  font-size: 13px;
  color: #f59e0b;
}

/* 评论动画 */
.comment-enter-active,
.comment-leave-active {
  transition: all 0.3s ease;
}

.comment-enter-from,
.comment-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-content {
    padding: 40px 20px 30px;
  }

  .article-title {
    font-size: 28px;
  }

  .article-meta {
    flex-wrap: wrap;
  }

  .body-content {
    padding: 24px 20px;
  }

  .interactions {
    flex-wrap: wrap;
  }

  .interaction-btn {
    flex: 1;
    min-width: 80px;
  }

  .comments-content {
    padding: 30px 16px;
  }

  .comment-form {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .article-title {
    font-size: 24px;
  }

  .author-info {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
