<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { articleApi } from '@/api/article'

const stats = ref({
  totalArticles: 0,
  publishedArticles: 0,
  draftArticles: 0
})

const fetchStats = async () => {
  try {
    const res = await articleApi.adminGetArticles({ page: 1, size: 1 })
    if (res.code === 200) {
      stats.value.totalArticles = res.data.total
    }

    const publishedRes = await articleApi.adminGetArticles({ page: 1, size: 1, status: 'PUBLISHED' })
    if (publishedRes.code === 200) {
      stats.value.publishedArticles = publishedRes.data.total
    }

    const draftRes = await articleApi.adminGetArticles({ page: 1, size: 1, status: 'DRAFT' })
    if (draftRes.code === 200) {
      stats.value.draftArticles = draftRes.data.total
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<template>
  <div class="dashboard">
    <h2>仪表盘</h2>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>文章总数</span>
          </template>
          <div class="stat-value">{{ stats.totalArticles }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>已发布</span>
          </template>
          <div class="stat-value published">{{ stats.publishedArticles }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>草稿</span>
          </template>
          <div class="stat-value draft">{{ stats.draftArticles }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard h2 {
  margin-bottom: var(--spacing-lg);
  color: var(--color-text);
}

.stat-value {
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  text-align: center;
  color: var(--color-primary);
}

.stat-value.published {
  color: var(--color-success);
}

.stat-value.draft {
  color: var(--color-text-muted);
}
</style>
