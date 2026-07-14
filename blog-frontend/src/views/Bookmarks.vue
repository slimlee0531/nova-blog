<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { bookmarkApi, type BookmarkItem } from '@/api/bookmark'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()

const bookmarks = ref<BookmarkItem[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(12)

const fetchBookmarks = async () => {
  if (!userStore.token) {
    router.push('/login')
    return
  }

  loading.value = true
  try {
    const res = await bookmarkApi.getBookmarks({
      page: page.value,
      size: pageSize.value
    })
    if (res.code === 200) {
      bookmarks.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

const removeBookmark = async (bookmark: BookmarkItem) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消收藏「${bookmark.title}」吗？`,
      '取消收藏',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await bookmarkApi.toggleBookmark(bookmark.articleId)
    if (res.code === 200) {
      ElMessage.success('已取消收藏')
      fetchBookmarks()
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const goToArticle = (slug: string) => {
  router.push(`/article/${slug}`)
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  window.scrollTo({ top: 0, behavior: 'smooth' })
  fetchBookmarks()
}

onMounted(() => {
  fetchBookmarks()
})
</script>

<template>
  <div class="bookmarks-page">
    <div class="bookmarks-container">
      <!-- Header -->
      <div class="page-header">
        <h1 class="page-title">我的收藏</h1>
        <p class="page-desc">共收藏了 {{ total }} 篇文章</p>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- Bookmarks Grid -->
      <div v-else-if="bookmarks.length" class="bookmarks-grid">
        <div
          v-for="bookmark in bookmarks"
          :key="bookmark.id"
          class="bookmark-card"
        >
          <!-- Featured Image -->
          <div
            class="bookmark-image"
            v-if="bookmark.featuredImage"
            :style="{ backgroundImage: `url(${bookmark.featuredImage})` }"
            @click="goToArticle(bookmark.slug)"
          ></div>
          <div class="bookmark-image placeholder" v-else @click="goToArticle(bookmark.slug)">
            <span>📝</span>
          </div>

          <!-- Content -->
          <div class="bookmark-content">
            <div class="bookmark-meta">
              <span class="bookmark-category" v-if="bookmark.categoryName">{{ bookmark.categoryName }}</span>
              <span class="bookmark-date">{{ formatDate(bookmark.publishedAt || bookmark.createdAt) }}</span>
            </div>

            <h3 class="bookmark-title" @click="goToArticle(bookmark.slug)">{{ bookmark.title }}</h3>
            <p class="bookmark-summary" v-if="bookmark.summary">{{ bookmark.summary }}</p>

            <div class="bookmark-footer">
              <div class="bookmark-stats">
                <span>👁 {{ bookmark.viewCount }}</span>
                <span>❤ {{ bookmark.likeCount }}</span>
                <span>💬 {{ bookmark.commentCount }}</span>
              </div>
              <button class="remove-btn" @click.stop="removeBookmark(bookmark)">
                取消收藏
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="empty-state">
        <div class="empty-icon">☆</div>
        <h3>暂无收藏</h3>
        <p>去发现感兴趣的文章吧</p>
        <button class="btn-primary" @click="router.push('/')">浏览文章</button>
      </div>

      <!-- Pagination -->
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
  </div>
</template>

<style scoped>
.bookmarks-page {
  min-height: 100vh;
  padding: 120px 24px 80px;
  background: var(--color-surface);
}

.bookmarks-container {
  max-width: 960px;
  margin: 0 auto;
}

/* Header */
.page-header {
  text-align: center;
  margin-bottom: 48px;
}

.page-title {
  font-size: 36px;
  font-weight: 700;
  color: var(--color-text);
  margin: 0 0 12px;
  letter-spacing: -0.5px;
}

.page-desc {
  font-size: 16px;
  color: var(--color-text-secondary);
  margin: 0;
}

/* Loading */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 40vh;
  color: var(--color-text-tertiary);
}

.spinner {
  width: 32px;
  height: 32px;
  border: 2px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-state p {
  margin-top: 16px;
}

/* Grid */
.bookmarks-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

/* Card */
.bookmark-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border-subtle);
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.2s ease;
}

.bookmark-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.bookmark-image {
  height: 160px;
  background-size: cover;
  background-position: center;
  cursor: pointer;
}

.bookmark-image.placeholder {
  background: var(--color-surface-container);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.bookmark-content {
  padding: 20px;
}

.bookmark-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.bookmark-category {
  color: var(--color-primary);
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.bookmark-date {
  color: var(--color-text-tertiary);
  font-size: 13px;
}

.bookmark-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 8px;
  line-height: 1.4;
  cursor: pointer;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.bookmark-title:hover {
  color: var(--color-primary);
}

.bookmark-summary {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin: 0 0 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.bookmark-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.bookmark-stats {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: var(--color-text-tertiary);
}

.remove-btn {
  padding: 6px 12px;
  font-size: 13px;
  color: var(--color-text-tertiary);
  background: none;
  border: 1px solid var(--color-border-subtle);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.remove-btn:hover {
  color: var(--color-error);
  border-color: var(--color-error);
  background: rgba(239, 68, 68, 0.05);
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 80px 24px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 24px;
  opacity: 0.5;
}

.empty-state h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 8px;
}

.empty-state p {
  font-size: 15px;
  color: var(--color-text-secondary);
  margin: 0 0 24px;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12px 28px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-on-primary);
  background: var(--color-primary);
  border: none;
  border-radius: 9999px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-primary:hover {
  background: var(--color-primary-hover);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* Pagination */
.pagination {
  margin-top: 48px;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: var(--color-primary);
}

/* Responsive */
@media (max-width: 768px) {
  .bookmarks-page {
    padding: 100px 16px 60px;
  }

  .page-title {
    font-size: 28px;
  }

  .bookmarks-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .bookmark-image {
    height: 140px;
  }
}
</style>
