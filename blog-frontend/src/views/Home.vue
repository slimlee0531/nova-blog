<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { articleApi } from '@/api/article'
import { marked } from 'marked'

const articles = ref<any[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const fetchArticles = async () => {
  loading.value = true
  try {
    const res: any = await articleApi.getArticles({
      page: page.value,
      size: pageSize.value
    })
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

const handlePageChange = (newPage: number) => {
  page.value = newPage
  fetchArticles()
}

const getExcerpt = (content: string) => {
  const text = content.replace(/[#*`]/g, '').substring(0, 200)
  return text + (content.length > 200 ? '...' : '')
}

onMounted(() => {
  fetchArticles()
})
</script>

<template>
  <div class="home-container">
    <h1>最新文章</h1>
    <div v-loading="loading">
      <el-card v-for="article in articles" :key="article.id" class="article-card">
        <router-link :to="`/article/${article.slug}`" class="article-title">
          {{ article.title }}
        </router-link>
        <div class="article-meta">
          <span v-if="article.categoryName">
            <el-tag size="small">{{ article.categoryName }}</el-tag>
          </span>
          <span class="article-date">{{ article.publishedAt }}</span>
          <span class="article-views">
            <el-icon><View /></el-icon> {{ article.viewCount }}
          </span>
        </div>
        <div class="article-excerpt">
          {{ article.summary || getExcerpt(article.content) }}
        </div>
      </el-card>

      <el-empty v-if="!loading && articles.length === 0" description="暂无文章" />

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
.home-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  margin-bottom: 30px;
  color: #333;
}

.article-card {
  margin-bottom: 20px;
}

.article-title {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  text-decoration: none;
  display: block;
  margin-bottom: 10px;
}

.article-title:hover {
  text-decoration: underline;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
  color: #666;
  font-size: 14px;
}

.article-date,
.article-views {
  display: flex;
  align-items: center;
  gap: 5px;
}

.article-excerpt {
  color: #666;
  line-height: 1.6;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>
