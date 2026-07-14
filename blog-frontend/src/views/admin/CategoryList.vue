<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { categoryApi } from '@/api/category'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Category } from '@/types'
import { Plus, EditPen, Delete, Folder } from '@element-plus/icons-vue'

const categories = ref<Category[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref({
  name: '',
  slug: '',
  description: ''
})
const isEdit = ref(false)
const editId = ref<number | null>(null)

const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await categoryApi.getList()
    if (res.code === 200) {
      categories.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  isEdit.value = false
  editId.value = null
  form.value = { name: '', slug: '', description: '' }
  dialogVisible.value = true
}

const handleEdit = (category: Category) => {
  isEdit.value = true
  editId.value = category.id
  form.value = { name: category.name, slug: category.slug, description: category.description || '' }
  dialogVisible.value = true
}

const handleDelete = async (category: Category) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类「${category.name}」吗？`,
      '删除分类',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res = await categoryApi.delete(category.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchCategories()
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const handleSubmit = async () => {
  if (!form.value.name || !form.value.slug) {
    ElMessage.warning('请填写名称和标识')
    return
  }

  submitting.value = true
  try {
    let res
    if (isEdit.value && editId.value) {
      res = await categoryApi.update(editId.value, form.value)
    } else {
      res = await categoryApi.create(form.value)
    }

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      fetchCategories()
    }
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

const handleCloseDialog = () => {
  form.value = { name: '', slug: '', description: '' }
}

onMounted(() => {
  fetchCategories()
})
</script>

<template>
  <div class="category-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>分类管理</h1>
        <p>共 {{ categories.length }} 个分类</p>
      </div>
      <button class="create-btn" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        <span>新建分类</span>
      </button>
    </div>

    <!-- 分类网格 -->
    <div class="categories-grid" v-loading="loading">
      <div
        v-for="category in categories"
        :key="category.id"
        class="category-card"
      >
        <div class="card-icon">
          <el-icon :size="24"><Folder /></el-icon>
        </div>
        <div class="card-content">
          <h3 class="card-title">{{ category.name }}</h3>
          <p class="card-slug">/{{ category.slug }}</p>
          <p class="card-desc" v-if="category.description">{{ category.description }}</p>
          <div class="card-stats">
            <span class="stat-badge">{{ category.articleCount }} 篇文章</span>
          </div>
        </div>
        <div class="card-actions">
          <button class="action-btn" @click="handleEdit(category)" title="编辑">
            <el-icon><EditPen /></el-icon>
          </button>
          <button class="action-btn delete" @click="handleDelete(category)" title="删除">
            <el-icon><Delete /></el-icon>
          </button>
        </div>
      </div>

      <!-- 新建卡片 -->
      <div class="category-card add-card" @click="handleCreate">
        <div class="add-icon">
          <el-icon :size="32"><Plus /></el-icon>
        </div>
        <span>新建分类</span>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && categories.length === 0" class="empty-state">
        <div class="empty-icon">📁</div>
        <h3>暂无分类</h3>
        <p>创建第一个分类来组织你的文章</p>
        <button class="create-btn" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          <span>新建分类</span>
        </button>
      </div>
    </div>

    <!-- 弹窗 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="dialogVisible" class="modal-overlay" @click.self="dialogVisible = false">
          <div class="modal-content">
            <div class="modal-header">
              <h3>{{ isEdit ? '编辑分类' : '新建分类' }}</h3>
              <button class="close-btn" @click="dialogVisible = false">×</button>
            </div>

            <div class="modal-body">
              <div class="form-group">
                <label>分类名称</label>
                <input
                  v-model="form.name"
                  type="text"
                  placeholder="例如：技术笔记"
                  @keyup.enter="handleSubmit"
                />
              </div>
              <div class="form-group">
                <label>URL 标识</label>
                <input
                  v-model="form.slug"
                  type="text"
                  placeholder="例如：tech-notes"
                  @keyup.enter="handleSubmit"
                />
              </div>
              <div class="form-group">
                <label>描述（可选）</label>
                <textarea
                  v-model="form.description"
                  rows="3"
                  placeholder="简短描述这个分类..."
                ></textarea>
              </div>
            </div>

            <div class="modal-footer">
              <button class="btn-secondary" @click="dialogVisible = false">取消</button>
              <button class="btn-primary" @click="handleSubmit" :disabled="submitting">
                {{ submitting ? '保存中...' : '保存' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.category-list {
  max-width: 1200px;
  margin: 0 auto;
}

/* 页面头部 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.header-content h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 4px;
}

[data-theme="dark"] .header-content h1 {
  color: #fff;
}

.header-content p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.create-btn:hover {
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transform: translateY(-1px);
}

/* 分类网格 */
.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.category-card {
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 16px;
  padding: 24px;
  transition: all 0.2s ease;
}

[data-theme="dark"] .category-card {
  background: #1a1a1a;
  border-color: #333;
}

.category-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
}

.card-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
  color: #667eea;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.card-content {
  margin-bottom: 16px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px;
}

[data-theme="dark"] .card-title {
  color: #fff;
}

.card-slug {
  font-size: 13px;
  color: #667eea;
  margin: 0 0 8px;
  font-family: monospace;
}

.card-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

[data-theme="dark"] .card-desc {
  color: #999;
}

.card-stats {
  display: flex;
  gap: 8px;
}

.stat-badge {
  padding: 4px 10px;
  background: #f5f5f5;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  color: #666;
}

[data-theme="dark"] .stat-badge {
  background: #2a2a2a;
  color: #999;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: 1px solid #e5e5e5;
  background: #fff;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

[data-theme="dark"] .action-btn {
  background: #2a2a2a;
  border-color: #333;
  color: #999;
}

.action-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.action-btn.delete:hover {
  border-color: #ef4444;
  color: #ef4444;
  background: rgba(239, 68, 68, 0.05);
}

/* 添加卡片 */
.add-card {
  border-style: dashed;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  cursor: pointer;
}

.add-card:hover {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.02);
}

.add-icon {
  width: 64px;
  height: 64px;
  background: #f5f5f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  margin-bottom: 12px;
  transition: all 0.2s ease;
}

[data-theme="dark"] .add-icon {
  background: #2a2a2a;
  color: #666;
}

.add-card:hover .add-icon {
  background: #667eea15;
  color: #667eea;
}

.add-card span {
  font-size: 14px;
  font-weight: 500;
  color: #666;
}

[data-theme="dark"] .add-card span {
  color: #999;
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}

[data-theme="dark"] .empty-state h3 {
  color: #fff;
}

.empty-state p {
  font-size: 14px;
  color: #999;
  margin: 0 0 24px;
}

/* 弹窗 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: #fff;
  border-radius: 16px;
  width: 100%;
  max-width: 480px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .modal-content {
  background: #1a1a1a;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e5e5;
}

[data-theme="dark"] .modal-header {
  border-bottom-color: #333;
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

[data-theme="dark"] .modal-header h3 {
  color: #fff;
}

.close-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: #f5f5f5;
  color: #666;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
}

[data-theme="dark"] .close-btn {
  background: #2a2a2a;
  color: #999;
}

.close-btn:hover {
  background: #e5e5e5;
  color: #1a1a1a;
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 8px;
}

[data-theme="dark"] .form-group label {
  color: #fff;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e5e5e5;
  border-radius: 10px;
  font-size: 14px;
  color: #1a1a1a;
  background: #fff;
  transition: all 0.2s ease;
  outline: none;
  box-sizing: border-box;
}

[data-theme="dark"] .form-group input,
[data-theme="dark"] .form-group textarea {
  border-color: #333;
  background: #2a2a2a;
  color: #fff;
}

.form-group input:focus,
.form-group textarea:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-group input::placeholder,
.form-group textarea::placeholder {
  color: #999;
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #e5e5e5;
}

[data-theme="dark"] .modal-footer {
  border-top-color: #333;
}

.btn-secondary {
  padding: 10px 20px;
  background: #f5f5f5;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  transition: all 0.2s ease;
}

[data-theme="dark"] .btn-secondary {
  background: #2a2a2a;
  color: #999;
}

.btn-secondary:hover {
  background: #e5e5e5;
  color: #1a1a1a;
}

.btn-primary {
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-primary:hover {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 动画 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}

.modal-enter-active .modal-content,
.modal-leave-active .modal-content {
  transition: transform 0.2s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-content {
  transform: scale(0.95);
}

.modal-leave-to .modal-content {
  transform: scale(0.95);
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .categories-grid {
    grid-template-columns: 1fr;
  }

  .category-card {
    padding: 20px;
  }
}
</style>
