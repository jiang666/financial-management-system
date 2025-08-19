<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">期末结账</h2>
      <p class="description">执行月末结账流程，包括结账检查、损益结转和期末调汇</p>
    </div>
    
    <!-- 结账状态 -->
    <el-card class="status-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>结账状态</span>
          <el-tag :type="getStatusTagType(currentPeriod.status)" size="large">
            {{ getStatusText(currentPeriod.status) }}
          </el-tag>
        </div>
      </template>
      
      <el-descriptions :column="3" border>
        <el-descriptions-item label="当前期间">{{ currentPeriod.period }}</el-descriptions-item>
        <el-descriptions-item label="结账状态">{{ getStatusText(currentPeriod.status) }}</el-descriptions-item>
        <el-descriptions-item label="结账时间">{{ currentPeriod.closingTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结账人">{{ currentPeriod.closingBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="总凭证数">{{ currentPeriod.totalVouchers }}</el-descriptions-item>
        <el-descriptions-item label="未记账凭证">{{ currentPeriod.unpostedVouchers }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
    
    <!-- 结账流程 -->
    <el-card class="process-card" shadow="never">
      <template #header>
        <span>结账流程</span>
      </template>
      
      <el-steps :active="currentStep" align-center>
        <el-step 
          v-for="(step, index) in closingSteps" 
          :key="index"
          :title="step.title"
          :description="step.description"
          :status="step.status"
        />
      </el-steps>
      
      <div class="step-content">
        <!-- 结账检查 -->
        <div v-if="currentStep === 0" class="step-panel">
          <h3>结账检查</h3>
          <el-button type="primary" @click="runPreCheck" :loading="checking">
            <el-icon><DocumentChecked /></el-icon>
            开始检查
          </el-button>
          
          <div v-if="checkResults.length > 0" class="check-results mt-lg">
            <el-alert
              :title="hasErrors ? '检查发现问题，请先处理' : '检查通过，可以继续结账'"
              :type="hasErrors ? 'error' : 'success'"
              show-icon
            />
            
            <el-table :data="checkResults" class="mt-md">
              <el-table-column label="检查项" prop="item" />
              <el-table-column label="状态" prop="status" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'pass' ? 'success' : 'danger'" size="small">
                    {{ row.status === 'pass' ? '通过' : '失败' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="说明" prop="message" />
            </el-table>
          </div>
        </div>
        
        <!-- 损益结转 -->
        <div v-if="currentStep === 1" class="step-panel">
          <h3>损益结转</h3>
          <p>自动生成损益结转凭证，将收入、费用科目余额结转至本年利润</p>
          
          <el-table :data="profitLossData" border>
            <el-table-column label="科目编码" prop="accountCode" width="120" />
            <el-table-column label="科目名称" prop="accountName" min-width="160" />
            <el-table-column label="本期发生额" prop="currentAmount" width="120" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.currentAmount) }}
              </template>
            </el-table-column>
            <el-table-column label="结转金额" prop="transferAmount" width="120" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.transferAmount) }}
              </template>
            </el-table-column>
          </el-table>
          
          <div class="mt-lg">
            <el-button type="primary" @click="generateProfitLossVoucher" :loading="generating">
              生成损益结转凭证
            </el-button>
          </div>
        </div>
        
        <!-- 期末调汇 -->
        <div v-if="currentStep === 2" class="step-panel">
          <h3>期末调汇</h3>
          <p>对外币科目按期末汇率调整，产生汇兑损益</p>
          
          <el-table :data="exchangeData" border>
            <el-table-column label="科目" prop="accountName" />
            <el-table-column label="币种" prop="currency" width="80" />
            <el-table-column label="外币金额" prop="foreignAmount" width="120" align="right" />
            <el-table-column label="账面汇率" prop="bookRate" width="100" align="right" />
            <el-table-column label="期末汇率" prop="endRate" width="100" align="right" />
            <el-table-column label="汇兑损益" prop="exchangeGain" width="120" align="right">
              <template #default="{ row }">
                <span :class="row.exchangeGain >= 0 ? 'text-success' : 'text-danger'">
                  {{ formatMoney(row.exchangeGain) }}
                </span>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="mt-lg">
            <el-button type="primary" @click="processExchangeAdjustment">
              处理汇兑损益
            </el-button>
          </div>
        </div>
        
        <!-- 执行结账 -->
        <div v-if="currentStep === 3" class="step-panel">
          <h3>执行结账</h3>
          <p>确认执行本期结账，结账后本期将不允许再录入凭证</p>
          
          <el-alert
            title="注意：结账后本期账务将被锁定，如需修改请先反结账"
            type="warning"
            show-icon
          />
          
          <div class="mt-lg">
            <el-button type="danger" @click="confirmClosing" :loading="closing">
              <el-icon><Lock /></el-icon>
              确认结账
            </el-button>
          </div>
        </div>
      </div>
      
      <div class="step-actions">
        <el-button v-if="currentStep > 0" @click="prevStep">上一步</el-button>
        <el-button 
          v-if="currentStep < 3 && !hasErrors" 
          type="primary" 
          @click="nextStep"
          :disabled="!canProceed"
        >
          下一步
        </el-button>
      </div>
    </el-card>
    
    <!-- 历史结账记录 -->
    <el-card class="history-card" shadow="never">
      <template #header>
        <span>历史结账记录</span>
      </template>
      
      <!-- 查询条件 -->
      <div class="search-form">
        <el-form :model="searchForm" inline>
          <el-form-item label="结账期间">
            <el-date-picker
              v-model="searchForm.period"
              type="month"
              placeholder="选择月份"
              format="YYYY-MM"
              value-format="YYYY-MM"
              style="width: 150px"
            />
          </el-form-item>
          <el-form-item label="结账人">
            <el-input
              v-model="searchForm.operator"
              placeholder="输入结账人"
              style="width: 120px"
              clearable
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择" style="width: 100px">
              <el-option label="全部" value="" />
              <el-option label="已结账" value="closed" />
              <el-option label="已反结账" value="reversed" />
            </el-select>
          </el-form-item>
          <el-form-item label="日期范围">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="paginatedRecords" border stripe v-loading="searchLoading" style="width: 100%">
        <el-table-column label="结账期间" prop="period" width="120" align="center" />
        <el-table-column label="期间开始" prop="startDate" width="120" align="center" />
        <el-table-column label="期间结束" prop="endDate" width="120" align="center" />
        <el-table-column label="结账日期" prop="closingDate" width="120" align="center" />
        <el-table-column label="结账时间" prop="closingTime" width="100" align="center" />
        <el-table-column label="结账人" prop="closingBy" width="100" align="center" />
        <el-table-column label="凭证数量" prop="voucherCount" width="100" align="center">
          <template #default="{ row }">
            <span class="stat-number">{{ row.voucherCount }} 张</span>
          </template>
        </el-table-column>
        <el-table-column label="结账金额" prop="totalAmount" width="140" align="right">
          <template #default="{ row }">
            <span class="amount-text">{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.remark" placement="top" v-if="row.remark && row.remark.length > 30">
              <span>{{ row.remark.substring(0, 30) }}...</span>
            </el-tooltip>
            <span v-else>{{ row.remark }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="viewDetails(row)">
              查看详情
            </el-button>
            <el-button 
              v-if="row.status === 'closed'" 
              type="warning" 
              text 
              size="small" 
              @click="reverseClosure(row)"
            >
              反结账
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :small="false"
          :disabled="searchLoading"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredRecords.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const checking = ref(false)
const generating = ref(false)
const closing = ref(false)
const searchLoading = ref(false)
const currentStep = ref(0)

// 查询表单
const searchForm = reactive({
  period: '',
  operator: '',
  status: '',
  dateRange: []
})

// 分页参数
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const currentPeriod = ref({
  period: '2025-08',
  status: 'open',
  totalVouchers: 25,
  unpostedVouchers: 2,
  closingTime: '',
  closingBy: ''
})

const closingSteps = ref([
  { title: '结账检查', description: '检查凭证记账状态', status: 'process' },
  { title: '损益结转', description: '生成损益结转凭证', status: 'wait' },
  { title: '期末调汇', description: '处理汇兑损益', status: 'wait' },
  { title: '执行结账', description: '锁定当前期间', status: 'wait' }
])

const checkResults = ref([])
const profitLossData = ref([])
const exchangeData = ref([])
const historyData = ref([
  {
    id: 1,
    period: '2025-08',
    startDate: '2025-08-01',
    endDate: '2025-08-31',
    closingDate: '2025-09-01',
    closingTime: '10:30:00',
    closingBy: '张三',
    voucherCount: 145,
    totalAmount: 2580000,
    status: 'closed',
    remark: '8月份正常结账，所有凭证已审核完毕'
  },
  {
    id: 2,
    period: '2025-07',
    startDate: '2025-07-01',
    endDate: '2025-07-31',
    closingDate: '2025-08-01',
    closingTime: '09:15:00',
    closingBy: '李四',
    voucherCount: 132,
    totalAmount: 2350000,
    status: 'closed',
    remark: '7月份正常结账，包含季度调整分录'
  },
  {
    id: 3,
    period: '2025-06',
    startDate: '2025-06-01',
    endDate: '2025-06-30',
    closingDate: '2025-07-01',
    closingTime: '08:45:00',
    closingBy: '王五',
    voucherCount: 128,
    totalAmount: 2180000,
    status: 'closed',
    remark: '6月份正常结账'
  },
  {
    id: 4,
    period: '2025-05',
    startDate: '2025-05-01',
    endDate: '2025-05-31',
    closingDate: '2025-06-01',
    closingTime: '14:20:00',
    closingBy: '赵六',
    voucherCount: 156,
    totalAmount: 2750000,
    status: 'reversed',
    remark: '5月份因调整事项需要重新结账'
  },
  {
    id: 5,
    period: '2025-04',
    startDate: '2025-04-01',
    endDate: '2025-04-30',
    closingDate: '2025-05-01',
    closingTime: '11:10:00',
    closingBy: '孙七',
    voucherCount: 142,
    totalAmount: 2420000,
    status: 'closed',
    remark: '4月份正常结账，包含固定资产折旧'
  },
  {
    id: 6,
    period: '2025-03',
    startDate: '2025-03-01',
    endDate: '2025-03-31',
    closingDate: '2025-04-01',
    closingTime: '16:30:00',
    closingBy: '周八',
    voucherCount: 138,
    totalAmount: 2290000,
    status: 'closed',
    remark: '3月份一季度结账，财务报表已出具'
  },
  {
    id: 7,
    period: '2025-02',
    startDate: '2025-02-01',
    endDate: '2025-02-28',
    closingDate: '2025-03-01',
    closingTime: '09:45:00',
    closingBy: '吴九',
    voucherCount: 98,
    totalAmount: 1680000,
    status: 'closed',
    remark: '2月份春节假期较短，凭证数量相对较少'
  },
  {
    id: 8,
    period: '2025-01',
    startDate: '2025-01-01',
    endDate: '2025-01-31',
    closingDate: '2025-02-01',
    closingTime: '13:15:00',
    closingBy: '郑十',
    voucherCount: 164,
    totalAmount: 3120000,
    status: 'closed',
    remark: '2025年开年第一个月，包含年初调整分录'
  },
  {
    id: 9,
    period: '2024-12',
    startDate: '2024-12-01',
    endDate: '2024-12-31',
    closingDate: '2025-01-01',
    closingTime: '10:00:00',
    closingBy: '冯一',
    voucherCount: 189,
    totalAmount: 3580000,
    status: 'closed',
    remark: '2024年年末结账，包含年终奖发放和计提'
  },
  {
    id: 10,
    period: '2024-11',
    startDate: '2024-11-01',
    endDate: '2024-11-30',
    closingDate: '2024-12-01',
    closingTime: '15:25:00',
    closingBy: '褚二',
    voucherCount: 147,
    totalAmount: 2460000,
    status: 'closed',
    remark: '11月份正常结账'
  },
  {
    id: 11,
    period: '2024-10',
    startDate: '2024-10-01',
    endDate: '2024-10-31',
    closingDate: '2024-11-01',
    closingTime: '11:40:00',
    closingBy: '卫三',
    voucherCount: 153,
    totalAmount: 2670000,
    status: 'closed',
    remark: '10月份国庆假期后正常结账，包含假期调休工资'
  },
  {
    id: 12,
    period: '2024-09',
    startDate: '2024-09-01',
    endDate: '2024-09-30',
    closingDate: '2024-10-01',
    closingTime: '08:30:00',
    closingBy: '蒋四',
    voucherCount: 141,
    totalAmount: 2380000,
    status: 'closed',
    remark: '9月份三季度结账，财务分析报告已完成'
  }
])

// 筛选后的记录
const filteredRecords = computed(() => {
  let records = historyData.value
  
  if (searchForm.period) {
    records = records.filter(record => record.period === searchForm.period)
  }
  
  if (searchForm.operator) {
    records = records.filter(record => 
      record.closingBy.includes(searchForm.operator)
    )
  }
  
  if (searchForm.status) {
    records = records.filter(record => record.status === searchForm.status)
  }
  
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    const [startDate, endDate] = searchForm.dateRange
    records = records.filter(record => 
      record.closingDate >= startDate && record.closingDate <= endDate
    )
  }
  
  return records
})

// 分页后的记录
const paginatedRecords = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return filteredRecords.value.slice(start, end)
})

