<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">应收管理</h2>
      <p class="description">管理销售发票录入、应收确认、收款登记和核销处理</p>
    </div>
    
    <!-- 应收概览 -->
    <el-row :gutter="20" class="receivable-overview">
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="应收账款总额" :value="receivableSummary.total" :precision="2" prefix="¥">
            <template #suffix>
              <el-tag type="warning" size="small">关注</el-tag>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="本月新增" :value="receivableSummary.thisMonth" :precision="2" prefix="¥">
            <template #suffix>
              <span class="trend-up">
                <el-icon><ArrowUp /></el-icon>
                15%
              </span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="本月回款" :value="receivableSummary.thisMonthPaid" :precision="2" prefix="¥">
            <template #suffix>
              <span class="trend-up">
                <el-icon><ArrowUp /></el-icon>
                8%
              </span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="逾期金额" :value="receivableSummary.overdue" :precision="2" prefix="¥">
            <template #suffix>
              <el-tag type="danger" size="small">{{ receivableSummary.overdueCount }}笔</el-tag>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 功能区 -->
    <el-tabs v-model="activeTab">
      <!-- 销售发票 -->
      <el-tab-pane label="销售发票" name="invoice">
        <div class="search-form">
          <el-form :model="invoiceSearchForm" inline>
            <el-form-item label="发票号码">
              <el-input v-model="invoiceSearchForm.invoiceNo" placeholder="请输入发票号码" clearable />
            </el-form-item>
            <el-form-item label="客户">
              <el-select v-model="invoiceSearchForm.customerId" placeholder="请选择客户" clearable style="width: 150px">
                <el-option
                  v-for="customer in customers"
                  :key="customer.id"
                  :label="customer.name"
                  :value="customer.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="开票日期">
              <el-date-picker
                v-model="invoiceSearchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="invoiceSearchForm.status" placeholder="请选择" clearable style="width: 120px">
                <el-option label="已开票" value="invoiced" />
                <el-option label="部分收款" value="partial" />
                <el-option label="已收齐" value="paid" />
                <el-option label="已逾期" value="overdue" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleInvoiceSearch">搜索</el-button>
              <el-button icon="Refresh" @click="handleInvoiceReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <div class="table-wrapper">
          <div class="table-header">
            <el-button type="primary" icon="Plus" @click="handleAddInvoice">开具发票</el-button>
            <el-button type="success" icon="Money" @click="handleBatchReceive">批量收款</el-button>
            <el-button icon="Download" @click="handleExportInvoice">导出</el-button>
          </div>
          
          <el-table :data="invoiceData" v-loading="loading" @selection-change="handleInvoiceSelection" style="width: 100%" stripe>
            <el-table-column type="selection" width="55" />
            <el-table-column prop="invoiceNo" label="发票号码" width="140" show-overflow-tooltip />
            <el-table-column prop="invoiceDate" label="开票日期" width="110" />
            <el-table-column prop="customerName" label="客户名称" min-width="120" show-overflow-tooltip />
            <el-table-column prop="totalAmount" label="发票金额" width="120" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.totalAmount) }}
              </template>
            </el-table-column>
            <el-table-column prop="taxAmount" label="税额" width="100" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.taxAmount) }}
              </template>
            </el-table-column>
            <el-table-column prop="receivedAmount" label="已收金额" width="120" align="right">
              <template #default="{ row }">
                <span class="amount-received">{{ formatMoney(row.receivedAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="remainingAmount" label="剩余金额" width="120" align="right">
              <template #default="{ row }">
                <span :class="row.remainingAmount > 0 ? 'amount-pending' : 'amount-zero'">
                  {{ formatMoney(row.remainingAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="dueDate" label="到期日" width="110" />
            <el-table-column prop="overdueDays" label="逾期天数" width="100">
              <template #default="{ row }">
                <span v-if="row.overdueDays > 0" class="overdue-days">{{ row.overdueDays }}天</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getInvoiceStatusType(row.status)" size="small">
                  {{ getInvoiceStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button link type="primary" size="small" @click="handleReceivePayment(row)" v-if="row.remainingAmount > 0">收款</el-button>
                  <el-button link type="success" size="small" @click="handleWriteOff(row)" v-if="row.remainingAmount > 0">核销</el-button>
                  <el-button link type="info" size="small" @click="handleViewInvoice(row)">详情</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      
      <!-- 收款登记 -->
      <el-tab-pane label="收款登记" name="payment">
        <el-card>
          <el-form :model="paymentForm" :rules="paymentRules" ref="paymentFormRef" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="收款日期" prop="paymentDate">
                  <el-date-picker
                    v-model="paymentForm.paymentDate"
                    type="date"
                    placeholder="选择日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="客户" prop="customerId">
                  <el-select v-model="paymentForm.customerId" placeholder="请选择客户" style="width: 100%" @change="loadCustomerInvoices">
                    <el-option
                      v-for="customer in customers"
                      :key="customer.id"
                      :label="customer.name"
                      :value="customer.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="收款金额" prop="amount">
                  <el-input-number
                    v-model="paymentForm.amount"
                    :min="0"
                    :precision="2"
                    style="width: 100%"
                    placeholder="请输入收款金额"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="收款方式" prop="paymentMethod">
                  <el-select v-model="paymentForm.paymentMethod" placeholder="请选择" style="width: 100%">
                    <el-option label="银行转账" value="transfer" />
                    <el-option label="现金" value="cash" />
                    <el-option label="支票" value="check" />
                    <el-option label="电子支付" value="electronic" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item label="收款账户" prop="accountId">
              <el-select v-model="paymentForm.accountId" placeholder="请选择收款账户" style="width: 100%">
                <el-option label="中国银行基本户" value="1" />
                <el-option label="工商银行一般户" value="2" />
                <el-option label="现金" value="3" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="对应发票">
              <el-table :data="customerInvoices" border max-height="200">
                <el-table-column type="selection" width="55" @selection-change="handleInvoiceAllocation" />
                <el-table-column prop="invoiceNo" label="发票号码" width="130" />
                <el-table-column prop="invoiceDate" label="开票日期" width="100" />
                <el-table-column prop="totalAmount" label="发票金额" width="110" align="right">
                  <template #default="{ row }">
                    {{ formatMoney(row.totalAmount) }}
                  </template>
                </el-table-column>
                <el-table-column prop="remainingAmount" label="剩余金额" width="110" align="right">
                  <template #default="{ row }">
                    {{ formatMoney(row.remainingAmount) }}
                  </template>
                </el-table-column>
                <el-table-column label="分配金额" width="150">
                  <template #default="{ row }">
                    <el-input-number
                      v-model="row.allocatedAmount"
                      :min="0"
                      :max="row.remainingAmount"
                      :precision="2"
                      size="small"
                      style="width: 100%"
                      @change="updateTotalAllocation"
                    />
                  </template>
                </el-table-column>
              </el-table>
            </el-form-item>
            
            <el-form-item label="备注">
              <el-input v-model="paymentForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleSubmitPayment">确认收款</el-button>
              <el-button @click="handleResetPayment">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <!-- 应收统计 -->
      <el-tab-pane label="应收统计" name="statistics">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>客户应收排行</span>
              </template>
              <el-table :data="customerRanking" border>
                <el-table-column type="index" label="排名" width="60" />
                <el-table-column prop="customerName" label="客户名称" />
                <el-table-column prop="amount" label="应收金额" width="120" align="right">
                  <template #default="{ row }">
                    {{ formatMoney(row.amount) }}
                  </template>
                </el-table-column>
                <el-table-column prop="percentage" label="占比" width="80" align="right">
                  <template #default="{ row }">
                    {{ row.percentage }}%
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>账龄分布</span>
              </template>
              <div ref="agingChart" style="height: 300px"></div>
            </el-card>
          </el-col>
        </el-row>
        
        <el-card class="mt-lg">
          <template #header>
            <span>应收趋势分析</span>
          </template>
          <div ref="trendChart" style="height: 350px"></div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 发票详情对话框 -->
    <el-dialog v-model="invoiceDialog" title="发票详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="发票号码">{{ currentInvoice.invoiceNo }}</el-descriptions-item>
        <el-descriptions-item label="开票日期">{{ currentInvoice.invoiceDate }}</el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ currentInvoice.customerName }}</el-descriptions-item>
        <el-descriptions-item label="发票金额">{{ formatMoney(currentInvoice.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="税额">{{ formatMoney(currentInvoice.taxAmount) }}</el-descriptions-item>
        <el-descriptions-item label="不含税金额">{{ formatMoney(currentInvoice.totalAmount - currentInvoice.taxAmount) }}</el-descriptions-item>
        <el-descriptions-item label="已收金额">{{ formatMoney(currentInvoice.receivedAmount) }}</el-descriptions-item>
        <el-descriptions-item label="剩余金额">{{ formatMoney(currentInvoice.remainingAmount) }}</el-descriptions-item>
        <el-descriptions-item label="到期日">{{ currentInvoice.dueDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getInvoiceStatusType(currentInvoice.status)">
            {{ getInvoiceStatusText(currentInvoice.status) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      
      <el-divider />
      
      <h4>收款记录</h4>
      <el-table :data="paymentHistory" border>
        <el-table-column prop="paymentDate" label="收款日期" width="100" />
        <el-table-column prop="amount" label="收款金额" width="120" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="收款方式" width="100" />
        <el-table-column prop="accountName" label="收款账户" width="150" />
        <el-table-column prop="remark" label="备注" />
      </el-table>
    </el-dialog>
    
    <!-- 收款对话框 -->
    <el-dialog v-model="receiveDialog" title="收款登记" width="600px">
      <el-form :model="receiveForm" :rules="receiveRules" ref="receiveFormRef" label-width="100px">
        <el-form-item label="发票信息">
          <el-input :value="`${receiveForm.invoiceNo} - ${formatMoney(receiveForm.remainingAmount)}`" disabled />
        </el-form-item>
        <el-form-item label="收款日期" prop="paymentDate">
          <el-date-picker
            v-model="receiveForm.paymentDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="收款金额" prop="amount">
          <el-input-number
            v-model="receiveForm.amount"
            :min="0"
            :max="receiveForm.remainingAmount"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="收款方式" prop="paymentMethod">
          <el-select v-model="receiveForm.paymentMethod" placeholder="请选择" style="width: 100%">
            <el-option label="银行转账" value="transfer" />
            <el-option label="现金" value="cash" />
            <el-option label="支票" value="check" />
          </el-select>
        </el-form-item>
        <el-form-item label="收款账户" prop="accountId">
          <el-select v-model="receiveForm.accountId" placeholder="请选择" style="width: 100%">
            <el-option label="中国银行基本户" value="1" />
            <el-option label="工商银行一般户" value="2" />
            <el-option label="现金" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="receiveForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="receiveDialog = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmReceive">确认收款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'

const activeTab = ref('invoice')
const loading = ref(false)
const invoiceDialog = ref(false)
const receiveDialog = ref(false)
const paymentFormRef = ref()
const receiveFormRef = ref()
const agingChart = ref(null)
const trendChart = ref(null)
let agingChartInstance = null
let trendChartInstance = null

// 应收概览数据
const receivableSummary = ref({
  total: 2580000,
  thisMonth: 650000,
  thisMonthPaid: 420000,
  overdue: 180000,
  overdueCount: 8
})

// 客户列表
const customers = ref([
  { id: 1, name: 'ABC公司' },
  { id: 2, name: 'XYZ集团' },
  { id: 3, name: '华通科技' },
  { id: 4, name: '恒大商贸' }
])

// 发票搜索表单
const invoiceSearchForm = ref({
  invoiceNo: '',
  customerId: '',
  dateRange: [],
  status: ''
})

// 收款表单
const paymentForm = ref({
  paymentDate: new Date().toISOString().split('T')[0],
  customerId: '',
  amount: 0,
  paymentMethod: 'transfer',
  accountId: '',
  remark: ''
})

// 收款对话框表单
const receiveForm = ref({
  invoiceId: '',
  invoiceNo: '',
  remainingAmount: 0,
  paymentDate: new Date().toISOString().split('T')[0],
  amount: 0,
  paymentMethod: 'transfer',
  accountId: '',
  remark: ''
})

// 发票数据
const invoiceData = ref([
  {
    id: 1,
    invoiceNo: 'INV20250001',
    invoiceDate: '2025-08-01',
    customerName: 'ABC公司',
    totalAmount: 234000,
    taxAmount: 30420,
    receivedAmount: 150000,
    remainingAmount: 84000,
    dueDate: '2025-08-31',
    overdueDays: 0,
    status: 'partial'
  },
  {
    id: 2,
    invoiceNo: 'INV20250002',
    invoiceDate: '2025-07-15',
    customerName: 'XYZ集团',
    totalAmount: 456000,
    taxAmount: 59280,
    receivedAmount: 456000,
    remainingAmount: 0,
    dueDate: '2025-08-15',
    overdueDays: 0,
    status: 'paid'
  },
  {
    id: 3,
    invoiceNo: 'INV20250003',
    invoiceDate: '2025-07-01',
    customerName: '华通科技',
    totalAmount: 123000,
    taxAmount: 15990,
    receivedAmount: 0,
    remainingAmount: 123000,
    dueDate: '2025-07-31',
    overdueDays: 15,
    status: 'overdue'
  }
])

// 客户发票列表（用于收款分配）
const customerInvoices = ref([])

// 客户应收排行
const customerRanking = ref([
  { customerName: 'ABC公司', amount: 850000, percentage: 33.0 },
  { customerName: 'XYZ集团', amount: 620000, percentage: 24.0 },
  { customerName: '华通科技', amount: 450000, percentage: 17.4 },
  { customerName: '恒大商贸', amount: 380000, percentage: 14.7 },
  { customerName: '其他客户', amount: 280000, percentage: 10.9 }
])

// 收款记录
const paymentHistory = ref([
  {
    paymentDate: '2025-08-10',
    amount: 150000,
    paymentMethod: '银行转账',
    accountName: '中国银行基本户',
    remark: '部分收款'
  }
])

// 当前查看的发票
const currentInvoice = ref({})

const paymentRules = {
  paymentDate: [{ required: true, message: '请选择收款日期', trigger: 'change' }],
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  amount: [{ required: true, message: '请输入收款金额', trigger: 'blur' }],
  paymentMethod: [{ required: true, message: '请选择收款方式', trigger: 'change' }],
  accountId: [{ required: true, message: '请选择收款账户', trigger: 'change' }]
}

const receiveRules = {
  paymentDate: [{ required: true, message: '请选择收款日期', trigger: 'change' }],
  amount: [{ required: true, message: '请输入收款金额', trigger: 'blur' }],
  paymentMethod: [{ required: true, message: '请选择收款方式', trigger: 'change' }],
  accountId: [{ required: true, message: '请选择收款账户', trigger: 'change' }]
}

const formatMoney = (amount) => {
  if (!amount) return '¥0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const getInvoiceStatusType = (status) => {
  const map = {
    'invoiced': 'warning',
    'partial': 'primary',
    'paid': 'success',
    'overdue': 'danger'
  }
  return map[status] || ''
}

const getInvoiceStatusText = (status) => {
  const map = {
    'invoiced': '已开票',
    'partial': '部分收款',
    'paid': '已收齐',
    'overdue': '已逾期'
  }
  return map[status] || status
}

const initCharts = () => {
  // 账龄分布图
  if (agingChart.value) {
    agingChartInstance = echarts.init(agingChart.value)
    const agingOption = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      series: [
        {
          name: '账龄分布',
          type: 'pie',
          radius: ['30%', '70%'],
          data: [
            { value: 1200000, name: '0-30天' },
            { value: 800000, name: '31-60天' },
            { value: 350000, name: '61-90天' },
            { value: 150000, name: '91-120天' },
            { value: 80000, name: '120天以上' }
          ],
          label: {
            formatter: '{b}\n{d}%'
          }
        }
      ]
    }
    agingChartInstance.setOption(agingOption)
  }
  
  // 应收趋势图
  if (trendChart.value) {
    trendChartInstance = echarts.init(trendChart.value)
    const trendOption = {
      title: {
        text: '应收账款趋势'
      },
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['应收余额', '新增应收', '回款金额']
      },
      xAxis: {
        type: 'category',
        data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月']
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (value) => value / 10000 + '万'
        }
      },
      series: [
        {
          name: '应收余额',
          type: 'line',
          data: [200, 220, 180, 250, 280, 260, 240, 258],
          itemStyle: { color: '#409eff' }
        },
        {
          name: '新增应收',
          type: 'bar',
          data: [80, 90, 70, 120, 110, 100, 95, 65],
          itemStyle: { color: '#e6a23c' }
        },
        {
          name: '回款金额',
          type: 'bar',
          data: [60, 70, 90, 90, 110, 120, 115, 42],
          itemStyle: { color: '#67c23a' }
        }
      ]
    }
    trendChartInstance.setOption(trendOption)
  }
}

const handleInvoiceSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('查询完成')
  }, 500)
}

const handleInvoiceReset = () => {
  invoiceSearchForm.value = {
    invoiceNo: '',
    customerId: '',
    dateRange: [],
    status: ''
  }
}

const handleInvoiceSelection = (selection) => {
  // 处理发票选择
}

const handleAddInvoice = () => {
  ElMessage.info('发票开具功能开发中')
}

const handleBatchReceive = () => {
  ElMessage.info('批量收款功能开发中')
}

const handleExportInvoice = () => {
  ElMessage.success('导出功能开发中')
}

const handleReceivePayment = (row) => {
  receiveForm.value = {
    invoiceId: row.id,
    invoiceNo: row.invoiceNo,
    remainingAmount: row.remainingAmount,
    paymentDate: new Date().toISOString().split('T')[0],
    amount: row.remainingAmount,
    paymentMethod: 'transfer',
    accountId: '',
    remark: ''
  }
  receiveDialog.value = true
}

const handleWriteOff = (row) => {
  ElMessage.info(`核销发票：${row.invoiceNo}`)
}

const handleViewInvoice = (row) => {
  currentInvoice.value = row
  invoiceDialog.value = true
}

const loadCustomerInvoices = () => {
  const customerId = paymentForm.value.customerId
  if (customerId) {
    customerInvoices.value = invoiceData.value
      .filter(invoice => invoice.remainingAmount > 0)
      .map(invoice => ({
        ...invoice,
        allocatedAmount: 0
      }))
  }
}

const handleInvoiceAllocation = (selection) => {
  // 处理发票分配选择
}

const updateTotalAllocation = () => {
  const total = customerInvoices.value.reduce((sum, invoice) => sum + (invoice.allocatedAmount || 0), 0)
  paymentForm.value.amount = total
}

const handleSubmitPayment = async () => {
  const valid = await paymentFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  ElMessage.success('收款登记成功')
  handleResetPayment()
}

const handleResetPayment = () => {
  paymentFormRef.value?.resetFields()
  customerInvoices.value = []
}

const handleConfirmReceive = async () => {
  const valid = await receiveFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  const invoice = invoiceData.value.find(inv => inv.id === receiveForm.value.invoiceId)
  if (invoice) {
    invoice.receivedAmount += receiveForm.value.amount
    invoice.remainingAmount -= receiveForm.value.amount
    if (invoice.remainingAmount <= 0) {
      invoice.status = 'paid'
      invoice.remainingAmount = 0
    } else {
      invoice.status = 'partial'
    }
  }
  
  receiveDialog.value = false
  ElMessage.success('收款成功')
}

onMounted(() => {
  nextTick(() => {
    initCharts()
  })
  
  window.addEventListener('resize', () => {
    agingChartInstance?.resize()
    trendChartInstance?.resize()
  })
})
</script>

<style scoped lang="scss">
.receivable-overview {
  margin-bottom: 20px;
  
  .stat-card {
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
    
    .trend-up {
      color: #67c23a;
      font-size: 12px;
      
      .el-icon {
        vertical-align: middle;
      }
    }
  }
}

.table-wrapper {
  .table-header {
    margin-bottom: 16px;
    display: flex;
    gap: 10px;
  }
}

.amount-received {
  color: #67c23a;
  font-weight: 600;
}

.amount-pending {
  color: #e6a23c;
  font-weight: 600;
}

.amount-zero {
  color: #909399;
}

.overdue-days {
  color: #f56c6c;
  font-weight: 600;
}

.mt-lg {
  margin-top: 20px;
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

@media (max-width: 768px) {
  .receivable-overview {
    .el-col {
      margin-bottom: 16px;
    }
  }
  
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