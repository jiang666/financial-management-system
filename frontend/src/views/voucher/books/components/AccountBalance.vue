<template>
  <div class="account-balance">
    <!-- 查询条件 -->
    <el-card class="query-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="查询日期">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item label="科目编码">
          <el-input
            v-model="queryForm.accountCode"
            placeholder="输入科目编码"
            style="width: 150px"
            clearable
          />
        </el-form-item>
        <el-form-item label="显示零余额">
          <el-switch v-model="queryForm.showZeroBalance" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 科目余额表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>科目余额表</span>
          <el-button text type="primary" @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table
        :data="balanceData"
        border
        stripe
        v-loading="loading"
        row-key="accountCode"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
        style="width: 100%"
      >
        <el-table-column prop="accountCode" label="科目编码" width="120" fixed="left" />
        <el-table-column prop="accountName" label="科目名称" width="220" fixed="left">
          <template #default="{ row }">
            <span :style="{ paddingLeft: (row.level - 1) * 20 + 'px' }">
              {{ row.accountName }}
            </span>
          </template>
        </el-table-column>
        <!-- 期初余额 -->
        <el-table-column label="期初余额" align="center">
          <el-table-column prop="openingDebit" label="借方" width="140" align="right">
            <template #default="{ row }">
              <span v-if="row.openingDebit > 0" class="amount-text">
                {{ formatAmount(row.openingDebit) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="openingCredit" label="贷方" width="140" align="right">
            <template #default="{ row }">
              <span v-if="row.openingCredit > 0" class="amount-text">
                {{ formatAmount(row.openingCredit) }}
              </span>
            </template>
          </el-table-column>
        </el-table-column>
        
        <!-- 本期发生额 -->
        <el-table-column label="本期发生" align="center">
          <el-table-column prop="currentDebit" label="借方" width="140" align="right">
            <template #default="{ row }">
              <span v-if="row.currentDebit > 0" class="amount-text">
                {{ formatAmount(row.currentDebit) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="currentCredit" label="贷方" width="140" align="right">
            <template #default="{ row }">
              <span v-if="row.currentCredit > 0" class="amount-text">
                {{ formatAmount(row.currentCredit) }}
              </span>
            </template>
          </el-table-column>
        </el-table-column>
        
        <!-- 本年累计 -->
        <el-table-column label="本年累计" align="center">
          <el-table-column prop="yearDebit" label="借方" width="140" align="right">
            <template #default="{ row }">
              <span v-if="row.yearDebit > 0" class="amount-text">
                {{ formatAmount(row.yearDebit) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="yearCredit" label="贷方" width="140" align="right">
            <template #default="{ row }">
              <span v-if="row.yearCredit > 0" class="amount-text">
                {{ formatAmount(row.yearCredit) }}
              </span>
            </template>
          </el-table-column>
        </el-table-column>
        
        <!-- 期末余额 -->
        <el-table-column label="期末余额" align="center">
          <el-table-column prop="endingDebit" label="借方" width="140" align="right">
            <template #default="{ row }">
              <span v-if="row.endingDebit > 0" class="amount-text">
                {{ formatAmount(row.endingDebit) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="endingCredit" label="贷方" width="140" align="right">
            <template #default="{ row }">
              <span v-if="row.endingCredit > 0" class="amount-text">
                {{ formatAmount(row.endingCredit) }}
              </span>
            </template>
          </el-table-column>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="viewDetails(row)">
              查看明细
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 汇总信息 -->
    <el-card class="summary-card" shadow="never">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="summary-item">
            <div class="summary-label">期初余额合计</div>
            <div class="summary-value">
              <div>借：{{ formatAmount(summaryData.totalOpeningDebit) }}</div>
              <div>贷：{{ formatAmount(summaryData.totalOpeningCredit) }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-item">
            <div class="summary-label">本期发生合计</div>
            <div class="summary-value">
              <div>借：{{ formatAmount(summaryData.totalCurrentDebit) }}</div>
              <div>贷：{{ formatAmount(summaryData.totalCurrentCredit) }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-item">
            <div class="summary-label">本年累计合计</div>
            <div class="summary-value">
              <div>借：{{ formatAmount(summaryData.totalYearDebit) }}</div>
              <div>贷：{{ formatAmount(summaryData.totalYearCredit) }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-item">
            <div class="summary-label">期末余额合计</div>
            <div class="summary-value">
              <div>借：{{ formatAmount(summaryData.totalEndingDebit) }}</div>
              <div>贷：{{ formatAmount(summaryData.totalEndingCredit) }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Download, Refresh } from '@element-plus/icons-vue'

const loading = ref(false)

// 查询表单
const queryForm = reactive({
  dateRange: ['2025-08-01', '2025-08-31'],
  accountCode: '',
  showZeroBalance: false
})

// 科目余额数据
const balanceData = ref([
  {
    accountCode: '1001',
    accountName: '库存现金',
    level: 1,
    openingDebit: 50000,
    openingCredit: 0,
    currentDebit: 150000,
    currentCredit: 120000,
    yearDebit: 800000,
    yearCredit: 750000,
    endingDebit: 80000,
    endingCredit: 0,
    hasChildren: true,
    children: [
      {
        accountCode: '100101',
        accountName: '人民币现金',
        level: 2,
        openingDebit: 45000,
        openingCredit: 0,
        currentDebit: 130000,
        currentCredit: 100000,
        yearDebit: 700000,
        yearCredit: 650000,
        endingDebit: 75000,
        endingCredit: 0
      },
      {
        accountCode: '100102',
        accountName: '外币现金',
        level: 2,
        openingDebit: 5000,
        openingCredit: 0,
        currentDebit: 20000,
        currentCredit: 20000,
        yearDebit: 100000,
        yearCredit: 100000,
        endingDebit: 5000,
        endingCredit: 0
      }
    ]
  },
  {
    accountCode: '1002',
    accountName: '银行存款',
    level: 1,
    openingDebit: 500000,
    openingCredit: 0,
    currentDebit: 800000,
    currentCredit: 600000,
    yearDebit: 4000000,
    yearCredit: 3600000,
    endingDebit: 700000,
    endingCredit: 0,
    hasChildren: true,
    children: [
      {
        accountCode: '100201',
        accountName: '工商银行基本户',
        level: 2,
        openingDebit: 300000,
        openingCredit: 0,
        currentDebit: 500000,
        currentCredit: 350000,
        yearDebit: 2500000,
        yearCredit: 2250000,
        endingDebit: 450000,
        endingCredit: 0
      },
      {
        accountCode: '100202',
        accountName: '建设银行一般户',
        level: 2,
        openingDebit: 200000,
        openingCredit: 0,
        currentDebit: 300000,
        currentCredit: 250000,
        yearDebit: 1500000,
        yearCredit: 1350000,
        endingDebit: 250000,
        endingCredit: 0
      }
    ]
  },
  {
    accountCode: '1122',
    accountName: '应收账款',
    level: 1,
    openingDebit: 200000,
    openingCredit: 0,
    currentDebit: 300000,
    currentCredit: 250000,
    yearDebit: 1200000,
    yearCredit: 1100000,
    endingDebit: 250000,
    endingCredit: 0
  },
  {
    accountCode: '2001',
    accountName: '短期借款',
    level: 1,
    openingDebit: 0,
    openingCredit: 100000,
    currentDebit: 20000,
    currentCredit: 50000,
    yearDebit: 100000,
    yearCredit: 150000,
    endingDebit: 0,
    endingCredit: 130000
  },
  {
    accountCode: '2202',
    accountName: '应付账款',
    level: 1,
    openingDebit: 0,
    openingCredit: 150000,
    currentDebit: 100000,
    currentCredit: 180000,
    yearDebit: 500000,
    yearCredit: 610000,
    endingDebit: 0,
    endingCredit: 230000
  },
  {
    accountCode: '6001',
    accountName: '主营业务收入',
    level: 1,
    openingDebit: 0,
    openingCredit: 0,
    currentDebit: 0,
    currentCredit: 500000,
    yearDebit: 0,
    yearCredit: 2500000,
    endingDebit: 0,
    endingCredit: 500000
  },
  {
    accountCode: '5001',
    accountName: '主营业务成本',
    level: 1,
    openingDebit: 0,
    openingCredit: 0,
    currentDebit: 300000,
    currentCredit: 0,
    yearDebit: 1500000,
    yearCredit: 0,
    endingDebit: 300000,
    endingCredit: 0
  }
])

// 汇总数据
const summaryData = ref({
  totalOpeningDebit: 750000,
  totalOpeningCredit: 250000,
  totalCurrentDebit: 1570000,
  totalCurrentCredit: 1550000,
  totalYearDebit: 8100000,
  totalYearCredit: 8010000,
  totalEndingDebit: 1330000,
  totalEndingCredit: 860000
})

// 格式化金额
const formatAmount = (amount) => {
  if (amount === 0) return '0.00'
  return Math.abs(amount).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

// 查询
const handleQuery = () => {
  loading.value = true
  // 模拟API调用
  setTimeout(() => {
    ElMessage.success('查询完成')
    loading.value = false
  }, 1000)
}

// 重置
const handleReset = () => {
  queryForm.dateRange = ['2025-08-01', '2025-08-31']
  queryForm.accountCode = ''
  queryForm.showZeroBalance = false
}

// 导出
const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

// 刷新
const handleRefresh = () => {
  handleQuery()
}

// 查看明细
const viewDetails = (row) => {
  ElMessage.info(`查看 ${row.accountName} 的明细账`)
}

onMounted(() => {
  // 初始化时加载数据
  handleQuery()
})
</script>

<style scoped lang="scss">
.account-balance {
  .query-card {
    margin-bottom: 20px;
  }

  .table-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .summary-card {
    .summary-item {
      text-align: center;
      padding: 20px;
      border-radius: 8px;
      background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);

      .summary-label {
        font-size: 14px;
        color: #666;
        margin-bottom: 8px;
      }

      .summary-value {
        font-size: 20px;
        font-weight: bold;
        
        &.positive {
          color: #409eff;
        }
        
        &.negative {
          color: #f56c6c;
        }
        
        &.neutral {
          color: #67c23a;
        }
      }
    }
  }

  .amount-text {
    color: #333;
  }

  .negative-amount {
    color: #f56c6c;
  }

  :deep(.el-table) {
    .el-table__row {
      &:hover {
        background-color: #f5f7fa;
      }
    }
  }
}
</style>