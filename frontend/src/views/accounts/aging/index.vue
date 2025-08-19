<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">账龄分析</h2>
      <p class="description">分析应收应付账款的账龄分布和逾期情况</p>
    </div>
    
    <!-- 分析条件 -->
    <el-card class="filter-card">
      <el-form :model="analysisForm" inline>
        <el-form-item label="分析类型">
          <el-radio-group v-model="analysisForm.type" @change="handleTypeChange">
            <el-radio-button label="receivable">应收账款</el-radio-button>
            <el-radio-button label="payable">应付账款</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分析日期">
          <el-date-picker
            v-model="analysisForm.analysisDate"
            type="date"
            placeholder="选择分析日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item label="账龄区间">
          <el-select v-model="analysisForm.agingType" placeholder="请选择" @change="handleAgingTypeChange">
            <el-option label="标准区间" value="standard" />
            <el-option label="自定义区间" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Refresh" @click="generateAnalysis">生成分析</el-button>
          <el-button type="success" icon="Download" @click="exportAnalysis">导出报表</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 自定义区间设置 -->
      <div v-if="analysisForm.agingType === 'custom'" class="custom-aging">
        <el-divider />
        <h4>自定义账龄区间</h4>
        <el-row :gutter="10">
          <el-col :span="4" v-for="(period, index) in customPeriods" :key="index">
            <el-input-group>
              <el-input v-model="period.start" placeholder="起始天数" size="small" />
              <el-input v-model="period.end" placeholder="结束天数" size="small" />
            </el-input-group>
            <p class="period-label">{{ period.label }}</p>
          </el-col>
        </el-row>
      </div>
    </el-card>
    
    <!-- 账龄概览 -->
    <el-row :gutter="20" class="aging-overview">
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="总金额" :value="agingSummary.total" :precision="2" prefix="¥">
            <template #suffix>
              <el-tag size="small">{{ agingSummary.totalCount }}笔</el-tag>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="未逾期" :value="agingSummary.current" :precision="2" prefix="¥">
            <template #suffix>
              <span class="percentage">{{ agingSummary.currentPercent }}%</span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="逾期金额" :value="agingSummary.overdue" :precision="2" prefix="¥">
            <template #suffix>
              <span class="overdue-percentage">{{ agingSummary.overduePercent }}%</span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="平均账龄" :value="agingSummary.avgDays">
            <template #suffix>
              <span>天</span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 账龄分析表 -->
    <el-card class="aging-table-card">
      <template #header>
        <div class="card-header">
          <span>账龄分析</span>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="showAgingChart">图表分析</el-button>
            <el-button size="small" @click="showDetailList">明细列表</el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="agingTableData" border show-summary :summary-method="getAgingSummary" style="width: 100%" stripe>
        <el-table-column prop="customerName" :label="analysisForm.type === 'receivable' ? '客户名称' : '供应商名称'" min-width="150" show-overflow-tooltip />
        <el-table-column prop="current" label="未逾期" width="120" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.current) }}
          </template>
        </el-table-column>
        <el-table-column prop="days1to30" label="1-30天" width="120" align="right">
          <template #default="{ row }">
            <span class="aging-amount aging-1">{{ formatMoney(row.days1to30) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="days31to60" label="31-60天" width="120" align="right">
          <template #default="{ row }">
            <span class="aging-amount aging-2">{{ formatMoney(row.days31to60) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="days61to90" label="61-90天" width="120" align="right">
          <template #default="{ row }">
            <span class="aging-amount aging-3">{{ formatMoney(row.days61to90) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="days91to120" label="91-120天" width="120" align="right">
          <template #default="{ row }">
            <span class="aging-amount aging-4">{{ formatMoney(row.days91to120) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="over120" label="120天以上" width="120" align="right">
          <template #default="{ row }">
            <span class="aging-amount aging-5">{{ formatMoney(row.over120) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="total" label="合计" width="120" align="right">
          <template #default="{ row }">
            <span class="total-amount">{{ formatMoney(row.total) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="avgDays" label="平均账龄" width="100" align="right">
          <template #default="{ row }">
            {{ row.avgDays }}天
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" size="small" @click="handleViewDetail(row)">查看明细</el-button>
              <el-button link type="success" size="small" @click="handleRemind(row)">催款提醒</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 账龄图表分析 -->
    <el-card class="chart-card" v-show="showCharts">
      <template #header>
        <span>账龄可视化分析</span>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="chart-container">
            <h4>账龄分布饼图</h4>
            <div ref="agingPieChart" style="height: 300px"></div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="chart-container">
            <h4>客户账龄排行</h4>
            <div ref="customerRankChart" style="height: 300px"></div>
          </div>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" class="mt-lg">
        <el-col :span="24">
          <div class="chart-container">
            <h4>账龄趋势分析</h4>
            <div ref="agingTrendChart" style="height: 350px"></div>
          </div>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 逾期预警 -->
    <el-card class="warning-card">
      <template #header>
        <span>逾期预警</span>
      </template>
      
      <el-table :data="overdueWarnings" border style="width: 100%" stripe>
        <el-table-column prop="customerName" :label="analysisForm.type === 'receivable' ? '客户名称' : '供应商名称'" min-width="120" show-overflow-tooltip />
        <el-table-column prop="invoiceNo" label="单据号" width="120" show-overflow-tooltip />
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="到期日" width="110" />
        <el-table-column prop="overdueDays" label="逾期天数" width="100">
          <template #default="{ row }">
            <span class="overdue-days">{{ row.overdueDays }}天</span>
          </template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getRiskType(row.riskLevel)" size="small">
              {{ row.riskLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contact" label="联系人" width="100" show-overflow-tooltip />
        <el-table-column prop="phone" label="联系电话" width="120" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" size="small" @click="handleCall(row)">电话催收</el-button>
              <el-button link type="warning" size="small" @click="handleEmail(row)">邮件提醒</el-button>
              <el-button link type="danger" size="small" @click="handleLegal(row)">法务处理</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 明细对话框 -->
    <el-dialog v-model="detailDialog" :title="`${currentCustomer}账龄明细`" width="1000px">
      <el-table :data="customerDetail" border style="width: 100%" stripe>
        <el-table-column prop="invoiceNo" label="单据号" width="120" show-overflow-tooltip />
        <el-table-column prop="invoiceDate" label="单据日期" width="110" />
        <el-table-column prop="dueDate" label="到期日" width="110" />
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="remainingAmount" label="余额" width="120" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.remainingAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="agingDays" label="账龄天数" width="100" align="right">
          <template #default="{ row }">
            {{ row.agingDays }}天
          </template>
        </el-table-column>
        <el-table-column prop="agingRange" label="账龄区间" width="100">
          <template #default="{ row }">
            <el-tag :type="getAgingRangeType(row.agingRange)" size="small">
              {{ row.agingRange }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'normal' ? 'success' : 'danger'" size="small">
              {{ row.status === 'normal' ? '正常' : '逾期' }}
            </el-tag>
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

const showCharts = ref(false)
const detailDialog = ref(false)
const currentCustomer = ref('')
const agingPieChart = ref(null)
const customerRankChart = ref(null)
const agingTrendChart = ref(null)
let agingPieChartInstance = null
let customerRankChartInstance = null
let agingTrendChartInstance = null

// 分析表单
const analysisForm = ref({
  type: 'receivable',
  analysisDate: new Date().toISOString().split('T')[0],
  agingType: 'standard'
})

// 自定义账龄区间
const customPeriods = ref([
  { start: '0', end: '30', label: '0-30天' },
  { start: '31', end: '60', label: '31-60天' },
  { start: '61', end: '90', label: '61-90天' },
  { start: '91', end: '120', label: '91-120天' },
  { start: '121', end: '365', label: '120天以上' }
])

// 账龄汇总
const agingSummary = ref({
  total: 2580000,
  totalCount: 156,
  current: 1850000,
  currentPercent: 71.7,
  overdue: 730000,
  overduePercent: 28.3,
  avgDays: 45
})

// 账龄分析表数据
const agingTableData = ref([
  {
    id: 1,
    customerName: 'ABC公司',
    current: 450000,
    days1to30: 120000,
    days31to60: 80000,
    days61to90: 50000,
    days91to120: 30000,
    over120: 20000,
    total: 750000,
    avgDays: 42
  },
  {
    id: 2,
    customerName: 'XYZ集团',
    current: 380000,
    days1to30: 150000,
    days31to60: 60000,
    days61to90: 30000,
    days91to120: 15000,
    over120: 5000,
    total: 640000,
    avgDays: 38
  },
  {
    id: 3,
    customerName: '华通科技',
    current: 280000,
    days1to30: 90000,
    days31to60: 70000,
    days61to90: 40000,
    days91to120: 25000,
    over120: 15000,
    total: 520000,
    avgDays: 48
  },
  {
    id: 4,
    customerName: '恒大商贸',
    current: 200000,
    days1to30: 80000,
    days31to60: 50000,
    days61to90: 30000,
    days91to120: 20000,
    over120: 30000,
    total: 410000,
    avgDays: 52
  },
  {
    id: 5,
    customerName: '鸿达实业',
    current: 150000,
    days1to30: 60000,
    days31to60: 40000,
    days61to90: 25000,
    days91to120: 15000,
    over120: 10000,
    total: 300000,
    avgDays: 40
  }
])

// 逾期预警数据
const overdueWarnings = ref([
  {
    customerName: 'ABC公司',
    invoiceNo: 'INV20250003',
    amount: 123000,
    dueDate: '2025-07-31',
    overdueDays: 15,
    riskLevel: '中等',
    contact: '张经理',
    phone: '138****1234'
  },
  {
    customerName: '华通科技',
    invoiceNo: 'INV20250008',
    amount: 89000,
    dueDate: '2025-07-25',
    overdueDays: 21,
    riskLevel: '高',
    contact: '李总监',
    phone: '139****5678'
  },
  {
    customerName: '恒大商贸',
    invoiceNo: 'INV20250012',
    amount: 156000,
    dueDate: '2025-07-20',
    overdueDays: 26,
    riskLevel: '高',
    contact: '王部长',
    phone: '137****9999'
  }
])

// 客户明细数据
const customerDetail = ref([])

const formatMoney = (amount) => {
  if (!amount) return '¥0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const getAgingSummary = (param) => {
  const { columns, data } = param
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (['current', 'days1to30', 'days31to60', 'days61to90', 'days91to120', 'over120', 'total'].includes(column.property)) {
      const values = data.map(item => Number(item[column.property]))
      sums[index] = formatMoney(values.reduce((prev, curr) => prev + curr, 0))
    } else if (column.property === 'avgDays') {
      const totalAmount = data.reduce((sum, item) => sum + item.total, 0)
      const weightedDays = data.reduce((sum, item) => sum + (item.avgDays * item.total), 0)
      sums[index] = totalAmount > 0 ? Math.round(weightedDays / totalAmount) + '天' : '0天'
    } else {
      sums[index] = ''
    }
  })
  return sums
}

const getRiskType = (level) => {
  const map = {
    '低': 'success',
    '中等': 'warning',
    '高': 'danger'
  }
  return map[level] || ''
}

const getAgingRangeType = (range) => {
  if (range.includes('未逾期') || range.includes('0-30')) return 'success'
  if (range.includes('31-60')) return 'warning'
  if (range.includes('61-90')) return 'danger'
  return 'info'
}

const initCharts = () => {
  // 账龄分布饼图
  if (agingPieChart.value) {
    agingPieChartInstance = echarts.init(agingPieChart.value)
    const pieOption = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      series: [
        {
          name: '账龄分布',
          type: 'pie',
          radius: ['30%', '70%'],
          data: [
            { value: 1850000, name: '未逾期', itemStyle: { color: '#67c23a' } },
            { value: 310000, name: '1-30天', itemStyle: { color: '#e6a23c' } },
            { value: 260000, name: '31-60天', itemStyle: { color: '#f56c6c' } },
            { value: 105000, name: '61-90天', itemStyle: { color: '#909399' } },
            { value: 55000, name: '120天以上', itemStyle: { color: '#000000' } }
          ],
          label: {
            formatter: '{b}\n{d}%'
          }
        }
      ]
    }
    agingPieChartInstance.setOption(pieOption)
  }
  
  // 客户账龄排行
  if (customerRankChart.value) {
    customerRankChartInstance = echarts.init(customerRankChart.value)
    const rankOption = {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      xAxis: {
        type: 'value',
        axisLabel: {
          formatter: (value) => value / 10000 + '万'
        }
      },
      yAxis: {
        type: 'category',
        data: ['鸿达实业', '恒大商贸', '华通科技', 'XYZ集团', 'ABC公司']
      },
      series: [
        {
          type: 'bar',
          data: [30, 41, 52, 64, 75],
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' }
            ])
          }
        }
      ]
    }
    customerRankChartInstance.setOption(rankOption)
  }
  
  // 账龄趋势图
  if (agingTrendChart.value) {
    agingTrendChartInstance = echarts.init(agingTrendChart.value)
    const trendOption = {
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['未逾期', '1-30天', '31-60天', '61-90天', '90天以上']
      },
      xAxis: {
        type: 'category',
        data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月']
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (value) => value / 10000 + '万'
        }
      },
      series: [
        {
          name: '未逾期',
          type: 'line',
          stack: 'Total',
          data: [180, 185, 175, 190, 180, 175, 170, 185],
          itemStyle: { color: '#67c23a' }
        },
        {
          name: '1-30天',
          type: 'line',
          stack: 'Total',
          data: [25, 30, 28, 35, 30, 28, 25, 31],
          itemStyle: { color: '#e6a23c' }
        },
        {
          name: '31-60天',
          type: 'line',
          stack: 'Total',
          data: [15, 18, 20, 22, 25, 23, 20, 26],
          itemStyle: { color: '#f56c6c' }
        },
        {
          name: '61-90天',
          type: 'line',
          stack: 'Total',
          data: [8, 10, 12, 15, 18, 15, 12, 10],
          itemStyle: { color: '#909399' }
        },
        {
          name: '90天以上',
          type: 'line',
          stack: 'Total',
          data: [3, 5, 8, 10, 12, 15, 18, 5],
          itemStyle: { color: '#000000' }
        }
      ]
    }
    agingTrendChartInstance.setOption(trendOption)
  }
}

const handleTypeChange = () => {
  generateAnalysis()
}

const handleDateChange = () => {
  generateAnalysis()
}

const handleAgingTypeChange = () => {
  generateAnalysis()
}

const generateAnalysis = () => {
  ElMessage.success('账龄分析生成成功')
}

const exportAnalysis = () => {
  ElMessage.success('导出功能开发中')
}

const showAgingChart = () => {
  showCharts.value = true
  nextTick(() => {
    initCharts()
  })
}

const showDetailList = () => {
  showCharts.value = false
}

const handleViewDetail = (row) => {
  currentCustomer.value = row.customerName
  customerDetail.value = [
    {
      invoiceNo: 'INV20250001',
      invoiceDate: '2025-08-01',
      dueDate: '2025-08-31',
      amount: 234000,
      remainingAmount: 84000,
      agingDays: 15,
      agingRange: '1-30天',
      status: 'normal'
    },
    {
      invoiceNo: 'INV20250003',
      invoiceDate: '2025-07-01',
      dueDate: '2025-07-31',
      amount: 123000,
      remainingAmount: 123000,
      agingDays: 45,
      agingRange: '31-60天',
      status: 'overdue'
    }
  ]
  detailDialog.value = true
}

const handleRemind = (row) => {
  ElMessage.success(`已发送催款提醒给${row.customerName}`)
}

const handleCall = (row) => {
  ElMessage.info(`拨打电话：${row.phone}`)
}

const handleEmail = (row) => {
  ElMessage.success(`已发送邮件提醒给${row.customerName}`)
}

const handleLegal = (row) => {
  ElMessage.warning(`已转法务处理：${row.customerName}`)
}

onMounted(() => {
  window.addEventListener('resize', () => {
    agingPieChartInstance?.resize()
    customerRankChartInstance?.resize()
    agingTrendChartInstance?.resize()
  })
})
</script>

<style scoped lang="scss">
.filter-card {
  margin-bottom: 20px;
  
  .custom-aging {
    .period-label {
      text-align: center;
      margin: 5px 0;
      font-size: 12px;
      color: #606266;
    }
  }
}

.aging-overview {
  margin-bottom: 20px;
  
  .stat-card {
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
    
    .percentage {
      color: #67c23a;
      font-size: 12px;
    }
    
    .overdue-percentage {
      color: #f56c6c;
      font-size: 12px;
    }
  }
}

.aging-table-card {
  margin-bottom: 20px;
  
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

.chart-card {
  margin-bottom: 20px;
  
  .chart-container {
    h4 {
      text-align: center;
      margin-bottom: 15px;
      color: #303133;
    }
  }
}

.warning-card {
  .overdue-days {
    color: #f56c6c;
    font-weight: 600;
  }
}

.aging-amount {
  font-weight: 600;
  
  &.aging-1 { color: #e6a23c; }
  &.aging-2 { color: #f56c6c; }
  &.aging-3 { color: #c45656; }
  &.aging-4 { color: #909399; }
  &.aging-5 { color: #000000; }
}

.total-amount {
  font-weight: 700;
  color: #303133;
}

.mt-lg {
  margin-top: 20px;
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
  .aging-overview {
    .el-col {
      margin-bottom: 16px;
    }
  }
  
  .chart-card {
    .el-col {
      margin-bottom: 20px;
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