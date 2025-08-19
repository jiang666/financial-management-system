<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">期初余额</h2>
      <p class="description">录入科目期初余额，进行试算平衡检查</p>
    </div>
    
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="科目编码">
          <el-input v-model="searchForm.code" placeholder="请输入科目编码" clearable />
        </el-form-item>
        <el-form-item label="科目名称">
          <el-input v-model="searchForm.name" placeholder="请输入科目名称" clearable />
        </el-form-item>
        <el-form-item label="科目类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 120px">
            <el-option label="资产" value="资产" />
            <el-option label="负债" value="负债" />
            <el-option label="权益" value="权益" />
            <el-option label="收入" value="收入" />
            <el-option label="成本" value="成本" />
            <el-option label="费用" value="费用" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="table-wrapper">
      <div class="table-header">
        <el-button type="primary" icon="Plus" @click="handleBatchEdit">批量录入</el-button>
        <el-button icon="Download" @click="handleExport">导出模板</el-button>
        <el-button icon="Upload" @click="handleImport">批量导入</el-button>
        <el-button type="success" icon="Check" @click="handleTrialBalance">试算平衡</el-button>
      </div>
      
      <el-table
        :data="tableData"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        :summary-method="getSummaries"
        show-summary
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="code" label="科目编码" width="120" />
        <el-table-column prop="name" label="科目名称" min-width="160" />
        <el-table-column prop="type" label="科目类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="direction" label="余额方向" width="100">
          <template #default="{ row }">
            <el-tag :type="row.direction === '借' ? 'success' : 'warning'">
              {{ row.direction }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="debitBalance" label="借方余额" width="160" align="right">
          <template #default="{ row, $index }">
            <el-input-number
              v-model="row.debitBalance"
              :min="0"
              :precision="2"
              :controls="false"
              @change="handleBalanceChange(row, $index)"
              style="width: 100%"
            />
          </template>
        </el-table-column>
        <el-table-column prop="creditBalance" label="贷方余额" width="160" align="right">
          <template #default="{ row, $index }">
            <el-input-number
              v-model="row.creditBalance"
              :min="0"
              :precision="2"
              :controls="false"
              @change="handleBalanceChange(row, $index)"
              style="width: 100%"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEditDetail(row)">明细</el-button>
            <el-button link type="success" @click="handleSave(row)">保存</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 试算平衡结果对话框 -->
    <el-dialog v-model="trialBalanceDialog" title="试算平衡检查" width="600px">
      <div class="trial-balance">
        <el-alert
          :title="balanceResult.isBalanced ? '试算平衡检查通过' : '试算平衡检查未通过'"
          :type="balanceResult.isBalanced ? 'success' : 'error'"
          show-icon
          :closable="false"
        />
        
        <el-descriptions :column="2" border class="mt-lg">
          <el-descriptions-item label="借方合计">
            <span class="amount">{{ formatMoney(balanceResult.totalDebit) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="贷方合计">
            <span class="amount">{{ formatMoney(balanceResult.totalCredit) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="差额">
            <span class="amount" :class="{ 'error-amount': balanceResult.difference !== 0 }">
              {{ formatMoney(balanceResult.difference) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="检查时间">
            {{ balanceResult.checkTime }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div v-if="!balanceResult.isBalanced" class="mt-lg">
          <h4>可能的原因：</h4>
          <ul>
            <li>某些科目的余额录入有误</li>
            <li>借贷方向设置错误</li>
            <li>存在重复录入的情况</li>
            <li>漏录某些科目的期初余额</li>
          </ul>
        </div>
      </div>
      <template #footer>
        <el-button @click="trialBalanceDialog = false">关闭</el-button>
        <el-button v-if="balanceResult.isBalanced" type="primary" @click="confirmBalance">
          确认期初余额
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 科目明细录入对话框 -->
    <el-dialog v-model="detailDialog" title="科目明细录入" width="800px">
      <el-form :model="detailForm" label-width="100px">
        <el-form-item label="科目信息">
          <span>{{ detailForm.code }} - {{ detailForm.name }}</span>
        </el-form-item>
        <el-form-item label="辅助核算" v-if="detailForm.auxiliary && detailForm.auxiliary.length > 0">
          <el-checkbox-group v-model="detailForm.selectedAuxiliary">
            <el-checkbox v-for="item in detailForm.auxiliary" :key="item" :label="item">
              {{ item }}核算
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      
      <div class="detail-table">
        <el-button type="primary" icon="Plus" @click="addDetailRow" class="mb-md">添加明细</el-button>
        <el-table :data="detailForm.details" border>
          <el-table-column label="部门" v-if="detailForm.selectedAuxiliary.includes('部门')">
            <template #default="{ row }">
              <el-select v-model="row.department" placeholder="选择部门">
                <el-option label="财务部" value="财务部" />
                <el-option label="销售部" value="销售部" />
                <el-option label="采购部" value="采购部" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="客户" v-if="detailForm.selectedAuxiliary.includes('客户')">
            <template #default="{ row }">
              <el-select v-model="row.customer" placeholder="选择客户">
                <el-option label="ABC公司" value="ABC公司" />
                <el-option label="XYZ公司" value="XYZ公司" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="借方金额" width="150">
            <template #default="{ row }">
              <el-input-number v-model="row.debitAmount" :min="0" :precision="2" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="贷方金额" width="150">
            <template #default="{ row }">
              <el-input-number v-model="row.creditAmount" :min="0" :precision="2" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ $index }">
              <el-button link type="danger" @click="removeDetailRow($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <template #footer>
        <el-button @click="detailDialog = false">取消</el-button>
        <el-button type="primary" @click="saveDetail">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { mockAccounts } from '@/api/mockData'

const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])
const trialBalanceDialog = ref(false)
const detailDialog = ref(false)

const searchForm = ref({
  code: '',
  name: '',
  type: ''
})

const balanceResult = ref({
  isBalanced: false,
  totalDebit: 0,
  totalCredit: 0,
  difference: 0,
  checkTime: ''
})

const detailForm = ref({
  id: '',
  code: '',
  name: '',
  auxiliary: [],
  selectedAuxiliary: [],
  details: []
})

onMounted(() => {
  loadData()
})

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    // 添加余额字段
    tableData.value = mockAccounts.map(item => ({
      ...item,
      debitBalance: item.direction === '借' ? item.balance : 0,
      creditBalance: item.direction === '贷' ? item.balance : 0
    }))
    loading.value = false
  }, 500)
}

const getTypeTagType = (type) => {
  const typeMap = {
    '资产': 'success',
    '负债': 'warning',
    '权益': '',
    '收入': 'info',
    '成本': 'danger',
    '费用': 'danger'
  }
  return typeMap[type] || ''
}

const formatMoney = (amount) => {
  if (!amount) return '0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const getSummaries = (param) => {
  const { columns, data } = param
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (column.property === 'debitBalance') {
      const values = data.map(item => Number(item.debitBalance) || 0)
      sums[index] = formatMoney(values.reduce((prev, curr) => prev + curr, 0))
    } else if (column.property === 'creditBalance') {
      const values = data.map(item => Number(item.creditBalance) || 0)
      sums[index] = formatMoney(values.reduce((prev, curr) => prev + curr, 0))
    } else {
      sums[index] = ''
    }
  })
  return sums
}

