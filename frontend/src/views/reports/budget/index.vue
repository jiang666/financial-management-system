<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">预算管理</h2>
      <p class="description">制定、执行和监控企业预算，实现精细化财务管理</p>
    </div>
    
    <!-- 预算概览 -->
    <el-row :gutter="16" class="budget-overview">
      <el-col :span="6" v-for="item in budgetSummary" :key="item.id">
        <el-card class="summary-card" :class="item.status">
          <div class="summary-content">
            <div class="summary-icon">
              <el-icon :size="32" :color="item.color">
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div class="summary-info">
              <h4>{{ item.title }}</h4>
              <p class="summary-value">{{ item.value }}</p>
              <div class="summary-progress">
                <el-progress 
                  :percentage="item.progress" 
                  :status="item.progressStatus"
                  :stroke-width="8"
                  :show-text="false"
                />
                <span class="progress-text">{{ item.progressText }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-tabs v-model="activeTab">
      <!-- 预算编制 -->
      <el-tab-pane label="预算编制" name="planning">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>预算编制</span>
              <div class="header-actions">
                <el-button type="primary" icon="Plus" @click="handleCreateBudget">新建预算</el-button>
                <el-button type="success" icon="Upload" @click="handleImportBudget">导入预算</el-button>
              </div>
            </div>
          </template>
          
          <div class="search-form">
            <el-form :model="planningSearchForm" inline>
              <el-form-item label="预算年度">
                <el-select v-model="planningSearchForm.year" placeholder="请选择年度" style="width: 120px">
                  <el-option label="2025年" value="2025" />
                  <el-option label="2024年" value="2024" />
                  <el-option label="2023年" value="2023" />
                </el-select>
              </el-form-item>
              <el-form-item label="预算类型">
                <el-select v-model="planningSearchForm.type" placeholder="请选择类型" style="width: 120px">
                  <el-option label="收入预算" value="revenue" />
                  <el-option label="成本预算" value="cost" />
                  <el-option label="费用预算" value="expense" />
                  <el-option label="资本预算" value="capital" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="planningSearchForm.status" placeholder="请选择状态" style="width: 120px">
                  <el-option label="草稿" value="draft" />
                  <el-option label="待审批" value="pending" />
                  <el-option label="已批准" value="approved" />
                  <el-option label="执行中" value="executing" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="Search" @click="handlePlanningSearch">搜索</el-button>
                <el-button icon="Refresh" @click="handlePlanningReset">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <el-table :data="budgetPlanData" border style="width: 100%" stripe>
            <el-table-column prop="budgetName" label="预算名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="year" label="预算年度" width="100" />
            <el-table-column prop="type" label="预算类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getBudgetTypeTag(row.type)" size="small">
                  {{ getBudgetTypeText(row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalAmount" label="预算总额" width="140" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.totalAmount) }}
              </template>
            </el-table-column>
            <el-table-column prop="usedAmount" label="已执行" width="140" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.usedAmount) }}
              </template>
            </el-table-column>
            <el-table-column prop="executionRate" label="执行率" width="120" align="right">
              <template #default="{ row }">
                <el-progress :percentage="row.executionRate" :status="getExecutionStatus(row.executionRate)" :stroke-width="8" />
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getBudgetStatusTag(row.status)" size="small">
                  {{ getBudgetStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="creator" label="创建人" width="100" />
            <el-table-column prop="createTime" label="创建时间" width="110" />
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button link type="primary" size="small" @click="handleViewBudget(row)">查看</el-button>
                  <el-button link type="success" size="small" @click="handleEditBudget(row)" v-if="row.status === 'draft'">编辑</el-button>
                  <el-button link type="warning" size="small" @click="handleSubmitBudget(row)" v-if="row.status === 'draft'">提交</el-button>
                  <el-button link type="info" size="small" @click="handleCopyBudget(row)">复制</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
      
      <!-- 预算执行 -->
      <el-tab-pane label="预算执行" name="execution">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>预算执行监控</span>
              <div class="header-actions">
                <el-select v-model="executionPeriod" placeholder="选择期间" style="width: 200px; margin-right: 10px;">
                  <el-option label="2025年8月" value="2025-08" />
                  <el-option label="2025年第3季度" value="2025-Q3" />
                  <el-option label="2025年度" value="2025" />
                </el-select>
                <el-button type="primary" icon="Refresh" @click="refreshExecutionData">刷新</el-button>
              </div>
            </div>
          </template>
          
          <!-- 执行情况图表 -->
          <el-row :gutter="20" class="execution-charts">
            <el-col :span="12">
              <div class="chart-container">
                <h4>预算执行进度</h4>
                <div ref="executionChart" style="height: 300px"></div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="chart-container">
                <h4>预算差异分析</h4>
                <div ref="varianceChart" style="height: 300px"></div>
              </div>
            </el-col>
          </el-row>
          
          <!-- 执行明细表 -->
          <div class="execution-details">
            <h4>预算执行明细</h4>
            <el-table :data="executionData" border style="width: 100%" stripe show-summary :summary-method="getExecutionSummary">
              <el-table-column prop="category" label="预算科目" min-width="150" />
              <el-table-column prop="budgetAmount" label="预算金额" width="140" align="right">
                <template #default="{ row }">
                  {{ formatMoney(row.budgetAmount) }}
                </template>
              </el-table-column>
              <el-table-column prop="actualAmount" label="实际金额" width="140" align="right">
                <template #default="{ row }">
                  {{ formatMoney(row.actualAmount) }}
                </template>
              </el-table-column>
              <el-table-column prop="variance" label="差异金额" width="140" align="right">
                <template #default="{ row }">
                  <span :class="row.variance >= 0 ? 'positive-variance' : 'negative-variance'">
                    {{ row.variance >= 0 ? '+' : '' }}{{ formatMoney(row.variance) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="varianceRate" label="差异率" width="120" align="right">
                <template #default="{ row }">
                  <span :class="Math.abs(row.varianceRate) > 10 ? 'high-variance' : 'normal-variance'">
                    {{ row.varianceRate >= 0 ? '+' : '' }}{{ row.varianceRate }}%
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="executionRate" label="执行率" width="140" align="right">
                <template #default="{ row }">
                  <el-progress :percentage="row.executionRate" :status="getExecutionStatus(row.executionRate)" :stroke-width="6" />
                </template>
              </el-table-column>
              <el-table-column prop="trend" label="趋势" width="120">
                <template #default="{ row }">
                  <el-icon :color="getTrendColor(row.trend)">
                    <component :is="getTrendIcon(row.trend)" />
                  </el-icon>
                  <span :style="{ color: getTrendColor(row.trend) }">{{ getTrendText(row.trend) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="160" fixed="right">
                <template #default="{ row }">
                  <div class="table-actions">
                    <el-button link type="primary" size="small" @click="handleViewDetails(row)">详情</el-button>
                    <el-button link type="warning" size="small" @click="handleAnalyzeVariance(row)">差异分析</el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-tab-pane>
      
      <!-- 预算调整 -->
      <el-tab-pane label="预算调整" name="adjustment">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>预算调整申请</span>
              <div class="header-actions">
                <el-button type="primary" icon="Plus" @click="handleCreateAdjustment">新建调整</el-button>
              </div>
            </div>
          </template>
          
          <el-table :data="adjustmentData" border style="width: 100%" stripe>
            <el-table-column prop="adjustmentNo" label="调整单号" width="140" />
            <el-table-column prop="budgetItem" label="预算科目" min-width="150" />
            <el-table-column prop="originalAmount" label="原预算金额" width="140" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.originalAmount) }}
              </template>
            </el-table-column>
            <el-table-column prop="adjustmentAmount" label="调整金额" width="140" align="right">
              <template #default="{ row }">
                <span :class="row.adjustmentAmount >= 0 ? 'positive-adjustment' : 'negative-adjustment'">
                  {{ row.adjustmentAmount >= 0 ? '+' : '' }}{{ formatMoney(row.adjustmentAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="newAmount" label="调整后金额" width="140" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.newAmount) }}
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="调整原因" min-width="200" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getAdjustmentStatusTag(row.status)" size="small">
                  {{ getAdjustmentStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applicant" label="申请人" width="100" />
            <el-table-column prop="applyTime" label="申请时间" width="110" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button link type="primary" size="small" @click="handleViewAdjustment(row)">查看</el-button>
                  <el-button link type="success" size="small" @click="handleApproveAdjustment(row)" v-if="row.status === 'pending'">审批</el-button>
                  <el-button link type="warning" size="small" @click="handleEditAdjustment(row)" v-if="row.status === 'draft'">编辑</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
      
      <!-- 预算分析 -->
      <el-tab-pane label="预算分析" name="analysis">
        <el-card>
          <template #header>
            <span>预算分析报告</span>
          </template>
          
          <!-- 分析选项 -->
          <div class="analysis-options">
            <el-form :model="analysisForm" inline>
              <el-form-item label="分析维度">
                <el-select v-model="analysisForm.dimension" @change="updateAnalysisChart">
                  <el-option label="按部门" value="department" />
                  <el-option label="按科目" value="account" />
                  <el-option label="按月份" value="month" />
                  <el-option label="按季度" value="quarter" />
                </el-select>
              </el-form-item>
              <el-form-item label="分析类型">
                <el-select v-model="analysisForm.type" @change="updateAnalysisChart">
                  <el-option label="执行率分析" value="execution" />
                  <el-option label="差异分析" value="variance" />
                  <el-option label="趋势分析" value="trend" />
                </el-select>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 分析图表 -->
          <div ref="analysisChart" style="height: 400px; margin: 20px 0"></div>
          
          <!-- 分析结论 -->
          <div class="analysis-conclusion">
            <h4>分析结论</h4>
            <el-alert
              v-for="conclusion in analysisConclusions"
              :key="conclusion.id"
              :title="conclusion.title"
              :description="conclusion.description"
              :type="conclusion.type"
              :closable="false"
              style="margin-bottom: 10px"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 新建预算对话框 -->
    <el-dialog v-model="budgetDialog" title="新建预算" width="800px">
      <el-form :model="budgetForm" :rules="budgetRules" ref="budgetFormRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预算名称" prop="budgetName">
              <el-input v-model="budgetForm.budgetName" placeholder="请输入预算名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预算年度" prop="year">
              <el-select v-model="budgetForm.year" placeholder="请选择年度" style="width: 100%">
                <el-option label="2025年" value="2025" />
                <el-option label="2026年" value="2026" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预算类型" prop="type">
              <el-select v-model="budgetForm.type" placeholder="请选择类型" style="width: 100%">
                <el-option label="收入预算" value="revenue" />
                <el-option label="成本预算" value="cost" />
                <el-option label="费用预算" value="expense" />
                <el-option label="资本预算" value="capital" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预算总额" prop="totalAmount">
              <el-input-number
                v-model="budgetForm.totalAmount"
                :min="0"
                :precision="2"
                style="width: 100%"
                placeholder="请输入预算总额"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="预算说明">
          <el-input v-model="budgetForm.description" type="textarea" :rows="3" placeholder="请输入预算说明" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="budgetDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitBudgetForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { TrendCharts, Wallet, PieChart, Trophy, ArrowUp, ArrowDown, Minus } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const activeTab = ref('planning')
const executionPeriod = ref('2025-08')
const budgetDialog = ref(false)
const budgetFormRef = ref()

const executionChart = ref(null)
const varianceChart = ref(null)
const analysisChart = ref(null)
let executionChartInstance = null
let varianceChartInstance = null
let analysisChartInstance = null

// 预算概览数据
const budgetSummary = ref([
  {
    id: 1,
    title: '总预算',
    value: '¥2,000万',
    progress: 75,
    progressStatus: 'success',
    progressText: '75% 执行',
    status: 'normal',
    icon: Wallet,
    color: '#409eff'
  },
  {
    id: 2,
    title: '收入预算',
    value: '¥5,000万',
    progress: 82,
    progressStatus: 'success',
    progressText: '82% 完成',
    status: 'good',
    icon: TrendCharts,
    color: '#67c23a'
  },
  {
    id: 3,
    title: '成本预算',
    value: '¥3,000万',
    progress: 78,
    progressStatus: 'warning',
    progressText: '78% 执行',
    status: 'warning',
    icon: PieChart,
    color: '#e6a23c'
  },
  {
    id: 4,
    title: '费用预算',
    value: '¥800万',
    progress: 95,
    progressStatus: 'exception',
    progressText: '95% 执行',
    status: 'danger',
    icon: Trophy,
    color: '#f56c6c'
  }
])

// 搜索表单
const planningSearchForm = ref({
  year: '2025',
  type: '',
  status: ''
})

// 分析表单
const analysisForm = ref({
  dimension: 'department',
  type: 'execution'
})

// 预算表单
const budgetForm = ref({
  budgetName: '',
  year: '2025',
  type: '',
  totalAmount: 0,
  description: ''
})

const budgetRules = {
  budgetName: [{ required: true, message: '请输入预算名称', trigger: 'blur' }],
  year: [{ required: true, message: '请选择预算年度', trigger: 'change' }],
  type: [{ required: true, message: '请选择预算类型', trigger: 'change' }],
  totalAmount: [{ required: true, message: '请输入预算总额', trigger: 'blur' }]
}

// 预算编制数据
const budgetPlanData = ref([
  {
    id: 1,
    budgetName: '2025年销售收入预算',
    year: '2025',
    type: 'revenue',
    totalAmount: 50000000,
    usedAmount: 41000000,
    executionRate: 82,
    status: 'executing',
    creator: '张经理',
    createTime: '2025-01-15'
  },
  {
    id: 2,
    budgetName: '2025年生产成本预算',
    year: '2025',
    type: 'cost',
    totalAmount: 30000000,
    usedAmount: 23400000,
    executionRate: 78,
    status: 'executing',
    creator: '李主管',
    createTime: '2025-01-20'
  },
  {
    id: 3,
    budgetName: '2025年管理费用预算',
    year: '2025',
    type: 'expense',
    totalAmount: 8000000,
    usedAmount: 7600000,
    executionRate: 95,
    status: 'executing',
    creator: '王总监',
    createTime: '2025-01-10'
  },
  {
    id: 4,
    budgetName: '2025年设备采购预算',
    year: '2025',
    type: 'capital',
    totalAmount: 5000000,
    usedAmount: 2500000,
    executionRate: 50,
    status: 'approved',
    creator: '刘工程师',
    createTime: '2025-02-01'
  }
])

// 执行数据
const executionData = ref([
  {
    category: '销售收入',
    budgetAmount: 5000000,
    actualAmount: 4100000,
    variance: -900000,
    varianceRate: -18,
    executionRate: 82,
    trend: 'up'
  },
  {
    category: '生产成本',
    budgetAmount: 3000000,
    actualAmount: 2340000,
    variance: -660000,
    varianceRate: -22,
    executionRate: 78,
    trend: 'down'
  },
  {
    category: '管理费用',
    budgetAmount: 800000,
    actualAmount: 760000,
    variance: -40000,
    varianceRate: -5,
    executionRate: 95,
    trend: 'stable'
  },
  {
    category: '销售费用',
    budgetAmount: 600000,
    actualAmount: 570000,
    variance: -30000,
    varianceRate: -5,
    executionRate: 95,
    trend: 'up'
  }
])

// 调整数据
const adjustmentData = ref([
  {
    id: 1,
    adjustmentNo: 'ADJ20250001',
    budgetItem: '销售费用',
    originalAmount: 600000,
    adjustmentAmount: 100000,
    newAmount: 700000,
    reason: '市场推广活动增加',
    status: 'pending',
    applicant: '张经理',
    applyTime: '2025-08-15'
  },
  {
    id: 2,
    adjustmentNo: 'ADJ20250002',
    budgetItem: '研发费用',
    originalAmount: 800000,
    adjustmentAmount: -50000,
    newAmount: 750000,
    reason: '项目进度调整',
    status: 'approved',
    applicant: '李工程师',
    applyTime: '2025-08-10'
  }
])

// 分析结论
const analysisConclusions = ref([
  {
    id: 1,
    title: '执行率分析',
    description: '整体预算执行率为78%，符合预期进度。销售费用预算使用率偏高，需要关注。',
    type: 'success'
  },
  {
    id: 2,
    title: '差异分析',
    description: '收入预算完成率82%，低于年度目标。建议加强市场推广，提升销售业绩。',
    type: 'warning'
  },
  {
    id: 3,
    title: '趋势分析',
    description: '成本控制良好，费用支出基本在控制范围内。建议继续保持现有管控水平。',
    type: 'info'
  }
])

const formatMoney = (amount) => {
  if (!amount) return '¥0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const getBudgetTypeTag = (type) => {
  const map = {
    'revenue': 'success',
    'cost': 'warning',
    'expense': 'danger',
    'capital': 'info'
  }
  return map[type] || ''
}

const getBudgetTypeText = (type) => {
  const map = {
    'revenue': '收入预算',
    'cost': '成本预算',
    'expense': '费用预算',
    'capital': '资本预算'
  }
  return map[type] || type
}

const getBudgetStatusTag = (status) => {
  const map = {
    'draft': 'info',
    'pending': 'warning',
    'approved': 'success',
    'executing': 'primary'
  }
  return map[status] || ''
}

const getBudgetStatusText = (status) => {
  const map = {
    'draft': '草稿',
    'pending': '待审批',
    'approved': '已批准',
    'executing': '执行中'
  }
  return map[status] || status
}

const getExecutionStatus = (rate) => {
  if (rate >= 90) return 'exception'
  if (rate >= 70) return 'success'
  return 'warning'
}

const getAdjustmentStatusTag = (status) => {
  const map = {
    'draft': 'info',
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'danger'
  }
  return map[status] || ''
}

const getAdjustmentStatusText = (status) => {
  const map = {
    'draft': '草稿',
    'pending': '待审批',
    'approved': '已批准',
    'rejected': '已拒绝'
  }
  return map[status] || status
}

const getTrendIcon = (trend) => {
  const map = {
    'up': ArrowUp,
    'down': ArrowDown,
    'stable': Minus
  }
  return map[trend] || Minus
}

const getTrendColor = (trend) => {
  const map = {
    'up': '#67c23a',
    'down': '#f56c6c',
    'stable': '#909399'
  }
  return map[trend] || '#909399'
}

const getTrendText = (trend) => {
  const map = {
    'up': '上升',
    'down': '下降',
    'stable': '平稳'
  }
  return map[trend] || '平稳'
}

const getExecutionSummary = (param) => {
  const { columns, data } = param
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (['budgetAmount', 'actualAmount', 'variance'].includes(column.property)) {
      const values = data.map(item => Number(item[column.property]))
      sums[index] = formatMoney(values.reduce((prev, curr) => prev + curr, 0))
    } else {
      sums[index] = ''
    }
  })
  return sums
}

const handleCreateBudget = () => {
  budgetForm.value = {
    budgetName: '',
    year: '2025',
    type: '',
    totalAmount: 0,
    description: ''
  }
  budgetDialog.value = true
}

const handleImportBudget = () => {
  ElMessage.info('导入预算功能开发中')
}

const handlePlanningSearch = () => {
  ElMessage.success('搜索完成')
}

const handlePlanningReset = () => {
  planningSearchForm.value = {
    year: '2025',
    type: '',
    status: ''
  }
}

const handleViewBudget = (row) => {
  ElMessage.info(`查看预算：${row.budgetName}`)
}

const handleEditBudget = (row) => {
  ElMessage.info(`编辑预算：${row.budgetName}`)
}

const handleSubmitBudget = (row) => {
  ElMessage.success(`预算已提交审批：${row.budgetName}`)
}

const handleCopyBudget = (row) => {
  ElMessage.info(`复制预算：${row.budgetName}`)
}

const refreshExecutionData = () => {
  ElMessage.success('执行数据已刷新')
}

const handleViewDetails = (row) => {
  ElMessage.info(`查看详情：${row.category}`)
}

const handleAnalyzeVariance = (row) => {
  ElMessage.info(`差异分析：${row.category}`)
}

const handleCreateAdjustment = () => {
  ElMessage.info('新建调整功能开发中')
}

const handleViewAdjustment = (row) => {
  ElMessage.info(`查看调整：${row.adjustmentNo}`)
}

const handleApproveAdjustment = (row) => {
  ElMessage.success(`调整已批准：${row.adjustmentNo}`)
}

const handleEditAdjustment = (row) => {
  ElMessage.info(`编辑调整：${row.adjustmentNo}`)
}

const handleSubmitBudgetForm = async () => {
  const valid = await budgetFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  budgetDialog.value = false
  ElMessage.success('预算创建成功')
}

const updateAnalysisChart = () => {
  initAnalysisChart()
}

const initExecutionChart = () => {
  if (executionChart.value) {
    executionChartInstance = echarts.init(executionChart.value)
    const option = {
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['预算金额', '实际金额']
      },
      xAxis: {
        type: 'category',
        data: ['销售收入', '生产成本', '管理费用', '销售费用']
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (value) => (value / 10000) + '万'
        }
      },
      series: [
        {
          name: '预算金额',
          type: 'bar',
          data: [500, 300, 80, 60],
          itemStyle: { color: '#409eff' }
        },
        {
          name: '实际金额',
          type: 'bar',
          data: [410, 234, 76, 57],
          itemStyle: { color: '#67c23a' }
        }
      ]
    }
    executionChartInstance.setOption(option)
  }
}

const initVarianceChart = () => {
  if (varianceChart.value) {
    varianceChartInstance = echarts.init(varianceChart.value)
    const option = {
      tooltip: {
        trigger: 'item'
      },
      series: [
        {
          name: '差异分析',
          type: 'pie',
          radius: ['40%', '70%'],
          data: [
            { value: 90, name: '超支项目', itemStyle: { color: '#f56c6c' } },
            { value: 66, name: '节约项目', itemStyle: { color: '#67c23a' } },
            { value: 4, name: '持平项目', itemStyle: { color: '#909399' } }
          ],
          label: {
            formatter: '{b}: {c}万元\n({d}%)'
          }
        }
      ]
    }
    varianceChartInstance.setOption(option)
  }
}

const initAnalysisChart = () => {
  if (analysisChart.value) {
    analysisChartInstance = echarts.init(analysisChart.value)
    const option = {
      title: {
        text: '预算执行分析',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月']
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: '{value}%'
        }
      },
      series: [
        {
          name: '执行率',
          type: 'line',
          data: [65, 70, 75, 78, 80, 82, 85, 78],
          smooth: true,
          itemStyle: { color: '#409eff' },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: '#409eff40' },
                { offset: 1, color: '#409eff10' }
              ]
            }
          }
        }
      ]
    }
    analysisChartInstance.setOption(option)
  }
}

