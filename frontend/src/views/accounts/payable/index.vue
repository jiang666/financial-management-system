<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">应付管理</h2>
      <p class="description">管理采购发票录入、应付确认、付款申请和付款执行</p>
    </div>
    
    <!-- 应付概览 -->
    <el-row :gutter="20" class="payable-overview">
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="应付账款总额" :value="payableSummary.total" :precision="2" prefix="¥">
            <template #suffix>
              <el-tag type="info" size="small">正常</el-tag>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="本月新增" :value="payableSummary.thisMonth" :precision="2" prefix="¥">
            <template #suffix>
              <span class="trend-up">
                <el-icon><ArrowUp /></el-icon>
                12%
              </span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="本月付款" :value="payableSummary.thisMonthPaid" :precision="2" prefix="¥">
            <template #suffix>
              <span class="trend-up">
                <el-icon><ArrowUp /></el-icon>
                18%
              </span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="待付款金额" :value="payableSummary.pending" :precision="2" prefix="¥">
            <template #suffix>
              <el-tag type="warning" size="small">{{ payableSummary.pendingCount }}笔</el-tag>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 功能区 -->
    <el-tabs v-model="activeTab">
      <!-- 采购发票 -->
      <el-tab-pane label="采购发票" name="invoice">
        <div class="search-form">
          <el-form :model="invoiceSearchForm" inline>
            <el-form-item label="发票号码">
              <el-input v-model="invoiceSearchForm.invoiceNo" placeholder="请输入发票号码" clearable />
            </el-form-item>
            <el-form-item label="供应商">
              <el-select v-model="invoiceSearchForm.supplierId" placeholder="请选择供应商" clearable style="width: 150px">
                <el-option
                  v-for="supplier in suppliers"
                  :key="supplier.id"
                  :label="supplier.name"
                  :value="supplier.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="发票日期">
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
                <el-option label="已确认" value="confirmed" />
                <el-option label="部分付款" value="partial" />
                <el-option label="已付清" value="paid" />
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
            <el-button type="primary" icon="Plus" @click="handleAddInvoice">录入发票</el-button>
            <el-button type="warning" icon="Postcard" @click="handleBatchApply">批量申请付款</el-button>
            <el-button icon="Download" @click="handleExportInvoice">导出</el-button>
          </div>
          
          <el-table :data="invoiceData" v-loading="loading" @selection-change="handleInvoiceSelection" style="width: 100%" stripe>
            <el-table-column type="selection" width="55" />
            <el-table-column prop="invoiceNo" label="发票号码" width="140" show-overflow-tooltip />
            <el-table-column prop="invoiceDate" label="发票日期" width="110" />
            <el-table-column prop="supplierName" label="供应商名称" min-width="120" show-overflow-tooltip />
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
            <el-table-column prop="paidAmount" label="已付金额" width="120" align="right">
              <template #default="{ row }">
                <span class="amount-paid">{{ formatMoney(row.paidAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="remainingAmount" label="剩余金额" width="120" align="right">
              <template #default="{ row }">
                <span :class="row.remainingAmount > 0 ? 'amount-pending' : 'amount-zero'">
                  {{ formatMoney(row.remainingAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="dueDate" label="付款期限" width="110" />
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
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button link type="primary" size="small" @click="handleApplyPayment(row)" v-if="row.remainingAmount > 0">申请付款</el-button>
                  <el-button link type="success" size="small" @click="handlePay(row)" v-if="row.remainingAmount > 0">付款</el-button>
                  <el-button link type="info" size="small" @click="handleViewInvoice(row)">详情</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      
      <!-- 付款申请 -->
      <el-tab-pane label="付款申请" name="application">
        <div class="search-form">
          <el-form :model="applicationSearchForm" inline>
            <el-form-item label="申请编号">
              <el-input v-model="applicationSearchForm.applicationNo" placeholder="请输入申请编号" clearable />
            </el-form-item>
            <el-form-item label="申请状态">
              <el-select v-model="applicationSearchForm.status" placeholder="请选择" clearable style="width: 120px">
                <el-option label="待审核" value="pending" />
                <el-option label="已审核" value="approved" />
                <el-option label="已拒绝" value="rejected" />
                <el-option label="已付款" value="paid" />
              </el-select>
            </el-form-item>
            <el-form-item label="申请日期">
              <el-date-picker
                v-model="applicationSearchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleApplicationSearch">搜索</el-button>
              <el-button icon="Refresh" @click="handleApplicationReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <div class="table-wrapper">
          <div class="table-header">
            <el-button type="primary" icon="Plus" @click="handleNewApplication">新建申请</el-button>
            <el-button type="success" icon="Check" @click="handleBatchApprove">批量审核</el-button>
          </div>
          
          <el-table :data="applicationData" v-loading="loading" @selection-change="handleApplicationSelection">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="applicationNo" label="申请编号" width="140" />
            <el-table-column prop="applicationDate" label="申请日期" width="100" />
            <el-table-column prop="supplierName" label="供应商" width="150" />
            <el-table-column prop="amount" label="申请金额" width="120" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.amount) }}
              </template>
            </el-table-column>
            <el-table-column prop="paymentMethod" label="付款方式" width="100" />
            <el-table-column prop="urgency" label="紧急程度" width="100">
              <template #default="{ row }">
                <el-tag :type="getUrgencyType(row.urgency)" size="small">
                  {{ row.urgency }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applicant" label="申请人" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getApplicationStatusType(row.status)" size="small">
                  {{ getApplicationStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="申请理由" min-width="200" />
            <el-table-column label="操作" fixed="right" width="200">
              <template #default="{ row }">
                <template v-if="row.status === 'pending'">
                  <el-button link type="success" @click="handleApprove(row)">审核</el-button>
                  <el-button link type="danger" @click="handleReject(row)">拒绝</el-button>
                </template>
                <template v-else-if="row.status === 'approved'">
                  <el-button link type="primary" @click="handleExecutePayment(row)">执行付款</el-button>
                </template>
                <el-button link type="info" @click="handleViewApplication(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      
      <!-- 付款执行 -->
      <el-tab-pane label="付款执行" name="payment">
        <el-card>
          <el-form :model="paymentForm" :rules="paymentRules" ref="paymentFormRef" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="付款申请" prop="applicationId">
                  <el-select v-model="paymentForm.applicationId" placeholder="请选择付款申请" style="width: 100%" @change="loadApplicationDetail">
                    <el-option
                      v-for="app in approvedApplications"
                      :key="app.id"
                      :label="`${app.applicationNo} - ${app.supplierName} - ${formatMoney(app.amount)}`"
                      :value="app.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="付款日期" prop="paymentDate">
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
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="付款金额" prop="amount">
                  <el-input-number
                    v-model="paymentForm.amount"
                    :min="0"
                    :precision="2"
                    style="width: 100%"
                    placeholder="请输入付款金额"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="付款方式" prop="paymentMethod">
                  <el-select v-model="paymentForm.paymentMethod" placeholder="请选择" style="width: 100%">
                    <el-option label="银行转账" value="transfer" />
                    <el-option label="现金" value="cash" />
                    <el-option label="支票" value="check" />
                    <el-option label="承兑汇票" value="draft" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="付款账户" prop="accountId">
                  <el-select v-model="paymentForm.accountId" placeholder="请选择付款账户" style="width: 100%">
                    <el-option label="中国银行基本户" value="1" />
                    <el-option label="工商银行一般户" value="2" />
                    <el-option label="现金" value="3" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="收款账户">
                  <el-input v-model="paymentForm.receiverAccount" placeholder="供应商收款账户" disabled />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item label="付款说明">
              <el-input v-model="paymentForm.remark" type="textarea" :rows="2" placeholder="请输入付款说明" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleSubmitPayment">确认付款</el-button>
              <el-button @click="handleResetPayment">重置</el-button>
              <el-button type="success" @click="handleGenerateVoucher">生成凭证</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <!-- 应付统计 -->
      <el-tab-pane label="应付统计" name="statistics">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>供应商应付排行</span>
              </template>
              <el-table :data="supplierRanking" border>
                <el-table-column type="index" label="排名" width="60" />
                <el-table-column prop="supplierName" label="供应商名称" />
                <el-table-column prop="amount" label="应付金额" width="120" align="right">
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
                <span>付款方式分布</span>
              </template>
              <div ref="paymentMethodChart" style="height: 300px"></div>
            </el-card>
          </el-col>
        </el-row>
        
        <el-card class="mt-lg">
          <template #header>
            <span>应付趋势分析</span>
          </template>
          <div ref="payableTrendChart" style="height: 350px"></div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 发票详情对话框 -->
    <el-dialog v-model="invoiceDialog" title="发票详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="发票号码">{{ currentInvoice.invoiceNo }}</el-descriptions-item>
        <el-descriptions-item label="发票日期">{{ currentInvoice.invoiceDate }}</el-descriptions-item>
        <el-descriptions-item label="供应商名称">{{ currentInvoice.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="发票金额">{{ formatMoney(currentInvoice.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="税额">{{ formatMoney(currentInvoice.taxAmount) }}</el-descriptions-item>
        <el-descriptions-item label="不含税金额">{{ formatMoney(currentInvoice.totalAmount - currentInvoice.taxAmount) }}</el-descriptions-item>
        <el-descriptions-item label="已付金额">{{ formatMoney(currentInvoice.paidAmount) }}</el-descriptions-item>
        <el-descriptions-item label="剩余金额">{{ formatMoney(currentInvoice.remainingAmount) }}</el-descriptions-item>
        <el-descriptions-item label="付款期限">{{ currentInvoice.dueDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getInvoiceStatusType(currentInvoice.status)">
            {{ getInvoiceStatusText(currentInvoice.status) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      
      <el-divider />
      
      <h4>付款记录</h4>
      <el-table :data="paymentHistory" border>
        <el-table-column prop="paymentDate" label="付款日期" width="100" />
        <el-table-column prop="amount" label="付款金额" width="120" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="付款方式" width="100" />
        <el-table-column prop="accountName" label="付款账户" width="150" />
        <el-table-column prop="remark" label="备注" />
      </el-table>
    </el-dialog>
    
    <!-- 付款申请对话框 -->
    <el-dialog v-model="applicationDialog" title="付款申请" width="600px">
      <el-form :model="applicationForm" :rules="applicationRules" ref="applicationFormRef" label-width="100px">
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="applicationForm.supplierId" placeholder="请选择供应商" style="width: 100%">
            <el-option
              v-for="supplier in suppliers"
              :key="supplier.id"
              :label="supplier.name"
              :value="supplier.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="申请金额" prop="amount">
          <el-input-number
            v-model="applicationForm.amount"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="付款方式" prop="paymentMethod">
          <el-select v-model="applicationForm.paymentMethod" placeholder="请选择" style="width: 100%">
            <el-option label="银行转账" value="transfer" />
            <el-option label="现金" value="cash" />
            <el-option label="支票" value="check" />
          </el-select>
        </el-form-item>
        <el-form-item label="紧急程度" prop="urgency">
          <el-radio-group v-model="applicationForm.urgency">
            <el-radio label="普通">普通</el-radio>
            <el-radio label="紧急">紧急</el-radio>
            <el-radio label="特急">特急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="申请理由" prop="reason">
          <el-input v-model="applicationForm.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applicationDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitApplication">提交申请</el-button>
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
const applicationDialog = ref(false)
const paymentFormRef = ref()
const applicationFormRef = ref()
const paymentMethodChart = ref(null)
const payableTrendChart = ref(null)
let paymentMethodChartInstance = null
let payableTrendChartInstance = null

// 应付概览数据
const payableSummary = ref({
  total: 1850000,
  thisMonth: 420000,
  thisMonthPaid: 380000,
  pending: 680000,
  pendingCount: 12
})

// 供应商列表
const suppliers = ref([
  { id: 1, name: '华强供应商' },
  { id: 2, name: '金泰材料' },
  { id: 3, name: '宏达贸易' },
  { id: 4, name: '兴业物流' }
])

// 发票搜索表单
const invoiceSearchForm = ref({
  invoiceNo: '',
  supplierId: '',
  dateRange: [],
  status: ''
})

// 申请搜索表单
const applicationSearchForm = ref({
  applicationNo: '',
  status: '',
  dateRange: []
})

// 付款表单
const paymentForm = ref({
  applicationId: '',
  paymentDate: new Date().toISOString().split('T')[0],
  amount: 0,
  paymentMethod: 'transfer',
  accountId: '',
  receiverAccount: '',
  remark: ''
})

// 申请表单
const applicationForm = ref({
  supplierId: '',
  amount: 0,
  paymentMethod: 'transfer',
  urgency: '普通',
  reason: ''
})

// 发票数据
const invoiceData = ref([
  {
    id: 1,
    invoiceNo: 'PI20250001',
    invoiceDate: '2025-08-01',
    supplierName: '华强供应商',
    totalAmount: 156000,
    taxAmount: 20280,
    paidAmount: 100000,
    remainingAmount: 56000,
    dueDate: '2025-08-31',
    overdueDays: 0,
    status: 'partial'
  },
  {
    id: 2,
    invoiceNo: 'PI20250002',
    invoiceDate: '2025-08-05',
    supplierName: '金泰材料',
    totalAmount: 234000,
    taxAmount: 30420,
    paidAmount: 0,
    remainingAmount: 234000,
    dueDate: '2025-09-05',
    overdueDays: 0,
    status: 'confirmed'
  },
  {
    id: 3,
    invoiceNo: 'PI20250003',
    invoiceDate: '2025-07-15',
    supplierName: '宏达贸易',
    totalAmount: 89000,
    taxAmount: 11570,
    paidAmount: 89000,
    remainingAmount: 0,
    dueDate: '2025-08-15',
    overdueDays: 0,
    status: 'paid'
  }
])

// 申请数据
const applicationData = ref([
  {
    id: 1,
    applicationNo: 'PA20250001',
    applicationDate: '2025-08-10',
    supplierName: '华强供应商',
    amount: 56000,
    paymentMethod: '银行转账',
    urgency: '普通',
    applicant: '采购部',
    status: 'pending',
    reason: '支付材料尾款'
  },
  {
    id: 2,
    applicationNo: 'PA20250002',
    applicationDate: '2025-08-12',
    supplierName: '金泰材料',
    amount: 234000,
    paymentMethod: '银行转账',
    urgency: '紧急',
    applicant: '采购部',
    status: 'approved',
    reason: '支付新订单货款'
  }
])

// 已审核申请（用于付款执行）
const approvedApplications = computed(() => {
  return applicationData.value.filter(app => app.status === 'approved')
})

// 供应商应付排行
const supplierRanking = ref([
  { supplierName: '华强供应商', amount: 450000, percentage: 24.3 },
  { supplierName: '金泰材料', amount: 380000, percentage: 20.5 },
  { supplierName: '宏达贸易', amount: 320000, percentage: 17.3 },
  { supplierName: '兴业物流', amount: 280000, percentage: 15.1 },
  { supplierName: '其他供应商', amount: 420000, percentage: 22.8 }
])

// 付款记录
const paymentHistory = ref([
  {
    paymentDate: '2025-08-08',
    amount: 100000,
    paymentMethod: '银行转账',
    accountName: '中国银行基本户',
    remark: '部分付款'
  }
])

// 当前查看的发票
const currentInvoice = ref({})

const paymentRules = {
  applicationId: [{ required: true, message: '请选择付款申请', trigger: 'change' }],
  paymentDate: [{ required: true, message: '请选择付款日期', trigger: 'change' }],
  amount: [{ required: true, message: '请输入付款金额', trigger: 'blur' }],
  paymentMethod: [{ required: true, message: '请选择付款方式', trigger: 'change' }],
  accountId: [{ required: true, message: '请选择付款账户', trigger: 'change' }]
}

const applicationRules = {
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  amount: [{ required: true, message: '请输入申请金额', trigger: 'blur' }],
  paymentMethod: [{ required: true, message: '请选择付款方式', trigger: 'change' }],
  urgency: [{ required: true, message: '请选择紧急程度', trigger: 'change' }],
  reason: [{ required: true, message: '请输入申请理由', trigger: 'blur' }]
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
    'confirmed': 'warning',
    'partial': 'primary',
    'paid': 'success',
    'overdue': 'danger'
  }
  return map[status] || ''
}

const getInvoiceStatusText = (status) => {
  const map = {
    'confirmed': '已确认',
    'partial': '部分付款',
    'paid': '已付清',
    'overdue': '已逾期'
  }
  return map[status] || status
}

const getApplicationStatusType = (status) => {
  const map = {
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'danger',
    'paid': 'info'
  }
  return map[status] || ''
}

const getApplicationStatusText = (status) => {
  const map = {
    'pending': '待审核',
    'approved': '已审核',
    'rejected': '已拒绝',
    'paid': '已付款'
  }
  return map[status] || status
}

const getUrgencyType = (urgency) => {
  const map = {
    '普通': '',
    '紧急': 'warning',
    '特急': 'danger'
  }
  return map[urgency] || ''
}

const initCharts = () => {
  // 付款方式分布图
  if (paymentMethodChart.value) {
    paymentMethodChartInstance = echarts.init(paymentMethodChart.value)
    const methodOption = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      series: [
        {
          name: '付款方式',
          type: 'pie',
          radius: ['30%', '70%'],
          data: [
            { value: 1200000, name: '银行转账' },
            { value: 350000, name: '支票' },
            { value: 200000, name: '承兑汇票' },
            { value: 100000, name: '现金' }
          ],
          label: {
            formatter: '{b}\n{d}%'
          }
        }
      ]
    }
    paymentMethodChartInstance.setOption(methodOption)
  }
  
  // 应付趋势图
  if (payableTrendChart.value) {
    payableTrendChartInstance = echarts.init(payableTrendChart.value)
    const trendOption = {
      title: {
        text: '应付账款趋势'
      },
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['应付余额', '新增应付', '付款金额']
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
          name: '应付余额',
          type: 'line',
          data: [180, 190, 170, 200, 185, 175, 165, 185],
          itemStyle: { color: '#f56c6c' }
        },
        {
          name: '新增应付',
          type: 'bar',
          data: [60, 70, 50, 80, 65, 55, 50, 42],
          itemStyle: { color: '#e6a23c' }
        },
        {
          name: '付款金额',
          type: 'bar',
          data: [50, 60, 60, 65, 70, 65, 60, 38],
          itemStyle: { color: '#67c23a' }
        }
      ]
    }
    payableTrendChartInstance.setOption(trendOption)
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
    supplierId: '',
    dateRange: [],
    status: ''
  }
}

const handleInvoiceSelection = (selection) => {
  // 处理发票选择
}

const handleAddInvoice = () => {
  ElMessage.info('发票录入功能开发中')
}

const handleBatchApply = () => {
  ElMessage.info('批量申请功能开发中')
}

const handleExportInvoice = () => {
  ElMessage.success('导出功能开发中')
}

const handleApplyPayment = (row) => {
  applicationForm.value = {
    supplierId: row.supplierId || 1,
    amount: row.remainingAmount,
    paymentMethod: 'transfer',
    urgency: '普通',
    reason: `支付发票${row.invoiceNo}款项`
  }
  applicationDialog.value = true
}

const handlePay = (row) => {
  ElMessage.info(`直接付款：${row.invoiceNo}`)
}

const handleViewInvoice = (row) => {
  currentInvoice.value = row
  invoiceDialog.value = true
}

const handleApplicationSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('查询完成')
  }, 500)
}

const handleApplicationReset = () => {
  applicationSearchForm.value = {
    applicationNo: '',
    status: '',
    dateRange: []
  }
}

const handleApplicationSelection = (selection) => {
  // 处理申请选择
}

const handleNewApplication = () => {
  applicationForm.value = {
    supplierId: '',
    amount: 0,
    paymentMethod: 'transfer',
    urgency: '普通',
    reason: ''
  }
  applicationDialog.value = true
}

const handleBatchApprove = () => {
  ElMessage.info('批量审核功能开发中')
}

const handleApprove = (row) => {
  row.status = 'approved'
  ElMessage.success('审核通过')
}

const handleReject = (row) => {
  row.status = 'rejected'
  ElMessage.success('审核拒绝')
}

const handleExecutePayment = (row) => {
  paymentForm.value.applicationId = row.id
  paymentForm.value.amount = row.amount
  activeTab.value = 'payment'
}

const handleViewApplication = (row) => {
  ElMessage.info(`查看申请详情：${row.applicationNo}`)
}

const loadApplicationDetail = () => {
  const app = applicationData.value.find(a => a.id === paymentForm.value.applicationId)
  if (app) {
    paymentForm.value.amount = app.amount
    paymentForm.value.receiverAccount = `${app.supplierName}收款账户`
  }
}

const handleSubmitPayment = async () => {
  const valid = await paymentFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  const app = applicationData.value.find(a => a.id === paymentForm.value.applicationId)
  if (app) {
    app.status = 'paid'
  }
  
  ElMessage.success('付款执行成功')
  handleResetPayment()
}

const handleResetPayment = () => {
  paymentFormRef.value?.resetFields()
}

const handleGenerateVoucher = () => {
  ElMessage.success('凭证生成功能开发中')
}

const handleSubmitApplication = async () => {
  const valid = await applicationFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  applicationDialog.value = false
  ElMessage.success('申请提交成功')
}

onMounted(() => {
  nextTick(() => {
    initCharts()
  })
  
  window.addEventListener('resize', () => {
    paymentMethodChartInstance?.resize()
    payableTrendChartInstance?.resize()
  })
})
</script>

<style scoped lang="scss">
.payable-overview {
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

.amount-paid {
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
  .payable-overview {
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