const hasErrors = computed(() => {
  return checkResults.value.some(result => result.status === 'fail')
})

const canProceed = computed(() => {
  if (currentStep.value === 0) return checkResults.value.length > 0 && !hasErrors.value
  return true
})

const getStatusTagType = (status) => {
  const typeMap = {
    'open': 'warning',
    'closed': 'success',
    'processing': 'primary'
  }
  return typeMap[status] || ''
}

const getStatusText = (status) => {
  const textMap = {
    'open': '未结账',
    'closed': '已结账',
    'processing': '结账中',
    'reversed': '已反结账'
  }
  return textMap[status] || status
}

// 查询处理
const handleSearch = () => {
  searchLoading.value = true
  pagination.currentPage = 1
  setTimeout(() => {
    ElMessage.success('查询完成')
    searchLoading.value = false
  }, 800)
}

// 重置查询
const resetSearch = () => {
  Object.assign(searchForm, {
    period: '',
    operator: '',
    status: '',
    dateRange: []
  })
  pagination.currentPage = 1
}

// 分页处理
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
}

const formatMoney = (amount) => {
  if (!amount) return '0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const runPreCheck = () => {
  checking.value = true
  setTimeout(() => {
    checkResults.value = [
      { item: '凭证记账检查', status: 'fail', message: '存在2张未记账凭证' },
      { item: '试算平衡检查', status: 'pass', message: '借贷平衡' },
      { item: '期初余额检查', status: 'pass', message: '期初数据完整' },
      { item: '科目余额检查', status: 'pass', message: '科目余额正常' }
    ]
    checking.value = false
    
    if (hasErrors.value) {
      ElMessage.error('检查发现问题，请先处理未记账凭证')
    } else {
      ElMessage.success('检查通过，可以继续结账流程')
      closingSteps.value[0].status = 'finish'
    }
  }, 2000)
}

