<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">凭证录入</h2>
      <p class="description">录入会计凭证，支持分录管理、模板应用和附件上传</p>
    </div>
    
    <!-- 凭证基本信息 -->
    <el-card class="voucher-header-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>凭证信息</span>
          <el-button-group>
            <el-button type="primary" icon="DocumentAdd" @click="handleNew">新建</el-button>
            <el-button icon="FolderOpened" @click="handleTemplate">选择模板</el-button>
            <el-button icon="Document" @click="handleSave" :disabled="!voucherForm.entries.length">保存</el-button>
            <el-button icon="Check" @click="handleSubmit" :disabled="!voucherForm.entries.length">提交审核</el-button>
          </el-button-group>
        </div>
      </template>
      
      <el-form :model="voucherForm" :rules="voucherRules" ref="voucherFormRef" label-width="100px" class="voucher-form">
        <el-row :gutter="24">
          <el-col :span="6">
            <el-form-item label="凭证字号" prop="voucherNo">
              <el-input v-model="voucherForm.voucherNo" placeholder="自动生成" :disabled="true" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="凭证日期" prop="voucherDate">
              <el-date-picker
                v-model="voucherForm.voucherDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="附件数量">
              <el-input-number 
                v-model="voucherForm.attachmentCount" 
                :min="0" 
                style="width: 100%" 
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="制单人">
              <el-input v-model="voucherForm.preparedBy" :disabled="true" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
    
    <!-- 分录录入 -->
    <el-card class="entries-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>分录明细</span>
          <div class="entry-controls">
            <el-button type="success" icon="Plus" @click="addEntry" size="small">添加分录</el-button>
            <el-button type="warning" icon="CopyDocument" @click="copyEntry" size="small" :disabled="!selectedEntry">复制分录</el-button>
            <el-button type="danger" icon="Delete" @click="deleteEntry" size="small" :disabled="!selectedEntry">删除分录</el-button>
          </div>
        </div>
      </template>
      
      <el-table 
        :data="voucherForm.entries" 
        @selection-change="handleEntrySelection"
        :summary-method="getEntrySummary"
        show-summary
        border
        class="entries-table"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="序号" width="80" align="center">
          <template #default="{ $index }">
            {{ $index + 1 }}
          </template>
        </el-table-column>
        <el-table-column label="科目编码" width="120">
          <template #default="{ row, $index }">
            <el-select
              v-model="row.accountCode"
              placeholder="选择科目"
              filterable
              @change="handleAccountChange(row, $index)"
              style="width: 100%"
            >
              <el-option
                v-for="account in accountOptions"
                :key="account.code"
                :label="account.code"
                :value="account.code"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="科目名称" min-width="160">
          <template #default="{ row }">
            <span>{{ row.accountName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="摘要" min-width="200">
          <template #default="{ row }">
            <el-input 
              v-model="row.summary" 
              placeholder="输入摘要"
              @change="updateSummaryForAll(row.summary)"
            />
          </template>
        </el-table-column>
        <el-table-column label="辅助核算" width="120">
          <template #default="{ row }">
            <el-button 
              v-if="row.hasAuxiliary"
              link 
              type="primary" 
              @click="openAuxiliaryDialog(row)"
              size="small"
            >
              {{ row.auxiliaryText || '设置' }}
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="借方金额" width="150" align="right">
          <template #default="{ row }">
            <el-input-number
              v-model="row.debitAmount"
              :precision="2"
              :min="0"
              :controls="false"
              @change="handleAmountChange(row, 'debit')"
              style="width: 100%"
              placeholder="0.00"
            />
          </template>
        </el-table-column>
        <el-table-column label="贷方金额" width="150" align="right">
          <template #default="{ row }">
            <el-input-number
              v-model="row.creditAmount"
              :precision="2"
              :min="0"
              :controls="false"
              @change="handleAmountChange(row, 'credit')"
              style="width: 100%"
              placeholder="0.00"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 凭证摘要和附件 -->
    <el-row :gutter="20" class="mt-lg">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>凭证摘要</span>
          </template>
          <el-input
            v-model="voucherForm.remark"
            type="textarea"
            :rows="4"
            placeholder="输入凭证备注信息..."
          />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>附件管理</span>
          </template>
          <el-upload
            class="upload-demo"
            drag
            action="#"
            multiple
            :file-list="attachmentList"
            :on-change="handleAttachmentChange"
            :auto-upload="false"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持jpg/png/pdf等格式，单个文件不超过10MB
              </div>
            </template>
          </el-upload>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 凭证模板对话框 -->
    <el-dialog v-model="templateDialog" title="选择凭证模板" width="800px">
      <el-table :data="templateList" @row-click="selectTemplate">
        <el-table-column prop="name" label="模板名称" min-width="200" />
        <el-table-column prop="description" label="模板描述" min-width="250" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="useCount" label="使用次数" width="100" align="center" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="selectTemplate(row)">选择</el-button>
            <el-button link type="success" @click="previewTemplate(row)">预览</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="templateDialog = false">取消</el-button>
      </template>
    </el-dialog>
    
    <!-- 辅助核算对话框 -->
    <el-dialog v-model="auxiliaryDialog" title="辅助核算设置" width="600px">
      <el-form :model="auxiliaryForm" label-width="100px" v-if="currentEntry">
        <el-form-item label="科目信息">
          <span>{{ currentEntry.accountCode }} - {{ currentEntry.accountName }}</span>
        </el-form-item>
        <el-form-item label="部门" v-if="currentEntry.auxiliaryTypes?.includes('部门')">
          <el-select v-model="auxiliaryForm.department" placeholder="选择部门" style="width: 100%">
            <el-option label="财务部" value="财务部" />
            <el-option label="销售部" value="销售部" />
            <el-option label="采购部" value="采购部" />
            <el-option label="生产部" value="生产部" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户" v-if="currentEntry.auxiliaryTypes?.includes('客户')">
          <el-select v-model="auxiliaryForm.customer" placeholder="选择客户" style="width: 100%">
            <el-option label="ABC公司" value="ABC公司" />
            <el-option label="XYZ公司" value="XYZ公司" />
            <el-option label="DEF公司" value="DEF公司" />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商" v-if="currentEntry.auxiliaryTypes?.includes('供应商')">
          <el-select v-model="auxiliaryForm.supplier" placeholder="选择供应商" style="width: 100%">
            <el-option label="材料供应商A" value="材料供应商A" />
            <el-option label="设备供应商B" value="设备供应商B" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目" v-if="currentEntry.auxiliaryTypes?.includes('项目')">
          <el-select v-model="auxiliaryForm.project" placeholder="选择项目" style="width: 100%">
            <el-option label="项目A" value="项目A" />
            <el-option label="项目B" value="项目B" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auxiliaryDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAuxiliary">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { mockAccounts } from '@/api/mockData'

const voucherFormRef = ref()
const templateDialog = ref(false)
const auxiliaryDialog = ref(false)
const selectedEntry = ref(null)
const currentEntry = ref(null)
const attachmentList = ref([])

const voucherForm = ref({
  voucherNo: '',
  voucherDate: '',
  attachmentCount: 0,
  preparedBy: '张三',
  remark: '',
  entries: []
})

const auxiliaryForm = ref({
  department: '',
  customer: '',
  supplier: '',
  project: ''
})

const voucherRules = {
  voucherDate: [
    { required: true, message: '请选择凭证日期', trigger: 'change' }
  ]
}

const accountOptions = ref([])
const templateList = ref([
  {
    id: 1,
    name: '销售收入凭证',
    description: '记录销售收入的标准凭证模板',
    category: '收入类',
    useCount: 25
  },
  {
    id: 2,
    name: '采购支出凭证',
    description: '记录采购支出的标准凭证模板',
    category: '成本类',
    useCount: 18
  },
  {
    id: 3,
    name: '费用报销凭证',
    description: '员工费用报销的标准凭证模板',
    category: '费用类',
    useCount: 32
  }
])

onMounted(() => {
  loadAccountOptions()
  generateVoucherNo()
  initializeVoucher()
})

const loadAccountOptions = () => {
  accountOptions.value = mockAccounts
}

const generateVoucherNo = () => {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  const serial = String(Math.floor(Math.random() * 999) + 1).padStart(3, '0')
  voucherForm.value.voucherNo = `记-${year}${month}${day}-${serial}`
  voucherForm.value.voucherDate = `${year}-${month}-${day}`
}

const initializeVoucher = () => {
  addEntry()
  addEntry()
}

const addEntry = () => {
  const newEntry = {
    id: Date.now() + Math.random(),
    accountCode: '',
    accountName: '',
    summary: '',
    auxiliaryTypes: [],
    hasAuxiliary: false,
    auxiliaryText: '',
    debitAmount: 0,
    creditAmount: 0
  }
  voucherForm.value.entries.push(newEntry)
}

const copyEntry = () => {
  if (!selectedEntry.value || selectedEntry.value.length === 0) {
    ElMessage.warning('请先选择要复制的分录')
    return
  }
  
  selectedEntry.value.forEach(entry => {
    const newEntry = {
      ...entry,
      id: Date.now() + Math.random(),
      debitAmount: 0,
      creditAmount: 0
    }
    voucherForm.value.entries.push(newEntry)
  })
  ElMessage.success('分录复制成功')
}

const deleteEntry = () => {
  if (!selectedEntry.value || selectedEntry.value.length === 0) {
    ElMessage.warning('请先选择要删除的分录')
    return
  }
  
  selectedEntry.value.forEach(entry => {
    const index = voucherForm.value.entries.findIndex(item => item.id === entry.id)
    if (index > -1) {
      voucherForm.value.entries.splice(index, 1)
    }
  })
  selectedEntry.value = []
  ElMessage.success('分录删除成功')
}

const handleEntrySelection = (selection) => {
  selectedEntry.value = selection
}

const handleAccountChange = (row, index) => {
  const account = accountOptions.value.find(acc => acc.code === row.accountCode)
  if (account) {
    row.accountName = account.name
    row.hasAuxiliary = account.auxiliary && account.auxiliary.length > 0
    row.auxiliaryTypes = account.auxiliary || []
    row.auxiliaryText = ''
  }
}

const updateSummaryForAll = (summary) => {
  voucherForm.value.entries.forEach(entry => {
    if (!entry.summary) {
      entry.summary = summary
    }
  })
}

const handleAmountChange = (row, type) => {
  if (type === 'debit' && row.debitAmount > 0) {
    row.creditAmount = 0
  } else if (type === 'credit' && row.creditAmount > 0) {
    row.debitAmount = 0
  }
}

const getEntrySummary = (param) => {
  const { columns, data } = param
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (column.label === '借方金额') {
      const values = data.map(item => Number(item.debitAmount) || 0)
      const total = values.reduce((prev, curr) => prev + curr, 0)
      sums[index] = total.toFixed(2)
    } else if (column.label === '贷方金额') {
      const values = data.map(item => Number(item.creditAmount) || 0)
      const total = values.reduce((prev, curr) => prev + curr, 0)
      sums[index] = total.toFixed(2)
    } else {
      sums[index] = ''
    }
  })
  return sums
}

