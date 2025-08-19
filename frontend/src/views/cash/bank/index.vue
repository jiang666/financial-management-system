<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">银行账户管理</h2>
      <p class="description">管理企业银行账户、日记账和流水导入</p>
    </div>
    
    <!-- 账户概览卡片 -->
    <el-row :gutter="16" class="account-cards">
      <el-col :span="6" v-for="account in accountSummary" :key="account.id">
        <el-card class="account-card" :class="{ active: selectedAccount?.id === account.id }" @click="selectAccount(account)">
          <div class="account-header">
            <el-icon :size="24" :color="account.color"><CreditCard /></el-icon>
            <el-dropdown trigger="click">
              <el-icon class="more-icon"><More /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleEditAccount(account)">编辑</el-dropdown-item>
                  <el-dropdown-item @click="handleViewDetail(account)">详情</el-dropdown-item>
                  <el-dropdown-item divided @click="handleDeleteAccount(account)" :disabled="account.isDefault">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="account-info">
            <h4>{{ account.bankName }}</h4>
            <p class="account-no">{{ formatAccountNo(account.accountNo) }}</p>
            <el-tag :type="account.isDefault ? 'danger' : 'info'" size="small">
              {{ account.accountType }}
            </el-tag>
          </div>
          <div class="account-balance">
            <p class="label">余额</p>
            <p class="amount">{{ formatMoney(account.balance) }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="account-card add-card" @click="handleAddAccount">
          <div class="add-content">
            <el-icon :size="32" color="#409eff"><Plus /></el-icon>
            <p>添加账户</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 银行日记账 -->
    <el-card class="journal-card">
      <template #header>
        <div class="card-header">
          <span>{{ selectedAccount ? `${selectedAccount.bankName} - 银行日记账` : '银行日记账' }}</span>
          <div class="header-actions">
            <el-button type="primary" icon="Upload" @click="handleImport">导入流水</el-button>
            <el-button icon="Download" @click="handleExport">导出</el-button>
            <el-button icon="Printer" @click="handlePrint">打印</el-button>
          </div>
        </div>
      </template>
      
      <!-- 搜索条件 -->
      <div class="search-form">
        <el-form :model="searchForm" inline>
          <el-form-item label="日期范围">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="交易类型">
            <el-select v-model="searchForm.transType" placeholder="请选择" clearable style="width: 120px">
              <el-option label="转入" value="in" />
              <el-option label="转出" value="out" />
            </el-select>
          </el-form-item>
          <el-form-item label="摘要">
            <el-input v-model="searchForm.summary" placeholder="请输入摘要" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
            <el-button icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 日记账表格 -->
      <el-table :data="journalData" v-loading="loading" style="width: 100%" stripe>
        <el-table-column prop="date" label="日期" width="100" />
        <el-table-column prop="voucherNo" label="凭证号" width="140" />
        <el-table-column prop="transNo" label="交易流水号" width="180" />
        <el-table-column prop="summary" label="摘要" min-width="200" />
        <el-table-column prop="oppositeAccount" label="对方单位" width="150" />
        <el-table-column prop="debit" label="借方(收入)" width="120" align="right">
          <template #default="{ row }">
            <span v-if="row.debit" class="amount-in">{{ formatMoney(row.debit) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="credit" label="贷方(支出)" width="120" align="right">
          <template #default="{ row }">
            <span v-if="row.credit" class="amount-out">{{ formatMoney(row.credit) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" width="120" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.balance) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="对账状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleReconcile(row)">对账</el-button>
            <el-button link type="info" @click="handleViewTransaction(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 汇总信息 -->
      <div class="summary-info">
        <el-descriptions :column="5" border>
          <el-descriptions-item label="期初余额">{{ formatMoney(summaryData.beginBalance) }}</el-descriptions-item>
          <el-descriptions-item label="本期收入">{{ formatMoney(summaryData.totalDebit) }}</el-descriptions-item>
          <el-descriptions-item label="本期支出">{{ formatMoney(summaryData.totalCredit) }}</el-descriptions-item>
          <el-descriptions-item label="期末余额">{{ formatMoney(summaryData.endBalance) }}</el-descriptions-item>
          <el-descriptions-item label="已对账金额">{{ formatMoney(summaryData.reconciledAmount) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
    
    <!-- 新增/编辑账户对话框 -->
    <el-dialog v-model="accountDialog" :title="accountDialogTitle" width="600px">
      <el-form :model="accountForm" :rules="accountRules" ref="accountFormRef" label-width="120px">
        <el-form-item label="银行名称" prop="bankName">
          <el-select v-model="accountForm.bankName" placeholder="请选择银行" style="width: 100%">
            <el-option label="中国银行" value="中国银行" />
            <el-option label="工商银行" value="工商银行" />
            <el-option label="建设银行" value="建设银行" />
            <el-option label="农业银行" value="农业银行" />
            <el-option label="交通银行" value="交通银行" />
            <el-option label="招商银行" value="招商银行" />
          </el-select>
        </el-form-item>
        <el-form-item label="账户类型" prop="accountType">
          <el-radio-group v-model="accountForm.accountType">
            <el-radio label="基本户">基本户</el-radio>
            <el-radio label="一般户">一般户</el-radio>
            <el-radio label="专用户">专用户</el-radio>
            <el-radio label="临时户">临时户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="账号" prop="accountNo">
          <el-input v-model="accountForm.accountNo" placeholder="请输入银行账号" />
        </el-form-item>
        <el-form-item label="账户名称" prop="accountName">
          <el-input v-model="accountForm.accountName" placeholder="请输入账户名称" />
        </el-form-item>
        <el-form-item label="开户行" prop="branchName">
          <el-input v-model="accountForm.branchName" placeholder="请输入开户行名称" />
        </el-form-item>
        <el-form-item label="初始余额">
          <el-input-number v-model="accountForm.initialBalance" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="accountForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="accountDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAccount">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 导入流水对话框 -->
    <el-dialog v-model="importDialog" title="导入银行流水" width="600px">
      <el-upload
        class="upload-demo"
        drag
        action="#"
        :before-upload="handleBeforeUpload"
        :on-change="handleFileChange"
        :auto-upload="false"
        accept=".xls,.xlsx,.csv"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 Excel、CSV 格式，文件大小不超过10MB
          </div>
        </template>
      </el-upload>
      
      <div v-if="importPreview.length > 0" class="import-preview">
        <h4>数据预览（前5条）</h4>
        <el-table :data="importPreview" border size="small">
          <el-table-column prop="date" label="日期" width="100" />
          <el-table-column prop="summary" label="摘要" />
          <el-table-column prop="amount" label="金额" width="100" />
        </el-table>
      </div>
      
      <template #footer>
        <el-button @click="importDialog = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmImport" :disabled="!importFile">确定导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const accountDialog = ref(false)
const importDialog = ref(false)
const selectedAccount = ref(null)
const accountFormRef = ref()
const importFile = ref(null)
const importPreview = ref([])

// 账户概览数据
const accountSummary = ref([
  {
    id: 1,
    bankName: '中国银行',
    accountNo: '1234567890123456',
    accountType: '基本户',
    balance: 856000,
    isDefault: true,
    color: '#409eff'
  },
  {
    id: 2,
    bankName: '工商银行',
    accountNo: '9876543210987654',
    accountType: '一般户',
    balance: 320000,
    isDefault: false,
    color: '#67c23a'
  },
  {
    id: 3,
    bankName: '建设银行',
    accountNo: '1111222233334444',
    accountType: '专用户',
    balance: 150000,
    isDefault: false,
    color: '#e6a23c'
  }
])

// 搜索表单
const searchForm = ref({
  dateRange: [],
  transType: '',
  summary: ''
})

// 账户表单
const accountForm = ref({
  id: '',
  bankName: '',
  accountType: '基本户',
  accountNo: '',
  accountName: '',
  branchName: '',
  initialBalance: 0,
  remark: ''
})

// 日记账数据
const journalData = ref([
  {
    id: 1,
    date: '2025-08-15',
    voucherNo: '记-20250815-001',
    transNo: 'BOC20250815001',
    summary: '收到客户货款',
    oppositeAccount: 'ABC公司',
    debit: 150000,
    credit: 0,
    balance: 1006000,
    status: 'reconciled'
  },
  {
    id: 2,
    date: '2025-08-14',
    voucherNo: '记-20250814-002',
    transNo: 'BOC20250814002',
    summary: '支付供应商货款',
    oppositeAccount: '材料供应商A',
    debit: 0,
    credit: 80000,
    balance: 856000,
    status: 'unreconciled'
  },
  {
    id: 3,
    date: '2025-08-13',
    voucherNo: '记-20250813-003',
    transNo: 'BOC20250813003',
    summary: '支付员工工资',
    oppositeAccount: '员工工资',
    debit: 0,
    credit: 120000,
    balance: 936000,
    status: 'reconciled'
  }
])

// 汇总数据
const summaryData = ref({
  beginBalance: 800000,
  totalDebit: 150000,
  totalCredit: 200000,
  endBalance: 750000,
  reconciledAmount: 270000
})

const accountRules = {
  bankName: [{ required: true, message: '请选择银行', trigger: 'change' }],
  accountType: [{ required: true, message: '请选择账户类型', trigger: 'change' }],
  accountNo: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  accountName: [{ required: true, message: '请输入账户名称', trigger: 'blur' }],
  branchName: [{ required: true, message: '请输入开户行', trigger: 'blur' }]
}

const accountDialogTitle = computed(() => {
  return accountForm.value.id ? '编辑银行账户' : '新增银行账户'
})

const formatMoney = (amount) => {
  if (!amount) return '¥0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const formatAccountNo = (accountNo) => {
  // 格式化账号显示，每4位加一个空格
  return accountNo.replace(/(\d{4})/g, '$1 ').trim()
}

const getStatusType = (status) => {
  const typeMap = {
    'reconciled': 'success',
    'unreconciled': 'warning',
    'error': 'danger'
  }
  return typeMap[status] || ''
}

const getStatusText = (status) => {
  const textMap = {
    'reconciled': '已对账',
    'unreconciled': '未对账',
    'error': '异常'
  }
  return textMap[status] || status
}

const selectAccount = (account) => {
  selectedAccount.value = account
  handleSearch()
}

const handleAddAccount = () => {
  accountForm.value = {
    id: '',
    bankName: '',
    accountType: '基本户',
    accountNo: '',
    accountName: '',
    branchName: '',
    initialBalance: 0,
    remark: ''
  }
  accountDialog.value = true
}

const handleEditAccount = (account) => {
  accountForm.value = { ...account }
  accountDialog.value = true
}

const handleDeleteAccount = async (account) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除银行账户"${account.bankName}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    ElMessage.success('删除成功')
  } catch {
    // 取消删除
  }
}

const handleViewDetail = (account) => {
  ElMessage.info(`查看账户详情：${account.bankName}`)
}

const handleSaveAccount = async () => {
  const valid = await accountFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  accountDialog.value = false
  ElMessage.success(accountForm.value.id ? '编辑成功' : '新增成功')
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('查询完成')
  }, 500)
}

const handleReset = () => {
  searchForm.value = {
    dateRange: [],
    transType: '',
    summary: ''
  }
}

const handleImport = () => {
  importDialog.value = true
  importFile.value = null
  importPreview.value = []
}

const handleBeforeUpload = (file) => {
  const isValid = file.type === 'application/vnd.ms-excel' || 
                  file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                  file.type === 'text/csv'
  
  if (!isValid) {
    ElMessage.error('只能上传 Excel 或 CSV 文件！')
    return false
  }
  
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB！')
    return false
  }
  
  return true
}

const handleFileChange = (file) => {
  importFile.value = file
  // 模拟预览数据
  importPreview.value = [
    { date: '2025-08-15', summary: '转账收入', amount: '50000.00' },
    { date: '2025-08-14', summary: '转账支出', amount: '-20000.00' },
    { date: '2025-08-13', summary: '利息收入', amount: '123.45' }
  ]
}

const handleConfirmImport = () => {
  if (!importFile.value) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }
  
  importDialog.value = false
  ElMessage.success('流水导入成功')
}

const handleExport = () => {
  ElMessage.success('导出功能开发中')
}

const handlePrint = () => {
  ElMessage.success('打印功能开发中')
}

const handleReconcile = (row) => {
  ElMessage.success(`对账成功：${row.transNo}`)
  row.status = 'reconciled'
}

const handleViewTransaction = (row) => {
  ElMessage.info(`查看交易详情：${row.transNo}`)
}
</script>

<style scoped lang="scss">
.account-cards {
  margin-bottom: 20px;
  
  .account-card {
    cursor: pointer;
    transition: all 0.3s ease;
    height: 180px;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
    
    &.active {
      border: 2px solid #409eff;
    }
    
    &.add-card {
      border: 2px dashed #dcdfe6;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &:hover {
        border-color: #409eff;
      }
      
      .add-content {
        text-align: center;
        color: #909399;
        
        p {
          margin-top: 8px;
        }
      }
    }
    
    .account-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      
      .more-icon {
        cursor: pointer;
        color: #909399;
        
        &:hover {
          color: #409eff;
        }
      }
    }
    
    .account-info {
      h4 {
        margin: 0 0 8px 0;
        font-size: 16px;
        color: #303133;
      }
      
      .account-no {
        margin: 8px 0;
        font-size: 13px;
        color: #606266;
        font-family: monospace;
      }
    }
    
    .account-balance {
      margin-top: 16px;
      padding-top: 12px;
      border-top: 1px solid #ebeef5;
      
      .label {
        margin: 0;
        font-size: 12px;
        color: #909399;
      }
      
      .amount {
        margin: 4px 0 0 0;
        font-size: 20px;
        font-weight: 600;
        color: #303133;
      }
    }
  }
}

.journal-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-actions {
      display: flex;
      gap: 8px;
    }
  }
}

.summary-info {
  margin-top: 20px;
}

.amount-in {
  color: #67c23a;
  font-weight: 600;
}

.amount-out {
  color: #f56c6c;
  font-weight: 600;
}

.import-preview {
  margin-top: 20px;
  
  h4 {
    margin-bottom: 12px;
    color: #303133;
  }
}

.table-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
  
  .el-button {
    margin: 0;
    padding: 4px 8px;
    white-space: nowrap;
  }
}

@media (max-width: 768px) {
  .account-cards {
    .el-col {
      margin-bottom: 16px;
    }
  }
  
  .table-actions {
    flex-direction: column;
    gap: 2px;
    
    .el-button {
      width: 100%;
      justify-content: center;
    }
  }
}
</style>