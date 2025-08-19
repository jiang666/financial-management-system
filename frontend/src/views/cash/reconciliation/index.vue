<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">银行对账</h2>
      <p class="description">银行对账单导入、自动对账、手工对账和余额调节表</p>
    </div>
    
    <!-- 对账步骤 -->
    <el-card class="steps-card">
      <el-steps :active="currentStep" align-center>
        <el-step title="选择账户" description="选择要对账的银行账户" />
        <el-step title="导入对账单" description="导入银行对账单数据" />
        <el-step title="自动对账" description="系统自动匹配对账" />
        <el-step title="手工调整" description="处理未匹配项目" />
        <el-step title="生成调节表" description="生成余额调节表" />
      </el-steps>
    </el-card>
    
    <!-- 对账内容 -->
    <el-card class="content-card">
      <!-- Step 1: 选择账户 -->
      <div v-if="currentStep === 0" class="step-content">
        <h3>选择银行账户</h3>
        <el-radio-group v-model="selectedBank" size="large">
          <el-radio-button v-for="bank in bankAccounts" :key="bank.id" :label="bank.id">
            {{ bank.name }} - {{ bank.accountNo }}
          </el-radio-button>
        </el-radio-group>
        
        <el-form :model="reconcileForm" label-width="120px" class="mt-lg">
          <el-form-item label="对账期间">
            <el-date-picker
              v-model="reconcileForm.period"
              type="month"
              placeholder="选择对账月份"
              format="YYYY-MM"
              value-format="YYYY-MM"
            />
          </el-form-item>
          <el-form-item label="期初余额">
            <el-input-number v-model="reconcileForm.beginBalance" :precision="2" :controls="false" style="width: 200px" />
          </el-form-item>
        </el-form>
      </div>
      
      <!-- Step 2: 导入对账单 -->
      <div v-if="currentStep === 1" class="step-content">
        <h3>导入银行对账单</h3>
        <el-upload
          class="upload-area"
          drag
          action="#"
          :before-upload="handleBeforeUpload"
          :on-change="handleFileChange"
          :auto-upload="false"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将对账单文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 Excel、CSV 格式的银行对账单
            </div>
          </template>
        </el-upload>
        
        <div v-if="importedData.length > 0" class="imported-preview">
          <h4>导入数据预览</h4>
          <el-table :data="importedData" border max-height="300">
            <el-table-column prop="date" label="日期" width="100" />
            <el-table-column prop="transNo" label="流水号" width="150" />
            <el-table-column prop="summary" label="摘要" min-width="200" />
            <el-table-column prop="amount" label="金额" width="120" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.amount) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <!-- Step 3: 自动对账 -->
      <div v-if="currentStep === 2" class="step-content">
        <h3>自动对账结果</h3>
        <el-row :gutter="20" class="match-statistics">
          <el-col :span="6">
            <el-statistic title="总笔数" :value="matchResult.total" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="已匹配" :value="matchResult.matched" value-style="color: #67c23a" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="未匹配" :value="matchResult.unmatched" value-style="color: #f56c6c" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="匹配率" :value="matchResult.rate" suffix="%" />
          </el-col>
        </el-row>
        
        <el-tabs v-model="activeTab" class="mt-lg">
          <el-tab-pane label="已匹配项目" name="matched">
            <el-table :data="matchedItems" border>
              <el-table-column prop="bankDate" label="银行日期" width="100" />
              <el-table-column prop="bankAmount" label="银行金额" width="120" align="right">
                <template #default="{ row }">
                  {{ formatMoney(row.bankAmount) }}
                </template>
              </el-table-column>
              <el-table-column prop="bankSummary" label="银行摘要" min-width="150" />
              <el-table-column prop="bookDate" label="账簿日期" width="100" />
              <el-table-column prop="bookAmount" label="账簿金额" width="120" align="right">
                <template #default="{ row }">
                  {{ formatMoney(row.bookAmount) }}
                </template>
              </el-table-column>
              <el-table-column prop="bookSummary" label="账簿摘要" min-width="150" />
            </el-table>
          </el-tab-pane>
          
          <el-tab-pane label="未匹配项目" name="unmatched">
            <el-table :data="unmatchedItems" border>
              <el-table-column prop="source" label="来源" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.source === 'bank' ? 'primary' : 'success'" size="small">
                    {{ row.source === 'bank' ? '银行' : '账簿' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="date" label="日期" width="100" />
              <el-table-column prop="transNo" label="流水号" width="150" />
              <el-table-column prop="summary" label="摘要" min-width="200" />
              <el-table-column prop="amount" label="金额" width="120" align="right">
                <template #default="{ row }">
                  {{ formatMoney(row.amount) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button link type="primary" @click="handleManualMatch(row)">手工匹配</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <!-- Step 4: 手工调整 -->
      <div v-if="currentStep === 3" class="step-content">
        <h3>手工对账调整</h3>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>银行已收企业未收</span>
              </template>
              <el-table :data="bankReceived" border>
                <el-table-column prop="date" label="日期" width="100" />
                <el-table-column prop="summary" label="摘要" />
                <el-table-column prop="amount" label="金额" width="100" align="right">
                  <template #default="{ row }">
                    {{ formatMoney(row.amount) }}
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>银行已付企业未付</span>
              </template>
              <el-table :data="bankPaid" border>
                <el-table-column prop="date" label="日期" width="100" />
                <el-table-column prop="summary" label="摘要" />
                <el-table-column prop="amount" label="金额" width="100" align="right">
                  <template #default="{ row }">
                    {{ formatMoney(row.amount) }}
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" class="mt-lg">
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>企业已收银行未收</span>
              </template>
              <el-table :data="companyReceived" border>
                <el-table-column prop="date" label="日期" width="100" />
                <el-table-column prop="summary" label="摘要" />
                <el-table-column prop="amount" label="金额" width="100" align="right">
                  <template #default="{ row }">
                    {{ formatMoney(row.amount) }}
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>企业已付银行未付</span>
              </template>
              <el-table :data="companyPaid" border>
                <el-table-column prop="date" label="日期" width="100" />
                <el-table-column prop="summary" label="摘要" />
                <el-table-column prop="amount" label="金额" width="100" align="right">
                  <template #default="{ row }">
                    {{ formatMoney(row.amount) }}
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- Step 5: 生成调节表 -->
      <div v-if="currentStep === 4" class="step-content">
        <h3>银行存款余额调节表</h3>
        <div class="reconcile-table">
          <el-descriptions :column="2" border size="large">
            <el-descriptions-item label="对账期间" :span="2">
              {{ reconcileForm.period }}
            </el-descriptions-item>
            <el-descriptions-item label="银行对账单余额">
              {{ formatMoney(reconcileResult.bankBalance) }}
            </el-descriptions-item>
            <el-descriptions-item label="企业账面余额">
              {{ formatMoney(reconcileResult.bookBalance) }}
            </el-descriptions-item>
            <el-descriptions-item label="加：银行已收企业未收">
              {{ formatMoney(reconcileResult.bankReceivedNotCompany) }}
            </el-descriptions-item>
            <el-descriptions-item label="加：企业已收银行未收">
              {{ formatMoney(reconcileResult.companyReceivedNotBank) }}
            </el-descriptions-item>
            <el-descriptions-item label="减：银行已付企业未付">
              {{ formatMoney(reconcileResult.bankPaidNotCompany) }}
            </el-descriptions-item>
            <el-descriptions-item label="减：企业已付银行未付">
              {{ formatMoney(reconcileResult.companyPaidNotBank) }}
            </el-descriptions-item>
            <el-descriptions-item label="调节后银行余额">
              <span class="adjusted-balance">{{ formatMoney(reconcileResult.adjustedBankBalance) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="调节后企业余额">
              <span class="adjusted-balance">{{ formatMoney(reconcileResult.adjustedBookBalance) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="差额" :span="2">
              <span :class="reconcileResult.difference === 0 ? 'text-success' : 'text-danger'">
                {{ formatMoney(reconcileResult.difference) }}
              </span>
            </el-descriptions-item>
          </el-descriptions>
          
          <div class="actions">
            <el-button type="primary" icon="Document" @click="handleSaveReconcile">保存对账结果</el-button>
            <el-button type="success" icon="Printer" @click="handlePrintReconcile">打印调节表</el-button>
            <el-button icon="Download" @click="handleExportReconcile">导出Excel</el-button>
          </div>
        </div>
      </div>
      
      <!-- 步骤操作按钮 -->
      <div class="step-actions">
        <el-button v-if="currentStep > 0" @click="prevStep">上一步</el-button>
        <el-button v-if="currentStep < 4" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-if="currentStep === 4" type="success" @click="completeReconcile">完成对账</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const currentStep = ref(0)
const activeTab = ref('matched')
const selectedBank = ref(1)

// 银行账户列表
const bankAccounts = ref([
  { id: 1, name: '中国银行', accountNo: '1234567890' },
  { id: 2, name: '工商银行', accountNo: '9876543210' }
])

// 对账表单
const reconcileForm = ref({
  period: '2025-08',
  beginBalance: 800000
})

// 导入的数据
const importedData = ref([])

// 匹配结果
const matchResult = ref({
  total: 50,
  matched: 42,
  unmatched: 8,
  rate: 84
})

// 已匹配项目
const matchedItems = ref([
  {
    bankDate: '2025-08-01',
    bankAmount: 50000,
    bankSummary: '转账收入',
    bookDate: '2025-08-01',
    bookAmount: 50000,
    bookSummary: '收到客户货款'
  }
])

// 未匹配项目
const unmatchedItems = ref([
  {
    source: 'bank',
    date: '2025-08-15',
    transNo: 'BOC20250815001',
    summary: '银行手续费',
    amount: 50
  }
])

// 调整项目
const bankReceived = ref([
  { date: '2025-08-15', summary: '利息收入', amount: 1200 }
])

const bankPaid = ref([
  { date: '2025-08-15', summary: '银行手续费', amount: 50 }
])

const companyReceived = ref([
  { date: '2025-08-14', summary: '收到支票', amount: 25000 }
])

const companyPaid = ref([
  { date: '2025-08-13', summary: '开出支票', amount: 15000 }
])

// 调节表结果
const reconcileResult = ref({
  bankBalance: 856000,
  bookBalance: 845000,
  bankReceivedNotCompany: 1200,
  bankPaidNotCompany: 50,
  companyReceivedNotBank: 25000,
  companyPaidNotBank: 15000,
  adjustedBankBalance: 857150,
  adjustedBookBalance: 857150,
  difference: 0
})

const formatMoney = (amount) => {
  if (!amount) return '¥0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const nextStep = () => {
  if (currentStep.value < 4) {
    if (currentStep.value === 1) {
      // 模拟导入数据
      importedData.value = [
        { date: '2025-08-01', transNo: 'BOC001', summary: '转账收入', amount: 50000 },
        { date: '2025-08-02', transNo: 'BOC002', summary: '转账支出', amount: -20000 }
      ]
    }
    currentStep.value++
  }
}

const completeReconcile = () => {
  ElMessage.success('对账完成')
  currentStep.value = 0
}

const handleBeforeUpload = (file) => {
  const isValid = file.type === 'application/vnd.ms-excel' || 
                  file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                  file.type === 'text/csv'
  
  if (!isValid) {
    ElMessage.error('只能上传 Excel 或 CSV 文件！')
    return false
  }
  
  return true
}

const handleFileChange = () => {
  ElMessage.success('文件上传成功')
}

const handleManualMatch = (row) => {
  ElMessage.info(`手工匹配：${row.summary}`)
}

const handleSaveReconcile = () => {
  ElMessage.success('对账结果已保存')
}

const handlePrintReconcile = () => {
  ElMessage.success('打印功能开发中')
}

const handleExportReconcile = () => {
  ElMessage.success('导出功能开发中')
}
</script>

<style scoped lang="scss">
.steps-card {
  margin-bottom: 20px;
}

.content-card {
  .step-content {
    min-height: 400px;
    padding: 20px;
    
    h3 {
      margin-bottom: 20px;
      color: #303133;
    }
  }
  
  .step-actions {
    text-align: center;
    padding: 20px;
    border-top: 1px solid #ebeef5;
  }
}

.upload-area {
  width: 100%;
  margin: 20px 0;
}

.imported-preview {
  margin-top: 20px;
  
  h4 {
    margin-bottom: 12px;
    color: #303133;
  }
}

.match-statistics {
  margin: 20px 0;
}

.reconcile-table {
  .adjusted-balance {
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
  
  .actions {
    margin-top: 30px;
    text-align: center;
  }
}
</style>