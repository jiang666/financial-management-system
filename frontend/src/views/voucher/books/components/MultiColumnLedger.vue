<template>
  <div class="multi-column-ledger">
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="科目类型">
          <el-select v-model="searchForm.accountType" placeholder="选择科目类型" style="width: 150px">
            <el-option label="费用类" value="费用" />
            <el-option label="收入类" value="收入" />
            <el-option label="成本类" value="成本" />
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
    
    <el-table :data="multiColumnData" v-loading="loading" border>
      <el-table-column prop="date" label="日期" width="100" />
      <el-table-column prop="voucherNo" label="凭证号" width="140" />
      <el-table-column prop="summary" label="摘要" min-width="150" />
      <el-table-column prop="office" label="办公费" width="100" align="right">
        <template #default="{ row }">
          {{ row.office ? formatMoney(row.office) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="travel" label="差旅费" width="100" align="right">
        <template #default="{ row }">
          {{ row.travel ? formatMoney(row.travel) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="communication" label="通讯费" width="100" align="right">
        <template #default="{ row }">
          {{ row.communication ? formatMoney(row.communication) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="total" label="合计" width="120" align="right">
        <template #default="{ row }">
          {{ formatMoney(row.total) }}
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
  accountType: '费用',
  dateRange: ['2025-01', '2025-08']
})

const multiColumnData = ref([
  {
    date: '2025-08-01',
    voucherNo: '记-001',
    summary: '办公用品采购',
    office: 1200,
    travel: 0,
    communication: 0,
    total: 1200
  },
  {
    date: '2025-08-02',
    voucherNo: '记-002',
    summary: '员工出差费用',
    office: 0,
    travel: 3500,
    communication: 0,
    total: 3500
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