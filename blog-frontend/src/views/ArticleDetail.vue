<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { commentApi } from '@/api/comment'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { formatDate } from '@/utils/format'
import type { Article, Comment } from '@/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const article = ref<Article | null>(null)
const comments = ref<Comment[]>([])
const loading = ref(false)
const commentContent = ref('')
const submitting = ref(false)

const fetchArticle = async () => {
  loading.value = true
  try {
    const slug = route.params.slug as string
    const res = await articleApi.getArticleBySlug(slug)
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
    const res = await commentApi.getCommentsByArticle(article.value.id)
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
    const res = await commentApi.createComment({
      articleId: article.value!.id,
      content: commentContent.value
    })
    if (res.code === 200) {
      ElMessage.success('评论成功，等待审核')
      const newComment: Comment = {
        id: res.data?.id || Date.now(),
        content: commentContent.value,
        authorName: userStore.userInfo?.displayName || userStore.userInfo?.username || '我',
        articleId: article.value!.id,
        status: 'PENDING',
        likeCount: 0,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
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

onMounted(() => {
  fetchArticle()
})
</script>

<template>
  <div class="article-page">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 文章内容 -->
    <template v-else-if="article">
      <!-- 文章头部 -->
      <div class="article-hero">
        <div class="hero-content container">
          <div class="article-meta">
            <span class="category" v-if="article.categoryName">{{ article.categoryName }}</span>
            <span class="date">{{ formatDate(article.publishedAt || article.createdAt) }}</span>
            <span class="reading-time" v-if="article.aiReadingTime">{{ article.aiReadingTime }}分钟阅读</span>
          </div>
          <h1 class="article-title">{{ article.title }}</h1>
          <div class="author-info">
            <el-avatar :size="44">
              {{ article.authorName?.charAt(0) || 'A' }}
            </el-avatar>
            <div class="author-detail">
              <span class="author-name">{{ article.authorName || '匿名' }}</span>
              <span class="author-bio">发布于 {{ formatDate(article.publishedAt || article.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 文章正文 -->
      <div class="article-body container">
        <div class="body-content" v-html="article.content"></div>
      </div>

      <!-- 文章底部 -->
      <div class="article-footer-section">
        <div class="footer-content container">
          <!-- 标签 -->
          <div class="article-tags" v-if="article.tags?.length">
            <span class="tag-label">标签：</span>
            <span v-for="tag in article.tags" :key="tag" class="tag">{{ tag }}</span>
          </div>

          <!-- 互动按钮 -->
          <div class="interactions">
            <button class="interaction-btn">
              <span>⭐</span>
              <span>收藏</span>
            </button>
            <button class="interaction-btn">
              <span>💬</span>
              <span>评论 {{ comments.length }}</span>
            </button>
            <button class="interaction-btn">
              <span>🔗</span>
              <span>分享</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comments-section container">
        <h3 class="comments-title">
          <span>💬</span> 评论 ({{ comments.length }})
        </h3>

        <!-- 评论输入框 -->
        <div class="comment-form" v-if="userStore.token">
          <div class="form-header">
            <el-avatar :size="36">
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
                <el-avatar :size="36">
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
              <div class="comment-status" v-if="comment.status === 'PENDING'">
                ⏳ 等待审核
              </div>
            </div>
          </TransitionGroup>

          <div v-if="comments.length === 0" class="empty-comments">
            暂无评论，快来发表第一条评论吧！
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
/* ==================== 加载状态 ==================== */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  color: var(--color-text-muted);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-state p {
  margin-top: var(--spacing-md);
}

/* ==================== 文章头部 ==================== */
.article-hero {
  background: var(--color-bg-card);
  border-bottom: 1px solid var(--color-border);
}

.hero-content {
  max-width: var(--article-max-width);
  margin: 0 auto;
  padding: var(--spacing-3xl) 0 var(--spacing-2xl);
}

.article-meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.category {
  background: var(--color-gradient);
  color: white;
  padding: 4px 14px;
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
}

.date,
.reading-time {
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.article-title {
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  color: var(--color-text);
  margin-bottom: var(--spacing-xl);
  line-height: var(--leading-tight);
}

.author-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.author-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--color-text);
}

.author-bio {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

/* ==================== 文章正文 ==================== */
.article-body {
  max-width: var(--article-max-width);
  margin: 0 auto;
  padding: var(--spacing-2xl) 0;
}

.body-content {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--spacing-2xl);
  box-shadow: var(--shadow-sm);
  line-height: var(--leading-relaxed);
  color: var(--color-text-secondary);
  font-size: var(--text-base);
  border: 1px solid var(--color-border);
}

.body-content :deep(h1),
.body-content :deep(h2),
.body-content :deep(h3) {
  color: var(--color-text);
  margin-top: var(--spacing-xl);
  margin-bottom: var(--spacing-md);
}

.body-content :deep(h2) {
  font-size: var(--text-2xl);
  padding-bottom: var(--spacing-sm);
  border-bottom: 2px solid var(--color-border);
}

.body-content :deep(p) {
  margin-bottom: var(--spacing-lg);
  color: var(--color-text-secondary);
}

.body-content :deep(code) {
  background: var(--color-primary-bg);
  color: var(--color-primary-dark);
  padding: 2px 6px;
  border-radius: var(--radius-xs);
  font-size: 0.9em;
}

.body-content :deep(pre) {
  background: var(--color-text);
  color: var(--color-text-inverse);
  padding: var(--spacing-lg);
  border-radius: var(--radius-md);
  overflow-x: auto;
  margin: var(--spacing-lg) 0;
}

.body-content :deep(pre code) {
  background: none;
  color: inherit;
  padding: 0;
}

.body-content :deep(blockquote) {
  border-left: 4px solid var(--color-primary);
  background: var(--color-primary-bg);
  padding: var(--spacing-md) var(--spacing-lg);
  margin: var(--spacing-lg) 0;
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
}

.body-content :deep(img) {
  max-width: 100%;
  border-radius: var(--radius-md);
  margin: var(--spacing-lg) 0;
}

/* ==================== 文章底部 ==================== */
.article-footer-section {
  background: var(--color-bg-card);
  border-top: 1px solid var(--color-border);
  border-bottom: 1px solid var(--color-border);
}

.footer-content {
  max-width: var(--article-max-width);
  margin: 0 auto;
  padding: var(--spacing-lg) 0;
}

.article-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
}

.tag-label {
  color: var(--color-text-muted);
}

.tag {
  background: var(--color-bg-subtle);
  color: var(--color-text-secondary);
  padding: 4px 14px;
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
}

.interactions {
  display: flex;
  justify-content: center;
  gap: var(--spacing-lg);
}

.interaction-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-xl);
  border: none;
  background: var(--color-bg-subtle);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.interaction-btn:hover {
  background: var(--color-primary);
  color: white;
  transform: translateY(-2px);
}

.interaction-btn span:first-child {
  font-size: var(--text-xl);
}

/* ==================== 评论区 ==================== */
.comments-section {
  max-width: var(--article-max-width);
  margin: 0 auto;
  padding: var(--spacing-2xl) 0;
}

.comments-title {
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--color-text);
  margin-bottom: var(--spacing-xl);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

/* ==================== 评论表单 ==================== */
.comment-form {
  background: var(--color-bg-subtle);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-2xl);
}

.form-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}

.form-label {
  font-weight: var(--font-medium);
  color: var(--color-text);
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--spacing-md);
}

.login-hint {
  text-align: center;
  padding: var(--spacing-2xl);
  background: var(--color-bg-subtle);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-2xl);
}

