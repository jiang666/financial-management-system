<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h2 class="title"></h2>
      <p class="description"></p>
    </div>
    
    <!-- 核心指标卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card modern-card">
          <div class="stat-bg">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <el-icon><Coin /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">总资产</div>
              <div class="stat-value">{{ formatNumber(totalAssets) }}</div>
              <div class="stat-trend up">
                <el-icon><ArrowUp /></el-icon>
                <span>+12.5%</span>
              </div>
            </div>
          </div>
          <div class="mini-chart" ref="assetsChart"></div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card modern-card">
          <div class="stat-bg">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">本月收入</div>
              <div class="stat-value">{{ formatNumber(monthlyIncome) }}</div>
              <div class="stat-trend up">
                <el-icon><ArrowUp /></el-icon>
                <span>+8.3%</span>
              </div>
            </div>
          </div>
          <div class="mini-chart" ref="incomeChart"></div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card modern-card">
          <div class="stat-bg">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
              <el-icon><Wallet /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">本月支出</div>
              <div class="stat-value">{{ formatNumber(monthlyExpense) }}</div>
              <div class="stat-trend down">
                <el-icon><ArrowDown /></el-icon>
                <span>-5.2%</span>
              </div>
            </div>
          </div>
          <div class="mini-chart" ref="expenseChart"></div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card modern-card">
          <div class="stat-bg">
            <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
              <el-icon><DocumentCopy /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">待处理凭证</div>
              <div class="stat-value">{{ pendingVouchers }}</div>
              <div class="stat-trend">
                <span class="urgent">需关注</span>
              </div>
            </div>
          </div>
          <div class="mini-chart" ref="vouchersChart"></div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 核心数据看板 -->
    <el-row :gutter="20" class="mt-lg">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>财务趋势分析</span>
              <el-radio-group v-model="trendPeriod" size="small">
                <el-radio-button label="7天">近7天</el-radio-button>
                <el-radio-button label="30天">近30天</el-radio-button>
                <el-radio-button label="90天">近90天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChart" style="height: 350px;"></div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>资产结构分析</span>
            </div>
          </template>
          <div ref="assetStructureChart" style="height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 详细数据分析 -->
    <el-row :gutter="20" class="mt-lg">
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>收支结构</span>
            </div>
          </template>
          <div ref="incomeExpenseChart" style="height: 280px;"></div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>部门费用占比</span>
            </div>
          </template>
          <div ref="departmentChart" style="height: 280px;"></div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>现金流分析</span>
            </div>
          </template>
          <div ref="cashFlowChart" style="height: 280px;"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 快捷操作和系统状态 -->
    <el-row :gutter="20" class="mt-lg">
      <el-col :span="16">
        <el-card class="modern-card">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
              <el-tag type="info" size="small">常用功能</el-tag>
            </div>
          </template>
          <div class="quick-actions">
            <div class="action-item" @click="navigateTo('/voucher/create')">
              <div class="action-icon" style="background: linear-gradient(45deg, #667eea, #764ba2);">
                <el-icon><DocumentAdd /></el-icon>
              </div>
              <div class="action-text">
                <div class="action-title">新增凭证</div>
                <div class="action-desc">快速录入会计凭证</div>
              </div>
            </div>
            <div class="action-item" @click="navigateTo('/settings/account')">
              <div class="action-icon" style="background: linear-gradient(45deg, #f093fb, #f5576c);">
                <el-icon><Notebook /></el-icon>
              </div>
              <div class="action-text">
                <div class="action-title">科目管理</div>
                <div class="action-desc">维护会计科目体系</div>
              </div>
            </div>
            <div class="action-item" @click="navigateTo('/report/balance')">
              <div class="action-icon" style="background: linear-gradient(45deg, #4facfe, #00f2fe);">
                <el-icon><DataAnalysis /></el-icon>
              </div>
              <div class="action-text">
                <div class="action-title">财务报表</div>
                <div class="action-desc">查看资产负债表</div>
              </div>
            </div>
            <div class="action-item" @click="navigateTo('/settings/organization')">
              <div class="action-icon" style="background: linear-gradient(45deg, #fa709a, #fee140);">
                <el-icon><OfficeBuilding /></el-icon>
              </div>
              <div class="action-text">
                <div class="action-title">组织架构</div>
                <div class="action-desc">管理部门人员结构</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="modern-card">
          <template #header>
            <div class="card-header">
              <span>系统动态</span>
              <el-badge :value="3" class="item">
                <el-icon><Bell /></el-icon>
              </el-badge>
            </div>
          </template>
          <div class="system-status">
            <div class="status-item">
              <div class="status-icon success">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="status-content">
                <div class="status-title">系统运行正常</div>
                <div class="status-time">2025-08-15 11:30:25</div>
              </div>
            </div>
            <div class="status-item">
              <div class="status-icon warning">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="status-content">
                <div class="status-title">15张凭证待审核</div>
                <div class="status-time">2025-08-15 10:15:00</div>
              </div>
            </div>
            <div class="status-item">
              <div class="status-icon info">
                <el-icon><InfoFilled /></el-icon>
              </div>
              <div class="status-content">
                <div class="status-title">月末结账提醒</div>
                <div class="status-time">2025-08-15 09:00:00</div>
              </div>
            </div>
          </div>
          
          <div class="weather-widget">
            <div class="weather-info">
              <div class="weather-icon">☀️</div>
              <div class="weather-text">
                <div>北京 晴朗 28°C</div>
                <div>适宜办公，心情愉悦</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const router = useRouter()

