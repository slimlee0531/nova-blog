<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { categoryApi } from '@/api/category'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'
import type { Category, ArticleCreateParams } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const saving = ref(false)
const activeTab = ref<'edit' | 'preview'>('edit')

const form = ref<ArticleCreateParams>({
  title: '',
  content: '',
  categoryId: undefined,
  status: 'DRAFT',
  summary: ''
})

const categories = ref<Category[]>([])

// Markdown 预览
const renderedContent = computed(() => {
  return renderMarkdown(form.value.content || '')
})

const fetchCategories = async () => {
  try {
    const res = await categoryApi.getList()
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
    const res = await articleApi.adminCreateArticle({
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

      <!-- 编辑/预览切换 -->
      <div class="editor-tabs">
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'edit' }"
          @click="activeTab = 'edit'"
        >
          ✏️ 编辑
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'preview' }"
          @click="activeTab = 'preview'"
        >
          👁️ 预览
        </button>
      </div>

      <!-- 编辑区 -->
      <div v-show="activeTab === 'edit'" class="editor-area">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="20"
          placeholder="请输入文章内容（支持Markdown格式）"
          class="content-input"
        />
      </div>

      <!-- 预览区 -->
      <div v-show="activeTab === 'preview'" class="preview-area">
        <div v-if="form.content" class="markdown-body" v-html="renderedContent"></div>
        <div v-else class="empty-preview">
          <p>📝 暂无内容，在编辑区输入 Markdown 即可预览</p>
        </div>
      </div>

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
  padding: var(--spacing-lg);
  min-height: calc(100vh - var(--header-height));
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
}

.write-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xl);
  padding-bottom: var(--spacing-lg);
  border-bottom: 1px solid var(--color-divider);
}

.write-header h2 {
  margin: 0;
  color: var(--color-text);
}

.header-actions {
  display: flex;
  gap: var(--spacing-sm);
}

.write-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.title-input :deep(.el-input__inner) {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  border: none;
  padding: 10px 0;
}

.title-input :deep(.el-input__inner:focus) {
  box-shadow: none;
}

.form-row {
  display: flex;
  gap: var(--spacing-md);
}

/* ==================== 编辑/预览标签 ==================== */
.editor-tabs {
  display: flex;
  gap: var(--spacing-xs);
  border-bottom: 2px solid var(--color-divider);
  padding-bottom: 0;
}

.tab-btn {
  padding: var(--spacing-sm) var(--spacing-md);
  border: none;
  background: none;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all var(--transition-fast);
}

.tab-btn:hover {
  color: var(--color-text);
}

.tab-btn.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
}

/* ==================== 编辑区 ==================== */
.content-input :deep(.el-textarea__inner) {
  font-family: var(--font-mono);
  line-height: var(--leading-relaxed);
  padding: var(--spacing-lg);
}

/* ==================== 预览区 ==================== */
.preview-area {
  min-height: 400px;
  background: var(--color-bg-subtle);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
}

.markdown-body {
  color: var(--color-text-secondary);
  line-height: var(--leading-relaxed);
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  color: var(--color-text);
  margin-top: var(--spacing-xl);
  margin-bottom: var(--spacing-md);
}

.markdown-body :deep(h2) {
  font-size: var(--text-2xl);
  padding-bottom: var(--spacing-sm);
  border-bottom: 2px solid var(--color-border);
}

.markdown-body :deep(p) {
  margin-bottom: var(--spacing-md);
}

.markdown-body :deep(code) {
  background: var(--color-primary-bg);
  color: var(--color-primary-dark);
  padding: 2px 6px;
  border-radius: var(--radius-xs);
  font-family: var(--font-mono);
  font-size: 0.9em;
}

.markdown-body :deep(pre) {
  background: var(--color-text);
  color: var(--color-text-inverse);
  padding: var(--spacing-lg);
  border-radius: var(--radius-md);
  overflow-x: auto;
  margin: var(--spacing-md) 0;
}

.markdown-body :deep(pre code) {
  background: none;
  color: inherit;
  padding: 0;
}

.markdown-body :deep(blockquote) {
  border-left: 4px solid var(--color-primary);
  background: var(--color-primary-bg);
  padding: var(--spacing-md) var(--spacing-lg);
  margin: var(--spacing-md) 0;
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
}

.markdown-body :deep(li) {
  margin-bottom: var(--spacing-xs);
}

.markdown-body :deep(img) {
  max-width: 100%;
  border-radius: var(--radius-md);
  margin: var(--spacing-md) 0;
}

.markdown-body :deep(blockquote) {
  border-left: 4px solid var(--color-primary);
  background: var(--color-primary-bg);
  padding: var(--spacing-md) var(--spacing-lg);
  margin: var(--spacing-md) 0;
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
}

.empty-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: var(--color-text-muted);
}

/* 提示 */
.tips-list {
  margin: 0;
  padding-left: var(--spacing-lg);
  color: var(--color-text-muted);
}

.tips-list li {
  margin: var(--spacing-xs) 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .write-header {
    flex-direction: column;
    gap: var(--spacing-md);
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
