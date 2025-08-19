<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">资金日报</h2>
      <p class="description">生成资金日报表、资金余额统计和收支分析</p>
    </div>
    
    <!-- 日期选择和操作区 -->
    <el-card class="control-card">
      <el-form :model="reportForm" inline>
        <el-form-item label="报表日期">
          <el-date-picker
            v-model="reportForm.date"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item label="报表类型">
          <el-select v-model="reportForm.type" placeholder="请选择" @change="handleTypeChange">
            <el-option label="日报" value="daily" />
            <el-option label="周报" value="weekly" />
            <el-option label="月报" value="monthly" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Refresh" @click="generateReport">生成报表</el-button>
          <el-button type="success" icon="Download" @click="exportReport">导出Excel</el-button>
          <el-button icon="Printer" @click="printReport">打印</el-button>
          <el-button icon="Message" @click="sendReport">发送邮件</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 资金概览 -->
    <el-row :gutter="20" class="fund-overview">
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="资金总额" :value="fundSummary.total" :precision="2" prefix="¥">
            <template #suffix>
              <el-tag type="success" size="small">充足</el-tag>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="现金余额" :value="fundSummary.cash" :precision="2" prefix="¥">
            <template #suffix>
              <span class="percentage">{{ fundSummary.cashPercent }}%</span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="银行存款" :value="fundSummary.bank" :precision="2" prefix="¥">
            <template #suffix>
              <span class="percentage">{{ fundSummary.bankPercent }}%</span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="其他货币资金" :value="fundSummary.other" :precision="2" prefix="¥">
            <template #suffix>
              <span class="percentage">{{ fundSummary.otherPercent }}%</span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 资金日报表 -->
    <el-card class="report-card">
      <template #header>
        <div class="card-header">
          <span>资金日报表 - {{ reportForm.date }}</span>
          <el-tag type="info">{{ reportStatus }}</el-tag>
        </div>
      </template>
      
      <!-- 账户余额明细 -->
      <h3>一、账户余额明细</h3>
      <el-table :data="accountBalances" border show-summary :summary-method="getAccountSummary" style="width: 100%">
        <el-table-column prop="accountType" label="账户类型" width="120" />
        <el-table-column prop="accountName" label="账户名称" width="180" />
        <el-table-column prop="accountNo" label="账号" width="160" />
        <el-table-column prop="beginBalance" label="期初余额" width="140" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.beginBalance) }}
          </template>
        </el-table-column>
        <el-table-column prop="todayIn" label="本日收入" width="140" align="right">
          <template #default="{ row }">
            <span class="amount-in">{{ formatMoney(row.todayIn) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="todayOut" label="本日支出" width="140" align="right">
          <template #default="{ row }">
            <span class="amount-out">{{ formatMoney(row.todayOut) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="endBalance" label="期末余额" width="140" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.endBalance) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'normal' ? 'success' : 'warning'" size="small">
              {{ row.status === 'normal' ? '正常' : '关注' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 收支明细 -->
      <h3 class="mt-lg">二、收支明细</h3>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="收入明细" name="income">
          <el-table :data="incomeDetails" border>
            <el-table-column prop="time" label="时间" width="100" />
            <el-table-column prop="voucherNo" label="凭证号" width="150" />
            <el-table-column prop="account" label="收款账户" width="150" />
            <el-table-column prop="source" label="付款方" width="150" />
            <el-table-column prop="summary" label="摘要" min-width="200" />
            <el-table-column prop="amount" label="金额" width="150" align="right">
              <template #default="{ row }">
                <span class="amount-in">{{ formatMoney(row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="类别" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.category }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div class="summary-row">
            <span>收入合计：</span>
            <span class="amount-in">{{ formatMoney(incomeSummary) }}</span>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="支出明细" name="expense">
          <el-table :data="expenseDetails" border>
            <el-table-column prop="time" label="时间" width="100" />
            <el-table-column prop="voucherNo" label="凭证号" width="150" />
            <el-table-column prop="account" label="付款账户" width="150" />
            <el-table-column prop="payee" label="收款方" width="150" />
            <el-table-column prop="summary" label="摘要" min-width="200" />
            <el-table-column prop="amount" label="金额" width="150" align="right">
              <template #default="{ row }">
                <span class="amount-out">{{ formatMoney(row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="类别" width="100">
              <template #default="{ row }">
                <el-tag type="warning" size="small">{{ row.category }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div class="summary-row">
            <span>支出合计：</span>
            <span class="amount-out">{{ formatMoney(expenseSummary) }}</span>
          </div>
        </el-tab-pane>
      </el-tabs>
      
      <!-- 资金流向分析 -->
      <h3 class="mt-lg">三、资金流向分析</h3>
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="chart-container">
            <div ref="flowChart" style="height: 300px"></div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="chart-container">
            <div ref="categoryChart" style="height: 300px"></div>
          </div>
        </el-col>
      </el-row>
      
      <!-- 资金预测 -->
      <h3 class="mt-lg">四、资金预测</h3>
      <el-table :data="fundForecast" border>
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="expectedIn" label="预计收入" width="150" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.expectedIn) }}
          </template>
        </el-table-column>
        <el-table-column prop="expectedOut" label="预计支出" width="150" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.expectedOut) }}
          </template>
        </el-table-column>
        <el-table-column prop="netFlow" label="净流量" width="150" align="right">
          <template #default="{ row }">
            <span :class="row.netFlow >= 0 ? 'amount-in' : 'amount-out'">
              {{ formatMoney(row.netFlow) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="expectedBalance" label="预计余额" width="150" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.expectedBalance) }}
          </template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险等级" width="120">
          <template #default="{ row }">
            <el-tag :type="getRiskType(row.riskLevel)" size="small">
              {{ row.riskLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="suggestion" label="资金建议" min-width="200" />
      </el-table>
      
      <!-- 备注说明 -->
      <div class="report-footer">
        <el-divider />
        <el-row>
          <el-col :span="8">
            <p><strong>制表人：</strong>{{ reportInfo.creator }}</p>
          </el-col>
          <el-col :span="8">
            <p><strong>审核人：</strong>{{ reportInfo.reviewer }}</p>
          </el-col>
          <el-col :span="8">
            <p><strong>生成时间：</strong>{{ reportInfo.createTime }}</p>
          </el-col>
        </el-row>
      </div>
    </el-card>
    
    <!-- 历史报表对话框 -->
    <el-dialog v-model="historyDialog" title="历史报表查询" width="800px">
      <el-table :data="historyReports" border>
        <el-table-column prop="date" label="报表日期" width="120" />
        <el-table-column prop="type" label="报表类型" width="100" />
        <el-table-column prop="totalBalance" label="资金总额" width="150" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.totalBalance) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalIn" label="总收入" width="150" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.totalIn) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalOut" label="总支出" width="150" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.totalOut) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewHistoryReport(row)">查看</el-button>
            <el-button link type="info" @click="downloadHistoryReport(row)">下载</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const activeTab = ref('income')
const historyDialog = ref(false)
const flowChart = ref(null)
const categoryChart = ref(null)
let flowChartInstance = null
let categoryChartInstance = null

// 报表表单
const reportForm = ref({
  date: new Date().toISOString().split('T')[0],
  type: 'daily'
})

// 报表信息
const reportInfo = ref({
  creator: '财务部',
  reviewer: '财务经理',
  createTime: new Date().toLocaleString()
})

// 资金汇总
const fundSummary = ref({
  total: 1326000,
  cash: 58600,
  bank: 1176000,
  other: 91400,
  cashPercent: 4.4,
  bankPercent: 88.7,
  otherPercent: 6.9
})

// 账户余额
const accountBalances = ref([
  {
    accountType: '现金',
    accountName: '库存现金',
    accountNo: '-',
    beginBalance: 50000,
    todayIn: 23000,
    todayOut: 14400,
    endBalance: 58600,
    status: 'normal'
  },
  {
    accountType: '银行存款',
    accountName: '中国银行',
    accountNo: '1234567890123456',
    beginBalance: 800000,
    todayIn: 150000,
    todayOut: 94000,
    endBalance: 856000,
    status: 'normal'
  },
  {
    accountType: '银行存款',
    accountName: '工商银行',
    accountNo: '9876543210987654',
    beginBalance: 300000,
    todayIn: 50000,
    todayOut: 30000,
    endBalance: 320000,
    status: 'normal'
  },
  {
    accountType: '其他货币资金',
    accountName: '支付宝账户',
    accountNo: 'pay@company.com',
    beginBalance: 80000,
    todayIn: 15000,
    todayOut: 3600,
    endBalance: 91400,
    status: 'normal'
  }
])

// 收入明细
const incomeDetails = ref([
  {
    time: '09:15',
    voucherNo: '现收-001',
    account: '中国银行',
    source: 'ABC公司',
    summary: '收到货款',
    amount: 150000,
    category: '销售收入'
  },
  {
    time: '10:30',
    voucherNo: '现收-002',
    account: '工商银行',
    source: 'XYZ公司',
    summary: '收到预付款',
    amount: 50000,
    category: '预收款'
  },
  {
    time: '14:20',
    voucherNo: '现收-003',
    account: '库存现金',
    source: '零售客户',
    summary: '现金销售',
    amount: 23000,
    category: '销售收入'
  },
  {
    time: '16:45',
    voucherNo: '现收-004',
    account: '支付宝',
    source: '线上客户',
    summary: '线上销售',
    amount: 15000,
    category: '销售收入'
  }
])

// 支出明细
const expenseDetails = ref([
  {
    time: '08:30',
    voucherNo: '现付-001',
    account: '中国银行',
    payee: '供应商A',
    summary: '支付材料款',
    amount: 80000,
    category: '采购支出'
  },
  {
    time: '11:00',
    voucherNo: '现付-002',
    account: '库存现金',
    payee: '员工报销',
    summary: '差旅费报销',
    amount: 8400,
    category: '管理费用'
  },
  {
    time: '13:45',
    voucherNo: '现付-003',
    account: '工商银行',
    payee: '税务局',
    summary: '缴纳税款',
    amount: 30000,
    category: '税费支出'
  },
  {
    time: '15:30',
    voucherNo: '现付-004',
    account: '中国银行',
    payee: '物业公司',
    summary: '物业费',
    amount: 14000,
    category: '管理费用'
  },
  {
    time: '17:00',
    voucherNo: '现付-005',
    account: '库存现金',
    payee: '办公用品店',
    summary: '办公用品',
    amount: 6000,
    category: '管理费用'
  }
])

// 资金预测
const fundForecast = ref([
  {
    date: '2025-08-16',
    expectedIn: 200000,
    expectedOut: 150000,
    netFlow: 50000,
    expectedBalance: 1376000,
    riskLevel: '低',
    suggestion: '资金充足，可正常运营'
  },
  {
    date: '2025-08-17',
    expectedIn: 100000,
    expectedOut: 180000,
    netFlow: -80000,
    expectedBalance: 1296000,
    riskLevel: '低',
    suggestion: '资金充足，注意大额支出'
  },
  {
    date: '2025-08-18',
    expectedIn: 50000,
    expectedOut: 200000,
    netFlow: -150000,
    expectedBalance: 1146000,
    riskLevel: '中',
    suggestion: '建议加快应收账款回收'
  },
  {
    date: '2025-08-19',
    expectedIn: 300000,
    expectedOut: 100000,
    netFlow: 200000,
    expectedBalance: 1346000,
    riskLevel: '低',
    suggestion: '资金回流良好'
  },
  {
    date: '2025-08-20',
    expectedIn: 150000,
    expectedOut: 250000,
    netFlow: -100000,
    expectedBalance: 1246000,
    riskLevel: '中',
    suggestion: '关注月末集中付款'
  }
])

// 历史报表
const historyReports = ref([
  {
    date: '2025-08-14',
    type: '日报',
    totalBalance: 1250000,
    totalIn: 180000,
    totalOut: 120000
  },
  {
    date: '2025-08-13',
    type: '日报',
    totalBalance: 1190000,
    totalIn: 220000,
    totalOut: 150000
  }
])

const reportStatus = computed(() => {
  return '已审核'
})

const incomeSummary = computed(() => {
  return incomeDetails.value.reduce((sum, item) => sum + item.amount, 0)
})

const expenseSummary = computed(() => {
  return expenseDetails.value.reduce((sum, item) => sum + item.amount, 0)
})

const formatMoney = (amount) => {
  if (!amount) return '¥0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const getAccountSummary = (param) => {
  const { columns, data } = param
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (['beginBalance', 'todayIn', 'todayOut', 'endBalance'].includes(column.property)) {
      const values = data.map(item => Number(item[column.property]))
      sums[index] = formatMoney(values.reduce((prev, curr) => prev + curr, 0))
    } else {
      sums[index] = ''
    }
  })
  return sums
}

const getRiskType = (level) => {
  const map = {
    '低': 'success',
    '中': 'warning',
    '高': 'danger'
  }
  return map[level] || ''
}

const initCharts = () => {
  // 资金流向图
  if (flowChart.value) {
    flowChartInstance = echarts.init(flowChart.value)
    const flowOption = {
      title: {
        text: '资金流向趋势',
        textStyle: { fontSize: 14 }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'cross' }
      },
      legend: {
        data: ['收入', '支出', '净流量']
      },
      xAxis: {
        type: 'category',
        data: ['08:00', '10:00', '12:00', '14:00', '16:00', '18:00']
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (value) => value / 10000 + '万'
        }
      },
      series: [
        {
          name: '收入',
          type: 'line',
          data: [0, 50000, 200000, 200000, 223000, 238000],
          itemStyle: { color: '#67c23a' }
        },
        {
          name: '支出',
          type: 'line',
          data: [0, 80000, 88400, 118400, 132400, 138400],
          itemStyle: { color: '#f56c6c' }
        },
        {
          name: '净流量',
          type: 'bar',
          data: [0, -30000, 111600, 81600, 90600, 99600],
          itemStyle: {
            color: (params) => params.value >= 0 ? '#67c23a' : '#f56c6c'
          }
        }
      ]
    }
    flowChartInstance.setOption(flowOption)
  }
  
  // 分类占比图
  if (categoryChart.value) {
    categoryChartInstance = echarts.init(categoryChart.value)
    const categoryOption = {
      title: {
        text: '收支分类占比',
        textStyle: { fontSize: 14 }
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: 10,
        top: 30
      },
      series: [
        {
          name: '收入分类',
          type: 'pie',
          radius: ['30%', '50%'],
          center: ['30%', '50%'],
          data: [
            { value: 188000, name: '销售收入' },
            { value: 50000, name: '预收款' }
          ],
          label: {
            formatter: '{b}\n{d}%'
          }
        },
        {
          name: '支出分类',
          type: 'pie',
          radius: ['30%', '50%'],
          center: ['70%', '50%'],
          data: [
            { value: 80000, name: '采购支出' },
            { value: 28400, name: '管理费用' },
            { value: 30000, name: '税费支出' }
          ],
          label: {
            formatter: '{b}\n{d}%'
          }
        }
      ]
    }
    categoryChartInstance.setOption(categoryOption)
  }
}