const generateProfitLossVoucher = () => {
  generating.value = true
  setTimeout(() => {
    profitLossData.value = [
      {
        accountCode: '6001',
        accountName: '主营业务收入',
        currentAmount: 500000,
        transferAmount: 500000
      },
      {
        accountCode: '6602',
        accountName: '管理费用',
        currentAmount: 50000,
        transferAmount: 50000
      }
    ]
    generating.value = false
    ElMessage.success('损益结转凭证生成完成')
    closingSteps.value[1].status = 'finish'
  }, 1500)
}

const processExchangeAdjustment = () => {
  exchangeData.value = [
    {
      accountName: '银行存款-美元户',
      currency: 'USD',
      foreignAmount: 10000,
      bookRate: 7.20,
      endRate: 7.25,
      exchangeGain: 500
    }
  ]
  ElMessage.success('汇兑损益处理完成')
  closingSteps.value[2].status = 'finish'
}

const confirmClosing = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要执行结账吗？结账后本期将不允许录入凭证！',
      '结账确认',
      {
        confirmButtonText: '确定结账',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    closing.value = true
    setTimeout(() => {
      currentPeriod.value.status = 'closed'
      currentPeriod.value.closingTime = new Date().toLocaleString()
      currentPeriod.value.closingBy = '财务主管'
      closingSteps.value[3].status = 'finish'
      closing.value = false
      ElMessage.success('结账完成')
    }, 2000)
  } catch {
    // 取消结账
  }
}

