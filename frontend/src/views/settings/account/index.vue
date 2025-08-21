<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">科目管理</h2>
      <p class="description">管理会计科目体系，设置科目编码规则</p>
    </div>
    
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="科目编码">
          <el-input v-model="searchForm.code" placeholder="请输入科目编码" clearable />
        </el-form-item>
        <el-form-item label="科目名称">
          <el-input v-model="searchForm.name" placeholder="请输入科目名称" clearable />
        </el-form-item>
        <el-form-item label="科目类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 140px">
            <el-option label="资产类" value="资产类" />
            <el-option label="负债类" value="负债类" />
            <el-option label="共同类" value="共同类" />
            <el-option label="所有者权益类" value="所有者权益类" />
            <el-option label="成本类" value="成本类" />
            <el-option label="损益类" value="损益类" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="table-wrapper">
      <div class="table-header">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增科目</el-button>
        <el-button icon="Download" @click="handleExport">导出</el-button>
        <el-button icon="Upload" @click="handleImport">导入</el-button>
        <el-button icon="Setting" @click="showCodeRuleDialog = true">编码规则设置</el-button>
      </div>
      
      <el-table
        :data="tableData"
        v-loading="loading"
        row-key="id"
        default-expand-all
        :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
      >
        <el-table-column prop="code" label="科目编码" width="120" />
        <el-table-column prop="name" label="科目名称" min-width="180" />
        <el-table-column prop="type" label="科目类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="direction" label="余额方向" width="100">
          <template #default="{ row }">
            <el-tag :type="row.direction === '借' ? 'success' : 'warning'">
              {{ row.direction }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" width="150" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.balance) }}
          </template>
        </el-table-column>
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
        <el-table-column label="操作" fixed="right" width="240">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleAddChild(row)">添加子科目</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 新增/编辑科目对话框 -->
    <el-dialog
      v-model="accountDialog"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form ref="accountFormRef" :model="accountForm" :rules="accountRules" label-width="100px">
        <el-form-item label="上级科目" v-if="accountForm.parentId">
          <el-input v-model="parentAccountName" disabled />
        </el-form-item>
        <el-form-item label="科目编码" prop="code">
          <el-input v-model="accountForm.code" placeholder="请输入科目编码" />
        </el-form-item>
        <el-form-item label="科目名称" prop="name">
          <el-input v-model="accountForm.name" placeholder="请输入科目名称" />
        </el-form-item>
        <el-form-item label="科目类型" prop="type">
          <el-select v-model="accountForm.type" placeholder="请选择科目类型">
            <el-option label="资产类" value="资产类" />
            <el-option label="负债类" value="负债类" />
            <el-option label="共同类" value="共同类" />
            <el-option label="所有者权益类" value="所有者权益类" />
            <el-option label="成本类" value="成本类" />
            <el-option label="损益类" value="损益类" />
          </el-select>
        </el-form-item>
        <el-form-item label="余额方向" prop="direction">
          <el-radio-group v-model="accountForm.direction">
            <el-radio label="借">借方</el-radio>
            <el-radio label="贷">贷方</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="辅助核算">
          <el-checkbox-group v-model="accountForm.auxiliary">
            <el-checkbox label="部门">部门核算</el-checkbox>
            <el-checkbox label="客户">客户核算</el-checkbox>
            <el-checkbox label="供应商">供应商核算</el-checkbox>
            <el-checkbox label="项目">项目核算</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="accountForm.remark" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="accountDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 编码规则设置对话框 -->
    <el-dialog v-model="showCodeRuleDialog" title="编码规则设置" width="600px">
      <el-form :model="codeRuleForm" label-width="120px">
        <el-form-item label="一级科目长度">
          <el-input-number v-model="codeRuleForm.level1" :min="2" :max="4" />
          <span class="ml-sm">位</span>
        </el-form-item>
        <el-form-item label="二级科目长度">
          <el-input-number v-model="codeRuleForm.level2" :min="2" :max="4" />
          <span class="ml-sm">位</span>
        </el-form-item>
        <el-form-item label="三级科目长度">
          <el-input-number v-model="codeRuleForm.level3" :min="2" :max="4" />
          <span class="ml-sm">位</span>
        </el-form-item>
        <el-form-item label="四级科目长度">
          <el-input-number v-model="codeRuleForm.level4" :min="2" :max="4" />
          <span class="ml-sm">位</span>
        </el-form-item>
        <el-form-item label="示例">
          <el-tag>一级: 1001</el-tag>
          <el-tag class="ml-sm">二级: 100201</el-tag>
          <el-tag class="ml-sm">三级: 10020101</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCodeRuleDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCodeRule">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { mockAccounts } from '@/api/mockData'

const loading = ref(false)
const tableData = ref([])
const accountDialog = ref(false)
const showCodeRuleDialog = ref(false)
const accountFormRef = ref()
const parentAccountName = ref('')

const searchForm = ref({
  code: '',
  name: '',
  type: ''
})

const accountForm = ref({
  id: '',
  code: '',
  name: '',
  type: '资产类',
  direction: '借',
  parentId: null,
  auxiliary: [],
  remark: ''
})

const codeRuleForm = ref({
  level1: 4,
  level2: 2,
  level3: 2,
  level4: 2
})

const accountRules = {
  code: [
    { required: true, message: '请输入科目编码', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入科目名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择科目类型', trigger: 'change' }
  ],
  direction: [
    { required: true, message: '请选择余额方向', trigger: 'change' }
  ]
}

const dialogTitle = computed(() => {
  if (accountForm.value.parentId && !accountForm.value.id) {
    return '添加子科目'
  }
  return accountForm.value.id ? '编辑科目' : '新增科目'
})

onMounted(() => {
  loadData()
})

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    // 构建树形数据
    const treeData = buildTree(mockAccounts)
    tableData.value = treeData
    loading.value = false
  }, 500)
}

