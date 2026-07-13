<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { articleApi } from '@/api/article'
import { commentApi } from '@/api/comment'
import { marked } from 'marked'

const route = useRoute()
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
    return
  }

  submitting.value = true
  try {
    await commentApi.create({
      articleId: article.value.id,
      content: commentContent.value
    })
    commentContent.value = ''
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
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <span v-if="article.categoryName">
            <el-tag>{{ article.categoryName }}</el-tag>
          </span>
          <span class="article-date">{{ article.publishedAt }}</span>
          <span class="article-author" v-if="article.authorName">
            <el-icon><User /></el-icon> {{ article.authorName }}
          </span>
          <span class="article-views">
            <el-icon><View /></el-icon> {{ article.viewCount }}
          </span>
        </div>
        <div class="article-content" v-html="renderedContent"></div>

        <el-divider />

        <div class="comment-section">
          <h3>评论 ({{ comments.length }})</h3>
          <el-input
            v-model="commentContent"
            type="textarea"
            :rows="4"
            placeholder="写下你的评论..."
          />
          <el-button type="primary" :loading="submitting" @click="submitComment" style="margin-top: 10px">
            发表评论
          </el-button>

          <div class="comment-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-header">
                <span class="comment-author">{{ comment.userName || comment.guestName || '匿名用户' }}</span>
                <span class="comment-date">{{ comment.createdAt }}</span>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
            </div>
            <el-empty v-if="comments.length === 0" description="暂无评论" :image-size="60" />
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
  padding: 20px;
}

.article-title {
  font-size: 28px;
  color: #333;
  margin-bottom: 20px;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 30px;
  color: #666;
  font-size: 14px;
}

.article-author,
.article-views {
  display: flex;
  align-items: center;
  gap: 5px;
}

.article-content {
  line-height: 1.8;
  font-size: 16px;
}

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3) {
  margin-top: 30px;
  margin-bottom: 15px;
}

.article-content :deep(p) {
  margin-bottom: 15px;
}

.article-content :deep(code) {
  background-color: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
}

.article-content :deep(pre) {
  background-color: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
}

.comment-section {
  margin-top: 30px;
}

.comment-section h3 {
  margin-bottom: 20px;
}

.comment-list {
  margin-top: 30px;
}

.comment-item {
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.comment-author {
  font-weight: bold;
  color: #333;
}

.comment-date {
  color: #999;
  font-size: 12px;
}

.comment-content {
  color: #666;
  line-height: 1.6;
}
</style>
