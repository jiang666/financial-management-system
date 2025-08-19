<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">币种汇率</h2>
      <p class="description">管理多币种和汇率设置，支持汇率历史查询</p>
    </div>
    
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="币种代码">
          <el-input v-model="searchForm.code" placeholder="请输入币种代码" clearable />
        </el-form-item>
        <el-form-item label="币种名称">
          <el-input v-model="searchForm.name" placeholder="请输入币种名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 100px">
            <el-option label="启用" value="启用" />
            <el-option label="停用" value="停用" />
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
        <el-button type="primary" icon="Plus" @click="handleAdd">新增币种</el-button>
        <el-button type="success" icon="Refresh" @click="handleUpdateRates">更新汇率</el-button>
        <el-button icon="DataAnalysis" @click="showRateHistory = true">汇率历史</el-button>
      </div>
      
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="code" label="币种代码" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isBase" type="danger">{{ row.code }}</el-tag>
            <span v-else>{{ row.code }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="币种名称" min-width="120" />
        <el-table-column prop="symbol" label="符号" width="80" />
        <el-table-column prop="exchangeRate" label="汇率" width="140" align="right">
          <template #default="{ row }">
            <span v-if="row.isBase" class="base-rate">1.0000</span>
            <el-input-number
              v-else
              v-model="row.exchangeRate"
              :precision="4"
              :min="0"
              :controls="false"
              @change="handleRateChange(row)"
              style="width: 100%"
            />
          </template>
        </el-table-column>
        <el-table-column label="基准币种" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.isBase"
              :disabled="row.isBase"
              @change="handleBaseChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="启用"
              inactive-value="停用"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleViewHistory(row)">历史</el-button>
            <el-button link type="danger" @click="handleDelete(row)" :disabled="row.isBase">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 新增/编辑币种对话框 -->
    <el-dialog
      v-model="currencyDialog"
      :title="dialogTitle"
      width="500px"
      @close="resetForm"
    >
      <el-form ref="currencyFormRef" :model="currencyForm" :rules="currencyRules" label-width="100px">
        <el-form-item label="币种代码" prop="code">
          <el-input v-model="currencyForm.code" placeholder="请输入币种代码（如：USD）" />
        </el-form-item>
        <el-form-item label="币种名称" prop="name">
          <el-input v-model="currencyForm.name" placeholder="请输入币种名称（如：美元）" />
        </el-form-item>
        <el-form-item label="币种符号" prop="symbol">
          <el-input v-model="currencyForm.symbol" placeholder="请输入币种符号（如：$）" />
        </el-form-item>
        <el-form-item label="汇率" prop="exchangeRate" v-if="!currencyForm.isBase">
          <el-input-number
            v-model="currencyForm.exchangeRate"
            :precision="4"
            :min="0"
            placeholder="请输入汇率"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="基准币种">
          <el-switch v-model="currencyForm.isBase" />
          <span class="ml-sm text-secondary">设为基准币种后汇率固定为1</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="currencyForm.remark" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="currencyDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 汇率历史对话框 -->
    <el-dialog v-model="showRateHistory" title="汇率历史" width="800px">
      <div class="history-search">
        <el-form inline>
          <el-form-item label="币种">
            <el-select v-model="historySearch.currency" placeholder="选择币种">
              <el-option
                v-for="item in tableData"
                :key="item.code"
                :label="item.name"
                :value="item.code"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="日期范围">
            <el-date-picker
              v-model="historySearch.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadRateHistory">查询</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table :data="rateHistoryData" v-loading="historyLoading">
        <el-table-column prop="currency" label="币种" width="100" />
        <el-table-column prop="rate" label="汇率" width="120" align="right">
          <template #default="{ row }">
            {{ row.rate.toFixed(4) }}
          </template>
        </el-table-column>
        <el-table-column prop="changeAmount" label="变动幅度" width="120" align="right">
          <template #default="{ row }">
            <span :class="row.changeAmount >= 0 ? 'text-success' : 'text-danger'">
              {{ row.changeAmount >= 0 ? '+' : '' }}{{ row.changeAmount.toFixed(4) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="changePercent" label="变动比例" width="120" align="right">
          <template #default="{ row }">
            <span :class="row.changePercent >= 0 ? 'text-success' : 'text-danger'">
              {{ row.changePercent >= 0 ? '+' : '' }}{{ (row.changePercent * 100).toFixed(2) }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column prop="operator" label="操作人" width="100" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { mockCurrencies } from '@/api/mockData'

const loading = ref(false)
const historyLoading = ref(false)
const tableData = ref([])
const currencyDialog = ref(false)
const showRateHistory = ref(false)
const currencyFormRef = ref()
const rateHistoryData = ref([])

const searchForm = ref({
  code: '',
  name: '',
  status: ''
})

const currencyForm = ref({
  id: '',
  code: '',
  name: '',
  symbol: '',
  exchangeRate: 1,
  isBase: false,
  remark: ''
})

const historySearch = ref({
  currency: '',
  dateRange: []
})

const currencyRules = {
  code: [
    { required: true, message: '请输入币种代码', trigger: 'blur' },
    { pattern: /^[A-Z]{3}$/, message: '币种代码应为3位大写字母', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入币种名称', trigger: 'blur' }
  ],
  symbol: [
    { required: true, message: '请输入币种符号', trigger: 'blur' }
  ],
  exchangeRate: [
    { required: true, message: '请输入汇率', trigger: 'blur' },
    { type: 'number', min: 0, message: '汇率必须大于0', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => {
  return currencyForm.value.id ? '编辑币种' : '新增币种'
})

onMounted(() => {
  loadData()
})

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    tableData.value = mockCurrencies.map(item => ({
      ...item,
      updateTime: '2025-08-15 09:00:00'
    }))
    loading.value = false
  }, 500)
}

const handleSearch = () => {
  loadData()
  ElMessage.success('搜索完成')
}

const handleReset = () => {
  searchForm.value = {
    code: '',
    name: '',
    status: ''
  }
  loadData()
}

const handleAdd = () => {
  currencyForm.value = {
    id: '',
    code: '',
    name: '',
    symbol: '',
    exchangeRate: 1,
    isBase: false,
    remark: ''
  }
  currencyDialog.value = true
}

const handleEdit = (row) => {
  currencyForm.value = { ...row }
  currencyDialog.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除币种"${row.name}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // 取消删除
  }
}

const handleRateChange = (row) => {
  row.updateTime = new Date().toLocaleString()
  ElMessage.success(`${row.name}汇率已更新`)
}

const handleBaseChange = (row) => {
  if (row.isBase) {
    // 取消其他币种的基准状态
    tableData.value.forEach(item => {
      if (item.id !== row.id) {
        item.isBase = false
      }
    })
    row.exchangeRate = 1
    ElMessage.success(`${row.name}已设为基准币种`)
  }
}

const handleStatusChange = (row) => {
  ElMessage.success(`币种"${row.name}"状态已更新`)
}

const handleSave = async () => {
  const valid = await currencyFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  if (currencyForm.value.isBase) {
    currencyForm.value.exchangeRate = 1
  }
  
  currencyDialog.value = false
  ElMessage.success(currencyForm.value.id ? '编辑成功' : '新增成功')
  loadData()
}

const resetForm = () => {
  currencyFormRef.value?.resetFields()
}

const handleUpdateRates = () => {
  loading.value = true
  setTimeout(() => {
    // 模拟从外部API获取汇率
    tableData.value.forEach(item => {
      if (!item.isBase) {
        const randomChange = (Math.random() - 0.5) * 0.1
        item.exchangeRate = Math.max(0.0001, item.exchangeRate + randomChange)
        item.updateTime = new Date().toLocaleString()
      }
    })
    loading.value = false
    ElMessage.success('汇率更新成功')
  }, 1000)
}

const handleViewHistory = (row) => {
  historySearch.value.currency = row.code
  showRateHistory.value = true
  loadRateHistory()
}

const loadRateHistory = () => {
  historyLoading.value = true
  setTimeout(() => {
    // 模拟汇率历史数据
    rateHistoryData.value = [
      {
        currency: historySearch.value.currency || 'USD',
        rate: 7.2365,
        changeAmount: 0.0123,
        changePercent: 0.0017,
        updateTime: '2025-08-15 09:00:00',
        operator: '系统自动'
      },
      {
        currency: historySearch.value.currency || 'USD',
        rate: 7.2242,
        changeAmount: -0.0089,
        changePercent: -0.0012,
        updateTime: '2025-08-14 09:00:00',
        operator: '系统自动'
      },
      {
        currency: historySearch.value.currency || 'USD',
        rate: 7.2331,
        changeAmount: 0.0156,
        changePercent: 0.0022,
        updateTime: '2025-08-13 09:00:00',
        operator: '张三'
      }
    ]
    historyLoading.value = false
  }, 500)
}
</script>

<style scoped lang="scss">
.base-rate {
  color: #F56C6C;
  font-weight: bold;
}

.text-secondary {
  color: #909399;
  font-size: 12px;
}

.text-success {
  color: #67C23A;
}

.text-danger {
  color: #F56C6C;
}

.history-search {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}
</style>