const balanceCheck = computed(() => {
  const totalDebit = voucherForm.value.entries.reduce((sum, entry) => sum + (entry.debitAmount || 0), 0)
  const totalCredit = voucherForm.value.entries.reduce((sum, entry) => sum + (entry.creditAmount || 0), 0)
  return Math.abs(totalDebit - totalCredit) < 0.01
})

const openAuxiliaryDialog = (row) => {
  currentEntry.value = row
  auxiliaryForm.value = {
    department: '',
    customer: '',
    supplier: '',
    project: ''
  }
  auxiliaryDialog.value = true
}

const saveAuxiliary = () => {
  if (currentEntry.value) {
    const texts = []
    if (auxiliaryForm.value.department) texts.push(`部门:${auxiliaryForm.value.department}`)
    if (auxiliaryForm.value.customer) texts.push(`客户:${auxiliaryForm.value.customer}`)
    if (auxiliaryForm.value.supplier) texts.push(`供应商:${auxiliaryForm.value.supplier}`)
    if (auxiliaryForm.value.project) texts.push(`项目:${auxiliaryForm.value.project}`)
    
    currentEntry.value.auxiliaryText = texts.join(', ')
  }
  auxiliaryDialog.value = false
}

const handleAttachmentChange = (file, fileList) => {
  attachmentList.value = fileList
  voucherForm.value.attachmentCount = fileList.length
}

