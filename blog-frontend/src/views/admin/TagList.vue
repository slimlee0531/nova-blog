<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { tagApi } from '@/api/tag'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Tag } from '@/types'
import { Plus, EditPen, Delete, Collection } from '@element-plus/icons-vue'

const tags = ref<Tag[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref({
  name: '',
  slug: '',
  description: '',
  color: '#667eea'
})
const isEdit = ref(false)
const editId = ref<number | null>(null)

const presetColors = [
  '#667eea', '#764ba2', '#10b981', '#f59e0b', '#ef4444',
  '#ec4899', '#8b5cf6', '#06b6d4', '#84cc16', '#f97316'
]

const fetchTags = async () => {
  loading.value = true
  try {
    const res = await tagApi.getList()
    if (res.code === 200) {
      tags.value = res.data
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
  form.value = { name: '', slug: '', description: '', color: '#667eea' }
  dialogVisible.value = true
}

const handleEdit = (tag: Tag) => {
  isEdit.value = true
  editId.value = tag.id
  form.value = { name: tag.name, slug: tag.slug, description: tag.description || '', color: tag.color || '#667eea' }
  dialogVisible.value = true
}

const handleDelete = async (tag: Tag) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除标签「${tag.name}」吗？`,
      '删除标签',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res = await tagApi.delete(tag.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchTags()
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
      res = await tagApi.update(editId.value, form.value)
    } else {
      res = await tagApi.create(form.value)
    }

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      fetchTags()
    }
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

const handleCloseDialog = () => {
  form.value = { name: '', slug: '', description: '', color: '#667eea' }
}

onMounted(() => {
  fetchTags()
})
</script>

<template>
  <div class="tag-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>标签管理</h1>
        <p>共 {{ tags.length }} 个标签</p>
      </div>
      <button class="create-btn" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        <span>新建标签</span>
      </button>
    </div>

    <!-- 标签云 -->
    <div class="tags-cloud" v-loading="loading">
      <div
        v-for="tag in tags"
        :key="tag.id"
        class="tag-item"
        :style="{ '--tag-color': tag.color || '#667eea' }"
      >
        <div class="tag-content">
          <div class="tag-color" :style="{ background: tag.color || '#667eea' }"></div>
          <div class="tag-info">
            <span class="tag-name">#{{ tag.name }}</span>
            <span class="tag-count">{{ tag.articleCount }} 篇文章</span>
          </div>
        </div>
        <div class="tag-actions">
          <button class="action-btn" @click="handleEdit(tag)" title="编辑">
            <el-icon><EditPen /></el-icon>
          </button>
          <button class="action-btn delete" @click="handleDelete(tag)" title="删除">
            <el-icon><Delete /></el-icon>
          </button>
        </div>
      </div>

      <!-- 添加标签 -->
      <div class="tag-item add-tag" @click="handleCreate">
        <el-icon :size="20"><Plus /></el-icon>
        <span>新建标签</span>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && tags.length === 0" class="empty-state">
        <div class="empty-icon">🏷️</div>
        <h3>暂无标签</h3>
        <p>创建标签来分类你的文章</p>
        <button class="create-btn" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          <span>新建标签</span>
        </button>
      </div>
    </div>

    <!-- 弹窗 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="dialogVisible" class="modal-overlay" @click.self="dialogVisible = false">
          <div class="modal-content">
            <div class="modal-header">
              <h3>{{ isEdit ? '编辑标签' : '新建标签' }}</h3>
              <button class="close-btn" @click="dialogVisible = false">×</button>
            </div>

            <div class="modal-body">
              <div class="form-group">
                <label>标签名称</label>
                <input
                  v-model="form.name"
                  type="text"
                  placeholder="例如：JavaScript"
                  @keyup.enter="handleSubmit"
                />
              </div>
              <div class="form-group">
                <label>URL 标识</label>
                <input
                  v-model="form.slug"
                  type="text"
                  placeholder="例如：javascript"
                  @keyup.enter="handleSubmit"
                />
              </div>
              <div class="form-group">
                <label>描述（可选）</label>
                <textarea
                  v-model="form.description"
                  rows="2"
                  placeholder="简短描述这个标签..."
                ></textarea>
              </div>
              <div class="form-group">
                <label>标签颜色</label>
                <div class="color-picker">
                  <div
                    v-for="color in presetColors"
                    :key="color"
                    class="color-option"
                    :class="{ active: form.color === color }"
                    :style="{ background: color }"
                    @click="form.color = color"
                  ></div>
                </div>
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
.tag-list {
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

/* 标签云 */
.tags-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tag-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 12px;
  min-width: 280px;
  transition: all 0.2s ease;
}

[data-theme="dark"] .tag-item {
  background: #1a1a1a;
  border-color: #333;
}

.tag-item:hover {
  border-color: var(--tag-color);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
}

.tag-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tag-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.tag-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.tag-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
}

[data-theme="dark"] .tag-name {
  color: #fff;
}

.tag-count {
  font-size: 12px;
  color: #999;
}

.tag-actions {
  display: flex;
  gap: 6px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.tag-item:hover .tag-actions {
  opacity: 1;
}

.action-btn {
  width: 32px;
  height: 32px;
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

/* 添加标签 */
.add-tag {
  border-style: dashed;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  color: #999;
}

[data-theme="dark"] .add-tag {
  color: #666;
}

.add-tag:hover {
  border-color: #667eea;
  color: #667eea;
  background: rgba(102, 126, 234, 0.02);
}

/* 空状态 */
.empty-state {
  width: 100%;
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
  min-height: 60px;
}

/* 颜色选择器 */
.color-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.color-option {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 2px solid transparent;
}

.color-option:hover {
  transform: scale(1.1);
}

.color-option.active {
  border-color: #1a1a1a;
  box-shadow: 0 0 0 2px #fff, 0 0 0 4px currentColor;
}

[data-theme="dark"] .color-option.active {
  border-color: #fff;
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

  .tag-item {
    min-width: 100%;
  }

  .tag-actions {
    opacity: 1;
  }
}
</style>
