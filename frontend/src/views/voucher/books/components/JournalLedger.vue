<template>
  <div class="journal-ledger">
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="日记账类型">
          <el-select v-model="searchForm.journalType" placeholder="选择类型" style="width: 150px">
            <el-option label="现金日记账" value="cash" />
            <el-option label="银行日记账" value="bank" />
          </el-select>
        </el-form-item>
        <el-form-item label="账户" v-if="searchForm.journalType === 'bank'">
          <el-select v-model="searchForm.bankAccount" placeholder="选择银行账户" style="width: 200px">
            <el-option label="中国银行基本户" value="中国银行基本户" />
            <el-option label="工商银行一般户" value="工商银行一般户" />
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
        </el-form-item>
      </el-form>
    </div>
    
    <el-table :data="journalData" v-loading="loading" border>
      <el-table-column prop="date" label="日期" width="100" />
      <el-table-column prop="voucherNo" label="凭证号" width="140" />
      <el-table-column prop="summary" label="摘要" min-width="200" />
      <el-table-column prop="oppositeAccount" label="对方科目" width="150" />
      <el-table-column prop="inAmount" label="收入" width="120" align="right">
        <template #default="{ row }">
          {{ row.inAmount ? formatMoney(row.inAmount) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="outAmount" label="支出" width="120" align="right">
        <template #default="{ row }">
          {{ row.outAmount ? formatMoney(row.outAmount) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="balance" label="余额" width="120" align="right">
        <template #default="{ row }">
          {{ formatMoney(row.balance) }}
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const searchForm = ref({
  journalType: 'cash',
  bankAccount: '',
  dateRange: ['2025-08', '2025-08']
})

const journalData = ref([
  {
    date: '2025-08-01',
    voucherNo: '记-001',
    summary: '期初余额',
    oppositeAccount: '',
    inAmount: 0,
    outAmount: 0,
    balance: 50000
  },
  {
    date: '2025-08-02',
    voucherNo: '记-002',
    summary: '收到客户现金',
    oppositeAccount: '应收账款',
    inAmount: 15000,
    outAmount: 0,
    balance: 65000
  },
  {
    date: '2025-08-03',
    voucherNo: '记-003',
    summary: '支付办公费用',
    oppositeAccount: '管理费用',
    inAmount: 0,
    outAmount: 1200,
    balance: 63800
  }
])

const formatMoney = (amount) => {
  if (!amount) return '0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const handleSearch = () => {
  ElMessage.success('查询完成')
}
</script>