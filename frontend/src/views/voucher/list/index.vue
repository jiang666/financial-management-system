<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">凭证信息列表</h2>
      <p class="description">查询、编辑和管理所有凭证信息</p>
    </div>
    
    <!-- 查询条件 -->
    <el-card class="query-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="凭证期间">
          <el-date-picker
            v-model="queryForm.period"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="凭证字号">
          <el-input
            v-model="queryForm.voucherNo"
            placeholder="输入凭证字号"
            style="width: 120px"
            clearable
          />
        </el-form-item>
        <el-form-item label="制单人">
          <el-input
            v-model="queryForm.creator"
            placeholder="输入制单人"
            style="width: 100px"
            clearable
          />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="queryForm.status" placeholder="请选择" style="width: 100px">
            <el-option label="全部" value="" />
            <el-option label="未审核" value="draft" />
            <el-option label="已审核" value="reviewed" />
            <el-option label="已记账" value="posted" />
            <el-option label="已冲销" value="reversed" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
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
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="action-card" shadow="never">
      <el-row>
        <el-col :span="24">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增凭证
          </el-button>
          <el-button type="success" @click="handleBatchReview" :disabled="!multipleSelection.length">
            <el-icon><CircleCheck /></el-icon>
            批量审核
          </el-button>
          <el-button type="info" @click="handleBatchPost" :disabled="!multipleSelection.length">
            <el-icon><Check /></el-icon>
            批量记账
          </el-button>
          <el-button type="warning" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出Excel
          </el-button>
          <el-button type="danger" @click="handleBatchDelete" :disabled="!multipleSelection.length">
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 凭证列表 -->
    <el-card class="table-card" shadow="never">
      <el-table
        ref="multipleTableRef"
        :data="voucherList"
        border
        stripe
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="voucherNo" label="凭证字号" width="120" align="center">
          <template #default="{ row }">
            <el-button type="primary" text @click="handleView(row)">
              {{ row.voucherNo }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="date" label="凭证日期" width="100" align="center" />
        <el-table-column prop="summary" label="摘要" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.summary" placement="top" v-if="row.summary && row.summary.length > 30">
              <span>{{ row.summary.substring(0, 30) }}...</span>
            </el-tooltip>
            <span v-else>{{ row.summary }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount-text">{{ formatAmount(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="entriesCount" label="分录数" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creator" label="制单人" width="100" align="center" />
        <el-table-column prop="createTime" label="制单时间" width="150" align="center" />
        <el-table-column prop="reviewer" label="审核人" width="100" align="center" />
        <el-table-column prop="reviewTime" label="审核时间" width="150" align="center" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button 
              type="success" 
              text 
              size="small" 
              @click="handleEdit(row)"
              v-if="row.status === 'draft'"
            >
              编辑
            </el-button>
            <el-button 
              type="warning" 
              text 
              size="small" 
              @click="handleReview(row)"
              v-if="row.status === 'draft'"
            >
              审核
            </el-button>
            <el-button 
              type="info" 
              text 
              size="small" 
              @click="handlePost(row)"
              v-if="row.status === 'reviewed'"
            >
              记账
            </el-button>
            <el-button 
              type="danger" 
              text 
              size="small" 
              @click="handleReverse(row)"
              v-if="row.status === 'posted'"
            >
              冲销
            </el-button>
            <el-button 
              type="danger" 
              text 
              size="small" 
              @click="handleDelete(row)"
              v-if="row.status === 'draft'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :small="false"
        :disabled="loading"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 凭证详情对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="80%"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div v-if="currentVoucher" class="voucher-detail">
        <!-- 凭证基本信息 -->
        <el-descriptions :column="3" border>
          <el-descriptions-item label="凭证字号">{{ currentVoucher.voucherNo }}</el-descriptions-item>
          <el-descriptions-item label="凭证日期">{{ currentVoucher.date }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentVoucher.status)" size="small">
              {{ getStatusText(currentVoucher.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="制单人">{{ currentVoucher.creator }}</el-descriptions-item>
          <el-descriptions-item label="制单时间">{{ currentVoucher.createTime }}</el-descriptions-item>
          <el-descriptions-item label="审核人">{{ currentVoucher.reviewer || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 分录明细 -->
        <div class="entries-section" style="margin-top: 20px;">
          <h4>分录明细</h4>
          <el-table :data="currentVoucher.entries" border stripe>
            <el-table-column prop="accountCode" label="科目编码" width="100" />
            <el-table-column prop="accountName" label="科目名称" width="150" />
            <el-table-column prop="summary" label="摘要" min-width="200" />
            <el-table-column prop="debitAmount" label="借方金额" width="120" align="right">
              <template #default="{ row }">
                <span v-if="row.debitAmount" class="amount-text">{{ formatAmount(row.debitAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="creditAmount" label="贷方金额" width="120" align="right">
              <template #default="{ row }">
                <span v-if="row.creditAmount" class="amount-text">{{ formatAmount(row.creditAmount) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button 
            type="primary" 
            @click="handleEdit(currentVoucher)" 
            v-if="currentVoucher && currentVoucher.status === 'draft'"
          >
            编辑凭证
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, CircleCheck, Check, Download, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const currentVoucher = ref(null)
const multipleTableRef = ref()
const multipleSelection = ref([])

// 查询表单
const queryForm = reactive({
  period: '2025-08',
  voucherNo: '',
  creator: '',
  status: '',
  dateRange: []
})

// 分页参数
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 凭证列表数据
const voucherList = ref([
  {
    id: 1,
    voucherNo: 'JZ001',
    date: '2025-08-01',
    summary: '收到某公司货款',
    totalAmount: 50000,
    entriesCount: 2,
    status: 'posted',
    creator: '张三',
    createTime: '2025-08-01 09:30:00',
    reviewer: '李四',
    reviewTime: '2025-08-01 10:15:00',
    entries: [
      {
        accountCode: '1001',
        accountName: '库存现金',
        summary: '收到某公司货款',
        debitAmount: 50000,
        creditAmount: null
      },
      {
        accountCode: '1122',
        accountName: '应收账款',
        summary: '收到某公司货款',
        debitAmount: null,
        creditAmount: 50000
      }
    ]
  },
  {
    id: 2,
    voucherNo: 'JZ002',
    date: '2025-08-02',
    summary: '银行转账收入',
    totalAmount: 100000,
    entriesCount: 2,
    status: 'reviewed',
    creator: '王五',
    createTime: '2025-08-02 14:20:00',
    reviewer: '李四',
    reviewTime: '2025-08-02 15:10:00',
    entries: [
      {
        accountCode: '1002',
        accountName: '银行存款',
        summary: '银行转账收入',
        debitAmount: 100000,
        creditAmount: null
      },
      {
        accountCode: '6001',
        accountName: '主营业务收入',
        summary: '银行转账收入',
        debitAmount: null,
        creditAmount: 100000
      }
    ]
  },
  {
    id: 3,
    voucherNo: 'JZ003',
    date: '2025-08-03',
    summary: '结转销售成本',
    totalAmount: 60000,
    entriesCount: 2,
    status: 'draft',
    creator: '赵六',
    createTime: '2025-08-03 11:45:00',
    reviewer: null,
    reviewTime: null,
    entries: [
      {
        accountCode: '5001',
        accountName: '主营业务成本',
        summary: '结转销售成本',
        debitAmount: 60000,
        creditAmount: null
      },
      {
        accountCode: '1401',
        accountName: '库存商品',
        summary: '结转销售成本',
        debitAmount: null,
        creditAmount: 60000
      }
    ]
  },
  {
    id: 4,
    voucherNo: 'JZ004',
    date: '2025-08-04',
    summary: '支付广告费',
    totalAmount: 5000,
    entriesCount: 2,
    status: 'draft',
    creator: '孙七',
    createTime: '2025-08-04 16:30:00',
    reviewer: null,
    reviewTime: null,
    entries: [
      {
        accountCode: '6602',
        accountName: '销售费用',
        summary: '支付广告费',
        debitAmount: 5000,
        creditAmount: null
      },
      {
        accountCode: '1001',
        accountName: '库存现金',
        summary: '支付广告费',
        debitAmount: null,
        creditAmount: 5000
      }
    ]
  },
  {
    id: 5,
    voucherNo: 'JZ005',
    date: '2025-08-05',
    summary: '采购办公用品',
    totalAmount: 8000,
    entriesCount: 2,
    status: 'reversed',
    creator: '周八',
    createTime: '2025-08-05 09:15:00',
    reviewer: '李四',
    reviewTime: '2025-08-05 10:00:00',
    entries: [
      {
        accountCode: '6601',
        accountName: '管理费用',
        summary: '采购办公用品',
        debitAmount: 8000,
        creditAmount: null
      },
      {
        accountCode: '1002',
        accountName: '银行存款',
        summary: '采购办公用品',
        debitAmount: null,
        creditAmount: 8000
      }
    ]
  },
  {
    id: 6,
    voucherNo: 'JZ006',
    date: '2025-08-06',
    summary: '支付员工工资',
    totalAmount: 120000,
    entriesCount: 3,
    status: 'posted',
    creator: '陈九',
    createTime: '2025-08-06 08:45:00',
    reviewer: '李四',
    reviewTime: '2025-08-06 09:30:00',
    entries: [
      {
        accountCode: '6701',
        accountName: '职工薪酬',
        summary: '支付员工工资',
        debitAmount: 120000,
        creditAmount: null
      },
      {
        accountCode: '1002',
        accountName: '银行存款',
        summary: '支付员工工资',
        debitAmount: null,
        creditAmount: 108000
      },
      {
        accountCode: '2221',
        accountName: '应交税费-个人所得税',
        summary: '代扣个人所得税',
        debitAmount: null,
        creditAmount: 12000
      }
    ]
  },
  {
    id: 7,
    voucherNo: 'JZ007',
    date: '2025-08-07',
    summary: '销售商品收入',
    totalAmount: 150000,
    entriesCount: 2,
    status: 'reviewed',
    creator: '刘十',
    createTime: '2025-08-07 13:20:00',
    reviewer: '张三',
    reviewTime: '2025-08-07 14:00:00',
    entries: [
      {
        accountCode: '1122',
        accountName: '应收账款',
        summary: '销售商品收入',
        debitAmount: 150000,
        creditAmount: null
      },
      {
        accountCode: '6001',
        accountName: '主营业务收入',
        summary: '销售商品收入',
        debitAmount: null,
        creditAmount: 150000
      }
    ]
  },
  {
    id: 8,
    voucherNo: 'JZ008',
    date: '2025-08-08',
    summary: '购买固定资产',
    totalAmount: 280000,
    entriesCount: 2,
    status: 'draft',
    creator: '吴一',
    createTime: '2025-08-08 10:15:00',
    reviewer: null,
    reviewTime: null,
    entries: [
      {
        accountCode: '1601',
        accountName: '固定资产',
        summary: '购买办公设备',
        debitAmount: 280000,
        creditAmount: null
      },
      {
        accountCode: '1002',
        accountName: '银行存款',
        summary: '支付设备款',
        debitAmount: null,
        creditAmount: 280000
      }
    ]
  },
  {
    id: 9,
    voucherNo: 'JZ009',
    date: '2025-08-09',
    summary: '收取客户预付款',
    totalAmount: 80000,
    entriesCount: 2,
    status: 'posted',
    creator: '郑二',
    createTime: '2025-08-09 15:30:00',
    reviewer: '王五',
    reviewTime: '2025-08-09 16:15:00',
    entries: [
      {
        accountCode: '1002',
        accountName: '银行存款',
        summary: '收取客户预付款',
        debitAmount: 80000,
        creditAmount: null
      },
      {
        accountCode: '2203',
        accountName: '预收账款',
        summary: '客户预付货款',
        debitAmount: null,
        creditAmount: 80000
      }
    ]
  },
  {
    id: 10,
    voucherNo: 'JZ010',
    date: '2025-08-10',
    summary: '支付水电费',
    totalAmount: 3500,
    entriesCount: 2,
    status: 'draft',
    creator: '冯三',
    createTime: '2025-08-10 11:00:00',
    reviewer: null,
    reviewTime: null,
    entries: [
      {
        accountCode: '6601',
        accountName: '管理费用',
        summary: '支付办公室水电费',
        debitAmount: 3500,
        creditAmount: null
      },
      {
        accountCode: '1001',
        accountName: '库存现金',
        summary: '支付水电费',
        debitAmount: null,
        creditAmount: 3500
      }
    ]
  },
  {
    id: 11,
    voucherNo: 'JZ011',
    date: '2025-08-11',
    summary: '计提折旧费用',
    totalAmount: 15000,
    entriesCount: 2,
    status: 'reviewed',
    creator: '褚四',
    createTime: '2025-08-11 09:45:00',
    reviewer: '赵六',
    reviewTime: '2025-08-11 10:30:00',
    entries: [
      {
        accountCode: '6601',
        accountName: '管理费用',
        summary: '计提固定资产折旧',
        debitAmount: 15000,
        creditAmount: null
      },
      {
        accountCode: '1602',
        accountName: '累计折旧',
        summary: '累计折旧',
        debitAmount: null,
        creditAmount: 15000
      }
    ]
  },
  {
    id: 12,
    voucherNo: 'JZ012',
    date: '2025-08-12',
    summary: '银行利息收入',
    totalAmount: 2800,
    entriesCount: 2,
    status: 'posted',
    creator: '卫五',
    createTime: '2025-08-12 16:20:00',
    reviewer: '孙七',
    reviewTime: '2025-08-12 17:00:00',
    entries: [
      {
        accountCode: '1002',
        accountName: '银行存款',
        summary: '银行利息收入',
        debitAmount: 2800,
        creditAmount: null
      },
      {
        accountCode: '6711',
        accountName: '利息收入',
        summary: '银行定期存款利息',
        debitAmount: null,
        creditAmount: 2800
      }
    ]
  }
])

// 更新分页总数
pagination.total = voucherList.value.length

// 格式化金额
const formatAmount = (amount) => {
  if (amount === 0 || amount === null || amount === undefined) return '0.00'
  return Math.abs(amount).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    'draft': 'info',
    'reviewed': 'warning', 
    'posted': 'success',
    'reversed': 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'draft': '未审核',
    'reviewed': '已审核',
    'posted': '已记账',
    'reversed': '已冲销'
  }
  return statusMap[status] || '未知'
}

// 多选处理
const handleSelectionChange = (val) => {
  multipleSelection.value = val
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

// 重置查询条件
const handleReset = () => {
  Object.assign(queryForm, {
    period: '2025-08',
    voucherNo: '',
    creator: '',
    status: '',
    dateRange: []
  })
}

// 分页处理
const handleSizeChange = (val) => {
  pagination.pageSize = val
  handleQuery()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  handleQuery()
}

// 新增凭证
const handleAdd = () => {
  ElMessage.info('跳转到凭证录入页面')
}

// 查看凭证详情
const handleView = (row) => {
  currentVoucher.value = row
  dialogTitle.value = `凭证详情 - ${row.voucherNo}`
  dialogVisible.value = true
}

// 编辑凭证
const handleEdit = (row) => {
  ElMessage.info(`编辑凭证 ${row.voucherNo}`)
  dialogVisible.value = false
}

// 删除凭证
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除凭证 ${row.voucherNo} 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    ElMessage.success('删除成功')
    handleQuery()
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 审核凭证
const handleReview = (row) => {
  ElMessageBox.confirm(
    `确定要审核凭证 ${row.voucherNo} 吗？`,
    '确认审核',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    row.status = 'reviewed'
    row.reviewer = '当前用户'
    row.reviewTime = new Date().toLocaleString()
    ElMessage.success('审核成功')
  })
}

// 记账
const handlePost = (row) => {
  ElMessageBox.confirm(
    `确定要对凭证 ${row.voucherNo} 进行记账吗？`,
    '确认记账',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    row.status = 'posted'
    ElMessage.success('记账成功')
  })
}

// 冲销凭证
const handleReverse = (row) => {
  ElMessageBox.confirm(
    `确定要冲销凭证 ${row.voucherNo} 吗？冲销后不可恢复！`,
    '确认冲销',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    row.status = 'reversed'
    ElMessage.success('冲销成功')
  })
}

// 批量操作
const handleBatchReview = () => {
  const count = multipleSelection.value.length
  ElMessage.success(`批量审核 ${count} 张凭证`)
}

const handleBatchPost = () => {
  const count = multipleSelection.value.length
  ElMessage.success(`批量记账 ${count} 张凭证`)
}

const handleBatchDelete = () => {
  const count = multipleSelection.value.length
  ElMessageBox.confirm(
    `确定要删除选中的 ${count} 张凭证吗？`,
    '确认批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    ElMessage.success(`批量删除 ${count} 张凭证成功`)
    multipleTableRef.value.clearSelection()
    handleQuery()
  })
}

const handleExport = () => {
  ElMessage.success('导出Excel功能开发中...')
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="scss">
.query-card, .action-card, .table-card {
  margin-bottom: 20px;
}

.action-card {
  padding: 10px 0;
}

.amount-text {
  font-weight: 500;
  color: #409eff;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.voucher-detail {
  .entries-section {
    h4 {
      margin-bottom: 10px;
      color: #333;
    }
  }
}

:deep(.el-table) {
  .el-table__row {
    &:hover {
      background-color: #f5f7fa;
    }
  }
}

:deep(.el-dialog__body) {
  padding: 20px;
}
</style>