onMounted(() => {
  nextTick(() => {
    initExecutionChart()
    initVarianceChart()
    initAnalysisChart()
    
    window.addEventListener('resize', () => {
      executionChartInstance?.resize()
      varianceChartInstance?.resize()
      analysisChartInstance?.resize()
    })
  })
})
</script>

<style scoped lang="scss">
.budget-overview {
  margin-bottom: 20px;
  
  .summary-card {
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
    }
    
    &.good {
      border-left: 4px solid #67c23a;
    }
    
    &.warning {
      border-left: 4px solid #e6a23c;
    }
    
    &.danger {
      border-left: 4px solid #f56c6c;
    }
    
    .summary-content {
      display: flex;
      align-items: center;
      
      .summary-icon {
        margin-right: 16px;
      }
      
      .summary-info {
        flex: 1;
        
        h4 {
          margin: 0 0 8px 0;
          color: #606266;
          font-size: 14px;
          font-weight: normal;
        }
        
        .summary-value {
          margin: 0 0 12px 0;
          color: #303133;
          font-size: 24px;
          font-weight: 600;
        }
        
        .summary-progress {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .progress-text {
            font-size: 12px;
            color: #909399;
            white-space: nowrap;
          }
        }
      }
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-actions {
    display: flex;
    gap: 8px;
  }
}

.execution-charts {
  margin-bottom: 30px;
  
  .chart-container {
    h4 {
      margin: 0 0 16px 0;
      text-align: center;
      color: #303133;
    }
  }
}

.execution-details {
  h4 {
    margin: 0 0 16px 0;
    color: #303133;
  }
  
  .positive-variance {
    color: #67c23a;
    font-weight: 600;
  }
  
  .negative-variance {
    color: #f56c6c;
    font-weight: 600;
  }
  
  .high-variance {
    color: #f56c6c;
    font-weight: 600;
  }
  
  .normal-variance {
    color: #606266;
  }
}

.positive-adjustment {
  color: #67c23a;
  font-weight: 600;
}

.negative-adjustment {
  color: #f56c6c;
  font-weight: 600;
}

.analysis-options {
  margin-bottom: 20px;
}

.analysis-conclusion {
  margin-top: 20px;
  
  h4 {
    margin: 0 0 16px 0;
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
  .budget-overview {
    .el-col {
      margin-bottom: 16px;
    }
  }
  
  .execution-charts {
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