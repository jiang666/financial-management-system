<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">凭证审核</h2>
      <p class="description">审核待处理凭证，支持批量审核和驳回处理</p>
    </div>
    
    <!-- 搜索条件 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="凭证字号">
          <el-input v-model="searchForm.voucherNo" placeholder="请输入凭证字号" clearable />
        </el-form-item>
        <el-form-item label="制单人">
          <el-input v-model="searchForm.preparedBy" placeholder="请输入制单人" clearable />
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
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="待审核" value="pending" />
            <el-option label="已审核" value="approved" />
            <el-option label="已驳回" value="rejected" />
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
          <el-button type="success" icon="Check" @click="handleBatchApprove" :disabled="!selectedVouchers.length">
            批量审核
          </el-button>
          <el-button type="warning" icon="Close" @click="handleBatchReject" :disabled="!selectedVouchers.length">
            批量驳回
          </el-button>
          <el-button icon="Download" @click="handleExport">导出列表</el-button>
        </div>
        <div class="header-right">
          <el-statistic title="待审核凭证" :value="pendingCount" class="statistic-item" />
          <el-statistic title="今日已审核" :value="todayApprovedCount" class="statistic-item" />
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
        <el-table-column prop="totalAmount" label="金额" width="120" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="entryCount" label="分录数" width="80" align="center" />
        <el-table-column prop="summary" label="摘要" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="150" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">查看</el-button>
            <el-button 
              v-if="row.status === 'pending'" 
              link 
              type="success" 
              @click="handleApprove(row)"
            >
              审核
            </el-button>
            <el-button 
              v-if="row.status === 'pending'" 
              link 
              type="warning" 
              @click="handleReject(row)"
            >
              驳回
            </el-button>
            <el-button 
              v-if="row.status === 'approved'" 
              link 
              type="info" 
              @click="handleViewAuditLog(row)"
            >
              审核日志
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
        
        <!-- 凭证摘要 -->
        <el-card v-if="currentVoucher.remark" shadow="never" class="mb-lg">
          <template #header>
            <span>凭证摘要</span>
          </template>
          <p>{{ currentVoucher.remark }}</p>
        </el-card>
      </div>
      
      <template #footer>
        <el-button @click="detailDialog = false">关闭</el-button>
        <el-button 
          v-if="currentVoucher?.status === 'pending'" 
          type="success" 
          @click="handleApprove(currentVoucher)"
        >
          审核通过
        </el-button>
        <el-button 
          v-if="currentVoucher?.status === 'pending'" 
          type="warning" 
          @click="handleReject(currentVoucher)"
        >
          驳回
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 驳回原因对话框 -->
    <el-dialog v-model="rejectDialog" title="驳回原因" width="500px">
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef" label-width="100px">
        <el-form-item label="驳回原因" prop="reason">
          <el-select v-model="rejectForm.reason" placeholder="请选择驳回原因" style="width: 100%">
            <el-option label="科目使用错误" value="科目使用错误" />
            <el-option label="金额录入错误" value="金额录入错误" />
            <el-option label="摘要不清楚" value="摘要不清楚" />
            <el-option label="缺少附件" value="缺少附件" />
            <el-option label="借贷不平衡" value="借贷不平衡" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" prop="detail">
          <el-input 
            v-model="rejectForm.detail" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入详细的驳回说明..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialog = false">取消</el-button>
        <el-button type="warning" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
    
    <!-- 审核日志对话框 -->
    <el-dialog v-model="auditLogDialog" title="审核日志" width="700px">
      <el-timeline>
        <el-timeline-item
          v-for="log in auditLogs"
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
const rejectDialog = ref(false)
const auditLogDialog = ref(false)
const selectedVouchers = ref([])
const currentVoucher = ref(null)
const auditLogs = ref([])
const rejectFormRef = ref()

const searchForm = ref({
  voucherNo: '',
  preparedBy: '',
  dateRange: [],
  status: ''
})

const rejectForm = ref({
  reason: '',
  detail: ''
})

const rejectRules = {
  reason: [
    { required: true, message: '请选择驳回原因', trigger: 'change' }
  ],
  detail: [
    { required: true, message: '请输入详细说明', trigger: 'blur' }
  ]
}

const pagination = ref({
  current: 1,
  size: 20,
  total: 0
})

const tableData = ref([])

const pendingCount = computed(() => {
  return tableData.value.filter(item => item.status === 'pending').length
})

