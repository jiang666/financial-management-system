<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">成本分析</h2>
      <p class="description">深度分析产品成本构成，优化成本结构，提升盈利能力</p>
    </div>
    
    <!-- 成本概览 -->
    <el-row :gutter="16" class="cost-overview">
      <el-col :span="6" v-for="item in costSummary" :key="item.id">
        <el-card class="cost-card" :class="item.trend">
          <div class="cost-content">
            <div class="cost-icon">
              <el-icon :size="32" :color="item.color">
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div class="cost-info">
              <h4>{{ item.title }}</h4>
              <p class="cost-value">{{ item.value }}</p>
              <div class="cost-change">
                <el-icon :color="item.changeColor">
                  <component :is="item.changeIcon" />
                </el-icon>
                <span :style="{ color: item.changeColor }">
                  {{ item.change }}
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-tabs v-model="activeTab">
      <!-- 成本构成分析 -->
      <el-tab-pane label="成本构成" name="structure">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>成本结构分析</span>
                  <el-radio-group v-model="structureType" size="small" @change="updateStructureChart">
                    <el-radio-button label="product">按产品</el-radio-button>
                    <el-radio-button label="category">按类别</el-radio-button>
                    <el-radio-button label="department">按部门</el-radio-button>
                  </el-radio-group>
                </div>
              </template>
              <div ref="structureChart" style="height: 400px"></div>
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card>
              <template #header>
                <span>成本占比</span>
              </template>
              <div ref="ratioChart" style="height: 400px"></div>
            </el-card>
          </el-col>
        </el-row>
        
        <!-- 成本明细表 -->
        <el-card class="cost-detail-card">
          <template #header>
            <span>成本明细</span>
          </template>
          
          <el-table :data="costDetailData" border style="width: 100%" stripe show-summary :summary-method="getCostSummary">
            <el-table-column prop="itemName" label="成本项目" min-width="150" />
            <el-table-column prop="directMaterial" label="直接材料" width="120" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.directMaterial) }}
              </template>
            </el-table-column>
            <el-table-column prop="directLabor" label="直接人工" width="120" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.directLabor) }}
              </template>
            </el-table-column>
            <el-table-column prop="manufacturing" label="制造费用" width="120" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.manufacturing) }}
              </template>
            </el-table-column>
            <el-table-column prop="totalCost" label="总成本" width="120" align="right">
              <template #default="{ row }">
                <span class="total-cost">{{ formatMoney(row.totalCost) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="unitCost" label="单位成本" width="120" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.unitCost) }}
              </template>
            </el-table-column>
            <el-table-column prop="costRate" label="成本率" width="100" align="right">
              <template #default="{ row }">
                {{ row.costRate }}%
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button link type="primary" size="small" @click="handleViewCostDetail(row)">详情</el-button>
                  <el-button link type="success" size="small" @click="handleAnalyzeCost(row)">分析</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
      
      <!-- 成本对比分析 -->
      <el-tab-pane label="成本对比" name="comparison">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>成本对比分析</span>
              <div class="header-controls">
                <el-select v-model="comparisonPeriod" placeholder="选择期间" style="width: 200px; margin-right: 10px;">
                  <el-option label="本年 vs 上年" value="year" />
                  <el-option label="本季 vs 上季" value="quarter" />
                  <el-option label="本月 vs 上月" value="month" />
                </el-select>
                <el-button type="primary" @click="updateComparisonChart">更新对比</el-button>
              </div>
            </div>
          </template>
          
          <div ref="comparisonChart" style="height: 500px"></div>
        </el-card>
        
        <!-- 对比分析表 -->
        <el-card class="comparison-table">
          <template #header>
            <span>成本变动分析</span>
          </template>
          
          <el-table :data="comparisonData" border style="width: 100%" stripe>
            <el-table-column prop="costItem" label="成本项目" min-width="150" />
            <el-table-column prop="currentPeriod" label="本期金额" width="140" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.currentPeriod) }}
              </template>
            </el-table-column>
            <el-table-column prop="previousPeriod" label="上期金额" width="140" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.previousPeriod) }}
              </template>
            </el-table-column>
            <el-table-column prop="variance" label="变动金额" width="140" align="right">
              <template #default="{ row }">
                <span :class="row.variance >= 0 ? 'cost-increase' : 'cost-decrease'">
                  {{ row.variance >= 0 ? '+' : '' }}{{ formatMoney(row.variance) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="varianceRate" label="变动率" width="120" align="right">
              <template #default="{ row }">
                <span :class="Math.abs(row.varianceRate) > 10 ? 'significant-change' : 'normal-change'">
                  {{ row.varianceRate >= 0 ? '+' : '' }}{{ row.varianceRate }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="变动原因" min-width="200" show-overflow-tooltip />
            <el-table-column prop="impact" label="影响评估" width="120">
              <template #default="{ row }">
                <el-tag :type="getImpactType(row.impact)" size="small">
                  {{ row.impact }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
      
      <!-- 成本控制 -->
      <el-tab-pane label="成本控制" name="control">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>成本预警</span>
              </template>
              
              <div class="warning-list">
                <el-alert
                  v-for="warning in costWarnings"
                  :key="warning.id"
                  :title="warning.title"
                  :description="warning.description"
                  :type="warning.type"
                  show-icon
                  :closable="false"
                  style="margin-bottom: 12px"
                />
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>成本控制目标</span>
              </template>
              
              <div class="control-targets">
                <div v-for="target in controlTargets" :key="target.id" class="target-item">
                  <div class="target-header">
                    <span class="target-name">{{ target.name }}</span>
                    <el-tag :type="getTargetStatus(target.achievement)" size="small">
                      {{ getTargetStatusText(target.achievement) }}
                    </el-tag>
                  </div>
                  <div class="target-progress">
                    <el-progress 
                      :percentage="target.achievement" 
                      :status="getTargetProgressStatus(target.achievement)"
                      :stroke-width="12"
                    />
                  </div>
                  <div class="target-info">
                    <span>目标：{{ formatMoney(target.target) }}</span>
                    <span>实际：{{ formatMoney(target.actual) }}</span>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <!-- 成本控制措施 -->
        <el-card class="control-measures">
          <template #header>
            <div class="card-header">
              <span>成本控制措施</span>
              <el-button type="primary" icon="Plus" @click="handleAddMeasure">新增措施</el-button>
            </div>
          </template>
          
          <el-table :data="controlMeasures" border style="width: 100%" stripe>
            <el-table-column prop="measureName" label="控制措施" min-width="200" />
            <el-table-column prop="targetCost" label="目标成本" width="140" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.targetCost) }}
              </template>
            </el-table-column>
            <el-table-column prop="expectedSaving" label="预期节约" width="140" align="right">
              <template #default="{ row }">
                <span class="cost-saving">{{ formatMoney(row.expectedSaving) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="actualSaving" label="实际节约" width="140" align="right">
              <template #default="{ row }">
                <span class="cost-saving">{{ formatMoney(row.actualSaving) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="implementDate" label="实施日期" width="110" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getMeasureStatusType(row.status)" size="small">
                  {{ getMeasureStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="responsible" label="负责人" width="100" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button link type="primary" size="small" @click="handleViewMeasure(row)">查看</el-button>
                  <el-button link type="success" size="small" @click="handleEditMeasure(row)">编辑</el-button>
                  <el-button link type="warning" size="small" @click="handleEvaluateMeasure(row)">评估</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
      
      <!-- 成本趋势 -->
      <el-tab-pane label="成本趋势" name="trend">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>成本趋势分析</span>
              <div class="header-controls">
                <el-radio-group v-model="trendPeriod" size="small" @change="updateTrendChart">
                  <el-radio-button label="month">月度趋势</el-radio-button>
                  <el-radio-button label="quarter">季度趋势</el-radio-button>
                  <el-radio-button label="year">年度趋势</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          
          <div ref="trendChart" style="height: 450px"></div>
        </el-card>
        
        <!-- 趋势分析结论 -->
        <el-card class="trend-analysis">
          <template #header>
            <span>趋势分析结论</span>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="8" v-for="conclusion in trendConclusions" :key="conclusion.id">
              <div class="conclusion-item">
                <h4>{{ conclusion.title }}</h4>
                <p>{{ conclusion.description }}</p>
                <el-tag :type="conclusion.type" size="small">{{ conclusion.level }}</el-tag>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { PieChart, TrendCharts, Warning, Aim, ArrowUp, ArrowDown, Minus } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const activeTab = ref('structure')
const structureType = ref('product')
const comparisonPeriod = ref('year')
const trendPeriod = ref('month')

const structureChart = ref(null)
const ratioChart = ref(null)
const comparisonChart = ref(null)
const trendChart = ref(null)
let structureChartInstance = null
let ratioChartInstance = null
let comparisonChartInstance = null
let trendChartInstance = null

// 成本概览数据
const costSummary = ref([
  {
    id: 1,
    title: '总成本',
    value: '¥3,200万',
    change: '较上期+5.2%',
    trend: 'increase',
    icon: PieChart,
    color: '#409eff',
    changeIcon: ArrowUp,
    changeColor: '#f56c6c'
  },
  {
    id: 2,
    title: '单位成本',
    value: '¥128/件',
    change: '较上期-2.1%',
    trend: 'decrease',
    icon: TrendCharts,
    color: '#67c23a',
    changeIcon: ArrowDown,
    changeColor: '#67c23a'
  },
  {
    id: 3,
    title: '材料成本',
    value: '¥1,920万',
    change: '较上期+8.5%',
    trend: 'increase',
    icon: Warning,
    color: '#e6a23c',
    changeIcon: ArrowUp,
    changeColor: '#f56c6c'
  },
  {
    id: 4,
    title: '人工成本',
    value: '¥800万',
    change: '较上期持平',
    trend: 'stable',
    icon: Aim,
    color: '#909399',
    changeIcon: Minus,
    changeColor: '#909399'
  }
])

// 成本明细数据
const costDetailData = ref([
  {
    itemName: '产品A',
    directMaterial: 1200000,
    directLabor: 400000,
    manufacturing: 200000,
    totalCost: 1800000,
    unitCost: 120,
    costRate: 64.2
  },
  {
    itemName: '产品B',
    directMaterial: 720000,
    directLabor: 240000,
    manufacturing: 120000,
    totalCost: 1080000,
    unitCost: 108,
    costRate: 58.7
  },
  {
    itemName: '产品C',
    directMaterial: 320000,
    directLabor: 160000,
    manufacturing: 80000,
    totalCost: 560000,
    unitCost: 140,
    costRate: 72.1
  }
])

// 对比数据
const comparisonData = ref([
  {
    costItem: '直接材料',
    currentPeriod: 1920000,
    previousPeriod: 1770000,
    variance: 150000,
    varianceRate: 8.5,
    reason: '原材料价格上涨',
    impact: '高'
  },
  {
    costItem: '直接人工',
    currentPeriod: 800000,
    previousPeriod: 820000,
    variance: -20000,
    varianceRate: -2.4,
    reason: '自动化程度提升',
    impact: '低'
  },
  {
    costItem: '制造费用',
    currentPeriod: 480000,
    previousPeriod: 450000,
    variance: 30000,
    varianceRate: 6.7,
    reason: '设备维护费用增加',
    impact: '中'
  }
])

// 成本预警
const costWarnings = ref([
  {
    id: 1,
    title: '材料成本超标',
    description: '直接材料成本较预算超支8.5%，需要关注原材料采购价格',
    type: 'error'
  },
  {
    id: 2,
    title: '制造费用上升',
    description: '制造费用较上期增长6.7%，主要由设备维护费用增加导致',
    type: 'warning'
  },
  {
    id: 3,
    title: '人工成本控制良好',
    description: '人工成本控制在目标范围内，自动化改造效果显著',
    type: 'success'
  }
])

// 控制目标
const controlTargets = ref([
  {
    id: 1,
    name: '材料成本控制',
    target: 1800000,
    actual: 1920000,
    achievement: 93.8
  },
  {
    id: 2,
    name: '人工成本控制',
    target: 850000,
    actual: 800000,
    achievement: 106.3
  },
  {
    id: 3,
    name: '制造费用控制',
    target: 450000,
    actual: 480000,
    achievement: 93.8
  }
])

// 控制措施
const controlMeasures = ref([
  {
    id: 1,
    measureName: '优化供应商结构',
    targetCost: 1800000,
    expectedSaving: 120000,
    actualSaving: 85000,
    implementDate: '2025-06-01',
    status: 'implementing',
    responsible: '采购部'
  },
  {
    id: 2,
    measureName: '提升生产效率',
    targetCost: 800000,
    expectedSaving: 50000,
    actualSaving: 62000,
    implementDate: '2025-05-15',
    status: 'completed',
    responsible: '生产部'
  },
  {
    id: 3,
    measureName: '设备维护优化',
    targetCost: 450000,
    expectedSaving: 30000,
    actualSaving: 0,
    implementDate: '2025-09-01',
    status: 'planning',
    responsible: '设备部'
  }
])

// 趋势分析结论
const trendConclusions = ref([
  {
    id: 1,
    title: '材料成本趋势',
    description: '材料成本呈上升趋势，主要受原材料价格波动影响，建议加强供应商管理',
    type: 'warning',
    level: '关注'
  },
  {
    id: 2,
    title: '人工成本趋势',
    description: '人工成本保持稳定，自动化改造效果良好，建议继续推进智能制造',
    type: 'success',
    level: '良好'
  },
  {
    id: 3,
    title: '制造费用趋势',
    description: '制造费用小幅上升，主要由设备更新和维护投入增加，属于正常范围',
    type: 'info',
    level: '正常'
  }
])

const formatMoney = (amount) => {
  if (!amount) return '¥0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const getCostSummary = (param) => {
  const { columns, data } = param
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (['directMaterial', 'directLabor', 'manufacturing', 'totalCost'].includes(column.property)) {
      const values = data.map(item => Number(item[column.property]))
      sums[index] = formatMoney(values.reduce((prev, curr) => prev + curr, 0))
    } else {
      sums[index] = ''
    }
  })
  return sums
}

const getImpactType = (impact) => {
  const map = {
    '高': 'danger',
    '中': 'warning',
    '低': 'success'
  }
  return map[impact] || 'info'
}

const getTargetStatus = (achievement) => {
  if (achievement >= 100) return 'success'
  if (achievement >= 90) return 'warning'
  return 'danger'
}

const getTargetStatusText = (achievement) => {
  if (achievement >= 100) return '达标'
  if (achievement >= 90) return '接近'
  return '未达标'
}

const getTargetProgressStatus = (achievement) => {
  if (achievement >= 100) return 'success'
  if (achievement >= 90) return 'warning'
  return 'exception'
}

const getMeasureStatusType = (status) => {
  const map = {
    'planning': 'info',
    'implementing': 'warning',
    'completed': 'success',
    'suspended': 'danger'
  }
  return map[status] || 'info'
}

const getMeasureStatusText = (status) => {
  const map = {
    'planning': '规划中',
    'implementing': '实施中',
    'completed': '已完成',
    'suspended': '已暂停'
  }
  return map[status] || status
}

const handleViewCostDetail = (row) => {
  ElMessage.info(`查看成本详情：${row.itemName}`)
}

const handleAnalyzeCost = (row) => {
  ElMessage.info(`成本分析：${row.itemName}`)
}

const handleAddMeasure = () => {
  ElMessage.info('新增控制措施功能开发中')
}

const handleViewMeasure = (row) => {
  ElMessage.info(`查看措施：${row.measureName}`)
}

const handleEditMeasure = (row) => {
  ElMessage.info(`编辑措施：${row.measureName}`)
}

const handleEvaluateMeasure = (row) => {
  ElMessage.info(`评估措施：${row.measureName}`)
}

const updateStructureChart = () => {
  initStructureChart()
}

const updateComparisonChart = () => {
  initComparisonChart()
}

const updateTrendChart = () => {
  initTrendChart()
}

const initStructureChart = () => {
  if (structureChart.value) {
    structureChartInstance = echarts.init(structureChart.value)
    
    const dataMap = {
      product: {
        categories: ['产品A', '产品B', '产品C'],
        data: [1800, 1080, 560]
      },
      category: {
        categories: ['直接材料', '直接人工', '制造费用'],
        data: [1920, 800, 480]
      },
      department: {
        categories: ['生产部', '采购部', '质控部'],
        data: [2200, 800, 400]
      }
    }
    
    const currentData = dataMap[structureType.value]
    
    const option = {
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: currentData.categories
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (value) => (value / 10000) + '万'
        }
      },
      series: [
        {
          name: '成本金额',
          type: 'bar',
          data: currentData.data,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#409eff' },
              { offset: 1, color: '#a0cfff' }
            ])
          }
        }
      ]
    }
    
    structureChartInstance.setOption(option)
  }
}

