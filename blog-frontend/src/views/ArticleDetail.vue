<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { commentApi } from '@/api/comment'
import { bookmarkApi } from '@/api/bookmark'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { formatDate } from '@/utils/format'
import { renderMarkdown } from '@/utils/markdown'
import type { Article, Comment } from '@/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const article = ref<Article | null>(null)

// Markdown 渲染
const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  return renderMarkdown(article.value.content)
})
const comments = ref<Comment[]>([])
const loading = ref(false)
const commentContent = ref('')
const submitting = ref(false)
const isBookmarked = ref(false)
const bookmarkLoading = ref(false)

const fetchArticle = async () => {
  loading.value = true
  try {
    const slug = route.params.slug as string
    const res = await articleApi.getArticleBySlug(slug)
    if (res.code === 200) {
      article.value = res.data
      fetchComments()
      checkBookmarkStatus()
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

const checkBookmarkStatus = async () => {
  if (!article.value || !userStore.token) return
  try {
    const res = await bookmarkApi.checkBookmark(article.value.id)
    if (res.code === 200) {
      isBookmarked.value = res.data.bookmarked
    }
  } catch (e) {
    console.error(e)
  }
}

const toggleBookmark = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!article.value || bookmarkLoading.value) return

  bookmarkLoading.value = true
  try {
    const res = await bookmarkApi.toggleBookmark(article.value.id)
    if (res.code === 200) {
      isBookmarked.value = res.data.bookmarked
      article.value.bookmarkCount = res.data.bookmarkCount
      ElMessage.success(isBookmarked.value ? '收藏成功' : '已取消收藏')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  } finally {
    bookmarkLoading.value = false
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

const shareArticle = () => {
  if (navigator.share) {
    navigator.share({
      title: article.value?.title,
      url: window.location.href
    })
  } else {
    navigator.clipboard.writeText(window.location.href)
    ElMessage.success('链接已复制')
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
        <div class="hero-container">
          <!-- 返回按钮 -->
          <button class="back-btn" @click="router.push('/')">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M12 4L6 10L12 16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>返回首页</span>
          </button>

          <!-- 文章元信息 -->
          <div class="article-meta">
            <span class="category" v-if="article.categoryName">{{ article.categoryName }}</span>
            <span class="separator">·</span>
            <span class="date">{{ formatDate(article.publishedAt || article.createdAt) }}</span>
            <span class="separator">·</span>
            <span class="reading-time" v-if="article.aiReadingTime">{{ article.aiReadingTime }}分钟阅读</span>
          </div>

          <!-- 文章标题 -->
          <h1 class="article-title">{{ article.title }}</h1>

          <!-- 作者信息 -->
          <div class="author-info">
            <div class="author-avatar">
              {{ article.authorName?.charAt(0) || 'A' }}
            </div>
            <div class="author-detail">
              <span class="author-name">{{ article.authorName || '匿名' }}</span>
              <span class="author-bio">发布于 {{ formatDate(article.publishedAt || article.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 文章正文 -->
      <div class="article-body">
        <div class="body-container">
          <div class="body-content" v-html="renderedContent"></div>
        </div>
      </div>

      <!-- 文章底部 -->
      <div class="article-footer">
        <div class="footer-container">
          <!-- 标签 -->
          <div class="article-tags" v-if="article.tagInfos?.length || article.tags?.length">
            <template v-if="article.tagInfos?.length">
              <span
                v-for="tag in article.tagInfos"
                :key="tag.id"
                class="tag"
                :style="tag.color ? { background: `${tag.color}18`, color: tag.color, borderColor: `${tag.color}40` } : {}"
              >#{{ tag.name }}</span>
            </template>
            <template v-else>
              <span v-for="tag in article.tags" :key="tag" class="tag">#{{ tag }}</span>
            </template>
          </div>

          <!-- 互动按钮 -->
          <div class="interactions">
            <button
              class="interaction-btn"
              :class="{ active: isBookmarked }"
              @click="toggleBookmark"
              :disabled="bookmarkLoading"
            >
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" :stroke="isBookmarked ? 'currentColor' : 'none'">
                <path d="M19 21L12 16L5 21V5C5 4.46957 5.21071 3.96086 5.58579 3.58579C5.96086 3.21071 6.46957 3 7 3H17C17.5304 3 18.0391 3.21071 18.4142 3.58579C18.7893 3.96086 19 4.46957 19 5V21Z" :stroke="isBookmarked ? 'none' : 'currentColor'" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span>{{ isBookmarked ? '已收藏' : '收藏' }}</span>
              <span class="count" v-if="article.bookmarkCount">{{ article.bookmarkCount }}</span>
            </button>

            <button class="interaction-btn" @click="shareArticle">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="18" cy="5" r="3"/>
                <circle cx="6" cy="12" r="3"/>
                <circle cx="18" cy="19" r="3"/>
                <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/>
                <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
              </svg>
              <span>分享</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comments-section">
        <div class="comments-container">
          <h3 class="comments-title">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
            评论 ({{ comments.length }})
          </h3>

          <!-- 评论输入框 -->
          <div class="comment-form" v-if="userStore.token">
            <div class="form-header">
              <div class="form-avatar">
                {{ userStore.userInfo?.username?.charAt(0) || 'U' }}
              </div>
              <span class="form-label">发表评论</span>
            </div>
            <textarea
              v-model="commentContent"
              rows="4"
              placeholder="写下你的想法..."
              :disabled="submitting"
            ></textarea>
            <div class="form-footer">
              <button
                class="submit-btn"
                @click="submitComment"
                :disabled="submitting"
              >
                {{ submitting ? '发布中...' : '发布评论' }}
              </button>
            </div>
          </div>
          <div class="login-hint" v-else>
            <p>登录后即可参与评论</p>
            <button class="login-btn" @click="router.push('/login')">立即登录</button>
          </div>

          <!-- 评论列表 -->
          <div class="comments-list">
            <TransitionGroup name="comment" tag="div">
              <div v-for="comment in comments" :key="comment.id" class="comment-item">
                <div class="comment-header">
                  <div class="comment-avatar">
                    {{ comment.authorName?.charAt(0) || 'A' }}
                  </div>
                  <div class="comment-info">
                    <span class="comment-author">{{ comment.authorName || '匿名' }}</span>
                    <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
                  </div>
                  <span class="comment-status" v-if="comment.status === 'PENDING'">待审核</span>
                </div>
                <div class="comment-body">
                  {{ comment.content }}
                </div>
              </div>
            </TransitionGroup>

            <div v-if="comments.length === 0" class="empty-comments">
              <div class="empty-icon">💬</div>
              <p>暂无评论</p>
              <span>快来发表第一条评论吧！</span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
/* ==================== Loading ==================== */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  color: #999;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f0f0f0;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-state p {
  margin-top: 16px;
  font-size: 15px;
}

/* ==================== Article Hero ==================== */
.article-hero {
  background: linear-gradient(180deg, #fafbfc 0%, #fff 100%);
  padding: 80px 24px 60px;
}

[data-theme="dark"] .article-hero {
  background: linear-gradient(180deg, #0a0a0a 0%, #111 100%);
}

.hero-container {
  max-width: 800px;
  margin: 0 auto;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 10px;
  color: #666;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 32px;
}

[data-theme="dark"] .back-btn {
  background: #1a1a1a;
  border-color: #333;
  color: #999;
}

.back-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.category {
  color: #667eea;
  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.separator {
  color: #ccc;
}

.date,
.reading-time {
  color: #999;
  font-size: 14px;
}

.article-title {
  font-size: 48px;
  font-weight: 800;
  color: #1a1a1a;
  margin-bottom: 24px;
  line-height: 1.15;
  letter-spacing: -1.5px;
}

[data-theme="dark"] .article-title {
  color: #fff;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
}

.author-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
}

[data-theme="dark"] .author-name {
  color: #fff;
}

.author-bio {
  font-size: 13px;
  color: #999;
}

/* ==================== Article Body ==================== */
.article-body {
  padding: 48px 24px;
}

.body-container {
  max-width: 720px;
  margin: 0 auto;
}

.body-content {
  line-height: 1.8;
  color: #333;
  font-size: 17px;
}

[data-theme="dark"] .body-content {
  color: #ccc;
}

.body-content :deep(h1),
.body-content :deep(h2),
.body-content :deep(h3) {
  color: #1a1a1a;
  margin-top: 48px;
  margin-bottom: 20px;
  font-weight: 700;
}

[data-theme="dark"] .body-content :deep(h1),
[data-theme="dark"] .body-content :deep(h2),
[data-theme="dark"] .body-content :deep(h3) {
  color: #fff;
}

.body-content :deep(h2) {
  font-size: 28px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

[data-theme="dark"] .body-content :deep(h2) {
  border-bottom-color: #222;
}

.body-content :deep(h3) {
  font-size: 22px;
}

.body-content :deep(p) {
  margin-bottom: 24px;
}

.body-content :deep(code) {
  background: #f5f5f5;
  color: #e44d26;
  padding: 3px 8px;
  border-radius: 6px;
  font-size: 0.9em;
  font-family: 'SF Mono', Menlo, monospace;
}

[data-theme="dark"] .body-content :deep(code) {
  background: #222;
  color: #ff6b6b;
}

.body-content :deep(pre) {
  background: #1a1a1a;
  color: #fff;
  padding: 24px;
  border-radius: 12px;
  overflow-x: auto;
  margin: 24px 0;
  font-size: 14px;
  line-height: 1.6;
}

.body-content :deep(pre code) {
  background: none;
  color: inherit;
  padding: 0;
}

.body-content :deep(blockquote) {
  border-left: 4px solid #667eea;
  padding: 20px 24px;
  margin: 24px 0;
  background: #f8f9fa;
  border-radius: 0 12px 12px 0;
  color: #555;
}

[data-theme="dark"] .body-content :deep(blockquote) {
  background: #1a1a1a;
  color: #999;
}

.body-content :deep(img) {
  max-width: 100%;
  border-radius: 12px;
  margin: 32px 0;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.body-content :deep(ul),
.body-content :deep(ol) {
  padding-left: 24px;
  margin-bottom: 24px;
}

.body-content :deep(li) {
  margin-bottom: 8px;
}

/* ==================== Article Footer ==================== */
.article-footer {
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
  padding: 32px 24px;
}

[data-theme="dark"] .article-footer {
  border-color: #222;
}

.footer-container {
  max-width: 720px;
  margin: 0 auto;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 24px;
}

.tag {
  padding: 8px 16px;
  background: #f5f5f5;
  color: #666;
  font-size: 14px;
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
  background: #667eea;
  color: #fff;
}

.interactions {
  display: flex;
  gap: 16px;
}

.interaction-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: 1px solid #e5e5e5;
  background: #fff;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

[data-theme="dark"] .interaction-btn {
  background: #1a1a1a;
  border-color: #333;
  color: #999;
}

.interaction-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.interaction-btn.active {
  background: #667eea;
  border-color: #667eea;
  color: #fff;
}

.interaction-btn .count {
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 12px;
}

.interaction-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ==================== Comments Section ==================== */
.comments-section {
  padding: 64px 24px;
  background: #fafbfc;
}

[data-theme="dark"] .comments-section {
  background: #0a0a0a;
}

.comments-container {
  max-width: 720px;
  margin: 0 auto;
}

.comments-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 32px;
}

[data-theme="dark"] .comments-title {
  color: #fff;
}

/* ==================== Comment Form ==================== */
.comment-form {
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 32px;
}

[data-theme="dark"] .comment-form {
  background: #1a1a1a;
  border-color: #333;
}

.form-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.form-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
}

.form-label {
  font-weight: 600;
  color: #1a1a1a;
  font-size: 15px;
}

[data-theme="dark"] .form-label {
  color: #fff;
}

textarea {
  width: 100%;
  padding: 16px;
  border: 1px solid #e5e5e5;
  border-radius: 12px;
  font-size: 15px;
  color: #1a1a1a;
  background: #fafbfc;
  resize: vertical;
  outline: none;
  transition: all 0.2s ease;
}

[data-theme="dark"] textarea {
  border-color: #333;
  background: #0a0a0a;
  color: #fff;
}

textarea:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

textarea::placeholder {
  color: #999;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.submit-btn {
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

.submit-btn:hover {
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transform: translateY(-1px);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.login-hint {
  text-align: center;
  padding: 40px;
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 16px;
  margin-bottom: 32px;
}

[data-theme="dark"] .login-hint {
  background: #1a1a1a;
  border-color: #333;
}

.login-hint p {
  color: #666;
  margin-bottom: 16px;
  font-size: 15px;
}

.login-btn {
  padding: 12px 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.login-btn:hover {
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
}

/* ==================== Comment List ==================== */
.comments-list {
  display: flex;
  flex-direction: column;
}

.comment-item {
  padding: 24px 0;
  border-bottom: 1px solid #f0f0f0;
}

[data-theme="dark"] .comment-item {
  border-color: #222;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  background: #f5f5f5;
  color: #666;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
}

[data-theme="dark"] .comment-avatar {
  background: #222;
  color: #999;
}

.comment-info {
  flex: 1;
}

.comment-author {
  display: block;
  font-weight: 600;
  font-size: 15px;
  color: #1a1a1a;
}

[data-theme="dark"] .comment-author {
  color: #fff;
}

.comment-time {
  font-size: 13px;
  color: #999;
}

.comment-status {
  padding: 4px 10px;
  background: #fff3cd;
  color: #856404;
  font-size: 12px;
  font-weight: 500;
  border-radius: 6px;
}

.comment-body {
  color: #444;
  line-height: 1.6;
  font-size: 15px;
  padding-left: 52px;
}

[data-theme="dark"] .comment-body {
  color: #ccc;
}

.empty-comments {
  text-align: center;
  padding: 60px 24px;
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 16px;
}

[data-theme="dark"] .empty-comments {
  background: #1a1a1a;
  border-color: #333;
}

.empty-comments .empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-comments p {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}

[data-theme="dark"] .empty-comments p {
  color: #fff;
}

.empty-comments span {
  font-size: 14px;
  color: #999;
}

/* ==================== Animations ==================== */
.comment-enter-active,
.comment-leave-active {
  transition: all 0.3s ease;
}

.comment-enter-from,
.comment-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* ==================== Responsive ==================== */
@media (max-width: 768px) {
  .article-hero {
    padding: 60px 20px 40px;
  }

  .article-title {
    font-size: 32px;
    letter-spacing: -1px;
  }

  .article-meta {
    flex-wrap: wrap;
  }

  .body-content {
    font-size: 16px;
  }

  .interactions {
    flex-direction: column;
  }

  .interaction-btn {
    justify-content: center;
  }

  .comment-body {
    padding-left: 0;
    margin-top: 12px;
  }
}
</style>