const todayApprovedCount = computed(() => {
  const today = new Date().toISOString().split('T')[0]
  return tableData.value.filter(item => 
    item.status === 'approved' && 
    item.auditTime?.includes(today)
  ).length
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
        totalAmount: 50000,
        entryCount: 2,
        summary: '销售商品收入',
        status: 'pending',
        submitTime: '2025-08-15 09:30:00',
        attachmentCount: 2,
        remark: '本月销售商品的收入确认',
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
        totalAmount: 30000,
        entryCount: 3,
        summary: '采购原材料',
        status: 'approved',
        submitTime: '2025-08-15 10:15:00',
        auditTime: '2025-08-15 11:00:00',
        attachmentCount: 1,
        remark: '采购生产用原材料',
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
        voucherNo: '记-20250815-003',
        voucherDate: '2025-08-15',
        preparedBy: '王五',
        totalAmount: 8000,
        entryCount: 2,
        summary: '报销差旅费',
        status: 'rejected',
        submitTime: '2025-08-15 14:20:00',
        auditTime: '2025-08-15 15:10:00',
        rejectReason: '缺少附件',
        rejectDetail: '缺少火车票和住宿发票',
        attachmentCount: 0,
        entries: [
          {
            accountCode: '6602',
            accountName: '差旅费',
            summary: '报销差旅费',
            auxiliaryText: '部门:销售部',
            debitAmount: 8000,
            creditAmount: 0
          },
          {
            accountCode: '1001',
            accountName: '库存现金',
            summary: '报销差旅费',
            auxiliaryText: '',
            debitAmount: 0,
            creditAmount: 8000
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
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'danger'
  }
  return typeMap[status] || ''
}

const getStatusText = (status) => {
  const textMap = {
    'pending': '待审核',
    'approved': '已审核',
    'rejected': '已驳回'
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
    preparedBy: '',
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

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要审核通过凭证"${row.voucherNo}"吗？`,
      '审核确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    
    row.status = 'approved'
    row.auditTime = new Date().toLocaleString()
    ElMessage.success('审核通过')
    detailDialog.value = false
  } catch {
    // 取消审核
  }
}

const handleReject = (row) => {
  currentVoucher.value = row
  rejectForm.value = {
    reason: '',
    detail: ''
  }
  rejectDialog.value = true
}

const confirmReject = async () => {
  const valid = await rejectFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  currentVoucher.value.status = 'rejected'
  currentVoucher.value.auditTime = new Date().toLocaleString()
  currentVoucher.value.rejectReason = rejectForm.value.reason
  currentVoucher.value.rejectDetail = rejectForm.value.detail
  
  rejectDialog.value = false
  detailDialog.value = false
  ElMessage.success('驳回成功')
}

const handleBatchApprove = async () => {
  const pendingVouchers = selectedVouchers.value.filter(v => v.status === 'pending')
  if (pendingVouchers.length === 0) {
    ElMessage.warning('请选择待审核的凭证')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要批量审核通过选中的${pendingVouchers.length}张凭证吗？`,
      '批量审核确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    
    pendingVouchers.forEach(voucher => {
      voucher.status = 'approved'
      voucher.auditTime = new Date().toLocaleString()
    })
    
    ElMessage.success(`批量审核成功，共处理${pendingVouchers.length}张凭证`)
  } catch {
    // 取消批量审核
  }
}

const handleBatchReject = () => {
  const pendingVouchers = selectedVouchers.value.filter(v => v.status === 'pending')
  if (pendingVouchers.length === 0) {
    ElMessage.warning('请选择待审核的凭证')
    return
  }
  
  ElMessage.info('批量驳回功能需要逐个处理驳回原因')
}

const handleViewAuditLog = (row) => {
  auditLogs.value = [
    {
      id: 1,
      action: '提交审核',
      operator: row.preparedBy,
      time: row.submitTime,
      type: 'primary',
      remark: '凭证制单完成，提交审核'
    }
  ]
  
  if (row.status === 'approved') {
    auditLogs.value.push({
      id: 2,
      action: '审核通过',
      operator: '审核员',
      time: row.auditTime,
      type: 'success',
      remark: '凭证审核通过'
    })
  } else if (row.status === 'rejected') {
    auditLogs.value.push({
      id: 2,
      action: '审核驳回',
      operator: '审核员',
      time: row.auditTime,
      type: 'danger',
      remark: `驳回原因：${row.rejectReason}。${row.rejectDetail}`
    })
  }
  
  auditLogDialog.value = true
}

const handleExport = () => {
  ElMessage.success('导出功能开发中')
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
  
  .search-form {
    .el-form-item {
      display: block;
      margin-bottom: 16px;
      
      .el-input,
      .el-select,
      .el-date-picker {
        width: 100% !important;
      }
    }
  }
}
</style>