<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">经营分析</h2>
      <p class="description">多维度分析企业经营状况，提供决策支持</p>
    </div>
    
    <!-- 时间范围选择 -->
    <el-card class="period-selector">
      <el-form :model="analysisForm" inline>
        <el-form-item label="分析期间">
          <el-date-picker
            v-model="analysisForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="refreshAnalysis"
          />
        </el-form-item>
        <el-form-item label="对比期间">
          <el-date-picker
            v-model="analysisForm.compareDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="对比开始日期"
            end-placeholder="对比结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="refreshAnalysis"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Refresh" @click="refreshAnalysis" :loading="loading">刷新分析</el-button>
          <el-button type="success" icon="Download" @click="exportAnalysis">导出报告</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 关键指标概览 -->
    <el-row :gutter="16" class="kpi-overview">
      <el-col :span="6" v-for="kpi in kpiData" :key="kpi.id">
        <el-card class="kpi-card" :class="{ 'positive': kpi.change > 0, 'negative': kpi.change < 0 }">
          <div class="kpi-content">
            <div class="kpi-icon">
              <el-icon :size="32" :color="kpi.color">
                <component :is="kpi.icon" />
              </el-icon>
            </div>
            <div class="kpi-info">
              <h4>{{ kpi.title }}</h4>
              <p class="kpi-value">{{ kpi.value }}</p>
              <div class="kpi-change">
                <el-icon v-if="kpi.change > 0" color="#67c23a"><ArrowUp /></el-icon>
                <el-icon v-else-if="kpi.change < 0" color="#f56c6c"><ArrowDown /></el-icon>
                <el-icon v-else color="#909399"><Minus /></el-icon>
                <span :class="kpi.change > 0 ? 'positive' : kpi.change < 0 ? 'negative' : 'neutral'">
                  {{ Math.abs(kpi.change) }}%
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 分析图表区域 -->
    <el-row :gutter="20" class="charts-section">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>收入结构分析</span>
          </template>
          <div ref="revenueChart" style="height: 300px"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>成本结构分析</span>
          </template>
          <div ref="costChart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="charts-section">
      <el-col :span="24">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>经营趋势分析</span>
              <el-radio-group v-model="trendType" size="small" @change="updateTrendChart">
                <el-radio-button label="revenue">收入趋势</el-radio-button>
                <el-radio-button label="profit">利润趋势</el-radio-button>
                <el-radio-button label="cost">成本趋势</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChart" style="height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 财务比率分析 -->
    <el-card class="ratio-analysis">
      <template #header>
        <span>财务比率分析</span>
      </template>
      
      <el-tabs v-model="activeRatioTab">
        <el-tab-pane label="盈利能力" name="profitability">
          <el-table :data="profitabilityRatios" border style="width: 100%" stripe>
            <el-table-column prop="ratioName" label="比率名称" width="200" />
            <el-table-column prop="currentValue" label="本期值" width="120" align="right">
              <template #default="{ row }">
                {{ row.currentValue }}{{ row.unit }}
              </template>
            </el-table-column>
            <el-table-column prop="previousValue" label="上期值" width="120" align="right">
              <template #default="{ row }">
                {{ row.previousValue }}{{ row.unit }}
              </template>
            </el-table-column>
            <el-table-column prop="change" label="变动" width="120" align="right">
              <template #default="{ row }">
                <span :class="row.change > 0 ? 'positive' : row.change < 0 ? 'negative' : 'neutral'">
                  {{ row.change > 0 ? '+' : '' }}{{ row.change }}{{ row.unit }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="benchmark" label="行业基准" width="120" align="right">
              <template #default="{ row }">
                {{ row.benchmark }}{{ row.unit }}
              </template>
            </el-table-column>
            <el-table-column prop="evaluation" label="评价" width="100">
              <template #default="{ row }">
                <el-tag :type="getEvaluationType(row.evaluation)" size="small">
                  {{ row.evaluation }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="偿债能力" name="solvency">
          <el-table :data="solvencyRatios" border style="width: 100%" stripe>
            <el-table-column prop="ratioName" label="比率名称" width="200" />
            <el-table-column prop="currentValue" label="本期值" width="120" align="right">
              <template #default="{ row }">
                {{ row.currentValue }}{{ row.unit }}
              </template>
            </el-table-column>
            <el-table-column prop="previousValue" label="上期值" width="120" align="right">
              <template #default="{ row }">
                {{ row.previousValue }}{{ row.unit }}
              </template>
            </el-table-column>
            <el-table-column prop="change" label="变动" width="120" align="right">
              <template #default="{ row }">
                <span :class="row.change > 0 ? 'positive' : row.change < 0 ? 'negative' : 'neutral'">
                  {{ row.change > 0 ? '+' : '' }}{{ row.change }}{{ row.unit }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="benchmark" label="行业基准" width="120" align="right">
              <template #default="{ row }">
                {{ row.benchmark }}{{ row.unit }}
              </template>
            </el-table-column>
            <el-table-column prop="evaluation" label="评价" width="100">
              <template #default="{ row }">
                <el-tag :type="getEvaluationType(row.evaluation)" size="small">
                  {{ row.evaluation }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="营运能力" name="efficiency">
          <el-table :data="efficiencyRatios" border style="width: 100%" stripe>
            <el-table-column prop="ratioName" label="比率名称" width="200" />
            <el-table-column prop="currentValue" label="本期值" width="120" align="right">
              <template #default="{ row }">
                {{ row.currentValue }}{{ row.unit }}
              </template>
            </el-table-column>
            <el-table-column prop="previousValue" label="上期值" width="120" align="right">
              <template #default="{ row }">
                {{ row.previousValue }}{{ row.unit }}
              </template>
            </el-table-column>
            <el-table-column prop="change" label="变动" width="120" align="right">
              <template #default="{ row }">
                <span :class="row.change > 0 ? 'positive' : row.change < 0 ? 'negative' : 'neutral'">
                  {{ row.change > 0 ? '+' : '' }}{{ row.change }}{{ row.unit }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="benchmark" label="行业基准" width="120" align="right">
              <template #default="{ row }">
                {{ row.benchmark }}{{ row.unit }}
              </template>
            </el-table-column>
            <el-table-column prop="evaluation" label="评价" width="100">
              <template #default="{ row }">
                <el-tag :type="getEvaluationType(row.evaluation)" size="small">
                  {{ row.evaluation }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 经营建议 -->
    <el-card class="suggestions">
      <template #header>
        <span>经营建议</span>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="8" v-for="suggestion in suggestions" :key="suggestion.category">
          <div class="suggestion-item">
            <h4>{{ suggestion.category }}</h4>
            <ul>
              <li v-for="item in suggestion.items" :key="item">{{ item }}</li>
            </ul>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowUp, ArrowDown, Minus, TrendCharts, Wallet, CreditCard, PieChart } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const loading = ref(false)
const activeRatioTab = ref('profitability')
const trendType = ref('revenue')

const revenueChart = ref(null)
const costChart = ref(null)
const trendChart = ref(null)
let revenueChartInstance = null
let costChartInstance = null
let trendChartInstance = null

// 分析表单
const analysisForm = ref({
  dateRange: ['2025-01-01', '2025-08-31'],
  compareDateRange: ['2024-01-01', '2024-08-31']
})

// 关键指标数据
const kpiData = ref([
  {
    id: 1,
    title: '营业收入',
    value: '¥5,000万',
    change: 11.2,
    icon: TrendCharts,
    color: '#409eff'
  },
  {
    id: 2,
    title: '净利润',
    value: '¥680万',
    change: 45.5,
    icon: Wallet,
    color: '#67c23a'
  },
  {
    id: 3,
    title: '毛利率',
    value: '40.0%',
    change: 2.8,
    icon: PieChart,
    color: '#e6a23c'
  },
  {
    id: 4,
    title: '净资产收益率',
    value: '13.6%',
    change: 3.2,
    icon: CreditCard,
    color: '#f56c6c'
  }
])

// 盈利能力比率
const profitabilityRatios = ref([
  {
    ratioName: '销售毛利率',
    currentValue: 40.0,
    previousValue: 37.8,
    change: 2.2,
    benchmark: 35.0,
    unit: '%',
    evaluation: '良好',
    description: '反映产品盈利能力，高于行业平均水平'
  },
  {
    ratioName: '销售净利率',
    currentValue: 13.6,
    previousValue: 10.4,
    change: 3.2,
    benchmark: 12.0,
    unit: '%',
    evaluation: '优秀',
    description: '净利润占营业收入的比例，表现优秀'
  },
  {
    ratioName: '资产收益率',
    currentValue: 7.7,
    previousValue: 5.8,
    change: 1.9,
    benchmark: 6.5,
    unit: '%',
    evaluation: '良好',
    description: '资产的盈利效率，高于行业基准'
  },
  {
    ratioName: '净资产收益率',
    currentValue: 13.6,
    previousValue: 10.4,
    change: 3.2,
    benchmark: 11.0,
    unit: '%',
    evaluation: '优秀',
    description: '股东投资回报率，表现突出'
  }
])

// 偿债能力比率
const solvencyRatios = ref([
  {
    ratioName: '流动比率',
    currentValue: 2.1,
    previousValue: 1.8,
    change: 0.3,
    benchmark: 2.0,
    unit: '',
    evaluation: '良好',
    description: '短期偿债能力良好，流动性充足'
  },
  {
    ratioName: '速动比率',
    currentValue: 1.5,
    previousValue: 1.2,
    change: 0.3,
    benchmark: 1.0,
    unit: '',
    evaluation: '优秀',
    description: '剔除存货后的短期偿债能力'
  },
  {
    ratioName: '资产负债率',
    currentValue: 43.2,
    previousValue: 44.1,
    change: -0.9,
    benchmark: 50.0,
    unit: '%',
    evaluation: '良好',
    description: '财务杠杆适中，风险可控'
  },
  {
    ratioName: '利息保障倍数',
    currentValue: 8.5,
    previousValue: 6.2,
    change: 2.3,
    benchmark: 5.0,
    unit: '倍',
    evaluation: '优秀',
    description: '长期偿债能力强，财务安全'
  }
])

// 营运能力比率
const efficiencyRatios = ref([
  {
    ratioName: '存货周转率',
    currentValue: 6.8,
    previousValue: 5.9,
    change: 0.9,
    benchmark: 6.0,
    unit: '次',
    evaluation: '良好',
    description: '存货管理效率良好，周转较快'
  },
  {
    ratioName: '应收账款周转率',
    currentValue: 12.5,
    previousValue: 10.8,
    change: 1.7,
    benchmark: 10.0,
    unit: '次',
    evaluation: '优秀',
    description: '收款效率高，资金回笼快'
  },
  {
    ratioName: '总资产周转率',
    currentValue: 0.57,
    previousValue: 0.52,
    change: 0.05,
    benchmark: 0.5,
    unit: '次',
    evaluation: '良好',
    description: '资产利用效率较高'
  },
  {
    ratioName: '应付账款周转率',
    currentValue: 8.3,
    previousValue: 9.1,
    change: -0.8,
    benchmark: 8.0,
    unit: '次',
    evaluation: '一般',
    description: '付款周期适中，现金流管理良好'
  }
])

// 经营建议
const suggestions = ref([
  {
    category: '收入增长',
    items: [
      '拓展新市场和客户群体',
      '优化产品结构，提高附加值',
      '加强营销推广，提升品牌影响力',
      '探索新的销售渠道'
    ]
  },
  {
    category: '成本控制',
    items: [
      '优化供应链管理，降低采购成本',
      '提高生产效率，减少浪费',
      '合理控制人工成本',
      '加强费用预算管理'
    ]
  },
  {
    category: '风险管理',
    items: [
      '加强应收账款管理',
      '优化库存结构',
      '建立风险预警机制',
      '完善内控制度'
    ]
  }
])

const getEvaluationType = (evaluation) => {
  const map = {
    '优秀': 'success',
    '良好': 'primary',
    '一般': 'warning',
    '较差': 'danger'
  }
  return map[evaluation] || 'info'
}

const refreshAnalysis = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('分析数据已更新')
  }, 1000)
}

const exportAnalysis = () => {
  ElMessage.success('导出功能开发中')
}

const updateTrendChart = () => {
  initTrendChart()
}

const initRevenueChart = () => {
  if (revenueChart.value) {
    revenueChartInstance = echarts.init(revenueChart.value)
    const option = {
      tooltip: {
        trigger: 'item'
      },
      series: [
        {
          name: '收入结构',
          type: 'pie',
          radius: ['40%', '70%'],
          data: [
            { value: 3000, name: '产品销售', itemStyle: { color: '#409eff' } },
            { value: 1500, name: '技术服务', itemStyle: { color: '#67c23a' } },
            { value: 500, name: '其他收入', itemStyle: { color: '#e6a23c' } }
          ],
          label: {
            formatter: '{b}: {c}万元\n({d}%)'
          }
        }
      ]
    }
    revenueChartInstance.setOption(option)
  }
}

const initCostChart = () => {
  if (costChart.value) {
    costChartInstance = echarts.init(costChart.value)
    const option = {
      tooltip: {
        trigger: 'item'
      },
      series: [
        {
          name: '成本结构',
          type: 'pie',
          radius: ['40%', '70%'],
          data: [
            { value: 1800, name: '原材料成本', itemStyle: { color: '#f56c6c' } },
            { value: 800, name: '人工成本', itemStyle: { color: '#e6a23c' } },
            { value: 400, name: '制造费用', itemStyle: { color: '#909399' } }
          ],
          label: {
            formatter: '{b}: {c}万元\n({d}%)'
          }
        }
      ]
    }
    costChartInstance.setOption(option)
  }
}

const initTrendChart = () => {
  if (trendChart.value) {
    trendChartInstance = echarts.init(trendChart.value)
    
    const dataMap = {
      revenue: {
        title: '营业收入趋势',
        data: [4200, 4300, 4500, 4600, 4800, 4900, 5000, 5200],
        color: '#409eff'
      },
      profit: {
        title: '净利润趋势',
        data: [420, 450, 480, 520, 580, 620, 680, 720],
        color: '#67c23a'
      },
      cost: {
        title: '营业成本趋势',
        data: [2800, 2900, 3000, 3100, 3200, 3300, 3400, 3500],
        color: '#f56c6c'
      }
    }
    
    const currentData = dataMap[trendType.value]
    
    const option = {
      title: {
        text: currentData.title,
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
          formatter: (value) => value + '万'
        }
      },
      series: [
        {
          data: currentData.data,
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          itemStyle: {
            color: currentData.color
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: currentData.color + '40' },
                { offset: 1, color: currentData.color + '10' }
              ]
            }
          }
        }
      ]
    }
    
    trendChartInstance.setOption(option)
  }
}

