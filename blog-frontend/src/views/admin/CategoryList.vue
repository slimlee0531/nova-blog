<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { categoryApi } from '@/api/category'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Category } from '@/types'

const categories = ref<Category[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
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

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个分类吗？', '提示', {
      type: 'warning'
    })
    const res = await categoryApi.delete(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchCategories()
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
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<template>
  <div class="category-list">
    <div class="list-header">
      <h2>分类管理</h2>
      <el-button type="primary" @click="handleCreate">新建分类</el-button>
    </div>

    <el-table :data="categories" v-loading="loading" style="width: 100%">
      <el-table-column prop="name" label="名称" width="150" />
      <el-table-column prop="slug" label="标识" width="150" />
      <el-table-column prop="description" label="描述" min-width="200" />
      <el-table-column prop="articleCount" label="文章数" width="80" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新建分类'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="分类名称" />
        </el-form-item>
        <el-form-item label="标识">
          <el-input v-model="form.slug" placeholder="URL标识（英文）" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="分类描述" />
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
</style>
