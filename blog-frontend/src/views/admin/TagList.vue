<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { tagApi } from '@/api/tag'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Tag } from '@/types'

const tags = ref<Tag[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = ref({
  name: '',
  slug: '',
  description: '',
  color: ''
})
const isEdit = ref(false)
const editId = ref<number | null>(null)

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
  form.value = { name: '', slug: '', description: '', color: '' }
  dialogVisible.value = true
}

const handleEdit = (tag: Tag) => {
  isEdit.value = true
  editId.value = tag.id
  form.value = { name: tag.name, slug: tag.slug, description: tag.description || '', color: tag.color || '' }
  dialogVisible.value = true
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个标签吗？', '提示', {
      type: 'warning'
    })
    const res = await tagApi.delete(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchTags()
    }
  } catch (e) {
    console.error(e)
  }
}

const handleSubmit = async () => {
  if (!form.value.name || !form.value.slug) {
    ElMessage.warning('请填写名称和标识')
    return
  }

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
  }
}

onMounted(() => {
  fetchTags()
})
</script>

<template>
  <div class="tag-list">
    <div class="list-header">
      <h2>标签管理</h2>
      <el-button type="primary" @click="handleCreate">新建标签</el-button>
    </div>

    <el-table :data="tags" v-loading="loading" style="width: 100%">
      <el-table-column prop="name" label="名称" width="150" />
      <el-table-column prop="slug" label="标识" width="150" />
      <el-table-column prop="description" label="描述" min-width="200" />
      <el-table-column prop="color" label="颜色" width="100">
        <template #default="{ row }">
          <div v-if="row.color" class="color-preview" :style="{ backgroundColor: row.color }"></div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="articleCount" label="文章数" width="80" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑标签' : '新建标签'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="标签名称" />
        </el-form-item>
        <el-form-item label="标识">
          <el-input v-model="form.slug" placeholder="URL标识（英文）" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="标签描述" />
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="form.color" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
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

.color-preview {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  border: 1px solid #ddd;
}
</style>
