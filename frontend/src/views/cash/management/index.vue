<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">现金管理</h2>
      <p class="description">管理现金收支、日记账和现金盘点</p>
    </div>
    
    <!-- 现金账户概览 -->
    <el-row :gutter="20" class="cash-overview">
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="现金余额" :value="cashBalance" :precision="2" prefix="¥">
            <template #suffix>
              <el-tag type="success" size="small">正常</el-tag>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="今日收入" :value="todayIncome" :precision="2" prefix="¥">
            <template #suffix>
              <span class="trend-up">
                <el-icon><ArrowUp /></el-icon>
                12%
              </span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="今日支出" :value="todayExpense" :precision="2" prefix="¥">
            <template #suffix>
              <span class="trend-down">
                <el-icon><ArrowDown /></el-icon>
                8%
              </span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="待报销单据" :value="pendingReimbursement">
            <template #suffix>
              <el-button type="primary" link size="small">处理</el-button>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 功能区 -->
    <el-tabs v-model="activeTab">
      <!-- 现金日记账 -->
      <el-tab-pane label="现金日记账" name="journal">
        <div class="search-form">
          <el-form :model="journalSearchForm" inline>
            <el-form-item label="日期范围">
              <el-date-picker
                v-model="journalSearchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item label="摘要">
              <el-input v-model="journalSearchForm.summary" placeholder="请输入摘要" clearable />
            </el-form-item>
            <el-form-item label="类型">
              <el-select v-model="journalSearchForm.type" placeholder="请选择" clearable style="width: 120px">
                <el-option label="收入" value="income" />
                <el-option label="支出" value="expense" />
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
            <el-button type="primary" icon="Plus" @click="handleAddEntry">新增记录</el-button>
            <el-button icon="Download" @click="handleExport">导出日记账</el-button>
            <el-button icon="Printer" @click="handlePrint">打印</el-button>
          </div>
          
          <el-table :data="journalData" v-loading="loading" @selection-change="handleSelectionChange" style="width: 100%" stripe>
            <el-table-column type="selection" width="55" />
            <el-table-column prop="date" label="日期" width="110" />
            <el-table-column prop="voucherNo" label="凭证号" width="140" show-overflow-tooltip />
            <el-table-column prop="summary" label="摘要" min-width="150" show-overflow-tooltip />
            <el-table-column prop="oppositeAccount" label="对方科目" min-width="120" show-overflow-tooltip />
            <el-table-column prop="income" label="收入" width="120" align="right">
              <template #default="{ row }">
                <span v-if="row.income" class="amount-income">{{ formatMoney(row.income) }}</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="expense" label="支出" width="120" align="right">
              <template #default="{ row }">
                <span v-if="row.expense" class="amount-expense">{{ formatMoney(row.expense) }}</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="balance" label="余额" width="120" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.balance) }}
              </template>
            </el-table-column>
            <el-table-column prop="operator" label="经办人" width="100" show-overflow-tooltip />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
                  <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="table-footer">
            <el-descriptions :column="4" border>
              <el-descriptions-item label="期初余额">{{ formatMoney(summaryData.beginBalance) }}</el-descriptions-item>
              <el-descriptions-item label="本期收入">{{ formatMoney(summaryData.totalIncome) }}</el-descriptions-item>
              <el-descriptions-item label="本期支出">{{ formatMoney(summaryData.totalExpense) }}</el-descriptions-item>
              <el-descriptions-item label="期末余额">{{ formatMoney(summaryData.endBalance) }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </el-tab-pane>
      
      <!-- 收支登记 -->
      <el-tab-pane label="收支登记" name="register">
        <el-card>
          <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="业务类型" prop="businessType">
                  <el-radio-group v-model="registerForm.businessType">
                    <el-radio-button label="income">现金收入</el-radio-button>
                    <el-radio-button label="expense">现金支出</el-radio-button>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="日期" prop="date">
                  <el-date-picker
                    v-model="registerForm.date"
                    type="date"
                    placeholder="选择日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="金额" prop="amount">
                  <el-input-number
                    v-model="registerForm.amount"
                    :min="0"
                    :precision="2"
                    style="width: 100%"
                    placeholder="请输入金额"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="对方科目" prop="oppositeAccount">
                  <el-select v-model="registerForm.oppositeAccount" placeholder="请选择科目" style="width: 100%">
                    <el-option label="应收账款" value="1122" />
                    <el-option label="应付账款" value="2202" />
                    <el-option label="管理费用" value="6602" />
                    <el-option label="销售费用" value="6601" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item label="摘要" prop="summary">
              <el-input v-model="registerForm.summary" type="textarea" :rows="2" placeholder="请输入摘要" />
            </el-form-item>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="经办人" prop="operator">
                  <el-input v-model="registerForm.operator" placeholder="请输入经办人" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="附件数量">
                  <el-input-number v-model="registerForm.attachmentCount" :min="0" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item label="备注">
              <el-input v-model="registerForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleSubmitRegister">提交</el-button>
              <el-button @click="handleResetRegister">重置</el-button>
              <el-button type="success" @click="handleSaveTemplate">保存为模板</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <!-- 现金盘点 -->
      <el-tab-pane label="现金盘点" name="inventory">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>现金盘点表</span>
              <el-button type="primary" @click="handleNewInventory">新建盘点</el-button>
            </div>
          </template>
          
          <el-form :model="inventoryForm" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="盘点日期">
                  <el-date-picker
                    v-model="inventoryForm.date"
                    type="date"
                    placeholder="选择日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="盘点人">
                  <el-input v-model="inventoryForm.operator" placeholder="请输入盘点人" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
          
          <!-- 现金明细 -->
          <el-table :data="inventoryDetails" border show-summary :summary-method="getInventorySummary" style="width: 100%" stripe>
            <el-table-column prop="denomination" label="面额" width="100" />
            <el-table-column prop="quantity" label="数量" width="150">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.quantity"
                  :min="0"
                  @change="calculateInventory"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" width="150" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.amount) }}
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 盘点结果 -->
          <el-descriptions :column="2" border class="mt-lg">
            <el-descriptions-item label="实盘金额">
              <span class="amount-text">{{ formatMoney(inventoryResult.actual) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="账面金额">
              <span class="amount-text">{{ formatMoney(inventoryResult.book) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="差额">
              <span :class="inventoryResult.difference !== 0 ? 'text-danger' : 'text-success'">
                {{ formatMoney(inventoryResult.difference) }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="盘点结果">
              <el-tag :type="getInventoryResultType()">
                {{ getInventoryResultText() }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
          
          <div class="mt-lg">
            <el-button type="primary" @click="handleSaveInventory">保存盘点</el-button>
            <el-button type="success" @click="handleConfirmInventory">确认盘点</el-button>
            <el-button @click="handlePrintInventory">打印盘点表</el-button>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 新增/编辑现金记录对话框 -->
    <el-dialog v-model="entryDialog" :title="entryDialogTitle" width="600px">
      <el-form :model="entryForm" :rules="entryRules" ref="entryFormRef" label-width="100px">
        <el-form-item label="日期" prop="date">
          <el-date-picker
            v-model="entryForm.date"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="entryForm.type">
            <el-radio label="income">收入</el-radio>
            <el-radio label="expense">支出</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="entryForm.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="摘要" prop="summary">
          <el-input v-model="entryForm.summary" placeholder="请输入摘要" />
        </el-form-item>
        <el-form-item label="对方科目" prop="oppositeAccount">
          <el-select v-model="entryForm.oppositeAccount" placeholder="请选择科目" style="width: 100%">
            <el-option label="应收账款" value="1122" />
            <el-option label="应付账款" value="2202" />
            <el-option label="管理费用" value="6602" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="entryDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEntry">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('journal')
const loading = ref(false)
const entryDialog = ref(false)
const selectedRows = ref([])
const registerFormRef = ref()
const entryFormRef = ref()

// 统计数据
const cashBalance = ref(58600.00)
const todayIncome = ref(12500.00)
const todayExpense = ref(8900.00)
const pendingReimbursement = ref(5)

// 搜索表单
const journalSearchForm = ref({
  dateRange: [],
  summary: '',
  type: ''
})

// 登记表单
const registerForm = ref({
  businessType: 'income',
  date: '',
  amount: 0,
  oppositeAccount: '',
  summary: '',
  operator: '',
  attachmentCount: 0,
  remark: ''
})

// 盘点表单
const inventoryForm = ref({
  date: new Date().toISOString().split('T')[0],
  operator: '出纳员'
})

// 日记账数据
const journalData = ref([
  {
    id: 1,
    date: '2025-08-15',
    voucherNo: '现收-001',
    summary: '收到客户货款',
    oppositeAccount: '应收账款',
    income: 15000,
    expense: 0,
    balance: 73600,
    operator: '张三'
  },
  {
    id: 2,
    date: '2025-08-15',
    voucherNo: '现付-001',
    summary: '支付办公用品费',
    oppositeAccount: '管理费用',
    income: 0,
    expense: 2500,
    balance: 71100,
    operator: '李四'
  },
  {
    id: 3,
    date: '2025-08-14',
    voucherNo: '现收-002',
    summary: '收到销售款',
    oppositeAccount: '主营业务收入',
    income: 8000,
    expense: 0,
    balance: 73600,
    operator: '张三'
  }
])

// 汇总数据
const summaryData = ref({
  beginBalance: 50000,
  totalIncome: 23000,
  totalExpense: 14400,
  endBalance: 58600
})

// 盘点明细
const inventoryDetails = ref([
  { denomination: '100元', quantity: 500, amount: 50000 },
  { denomination: '50元', quantity: 100, amount: 5000 },
  { denomination: '20元', quantity: 150, amount: 3000 },
  { denomination: '10元', quantity: 50, amount: 500 },
  { denomination: '5元', quantity: 20, amount: 100 },
  { denomination: '1元', quantity: 0, amount: 0 },
  { denomination: '硬币', quantity: 0, amount: 0 }
])

// 盘点结果
const inventoryResult = ref({
  actual: 58600,
  book: 58600,
  difference: 0
})

// 新增/编辑表单
const entryForm = ref({
  id: '',
  date: '',
  type: 'income',
  amount: 0,
  summary: '',
  oppositeAccount: ''
})

const registerRules = {
  businessType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  date: [{ required: true, message: '请选择日期', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  oppositeAccount: [{ required: true, message: '请选择对方科目', trigger: 'change' }],
  summary: [{ required: true, message: '请输入摘要', trigger: 'blur' }],
  operator: [{ required: true, message: '请输入经办人', trigger: 'blur' }]
}

const entryRules = {
  date: [{ required: true, message: '请选择日期', trigger: 'change' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  summary: [{ required: true, message: '请输入摘要', trigger: 'blur' }],
  oppositeAccount: [{ required: true, message: '请选择对方科目', trigger: 'change' }]
}

const entryDialogTitle = computed(() => {
  return entryForm.value.id ? '编辑现金记录' : '新增现金记录'
})

const formatMoney = (amount) => {
  if (!amount) return '¥0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('查询完成')
  }, 500)
}

const handleReset = () => {
  journalSearchForm.value = {
    dateRange: [],
    summary: '',
    type: ''
  }
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleAddEntry = () => {
  entryForm.value = {
    id: '',
    date: new Date().toISOString().split('T')[0],
    type: 'income',
    amount: 0,
    summary: '',
    oppositeAccount: ''
  }
  entryDialog.value = true
}

const handleEdit = (row) => {
  entryForm.value = {
    id: row.id,
    date: row.date,
    type: row.income > 0 ? 'income' : 'expense',
    amount: row.income || row.expense,
    summary: row.summary,
    oppositeAccount: row.oppositeAccount
  }
  entryDialog.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除该条记录吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('删除成功')
  } catch {
    // 取消删除
  }
}

const handleSaveEntry = async () => {
  const valid = await entryFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  entryDialog.value = false
  ElMessage.success(entryForm.value.id ? '编辑成功' : '新增成功')
}

const handleExport = () => {
  ElMessage.success('导出功能开发中')
}

const handlePrint = () => {
  ElMessage.success('打印功能开发中')
}

const handleSubmitRegister = async () => {
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  ElMessage.success('登记成功')
  handleResetRegister()
}

const handleResetRegister = () => {
  registerFormRef.value?.resetFields()
}

const handleSaveTemplate = () => {
  ElMessage.success('模板保存成功')
}

const handleNewInventory = () => {
  inventoryDetails.value.forEach(item => {
    item.quantity = 0
    item.amount = 0
  })
  calculateInventory()
}

const calculateInventory = () => {
  inventoryDetails.value.forEach(item => {
    const denomination = parseFloat(item.denomination.replace(/[^\d.]/g, '')) || 0
    item.amount = denomination * item.quantity
  })
  
  const actual = inventoryDetails.value.reduce((sum, item) => sum + item.amount, 0)
  inventoryResult.value.actual = actual
  inventoryResult.value.difference = actual - inventoryResult.value.book
}

const getInventorySummary = (param) => {
  const { columns, data } = param
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (column.property === 'amount') {
      const values = data.map(item => Number(item.amount))
      sums[index] = formatMoney(values.reduce((prev, curr) => prev + curr, 0))
    } else {
      sums[index] = ''
    }
  })
  return sums
}

const getInventoryResultType = () => {
  if (inventoryResult.value.difference === 0) return 'success'
  if (Math.abs(inventoryResult.value.difference) < 100) return 'warning'
  return 'danger'
}

const getInventoryResultText = () => {
  if (inventoryResult.value.difference === 0) return '账实相符'
  if (inventoryResult.value.difference > 0) return '盘盈'
  return '盘亏'
}

const handleSaveInventory = () => {
  ElMessage.success('盘点记录已保存')
}

const handleConfirmInventory = () => {
  ElMessage.success('盘点已确认')
}

const handlePrintInventory = () => {
  ElMessage.success('打印功能开发中')
}

onMounted(() => {
  calculateInventory()
})
</script>

<style scoped lang="scss">
.cash-overview {
  margin-bottom: 20px;
  
  .stat-card {
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
    
    .trend-up {
      color: #67c23a;
      font-size: 12px;
      
      .el-icon {
        vertical-align: middle;
      }
    }
    
    .trend-down {
      color: #f56c6c;
      font-size: 12px;
      
      .el-icon {
        vertical-align: middle;
      }
    }
  }
}

.table-footer {
  margin-top: 20px;
}

.amount-income {
  color: #67c23a;
  font-weight: 600;
}

.amount-expense {
  color: #f56c6c;
  font-weight: 600;
}

.amount-text {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.text-success {
  color: #67c23a;
  font-weight: 600;
}

.text-danger {
  color: #f56c6c;
  font-weight: 600;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  .cash-overview {
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