// 数据统计
const totalAssets = ref(8650000)
const monthlyIncome = ref(1580000)
const monthlyExpense = ref(920000)
const pendingVouchers = ref(15)
const trendPeriod = ref('30天')

// 图表实例
const charts = ref([])
const assetsChart = ref()
const incomeChart = ref()
const expenseChart = ref()
const vouchersChart = ref()
const trendChart = ref()
const assetStructureChart = ref()
const incomeExpenseChart = ref()
const departmentChart = ref()
const cashFlowChart = ref()

// 格式化数字显示
const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toLocaleString()
}

// 初始化迷你图表
const initMiniCharts = () => {
  // 总资产趋势
  const assetsChartInstance = echarts.init(assetsChart.value)
  const assetsOption = {
    grid: { left: 0, right: 0, top: 0, bottom: 0 },
    xAxis: { type: 'category', show: false, data: ['', '', '', '', '', '', ''] },
    yAxis: { type: 'value', show: false },
    series: [{
      type: 'line',
      data: [820, 932, 901, 934, 1290, 1330, 1320],
      smooth: true,
      lineStyle: { color: '#667eea', width: 2 },
      areaStyle: { 
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(102, 126, 234, 0.6)' },
          { offset: 1, color: 'rgba(102, 126, 234, 0.1)' }
        ])
      },
      symbol: 'none'
    }]
  }
  assetsChartInstance.setOption(assetsOption)
  charts.value.push(assetsChartInstance)

  // 收入趋势
  const incomeChartInstance = echarts.init(incomeChart.value)
  const incomeOption = {
    grid: { left: 0, right: 0, top: 0, bottom: 0 },
    xAxis: { type: 'category', show: false, data: ['', '', '', '', '', '', ''] },
    yAxis: { type: 'value', show: false },
    series: [{
      type: 'line',
      data: [650, 720, 680, 750, 820, 880, 920],
      smooth: true,
      lineStyle: { color: '#f093fb', width: 2 },
      areaStyle: { 
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(240, 147, 251, 0.6)' },
          { offset: 1, color: 'rgba(240, 147, 251, 0.1)' }
        ])
      },
      symbol: 'none'
    }]
  }
  incomeChartInstance.setOption(incomeOption)
  charts.value.push(incomeChartInstance)

  // 支出趋势
  const expenseChartInstance = echarts.init(expenseChart.value)
  const expenseOption = {
    grid: { left: 0, right: 0, top: 0, bottom: 0 },
    xAxis: { type: 'category', show: false, data: ['', '', '', '', '', '', ''] },
    yAxis: { type: 'value', show: false },
    series: [{
      type: 'line',
      data: [520, 480, 510, 490, 460, 440, 420],
      smooth: true,
      lineStyle: { color: '#4facfe', width: 2 },
      areaStyle: { 
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(79, 172, 254, 0.6)' },
          { offset: 1, color: 'rgba(79, 172, 254, 0.1)' }
        ])
      },
      symbol: 'none'
    }]
  }
  expenseChartInstance.setOption(expenseOption)
  charts.value.push(expenseChartInstance)

  // 凭证处理趋势
  const vouchersChartInstance = echarts.init(vouchersChart.value)
  const vouchersOption = {
    grid: { left: 0, right: 0, top: 0, bottom: 0 },
    xAxis: { type: 'category', show: false, data: ['', '', '', '', '', '', ''] },
    yAxis: { type: 'value', show: false },
    series: [{
      type: 'bar',
      data: [12, 18, 15, 22, 20, 18, 15],
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(250, 112, 154, 0.8)' },
          { offset: 1, color: 'rgba(254, 225, 64, 0.8)' }
        ])
      },
      barWidth: '60%'
    }]
  }
  vouchersChartInstance.setOption(vouchersOption)
  charts.value.push(vouchersChartInstance)
}

