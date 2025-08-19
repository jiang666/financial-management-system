<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">支票管理</h2>
      <p class="description">管理支票的申领、开具、核销和作废登记</p>
    </div>
    
    <!-- 统计概览 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="库存支票" :value="checkStock.total">
            <template #suffix>
              <span>张</span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="已开具" :value="checkStock.used">
            <template #suffix>
              <span>张</span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="已核销" :value="checkStock.cleared">
            <template #suffix>
              <span>张</span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="已作废" :value="checkStock.voided">
            <template #suffix>
              <span>张</span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 功能区 -->
    <el-tabs v-model="activeTab">
      <!-- 支票台账 -->
      <el-tab-pane label="支票台账" name="ledger">
        <div class="search-form">
          <el-form :model="searchForm" inline>
            <el-form-item label="支票号码">
              <el-input v-model="searchForm.checkNo" placeholder="请输入支票号码" clearable />
            </el-form-item>
            <el-form-item label="支票状态">
              <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
                <el-option label="库存" value="stock" />
                <el-option label="已开具" value="issued" />
                <el-option label="已核销" value="cleared" />
                <el-option label="已作废" value="voided" />
              </el-select>
            </el-form-item>
            <el-form-item label="日期范围">
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
              <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
              <el-button icon="Refresh" @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <div class="table-wrapper">
          <div class="table-header">
            <el-button type="primary" icon="Plus" @click="handleRegister">支票申领</el-button>
            <el-button type="success" icon="Edit" @click="handleIssue">开具支票</el-button>
            <el-button icon="Download" @click="handleExport">导出台账</el-button>
          </div>
          
          <el-table :data="paginatedCheckData" v-loading="loading" @selection-change="handleSelectionChange" border stripe style="width: 100%">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="checkNo" label="支票号码" width="120" fixed="left" align="center" />
            <el-table-column prop="checkType" label="支票类型" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getCheckTypeTag(row.checkType)" size="small">
                  {{ row.checkType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="bankName" label="开户银行" width="120" align="center" />
            <el-table-column prop="accountNumber" label="银行账号" width="160" align="center" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" effect="dark" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="payee" label="收款人" width="120" align="center" />
            <el-table-column prop="amount" label="金额" width="120" align="right">
              <template #default="{ row }">
                <span v-if="row.amount" class="amount-text">{{ formatMoney(row.amount) }}</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="issueDate" label="开具日期" width="110" align="center" />
            <el-table-column prop="dueDate" label="到期日" width="110" align="center" />
            <el-table-column prop="purpose" label="用途" min-width="180">
              <template #default="{ row }">
                <el-tooltip :content="row.purpose" placement="top" v-if="row.purpose && row.purpose.length > 20">
                  <span>{{ row.purpose.substring(0, 20) }}...</span>
                </el-tooltip>
                <span v-else>{{ row.purpose || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="operator" label="经办人" width="100" align="center" />
            <el-table-column label="操作" fixed="right" width="200" align="center">
              <template #default="{ row }">
                <template v-if="row.status === 'stock'">
                  <el-button link type="primary" size="small" @click="handleIssueCheck(row)">开具</el-button>
                  <el-button link type="danger" size="small" @click="handleVoid(row)">作废</el-button>
                </template>
                <template v-else-if="row.status === 'issued'">
                  <el-button link type="success" size="small" @click="handleClear(row)">核销</el-button>
                  <el-button link type="danger" size="small" @click="handleVoid(row)">作废</el-button>
                </template>
                <el-button link type="info" size="small" @click="handleView(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="pagination.currentPage"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :small="false"
              :disabled="loading"
              :background="true"
              layout="total, sizes, prev, pager, next, jumper"
              :total="filteredCheckData.length"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </el-tab-pane>
      
      <!-- 支票申领 -->
      <el-tab-pane label="支票申领" name="register">
        <el-card>
          <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="申领日期" prop="applyDate">
                  <el-date-picker
                    v-model="registerForm.applyDate"
                    type="date"
                    placeholder="选择日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="支票类型" prop="checkType">
                  <el-select v-model="registerForm.checkType" placeholder="请选择" style="width: 100%">
                    <el-option label="现金支票" value="现金支票" />
                    <el-option label="转账支票" value="转账支票" />
                    <el-option label="普通支票" value="普通支票" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="开户银行" prop="bankName">
                  <el-select v-model="registerForm.bankName" placeholder="请选择银行" style="width: 100%">
                    <el-option label="中国银行" value="中国银行" />
                    <el-option label="工商银行" value="工商银行" />
                    <el-option label="建设银行" value="建设银行" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="申领数量" prop="quantity">
                  <el-input-number v-model="registerForm.quantity" :min="1" :max="100" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="起始号码" prop="startNo">
                  <el-input v-model="registerForm.startNo" placeholder="请输入起始号码" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="结束号码">
                  <el-input v-model="endNo" placeholder="自动计算" disabled />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item label="申领人" prop="applicant">
              <el-input v-model="registerForm.applicant" placeholder="请输入申领人" />
            </el-form-item>
            
            <el-form-item label="申领原因" prop="reason">
              <el-input v-model="registerForm.reason" type="textarea" :rows="2" placeholder="请输入申领原因" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleSubmitRegister">提交申领</el-button>
              <el-button @click="handleResetRegister">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <!-- 支票开具 -->
      <el-tab-pane label="支票开具" name="issue">
        <el-card>
          <el-form :model="issueForm" :rules="issueRules" ref="issueFormRef" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="支票号码" prop="checkNo">
                  <el-select v-model="issueForm.checkNo" placeholder="请选择支票" style="width: 100%">
                    <el-option
                      v-for="check in availableChecks"
                      :key="check.checkNo"
                      :label="`${check.checkNo} - ${check.checkType}`"
                      :value="check.checkNo"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="开具日期" prop="issueDate">
                  <el-date-picker
                    v-model="issueForm.issueDate"
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
                <el-form-item label="收款人" prop="payee">
                  <el-input v-model="issueForm.payee" placeholder="请输入收款人名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="金额" prop="amount">
                  <el-input-number
                    v-model="issueForm.amount"
                    :min="0"
                    :precision="2"
                    style="width: 100%"
                    placeholder="请输入金额"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item label="金额大写">
              <el-input v-model="amountInWords" placeholder="自动转换" disabled />
            </el-form-item>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="到期日" prop="dueDate">
                  <el-date-picker
                    v-model="issueForm.dueDate"
                    type="date"
                    placeholder="选择到期日"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="经办人" prop="operator">
                  <el-input v-model="issueForm.operator" placeholder="请输入经办人" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item label="用途" prop="purpose">
              <el-input v-model="issueForm.purpose" type="textarea" :rows="2" placeholder="请输入支票用途" />
            </el-form-item>
            
            <el-form-item label="备注">
              <el-input v-model="issueForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleSubmitIssue">确认开具</el-button>
              <el-button @click="handleResetIssue">重置</el-button>
              <el-button type="success" @click="handlePrintCheck">打印支票</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <!-- 支票核销 -->
      <el-tab-pane label="支票核销" name="clear">
        <div class="search-form">
          <el-form :model="clearSearchForm" inline>
            <el-form-item label="待核销支票">
              <el-select v-model="clearSearchForm.checkNo" placeholder="请选择" style="width: 200px">
                <el-option
                  v-for="check in pendingClearChecks"
                  :key="check.checkNo"
                  :label="`${check.checkNo} - ${check.payee} - ${formatMoney(check.amount)}`"
                  :value="check.checkNo"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadCheckDetail">查询详情</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <el-card v-if="clearingCheck">
          <el-descriptions title="支票信息" :column="2" border>
            <el-descriptions-item label="支票号码">{{ clearingCheck.checkNo }}</el-descriptions-item>
            <el-descriptions-item label="支票类型">{{ clearingCheck.checkType }}</el-descriptions-item>
            <el-descriptions-item label="收款人">{{ clearingCheck.payee }}</el-descriptions-item>
            <el-descriptions-item label="金额">{{ formatMoney(clearingCheck.amount) }}</el-descriptions-item>
            <el-descriptions-item label="开具日期">{{ clearingCheck.issueDate }}</el-descriptions-item>
            <el-descriptions-item label="到期日">{{ clearingCheck.dueDate }}</el-descriptions-item>
            <el-descriptions-item label="用途" :span="2">{{ clearingCheck.purpose }}</el-descriptions-item>
          </el-descriptions>
          
          <el-divider />
          
          <el-form :model="clearForm" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="核销日期">
                  <el-date-picker
                    v-model="clearForm.clearDate"
                    type="date"
                    placeholder="选择日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="实际金额">
                  <el-input-number
                    v-model="clearForm.actualAmount"
                    :min="0"
                    :precision="2"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item label="核销说明">
              <el-input v-model="clearForm.clearRemark" type="textarea" :rows="2" placeholder="请输入核销说明" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="success" @click="handleConfirmClear">确认核销</el-button>
              <el-button @click="clearingCheck = null">取消</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 支票详情对话框 -->
    <el-dialog v-model="detailDialog" title="支票详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="支票号码">{{ currentCheck.checkNo }}</el-descriptions-item>
        <el-descriptions-item label="支票类型">{{ currentCheck.checkType }}</el-descriptions-item>
        <el-descriptions-item label="开户银行">{{ currentCheck.bankName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentCheck.status)">
            {{ getStatusText(currentCheck.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收款人">{{ currentCheck.payee || '-' }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ currentCheck.amount ? formatMoney(currentCheck.amount) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="开具日期">{{ currentCheck.issueDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="到期日">{{ currentCheck.dueDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用途" :span="2">{{ currentCheck.purpose || '-' }}</el-descriptions-item>
        <el-descriptions-item label="经办人">{{ currentCheck.operator }}</el-descriptions-item>
        <el-descriptions-item label="申领日期">{{ currentCheck.registerDate }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider />
      
      <h4>操作记录</h4>
      <el-timeline>
        <el-timeline-item
          v-for="log in checkLogs"
          :key="log.id"
          :timestamp="log.time"
          :type="log.type"
        >
          {{ log.action }} - {{ log.operator }}
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('ledger')
const loading = ref(false)
const detailDialog = ref(false)
const selectedRows = ref([])
const registerFormRef = ref()
const issueFormRef = ref()
const currentCheck = ref({})
const clearingCheck = ref(null)

// 统计数据
const checkStock = ref({
  total: 50,
  used: 15,
  cleared: 10,
  voided: 2
})

// 搜索表单
const searchForm = ref({
  checkNo: '',
  status: '',
  dateRange: []
})

// 申领表单
const registerForm = ref({
  applyDate: new Date().toISOString().split('T')[0],
  checkType: '转账支票',
  bankName: '',
  quantity: 10,
  startNo: '',
  applicant: '',
  reason: ''
})

// 开具表单
const issueForm = ref({
  checkNo: '',
  issueDate: new Date().toISOString().split('T')[0],
  payee: '',
  amount: 0,
  dueDate: '',
  operator: '',
  purpose: '',
  remark: ''
})

// 核销搜索
const clearSearchForm = ref({
  checkNo: ''
})

// 核销表单
const clearForm = ref({
  clearDate: new Date().toISOString().split('T')[0],
  actualAmount: 0,
  clearRemark: ''
})

// 支票数据
const checkData = ref([
  {
    id: 1,
    checkNo: 'CK20250001',
    checkType: '转账支票',
    bankName: '中国银行',
    accountNumber: '6217001234567890123',
    status: 'stock',
    payee: '',
    amount: 0,
    issueDate: '',
    dueDate: '',
    purpose: '',
    operator: '财务部',
    registerDate: '2025-08-01'
  },
  {
    id: 2,
    checkNo: 'CK20250002',
    checkType: '转账支票',
    bankName: '中国银行',
    accountNumber: '6217001234567890123',
    status: 'issued',
    payee: 'ABC供应商',
    amount: 50000,
    issueDate: '2025-08-10',
    dueDate: '2025-08-20',
    purpose: '支付货款',
    operator: '张三',
    registerDate: '2025-08-01'
  },
  {
    id: 3,
    checkNo: 'CK20250003',
    checkType: '现金支票',
    bankName: '工商银行',
    accountNumber: '6222021234567890456',
    status: 'cleared',
    payee: '员工报销',
    amount: 8500,
    issueDate: '2025-08-05',
    dueDate: '2025-08-15',
    purpose: '差旅费报销',
    operator: '李四',
    registerDate: '2025-08-01'
  },
  {
    id: 4,
    checkNo: 'CK20250004',
    checkType: '普通支票',
    bankName: '建设银行',
    accountNumber: '6227001234567890789',
    status: 'stock',
    payee: '',
    amount: 0,
    issueDate: '',
    dueDate: '',
    purpose: '',
    operator: '财务部',
    registerDate: '2025-08-02'
  },
  {
    id: 5,
    checkNo: 'CK20250005',
    checkType: '转账支票',
    bankName: '中国银行',
    accountNumber: '6217001234567890123',
    status: 'issued',
    payee: 'XYZ科技有限公司',
    amount: 125000,
    issueDate: '2025-08-12',
    dueDate: '2025-08-22',
    purpose: '采购设备款项支付',
    operator: '王五',
    registerDate: '2025-08-02'
  },
  {
    id: 6,
    checkNo: 'CK20250006',
    checkType: '现金支票',
    bankName: '工商银行',
    accountNumber: '6222021234567890456',
    status: 'voided',
    payee: '临时供应商',
    amount: 3200,
    issueDate: '2025-08-08',
    dueDate: '2025-08-18',
    purpose: '办公用品采购（已作废）',
    operator: '赵六',
    registerDate: '2025-08-03'
  },
  {
    id: 7,
    checkNo: 'CK20250007',
    checkType: '转账支票',
    bankName: '建设银行',
    accountNumber: '6227001234567890789',
    status: 'cleared',
    payee: '德邦物流',
    amount: 18600,
    issueDate: '2025-08-06',
    dueDate: '2025-08-16',
    purpose: '物流运输费用',
    operator: '孙七',
    registerDate: '2025-08-03'
  },
  {
    id: 8,
    checkNo: 'CK20250008',
    checkType: '普通支票',
    bankName: '农业银行',
    accountNumber: '6228481234567890012',
    status: 'stock',
    payee: '',
    amount: 0,
    issueDate: '',
    dueDate: '',
    purpose: '',
    operator: '财务部',
    registerDate: '2025-08-04'
  },
  {
    id: 9,
    checkNo: 'CK20250009',
    checkType: '现金支票',
    bankName: '招商银行',
    accountNumber: '6225881234567890345',
    status: 'issued',
    payee: '市政公用事业',
    amount: 7800,
    issueDate: '2025-08-14',
    dueDate: '2025-08-24',
    purpose: '水电费缴纳',
    operator: '周八',
    registerDate: '2025-08-04'
  },
  {
    id: 10,
    checkNo: 'CK20250010',
    checkType: '转账支票',
    bankName: '交通银行',
    accountNumber: '6222601234567890678',
    status: 'cleared',
    payee: '华为技术有限公司',
    amount: 286000,
    issueDate: '2025-08-09',
    dueDate: '2025-08-19',
    purpose: '通信设备采购款',
    operator: '吴九',
    registerDate: '2025-08-05'
  },
  {
    id: 11,
    checkNo: 'CK20250011',
    checkType: '普通支票',
    bankName: '光大银行',
    accountNumber: '6226621234567890901',
    status: 'stock',
    payee: '',
    amount: 0,
    issueDate: '',
    dueDate: '',
    purpose: '',
    operator: '财务部',
    registerDate: '2025-08-05'
  },
  {
    id: 12,
    checkNo: 'CK20250012',
    checkType: '现金支票',
    bankName: '民生银行',
    accountNumber: '6226221234567890234',
    status: 'issued',
    payee: '员工福利中心',
    amount: 15500,
    issueDate: '2025-08-13',
    dueDate: '2025-08-23',
    purpose: '员工生日福利发放',
    operator: '郑十',
    registerDate: '2025-08-06'
  }
])

// 筛选后的支票数据
const filteredCheckData = computed(() => {
  let filtered = checkData.value
  
  if (searchForm.value.checkNo) {
    filtered = filtered.filter(check => 
      check.checkNo.toLowerCase().includes(searchForm.value.checkNo.toLowerCase())
    )
  }
  
  if (searchForm.value.status) {
    filtered = filtered.filter(check => check.status === searchForm.value.status)
  }
  
  if (searchForm.value.dateRange && searchForm.value.dateRange.length === 2) {
    const [startDate, endDate] = searchForm.value.dateRange
    filtered = filtered.filter(check => {
      if (!check.issueDate) return false
      return check.issueDate >= startDate && check.issueDate <= endDate
    })
  }
  
  return filtered
})

// 分页数据
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 分页后的支票数据
const paginatedCheckData = computed(() => {
  const start = (pagination.value.currentPage - 1) * pagination.value.pageSize
  const end = start + pagination.value.pageSize
  return filteredCheckData.value.slice(start, end)
})

// 可用支票
const availableChecks = computed(() => {
  return checkData.value.filter(check => check.status === 'stock')
})

// 待核销支票
const pendingClearChecks = computed(() => {
  return checkData.value.filter(check => check.status === 'issued')
})

// 支票操作日志
const checkLogs = ref([
  { id: 1, time: '2025-08-01 09:00', action: '支票申领', operator: '财务部', type: 'primary' },
  { id: 2, time: '2025-08-10 14:30', action: '支票开具', operator: '张三', type: 'success' },
  { id: 3, time: '2025-08-15 16:00', action: '支票核销', operator: '李四', type: 'warning' }
])

// 计算结束号码
const endNo = computed(() => {
  if (registerForm.value.startNo && registerForm.value.quantity) {
    const start = parseInt(registerForm.value.startNo.replace(/\D/g, ''))
    const prefix = registerForm.value.startNo.replace(/\d/g, '')
    const end = start + registerForm.value.quantity - 1
    return prefix + String(end).padStart(registerForm.value.startNo.replace(/\D/g, '').length, '0')
  }
  return ''
})

// 金额大写转换
const amountInWords = computed(() => {
  if (!issueForm.value.amount) return ''
  return convertToChineseNumber(issueForm.value.amount)
})

const registerRules = {
  applyDate: [{ required: true, message: '请选择申领日期', trigger: 'change' }],
  checkType: [{ required: true, message: '请选择支票类型', trigger: 'change' }],
  bankName: [{ required: true, message: '请选择开户银行', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入申领数量', trigger: 'blur' }],
  startNo: [{ required: true, message: '请输入起始号码', trigger: 'blur' }],
  applicant: [{ required: true, message: '请输入申领人', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入申领原因', trigger: 'blur' }]
}

const issueRules = {
  checkNo: [{ required: true, message: '请选择支票', trigger: 'change' }],
  issueDate: [{ required: true, message: '请选择开具日期', trigger: 'change' }],
  payee: [{ required: true, message: '请输入收款人', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  dueDate: [{ required: true, message: '请选择到期日', trigger: 'change' }],
  operator: [{ required: true, message: '请输入经办人', trigger: 'blur' }],
  purpose: [{ required: true, message: '请输入用途', trigger: 'blur' }]
}

const formatMoney = (amount) => {
  if (!amount) return '¥0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const convertToChineseNumber = (amount) => {
  const digits = ['零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖']
  const units = ['', '拾', '佰', '仟', '万', '拾', '佰', '仟', '亿']
  const str = amount.toFixed(2).replace('.', '')
  let result = ''
  
  for (let i = 0; i < str.length - 2; i++) {
    const digit = parseInt(str[i])
    if (digit !== 0) {
      result += digits[digit] + units[str.length - 3 - i]
    } else if (result[result.length - 1] !== '零') {
      result += '零'
    }
  }
  
  result += '元'
  const jiao = parseInt(str[str.length - 2])
  const fen = parseInt(str[str.length - 1])
  
  if (jiao > 0) result += digits[jiao] + '角'
  if (fen > 0) result += digits[fen] + '分'
  if (jiao === 0 && fen === 0) result += '整'
  
  return result
}

const getCheckTypeTag = (type) => {
  const map = {
    '现金支票': 'success',
    '转账支票': 'primary',
    '普通支票': 'info'
  }
  return map[type] || ''
}

const getStatusType = (status) => {
  const map = {
    'stock': '',
    'issued': 'warning',
    'cleared': 'success',
    'voided': 'danger'
  }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = {
    'stock': '库存',
    'issued': '已开具',
    'cleared': '已核销',
    'voided': '已作废'
  }
  return map[status] || status
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('查询完成')
  }, 500)
}

const handleReset = () => {
  searchForm.value = {
    checkNo: '',
    status: '',
    dateRange: []
  }
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleRegister = () => {
  activeTab.value = 'register'
}

const handleIssue = () => {
  activeTab.value = 'issue'
}

const handleExport = () => {
  ElMessage.success('导出功能开发中')
}

const handleSubmitRegister = async () => {
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  ElMessage.success('支票申领成功')
  handleResetRegister()
}

const handleResetRegister = () => {
  registerFormRef.value?.resetFields()
}

const handleSubmitIssue = async () => {
  const valid = await issueFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  ElMessage.success('支票开具成功')
  handleResetIssue()
}

const handleResetIssue = () => {
  issueFormRef.value?.resetFields()
}

const handlePrintCheck = () => {
  ElMessage.success('打印功能开发中')
}

const handleIssueCheck = (row) => {
  issueForm.value.checkNo = row.checkNo
  activeTab.value = 'issue'
}

const handleClear = (row) => {
  clearSearchForm.value.checkNo = row.checkNo
  activeTab.value = 'clear'
  loadCheckDetail()
}

const handleVoid = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要作废支票 ${row.checkNo} 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    row.status = 'voided'
    ElMessage.success('支票已作废')
  } catch {
    // 取消作废
  }
}

const handleSizeChange = (val) => {
  pagination.value.pageSize = val
  pagination.value.currentPage = 1
}

const handleCurrentChange = (val) => {
  pagination.value.currentPage = val
}

const handleView = (row) => {
  currentCheck.value = row
  detailDialog.value = true
}

const loadCheckDetail = () => {
  const check = checkData.value.find(c => c.checkNo === clearSearchForm.value.checkNo)
  if (check) {
    clearingCheck.value = check
    clearForm.value.actualAmount = check.amount
  }
}

const handleConfirmClear = () => {
  const check = checkData.value.find(c => c.checkNo === clearingCheck.value.checkNo)
  if (check) {
    check.status = 'cleared'
    clearingCheck.value = null
    ElMessage.success('支票核销成功')
  }
}
</script>

<style scoped lang="scss">
.stat-cards {
  margin-bottom: 20px;
  
  .stat-card {
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
  }
}

.table-wrapper {
  .table-header {
    margin-bottom: 16px;
    display: flex;
    gap: 10px;
  }
  
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 24px;
    padding-top: 16px;
  }
}

@media (max-width: 768px) {
  .stat-cards {
    .el-col {
      margin-bottom: 16px;
    }
  }
}
</style>