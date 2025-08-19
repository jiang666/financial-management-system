<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">客户对账</h2>
      <p class="description">生成、发送和确认客户对账单，处理差异调整</p>
    </div>
    
    <el-tabs v-model="activeTab">
      <!-- 对账单生成 -->
      <el-tab-pane label="对账单生成" name="generate">
        <el-card>
          <el-form :model="generateForm" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="客户">
                  <el-select v-model="generateForm.customerId" placeholder="请选择客户" style="width: 100%">
                    <el-option label="ABC公司" value="1" />
                    <el-option label="XYZ集团" value="2" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="对账期间">
                  <el-date-picker
                    v-model="generateForm.period"
                    type="month"
                    placeholder="选择月份"
                    format="YYYY-MM"
                    value-format="YYYY-MM"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item>
              <el-button type="primary" @click="generateStatement">生成对账单</el-button>
              <el-button type="success" @click="previewStatement">预览</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <!-- 对账单预览 -->
        <el-card v-if="showPreview" class="mt-lg">
          <template #header>
            <div class="card-header">
              <span>客户对账单预览</span>
              <div class="header-actions">
                <el-button type="success" @click="sendStatement">发送对账单</el-button>
                <el-button @click="downloadStatement">下载PDF</el-button>
              </div>
            </div>
          </template>
          
          <div class="statement-preview">
            <div class="statement-header">
              <h3>客户对账单</h3>
              <p>对账期间：{{ generateForm.period }}</p>
              <p>客户名称：{{ currentCustomer.name }}</p>
            </div>
            
            <el-table :data="statementData" border>
              <el-table-column prop="date" label="日期" width="100" />
              <el-table-column prop="documentNo" label="单据号" width="120" />
              <el-table-column prop="summary" label="摘要" min-width="200" />
              <el-table-column prop="debit" label="借方" width="120" align="right">
                <template #default="{ row }">
                  {{ row.debit ? formatMoney(row.debit) : '' }}
                </template>
              </el-table-column>
              <el-table-column prop="credit" label="贷方" width="120" align="right">
                <template #default="{ row }">
                  {{ row.credit ? formatMoney(row.credit) : '' }}
                </template>
              </el-table-column>
              <el-table-column prop="balance" label="余额" width="120" align="right">
                <template #default="{ row }">
                  {{ formatMoney(row.balance) }}
                </template>
              </el-table-column>
            </el-table>
            
            <div class="statement-summary">
              <p><strong>期初余额：</strong>{{ formatMoney(statementSummary.beginBalance) }}</p>
              <p><strong>本期发生额：</strong>{{ formatMoney(statementSummary.currentAmount) }}</p>
              <p><strong>期末余额：</strong>{{ formatMoney(statementSummary.endBalance) }}</p>
            </div>
          </div>
        </el-card>
      </el-tab-pane>
      
      <!-- 对账确认 -->
      <el-tab-pane label="对账确认" name="confirm">
        <div class="search-form">
          <el-form :model="confirmSearchForm" inline>
            <el-form-item label="客户">
              <el-select v-model="confirmSearchForm.customerId" placeholder="请选择客户" clearable style="width: 150px">
                <el-option label="ABC公司" value="1" />
                <el-option label="XYZ集团" value="2" />
              </el-select>
            </el-form-item>
            <el-form-item label="确认状态">
              <el-select v-model="confirmSearchForm.status" placeholder="请选择" clearable style="width: 120px">
                <el-option label="已发送" value="sent" />
                <el-option label="已确认" value="confirmed" />
                <el-option label="有差异" value="disputed" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleConfirmSearch">搜索</el-button>
              <el-button icon="Refresh" @click="handleConfirmReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <el-table :data="paginatedConfirmData" v-loading="loading" style="width: 100%" stripe>
          <el-table-column prop="statementNo" label="对账单号" width="140" show-overflow-tooltip />
          <el-table-column prop="customerName" label="客户名称" min-width="130" show-overflow-tooltip />
          <el-table-column prop="period" label="对账期间" width="110" />
          <el-table-column prop="sendDate" label="发送日期" width="110" />
          <el-table-column prop="endBalance" label="期末余额" width="130" align="right">
            <template #default="{ row }">
              <span class="balance-amount">{{ formatMoney(row.endBalance) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="confirmDate" label="确认日期" width="110">
            <template #default="{ row }">
              <span v-if="row.confirmDate">{{ row.confirmDate }}</span>
              <el-tag v-else type="info" size="small">待确认</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getConfirmStatusType(row.status)" size="small">
                {{ getConfirmStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="difference" label="差异金额" width="130" align="right">
            <template #default="{ row }">
              <span v-if="row.difference" class="difference-amount">
                {{ formatMoney(row.difference) }}
              </span>
              <span v-else class="no-difference">¥0.00</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button link type="primary" size="small" @click="handleViewStatement(row)">查看</el-button>
                <el-button link type="success" size="small" @click="handleResend(row)" v-if="row.status === 'sent'">重发</el-button>
                <el-button link type="warning" size="small" @click="handleAdjustment(row)" v-if="row.status === 'disputed'">调整</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="confirmPagination.currentPage"
            v-model:page-size="confirmPagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="confirmData.length"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @size-change="handleConfirmSizeChange"
            @current-change="handleConfirmCurrentChange"
          />
        </div>
      </el-tab-pane>
      
      <!-- 差异处理 -->
      <el-tab-pane label="差异处理" name="difference">
        <div class="search-form">
          <el-form :model="differenceSearchForm" inline>
            <el-form-item label="处理状态">
              <el-select v-model="differenceSearchForm.status" placeholder="请选择" clearable style="width: 120px">
                <el-option label="待处理" value="pending" />
                <el-option label="已处理" value="resolved" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleDifferenceSearch">搜索</el-button>
              <el-button icon="Refresh" @click="handleDifferenceReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <el-table :data="differenceData" v-loading="loading">
          <el-table-column prop="customerName" label="客户名称" width="150" />
          <el-table-column prop="statementNo" label="对账单号" width="140" />
          <el-table-column prop="period" label="对账期间" width="100" />
          <el-table-column prop="ourBalance" label="我方余额" width="120" align="right">
            <template #default="{ row }">
              {{ formatMoney(row.ourBalance) }}
            </template>
          </el-table-column>
          <el-table-column prop="customerBalance" label="客户余额" width="120" align="right">
            <template #default="{ row }">
              {{ formatMoney(row.customerBalance) }}
            </template>
          </el-table-column>
          <el-table-column prop="difference" label="差异金额" width="120" align="right">
            <template #default="{ row }">
              <span class="difference-amount">{{ formatMoney(row.difference) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="差异原因" min-width="200" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'pending' ? 'warning' : 'success'" size="small">
                {{ row.status === 'pending' ? '待处理' : '已处理' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleProcessDifference(row)" v-if="row.status === 'pending'">处理</el-button>
              <el-button link type="info" @click="handleViewDifference(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 差异处理对话框 -->
    <el-dialog v-model="differenceDialog" title="差异处理" width="600px">
      <el-form :model="differenceForm" label-width="100px">
        <el-form-item label="差异金额">
          <el-input :value="formatMoney(currentDifference.difference)" disabled />
        </el-form-item>
        <el-form-item label="处理方式">
          <el-radio-group v-model="differenceForm.processType">
            <el-radio label="adjust">我方调整</el-radio>
            <el-radio label="negotiate">协商解决</el-radio>
            <el-radio label="ignore">暂时搁置</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="调整金额" v-if="differenceForm.processType === 'adjust'">
          <el-input-number v-model="differenceForm.adjustAmount" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input v-model="differenceForm.processRemark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="differenceDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitDifference">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('generate')
const loading = ref(false)
const showPreview = ref(false)
const differenceDialog = ref(false)

// 对账确认分页
const confirmPagination = ref({
  currentPage: 1,
  pageSize: 10
})

// 生成表单
const generateForm = ref({
  customerId: '',
  period: new Date().toISOString().slice(0, 7)
})

// 确认搜索表单
const confirmSearchForm = ref({
  customerId: '',
  status: ''
})

// 差异搜索表单
const differenceSearchForm = ref({
  status: ''
})

// 差异处理表单
const differenceForm = ref({
  processType: 'adjust',
  adjustAmount: 0,
  processRemark: ''
})

// 当前客户
const currentCustomer = ref({
  name: 'ABC公司'
})

// 对账单数据
const statementData = ref([
  {
    date: '2025-08-01',
    documentNo: 'INV20250001',
    summary: '销售发票',
    debit: 234000,
    credit: 0,
    balance: 234000
  },
  {
    date: '2025-08-05',
    documentNo: 'REC20250001',
    summary: '收款',
    debit: 0,
    credit: 150000,
    balance: 84000
  }
])

// 对账单汇总
const statementSummary = ref({
  beginBalance: 0,
  currentAmount: 234000,
  endBalance: 84000
})

// 确认数据
const confirmData = ref([
  {
    statementNo: 'ST20250001',
    customerName: 'ABC公司',
    period: '2025-08',
    sendDate: '2025-08-31',
    endBalance: 84000,
    confirmDate: '2025-09-02',
    status: 'confirmed',
    difference: 0
  },
  {
    statementNo: 'ST20250002',
    customerName: 'XYZ集团',
    period: '2025-08',
    sendDate: '2025-08-31',
    endBalance: 156000,
    confirmDate: '',
    status: 'disputed',
    difference: 12000
  },
  {
    statementNo: 'ST20250003',
    customerName: '华通科技',
    period: '2025-08',
    sendDate: '2025-08-31',
    endBalance: 98000,
    confirmDate: '2025-09-01',
    status: 'confirmed',
    difference: 0
  },
  {
    statementNo: 'ST20250004',
    customerName: '恒大商贸',
    period: '2025-08',
    sendDate: '2025-08-31',
    endBalance: 276000,
    confirmDate: '',
    status: 'sent',
    difference: 0
  },
  {
    statementNo: 'ST20250005',
    customerName: '远东实业',
    period: '2025-08',
    sendDate: '2025-08-31',
    endBalance: 342000,
    confirmDate: '2025-09-03',
    status: 'confirmed',
    difference: 0
  },
  {
    statementNo: 'ST20250006',
    customerName: '海联科技',
    period: '2025-08',
    sendDate: '2025-08-31',
    endBalance: 189000,
    confirmDate: '',
    status: 'disputed',
    difference: 8500
  },
  {
    statementNo: 'ST20250007',
    customerName: '新世纪集团',
    period: '2025-08',
    sendDate: '2025-08-31',
    endBalance: 425000,
    confirmDate: '',
    status: 'sent',
    difference: 0
  },
  {
    statementNo: 'ST20250008',
    customerName: '中科智能',
    period: '2025-08',
    sendDate: '2025-08-31',
    endBalance: 167000,
    confirmDate: '2025-09-02',
    status: 'confirmed',
    difference: 0
  },
  {
    statementNo: 'ST20250009',
    customerName: '东方贸易',
    period: '2025-08',
    sendDate: '2025-08-31',
    endBalance: 298000,
    confirmDate: '',
    status: 'sent',
    difference: 0
  },
  {
    statementNo: 'ST20250010',
    customerName: '北方物流',
    period: '2025-08',
    sendDate: '2025-08-31',
    endBalance: 134000,
    confirmDate: '',
    status: 'disputed',
    difference: 5600
  },
  {
    statementNo: 'ST20250011',
    customerName: '盛达电子',
    period: '2025-08',
    sendDate: '2025-08-31',
    endBalance: 387000,
    confirmDate: '2025-09-04',
    status: 'confirmed',
    difference: 0
  },
  {
    statementNo: 'ST20250012',
    customerName: '联创科技',
    period: '2025-08',
    sendDate: '2025-08-31',
    endBalance: 223000,
    confirmDate: '',
    status: 'sent',
    difference: 0
  }
])

// 分页计算
const paginatedConfirmData = computed(() => {
  const start = (confirmPagination.value.currentPage - 1) * confirmPagination.value.pageSize
  const end = start + confirmPagination.value.pageSize
  return confirmData.value.slice(start, end)
})

// 差异数据
const differenceData = ref([
  {
    customerName: 'XYZ集团',
    statementNo: 'ST20250002',
    period: '2025-08',
    ourBalance: 156000,
    customerBalance: 144000,
    difference: 12000,
    reason: '发票时间差异',
    status: 'pending'
  }
])

// 当前差异
const currentDifference = ref({})

const formatMoney = (amount) => {
  if (!amount) return '¥0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const getConfirmStatusType = (status) => {
  const map = {
    'sent': 'warning',
    'confirmed': 'success',
    'disputed': 'danger'
  }
  return map[status] || ''
}

const getConfirmStatusText = (status) => {
  const map = {
    'sent': '已发送',
    'confirmed': '已确认',
    'disputed': '有差异'
  }
  return map[status] || status
}

const generateStatement = () => {
  showPreview.value = true
  ElMessage.success('对账单生成成功')
}

const previewStatement = () => {
  showPreview.value = true
}

const sendStatement = () => {
  ElMessage.success('对账单已发送')
}

const downloadStatement = () => {
  ElMessage.success('PDF下载功能开发中')
}

const handleConfirmSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('查询完成')
  }, 500)
}

const handleConfirmReset = () => {
  confirmSearchForm.value = {
    customerId: '',
    status: ''
  }
}

const handleViewStatement = (row) => {
  ElMessage.info(`查看对账单：${row.statementNo}`)
}

const handleResend = (row) => {
  ElMessage.success(`重新发送对账单：${row.statementNo}`)
}

const handleAdjustment = (row) => {
  ElMessage.info(`差异调整：${row.statementNo}`)
}

const handleDifferenceSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('查询完成')
  }, 500)
}

const handleDifferenceReset = () => {
  differenceSearchForm.value = {
    status: ''
  }
}

const handleProcessDifference = (row) => {
  currentDifference.value = row
  differenceForm.value.adjustAmount = Math.abs(row.difference)
  differenceDialog.value = true
}

const handleViewDifference = (row) => {
  ElMessage.info(`查看差异详情：${row.statementNo}`)
}

const handleSubmitDifference = () => {
  currentDifference.value.status = 'resolved'
  differenceDialog.value = false
  ElMessage.success('差异处理完成')
}

// 分页处理
const handleConfirmSizeChange = (size) => {
  confirmPagination.value.pageSize = size
  confirmPagination.value.currentPage = 1
}

const handleConfirmCurrentChange = (page) => {
  confirmPagination.value.currentPage = page
}
</script>

<style scoped lang="scss">
.statement-preview {
  .statement-header {
    text-align: center;
    margin-bottom: 20px;
    
    h3 {
      margin-bottom: 10px;
      color: #303133;
    }
    
    p {
      margin: 5px 0;
      color: #606266;
    }
  }
  
  .statement-summary {
    margin-top: 20px;
    padding: 15px;
    background: #f5f7fa;
    border-radius: 4px;
    
    p {
      margin: 8px 0;
      font-size: 14px;
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

.difference-amount {
  color: #f56c6c;
  font-weight: 600;
}

.no-difference {
  color: #67c23a;
  font-weight: 600;
}

.balance-amount {
  color: #303133;
  font-weight: 600;
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

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.mt-lg {
  margin-top: 20px;
}

@media (max-width: 768px) {
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