const initRatioChart = () => {
  if (ratioChart.value) {
    ratioChartInstance = echarts.init(ratioChart.value)
    const option = {
      tooltip: {
        trigger: 'item'
      },
      series: [
        {
          name: '成本占比',
          type: 'pie',
          radius: ['40%', '70%'],
          data: [
            { value: 1920, name: '直接材料', itemStyle: { color: '#409eff' } },
            { value: 800, name: '直接人工', itemStyle: { color: '#67c23a' } },
            { value: 480, name: '制造费用', itemStyle: { color: '#e6a23c' } }
          ],
          label: {
            formatter: '{b}\n{c}万元\n({d}%)'
          }
        }
      ]
    }
    ratioChartInstance.setOption(option)
  }
}

const initComparisonChart = () => {
  if (comparisonChart.value) {
    comparisonChartInstance = echarts.init(comparisonChart.value)
    const option = {
      title: {
        text: '成本同期对比分析',
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
        data: ['直接材料', '直接人工', '制造费用', '其他费用', '质量成本']
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
          data: [192, 80, 48, 35, 25],
          itemStyle: { color: '#409eff' }
        },
        {
          name: '上期',
          type: 'bar',
          data: [177, 82, 45, 30, 20],
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
    
    const dataMap = {
      month: {
        xData: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月'],
        series: [
          { name: '直接材料', data: [160, 165, 170, 175, 180, 185, 190, 192] },
          { name: '直接人工', data: [82, 81, 80, 79, 80, 81, 80, 80] },
          { name: '制造费用', data: [42, 43, 44, 45, 46, 47, 48, 48] }
        ]
      },
      quarter: {
        xData: ['Q1', 'Q2', 'Q3', 'Q4'],
        series: [
          { name: '直接材料', data: [495, 540, 577, 576] },
          { name: '直接人工', data: [243, 239, 241, 240] },
          { name: '制造费用', data: [129, 138, 142, 144] }
        ]
      },
      year: {
        xData: ['2021', '2022', '2023', '2024', '2025'],
        series: [
          { name: '直接材料', data: [1680, 1750, 1820, 1890, 2188] },
          { name: '直接人工', data: [720, 740, 760, 780, 963] },
          { name: '制造费用', data: [480, 500, 520, 540, 553] }
        ]
      }
    }
    
    const currentData = dataMap[trendPeriod.value]
    
    const option = {
      title: {
        text: '成本趋势分析',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['直接材料', '直接人工', '制造费用'],
        top: 30
      },
      xAxis: {
        type: 'category',
        data: currentData.xData
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (value) => (value / 100) + '万'
        }
      },
      series: currentData.series.map((item, index) => ({
        name: item.name,
        type: 'line',
        data: item.data,
        smooth: true,
        itemStyle: {
          color: ['#409eff', '#67c23a', '#e6a23c'][index]
        }
      }))
    }
    
    trendChartInstance.setOption(option)
  }
}

onMounted(() => {
  nextTick(() => {
    initStructureChart()
    initRatioChart()
    initComparisonChart()
    initTrendChart()
    
    window.addEventListener('resize', () => {
      structureChartInstance?.resize()
      ratioChartInstance?.resize()
      comparisonChartInstance?.resize()
      trendChartInstance?.resize()
    })
  })
})
</script>

<style scoped lang="scss">
.cost-overview {
  margin-bottom: 20px;
  
  .cost-card {
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
    }
    
    &.increase {
      border-left: 4px solid #f56c6c;
    }
    
    &.decrease {
      border-left: 4px solid #67c23a;
    }
    
    &.stable {
      border-left: 4px solid #909399;
    }
    
    .cost-content {
      display: flex;
      align-items: center;
      
      .cost-icon {
        margin-right: 16px;
      }
      
      .cost-info {
        flex: 1;
        
        h4 {
          margin: 0 0 8px 0;
          color: #606266;
          font-size: 14px;
          font-weight: normal;
        }
        
        .cost-value {
          margin: 0 0 8px 0;
          color: #303133;
          font-size: 24px;
          font-weight: 600;
        }
        
        .cost-change {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
        }
      }
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-controls {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.cost-detail-card {
  margin-top: 20px;
  
  .total-cost {
    color: #409eff;
    font-weight: 600;
  }
}

.comparison-table {
  margin-top: 20px;
  
  .cost-increase {
    color: #f56c6c;
    font-weight: 600;
  }
  
  .cost-decrease {
    color: #67c23a;
    font-weight: 600;
  }
  
  .significant-change {
    color: #f56c6c;
    font-weight: 600;
  }
  
  .normal-change {
    color: #606266;
  }
}

.warning-list {
  max-height: 300px;
  overflow-y: auto;
}

.control-targets {
  .target-item {
    margin-bottom: 20px;
    padding: 16px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    
    .target-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      
      .target-name {
        font-weight: 600;
        color: #303133;
      }
    }
    
    .target-progress {
      margin-bottom: 12px;
    }
    
    .target-info {
      display: flex;
      justify-content: space-between;
      font-size: 12px;
      color: #909399;
    }
  }
}

.control-measures {
  margin-top: 20px;
  
  .cost-saving {
    color: #67c23a;
    font-weight: 600;
  }
}

.trend-analysis {
  margin-top: 20px;
  
  .conclusion-item {
    padding: 16px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    background: #fafafa;
    
    h4 {
      margin: 0 0 12px 0;
      color: #303133;
      font-size: 16px;
    }
    
    p {
      margin: 0 0 12px 0;
      color: #606266;
      line-height: 1.6;
    }
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
  .cost-overview {
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