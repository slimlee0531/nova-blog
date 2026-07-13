<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { categoryApi } from '@/api/category'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const loading = ref(false)
const saving = ref(false)

const form = ref({
  title: '',
  content: '',
  categoryId: null,
  status: 'DRAFT',
  visibility: 'PUBLIC',
  summary: '',
  metaTitle: '',
  metaDescription: ''
})

const categories = ref<any[]>([])

const fetchCategories = async () => {
  try {
    const res: any = await categoryApi.getList()
    if (res.code === 200) {
      categories.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchArticle = async () => {
  if (!isEdit.value) return

  loading.value = true
  try {
    const res: any = await articleApi.adminGetArticle(Number(route.params.id))
    if (res.code === 200) {
      form.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!form.value.title || !form.value.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }

  saving.value = true
  try {
    let res: any
    if (isEdit.value) {
      res = await articleApi.adminUpdateArticle(Number(route.params.id), form.value)
    } else {
      res = await articleApi.adminCreateArticle(form.value)
    }

    if (res.code === 200) {
      ElMessage.success('保存成功')
      router.push('/admin/articles')
    }
  } catch (e) {
    console.error(e)
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  router.push('/admin/articles')
}

onMounted(() => {
  fetchCategories()
  fetchArticle()
})
</script>

<template>
  <div class="article-edit" v-loading="loading">
    <div class="edit-header">
      <h2>{{ isEdit ? '编辑文章' : '新建文章' }}</h2>
      <div class="edit-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </div>
    </div>

    <el-form :model="form" label-width="100px">
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="请输入文章标题" />
      </el-form-item>

      <el-form-item label="内容">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="20"
          placeholder="请输入文章内容（支持Markdown）"
        />
      </el-form-item>

      <el-form-item label="摘要">
        <el-input
          v-model="form.summary"
          type="textarea"
          :rows="3"
          placeholder="文章摘要（可选）"
        />
      </el-form-item>

      <el-form-item label="分类">
        <el-select v-model="form.categoryId" placeholder="请选择分类" clearable>
          <el-option
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="状态">
        <el-select v-model="form.status">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="发布" value="PUBLISHED" />
        </el-select>
      </el-form-item>

      <el-form-item label="可见性">
        <el-select v-model="form.visibility">
          <el-option label="公开" value="PUBLIC" />
          <el-option label="私密" value="PRIVATE" />
        </el-select>
      </el-form-item>

      <el-form-item label="SEO标题">
        <el-input v-model="form.metaTitle" placeholder="SEO标题（可选）" />
      </el-form-item>

      <el-form-item label="SEO描述">
        <el-input
          v-model="form.metaDescription"
          type="textarea"
          :rows="2"
          placeholder="SEO描述（可选）"
        />
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.edit-header h2 {
  margin: 0;
}

.edit-actions {
  display: flex;
  gap: 10px;
}
</style>
