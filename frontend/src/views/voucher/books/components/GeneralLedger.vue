<template>
  <div class="general-ledger">
    <!-- 查询条件 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="科目选择">
          <el-select v-model="searchForm.accountCode" placeholder="选择科目(默认显示所有)" filterable style="width: 200px" clearable>
            <el-option label="全部科目" value="" />
            <el-option
              v-for="account in accountOptions"
              :key="account.code"
              :label="`${account.code} ${account.name}`"
              :value="account.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="查询期间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="monthrange"
            range-separator="至"
            start-placeholder="开始月份"
            end-placeholder="结束月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
          <el-button icon="Download" @click="handleExport">导出</el-button>
          <el-button icon="Printer" @click="handlePrint">打印</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 总账展示 -->
    <div class="ledger-table" v-if="showLedgerTable">
      <div class="account-header">
        <h3>{{ getAccountDisplayName() }} 总账</h3>
        <p>查询期间：{{ searchForm.dateRange?.[0] }} 至 {{ searchForm.dateRange?.[1] }}</p>
      </div>
      
      <el-table :data="ledgerData" v-loading="loading" border>
        <el-table-column prop="accountCode" label="科目编码" width="120" v-if="!searchForm.accountCode" />
        <el-table-column prop="accountName" label="科目名称" width="150" v-if="!searchForm.accountCode" />
        <el-table-column prop="date" label="日期" width="100" />
        <el-table-column prop="voucherNo" label="凭证字号" width="140" />
        <el-table-column prop="summary" label="摘要" min-width="200" />
        <el-table-column prop="debitAmount" label="借方发生额" width="120" align="right">
          <template #default="{ row }">
            {{ row.debitAmount ? formatMoney(row.debitAmount) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="creditAmount" label="贷方发生额" width="120" align="right">
          <template #default="{ row }">
            {{ row.creditAmount ? formatMoney(row.creditAmount) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="direction" label="方向" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.direction === '借' ? 'success' : 'warning'" size="small">
              {{ row.direction }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" width="120" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.balance) }}
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 合计行 -->
      <div class="summary-row">
        <el-descriptions :column="4" border>
          <el-descriptions-item label="期初余额">{{ formatMoney(summaryData.beginBalance) }}</el-descriptions-item>
          <el-descriptions-item label="借方发生额">{{ formatMoney(summaryData.totalDebit) }}</el-descriptions-item>
          <el-descriptions-item label="贷方发生额">{{ formatMoney(summaryData.totalCredit) }}</el-descriptions-item>
          <el-descriptions-item label="期末余额">{{ formatMoney(summaryData.endBalance) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { mockAccounts } from '@/api/mockData'

const loading = ref(false)
const currentAccount = ref(null)

const searchForm = ref({
  accountCode: '',
  dateRange: ['2025-01', '2025-08']
})

const accountOptions = ref(mockAccounts)
const ledgerData = ref([])

const summaryData = ref({
  beginBalance: 0,
  totalDebit: 0,
  totalCredit: 0,
  endBalance: 0
})

// 计算是否显示总账表格
const showLedgerTable = computed(() => {
  return ledgerData.value.length > 0
})

// 获取账目显示名称
const getAccountDisplayName = () => {
  if (!searchForm.value.accountCode) {
    return '所有科目'
  }
  return currentAccount.value ? `${currentAccount.value.code} ${currentAccount.value.name}` : ''
}

watch(() => searchForm.value.accountCode, (newCode) => {
  if (newCode) {
    currentAccount.value = accountOptions.value.find(acc => acc.code === newCode)
  } else {
    currentAccount.value = null
  }
  loadLedgerData()
})

const loadLedgerData = () => {
  loading.value = true
  setTimeout(() => {
    if (!searchForm.value.accountCode) {
      // 显示所有科目的总账数据
      ledgerData.value = [
        {
          accountCode: '1001',
          accountName: '库存现金',
          date: '2025-08-01',
          voucherNo: '记-20250801-001',
          summary: '期初余额',
          debitAmount: 0,
          creditAmount: 0,
          direction: '借',
          balance: 100000
        },
        {
          accountCode: '1001',
          accountName: '库存现金',
          date: '2025-08-02',
          voucherNo: '记-20250802-001',
          summary: '销售收入',
          debitAmount: 50000,
          creditAmount: 0,
          direction: '借',
          balance: 150000
        },
        {
          accountCode: '1002',
          accountName: '银行存款',
          date: '2025-08-01',
          voucherNo: '记-20250801-002',
          summary: '期初余额',
          debitAmount: 0,
          creditAmount: 0,
          direction: '借',
          balance: 500000
        },
        {
          accountCode: '1002',
          accountName: '银行存款',
          date: '2025-08-03',
          voucherNo: '记-20250803-001',
          summary: '收到客户转账',
          debitAmount: 200000,
          creditAmount: 0,
          direction: '借',
          balance: 700000
        },
        {
          accountCode: '1122',
          accountName: '应收账款',
          date: '2025-08-04',
          voucherNo: '记-20250804-001',
          summary: '销售商品',
          debitAmount: 80000,
          creditAmount: 0,
          direction: '借',
          balance: 80000
        }
      ]
      
      summaryData.value = {
        beginBalance: 600000,
        totalDebit: 330000,
        totalCredit: 0,
        endBalance: 930000
      }
    } else {
      // 显示特定科目的总账数据
      ledgerData.value = [
        {
          date: '2025-08-01',
          voucherNo: '记-20250801-001',
          summary: '期初余额',
          debitAmount: 0,
          creditAmount: 0,
          direction: '借',
          balance: 100000
        },
        {
          date: '2025-08-02',
          voucherNo: '记-20250802-001',
          summary: '销售收入',
          debitAmount: 50000,
          creditAmount: 0,
          direction: '借',
          balance: 150000
        },
        {
          date: '2025-08-03',
          voucherNo: '记-20250803-001',
          summary: '收回货款',
          debitAmount: 0,
          creditAmount: 30000,
          direction: '借',
          balance: 120000
        }
      ]
      
      summaryData.value = {
        beginBalance: 100000,
        totalDebit: 50000,
        totalCredit: 30000,
        endBalance: 120000
      }
    }
    
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

const handleSearch = () => {
  loadLedgerData()
  ElMessage.success('查询完成')
}

// 页面加载时初始化显示所有账目
onMounted(() => {
  loadLedgerData()
})

const handleExport = () => {
  ElMessage.success('导出功能开发中')
}

const handlePrint = () => {
  ElMessage.success('打印功能开发中')
}
</script>

<style scoped lang="scss">
.general-ledger {
  .account-header {
    margin-bottom: 20px;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 4px;
    
    h3 {
      margin: 0 0 8px 0;
      color: #303133;
    }
    
    p {
      margin: 0;
      color: #606266;
      font-size: 14px;
    }
  }
  
  .summary-row {
    margin-top: 20px;
  }
}
</style>