onMounted(() => {
  nextTick(() => {
    initRevenueChart()
    initCostChart()
    initTrendChart()
    
    window.addEventListener('resize', () => {
      revenueChartInstance?.resize()
      costChartInstance?.resize()
      trendChartInstance?.resize()
    })
  })
})
</script>

<style scoped lang="scss">
.period-selector {
  margin-bottom: 20px;
}

.kpi-overview {
  margin-bottom: 20px;
  
  .kpi-card {
    transition: all 0.3s ease;
    cursor: pointer;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
    }
    
    &.positive {
      border-left: 4px solid #67c23a;
    }
    
    &.negative {
      border-left: 4px solid #f56c6c;
    }
    
    .kpi-content {
      display: flex;
      align-items: center;
      
      .kpi-icon {
        margin-right: 16px;
      }
      
      .kpi-info {
        flex: 1;
        
        h4 {
          margin: 0 0 8px 0;
          color: #606266;
          font-size: 14px;
          font-weight: normal;
        }
        
        .kpi-value {
          margin: 0 0 8px 0;
          color: #303133;
          font-size: 24px;
          font-weight: 600;
        }
        
        .kpi-change {
          display: flex;
          align-items: center;
          gap: 4px;
          
          .positive {
            color: #67c23a;
          }
          
          .negative {
            color: #f56c6c;
          }
          
          .neutral {
            color: #909399;
          }
        }
      }
    }
  }
}

.charts-section {
  margin-bottom: 20px;
  
  .chart-card {
    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
}

.ratio-analysis {
  margin-bottom: 20px;
  
  .positive {
    color: #67c23a;
    font-weight: 600;
  }
  
  .negative {
    color: #f56c6c;
    font-weight: 600;
  }
  
  .neutral {
    color: #909399;
    font-weight: 600;
  }
}

.suggestions {
  .suggestion-item {
    h4 {
      margin: 0 0 16px 0;
      padding-bottom: 8px;
      border-bottom: 2px solid #409eff;
      color: #303133;
      font-size: 16px;
    }
    
    ul {
      margin: 0;
      padding-left: 20px;
      
      li {
        margin-bottom: 8px;
        color: #606266;
        line-height: 1.6;
      }
    }
  }
}

@media (max-width: 768px) {
  .kpi-overview {
    .el-col {
      margin-bottom: 16px;
    }
  }
  
  .charts-section {
    .el-col {
      margin-bottom: 20px;
    }
  }
  
  .suggestions {
    .el-col {
      margin-bottom: 20px;
    }
  }
}
</style>