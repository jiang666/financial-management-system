<template>
  <div class="chronological-book">
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
        <el-form-item label="凭证字号">
          <el-input
            v-model="queryForm.voucherNo"
            placeholder="输入凭证号"
            style="width: 120px"
            clearable
          />
        </el-form-item>
        <el-form-item label="科目编码">
          <el-input
            v-model="queryForm.accountCode"
            placeholder="输入科目编码"
            style="width: 120px"
            clearable
          />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input
            v-model="queryForm.summary"
            placeholder="输入摘要关键字"
            style="width: 150px"
            clearable
          />
        </el-form-item>
        <el-form-item label="包含未记账">
          <el-switch v-model="queryForm.includeUnposted" />
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

    <!-- 序时账簿 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>序时账簿</span>
          <div class="header-actions">
            <el-button text type="primary" @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button text type="warning" @click="toggleGroupByVoucher">
              <el-icon><Collection /></el-icon>
              {{ groupByVoucher ? '展开分录' : '按凭证分组' }}
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table
        :data="chronologicalData"
        border
        stripe
        v-loading="loading"
        :row-key="groupByVoucher ? 'voucherNo' : 'id'"
        :tree-props="groupByVoucher ? { children: 'entries', hasChildren: 'hasChildren' } : {}"
        style="width: 100%"
        :summary-method="getSummaries"
        show-summary
      >
        <el-table-column prop="date" label="日期" width="100" align="center" />
        <el-table-column prop="voucherNo" label="凭证字号" width="120" align="center">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="viewVoucher(row)">
              {{ row.voucherNo }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="entryNo" label="分录号" width="80" align="center" v-if="!groupByVoucher" />
        <el-table-column prop="accountCode" label="科目编码" width="100" />
        <el-table-column prop="accountName" label="科目名称" width="150" />
        <el-table-column prop="summary" label="摘要" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.summary" placement="top" v-if="row.summary && row.summary.length > 20">
              <span>{{ row.summary.substring(0, 20) }}...</span>
            </el-tooltip>
            <span v-else>{{ row.summary }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="debitAmount" label="借方金额" width="130" align="right">
          <template #default="{ row }">
            <span v-if="row.debitAmount" class="amount-text positive">
              {{ formatAmount(row.debitAmount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="creditAmount" label="贷方金额" width="130" align="right">
          <template #default="{ row }">
            <span v-if="row.creditAmount" class="amount-text negative">
              {{ formatAmount(row.creditAmount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="direction" label="余额方向" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.direction" :type="row.direction === '借' ? 'warning' : 'success'" size="small">
              {{ row.direction }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" width="130" align="right">
          <template #default="{ row }">
            <span v-if="row.balance !== undefined" class="amount-text" :class="{ 'negative': row.balance < 0 }">
              {{ formatAmount(Math.abs(row.balance)) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '已记账' ? 'success' : 'warning'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="viewVoucher(row)">
              查看凭证
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 统计信息 -->
    <el-card class="stats-card" shadow="never">
      <el-row :gutter="20">
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-label">记录条数</div>
            <div class="stat-value">{{ chronologicalData.length }}</div>
          </div>
        </el-col>
        <el-col :span="5">
          <div class="stat-item">
            <div class="stat-label">借方合计</div>
            <div class="stat-value positive">{{ formatAmount(totalDebit) }}</div>
          </div>
        </el-col>
        <el-col :span="5">
          <div class="stat-item">
            <div class="stat-label">贷方合计</div>
            <div class="stat-value negative">{{ formatAmount(totalCredit) }}</div>
          </div>
        </el-col>
        <el-col :span="5">
          <div class="stat-item">
            <div class="stat-label">借贷差额</div>
            <div class="stat-value" :class="{ 'positive': difference > 0, 'negative': difference < 0 }">
              {{ formatAmount(Math.abs(difference)) }}
            </div>
          </div>
        </el-col>
        <el-col :span="5">
          <div class="stat-item">
            <div class="stat-label">凭证数量</div>
            <div class="stat-value">{{ voucherCount }} 张</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Download, Refresh, Collection } from '@element-plus/icons-vue'

const loading = ref(false)
const groupByVoucher = ref(false)

// 查询表单
const queryForm = reactive({
  dateRange: ['2025-08-01', '2025-08-31'],
  voucherNo: '',
  accountCode: '',
  summary: '',
  includeUnposted: false
})

// 序时账数据
const chronologicalData = ref([
  {
    id: 1,
    date: '2025-08-01',
    voucherNo: 'JZ001',
    entryNo: 1,
    accountCode: '1001',
    accountName: '库存现金',
    summary: '收到某公司货款',
    debitAmount: 50000,
    creditAmount: null,
    direction: '借',
    balance: 50000,
    status: '已记账'
  },
  {
    id: 2,
    date: '2025-08-01',
    voucherNo: 'JZ001',
    entryNo: 2,
    accountCode: '1122',
    accountName: '应收账款',
    summary: '收到某公司货款',
    debitAmount: null,
    creditAmount: 50000,
    direction: '借',
    balance: 150000,
    status: '已记账'
  },
  {
    id: 3,
    date: '2025-08-02',
    voucherNo: 'JZ002',
    entryNo: 1,
    accountCode: '1002',
    accountName: '银行存款',
    summary: '银行转账收入',
    debitAmount: 100000,
    creditAmount: null,
    direction: '借',
    balance: 600000,
    status: '已记账'
  },
  {
    id: 4,
    date: '2025-08-02',
    voucherNo: 'JZ002',
    entryNo: 2,
    accountCode: '6001',
    accountName: '主营业务收入',
    summary: '银行转账收入',
    debitAmount: null,
    creditAmount: 100000,
    direction: '贷',
    balance: -200000,
    status: '已记账'
  },
  {
    id: 5,
    date: '2025-08-03',
    voucherNo: 'JZ003',
    entryNo: 1,
    accountCode: '5001',
    accountName: '主营业务成本',
    summary: '结转销售成本',
    debitAmount: 60000,
    creditAmount: null,
    direction: '借',
    balance: 60000,
    status: '已记账'
  },
  {
    id: 6,
    date: '2025-08-03',
    voucherNo: 'JZ003',
    entryNo: 2,
    accountCode: '1401',
    accountName: '库存商品',
    summary: '结转销售成本',
    debitAmount: null,
    creditAmount: 60000,
    direction: '借',
    balance: 340000,
    status: '已记账'
  },
  {
    id: 7,
    date: '2025-08-04',
    voucherNo: 'JZ004',
    entryNo: 1,
    accountCode: '6602',
    accountName: '销售费用',
    summary: '支付广告费',
    debitAmount: 5000,
    creditAmount: null,
    direction: '借',
    balance: 15000,
    status: '未记账'
  },
  {
    id: 8,
    date: '2025-08-04',
    voucherNo: 'JZ004',
    entryNo: 2,
    accountCode: '1001',
    accountName: '库存现金',
    summary: '支付广告费',
    debitAmount: null,
    creditAmount: 5000,
    direction: '借',
    balance: 45000,
    status: '未记账'
  }
])

// 计算属性
const totalDebit = computed(() => {
  return chronologicalData.value.reduce((sum, item) => sum + (item.debitAmount || 0), 0)
})

const totalCredit = computed(() => {
  return chronologicalData.value.reduce((sum, item) => sum + (item.creditAmount || 0), 0)
})

const difference = computed(() => {
  return totalDebit.value - totalCredit.value
})

const voucherCount = computed(() => {
  const uniqueVouchers = new Set(chronologicalData.value.map(item => item.voucherNo))
  return uniqueVouchers.size
})

// 格式化金额
const formatAmount = (amount) => {
  if (amount === 0 || amount === null || amount === undefined) return '0.00'
  return Math.abs(amount).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

// 汇总方法
const getSummaries = (param) => {
  const { columns } = param
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    
    if (column.property === 'debitAmount') {
      sums[index] = formatAmount(totalDebit.value)
    } else if (column.property === 'creditAmount') {
      sums[index] = formatAmount(totalCredit.value)
    } else {
      sums[index] = ''
    }
  })
  return sums
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
  queryForm.voucherNo = ''
  queryForm.accountCode = ''
  queryForm.summary = ''
  queryForm.includeUnposted = false
}

// 导出
const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

// 刷新
const handleRefresh = () => {
  handleQuery()
}

// 切换分组模式
const toggleGroupByVoucher = () => {
  groupByVoucher.value = !groupByVoucher.value
  if (groupByVoucher.value) {
    groupDataByVoucher()
  } else {
    // 恢复平铺模式
    handleQuery()
  }
}

// 按凭证分组数据
const groupDataByVoucher = () => {
  const grouped = {}
  chronologicalData.value.forEach(item => {
    if (!grouped[item.voucherNo]) {
      grouped[item.voucherNo] = {
        voucherNo: item.voucherNo,
        date: item.date,
        status: item.status,
        hasChildren: true,
        entries: []
      }
    }
    grouped[item.voucherNo].entries.push(item)
  })
  chronologicalData.value = Object.values(grouped)
}

// 查看凭证
const viewVoucher = (row) => {
  ElMessage.info(`查看凭证 ${row.voucherNo}`)
}

onMounted(() => {
  // 初始化时加载数据
  handleQuery()
})
</script>

<style scoped lang="scss">
.chronological-book {
  .query-card {
    margin-bottom: 20px;
  }

  .table-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .header-actions {
        display: flex;
        gap: 10px;
      }
    }
  }

  .stats-card {
    .stat-item {
      text-align: center;
      padding: 15px;
      border-radius: 8px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;

      .stat-label {
        font-size: 12px;
        opacity: 0.9;
        margin-bottom: 5px;
      }

      .stat-value {
        font-size: 16px;
        font-weight: bold;
        
        &.positive {
          color: #67c23a;
        }
        
        &.negative {
          color: #f56c6c;
        }
      }
    }
  }

  .amount-text {
    font-weight: 500;
    
    &.positive {
      color: #409eff;
    }
    
    &.negative {
      color: #67c23a;
    }
  }

  .negative {
    color: #f56c6c;
  }

  :deep(.el-table) {
    .el-table__row {
      &:hover {
        background-color: #f5f7fa;
      }
    }
    
    .el-table__footer-wrapper {
      .el-table__footer {
        font-weight: bold;
        background-color: #f8f9fa;
      }
    }
  }
}
</style>