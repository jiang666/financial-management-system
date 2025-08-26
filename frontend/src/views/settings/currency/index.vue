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
        <el-table-column prop="exchangeRate" label="汇率" width="160" align="right">
          <template #default="{ row }">
            <span v-if="row.isBase" class="base-rate">1.00000000</span>
            <el-input
              v-else
              v-model="row.exchangeRate"
              @change="handleRateChange(row)"
              @blur="validateTableRate(row)"
              style="width: 100%; text-align: right"
              placeholder="请输入汇率"
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
        <el-form-item label="英文名称">
          <el-input v-model="currencyForm.nameEn" placeholder="请输入英文名称（如：US Dollar）" />
        </el-form-item>
        <el-form-item label="币种符号" prop="symbol">
          <el-input v-model="currencyForm.symbol" placeholder="请输入币种符号（如：$）" />
        </el-form-item>
        <el-form-item label="汇率" prop="exchangeRate" v-if="!currencyForm.isBase">
          <el-input
            v-model="currencyForm.exchangeRate"
            placeholder="请输入汇率（支持8位小数）"
            @blur="validateExchangeRate"
          >
            <template #append>汇率</template>
          </el-input>
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
            {{ row.rate.toFixed(8) }}
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
import { currencyApi, exchangeRateApi } from '@/api/currency'

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
  nameEn: '',
  symbol: '',
  exchangeRate: '1.00000000',
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
    { 
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请输入汇率'))
          return
        }
        const rate = parseFloat(value)
        if (isNaN(rate) || rate <= 0) {
          callback(new Error('汇率必须为大于0的数字'))
          return
        }
        if (rate > 999999) {
          callback(new Error('汇率不能超过999999'))
          return
        }
        callback()
      },
      trigger: 'blur' 
    }
  ]
}

const dialogTitle = computed(() => {
  return currencyForm.value.id ? '编辑币种' : '新增币种'
})

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    // 获取所有币种列表
    const res = await currencyApi.getAllCurrencies()
    if (res.code === 200) {
      tableData.value = res.data.map(item => ({
        ...item,
        exchangeRate: item.currentRate ? item.currentRate.toString() : '1.00000000',
        status: item.status === 1 ? '启用' : '停用',
        isBase: item.isBase === 1,
        updateTime: item.lastRateUpdate ? new Date(item.lastRateUpdate).toLocaleString() : '-'
      }))
    }
  } catch (error) {
    console.error('获取币种列表失败:', error)
    ElMessage.error('获取币种列表失败')
  } finally {
    loading.value = false
  }
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
    nameEn: '',
    symbol: '',
    exchangeRate: '1.00000000',
    isBase: false,
    remark: ''
  }
  currencyDialog.value = true
}

