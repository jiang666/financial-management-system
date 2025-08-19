<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">凭证记账</h2>
      <p class="description">处理已审核凭证的记账，支持批量记账和记账检查</p>
    </div>
    
    <!-- 搜索条件 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="凭证字号">
          <el-input v-model="searchForm.voucherNo" placeholder="请输入凭证字号" clearable />
        </el-form-item>
        <el-form-item label="凭证日期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="记账状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="待记账" value="approved" />
            <el-option label="已记账" value="posted" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 凭证列表 -->
    <div class="table-wrapper">
      <div class="table-header">
        <div class="header-left">
          <el-button type="primary" icon="Check" @click="handleBatchPost" :disabled="!selectedVouchers.length">
            批量记账
          </el-button>
          <el-button icon="Document" @click="handlePreCheck" :disabled="!selectedVouchers.length">
            记账检查
          </el-button>
          <el-button type="danger" icon="RefreshLeft" @click="handleBatchUnpost" :disabled="!canUnpost">
            反记账
          </el-button>
        </div>
        <div class="header-right">
          <el-statistic title="待记账凭证" :value="pendingCount" class="statistic-item" />
          <el-statistic title="今日已记账" :value="todayPostedCount" class="statistic-item" />
        </div>
      </div>
      
      <el-table
        :data="tableData"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
        row-key="id"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="voucherNo" label="凭证字号" width="140" />
        <el-table-column prop="voucherDate" label="凭证日期" width="120" />
        <el-table-column prop="preparedBy" label="制单人" width="100" />
        <el-table-column prop="auditBy" label="审核人" width="100" />
        <el-table-column prop="totalAmount" label="金额" width="120" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="entryCount" label="分录数" width="80" align="center" />
        <el-table-column prop="summary" label="摘要" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="postTime" label="记账时间" width="150" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">查看</el-button>
            <el-button 
              v-if="row.status === 'approved'" 
              link 
              type="success" 
              @click="handlePost(row)"
            >
              记账
            </el-button>
            <el-button 
              v-if="row.status === 'posted'" 
              link 
              type="warning" 
              @click="handleUnpost(row)"
            >
              反记账
            </el-button>
            <el-button 
              v-if="row.status === 'posted'" 
              link 
              type="info" 
              @click="handleViewPostLog(row)"
            >
              记账日志
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>
    
    <!-- 凭证详情对话框 -->
    <el-dialog v-model="detailDialog" title="凭证详情" width="90%" top="5vh">
      <div v-if="currentVoucher" class="voucher-detail">
        <!-- 凭证基本信息 -->
        <el-descriptions :column="4" border class="mb-lg">
          <el-descriptions-item label="凭证字号">{{ currentVoucher.voucherNo }}</el-descriptions-item>
          <el-descriptions-item label="凭证日期">{{ currentVoucher.voucherDate }}</el-descriptions-item>
          <el-descriptions-item label="制单人">{{ currentVoucher.preparedBy }}</el-descriptions-item>
          <el-descriptions-item label="审核人">{{ currentVoucher.auditBy }}</el-descriptions-item>
          <el-descriptions-item label="记账状态">
            <el-tag :type="getStatusType(currentVoucher.status)">
              {{ getStatusText(currentVoucher.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="记账时间">{{ currentVoucher.postTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="记账人">{{ currentVoucher.postBy || '-' }}</el-descriptions-item>
          <el-descriptions-item label="附件数量">{{ currentVoucher.attachmentCount }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 分录明细 -->
        <el-table :data="currentVoucher.entries" border class="mb-lg">
          <el-table-column label="序号" width="80" align="center">
            <template #default="{ $index }">{{ $index + 1 }}</template>
          </el-table-column>
          <el-table-column prop="accountCode" label="科目编码" width="120" />
          <el-table-column prop="accountName" label="科目名称" min-width="160" />
          <el-table-column prop="summary" label="摘要" min-width="200" />
          <el-table-column prop="auxiliaryText" label="辅助核算" width="150" />
          <el-table-column prop="debitAmount" label="借方金额" width="120" align="right">
            <template #default="{ row }">
              {{ row.debitAmount ? formatMoney(row.debitAmount) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="creditAmount" label="贷方金额" width="120" align="right">
            <template #default="{ row }">
              {{ row.creditAmount ? formatMoney(row.creditAmount) : '-' }}
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <template #footer>
        <el-button @click="detailDialog = false">关闭</el-button>
        <el-button 
          v-if="currentVoucher?.status === 'approved'" 
          type="primary" 
          @click="handlePost(currentVoucher)"
        >
          记账
        </el-button>
        <el-button 
          v-if="currentVoucher?.status === 'posted'" 
          type="warning" 
          @click="handleUnpost(currentVoucher)"
        >
          反记账
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 记账检查对话框 -->
    <el-dialog v-model="checkDialog" title="记账检查" width="600px">
      <div class="check-result">
        <el-alert
          :title="checkResult.hasError ? '检查发现问题' : '检查通过'"
          :type="checkResult.hasError ? 'error' : 'success'"
          show-icon
          :closable="false"
        />
        
        <div v-if="checkResult.items && checkResult.items.length > 0" class="mt-lg">
          <h4>检查结果：</h4>
          <ul class="check-list">
            <li v-for="item in checkResult.items" :key="item.id" :class="item.type">
              <el-icon v-if="item.type === 'error'"><CircleClose /></el-icon>
              <el-icon v-else-if="item.type === 'warning'"><Warning /></el-icon>
              <el-icon v-else><CircleCheck /></el-icon>
              <span>{{ item.message }}</span>
            </li>
          </ul>
        </div>
      </div>
      <template #footer>
        <el-button @click="checkDialog = false">关闭</el-button>
        <el-button 
          v-if="!checkResult.hasError" 
          type="primary" 
          @click="proceedWithPosting"
        >
          继续记账
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 记账日志对话框 -->
    <el-dialog v-model="postLogDialog" title="记账日志" width="700px">
      <el-timeline>
        <el-timeline-item
          v-for="log in postLogs"
          :key="log.id"
          :timestamp="log.time"
          :type="log.type"
        >
          <h4>{{ log.action }}</h4>
          <p>操作人：{{ log.operator }}</p>
          <p v-if="log.remark">备注：{{ log.remark }}</p>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const detailDialog = ref(false)
const checkDialog = ref(false)
const postLogDialog = ref(false)
const selectedVouchers = ref([])
const currentVoucher = ref(null)
const postLogs = ref([])

const searchForm = ref({
  voucherNo: '',
  dateRange: [],
  status: ''
})

const checkResult = ref({
  hasError: false,
  items: []
})

const pagination = ref({
  current: 1,
  size: 20,
  total: 0
})

const tableData = ref([])

const pendingCount = computed(() => {
  return tableData.value.filter(item => item.status === 'approved').length
})

const todayPostedCount = computed(() => {
  const today = new Date().toISOString().split('T')[0]
  return tableData.value.filter(item => 
    item.status === 'posted' && 
    item.postTime?.includes(today)
  ).length
})

const canUnpost = computed(() => {
  return selectedVouchers.value.some(v => v.status === 'posted')
})

onMounted(() => {
  loadData()
})

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    tableData.value = [
      {
        id: 1,
        voucherNo: '记-20250815-001',
        voucherDate: '2025-08-15',
        preparedBy: '张三',
        auditBy: '审核员',
        totalAmount: 50000,
        entryCount: 2,
        summary: '销售商品收入',
        status: 'approved',
        attachmentCount: 2,
        entries: [
          {
            accountCode: '1122',
            accountName: '应收账款',
            summary: '销售商品收入',
            auxiliaryText: '客户:ABC公司',
            debitAmount: 50000,
            creditAmount: 0
          },
          {
            accountCode: '6001',
            accountName: '主营业务收入',
            summary: '销售商品收入',
            auxiliaryText: '',
            debitAmount: 0,
            creditAmount: 50000
          }
        ]
      },
      {
        id: 2,
        voucherNo: '记-20250815-002',
        voucherDate: '2025-08-15',
        preparedBy: '李四',
        auditBy: '审核员',
        postBy: '记账员',
        totalAmount: 30000,
        entryCount: 3,
        summary: '采购原材料',
        status: 'posted',
        postTime: '2025-08-15 11:30:00',
        attachmentCount: 1,
        entries: [
          {
            accountCode: '1403',
            accountName: '原材料',
            summary: '采购原材料',
            auxiliaryText: '',
            debitAmount: 26548,
            creditAmount: 0
          },
          {
            accountCode: '2221',
            accountName: '应交税费-增值税-进项税额',
            summary: '采购原材料',
            auxiliaryText: '',
            debitAmount: 3452,
            creditAmount: 0
          },
          {
            accountCode: '2202',
            accountName: '应付账款',
            summary: '采购原材料',
            auxiliaryText: '供应商:材料供应商A',
            debitAmount: 0,
            creditAmount: 30000
          }
        ]
      },
      {
        id: 3,
        voucherNo: '记-20250814-003',
        voucherDate: '2025-08-14',
        preparedBy: '王五',
        auditBy: '审核员',
        totalAmount: 25000,
        entryCount: 2,
        summary: '收到客户货款',
        status: 'approved',
        attachmentCount: 1,
        entries: [
          {
            accountCode: '1002',
            accountName: '银行存款',
            summary: '收到客户货款',
            auxiliaryText: '',
            debitAmount: 25000,
            creditAmount: 0
          },
          {
            accountCode: '1122',
            accountName: '应收账款',
            summary: '收到客户货款',
            auxiliaryText: '客户:XYZ公司',
            debitAmount: 0,
            creditAmount: 25000
          }
        ]
      }
    ]
    pagination.value.total = tableData.value.length
    loading.value = false
  }, 500)
}

const formatMoney = (amount) => {
  if (!amount) return '0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const getStatusType = (status) => {
  const typeMap = {
    'approved': 'warning',
    'posted': 'success'
  }
  return typeMap[status] || ''
}

const getStatusText = (status) => {
  const textMap = {
    'approved': '待记账',
    'posted': '已记账'
  }
  return textMap[status] || status
}

const handleSearch = () => {
  loadData()
  ElMessage.success('搜索完成')
}

const handleReset = () => {
  searchForm.value = {
    voucherNo: '',
    dateRange: [],
    status: ''
  }
  loadData()
}

const handleSelectionChange = (selection) => {
  selectedVouchers.value = selection
}

const handleRowClick = (row) => {
  handleViewDetail(row)
}

const handleViewDetail = (row) => {
  currentVoucher.value = row
  detailDialog.value = true
}

const handlePost = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要记账凭证"${row.voucherNo}"吗？记账后将无法修改！`,
      '记账确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    row.status = 'posted'
    row.postTime = new Date().toLocaleString()
    row.postBy = '记账员'
    ElMessage.success('记账成功')
    detailDialog.value = false
  } catch {
    // 取消记账
  }
}

const handleUnpost = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要反记账凭证"${row.voucherNo}"吗？`,
      '反记账确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    row.status = 'approved'
    row.postTime = ''
    row.postBy = ''
    ElMessage.success('反记账成功')
    detailDialog.value = false
  } catch {
    // 取消反记账
  }
}

const handleBatchPost = async () => {
  const pendingVouchers = selectedVouchers.value.filter(v => v.status === 'approved')
  if (pendingVouchers.length === 0) {
    ElMessage.warning('请选择待记账的凭证')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要批量记账选中的${pendingVouchers.length}张凭证吗？`,
      '批量记账确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    pendingVouchers.forEach(voucher => {
      voucher.status = 'posted'
      voucher.postTime = new Date().toLocaleString()
      voucher.postBy = '记账员'
    })
    
    ElMessage.success(`批量记账成功，共处理${pendingVouchers.length}张凭证`)
  } catch {
    // 取消批量记账
  }
}

const handleBatchUnpost = async () => {
  const postedVouchers = selectedVouchers.value.filter(v => v.status === 'posted')
  if (postedVouchers.length === 0) {
    ElMessage.warning('请选择已记账的凭证')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要批量反记账选中的${postedVouchers.length}张凭证吗？`,
      '批量反记账确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    postedVouchers.forEach(voucher => {
      voucher.status = 'approved'
      voucher.postTime = ''
      voucher.postBy = ''
    })
    
    ElMessage.success(`批量反记账成功，共处理${postedVouchers.length}张凭证`)
  } catch {
    // 取消批量反记账
  }
}

const handlePreCheck = () => {
  if (selectedVouchers.value.length === 0) {
    ElMessage.warning('请先选择要检查的凭证')
    return
  }
  
  // 模拟检查逻辑
  const pendingVouchers = selectedVouchers.value.filter(v => v.status === 'approved')
  const errors = []
  const warnings = []
  const success = []
  
  pendingVouchers.forEach(voucher => {
    // 检查借贷平衡
    const totalDebit = voucher.entries.reduce((sum, entry) => sum + (entry.debitAmount || 0), 0)
    const totalCredit = voucher.entries.reduce((sum, entry) => sum + (entry.creditAmount || 0), 0)
    
    if (Math.abs(totalDebit - totalCredit) > 0.01) {
      errors.push({
        id: Date.now() + Math.random(),
        type: 'error',
        message: `凭证 ${voucher.voucherNo} 借贷不平衡`
      })
    } else {
      success.push({
        id: Date.now() + Math.random(),
        type: 'success',
        message: `凭证 ${voucher.voucherNo} 检查通过`
      })
    }
    
    // 检查科目使用
    voucher.entries.forEach(entry => {
      if (!entry.accountCode || !entry.accountName) {
        errors.push({
          id: Date.now() + Math.random(),
          type: 'error',
          message: `凭证 ${voucher.voucherNo} 存在空科目`
        })
      }
    })
    
    // 检查摘要
    const emptySummary = voucher.entries.some(entry => !entry.summary)
    if (emptySummary) {
      warnings.push({
        id: Date.now() + Math.random(),
        type: 'warning',
        message: `凭证 ${voucher.voucherNo} 存在空摘要`
      })
    }
  })
  
  checkResult.value = {
    hasError: errors.length > 0,
    items: [...errors, ...warnings, ...success]
  }
  
  checkDialog.value = true
}

const proceedWithPosting = () => {
  checkDialog.value = false
  handleBatchPost()
}

const handleViewPostLog = (row) => {
  postLogs.value = [
    {
      id: 1,
      action: '审核通过',
      operator: row.auditBy,
      time: '2025-08-15 11:00:00',
      type: 'success',
      remark: '凭证审核通过，准备记账'
    },
    {
      id: 2,
      action: '记账',
      operator: row.postBy,
      time: row.postTime,
      type: 'primary',
      remark: '凭证记账完成'
    }
  ]
  
  postLogDialog.value = true
}
</script>

<style scoped lang="scss">
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  .header-left {
    display: flex;
    gap: 8px;
  }
  
  .header-right {
    display: flex;
    gap: 24px;
    
    .statistic-item {
      min-width: 120px;
      text-align: center;
    }
  }
}

.voucher-detail {
  .el-descriptions {
    margin-bottom: 20px;
  }
  
  .el-table {
    margin-bottom: 20px;
  }
}

.check-result {
  .check-list {
    list-style: none;
    padding: 0;
    
    li {
      display: flex;
      align-items: center;
      margin-bottom: 8px;
      padding: 8px;
      border-radius: 4px;
      
      .el-icon {
        margin-right: 8px;
      }
      
      &.error {
        background: #fef0f0;
        color: #f56c6c;
      }
      
      &.warning {
        background: #fdf6ec;
        color: #e6a23c;
      }
      
      &.success {
        background: #f0f9ff;
        color: #67c23a;
      }
    }
  }
}

@media (max-width: 768px) {
  .table-header {
    flex-direction: column;
    gap: 16px;
    
    .header-left,
    .header-right {
      width: 100%;
      justify-content: center;
    }
    
    .header-right {
      .statistic-item {
        flex: 1;
      }
    }
  }
}
</style>