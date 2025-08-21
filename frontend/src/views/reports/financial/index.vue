<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">财务报表</h2>
      <p class="description">生成和查看三大财务报表：资产负债表、利润表、现金流量表</p>
    </div>
    
    <!-- 报表生成控制面板 -->
    <el-card class="control-panel">
      <el-form :model="reportForm" inline>
        <el-form-item label="报表类型">
          <el-select v-model="reportForm.reportType" placeholder="请选择报表类型" @change="handleReportTypeChange" style="width: 200px">
            <el-option label="资产负债表" value="balance_sheet" />
            <el-option label="利润表" value="income_statement" />
            <el-option label="现金流量表" value="cash_flow" />
            <el-option label="所有者权益（股东权益）变动表" value="equity_statement" />
          </el-select>
        </el-form-item>
        <el-form-item label="报告期间">
          <el-select v-model="reportForm.period" placeholder="请选择期间" style="width: 140px">
            <el-option label="本月" value="current_month" />
            <el-option label="本季度" value="current_quarter" />
            <el-option label="本年度" value="current_year" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="结束日期" v-if="reportForm.period === 'custom'">
          <el-date-picker
            v-model="reportForm.endDate"
            type="date"
            placeholder="选择结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Refresh" @click="generateReport" :loading="generating">生成报表</el-button>
          <el-button type="success" icon="Download" @click="exportReport" :disabled="!reportData">导出报表</el-button>
          <el-button type="info" icon="Printer" @click="printReport" :disabled="!reportData">打印报表</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 报表显示区域 -->
    <el-card v-if="reportData" class="report-display">
      <template #header>
        <div class="report-header">
          <div class="report-title">
            <h3>{{ getReportTitle() }}</h3>
            <p class="report-period">报告期间：{{ getReportPeriod() }}</p>
            <p class="generate-time">生成时间：{{ reportData.generateTime }}</p>
          </div>
          <div class="report-actions">
            <el-button type="primary" size="small" @click="showComparison">同期对比</el-button>
            <el-button type="success" size="small" @click="showTrend">趋势分析</el-button>
          </div>
        </div>
      </template>
      
      <!-- 资产负债表 -->
      <div v-if="reportForm.reportType === 'balance_sheet'" class="balance-sheet">
        <el-row :gutter="20">
          <el-col :span="12">
            <h4 class="section-title">资产</h4>
            <el-table :data="reportData.assets" border show-summary :summary-method="getAssetsSummary" style="width: 100%">
              <el-table-column prop="itemName" label="项目" min-width="150" />
              <el-table-column prop="currentAmount" label="期末余额" width="120" align="right">
                <template #default="{ row }">
                  {{ formatMoney(row.currentAmount) }}
                </template>
              </el-table-column>
              <el-table-column prop="previousAmount" label="期初余额" width="120" align="right">
                <template #default="{ row }">
                  {{ formatMoney(row.previousAmount) }}
                </template>
              </el-table-column>
            </el-table>
          </el-col>
          
          <el-col :span="12">
            <h4 class="section-title">负债和所有者权益</h4>
            <el-table :data="reportData.liabilities" border show-summary :summary-method="getLiabilitiesSummary" style="width: 100%">
              <el-table-column prop="itemName" label="项目" min-width="150" />
              <el-table-column prop="currentAmount" label="期末余额" width="120" align="right">
                <template #default="{ row }">
                  {{ formatMoney(row.currentAmount) }}
                </template>
              </el-table-column>
              <el-table-column prop="previousAmount" label="期初余额" width="120" align="right">
                <template #default="{ row }">
                  {{ formatMoney(row.previousAmount) }}
                </template>
              </el-table-column>
            </el-table>
          </el-col>
        </el-row>
      </div>
      
      <!-- 利润表 -->
      <div v-if="reportForm.reportType === 'income_statement'" class="income-statement">
        <el-table :data="reportData.incomeItems" border show-summary :summary-method="getIncomeSummary" style="width: 100%">
          <el-table-column prop="itemName" label="项目" min-width="200" />
          <el-table-column prop="currentPeriod" label="本期金额" width="150" align="right">
            <template #default="{ row }">
              <span :class="getIncomeItemClass(row.itemType)">{{ formatMoney(row.currentPeriod) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="previousPeriod" label="上期金额" width="150" align="right">
            <template #default="{ row }">
              <span :class="getIncomeItemClass(row.itemType)">{{ formatMoney(row.previousPeriod) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="changeAmount" label="变动金额" width="150" align="right">
            <template #default="{ row }">
              <span :class="row.changeAmount >= 0 ? 'positive-change' : 'negative-change'">
                {{ row.changeAmount >= 0 ? '+' : '' }}{{ formatMoney(row.changeAmount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="changeRate" label="变动比率" width="120" align="right">
            <template #default="{ row }">
              <span :class="row.changeRate >= 0 ? 'positive-change' : 'negative-change'">
                {{ row.changeRate >= 0 ? '+' : '' }}{{ row.changeRate }}%
              </span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 现金流量表 -->
      <div v-if="reportForm.reportType === 'cash_flow'" class="cash-flow">
        <div class="cash-flow-section">
          <h4 class="section-title">经营活动产生的现金流量</h4>
          <el-table :data="reportData.operatingCashFlow" border style="width: 100%">
            <el-table-column prop="itemName" label="项目" min-width="200" />
            <el-table-column prop="currentPeriod" label="本期金额" width="150" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.currentPeriod) }}
              </template>
            </el-table-column>
            <el-table-column prop="previousPeriod" label="上期金额" width="150" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.previousPeriod) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <div class="cash-flow-section">
          <h4 class="section-title">投资活动产生的现金流量</h4>
          <el-table :data="reportData.investingCashFlow" border style="width: 100%">
            <el-table-column prop="itemName" label="项目" min-width="200" />
            <el-table-column prop="currentPeriod" label="本期金额" width="150" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.currentPeriod) }}
              </template>
            </el-table-column>
            <el-table-column prop="previousPeriod" label="上期金额" width="150" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.previousPeriod) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <div class="cash-flow-section">
          <h4 class="section-title">筹资活动产生的现金流量</h4>
          <el-table :data="reportData.financingCashFlow" border style="width: 100%">
            <el-table-column prop="itemName" label="项目" min-width="200" />
            <el-table-column prop="currentPeriod" label="本期金额" width="150" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.currentPeriod) }}
              </template>
            </el-table-column>
            <el-table-column prop="previousPeriod" label="上期金额" width="150" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.previousPeriod) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <!-- 所有者权益（股东权益）变动表 -->
      <div v-if="reportForm.reportType === 'equity_statement'" class="equity-statement">
        <el-table :data="reportData.equityItems" border style="width: 100%" show-summary :summary-method="getEquitySummary">
          <el-table-column prop="itemName" label="项目" width="200" fixed="left" />
          <el-table-column prop="paidInCapital" label="实收资本" width="120" align="right">
            <template #default="{ row }">
              <span v-if="row.paidInCapital !== 0">{{ formatMoney(row.paidInCapital) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="capitalReserve" label="资本公积" width="120" align="right">
            <template #default="{ row }">
              <span v-if="row.capitalReserve !== 0">{{ formatMoney(row.capitalReserve) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="surplusReserve" label="盈余公积" width="120" align="right">
            <template #default="{ row }">
              <span v-if="row.surplusReserve !== 0">{{ formatMoney(row.surplusReserve) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="retainedEarnings" label="未分配利润" width="120" align="right">
            <template #default="{ row }">
              <span v-if="row.retainedEarnings !== 0">{{ formatMoney(row.retainedEarnings) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="totalEquity" label="所有者权益合计" width="140" align="right">
            <template #default="{ row }">
              <span class="total-equity">{{ formatMoney(row.totalEquity) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
    
    <!-- 对比分析对话框 -->
    <el-dialog v-model="comparisonDialog" title="同期对比分析" width="1200px">
      <div ref="comparisonChart" style="height: 400px"></div>
    </el-dialog>
    
    <!-- 趋势分析对话框 -->
    <el-dialog v-model="trendDialog" title="趋势分析" width="1200px">
      <div ref="trendChart" style="height: 400px"></div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const generating = ref(false)
const comparisonDialog = ref(false)
const trendDialog = ref(false)
const comparisonChart = ref(null)
const trendChart = ref(null)
let comparisonChartInstance = null
let trendChartInstance = null

// 报表生成表单
const reportForm = ref({
  reportType: 'balance_sheet',
  period: 'current_month',
  endDate: new Date().toISOString().split('T')[0]
})

// 报表数据
const reportData = ref(null)

// 模拟报表数据
const mockReportData = {
  balance_sheet: {
    generateTime: new Date().toLocaleString(),
    assets: [
      { itemName: '货币资金', currentAmount: 1200000, previousAmount: 1000000 },
      { itemName: '应收账款', currentAmount: 800000, previousAmount: 750000 },
      { itemName: '存货', currentAmount: 1500000, previousAmount: 1400000 },
      { itemName: '固定资产', currentAmount: 5000000, previousAmount: 5200000 },
      { itemName: '无形资产', currentAmount: 300000, previousAmount: 320000 }
    ],
    liabilities: [
      { itemName: '应付账款', currentAmount: 600000, previousAmount: 580000 },
      { itemName: '短期借款', currentAmount: 1000000, previousAmount: 1200000 },
      { itemName: '长期借款', currentAmount: 2000000, previousAmount: 2100000 },
      { itemName: '实收资本', currentAmount: 3000000, previousAmount: 3000000 },
      { itemName: '未分配利润', currentAmount: 2200000, previousAmount: 1990000 }
    ]
  },
  income_statement: {
    generateTime: new Date().toLocaleString(),
    incomeItems: [
      { itemName: '营业收入', itemType: 'revenue', currentPeriod: 5000000, previousPeriod: 4500000, changeAmount: 500000, changeRate: 11.11 },
      { itemName: '营业成本', itemType: 'cost', currentPeriod: 3000000, previousPeriod: 2800000, changeAmount: 200000, changeRate: 7.14 },
      { itemName: '销售费用', itemType: 'expense', currentPeriod: 500000, previousPeriod: 480000, changeAmount: 20000, changeRate: 4.17 },
      { itemName: '管理费用', itemType: 'expense', currentPeriod: 600000, previousPeriod: 550000, changeAmount: 50000, changeRate: 9.09 },
      { itemName: '财务费用', itemType: 'expense', currentPeriod: 100000, previousPeriod: 120000, changeAmount: -20000, changeRate: -16.67 },
      { itemName: '营业利润', itemType: 'profit', currentPeriod: 800000, previousPeriod: 550000, changeAmount: 250000, changeRate: 45.45 },
      { itemName: '净利润', itemType: 'profit', currentPeriod: 680000, previousPeriod: 467500, changeAmount: 212500, changeRate: 45.45 }
    ]
  },
  cash_flow: {
    generateTime: new Date().toLocaleString(),
    operatingCashFlow: [
      { itemName: '销售商品、提供劳务收到的现金', currentPeriod: 5200000, previousPeriod: 4800000 },
      { itemName: '购买商品、接受劳务支付的现金', currentPeriod: -3200000, previousPeriod: -3000000 },
      { itemName: '支付给职工及为职工支付的现金', currentPeriod: -800000, previousPeriod: -750000 },
      { itemName: '支付的各项税费', currentPeriod: -300000, previousPeriod: -280000 },
      { itemName: '经营活动产生的现金流量净额', currentPeriod: 900000, previousPeriod: 770000 }
    ],
    investingCashFlow: [
      { itemName: '购建固定资产支付的现金', currentPeriod: -500000, previousPeriod: -800000 },
      { itemName: '投资支付的现金', currentPeriod: -200000, previousPeriod: -100000 },
      { itemName: '投资活动产生的现金流量净额', currentPeriod: -700000, previousPeriod: -900000 }
    ],
    financingCashFlow: [
      { itemName: '吸收投资收到的现金', currentPeriod: 0, previousPeriod: 1000000 },
      { itemName: '取得借款收到的现金', currentPeriod: 500000, previousPeriod: 0 },
      { itemName: '偿还债务支付的现金', currentPeriod: -300000, previousPeriod: -200000 },
      { itemName: '分配股利支付的现金', currentPeriod: -200000, previousPeriod: -150000 },
      { itemName: '筹资活动产生的现金流量净额', currentPeriod: 0, previousPeriod: 650000 }
    ]
  },
  equity_statement: {
    generateTime: new Date().toLocaleString(),
    equityItems: [
      { itemName: '一、期初余额', paidInCapital: 3000000, capitalReserve: 500000, surplusReserve: 300000, retainedEarnings: 1990000, totalEquity: 5790000 },
      { itemName: '二、本期增减变动金额（减少以"-"号填列）', paidInCapital: 0, capitalReserve: 0, surplusReserve: 68000, retainedEarnings: 612000, totalEquity: 680000 },
      { itemName: '（一）综合收益总额', paidInCapital: 0, capitalReserve: 0, surplusReserve: 0, retainedEarnings: 680000, totalEquity: 680000 },
      { itemName: '（二）所有者投入和减少资本', paidInCapital: 0, capitalReserve: 0, surplusReserve: 0, retainedEarnings: 0, totalEquity: 0 },
      { itemName: '（三）利润分配', paidInCapital: 0, capitalReserve: 0, surplusReserve: 68000, retainedEarnings: -68000, totalEquity: 0 },
      { itemName: '1.提取盈余公积', paidInCapital: 0, capitalReserve: 0, surplusReserve: 68000, retainedEarnings: -68000, totalEquity: 0 },
      { itemName: '2.对所有者（或股东）的分配', paidInCapital: 0, capitalReserve: 0, surplusReserve: 0, retainedEarnings: 0, totalEquity: 0 },
      { itemName: '三、期末余额', paidInCapital: 3000000, capitalReserve: 500000, surplusReserve: 368000, retainedEarnings: 2602000, totalEquity: 6470000 }
    ]
  }
}

const formatMoney = (amount) => {
  if (!amount) return '¥0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const getReportTitle = () => {
  const titles = {
    'balance_sheet': '资产负债表',
    'income_statement': '利润表',
    'cash_flow': '现金流量表',
    'equity_statement': '所有者权益（股东权益）变动表'
  }
  return titles[reportForm.value.reportType] || '财务报表'
}

const getReportPeriod = () => {
  const periods = {
    'current_month': '本月',
    'current_quarter': '本季度',
    'current_year': '本年度',
    'custom': `截至${reportForm.value.endDate}`
  }
  return periods[reportForm.value.period] || '本期'
}

const getIncomeItemClass = (itemType) => {
  const classes = {
    'revenue': 'revenue-item',
    'cost': 'cost-item',
    'expense': 'expense-item',
    'profit': 'profit-item'
  }
  return classes[itemType] || ''
}

const getAssetsSummary = (param) => {
  const { columns, data } = param
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '资产总计'
      return
    }
    if (column.property === 'currentAmount' || column.property === 'previousAmount') {
      const values = data.map(item => Number(item[column.property]))
      sums[index] = formatMoney(values.reduce((prev, curr) => prev + curr, 0))
    } else {
      sums[index] = ''
    }
  })
  return sums
}

const getLiabilitiesSummary = (param) => {
  const { columns, data } = param
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '负债和所有者权益总计'
      return
    }
    if (column.property === 'currentAmount' || column.property === 'previousAmount') {
      const values = data.map(item => Number(item[column.property]))
      sums[index] = formatMoney(values.reduce((prev, curr) => prev + curr, 0))
    } else {
      sums[index] = ''
    }
  })
  return sums
}

const getIncomeSummary = (param) => {
  return ['', '', '', '', '']
}

const getEquitySummary = (param) => {
  return ['', '', '', '', '', '']
}

const handleReportTypeChange = () => {
  reportData.value = null
}

const generateReport = () => {
  generating.value = true
  setTimeout(() => {
    reportData.value = mockReportData[reportForm.value.reportType]
    generating.value = false
    ElMessage.success('报表生成成功')
  }, 1000)
}

const exportReport = () => {
  ElMessage.success('报表导出功能开发中')
}

const printReport = () => {
  window.print()
}

const showComparison = () => {
  comparisonDialog.value = true
  nextTick(() => {
    initComparisonChart()
  })
}

const showTrend = () => {
  trendDialog.value = true
  nextTick(() => {
    initTrendChart()
  })
}

const initComparisonChart = () => {
  if (comparisonChart.value) {
    comparisonChartInstance = echarts.init(comparisonChart.value)
    const option = {
      title: {
        text: '同期对比分析',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['本期', '上期'],
        top: 30
      },
      xAxis: {
        type: 'category',
        data: ['营业收入', '营业成本', '销售费用', '管理费用', '营业利润', '净利润']
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (value) => (value / 10000) + '万'
        }
      },
      series: [
        {
          name: '本期',
          type: 'bar',
          data: [5000000, 3000000, 500000, 600000, 800000, 680000],
          itemStyle: { color: '#409eff' }
        },
        {
          name: '上期',
          type: 'bar',
          data: [4500000, 2800000, 480000, 550000, 550000, 467500],
          itemStyle: { color: '#67c23a' }
        }
      ]
    }
    comparisonChartInstance.setOption(option)
  }
}

const initTrendChart = () => {
  if (trendChart.value) {
    trendChartInstance = echarts.init(trendChart.value)
    const option = {
      title: {
        text: '财务指标趋势分析',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['营业收入', '净利润', '资产总额'],
        top: 30
      },
      xAxis: {
        type: 'category',
        data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月']
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (value) => (value / 10000) + '万'
        }
      },
      series: [
        {
          name: '营业收入',
          type: 'line',
          data: [4200000, 4300000, 4500000, 4600000, 4800000, 4900000, 5000000, 5200000],
          smooth: true,
          itemStyle: { color: '#409eff' }
        },
        {
          name: '净利润',
          type: 'line',
          data: [420000, 450000, 480000, 520000, 580000, 620000, 680000, 720000],
          smooth: true,
          itemStyle: { color: '#67c23a' }
        },
        {
          name: '资产总额',
          type: 'line',
          data: [8500000, 8600000, 8700000, 8750000, 8800000, 8850000, 8900000, 8800000],
          smooth: true,
          itemStyle: { color: '#e6a23c' }
        }
      ]
    }
    trendChartInstance.setOption(option)
  }
}
</script>

<style scoped lang="scss">
.control-panel {
  margin-bottom: 20px;
}

.report-display {
  .report-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    
    .report-title {
      h3 {
        margin: 0 0 8px 0;
        color: #303133;
        font-size: 20px;
      }
      
      .report-period {
        margin: 4px 0;
        color: #606266;
        font-size: 14px;
      }
      
      .generate-time {
        margin: 4px 0 0 0;
        color: #909399;
        font-size: 12px;
      }
    }
    
    .report-actions {
      display: flex;
      gap: 8px;
    }
  }
}

.balance-sheet {
  .section-title {
    margin: 0 0 16px 0;
    padding: 8px 0;
    border-bottom: 2px solid #409eff;
    color: #303133;
    font-size: 16px;
  }
}

.income-statement {
  .revenue-item {
    color: #67c23a;
    font-weight: 600;
  }
  
  .cost-item {
    color: #f56c6c;
    font-weight: 600;
  }
  
  .expense-item {
    color: #e6a23c;
    font-weight: 600;
  }
  
  .profit-item {
    color: #409eff;
    font-weight: 700;
  }
  
  .positive-change {
    color: #67c23a;
    font-weight: 600;
  }
  
  .negative-change {
    color: #f56c6c;
    font-weight: 600;
  }
}

.cash-flow {
  .cash-flow-section {
    margin-bottom: 30px;
    
    .section-title {
      margin: 0 0 16px 0;
      padding: 8px 0;
      border-bottom: 2px solid #409eff;
      color: #303133;
      font-size: 16px;
    }
  }
}

.equity-statement {
  .total-equity {
    font-weight: 700;
    color: #409eff;
  }
  
  :deep(.el-table) {
    .el-table__row {
      &:first-child,
      &:nth-child(2),
      &:last-child {
        background-color: #f5f7fa;
        font-weight: 600;
      }
    }
  }
}

@media (max-width: 768px) {
  .report-header {
    flex-direction: column;
    gap: 16px;
    
    .report-actions {
      align-self: stretch;
      
      .el-button {
        flex: 1;
      }
    }
  }
  
  .balance-sheet {
    .el-row {
      flex-direction: column;
      
      .el-col {
        margin-bottom: 20px;
      }
    }
  }
}
</style>