const handleEdit = (row) => {
  currencyForm.value = { 
    ...row,
    exchangeRate: row.exchangeRate ? row.exchangeRate.toString() : '1.00000000'
  }
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
    
    const res = await currencyApi.deleteCurrency(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const handleRateChange = (row) => {
  console.log('表格汇率变更:', row.exchangeRate)
  row.updateTime = new Date().toLocaleString()
  ElMessage.success(`${row.name}汇率已更新`)
}

const validateTableRate = (row) => {
  console.log('表格汇率验证:', row.exchangeRate)
  
  if (!row.exchangeRate) {
    row.exchangeRate = '1.00000000'
    return
  }
  
  const rateValue = parseFloat(row.exchangeRate)
  if (isNaN(rateValue) || rateValue <= 0) {
    ElMessage.warning('请输入有效的汇率值')
    row.exchangeRate = '1.00000000'
    return
  }
  
  // 限制小数位数为8位，但保持字符串精度
  const parts = row.exchangeRate.toString().split('.')
  if (parts[1] && parts[1].length > 8) {
    // 直接截取字符串，不使用parseFloat避免精度丢失
    row.exchangeRate = parts[0] + '.' + parts[1].substring(0, 8)
  }
  
  console.log('表格汇率验证后:', row.exchangeRate)
}

const handleBaseChange = async (row) => {
  if (row.isBase) {
    try {
      const res = await currencyApi.setBaseCurrency(row.id)
      if (res.code === 200) {
        ElMessage.success(`${row.name}已设为基准币种`)
        loadData()
      }
    } catch (error) {
      console.error('设置基准币种失败:', error)
      ElMessage.error('设置基准币种失败')
      row.isBase = false
    }
  }
}

const handleStatusChange = async (row) => {
  try {
    const status = row.status === '启用' ? 1 : 0
    const res = await currencyApi.toggleCurrencyStatus(row.id, status)
    if (res.code === 200) {
      ElMessage.success(`币种"${row.name}"状态已更新`)
    }
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
    // 恢复原状态
    row.status = row.status === '启用' ? '停用' : '启用'
  }
}

const handleSave = async () => {
  const valid = await currencyFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  try {
    if (currencyForm.value.id) {
      // 编辑币种
      const updateData = {
        name: currencyForm.value.name,
        nameEn: currencyForm.value.nameEn || currencyForm.value.name,
        symbol: currencyForm.value.symbol,
        decimalPlaces: 8,
        sortOrder: currencyForm.value.sortOrder || 0
      }
      const res = await currencyApi.updateCurrency(currencyForm.value.id, updateData)
      if (res.code === 200) {
        ElMessage.success('编辑成功')
        currencyDialog.value = false
        loadData()
      }
    } else {
      // 新增币种
      const createData = {
        code: currencyForm.value.code,
        name: currencyForm.value.name,
        nameEn: currencyForm.value.nameEn || currencyForm.value.name,
        symbol: currencyForm.value.symbol,
        decimalPlaces: 8,
        sortOrder: currencyForm.value.sortOrder || 0
      }
      const res = await currencyApi.createCurrency(createData)
      if (res.code === 200) {
        ElMessage.success('新增成功')
        
        // 如果设置为基准币种
        if (currencyForm.value.isBase) {
          await currencyApi.setBaseCurrency(res.data.id)
        }
        
        // 如果不是基准币种且设置了汇率，更新汇率
        if (!currencyForm.value.isBase && currencyForm.value.exchangeRate !== '1' && currencyForm.value.exchangeRate !== '1.00000000') {
          const baseCurrency = await currencyApi.getBaseCurrency()
          if (baseCurrency.code === 200 && baseCurrency.data) {
            console.log('汇率值类型:', typeof currencyForm.value.exchangeRate)
            console.log('汇率值:', currencyForm.value.exchangeRate)
            console.log('汇率值JSON.stringify:', JSON.stringify(currencyForm.value.exchangeRate))
            
            // 直接使用字符串形式传递，保持精度
            await exchangeRateApi.updateRate(baseCurrency.data.id, res.data.id, {
              rate: currencyForm.value.exchangeRate
            })
          }
        }
        
        currencyDialog.value = false
        loadData()
      }
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.response?.data?.message || '保存失败')
  }
}

const resetForm = () => {
  currencyFormRef.value?.resetFields()
}

const validateExchangeRate = () => {
  console.log('汇率验证:', currencyForm.value.exchangeRate)
  
  if (!currencyForm.value.exchangeRate) {
    currencyForm.value.exchangeRate = '1.00000000'
    return
  }
  
  // 验证是否为有效数字
  const rateValue = parseFloat(currencyForm.value.exchangeRate)
  if (isNaN(rateValue) || rateValue <= 0) {
    ElMessage.warning('请输入有效的汇率值')
    currencyForm.value.exchangeRate = '1.00000000'
    return
  }
  
  // 限制小数位数为8位，但保持字符串精度
  const parts = currencyForm.value.exchangeRate.toString().split('.')
  if (parts[1] && parts[1].length > 8) {
    // 直接截取字符串，不使用parseFloat避免精度丢失
    currencyForm.value.exchangeRate = parts[0] + '.' + parts[1].substring(0, 8)
  }
  
  console.log('汇率验证后:', currencyForm.value.exchangeRate)
}

const handleUpdateRates = async () => {
  loading.value = true
  try {
    const res = await exchangeRateApi.syncRatesFromExternalAPI()
    if (res.code === 200) {
      if (res.data.errorMessage) {
        ElMessage.warning(res.data.errorMessage)
      } else {
        ElMessage.success(`汇率更新成功，成功${res.data.successCount}条，失败${res.data.failCount}条`)
      }
      loadData()
    }
  } catch (error) {
    console.error('更新汇率失败:', error)
    ElMessage.error('更新汇率失败')
  } finally {
    loading.value = false
  }
}

const handleViewHistory = (row) => {
  historySearch.value.currency = row.code
  showRateHistory.value = true
  loadRateHistory()
}

const loadRateHistory = async () => {
  historyLoading.value = true
  try {
    const params = {
      currencyCode: historySearch.value.currency,
      page: 0,
      size: 20
    }
    
    if (historySearch.value.dateRange && historySearch.value.dateRange.length === 2) {
      params.startDate = new Date(historySearch.value.dateRange[0]).getTime()
      params.endDate = new Date(historySearch.value.dateRange[1]).getTime()
    }
    
    const res = await exchangeRateApi.getRateHistory(params)
    if (res.code === 200) {
      // 转换数据格式
      rateHistoryData.value = res.data.content.map((item, index, arr) => {
        const previousRate = index < arr.length - 1 ? arr[index + 1].rate : item.rate
        const changeAmount = item.rate - previousRate
        const changePercent = previousRate !== 0 ? changeAmount / previousRate : 0
        
        return {
          currency: item.fromCurrencyCode || item.toCurrencyCode,
          rate: item.rate,
          changeAmount: changeAmount,
          changePercent: changePercent,
          updateTime: new Date(item.effectiveDate).toLocaleString(),
          operator: item.createdBy
        }
      })
    }
  } catch (error) {
    console.error('获取汇率历史失败:', error)
    ElMessage.error('获取汇率历史失败')
    rateHistoryData.value = []
  } finally {
    historyLoading.value = false
  }
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