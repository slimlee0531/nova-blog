<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { categoryApi } from '@/api/category'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const saving = ref(false)

const form = ref({
  title: '',
  content: '',
  categoryId: null,
  status: 'DRAFT',
  summary: ''
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

const handleSave = async (status: string) => {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入文章标题')
    return
  }
  if (!form.value.content.trim()) {
    ElMessage.warning('请输入文章内容')
    return
  }

  saving.value = true
  try {
    const res: any = await articleApi.adminCreateArticle({
      ...form.value,
      status
    })
    if (res.code === 200) {
      ElMessage.success(status === 'PUBLISHED' ? '发布成功' : '保存成功')
      router.push('/')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  fetchCategories()
})
</script>

<template>
  <div class="write-container">
    <div class="write-header">
      <h2>写文章</h2>
      <div class="header-actions">
        <el-button @click="router.back()">取消</el-button>
        <el-button @click="handleSave('DRAFT')" :loading="saving">保存草稿</el-button>
        <el-button type="primary" @click="handleSave('PUBLISHED')" :loading="saving">发布文章</el-button>
      </div>
    </div>

    <div class="write-form">
      <el-input
        v-model="form.title"
        placeholder="请输入文章标题"
        class="title-input"
        size="large"
      />

      <div class="form-row">
        <el-select v-model="form.categoryId" placeholder="选择分类" clearable>
          <el-option
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
      </div>

      <el-input
        v-model="form.summary"
        type="textarea"
        :rows="2"
        placeholder="文章摘要（可选）"
        class="summary-input"
      />

      <el-input
        v-model="form.content"
        type="textarea"
        :rows="20"
        placeholder="请输入文章内容（支持Markdown格式）"
        class="content-input"
      />

      <div class="write-tips">
        <el-alert
          title="写作提示"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <ul class="tips-list">
              <li>支持Markdown格式</li>
              <li>使用 # 标题，** 加粗**，* 斜体*</li>
              <li>使用 ``` 代码块，> 引用</li>
            </ul>
          </template>
        </el-alert>
      </div>
    </div>
  </div>
</template>

<style scoped>
.write-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - 60px);
  background: white;
}

.write-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.write-header h2 {
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.write-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.title-input :deep(.el-input__inner) {
  font-size: 24px;
  font-weight: bold;
  border: none;
  padding: 10px 0;
}

.title-input :deep(.el-input__inner:focus) {
  box-shadow: none;
}

.form-row {
  display: flex;
  gap: 15px;
}

.content-input :deep(.el-textarea__inner) {
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  line-height: 1.8;
  padding: 20px;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  color: #666;
}

.tips-list li {
  margin: 5px 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .write-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
