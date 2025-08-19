<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">绩效考核</h2>
      <p class="description">全面的财务绩效考核体系，促进企业持续发展</p>
    </div>
    
    <!-- 绩效概览 -->
    <el-row :gutter="16" class="performance-overview">
      <el-col :span="6" v-for="metric in performanceMetrics" :key="metric.id">
        <el-card class="metric-card" :class="metric.level">
          <div class="metric-content">
            <div class="metric-icon">
              <el-icon :size="32" :color="metric.color">
                <component :is="metric.icon" />
              </el-icon>
            </div>
            <div class="metric-info">
              <h4>{{ metric.title }}</h4>
              <p class="metric-value">{{ metric.value }}</p>
              <div class="metric-target">
                <span>目标：{{ metric.target }}</span>
                <el-tag :type="metric.achievementLevel" size="small">
                  {{ getAchievementText(metric.achievement) }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-tabs v-model="activeTab">
      <!-- 绩效仪表板 -->
      <el-tab-pane label="绩效仪表板" name="dashboard">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>关键绩效指标(KPI)</span>
              </template>
              <div ref="kpiChart" style="height: 350px"></div>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>绩效评分分布</span>
              </template>
              <div ref="scoreChart" style="height: 350px"></div>
            </el-card>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" class="dashboard-section">
          <el-col :span="24">
            <el-card>
              <template #header>
                <span>绩效趋势分析</span>
              </template>
              <div ref="trendChart" style="height: 400px"></div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
      
      <!-- 指标管理 -->
      <el-tab-pane label="指标管理" name="indicators">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>绩效指标管理</span>
              <div class="header-actions">
                <el-button type="primary" icon="Plus" @click="handleAddIndicator">新增指标</el-button>
                <el-button type="success" icon="Upload" @click="handleImportIndicators">批量导入</el-button>
              </div>
            </div>
          </template>
          
          <div class="search-form">
            <el-form :model="indicatorSearchForm" inline>
              <el-form-item label="指标分类">
                <el-select v-model="indicatorSearchForm.category" placeholder="请选择分类" style="width: 120px">
                  <el-option label="财务指标" value="financial" />
                  <el-option label="运营指标" value="operational" />
                  <el-option label="客户指标" value="customer" />
                  <el-option label="学习指标" value="learning" />
                </el-select>
              </el-form-item>
              <el-form-item label="考核周期">
                <el-select v-model="indicatorSearchForm.period" placeholder="请选择周期" style="width: 120px">
                  <el-option label="月度" value="monthly" />
                  <el-option label="季度" value="quarterly" />
                  <el-option label="年度" value="yearly" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="indicatorSearchForm.status" placeholder="请选择状态" style="width: 120px">
                  <el-option label="启用" value="active" />
                  <el-option label="停用" value="inactive" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="Search" @click="handleIndicatorSearch">搜索</el-button>
                <el-button icon="Refresh" @click="handleIndicatorReset">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <el-table :data="indicatorData" border style="width: 100%" stripe>
            <el-table-column prop="indicatorName" label="指标名称" min-width="150" />
            <el-table-column prop="category" label="分类" width="120">
              <template #default="{ row }">
                <el-tag :type="getCategoryType(row.category)" size="small">
                  {{ getCategoryText(row.category) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="unit" label="单位" width="80" />
            <el-table-column prop="weight" label="权重" width="80" align="right">
              <template #default="{ row }">
                {{ row.weight }}%
              </template>
            </el-table-column>
            <el-table-column prop="target" label="目标值" width="120" align="right" />
            <el-table-column prop="actual" label="实际值" width="120" align="right">
              <template #default="{ row }">
                <span :class="getValueClass(row.actual, row.target)">{{ row.actual }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="achievement" label="达成率" width="120" align="right">
              <template #default="{ row }">
                <el-progress :percentage="row.achievement" :status="getAchievementStatus(row.achievement)" :stroke-width="8" />
              </template>
            </el-table-column>
            <el-table-column prop="period" label="考核周期" width="100">
              <template #default="{ row }">
                {{ getPeriodText(row.period) }}
              </template>
            </el-table-column>
            <el-table-column prop="responsible" label="责任人" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
                  {{ row.status === 'active' ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button link type="primary" size="small" @click="handleViewIndicator(row)">查看</el-button>
                  <el-button link type="success" size="small" @click="handleEditIndicator(row)">编辑</el-button>
                  <el-button link type="warning" size="small" @click="handleSetTarget(row)">设定目标</el-button>
                  <el-button link type="info" size="small" @click="handleViewHistory(row)">历史</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
      
      <!-- 考核评分 -->
      <el-tab-pane label="考核评分" name="evaluation">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card>
              <template #header>
                <span>评分设置</span>
              </template>
              
              <el-form :model="evaluationForm" label-width="100px">
                <el-form-item label="考核期间">
                  <el-date-picker
                    v-model="evaluationForm.period"
                    type="month"
                    placeholder="选择考核期间"
                    format="YYYY-MM"
                    value-format="YYYY-MM"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="考核对象">
                  <el-select v-model="evaluationForm.target" placeholder="请选择" style="width: 100%">
                    <el-option label="公司整体" value="company" />
                    <el-option label="财务部" value="finance" />
                    <el-option label="销售部" value="sales" />
                    <el-option label="生产部" value="production" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="generateEvaluation">生成评分</el-button>
                  <el-button @click="resetEvaluation">重置</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>
          
          <el-col :span="16">
            <el-card>
              <template #header>
                <span>评分结果</span>
              </template>
              
              <div v-if="evaluationResult" class="evaluation-result">
                <div class="score-header">
                  <h3>综合评分：<span class="total-score">{{ evaluationResult.totalScore }}</span>分</h3>
                  <el-tag :type="getScoreLevel(evaluationResult.totalScore)" size="large">
                    {{ getScoreText(evaluationResult.totalScore) }}
                  </el-tag>
                </div>
                
                <el-table :data="evaluationResult.details" border style="width: 100%">
                  <el-table-column prop="category" label="指标类别" width="120" />
                  <el-table-column prop="weight" label="权重" width="80" align="right">
                    <template #default="{ row }">
                      {{ row.weight }}%
                    </template>
                  </el-table-column>
                  <el-table-column prop="score" label="得分" width="80" align="right">
                    <template #default="{ row }">
                      <span :class="getScoreClass(row.score)">{{ row.score }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="weightedScore" label="加权得分" width="100" align="right">
                    <template #default="{ row }">
                      {{ row.weightedScore }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="evaluation" label="评价" width="100">
                    <template #default="{ row }">
                      <el-tag :type="getEvaluationType(row.evaluation)" size="small">
                        {{ row.evaluation }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="improvement" label="改进建议" min-width="200" show-overflow-tooltip />
                </el-table>
              </div>
              
              <el-empty v-else description="请选择考核期间和对象生成评分" />
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
      
      <!-- 绩效报告 -->
      <el-tab-pane label="绩效报告" name="report">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>绩效分析报告</span>
              <div class="header-actions">
                <el-select v-model="reportPeriod" placeholder="选择期间" style="width: 200px; margin-right: 10px;">
                  <el-option label="2025年8月" value="2025-08" />
                  <el-option label="2025年第3季度" value="2025-Q3" />
                  <el-option label="2025年度" value="2025" />
                </el-select>
                <el-button type="primary" @click="generateReport">生成报告</el-button>
                <el-button type="success" @click="exportReport">导出报告</el-button>
              </div>
            </div>
          </template>
          
          <div v-if="performanceReport" class="performance-report">
            <!-- 执行摘要 -->
            <div class="report-section">
              <h3>执行摘要</h3>
              <p>{{ performanceReport.summary }}</p>
            </div>
            
            <!-- 关键发现 -->
            <div class="report-section">
              <h3>关键发现</h3>
              <el-row :gutter="20">
                <el-col :span="8" v-for="finding in performanceReport.keyFindings" :key="finding.id">
                  <div class="finding-item">
                    <h4>{{ finding.title }}</h4>
                    <p>{{ finding.description }}</p>
                    <el-tag :type="finding.type" size="small">{{ finding.impact }}</el-tag>
                  </div>
                </el-col>
              </el-row>
            </div>
            
            <!-- 改进建议 -->
            <div class="report-section">
              <h3>改进建议</h3>
              <el-timeline>
                <el-timeline-item
                  v-for="suggestion in performanceReport.suggestions"
                  :key="suggestion.id"
                  :timestamp="suggestion.priority"
                  placement="top"
                >
                  <el-card>
                    <h4>{{ suggestion.title }}</h4>
                    <p>{{ suggestion.description }}</p>
                    <div class="suggestion-meta">
                      <el-tag size="small">{{ suggestion.category }}</el-tag>
                      <span class="expected-impact">预期影响：{{ suggestion.expectedImpact }}</span>
                    </div>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </div>
          </div>
          
          <el-empty v-else description="请生成绩效分析报告" />
        </el-card>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 新增指标对话框 -->
    <el-dialog v-model="indicatorDialog" title="新增绩效指标" width="600px">
      <el-form :model="indicatorForm" :rules="indicatorRules" ref="indicatorFormRef" label-width="120px">
        <el-form-item label="指标名称" prop="indicatorName">
          <el-input v-model="indicatorForm.indicatorName" placeholder="请输入指标名称" />
        </el-form-item>
        <el-form-item label="指标分类" prop="category">
          <el-select v-model="indicatorForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="财务指标" value="financial" />
            <el-option label="运营指标" value="operational" />
            <el-option label="客户指标" value="customer" />
            <el-option label="学习指标" value="learning" />
          </el-select>
        </el-form-item>
        <el-form-item label="计量单位" prop="unit">
          <el-input v-model="indicatorForm.unit" placeholder="请输入单位" />
        </el-form-item>
        <el-form-item label="权重" prop="weight">
          <el-input-number
            v-model="indicatorForm.weight"
            :min="0"
            :max="100"
            :precision="1"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="考核周期" prop="period">
          <el-select v-model="indicatorForm.period" placeholder="请选择周期" style="width: 100%">
            <el-option label="月度" value="monthly" />
            <el-option label="季度" value="quarterly" />
            <el-option label="年度" value="yearly" />
          </el-select>
        </el-form-item>
        <el-form-item label="责任人" prop="responsible">
          <el-input v-model="indicatorForm.responsible" placeholder="请输入责任人" />
        </el-form-item>
        <el-form-item label="指标说明">
          <el-input v-model="indicatorForm.description" type="textarea" :rows="3" placeholder="请输入指标说明" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="indicatorDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitIndicator">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Trophy, Aim, TrendCharts, DataAnalysis } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const activeTab = ref('dashboard')
const reportPeriod = ref('2025-08')
const indicatorDialog = ref(false)
const indicatorFormRef = ref()

const kpiChart = ref(null)
const scoreChart = ref(null)
const trendChart = ref(null)
let kpiChartInstance = null
let scoreChartInstance = null
let trendChartInstance = null

// 绩效指标概览
const performanceMetrics = ref([
  {
    id: 1,
    title: '总资产收益率',
    value: '8.5%',
    target: '8.0%',
    achievement: 106.3,
    achievementLevel: 'success',
    level: 'excellent',
    icon: Trophy,
    color: '#67c23a'
  },
  {
    id: 2,
    title: '净资产收益率',
    value: '15.2%',
    target: '15.0%',
    achievement: 101.3,
    achievementLevel: 'success',
    level: 'good',
    icon: Aim,
    color: '#409eff'
  },
  {
    id: 3,
    title: '销售利润率',
    value: '12.8%',
    target: '13.5%',
    achievement: 94.8,
    achievementLevel: 'warning',
    level: 'warning',
    icon: TrendCharts,
    color: '#e6a23c'
  },
  {
    id: 4,
    title: '成本控制率',
    value: '78.2%',
    target: '75.0%',
    achievement: 95.9,
    achievementLevel: 'warning',
    level: 'warning',
    icon: DataAnalysis,
    color: '#f56c6c'
  }
])

// 搜索表单
const indicatorSearchForm = ref({
  category: '',
  period: '',
  status: ''
})

// 评分表单
const evaluationForm = ref({
  period: '2025-08',
  target: 'company'
})

// 指标表单
const indicatorForm = ref({
  indicatorName: '',
  category: '',
  unit: '',
  weight: 0,
  period: '',
  responsible: '',
  description: ''
})

const indicatorRules = {
  indicatorName: [{ required: true, message: '请输入指标名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择指标分类', trigger: 'change' }],
  unit: [{ required: true, message: '请输入计量单位', trigger: 'blur' }],
  weight: [{ required: true, message: '请输入权重', trigger: 'blur' }],
  period: [{ required: true, message: '请选择考核周期', trigger: 'change' }],
  responsible: [{ required: true, message: '请输入责任人', trigger: 'blur' }]
}

// 指标数据
const indicatorData = ref([
  {
    id: 1,
    indicatorName: '总资产收益率',
    category: 'financial',
    unit: '%',
    weight: 25.0,
    target: 8.0,
    actual: 8.5,
    achievement: 106.3,
    period: 'yearly',
    responsible: '财务总监',
    status: 'active'
  },
  {
    id: 2,
    indicatorName: '净资产收益率',
    category: 'financial',
    unit: '%',
    weight: 20.0,
    target: 15.0,
    actual: 15.2,
    achievement: 101.3,
    period: 'yearly',
    responsible: '财务总监',
    status: 'active'
  },
  {
    id: 3,
    indicatorName: '销售增长率',
    category: 'operational',
    unit: '%',
    weight: 15.0,
    target: 20.0,
    actual: 18.5,
    achievement: 92.5,
    period: 'yearly',
    responsible: '销售总监',
    status: 'active'
  },
  {
    id: 4,
    indicatorName: '客户满意度',
    category: 'customer',
    unit: '分',
    weight: 10.0,
    target: 90.0,
    actual: 88.5,
    achievement: 98.3,
    period: 'quarterly',
    responsible: '客服经理',
    status: 'active'
  },
  {
    id: 5,
    indicatorName: '员工培训率',
    category: 'learning',
    unit: '%',
    weight: 8.0,
    target: 95.0,
    actual: 92.0,
    achievement: 96.8,
    period: 'yearly',
    responsible: '人事经理',
    status: 'active'
  }
])

// 评分结果
const evaluationResult = ref(null)

// 绩效报告
const performanceReport = ref(null)

const getAchievementText = (achievement) => {
  if (achievement >= 100) return '达标'
  if (achievement >= 90) return '接近'
  return '未达标'
}

const getCategoryType = (category) => {
  const map = {
    'financial': 'primary',
    'operational': 'success',
    'customer': 'warning',
    'learning': 'info'
  }
  return map[category] || ''
}

const getCategoryText = (category) => {
  const map = {
    'financial': '财务指标',
    'operational': '运营指标',
    'customer': '客户指标',
    'learning': '学习指标'
  }
  return map[category] || category
}

const getPeriodText = (period) => {
  const map = {
    'monthly': '月度',
    'quarterly': '季度',
    'yearly': '年度'
  }
  return map[period] || period
}

const getValueClass = (actual, target) => {
  if (actual >= target) return 'value-good'
  if (actual >= target * 0.9) return 'value-warning'
  return 'value-poor'
}

const getAchievementStatus = (achievement) => {
  if (achievement >= 100) return 'success'
  if (achievement >= 90) return 'warning'
  return 'exception'
}

const getScoreLevel = (score) => {
  if (score >= 90) return 'success'
  if (score >= 80) return 'primary'
  if (score >= 70) return 'warning'
  return 'danger'
}

const getScoreText = (score) => {
  if (score >= 90) return '优秀'
  if (score >= 80) return '良好'
  if (score >= 70) return '合格'
  return '需改进'
}

const getScoreClass = (score) => {
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 70) return 'score-normal'
  return 'score-poor'
}

const getEvaluationType = (evaluation) => {
  const map = {
    '优秀': 'success',
    '良好': 'primary',
    '合格': 'warning',
    '需改进': 'danger'
  }
  return map[evaluation] || 'info'
}

const handleAddIndicator = () => {
  indicatorForm.value = {
    indicatorName: '',
    category: '',
    unit: '',
    weight: 0,
    period: '',
    responsible: '',
    description: ''
  }
  indicatorDialog.value = true
}

const handleImportIndicators = () => {
  ElMessage.info('批量导入功能开发中')
}

const handleIndicatorSearch = () => {
  ElMessage.success('搜索完成')
}

const handleIndicatorReset = () => {
  indicatorSearchForm.value = {
    category: '',
    period: '',
    status: ''
  }
}

const handleViewIndicator = (row) => {
  ElMessage.info(`查看指标：${row.indicatorName}`)
}

const handleEditIndicator = (row) => {
  ElMessage.info(`编辑指标：${row.indicatorName}`)
}

const handleSetTarget = (row) => {
  ElMessage.info(`设定目标：${row.indicatorName}`)
}

const handleViewHistory = (row) => {
  ElMessage.info(`查看历史：${row.indicatorName}`)
}

const handleSubmitIndicator = async () => {
  const valid = await indicatorFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  indicatorDialog.value = false
  ElMessage.success('指标创建成功')
}

const generateEvaluation = () => {
  evaluationResult.value = {
    totalScore: 85.6,
    details: [
      {
        category: '财务指标',
        weight: 50,
        score: 88,
        weightedScore: 44.0,
        evaluation: '良好',
        improvement: '继续保持，关注现金流管理'
      },
      {
        category: '运营指标',
        weight: 30,
        score: 82,
        weightedScore: 24.6,
        evaluation: '良好',
        improvement: '提升生产效率，降低运营成本'
      },
      {
        category: '客户指标',
        weight: 15,
        score: 85,
        weightedScore: 12.8,
        evaluation: '良好',
        improvement: '加强客户关系维护'
      },
      {
        category: '学习指标',
        weight: 5,
        score: 90,
        weightedScore: 4.5,
        evaluation: '优秀',
        improvement: '保持培训投入，提升员工技能'
      }
    ]
  }
  ElMessage.success('评分生成成功')
}

const resetEvaluation = () => {
  evaluationResult.value = null
  evaluationForm.value = {
    period: '2025-08',
    target: 'company'
  }
}

const generateReport = () => {
  performanceReport.value = {
    summary: '本期绩效整体表现良好，总评分85.6分，达到良好水平。财务指标和运营指标表现稳定，客户满意度有所提升，员工培训投入持续增加。',
    keyFindings: [
      {
        id: 1,
        title: '财务表现稳健',
        description: '总资产收益率和净资产收益率均超过目标，盈利能力稳步提升',
        type: 'success',
        impact: '积极'
      },
      {
        id: 2,
        title: '成本控制需加强',
        description: '成本控制率未达预期，需要进一步优化成本结构',
        type: 'warning',
        impact: '关注'
      },
      {
        id: 3,
        title: '客户满意度提升',
        description: '客户满意度较上期提升2.5分，客户关系管理见效',
        type: 'success',
        impact: '积极'
      }
    ],
    suggestions: [
      {
        id: 1,
        title: '优化成本控制体系',
        description: '建立更精细化的成本控制机制，重点关注材料成本和制造费用的管控',
        priority: '高优先级',
        category: '成本管理',
        expectedImpact: '降低成本2-3%'
      },
      {
        id: 2,
        title: '提升销售效率',
        description: '加强销售团队培训，优化销售流程，提高客户转化率',
        priority: '中优先级',
        category: '销售管理',
        expectedImpact: '提升销售收入5-8%'
      },
      {
        id: 3,
        title: '加强数字化建设',
        description: '推进财务数字化转型，提升数据分析能力和决策支持水平',
        priority: '中优先级',
        category: '技术升级',
        expectedImpact: '提升运营效率10-15%'
      }
    ]
  }
  ElMessage.success('报告生成成功')
}

const exportReport = () => {
  ElMessage.success('导出功能开发中')
}

const initKpiChart = () => {
  if (kpiChart.value) {
    kpiChartInstance = echarts.init(kpiChart.value)
    const option = {
      tooltip: {
        trigger: 'axis'
      },
      radar: {
        indicator: [
          { name: '总资产收益率', max: 120 },
          { name: '净资产收益率', max: 120 },
          { name: '销售利润率', max: 120 },
          { name: '成本控制率', max: 120 },
          { name: '客户满意度', max: 120 }
        ]
      },
      series: [
        {
          name: 'KPI达成率',
          type: 'radar',
          data: [
            {
              value: [106.3, 101.3, 94.8, 95.9, 98.3],
              name: '实际达成率',
              itemStyle: { color: '#409eff' },
              areaStyle: { color: 'rgba(64, 158, 255, 0.3)' }
            }
          ]
        }
      ]
    }
    kpiChartInstance.setOption(option)
  }
}

const initScoreChart = () => {
  if (scoreChart.value) {
    scoreChartInstance = echarts.init(scoreChart.value)
    const option = {
      tooltip: {
        trigger: 'item'
      },
      series: [
        {
          name: '绩效评分分布',
          type: 'pie',
          radius: ['40%', '70%'],
          data: [
            { value: 2, name: '优秀(90-100)', itemStyle: { color: '#67c23a' } },
            { value: 5, name: '良好(80-89)', itemStyle: { color: '#409eff' } },
            { value: 3, name: '合格(70-79)', itemStyle: { color: '#e6a23c' } },
            { value: 1, name: '需改进(<70)', itemStyle: { color: '#f56c6c' } }
          ],
          label: {
            formatter: '{b}\n{c}人\n({d}%)'
          }
        }
      ]
    }
    scoreChartInstance.setOption(option)
  }
}

const initTrendChart = () => {
  if (trendChart.value) {
    trendChartInstance = echarts.init(trendChart.value)
    const option = {
      title: {
        text: '绩效趋势分析',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['财务指标', '运营指标', '客户指标', '学习指标'],
        top: 30
      },
      xAxis: {
        type: 'category',
        data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月']
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: '{value}分'
        }
      },
      series: [
        {
          name: '财务指标',
          type: 'line',
          data: [82, 84, 85, 86, 87, 88, 89, 88],
          smooth: true,
          itemStyle: { color: '#409eff' }
        },
        {
          name: '运营指标',
          type: 'line',
          data: [78, 79, 80, 81, 82, 83, 82, 82],
          smooth: true,
          itemStyle: { color: '#67c23a' }
        },
        {
          name: '客户指标',
          type: 'line',
          data: [80, 81, 82, 83, 84, 85, 86, 85],
          smooth: true,
          itemStyle: { color: '#e6a23c' }
        },
        {
          name: '学习指标',
          type: 'line',
          data: [85, 86, 87, 88, 89, 90, 91, 90],
          smooth: true,
          itemStyle: { color: '#f56c6c' }
        }
      ]
    }
    trendChartInstance.setOption(option)
  }
}

onMounted(() => {
  nextTick(() => {
    initKpiChart()
    initScoreChart()
    initTrendChart()
    
    window.addEventListener('resize', () => {
      kpiChartInstance?.resize()
      scoreChartInstance?.resize()
      trendChartInstance?.resize()
    })
  })
})
</script>

<style scoped lang="scss">
.performance-overview {
  margin-bottom: 20px;
  
  .metric-card {
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
    }
    
    &.excellent {
      border-left: 4px solid #67c23a;
    }
    
    &.good {
      border-left: 4px solid #409eff;
    }
    
    &.warning {
      border-left: 4px solid #e6a23c;
    }
    
    &.poor {
      border-left: 4px solid #f56c6c;
    }
    
    .metric-content {
      display: flex;
      align-items: center;
      
      .metric-icon {
        margin-right: 16px;
      }
      
      .metric-info {
        flex: 1;
        
        h4 {
          margin: 0 0 8px 0;
          color: #606266;
          font-size: 14px;
          font-weight: normal;
        }
        
        .metric-value {
          margin: 0 0 8px 0;
          color: #303133;
          font-size: 24px;
          font-weight: 600;
        }
        
        .metric-target {
          display: flex;
          justify-content: space-between;
          align-items: center;
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
}

.dashboard-section {
  margin-top: 20px;
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

.value-good {
  color: #67c23a;
  font-weight: 600;
}

.value-warning {
  color: #e6a23c;
  font-weight: 600;
}

.value-poor {
  color: #f56c6c;
  font-weight: 600;
}

.evaluation-result {
  .score-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 4px;
    
    h3 {
      margin: 0;
      color: #303133;
      
      .total-score {
        color: #409eff;
        font-size: 32px;
      }
    }
  }
}

.score-excellent {
  color: #67c23a;
  font-weight: 600;
}

.score-good {
  color: #409eff;
  font-weight: 600;
}

.score-normal {
  color: #e6a23c;
  font-weight: 600;
}

.score-poor {
  color: #f56c6c;
  font-weight: 600;
}

.performance-report {
  .report-section {
    margin-bottom: 30px;
    
    h3 {
      margin: 0 0 16px 0;
      padding-bottom: 8px;
      border-bottom: 2px solid #409eff;
      color: #303133;
    }
    
    p {
      color: #606266;
      line-height: 1.6;
    }
  }
  
  .finding-item {
    padding: 16px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    background: #fafafa;
    
    h4 {
      margin: 0 0 8px 0;
      color: #303133;
    }
    
    p {
      margin: 0 0 8px 0;
      color: #606266;
    }
  }
  
  .suggestion-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 8px;
    
    .expected-impact {
      font-size: 12px;
      color: #909399;
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
  .performance-overview {
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
  
  .evaluation-result .score-header {
    flex-direction: column;
    gap: 12px;
  }
}
</style>