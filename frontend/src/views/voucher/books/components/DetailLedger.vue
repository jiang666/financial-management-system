<template>
  <div class="detail-ledger">
    <!-- 查询条件 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="科目选择">
          <el-select v-model="searchForm.accountCode" placeholder="选择科目" filterable style="width: 200px">
            <el-option
              v-for="account in accountOptions"
              :key="account.code"
              :label="`${account.code} ${account.name}`"
              :value="account.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="辅助核算" v-if="hasAuxiliary">
          <el-select v-model="searchForm.auxiliary" placeholder="选择辅助项" style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="财务部" value="财务部" />
            <el-option label="销售部" value="销售部" />
            <el-option label="ABC公司" value="ABC公司" />
          </el-select>
        </el-form-item>
        <el-form-item label="查询期间">
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
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
          <el-button icon="Download" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 明细账展示 -->
    <div class="ledger-table" v-if="currentAccount">
      <div class="account-header">
        <h3>{{ currentAccount.code }} {{ currentAccount.name }} 明细账</h3>
        <p v-if="searchForm.auxiliary">辅助核算：{{ searchForm.auxiliary }}</p>
        <p>查询期间：{{ searchForm.dateRange?.[0] }} 至 {{ searchForm.dateRange?.[1] }}</p>
      </div>
      
      <el-table :data="ledgerData" v-loading="loading" border>
        <el-table-column prop="date" label="日期" width="100" />
        <el-table-column prop="voucherNo" label="凭证字号" width="140" />
        <el-table-column prop="summary" label="摘要" min-width="180" />
        <el-table-column prop="auxiliaryText" label="辅助核算" width="120" v-if="hasAuxiliary" />
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
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewVoucher(row.voucherNo)" size="small">
              查看凭证
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <el-empty v-else description="请选择科目查看明细账" />
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { mockAccounts } from '@/api/mockData'

const loading = ref(false)
const currentAccount = ref(null)

const searchForm = ref({
  accountCode: '',
  auxiliary: '',
  dateRange: ['2025-08-01', '2025-08-15']
})

const accountOptions = ref(mockAccounts)
const ledgerData = ref([])

const hasAuxiliary = computed(() => {
  return currentAccount.value?.auxiliary && currentAccount.value.auxiliary.length > 0
})

watch(() => searchForm.value.accountCode, (newCode) => {
  if (newCode) {
    currentAccount.value = accountOptions.value.find(acc => acc.code === newCode)
    searchForm.value.auxiliary = ''
    loadLedgerData()
  }
})

const loadLedgerData = () => {
  if (!currentAccount.value) return
  
  loading.value = true
  setTimeout(() => {
    // 模拟明细账数据
    ledgerData.value = [
      {
        date: '2025-08-01',
        voucherNo: '记-20250801-001',
        summary: '期初余额',
        auxiliaryText: '',
        debitAmount: 0,
        creditAmount: 0,
        direction: '借',
        balance: 100000
      },
      {
        date: '2025-08-02',
        voucherNo: '记-20250802-001',
        summary: '销售商品给ABC公司',
        auxiliaryText: 'ABC公司',
        debitAmount: 50000,
        creditAmount: 0,
        direction: '借',
        balance: 150000
      },
      {
        date: '2025-08-03',
        voucherNo: '记-20250803-001',
        summary: '收到ABC公司货款',
        auxiliaryText: 'ABC公司',
        debitAmount: 0,
        creditAmount: 30000,
        direction: '借',
        balance: 120000
      },
      {
        date: '2025-08-05',
        voucherNo: '记-20250805-001',
        summary: '销售商品给XYZ公司',
        auxiliaryText: 'XYZ公司',
        debitAmount: 25000,
        creditAmount: 0,
        direction: '借',
        balance: 145000
      }
    ]
    
    // 如果选择了辅助核算，过滤数据
    if (searchForm.value.auxiliary) {
      ledgerData.value = ledgerData.value.filter(item => 
        item.auxiliaryText.includes(searchForm.value.auxiliary)
      )
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
  if (!searchForm.value.accountCode) {
    ElMessage.warning('请先选择科目')
    return
  }
  loadLedgerData()
  ElMessage.success('查询完成')
}

const handleExport = () => {
  ElMessage.success('导出功能开发中')
}

const viewVoucher = (voucherNo) => {
  ElMessage.info(`查看凭证：${voucherNo}`)
}
</script>

<style scoped lang="scss">
.detail-ledger {
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
      margin: 4px 0;
      color: #606266;
      font-size: 14px;
    }
  }
}
</style>