const handleDateChange = () => {
  generateReport()
}

const handleTypeChange = () => {
  generateReport()
}

const generateReport = () => {
  ElMessage.success('报表生成成功')
  nextTick(() => {
    initCharts()
  })
}

const exportReport = () => {
  ElMessage.success('导出功能开发中')
}

const printReport = () => {
  ElMessage.success('打印功能开发中')
}

const sendReport = () => {
  ElMessage.success('邮件发送功能开发中')
}

const viewHistoryReport = (row) => {
  ElMessage.info(`查看报表：${row.date}`)
}

const downloadHistoryReport = (row) => {
  ElMessage.success(`下载报表：${row.date}`)
}

onMounted(() => {
  nextTick(() => {
    initCharts()
  })
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    flowChartInstance?.resize()
    categoryChartInstance?.resize()
  })
})
</script>

<style scoped lang="scss">
.control-card {
  margin-bottom: 20px;
}

.fund-overview {
  margin-bottom: 20px;
  
  .stat-card {
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
    
    .percentage {
      font-size: 12px;
      color: #909399;
    }
  }
}

.report-card {
  h3 {
    margin: 24px 0 16px 0;
    color: #303133;
    font-size: 16px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.amount-in {
  color: #67c23a;
  font-weight: 600;
}

.amount-out {
  color: #f56c6c;
  font-weight: 600;
}

.summary-row {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  text-align: right;
  font-size: 16px;
  
  span:first-child {
    margin-right: 8px;
  }
}

.chart-container {
  background: #fff;
  border-radius: 4px;
  padding: 10px;
}

.report-footer {
  margin-top: 30px;
  
  p {
    margin: 8px 0;
    color: #606266;
  }
}

.mt-lg {
  margin-top: 24px;
}

@media (max-width: 768px) {
  .fund-overview {
    .el-col {
      margin-bottom: 16px;
    }
  }
  
  .chart-container {
    margin-bottom: 20px;
  }
}
</style>