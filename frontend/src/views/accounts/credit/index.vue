<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">信用管理</h2>
      <p class="description">设置信用额度、信用检查和预警提醒</p>
    </div>
    
    <!-- 信用概览 -->
    <el-row :gutter="20" class="credit-overview">
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="信用总额度" :value="creditSummary.totalLimit" :precision="2" prefix="¥">
            <template #suffix>
              <el-tag size="small">{{ creditSummary.customerCount }}家客户</el-tag>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="已用额度" :value="creditSummary.usedLimit" :precision="2" prefix="¥">
            <template #suffix>
              <span class="usage-rate">{{ creditSummary.usageRate }}%</span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="可用额度" :value="creditSummary.availableLimit" :precision="2" prefix="¥">
            <template #suffix>
              <span class="available-rate">{{ creditSummary.availableRate }}%</span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="超限客户" :value="creditSummary.overlimitCustomers">
            <template #suffix>
              <el-tag type="danger" size="small">家</el-tag>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>
    
    <el-tabs v-model="activeTab">
      <!-- 信用额度设置 -->
      <el-tab-pane label="信用额度" name="limit">
        <div class="search-form">
          <el-form :model="limitSearchForm" inline>
            <el-form-item label="客户名称">
              <el-input v-model="limitSearchForm.customerName" placeholder="请输入客户名称" clearable />
            </el-form-item>
            <el-form-item label="信用等级">
              <el-select v-model="limitSearchForm.creditGrade" placeholder="请选择" clearable style="width: 120px">
                <el-option label="AAA" value="AAA" />
                <el-option label="AA" value="AA" />
                <el-option label="A" value="A" />
                <el-option label="BBB" value="BBB" />
                <el-option label="BB" value="BB" />
              </el-select>
            </el-form-item>
            <el-form-item label="额度状态">
              <el-select v-model="limitSearchForm.status" placeholder="请选择" clearable style="width: 120px">
                <el-option label="正常" value="normal" />
                <el-option label="超限" value="overlimit" />
                <el-option label="冻结" value="frozen" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleLimitSearch">搜索</el-button>
              <el-button icon="Refresh" @click="handleLimitReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <div class="table-wrapper">
          <div class="table-header">
            <el-button type="primary" icon="Plus" @click="handleSetLimit">设置额度</el-button>
            <el-button type="warning" icon="Edit" @click="handleBatchAdjust">批量调整</el-button>
            <el-button icon="Download" @click="handleExportLimit">导出</el-button>
          </div>
          
          <el-table :data="creditLimitData" v-loading="loading" @selection-change="handleLimitSelection" style="width: 100%" stripe>
            <el-table-column type="selection" width="55" />
            <el-table-column prop="customerName" label="客户名称" min-width="120" show-overflow-tooltip />
            <el-table-column prop="customerCode" label="客户编码" width="120" show-overflow-tooltip />
            <el-table-column prop="creditGrade" label="信用等级" width="100">
              <template #default="{ row }">
                <el-tag :type="getCreditGradeType(row.creditGrade)" size="small">
                  {{ row.creditGrade }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="creditLimit" label="信用额度" width="120" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.creditLimit) }}
              </template>
            </el-table-column>
            <el-table-column prop="usedAmount" label="已用额度" width="120" align="right">
              <template #default="{ row }">
                <span class="used-amount">{{ formatMoney(row.usedAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="availableAmount" label="可用额度" width="120" align="right">
              <template #default="{ row }">
                <span :class="row.availableAmount > 0 ? 'available-amount' : 'overlimit-amount'">
                  {{ formatMoney(row.availableAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="usageRate" label="使用率" width="120" align="right">
              <template #default="{ row }">
                <el-progress :percentage="row.usageRate" :status="getUsageStatus(row.usageRate)" :stroke-width="8" />
              </template>
            </el-table-column>
            <el-table-column prop="effectiveDate" label="生效日期" width="110" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getCreditStatusType(row.status)" size="small">
                  {{ getCreditStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="240" fixed="right">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button link type="primary" size="small" @click="handleAdjustLimit(row)">调整</el-button>
                  <el-button link type="warning" size="small" @click="handleFreezeLimit(row)" v-if="row.status === 'normal'">冻结</el-button>
                  <el-button link type="success" size="small" @click="handleUnfreezeLimit(row)" v-if="row.status === 'frozen'">解冻</el-button>
                  <el-button link type="info" size="small" @click="handleViewHistory(row)">历史</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      
      <!-- 信用检查 -->
      <el-tab-pane label="信用检查" name="check">
        <el-card>
          <el-form :model="checkForm" :rules="checkRules" ref="checkFormRef" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="客户" prop="customerId">
                  <el-select v-model="checkForm.customerId" placeholder="请选择客户" style="width: 100%" @change="loadCustomerCredit">
                    <el-option
                      v-for="customer in customers"
                      :key="customer.id"
                      :label="customer.name"
                      :value="customer.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="交易金额" prop="transactionAmount">
                  <el-input-number
                    v-model="checkForm.transactionAmount"
                    :min="0"
                    :precision="2"
                    style="width: 100%"
                    placeholder="请输入交易金额"
                    @change="performCreditCheck"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item label="交易类型" prop="transactionType">
              <el-radio-group v-model="checkForm.transactionType">
                <el-radio label="sale">销售订单</el-radio>
                <el-radio label="delivery">发货单</el-radio>
                <el-radio label="invoice">开票</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleCreditCheck">信用检查</el-button>
              <el-button @click="handleResetCheck">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <!-- 检查结果 -->
        <el-card v-if="checkResult" class="check-result-card">
          <template #header>
            <span>信用检查结果</span>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="客户名称">{{ checkResult.customerName }}</el-descriptions-item>
                <el-descriptions-item label="信用等级">
                  <el-tag :type="getCreditGradeType(checkResult.creditGrade)">
                    {{ checkResult.creditGrade }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="信用额度">{{ formatMoney(checkResult.creditLimit) }}</el-descriptions-item>
                <el-descriptions-item label="已用额度">{{ formatMoney(checkResult.usedAmount) }}</el-descriptions-item>
                <el-descriptions-item label="可用额度">{{ formatMoney(checkResult.availableAmount) }}</el-descriptions-item>
                <el-descriptions-item label="本次交易">{{ formatMoney(checkResult.transactionAmount) }}</el-descriptions-item>
                <el-descriptions-item label="交易后余额">{{ formatMoney(checkResult.afterTransactionBalance) }}</el-descriptions-item>
              </el-descriptions>
            </el-col>
            
            <el-col :span="12">
              <div class="check-status">
                <el-result
                  :icon="checkResult.passed ? 'success' : 'warning'"
                  :title="checkResult.passed ? '信用检查通过' : '信用检查未通过'"
                  :sub-title="checkResult.message"
                >
                  <template #extra>
                    <el-button v-if="checkResult.passed" type="success">继续交易</el-button>
                    <el-button v-else type="primary" @click="handleApprovalRequest">申请审批</el-button>
                  </template>
                </el-result>
              </div>
            </el-col>
          </el-row>
          
          <div v-if="checkResult.warnings && checkResult.warnings.length > 0" class="warnings">
            <h4>风险提示</h4>
            <el-alert
              v-for="warning in checkResult.warnings"
              :key="warning"
              :title="warning"
              type="warning"
              :closable="false"
              class="warning-item"
            />
          </div>
        </el-card>
      </el-tab-pane>
      
      <!-- 预警设置 -->
      <el-tab-pane label="预警设置" name="warning">
        <el-card>
          <el-form :model="warningForm" label-width="150px">
            <h4>额度预警设置</h4>
            <el-form-item label="预警阈值">
              <el-input-number v-model="warningForm.warningThreshold" :min="0" :max="100" :precision="0" />
              <span style="margin-left: 8px">%（当使用率超过此值时预警）</span>
            </el-form-item>
            
            <el-form-item label="危险阈值">
              <el-input-number v-model="warningForm.dangerThreshold" :min="0" :max="100" :precision="0" />
              <span style="margin-left: 8px">%（当使用率超过此值时禁止交易）</span>
            </el-form-item>
            
            <h4>逾期预警设置</h4>
            <el-form-item label="逾期预警天数">
              <el-input-number v-model="warningForm.overdueDays" :min="1" :precision="0" />
              <span style="margin-left: 8px">天（逾期超过此天数时预警）</span>
            </el-form-item>
            
            <el-form-item label="自动冻结">
              <el-switch v-model="warningForm.autoFreeze" />
              <span style="margin-left: 8px">逾期超过指定天数自动冻结信用额度</span>
            </el-form-item>
            
            <el-form-item label="自动冻结天数" v-if="warningForm.autoFreeze">
              <el-input-number v-model="warningForm.autoFreezeDays" :min="1" :precision="0" />
              <span style="margin-left: 8px">天</span>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleSaveWarningSettings">保存设置</el-button>
              <el-button @click="handleResetWarningSettings">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <!-- 预警列表 -->
        <el-card class="warning-list-card">
          <template #header>
            <span>当前预警</span>
          </template>
          
          <el-table :data="warningList" border style="width: 100%" stripe>
            <el-table-column prop="customerName" label="客户名称" min-width="120" show-overflow-tooltip />
            <el-table-column prop="warningType" label="预警类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getWarningType(row.warningType)" size="small">
                  {{ getWarningText(row.warningType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="warningLevel" label="预警级别" width="100">
              <template #default="{ row }">
                <el-tag :type="getWarningLevelType(row.warningLevel)" size="small">
                  {{ row.warningLevel }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="currentValue" label="当前值" width="120" align="right" />
            <el-table-column prop="thresholdValue" label="阈值" width="120" align="right" />
            <el-table-column prop="warningTime" label="预警时间" width="150" />
            <el-table-column prop="description" label="预警描述" min-width="150" show-overflow-tooltip />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button link type="primary" size="small" @click="handleProcessWarning(row)">处理</el-button>
                  <el-button link type="info" size="small" @click="handleIgnoreWarning(row)">忽略</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 设置额度对话框 -->
    <el-dialog v-model="limitDialog" :title="limitDialogTitle" width="600px">
      <el-form :model="limitForm" :rules="limitRules" ref="limitFormRef" label-width="100px">
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="limitForm.customerId" placeholder="请选择客户" style="width: 100%">
            <el-option
              v-for="customer in customers"
              :key="customer.id"
              :label="customer.name"
              :value="customer.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="信用等级" prop="creditGrade">
          <el-select v-model="limitForm.creditGrade" placeholder="请选择" style="width: 100%">
            <el-option label="AAA" value="AAA" />
            <el-option label="AA" value="AA" />
            <el-option label="A" value="A" />
            <el-option label="BBB" value="BBB" />
            <el-option label="BB" value="BB" />
          </el-select>
        </el-form-item>
        <el-form-item label="信用额度" prop="creditLimit">
          <el-input-number
            v-model="limitForm.creditLimit"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="生效日期" prop="effectiveDate">
          <el-date-picker
            v-model="limitForm.effectiveDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="limitForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="limitDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitLimit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 信用历史对话框 -->
    <el-dialog v-model="historyDialog" :title="`${currentCustomerName} - 信用额度历史`" width="1000px">
      <!-- 历史概览卡片 -->
      <el-row :gutter="16" class="history-overview">
        <el-col :span="6">
          <el-card class="history-stat-card">
            <el-statistic title="当前额度" :value="historyStats.currentLimit" :precision="2" prefix="¥" />
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="history-stat-card">
            <el-statistic title="历史最高" :value="historyStats.maxLimit" :precision="2" prefix="¥" />
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="history-stat-card">
            <el-statistic title="调整次数" :value="historyStats.adjustCount">
              <template #suffix>
                <span>次</span>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="history-stat-card">
            <el-statistic title="平均额度" :value="historyStats.avgLimit" :precision="2" prefix="¥" />
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 额度变化趋势图 -->
      <el-card class="trend-card">
        <template #header>
          <span>额度变化趋势</span>
        </template>
        <div ref="limitTrendChart" style="height: 300px"></div>
      </el-card>
      
      <!-- 历史记录表格 -->
      <el-card class="history-table-card">
        <template #header>
          <div class="card-header">
            <span>变更历史记录</span>
            <div class="header-actions">
              <el-button type="primary" size="small" @click="handleExportHistory">导出历史</el-button>
            </div>
          </div>
        </template>
        
        <el-table :data="paginatedHistoryData" border style="width: 100%" stripe>
          <el-table-column prop="changeDate" label="变更日期" width="110" />
          <el-table-column prop="changeType" label="变更类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getChangeType(row.changeType)" size="small">
                {{ getChangeTypeText(row.changeType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="oldCreditGrade" label="原信用等级" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.oldCreditGrade" :type="getCreditGradeType(row.oldCreditGrade)" size="small">
                {{ row.oldCreditGrade }}
              </el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="newCreditGrade" label="新信用等级" width="100">
            <template #default="{ row }">
              <el-tag :type="getCreditGradeType(row.newCreditGrade)" size="small">
                {{ row.newCreditGrade }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="oldLimit" label="原额度" width="120" align="right">
            <template #default="{ row }">
              <span v-if="row.oldLimit">{{ formatMoney(row.oldLimit) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="newLimit" label="新额度" width="120" align="right">
            <template #default="{ row }">
              <span class="new-limit">{{ formatMoney(row.newLimit) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="changeAmount" label="变更金额" width="120" align="right">
            <template #default="{ row }">
              <span :class="row.changeAmount > 0 ? 'increase-amount' : 'decrease-amount'">
                {{ row.changeAmount > 0 ? '+' : '' }}{{ formatMoney(row.changeAmount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="operator" label="操作人" width="100" />
          <el-table-column prop="reason" label="变更原因" min-width="150" show-overflow-tooltip />
          <el-table-column prop="status" label="审批状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getApprovalStatusType(row.status)" size="small">
                {{ getApprovalStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="approver" label="审批人" width="100" />
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="historyPagination.currentPage"
            v-model:page-size="historyPagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="creditHistory.length"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @size-change="handleHistorySizeChange"
            @current-change="handleHistoryCurrentChange"
          />
        </div>
      </el-card>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'

const activeTab = ref('limit')
const loading = ref(false)
const limitDialog = ref(false)
const historyDialog = ref(false)
const checkFormRef = ref()
const limitFormRef = ref()
const limitTrendChart = ref(null)
const currentCustomerName = ref('')
let limitTrendChartInstance = null

// 信用概览数据
const creditSummary = ref({
  totalLimit: 5000000,
  usedLimit: 2800000,
  availableLimit: 2200000,
  customerCount: 48,
  usageRate: 56,
  availableRate: 44,
  overlimitCustomers: 3
})

// 客户列表
const customers = ref([
  { id: 1, name: 'ABC公司' },
  { id: 2, name: 'XYZ集团' },
  { id: 3, name: '华通科技' },
  { id: 4, name: '恒大商贸' }
])

// 额度搜索表单
const limitSearchForm = ref({
  customerName: '',
  creditGrade: '',
  status: ''
})

// 信用检查表单
const checkForm = ref({
  customerId: '',
  transactionAmount: 0,
  transactionType: 'sale'
})

// 预警设置表单
const warningForm = ref({
  warningThreshold: 80,
  dangerThreshold: 95,
  overdueDays: 30,
  autoFreeze: true,
  autoFreezeDays: 60
})

// 额度设置表单
const limitForm = ref({
  customerId: '',
  creditGrade: 'A',
  creditLimit: 0,
  effectiveDate: new Date().toISOString().split('T')[0],
  remark: ''
})

// 信用额度数据
const creditLimitData = ref([
  {
    id: 1,
    customerName: 'ABC公司',
    customerCode: 'C001',
    creditGrade: 'AAA',
    creditLimit: 1000000,
    usedAmount: 650000,
    availableAmount: 350000,
    usageRate: 65,
    effectiveDate: '2025-01-01',
    status: 'normal'
  },
  {
    id: 2,
    customerName: 'XYZ集团',
    customerCode: 'C002',
    creditGrade: 'AA',
    creditLimit: 800000,
    usedAmount: 820000,
    availableAmount: -20000,
    usageRate: 102.5,
    effectiveDate: '2025-01-01',
    status: 'overlimit'
  },
  {
    id: 3,
    customerName: '华通科技',
    customerCode: 'C003',
    creditGrade: 'A',
    creditLimit: 500000,
    usedAmount: 0,
    availableAmount: 500000,
    usageRate: 0,
    effectiveDate: '2025-01-01',
    status: 'frozen'
  }
])

// 检查结果
const checkResult = ref(null)

// 预警列表
const warningList = ref([
  {
    customerName: 'XYZ集团',
    warningType: 'overlimit',
    warningLevel: '高',
    currentValue: '102.5%',
    thresholdValue: '95%',
    warningTime: '2025-08-15 10:30',
    description: '信用额度使用率超限'
  },
  {
    customerName: 'ABC公司',
    warningType: 'overdue',
    warningLevel: '中',
    currentValue: '45天',
    thresholdValue: '30天',
    warningTime: '2025-08-15 09:15',
    description: '存在逾期应收账款'
  }
])

// 历史记录分页
const historyPagination = ref({
  currentPage: 1,
  pageSize: 10
})

// 历史统计数据
const historyStats = ref({
  currentLimit: 1000000,
  maxLimit: 1200000,
  adjustCount: 8,
  avgLimit: 950000
})

// 信用历史记录
const creditHistory = ref([
  {
    id: 1,
    changeDate: '2025-08-01',
    changeType: 'increase',
    oldCreditGrade: 'AA',
    newCreditGrade: 'AAA',
    oldLimit: 800000,
    newLimit: 1000000,
    changeAmount: 200000,
    operator: '张经理',
    reason: '客户业务规模扩大，资信状况良好',
    status: 'approved',
    approver: '李总监'
  },
  {
    id: 2,
    changeDate: '2025-06-15',
    changeType: 'decrease',
    oldCreditGrade: 'AAA',
    newCreditGrade: 'AA',
    oldLimit: 1200000,
    newLimit: 800000,
    changeAmount: -400000,
    operator: '王专员',
    reason: '客户出现逾期情况，降低信用等级',
    status: 'approved',
    approver: '李总监'
  },
  {
    id: 3,
    changeDate: '2025-04-10',
    changeType: 'freeze',
    oldCreditGrade: 'AAA',
    newCreditGrade: 'AAA',
    oldLimit: 1200000,
    newLimit: 1200000,
    changeAmount: 0,
    operator: '张经理',
    reason: '客户申请临时冻结额度',
    status: 'approved',
    approver: '李总监'
  },
  {
    id: 4,
    changeDate: '2025-03-20',
    changeType: 'unfreeze',
    oldCreditGrade: 'AAA',
    newCreditGrade: 'AAA',
    oldLimit: 1200000,
    newLimit: 1200000,
    changeAmount: 0,
    operator: '张经理',
    reason: '恢复正常信用额度使用',
    status: 'approved',
    approver: '李总监'
  },
  {
    id: 5,
    changeDate: '2025-01-15',
    changeType: 'increase',
    oldCreditGrade: 'AA',
    newCreditGrade: 'AAA',
    oldLimit: 800000,
    newLimit: 1200000,
    changeAmount: 400000,
    operator: '王专员',
    reason: '年度信用评估，提高信用等级和额度',
    status: 'approved',
    approver: '李总监'
  },
  {
    id: 6,
    changeDate: '2024-12-01',
    changeType: 'adjust',
    oldCreditGrade: 'A',
    newCreditGrade: 'AA',
    oldLimit: 600000,
    newLimit: 800000,
    changeAmount: 200000,
    operator: '张经理',
    reason: '客户合作满一年，信用记录良好',
    status: 'approved',
    approver: '李总监'
  },
  {
    id: 7,
    changeDate: '2024-10-15',
    changeType: 'create',
    oldCreditGrade: null,
    newCreditGrade: 'A',
    oldLimit: 0,
    newLimit: 600000,
    changeAmount: 600000,
    operator: '王专员',
    reason: '新客户初始信用额度设置',
    status: 'approved',
    approver: '李总监'
  },
  {
    id: 8,
    changeDate: '2024-09-01',
    changeType: 'pending',
    oldCreditGrade: 'A',
    newCreditGrade: 'AA',
    oldLimit: 600000,
    newLimit: 700000,
    changeAmount: 100000,
    operator: '张经理',
    reason: '客户申请提高额度',
    status: 'pending',
    approver: ''
  }
])

// 分页计算
const paginatedHistoryData = computed(() => {
  const start = (historyPagination.value.currentPage - 1) * historyPagination.value.pageSize
  const end = start + historyPagination.value.pageSize
  return creditHistory.value.slice(start, end)
})

const limitDialogTitle = computed(() => {
  return limitForm.value.id ? '调整信用额度' : '设置信用额度'
})

const checkRules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  transactionAmount: [{ required: true, message: '请输入交易金额', trigger: 'blur' }],
  transactionType: [{ required: true, message: '请选择交易类型', trigger: 'change' }]
}

const limitRules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  creditGrade: [{ required: true, message: '请选择信用等级', trigger: 'change' }],
  creditLimit: [{ required: true, message: '请输入信用额度', trigger: 'blur' }],
  effectiveDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }]
}

const formatMoney = (amount) => {
  if (!amount) return '¥0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const getCreditGradeType = (grade) => {
  const map = {
    'AAA': 'success',
    'AA': 'primary',
    'A': 'info',
    'BBB': 'warning',
    'BB': 'danger'
  }
  return map[grade] || ''
}

const getCreditStatusType = (status) => {
  const map = {
    'normal': 'success',
    'overlimit': 'danger',
    'frozen': 'info'
  }
  return map[status] || ''
}

const getCreditStatusText = (status) => {
  const map = {
    'normal': '正常',
    'overlimit': '超限',
    'frozen': '冻结'
  }
  return map[status] || status
}

const getUsageStatus = (rate) => {
  if (rate >= 95) return 'exception'
  if (rate >= 80) return 'warning'
  return 'success'
}

const getWarningType = (type) => {
  const map = {
    'overlimit': 'danger',
    'overdue': 'warning',
    'risk': 'info'
  }
  return map[type] || ''
}

const getWarningText = (type) => {
  const map = {
    'overlimit': '额度超限',
    'overdue': '逾期预警',
    'risk': '风险预警'
  }
  return map[type] || type
}

const getWarningLevelType = (level) => {
  const map = {
    '高': 'danger',
    '中': 'warning',
    '低': 'info'
  }
  return map[level] || ''
}

const handleLimitSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('查询完成')
  }, 500)
}

const handleLimitReset = () => {
  limitSearchForm.value = {
    customerName: '',
    creditGrade: '',
    status: ''
  }
}

const handleLimitSelection = (selection) => {
  // 处理选择
}

const handleSetLimit = () => {
  limitForm.value = {
    customerId: '',
    creditGrade: 'A',
    creditLimit: 0,
    effectiveDate: new Date().toISOString().split('T')[0],
    remark: ''
  }
  limitDialog.value = true
}

const handleBatchAdjust = () => {
  ElMessage.info('批量调整功能开发中')
}

const handleExportLimit = () => {
  ElMessage.success('导出功能开发中')
}

const handleAdjustLimit = (row) => {
  limitForm.value = { ...row }
  limitDialog.value = true
}

const handleFreezeLimit = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要冻结 ${row.customerName} 的信用额度吗？`,
      '确认冻结',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    row.status = 'frozen'
    ElMessage.success('信用额度已冻结')
  } catch {
    // 取消冻结
  }
}

const handleUnfreezeLimit = (row) => {
  row.status = 'normal'
  ElMessage.success('信用额度已解冻')
}

const handleViewHistory = (row) => {
  currentCustomerName.value = row.customerName
  // 根据客户设置历史统计数据
  historyStats.value = {
    currentLimit: row.creditLimit,
    maxLimit: Math.max(row.creditLimit * 1.2, 1200000),
    adjustCount: creditHistory.value.length,
    avgLimit: creditHistory.value.reduce((sum, h) => sum + h.newLimit, 0) / creditHistory.value.length
  }
  historyDialog.value = true
  nextTick(() => {
    initLimitTrendChart()
  })
}

const loadCustomerCredit = () => {
  const customer = creditLimitData.value.find(c => c.id === checkForm.value.customerId)
  if (customer) {
    performCreditCheck()
  }
}

const performCreditCheck = () => {
  if (!checkForm.value.customerId || !checkForm.value.transactionAmount) return
  
  const customer = creditLimitData.value.find(c => c.id === checkForm.value.customerId)
  if (!customer) return
  
  const afterTransactionBalance = customer.availableAmount - checkForm.value.transactionAmount
  const passed = afterTransactionBalance >= 0 && customer.status === 'normal'
  
  checkResult.value = {
    customerName: customer.customerName,
    creditGrade: customer.creditGrade,
    creditLimit: customer.creditLimit,
    usedAmount: customer.usedAmount,
    availableAmount: customer.availableAmount,
    transactionAmount: checkForm.value.transactionAmount,
    afterTransactionBalance: afterTransactionBalance,
    passed: passed,
    message: passed ? '信用额度充足，可以继续交易' : '信用额度不足或状态异常',
    warnings: passed ? [] : ['客户信用额度可能不足', '建议联系客户确认付款计划']
  }
}

const handleCreditCheck = async () => {
  const valid = await checkFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  performCreditCheck()
  ElMessage.success('信用检查完成')
}

const handleResetCheck = () => {
  checkFormRef.value?.resetFields()
  checkResult.value = null
}

const handleApprovalRequest = () => {
  ElMessage.info('审批申请功能开发中')
}

const handleSaveWarningSettings = () => {
  ElMessage.success('预警设置已保存')
}

const handleResetWarningSettings = () => {
  warningForm.value = {
    warningThreshold: 80,
    dangerThreshold: 95,
    overdueDays: 30,
    autoFreeze: true,
    autoFreezeDays: 60
  }
}

const handleProcessWarning = (row) => {
  ElMessage.info(`处理预警：${row.customerName}`)
}

const handleIgnoreWarning = (row) => {
  ElMessage.success(`已忽略预警：${row.customerName}`)
}

const handleSubmitLimit = async () => {
  const valid = await limitFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  limitDialog.value = false
  ElMessage.success(limitForm.value.id ? '信用额度调整成功' : '信用额度设置成功')
}

// 历史记录相关函数
const getChangeType = (type) => {
  const map = {
    'create': 'primary',
    'increase': 'success',
    'decrease': 'warning',
    'adjust': 'info',
    'freeze': 'danger',
    'unfreeze': 'success',
    'pending': 'warning'
  }
  return map[type] || ''
}

const getChangeTypeText = (type) => {
  const map = {
    'create': '新建',
    'increase': '增加',
    'decrease': '减少',
    'adjust': '调整',
    'freeze': '冻结',
    'unfreeze': '解冻',
    'pending': '待审批'
  }
  return map[type] || type
}

const getApprovalStatusType = (status) => {
  const map = {
    'approved': 'success',
    'rejected': 'danger',
    'pending': 'warning'
  }
  return map[status] || ''
}

const getApprovalStatusText = (status) => {
  const map = {
    'approved': '已批准',
    'rejected': '已拒绝',
    'pending': '待审批'
  }
  return map[status] || status
}

// 分页处理
const handleHistorySizeChange = (size) => {
  historyPagination.value.pageSize = size
  historyPagination.value.currentPage = 1
}

const handleHistoryCurrentChange = (page) => {
  historyPagination.value.currentPage = page
}

const handleExportHistory = () => {
  ElMessage.success('历史记录导出功能开发中')
}

// 初始化额度趋势图
const initLimitTrendChart = () => {
  if (limitTrendChart.value) {
    limitTrendChartInstance = echarts.init(limitTrendChart.value)
    
    // 基于历史数据生成趋势图数据
    const sortedHistory = [...creditHistory.value].sort((a, b) => new Date(a.changeDate) - new Date(b.changeDate))
    const dates = sortedHistory.map(h => h.changeDate)
    const limits = sortedHistory.map(h => h.newLimit / 10000) // 转换为万元
    
    const option = {
      tooltip: {
        trigger: 'axis',
        formatter: function(params) {
          const point = params[0]
          return `${point.name}<br/>信用额度: ${point.value}万元`
        }
      },
      xAxis: {
        type: 'category',
        data: dates,
        axisLabel: {
          formatter: (value) => {
            return value.slice(5) // 只显示月-日
          }
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (value) => value + '万'
        }
      },
      series: [
        {
          name: '信用额度',
          type: 'line',
          data: limits,
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          itemStyle: {
            color: '#409eff'
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
                { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
              ]
            }
          }
        }
      ]
    }
    
    limitTrendChartInstance.setOption(option)
    
    // 监听窗口大小变化
    window.addEventListener('resize', () => {
      limitTrendChartInstance?.resize()
    })
  }
}
</script>

<style scoped lang="scss">
.credit-overview {
  margin-bottom: 20px;
  
  .stat-card {
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
    
    .usage-rate {
      color: #e6a23c;
      font-size: 12px;
    }
    
    .available-rate {
      color: #67c23a;
      font-size: 12px;
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

.check-result-card {
  margin-top: 20px;
  
  .check-status {
    text-align: center;
  }
  
  .warnings {
    margin-top: 20px;
    
    h4 {
      margin-bottom: 15px;
      color: #303133;
    }
    
    .warning-item {
      margin-bottom: 10px;
    }
  }
}

.warning-list-card {
  margin-top: 20px;
}

.used-amount {
  color: #e6a23c;
  font-weight: 600;
}

.available-amount {
  color: #67c23a;
  font-weight: 600;
}

.overlimit-amount {
  color: #f56c6c;
  font-weight: 600;
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

// 历史记录对话框样式
.history-overview {
  margin-bottom: 20px;
  
  .history-stat-card {
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
  }
}

.trend-card {
  margin-bottom: 20px;
}

.history-table-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-actions {
      display: flex;
      gap: 8px;
    }
  }
}

.new-limit {
  color: #303133;
  font-weight: 600;
}

.increase-amount {
  color: #67c23a;
  font-weight: 600;
}

.decrease-amount {
  color: #f56c6c;
  font-weight: 600;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .credit-overview {
    .el-col {
      margin-bottom: 16px;
    }
  }
  
  .history-overview {
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