const handleSearch = () => {
  loadData()
  ElMessage.success('搜索完成')
}

const handleReset = () => {
  searchForm.value = {
    code: '',
    name: '',
    type: ''
  }
  loadData()
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleBalanceChange = (row, index) => {
  // 根据科目余额方向自动调整
  if (row.direction === '借' && row.creditBalance > 0) {
    row.creditBalance = 0
  } else if (row.direction === '贷' && row.debitBalance > 0) {
    row.debitBalance = 0
  }
}

const handleBatchEdit = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要批量录入的科目')
    return
  }
  ElMessage.success('批量录入功能开发中')
}

const handleTrialBalance = () => {
  const totalDebit = tableData.value.reduce((sum, item) => sum + (item.debitBalance || 0), 0)
  const totalCredit = tableData.value.reduce((sum, item) => sum + (item.creditBalance || 0), 0)
  const difference = Math.abs(totalDebit - totalCredit)
  
  balanceResult.value = {
    isBalanced: difference < 0.01, // 允许1分钱的误差
    totalDebit,
    totalCredit,
    difference,
    checkTime: new Date().toLocaleString()
  }
  
  trialBalanceDialog.value = true
}

const confirmBalance = () => {
  trialBalanceDialog.value = false
  ElMessage.success('期初余额确认成功')
}

const handleEditDetail = (row) => {
  detailForm.value = {
    id: row.id,
    code: row.code,
    name: row.name,
    auxiliary: ['部门', '客户'],
    selectedAuxiliary: [],
    details: []
  }
  detailDialog.value = true
}

const addDetailRow = () => {
  detailForm.value.details.push({
    department: '',
    customer: '',
    debitAmount: 0,
    creditAmount: 0
  })
}

const removeDetailRow = (index) => {
  detailForm.value.details.splice(index, 1)
}

const saveDetail = () => {
  detailDialog.value = false
  ElMessage.success('明细保存成功')
}

const handleSave = (row) => {
  ElMessage.success(`科目"${row.name}"余额保存成功`)
}

const handleExport = () => {
  ElMessage.success('导出模板功能开发中')
}

const handleImport = () => {
  ElMessage.success('批量导入功能开发中')
}
</script>

<style scoped lang="scss">
.trial-balance {
  .amount {
    font-weight: bold;
    color: #67C23A;
    
    &.error-amount {
      color: #F56C6C;
    }
  }
  
  ul {
    padding-left: 20px;
    
    li {
      margin-bottom: 5px;
    }
  }
}

.detail-table {
  margin-top: 20px;
}
</style>