// 初始化主要图表
const initMainCharts = () => {
  // 财务趋势分析
  const trendChartInstance = echarts.init(trendChart.value)
  const trendOption = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
    legend: { data: ['收入', '支出', '利润'], top: 10 },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月']
    },
    yAxis: { type: 'value', axisLabel: { formatter: '{value}万' } },
    series: [
      {
        name: '收入',
        type: 'line',
        data: [120, 132, 101, 134, 190, 230, 210, 158],
        smooth: true,
        lineStyle: { color: '#67C23A', width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.1)' }
          ])
        }
      },
      {
        name: '支出',
        type: 'line',
        data: [80, 82, 91, 84, 109, 110, 120, 92],
        smooth: true,
        lineStyle: { color: '#E6A23C', width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(230, 162, 60, 0.3)' },
            { offset: 1, color: 'rgba(230, 162, 60, 0.1)' }
          ])
        }
      },
      {
        name: '利润',
        type: 'bar',
        data: [40, 50, 10, 50, 81, 120, 90, 66],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.8)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.4)' }
          ])
        }
      }
    ]
  }
  trendChartInstance.setOption(trendOption)
  charts.value.push(trendChartInstance)

  // 资产结构分析
  const assetStructureChartInstance = echarts.init(assetStructureChart.value)
  const assetStructureOption = {
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c}万 ({d}%)' },
    series: [{
      name: '资产结构',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '50%'],
      data: [
        { value: 3500, name: '流动资产', itemStyle: { color: '#409EFF' } },
        { value: 2800, name: '固定资产', itemStyle: { color: '#67C23A' } },
        { value: 1200, name: '无形资产', itemStyle: { color: '#E6A23C' } },
        { value: 800, name: '其他资产', itemStyle: { color: '#F56C6C' } }
      ],
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } },
      label: { formatter: '{b}\n{d}%' },
      labelLine: { show: true }
    }]
  }
  assetStructureChartInstance.setOption(assetStructureOption)
  charts.value.push(assetStructureChartInstance)

  // 收支结构
  const incomeExpenseChartInstance = echarts.init(incomeExpenseChart.value)
  const incomeExpenseOption = {
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '70%',
      data: [
        { value: 580, name: '主营业务收入', itemStyle: { color: '#5470c6' } },
        { value: 120, name: '其他业务收入', itemStyle: { color: '#91cc75' } },
        { value: 320, name: '运营成本', itemStyle: { color: '#fac858' } },
        { value: 180, name: '管理费用', itemStyle: { color: '#ee6666' } }
      ],
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  }
  incomeExpenseChartInstance.setOption(incomeExpenseOption)
  charts.value.push(incomeExpenseChartInstance)

  // 部门费用占比
  const departmentChartInstance = echarts.init(departmentChart.value)
  const departmentOption = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: {
      type: 'category',
      data: ['研发部', '销售部', '市场部', '行政部', '财务部']
    },
    series: [{
      type: 'bar',
      data: [320, 280, 220, 180, 120],
      itemStyle: {
        color: (params) => {
          const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de']
          return colors[params.dataIndex]
        }
      },
      barWidth: '60%'
    }]
  }
  departmentChartInstance.setOption(departmentOption)
  charts.value.push(departmentChartInstance)

  // 现金流分析
  const cashFlowChartInstance = echarts.init(cashFlowChart.value)
  const cashFlowOption = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['现金流入', '现金流出'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['Q1', 'Q2', 'Q3', 'Q4']
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '现金流入',
        type: 'line',
        data: [420, 580, 650, 720],
        lineStyle: { color: '#67C23A', width: 3 },
        itemStyle: { color: '#67C23A' }
      },
      {
        name: '现金流出',
        type: 'line',
        data: [380, 520, 580, 620],
        lineStyle: { color: '#E6A23C', width: 3 },
        itemStyle: { color: '#E6A23C' }
      }
    ]
  }
  cashFlowChartInstance.setOption(cashFlowOption)
  charts.value.push(cashFlowChartInstance)
}

// 处理窗口大小变化
const handleResize = () => {
  charts.value.forEach(chart => {
    chart.resize()
  })
}

const navigateTo = (path) => {
  if (path.startsWith('/voucher') || path.startsWith('/report') || path.startsWith('/cash')) {
    ElMessage.info('该功能模块正在开发中')
    return
  }
  router.push(path)
}

