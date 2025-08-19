<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">凭证查询</h2>
      <p class="description">高级搜索凭证信息，支持多条件组合查询和详情查看</p>
    </div>
    
    <!-- 高级搜索条件 -->
    <el-card class="search-card" shadow="never">
      <template #header>
        <div class="search-header">
          <span>查询条件</span>
          <el-button @click="toggleAdvanced" link>
            {{ showAdvanced ? '收起' : '展开' }}高级查询
            <el-icon><ArrowDown v-if="!showAdvanced" /><ArrowUp v-else /></el-icon>
          </el-button>
        </div>
      </template>
      
      <el-form :model="searchForm" inline>
        <!-- 基础查询条件 -->
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="凭证字号">
              <el-input v-model="searchForm.voucherNo" placeholder="请输入凭证字号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="凭证日期">
              <el-date-picker
                v-model="searchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="科目编码">
              <el-select v-model="searchForm.accountCode" placeholder="选择科目" clearable filterable style="width: 100%">
                <el-option
                  v-for="account in accountOptions"
                  :key="account.code"
                  :label="`${account.code} ${account.name}`"
                  :value="account.code"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 100%">
                <el-option label="草稿" value="draft" />
                <el-option label="待审核" value="pending" />
                <el-option label="已审核" value="approved" />
                <el-option label="已记账" value="posted" />
                <el-option label="已驳回" value="rejected" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 高级查询条件 -->
        <el-collapse-transition>
          <div v-if="showAdvanced">
            <el-row :gutter="16">
              <el-col :span="6">
                <el-form-item label="制单人">
                  <el-input v-model="searchForm.preparedBy" placeholder="请输入制单人" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="审核人">
                  <el-input v-model="searchForm.auditBy" placeholder="请输入审核人" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="金额范围">
                  <el-input v-model="searchForm.minAmount" placeholder="最小金额" style="width: 45%" />
                  <span style="width: 10%; text-align: center; display: inline-block">-</span>
                  <el-input v-model="searchForm.maxAmount" placeholder="最大金额" style="width: 45%" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="摘要">
                  <el-input v-model="searchForm.summary" placeholder="请输入摘要关键词" clearable />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="6">
                <el-form-item label="辅助核算">
                  <el-select v-model="searchForm.auxiliary" placeholder="选择辅助项" clearable style="width: 100%">
                    <el-option label="财务部" value="财务部" />
                    <el-option label="销售部" value="销售部" />
                    <el-option label="ABC公司" value="ABC公司" />
                    <el-option label="XYZ公司" value="XYZ公司" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="是否有附件">
                  <el-select v-model="searchForm.hasAttachment" placeholder="请选择" clearable style="width: 100%">
                    <el-option label="有附件" value="true" />
                    <el-option label="无附件" value="false" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="提交时间">
                  <el-date-picker
                    v-model="searchForm.submitTimeRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始时间"
                    end-placeholder="结束时间"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </el-collapse-transition>
        
        <el-row>
          <el-col :span="24">
            <el-form-item class="search-buttons">
              <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
              <el-button icon="Refresh" @click="handleReset">重置</el-button>
              <el-button icon="Download" @click="handleExport">导出</el-button>
              <el-button icon="Printer" @click="handlePrint" :disabled="!selectedVouchers.length">打印</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
    
    <!-- 查询结果 -->
    <div class="table-wrapper">
      <div class="table-header">
        <div class="result-info">
          <el-tag type="info">共找到 {{ pagination.total }} 张凭证</el-tag>
          <el-tag v-if="selectedVouchers.length" type="primary">
            已选择 {{ selectedVouchers.length }} 张
          </el-tag>
        </div>
        <div class="view-controls">
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button label="list">列表视图</el-radio-button>
            <el-radio-button label="card">卡片视图</el-radio-button>
          </el-radio-group>
        </div>
      </div>
      
      <!-- 列表视图 -->
      <el-table
        v-if="viewMode === 'list'"
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
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">查看</el-button>
            <el-button link type="info" @click="handlePrintSingle(row)">打印</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 卡片视图 -->
      <div v-else class="card-view">
        <el-row :gutter="16">
          <el-col :span="8" v-for="voucher in tableData" :key="voucher.id">
            <el-card class="voucher-card" shadow="hover" @click="handleViewDetail(voucher)">
              <template #header>
                <div class="card-header">
                  <span class="voucher-no">{{ voucher.voucherNo }}</span>
                  <el-tag :type="getStatusType(voucher.status)" size="small">
                    {{ getStatusText(voucher.status) }}
                  </el-tag>
                </div>
              </template>
              <div class="card-content">
                <p><strong>日期：</strong>{{ voucher.voucherDate }}</p>
                <p><strong>制单人：</strong>{{ voucher.preparedBy }}</p>
                <p><strong>金额：</strong>{{ formatMoney(voucher.totalAmount) }}</p>
                <p><strong>摘要：</strong>{{ voucher.summary }}</p>
                <div class="card-actions">
                  <el-button size="small" type="primary" @click.stop="handleViewDetail(voucher)">查看详情</el-button>
                  <el-button size="small" @click.stop="handlePrintSingle(voucher)">打印</el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
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
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentVoucher.status)">
              {{ getStatusText(currentVoucher.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="附件数量">{{ currentVoucher.attachmentCount }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentVoucher.submitTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核人">{{ currentVoucher.auditBy || '-' }}</el-descriptions-item>
          <el-descriptions-item label="记账时间">{{ currentVoucher.postTime || '-' }}</el-descriptions-item>
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
        <el-card v-if="currentVoucher.remark" shadow="never">
          <template #header>
            <span>凭证摘要</span>
          </template>
          <p>{{ currentVoucher.remark }}</p>
        </el-card>
      </div>
      
      <template #footer>
        <el-button @click="detailDialog = false">关闭</el-button>
        <el-button type="primary" @click="handlePrintSingle(currentVoucher)">打印</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { mockAccounts } from '@/api/mockData'

const loading = ref(false)
const detailDialog = ref(false)
const showAdvanced = ref(false)
const viewMode = ref('list')
const selectedVouchers = ref([])
const currentVoucher = ref(null)

const searchForm = ref({
  voucherNo: '',
  dateRange: [],
  accountCode: '',
  status: '',
  preparedBy: '',
  auditBy: '',
  minAmount: '',
  maxAmount: '',
  summary: '',
  auxiliary: '',
  hasAttachment: '',
  submitTimeRange: []
})

const pagination = ref({
  current: 1,
  size: 20,
  total: 0
})

const tableData = ref([])
const accountOptions = ref([])

onMounted(() => {
  loadAccountOptions()
  loadData()
})

const loadAccountOptions = () => {
  accountOptions.value = mockAccounts
}

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    // 模拟数据
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
        status: 'posted',
        submitTime: '2025-08-15 09:30:00',
        postTime: '2025-08-15 11:00:00',
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
        auditBy: '审核员',
        totalAmount: 30000,
        entryCount: 3,
        summary: '采购原材料',
        status: 'approved',
        submitTime: '2025-08-15 10:15:00',
        attachmentCount: 1,
        entries: []
      },
      {
        id: 3,
        voucherNo: '记-20250814-003',
        voucherDate: '2025-08-14',
        preparedBy: '王五',
        totalAmount: 8000,
        entryCount: 2,
        summary: '报销差旅费',
        status: 'rejected',
        submitTime: '2025-08-14 14:20:00',
        attachmentCount: 0,
        entries: []
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
    'draft': '',
    'pending': 'warning',
    'approved': 'primary',
    'posted': 'success',
    'rejected': 'danger'
  }
  return typeMap[status] || ''
}

const getStatusText = (status) => {
  const textMap = {
    'draft': '草稿',
    'pending': '待审核',
    'approved': '已审核',
    'posted': '已记账',
    'rejected': '已驳回'
  }
  return textMap[status] || status
}

const toggleAdvanced = () => {
  showAdvanced.value = !showAdvanced.value
}

const handleSearch = () => {
  pagination.value.current = 1
  loadData()
  ElMessage.success('搜索完成')
}

const handleReset = () => {
  searchForm.value = {
    voucherNo: '',
    dateRange: [],
    accountCode: '',
    status: '',
    preparedBy: '',
    auditBy: '',
    minAmount: '',
    maxAmount: '',
    summary: '',
    auxiliary: '',
    hasAttachment: '',
    submitTimeRange: []
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

const handleExport = () => {
  ElMessage.success('导出功能开发中')
}

const handlePrint = () => {
  if (selectedVouchers.value.length === 0) {
    ElMessage.warning('请先选择要打印的凭证')
    return
  }
  ElMessage.success(`准备打印${selectedVouchers.value.length}张凭证`)
}

const handlePrintSingle = (row) => {
  ElMessage.success(`准备打印凭证：${row.voucherNo}`)
}
</script>

<style scoped lang="scss">
.search-card {
  margin-bottom: 20px;
  
  .search-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    span {
      font-size: 16px;
      font-weight: 600;
    }
  }
  
  .search-buttons {
    text-align: center;
    margin-bottom: 0;
  }
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  .result-info {
    display: flex;
    gap: 8px;
  }
}

.card-view {
  margin-bottom: 20px;
  
  .voucher-card {
    margin-bottom: 16px;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .voucher-no {
        font-weight: 600;
        color: #303133;
      }
    }
    
    .card-content {
      p {
        margin: 8px 0;
        color: #606266;
        
        strong {
          color: #303133;
        }
      }
      
      .card-actions {
        margin-top: 16px;
        text-align: right;
      }
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
  .search-card {
    .el-col {
      span: 24 !important;
      margin-bottom: 16px;
    }
  }
  
  .table-header {
    flex-direction: column;
    gap: 16px;
    
    .result-info,
    .view-controls {
      width: 100%;
      justify-content: center;
    }
  }
  
  .card-view {
    .el-col {
      span: 24 !important;
    }
  }
}
</style>