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
    await commentApi.create({
      articleId: article.value.id,
      content: commentContent.value
    })
    commentContent.value = ''
    ElMessage.success('评论提交成功，等待审核')
    fetchComments()
  } catch (e) {
    console.error(e)
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
  <div class="article-container">
    <div v-loading="loading">
      <template v-if="article">
        <!-- 文章头部 -->
        <div class="article-header">
          <h1 class="article-title">{{ article.title }}</h1>
          <div class="article-meta">
            <span class="meta-item">
              <el-avatar :size="24">{{ article.authorName?.charAt(0) || 'A' }}</el-avatar>
              {{ article.authorName || '匿名' }}
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon> {{ article.publishedAt?.substring(0, 10) }}
            </span>
            <span class="meta-item">
              <el-icon><View /></el-icon> {{ article.viewCount }} 阅读
            </span>
            <el-tag v-if="article.categoryName" size="small">{{ article.categoryName }}</el-tag>
          </div>
        </div>

        <!-- 文章内容 -->
        <div class="article-content" v-html="renderedContent"></div>

        <!-- 文章底部 -->
        <div class="article-footer">
          <div class="footer-stats">
            <span class="stat-item">
              <el-icon><View /></el-icon> {{ article.viewCount }} 阅读
            </span>
            <span class="stat-item">
              <el-icon><ChatDotRound /></el-icon> {{ comments.length }} 评论
            </span>
            <span class="stat-item">
              <el-icon><Star /></el-icon> {{ article.likeCount || 0 }} 点赞
            </span>
          </div>
        </div>

        <!-- 评论区 -->
        <div class="comment-section">
          <h3 class="comment-title">
            <el-icon><ChatLineSquare /></el-icon> 评论 ({{ comments.length }})
          </h3>

          <!-- 评论输入框 -->
          <div class="comment-input" v-if="userStore.token">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="4"
              placeholder="写下你的评论..."
            />
            <el-button
              type="primary"
              :loading="submitting"
              @click="submitComment"
              class="submit-btn"
            >
              发表评论
            </el-button>
          </div>
          <div class="comment-login" v-else>
            <el-button type="primary" @click="router.push('/login')">登录</el-button>
            后发表评论
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
                  <span class="comment-date">{{ comment.createdAt?.substring(0, 16) }}</span>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
                <div class="comment-actions">
                  <el-button text size="small">
                    <el-icon><Star /></el-icon> 点赞
                  </el-button>
                  <el-button text size="small">
                    <el-icon><ChatRound /></el-icon> 回复
                  </el-button>
                </div>
              </div>
            </div>

            <el-empty v-if="comments.length === 0" description="暂无评论，快来抢沙发吧！" />
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.article-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 30px 20px;
  background: white;
  min-height: calc(100vh - 60px);
}

/* 文章头部 */
.article-header {
  margin-bottom: 40px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.article-title {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
  line-height: 1.3;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  color: #666;
  font-size: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 文章内容 */
.article-content {
  line-height: 1.8;
  font-size: 16px;
  color: #333;
}

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3) {
  margin-top: 30px;
  margin-bottom: 15px;
  color: #333;
}

.article-content :deep(p) {
  margin-bottom: 16px;
}

.article-content :deep(code) {
  background-color: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 14px;
}

.article-content :deep(pre) {
  background-color: #282c34;
  color: #abb2bf;
  padding: 20px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 20px 0;
}

.article-content :deep(pre code) {
  background: none;
  padding: 0;
  color: inherit;
}

.article-content :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding-left: 16px;
  margin: 20px 0;
  color: #666;
  background: #f9f9f9;
  padding: 16px;
  border-radius: 0 8px 8px 0;
}

.article-content :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 20px 0;
}

.article-content :deep(ul),
.article-content :deep(ol) {
  padding-left: 24px;
  margin: 16px 0;
}

.article-content :deep(li) {
  margin: 8px 0;
}

/* 文章底部 */
.article-footer {
  margin: 40px 0;
  padding: 20px 0;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
}

.footer-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
}

/* 评论区 */
.comment-section {
  margin-top: 40px;
}

.comment-title {
  font-size: 18px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-input {
  margin-bottom: 30px;
}

.submit-btn {
  margin-top: 12px;
}

.comment-login {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  text-align: center;
  margin-bottom: 30px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.comment-author {
  font-weight: 600;
  color: #333;
}

.comment-date {
  color: #999;
  font-size: 12px;
}

.comment-content {
  color: #333;
  line-height: 1.6;
}

.comment-actions {
  margin-top: 12px;
  display: flex;
  gap: 12px;
}

/* 响应式 */
@media (max-width: 768px) {
  .article-container {
    padding: 20px 16px;
  }

  .article-title {
    font-size: 24px;
  }

  .article-meta {
    flex-wrap: wrap;
    gap: 12px;
  }

  .footer-stats {
    flex-wrap: wrap;
    gap: 20px;
  }
}
</style>