onMounted(async () => {
  await nextTick()
  initMiniCharts()
  initMainCharts()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  charts.value.forEach(chart => {
    chart.dispose()
  })
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
.dashboard-container {
  background: #f0f2f5;
  min-height: calc(100vh - 60px);
  
  .page-header {
    text-align: center;
    margin-bottom: 30px;
    
    .title {
      font-size: 32px;
      font-weight: 600;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin-bottom: 10px;
    }
    
    .description {
      font-size: 16px;
      color: #666;
    }
  }
  
  // 现代化统计卡片
  .stats-row {
    margin-bottom: 30px;
  }
  
  .stat-card {
    position: relative;
    background: #fff;
    border-radius: 16px;
    padding: 24px;
    margin-bottom: 20px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.2);
    overflow: hidden;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-8px);
      box-shadow: 0 16px 48px rgba(0, 0, 0, 0.15);
    }
    
    .stat-bg {
      display: flex;
      align-items: center;
      position: relative;
      z-index: 2;
    }
    
    .stat-icon {
      width: 72px;
      height: 72px;
      border-radius: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 32px;
      margin-right: 20px;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
    }
    
    .stat-content {
      flex: 1;
      
      .stat-title {
        color: #8a8a8a;
        font-size: 14px;
        font-weight: 500;
        margin-bottom: 8px;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }
      
      .stat-value {
        font-size: 32px;
        font-weight: 700;
        color: #2c3e50;
        margin-bottom: 8px;
        line-height: 1;
      }
      
      .stat-trend {
        display: flex;
        align-items: center;
        font-size: 12px;
        font-weight: 600;
        
        &.up {
          color: #27ae60;
        }
        
        &.down {
          color: #e74c3c;
        }
        
        .el-icon {
          margin-right: 4px;
        }
        
        .urgent {
          background: linear-gradient(45deg, #ff6b6b, #ffa726);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
          font-weight: 700;
        }
      }
    }
    
    .mini-chart {
      position: absolute;
      bottom: -10px;
      right: -10px;
      width: 120px;
      height: 60px;
      opacity: 0.3;
      z-index: 1;
    }
  }
  
  // 图表卡片
  .chart-card {
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.2);
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
    }
    
    :deep(.el-card__header) {
      background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
      border-bottom: 1px solid #eee;
    }
  }
  
  // 现代化快捷操作
  .quick-actions {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 24px;
    
    .action-item {
      display: flex;
      align-items: center;
      padding: 24px;
      background: #fff;
      border-radius: 16px;
      cursor: pointer;
      transition: all 0.3s ease;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.2);
      
      &:hover {
        transform: translateY(-4px) scale(1.02);
        box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
      }
      
      .action-icon {
        width: 64px;
        height: 64px;
        border-radius: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        font-size: 28px;
        margin-right: 20px;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
        
        .el-icon {
          color: #fff;
        }
      }
      
      .action-text {
        flex: 1;
        
        .action-title {
          font-size: 18px;
          font-weight: 600;
          color: #2c3e50;
          margin-bottom: 4px;
        }
        
        .action-desc {
          font-size: 14px;
          color: #7f8c8d;
          line-height: 1.4;
        }
      }
    }
  }
  
  // 系统状态
  .system-status {
    .status-item {
      display: flex;
      align-items: center;
      padding: 16px 0;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      .status-icon {
        width: 40px;
        height: 40px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16px;
        
        &.success {
          background: rgba(39, 174, 96, 0.1);
          color: #27ae60;
        }
        
        &.warning {
          background: rgba(230, 162, 60, 0.1);
          color: #e6a23c;
        }
        
        &.info {
          background: rgba(64, 158, 255, 0.1);
          color: #409eff;
        }
      }
      
      .status-content {
        flex: 1;
        
        .status-title {
          font-size: 14px;
          font-weight: 500;
          color: #2c3e50;
          margin-bottom: 4px;
        }
        
        .status-time {
          font-size: 12px;
          color: #7f8c8d;
        }
      }
    }
  }
  
  // 天气小部件
  .weather-widget {
    margin-top: 24px;
    padding: 20px;
    background: linear-gradient(135deg, #74b9ff 0%, #0984e3 100%);
    border-radius: 16px;
    color: #fff;
    
    .weather-info {
      display: flex;
      align-items: center;
      
      .weather-icon {
        font-size: 32px;
        margin-right: 16px;
      }
      
      .weather-text {
        flex: 1;
        
        div:first-child {
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 4px;
        }
        
        div:last-child {
          font-size: 14px;
          opacity: 0.9;
        }
      }
    }
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    span {
      font-size: 18px;
      font-weight: 600;
      color: #2c3e50;
    }
  }
  
  // 现代化卡片
  .modern-card {
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.2);
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
    }
    
    :deep(.el-card__header) {
      background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
      border-bottom: 1px solid #eee;
      border-radius: 16px 16px 0 0;
    }
  }
  
  // 响应式
  @media (max-width: 1200px) {
    .quick-actions {
      grid-template-columns: 1fr;
    }
  }
  
  @media (max-width: 768px) {
    .stats-row {
      .el-col {
        margin-bottom: 16px;
      }
    }
    
    .stat-card {
      .stat-icon {
        width: 56px;
        height: 56px;
        font-size: 24px;
      }
      
      .stat-value {
        font-size: 24px;
      }
    }
  }
}
</style>