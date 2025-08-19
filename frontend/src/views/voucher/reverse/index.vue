<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">凭证冲销</h2>
      <p class="description">对已记账凭证进行红字冲销、蓝字冲销或部分冲销处理</p>
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
        <el-form-item label="制单人">
          <el-input v-model="searchForm.preparedBy" placeholder="请输入制单人" clearable />
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
        <el-alert
          title="注意：只有已记账的凭证才能进行冲销操作"
          type="info"
          show-icon
          :closable="false"
        />
      </div>
      
      <el-table
        :data="tableData"
        v-loading="loading"
        @row-click="handleRowClick"
        row-key="id"
      >
        <el-table-column prop="voucherNo" label="凭证字号" width="140" />
        <el-table-column prop="voucherDate" label="凭证日期" width="120" />
        <el-table-column prop="preparedBy" label="制单人" width="100" />
        <el-table-column prop="totalAmount" label="金额" width="120" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="summary" label="摘要" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reverseStatus" label="冲销状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.reverseStatus" :type="getReverseStatusType(row.reverseStatus)">
              {{ getReverseStatusText(row.reverseStatus) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">查看</el-button>
            <el-dropdown 
              v-if="row.status === 'posted' && !row.reverseStatus"
              trigger="click"
              @command="(command) => handleReverseAction(command, row)"
            >
              <el-button link type="warning">
                冲销<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="red">红字冲销</el-dropdown-item>
                  <el-dropdown-item command="blue">蓝字冲销</el-dropdown-item>
                  <el-dropdown-item command="partial">部分冲销</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button 
              v-if="row.reverseVoucherNo" 
              link 
              type="info" 
              @click="viewReverseVoucher(row.reverseVoucherNo)"
            >
              查看冲销凭证
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
        <el-descriptions :column="4" border class="mb-lg">
          <el-descriptions-item label="凭证字号">{{ currentVoucher.voucherNo }}</el-descriptions-item>
          <el-descriptions-item label="凭证日期">{{ currentVoucher.voucherDate }}</el-descriptions-item>
          <el-descriptions-item label="制单人">{{ currentVoucher.preparedBy }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentVoucher.status)">
              {{ getStatusText(currentVoucher.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <el-table :data="currentVoucher.entries" border>
          <el-table-column label="序号" width="80" align="center">
            <template #default="{ $index }">{{ $index + 1 }}</template>
          </el-table-column>
          <el-table-column prop="accountCode" label="科目编码" width="120" />
          <el-table-column prop="accountName" label="科目名称" min-width="160" />
          <el-table-column prop="summary" label="摘要" min-width="200" />
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
      </template>
    </el-dialog>
    
    <!-- 冲销对话框 -->
    <el-dialog v-model="reverseDialog" :title="reverseDialogTitle" width="800px">
      <el-form :model="reverseForm" :rules="reverseRules" ref="reverseFormRef" label-width="100px">
        <el-form-item label="冲销类型">
          <el-radio-group v-model="reverseForm.type" @change="handleReverseTypeChange">
            <el-radio label="red">红字冲销</el-radio>
            <el-radio label="blue">蓝字冲销</el-radio>
            <el-radio label="partial">部分冲销</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="冲销日期" prop="reverseDate">
          <el-date-picker
            v-model="reverseForm.reverseDate"
            type="date"
            placeholder="选择冲销日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item label="冲销原因" prop="reason">
          <el-input 
            v-model="reverseForm.reason" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入冲销原因..."
          />
        </el-form-item>
        
        <!-- 部分冲销金额设置 -->
        <el-form-item v-if="reverseForm.type === 'partial'" label="冲销明细">
          <el-table :data="partialReverseEntries" border>
            <el-table-column prop="accountName" label="科目名称" />
            <el-table-column prop="originalAmount" label="原始金额" width="120" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.originalAmount) }}
              </template>
            </el-table-column>
            <el-table-column label="冲销金额" width="150">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.reverseAmount"
                  :min="0"
                  :max="row.originalAmount"
                  :precision="2"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="reverseDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmReverse">确认冲销</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const detailDialog = ref(false)
const reverseDialog = ref(false)
const currentVoucher = ref(null)
const reverseFormRef = ref()

const searchForm = ref({
  voucherNo: '',
  dateRange: [],
  preparedBy: ''
})

const reverseForm = ref({
  type: 'red',
  reverseDate: '',
  reason: ''
})

const reverseRules = {
  reverseDate: [
    { required: true, message: '请选择冲销日期', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入冲销原因', trigger: 'blur' }
  ]
}

const pagination = ref({
  current: 1,
  size: 20,
  total: 0
})

const tableData = ref([])
const partialReverseEntries = ref([])

const reverseDialogTitle = computed(() => {
  const typeMap = {
    'red': '红字冲销',
    'blue': '蓝字冲销',
    'partial': '部分冲销'
  }
  return typeMap[reverseForm.value.type] || '凭证冲销'
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
        summary: '销售商品收入',
        status: 'posted',
        reverseStatus: '',
        reverseVoucherNo: '',
        entries: [
          {
            accountCode: '1122',
            accountName: '应收账款',
            summary: '销售商品收入',
            debitAmount: 50000,
            creditAmount: 0
          },
          {
            accountCode: '6001',
            accountName: '主营业务收入',
            summary: '销售商品收入',
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
        summary: '采购原材料',
        status: 'posted',
        reverseStatus: 'red_reversed',
        reverseVoucherNo: '记-20250816-001',
        entries: []
      },
      {
        id: 3,
        voucherNo: '记-20250814-003',
        voucherDate: '2025-08-14',
        preparedBy: '王五',
        totalAmount: 8000,
        summary: '报销差旅费',
        status: 'posted',
        reverseStatus: 'partial_reversed',
        reverseVoucherNo: '记-20250816-002',
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
    'posted': 'success',
    'approved': 'primary'
  }
  return typeMap[status] || ''
}

const getStatusText = (status) => {
  const textMap = {
    'posted': '已记账',
    'approved': '已审核'
  }
  return textMap[status] || status
}

const getReverseStatusType = (status) => {
  const typeMap = {
    'red_reversed': 'danger',
    'blue_reversed': 'primary',
    'partial_reversed': 'warning'
  }
  return typeMap[status] || ''
}

const getReverseStatusText = (status) => {
  const textMap = {
    'red_reversed': '红字冲销',
    'blue_reversed': '蓝字冲销',
    'partial_reversed': '部分冲销'
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
    preparedBy: ''
  }
  loadData()
}

const handleRowClick = (row) => {
  handleViewDetail(row)
}

const handleViewDetail = (row) => {
  currentVoucher.value = row
  detailDialog.value = true
}

const handleReverseAction = (command, row) => {
  currentVoucher.value = row
  reverseForm.value = {
    type: command,
    reverseDate: new Date().toISOString().split('T')[0],
    reason: ''
  }
  
  if (command === 'partial') {
    partialReverseEntries.value = row.entries.map(entry => ({
      ...entry,
      originalAmount: entry.debitAmount || entry.creditAmount,
      reverseAmount: 0
    }))
  }
  
  reverseDialog.value = true
}

const handleReverseTypeChange = () => {
  if (reverseForm.value.type === 'partial' && currentVoucher.value) {
    partialReverseEntries.value = currentVoucher.value.entries.map(entry => ({
      ...entry,
      originalAmount: entry.debitAmount || entry.creditAmount,
      reverseAmount: 0
    }))
  }
}

const confirmReverse = async () => {
  const valid = await reverseFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  const typeText = {
    'red': '红字冲销',
    'blue': '蓝字冲销',
    'partial': '部分冲销'
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要对凭证"${currentVoucher.value.voucherNo}"进行${typeText[reverseForm.value.type]}吗？`,
      '冲销确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 模拟冲销处理
    const reverseStatusMap = {
      'red': 'red_reversed',
      'blue': 'blue_reversed',
      'partial': 'partial_reversed'
    }
    
    currentVoucher.value.reverseStatus = reverseStatusMap[reverseForm.value.type]
    currentVoucher.value.reverseVoucherNo = `记-${new Date().toISOString().slice(0, 10).replace(/-/g, '')}-${String(Math.floor(Math.random() * 999) + 1).padStart(3, '0')}`
    
    reverseDialog.value = false
    ElMessage.success(`${typeText[reverseForm.value.type]}成功，生成冲销凭证：${currentVoucher.value.reverseVoucherNo}`)
  } catch {
    // 取消冲销
  }
}

const viewReverseVoucher = (voucherNo) => {
  ElMessage.info(`查看冲销凭证：${voucherNo}`)
}
</script>

<style scoped lang="scss">
.table-header {
  margin-bottom: 16px;
}

.voucher-detail {
  .el-descriptions {
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .search-form {
    .el-form-item {
      display: block;
      margin-bottom: 16px;
      
      .el-input,
      .el-date-picker {
        width: 100% !important;
      }
    }
  }
}
</style>