const nextStep = () => {
  if (currentStep.value < 3) {
    currentStep.value++
    closingSteps.value[currentStep.value].status = 'process'
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const reverseClosure = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要对 ${row.period} 期间进行反结账操作吗？反结账后该期间凭证可以重新修改。`,
      '确认反结账',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    row.status = 'reversed'
    row.remark = row.remark + '（已执行反结账操作）'
    ElMessage.success('反结账成功！')
  } catch {
    ElMessage.info('已取消反结账')
  }
}

const viewDetails = (row) => {
  ElMessage.info(`查看期间${row.period}的结账详情`)
}
</script>

<style scoped lang="scss">
.status-card {
  margin-bottom: 20px;
}

.process-card {
  margin-bottom: 20px;
  
  .step-content {
    margin: 40px 0;
    min-height: 300px;
    
    .step-panel {
      h3 {
        margin-bottom: 16px;
        color: #303133;
      }
      
      p {
        margin-bottom: 20px;
        color: #606266;
      }
    }
  }
  
  .step-actions {
    text-align: center;
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
  }
}

.check-results {
  .el-table {
    margin-top: 16px;
  }
}

.text-success {
  color: #67c23a;
}

.text-danger {
  color: #f56c6c;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-card {
  .search-form {
    margin-bottom: 20px;
    padding: 20px;
    background-color: #f5f7fa;
    border-radius: 4px;
  }
  
  .stat-number {
    font-weight: 500;
    color: #409eff;
  }
  
  .amount-text {
    font-weight: 500;
    color: #67c23a;
  }
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>