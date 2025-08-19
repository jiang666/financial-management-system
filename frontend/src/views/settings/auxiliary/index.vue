<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">辅助核算</h2>
      <p class="description">设置辅助核算项目，配置部门、客户、供应商、项目等核算维度</p>
    </div>
    
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="核算类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 120px">
            <el-option label="部门" value="部门" />
            <el-option label="客户" value="客户" />
            <el-option label="供应商" value="供应商" />
            <el-option label="项目" value="项目" />
          </el-select>
        </el-form-item>
        <el-form-item label="核算编码">
          <el-input v-model="searchForm.code" placeholder="请输入编码" clearable />
        </el-form-item>
        <el-form-item label="核算名称">
          <el-input v-model="searchForm.name" placeholder="请输入名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="table-wrapper">
      <div class="table-header">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增辅助核算</el-button>
        <el-button icon="Download" @click="handleExport">导出</el-button>
        <el-button icon="Upload" @click="handleImport">批量导入</el-button>
      </div>
      
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="type" label="核算类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="name" label="名称" min-width="200" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="启用"
              inactive-value="停用"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :small="false"
          :disabled="false"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="auxDialog"
      :title="dialogTitle"
      width="500px"
      @close="resetForm"
    >
      <el-form ref="auxFormRef" :model="auxForm" :rules="auxRules" label-width="100px">
        <el-form-item label="核算类型" prop="type">
          <el-select v-model="auxForm.type" placeholder="请选择核算类型">
            <el-option label="部门" value="部门" />
            <el-option label="客户" value="客户" />
            <el-option label="供应商" value="供应商" />
            <el-option label="项目" value="项目" />
          </el-select>
        </el-form-item>
        <el-form-item label="编码" prop="code">
          <el-input v-model="auxForm.code" placeholder="请输入编码" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="auxForm.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="联系人" v-if="auxForm.type === '客户' || auxForm.type === '供应商'">
          <el-input v-model="auxForm.contact" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" v-if="auxForm.type === '客户' || auxForm.type === '供应商'">
          <el-input v-model="auxForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="地址" v-if="auxForm.type === '客户' || auxForm.type === '供应商'">
          <el-input v-model="auxForm.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="负责人" v-if="auxForm.type === '部门' || auxForm.type === '项目'">
          <el-input v-model="auxForm.manager" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="项目预算" v-if="auxForm.type === '项目'">
          <el-input-number v-model="auxForm.budget" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="项目期限" v-if="auxForm.type === '项目'">
          <el-date-picker
            v-model="auxForm.deadline"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="auxForm.remark" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auxDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { mockAuxiliaryItems } from '@/api/mockData'

const loading = ref(false)
const tableData = ref([])
const auxDialog = ref(false)
const auxFormRef = ref()
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = ref({
  type: '',
  code: '',
  name: ''
})

const auxForm = ref({
  id: '',
  type: '部门',
  code: '',
  name: '',
  contact: '',
  phone: '',
  address: '',
  manager: '',
  budget: 0,
  deadline: '',
  remark: ''
})

const auxRules = {
  type: [
    { required: true, message: '请选择核算类型', trigger: 'change' }
  ],
  code: [
    { required: true, message: '请输入编码', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => {
  return auxForm.value.id ? '编辑辅助核算' : '新增辅助核算'
})

onMounted(() => {
  loadData()
})

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    tableData.value = mockAuxiliaryItems
    total.value = mockAuxiliaryItems.length
    loading.value = false
  }, 500)
}

const getTypeTagType = (type) => {
  const typeMap = {
    '部门': 'success',
    '客户': 'warning',
    '供应商': 'info',
    '项目': ''
  }
  return typeMap[type] || ''
}

const handleSearch = () => {
  loadData()
  ElMessage.success('搜索完成')
}

const handleReset = () => {
  searchForm.value = {
    type: '',
    code: '',
    name: ''
  }
  loadData()
}

const handleAdd = () => {
  auxForm.value = {
    id: '',
    type: '部门',
    code: '',
    name: '',
    contact: '',
    phone: '',
    address: '',
    manager: '',
    budget: 0,
    deadline: '',
    remark: ''
  }
  auxDialog.value = true
}

const handleEdit = (row) => {
  auxForm.value = { ...row }
  auxDialog.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除辅助核算项"${row.name}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // 取消删除
  }
}

const handleStatusChange = (row) => {
  ElMessage.success(`辅助核算项"${row.name}"状态已更新`)
}

const handleSave = async () => {
  const valid = await auxFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  auxDialog.value = false
  ElMessage.success(auxForm.value.id ? '编辑成功' : '新增成功')
  loadData()
}

const resetForm = () => {
  auxFormRef.value?.resetFields()
}

const handleExport = () => {
  ElMessage.success('导出功能开发中')
}

const handleImport = () => {
  ElMessage.success('导入功能开发中')
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadData()
}
</script>