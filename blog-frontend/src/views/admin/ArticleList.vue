<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const articles = ref<any[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const fetchArticles = async () => {
  loading.value = true
  try {
    const res: any = await articleApi.adminGetArticles({
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

const handleCreate = () => {
  router.push('/admin/article/edit')
}

const handleEdit = (id: number) => {
  router.push(`/admin/article/edit/${id}`)
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？', '提示', {
      type: 'warning'
    })
    const res: any = await articleApi.adminDeleteArticle(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchArticles()
    }
  } catch (e) {
    console.error(e)
  }
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  fetchArticles()
}

const getStatusType = (status: string) => {
  const map: any = {
    'PUBLISHED': 'success',
    'DRAFT': 'info',
    'ARCHIVED': 'warning'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: any = {
    'PUBLISHED': '已发布',
    'DRAFT': '草稿',
    'ARCHIVED': '已归档'
  }
  return map[status] || status
}

onMounted(() => {
  fetchArticles()
})
</script>

<template>
  <div class="article-list">
    <div class="list-header">
      <h2>文章管理</h2>
      <el-button type="primary" @click="handleCreate">新建文章</el-button>
    </div>

    <el-table :data="articles" v-loading="loading" style="width: 100%">
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column prop="publishedAt" label="发布时间" width="180" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row.id)">编辑</el-button>
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