.login-hint p {
  color: var(--color-text-muted);
  margin-bottom: var(--spacing-md);
}

/* ==================== 评论列表 ==================== */
.comments-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.comment-item {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
}

.comment-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}

.comment-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.comment-author {
  font-weight: var(--font-medium);
  color: var(--color-text);
}

.comment-time {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.comment-body {
  color: var(--color-text-secondary);
  line-height: var(--leading-normal);
}

.comment-status {
  margin-top: var(--spacing-sm);
  font-size: var(--text-xs);
  color: var(--color-warning);
}

.empty-comments {
  text-align: center;
  padding: var(--spacing-2xl);
  color: var(--color-text-muted);
}

/* ==================== 响应式 ==================== */
@media (max-width: 768px) {
  .hero-content {
    padding: var(--spacing-2xl) 0 var(--spacing-xl);
  }

  .article-title {
    font-size: var(--text-2xl);
  }

  .article-meta {
    flex-wrap: wrap;
  }

  .body-content {
    padding: var(--spacing-lg);
  }

  .interactions {
    flex-wrap: wrap;
  }

  .interaction-btn {
    flex: 1;
    min-width: 80px;
  }
}

@media (max-width: 480px) {
  .article-title {
    font-size: var(--text-xl);
  }

  .author-info {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
