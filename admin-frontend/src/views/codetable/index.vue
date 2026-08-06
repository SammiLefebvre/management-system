<template>
  <div class="codetable-page">
    <AppCard class="page-card">
      <template #header>
        <div class="page-header">
          <span>码表管理</span>
          <el-button type="primary" round @click="showDialog = true">新增码值</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="fetchData">
        <el-tab-pane label="紧急程度" name="emergency_level" />
        <el-tab-pane label="项目组名" name="project_group" />
        <el-tab-pane label="工单类型" name="work_order_type" />
        <el-tab-pane label="故障类型" name="fault_type" />
      </el-tabs>

      <el-table :data="tableData">
        <el-table-column prop="codeValue" label="码值" width="150" />
        <el-table-column prop="codeLabel" label="显示名称" width="200" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" round @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" round @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </AppCard>

    <el-dialog v-model="showDialog" title="编辑码值" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="码值"><el-input v-model="form.codeValue" /></el-form-item>
        <el-form-item label="显示名"><el-input v-model="form.codeLabel" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="showDialog = false">取消</el-button>
        <el-button type="primary" round @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { useCodeTable } from '@/composables/useCodeTable'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppCard from '@/components/AppCard.vue'

const activeTab = ref('emergency_level')
const showDialog = ref(false)
const editingId = ref<number | null>(null)
const form = reactive({ codeType: '', codeValue: '', codeLabel: '', sortOrder: 0 })
const { cache, load, refresh, add, update, remove } = useCodeTable()

const tableData = computed(() => cache.value[activeTab.value] || [])

async function fetchData(type?: string) {
  await load(type || activeTab.value)
}

function handleEdit(row: any) {
  editingId.value = row.id
  form.codeType = row.codeType
  form.codeValue = row.codeValue
  form.codeLabel = row.codeLabel
  form.sortOrder = row.sortOrder
  showDialog.value = true
}

async function handleSave() {
  if (editingId.value) {
    await update(activeTab.value, { ...form, id: editingId.value })
    ElMessage.success('修改成功')
  } else {
    await add(activeTab.value, { ...form, codeType: activeTab.value })
    ElMessage.success('新增成功')
  }
  showDialog.value = false
  editingId.value = null
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    await remove(activeTab.value, id)
    ElMessage.success('已删除')
  } catch { /* 取消 */ }
}

watch(activeTab, (type) => fetchData(type), { immediate: true })
</script>

<script lang="ts">
export default { name: 'CodeTablePage' }
</script>

<style scoped>
.codetable-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.page-card {
  flex: 1;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 18px;
}
</style>