const buildTree = (data) => {
  const tree = []
  const map = {}
  
  data.forEach(item => {
    map[item.id] = { ...item, children: [] }
  })
  
  data.forEach(item => {
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    } else if (!item.parentId) {
      tree.push(map[item.id])
    }
  })
  
  return tree
}

const getTypeTagType = (type) => {
  const typeMap = {
    '资产类': 'success',
    '负债类': 'warning',
    '共同类': '',
    '所有者权益类': 'info',
    '成本类': 'danger',
    '损益类': 'danger'
  }
  return typeMap[type] || ''
}

const formatMoney = (amount) => {
  if (!amount) return '0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const handleSearch = () => {
  loadData()
  ElMessage.success('搜索完成')
}

const handleReset = () => {
  searchForm.value = {
    code: '',
    name: '',
    type: ''
  }
  loadData()
}

const handleAdd = () => {
  accountForm.value = {
    id: '',
    code: '',
    name: '',
    type: '资产类',
    direction: '借',
    parentId: null,
    auxiliary: [],
    remark: ''
  }
  parentAccountName.value = ''
  accountDialog.value = true
}

const handleAddChild = (row) => {
  accountForm.value = {
    id: '',
    code: row.code,
    name: '',
    type: row.type,
    direction: row.direction,
    parentId: row.id,
    auxiliary: [],
    remark: ''
  }
  parentAccountName.value = `${row.code} ${row.name}`
  accountDialog.value = true
}

const handleEdit = (row) => {
  accountForm.value = {
    ...row,
    auxiliary: row.auxiliary || []
  }
  accountDialog.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除科目"${row.name}"吗？`,
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
  ElMessage.success(`科目"${row.name}"状态已更新`)
}

const handleSave = async () => {
  const valid = await accountFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  accountDialog.value = false
  ElMessage.success(accountForm.value.id ? '编辑成功' : '新增成功')
  loadData()
}

const resetForm = () => {
  accountFormRef.value?.resetFields()
}

const handleExport = () => {
  ElMessage.success('导出功能开发中')
}

const handleImport = () => {
  ElMessage.success('导入功能开发中')
}

const saveCodeRule = () => {
  showCodeRuleDialog.value = false
  ElMessage.success('编码规则保存成功')
}
</script>

<style scoped lang="scss">
.ml-sm {
  margin-left: 10px;
}
</style>