const handleNew = () => {
  voucherForm.value = {
    voucherNo: '',
    voucherDate: '',
    attachmentCount: 0,
    preparedBy: '张三',
    remark: '',
    entries: []
  }
  generateVoucherNo()
  initializeVoucher()
  attachmentList.value = []
  ElMessage.success('新建凭证成功')
}

const handleTemplate = () => {
  templateDialog.value = true
}

const selectTemplate = (template) => {
  ElMessage.success(`已选择模板：${template.name}`)
  templateDialog.value = false
  // 这里可以根据模板加载预设的分录
}

const previewTemplate = (template) => {
  ElMessage.info(`预览模板：${template.name}`)
}

const handleSave = async () => {
  if (!balanceCheck.value) {
    ElMessage.error('借贷方金额不平衡，无法保存')
    return
  }
  
  try {
    await voucherFormRef.value.validate()
    ElMessage.success('凭证保存成功')
  } catch {
    ElMessage.error('请检查必填项')
  }
}

const handleSubmit = async () => {
  if (!balanceCheck.value) {
    ElMessage.error('借贷方金额不平衡，无法提交')
    return
  }
  
  try {
    await voucherFormRef.value.validate()
    await ElMessageBox.confirm('确定要提交凭证进行审核吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('凭证提交成功，等待审核')
  } catch {
    // 取消提交
  }
}
</script>

<style scoped lang="scss">
.voucher-header-card {
  margin-bottom: 20px;
  
  .voucher-form {
    .el-form-item {
      margin-bottom: 16px;
    }
  }
}

.entries-card {
  margin-bottom: 20px;
  
  .entry-controls {
    display: flex;
    gap: 8px;
  }
  
  .entries-table {
    .el-input-number {
      .el-input__inner {
        text-align: right;
      }
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  span {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}

.upload-demo {
  .el-upload {
    width: 100%;
  }
  
  .el-upload-dragger {
    width: 100%;
    height: 140px;
  }
}

// 借贷平衡状态样式
.balance-indicator {
  position: fixed;
  top: 50%;
  right: 20px;
  transform: translateY(-50%);
  padding: 12px 16px;
  border-radius: 8px;
  color: white;
  font-weight: 600;
  z-index: 1000;
  
  &.balanced {
    background: #67C23A;
  }
  
  &.unbalanced {
    background: #F56C6C;
  }
}

@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }
  
  .entry-controls {
    flex-direction: column;
    
    .el-button {
      margin-bottom: 8px;
    }
  }
  
  .entries-table {
    overflow-x: auto;
  }
}
</style>