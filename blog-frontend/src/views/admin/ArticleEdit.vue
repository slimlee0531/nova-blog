<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { categoryApi } from '@/api/category'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'
import type { Category, ArticleCreateParams } from '@/types'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const loading = ref(false)
const saving = ref(false)
const activeTab = ref<'edit' | 'preview'>('edit')

const form = ref<ArticleCreateParams>({
  title: '',
  content: '',
  categoryId: undefined,
  status: 'DRAFT',
  visibility: 'PUBLIC',
  summary: '',
  metaTitle: '',
  metaDescription: ''
})

// Markdown 预览
const renderedContent = computed(() => {
  return renderMarkdown(form.value.content || '')
})

const categories = ref<Category[]>([])

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

const fetchArticle = async () => {
  if (!isEdit.value) return

  loading.value = true
  try {
    const res = await articleApi.adminGetArticle(Number(route.params.id))
    if (res.code === 200) {
      form.value = {
        title: res.data.title,
        content: res.data.content,
        categoryId: res.data.categoryId,
        status: res.data.status,
        visibility: res.data.visibility,
        summary: res.data.summary || '',
        metaTitle: res.data.metaTitle || '',
        metaDescription: res.data.metaDescription || '',
        tags: res.data.tags
      }
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
    let res
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
        <!-- 编辑/预览切换 -->
        <div class="editor-wrapper">
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
          <div v-show="activeTab === 'edit'">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="20"
              placeholder="请输入文章内容（支持Markdown）"
            />
          </div>

          <!-- 预览区 -->
          <div v-show="activeTab === 'preview'" class="preview-area">
            <div v-if="form.content" class="markdown-body" v-html="renderedContent"></div>
            <div v-else class="empty-preview">
              <p>📝 暂无内容</p>
            </div>
          </div>
        </div>
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
  margin-bottom: var(--spacing-xl);
}

.edit-header h2 {
  margin: 0;
  color: var(--color-text);
}

.edit-actions {
  display: flex;
  gap: var(--spacing-sm);
}

/* ==================== 编辑器包装 ==================== */
.editor-wrapper {
  width: 100%;
}

.editor-tabs {
  display: flex;
  gap: var(--spacing-xs);
  border-bottom: 2px solid var(--color-divider);
  margin-bottom: 0;
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

/* ==================== 预览区 ==================== */
.preview-area {
  min-height: 400px;
  background: var(--color-bg-subtle);
  border: 1px solid var(--color-border);
  border-top: none;
  border-radius: 0 0 var(--radius-md) var(--radius-md);
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

.empty-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: var(--color-text-muted);
}
</style>
