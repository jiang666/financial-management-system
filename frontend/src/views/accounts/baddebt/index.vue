<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">坏账管理</h2>
      <p class="description">管理坏账计提、坏账核销和坏账收回</p>
    </div>
    
    <!-- 坏账概览 -->
    <el-row :gutter="20" class="baddebt-overview">
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="坏账准备余额" :value="baddebtSummary.provision" :precision="2" prefix="¥">
            <template #suffix>
              <el-tag type="warning" size="small">计提</el-tag>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="本年计提" :value="baddebtSummary.thisYearProvision" :precision="2" prefix="¥">
            <template #suffix>
              <span class="trend-up">
                <el-icon><ArrowUp /></el-icon>
                5%
              </span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="核销金额" :value="baddebtSummary.writeOff" :precision="2" prefix="¥">
            <template #suffix>
              <el-tag size="small">{{ baddebtSummary.writeOffCount }}笔</el-tag>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="收回金额" :value="baddebtSummary.recovery" :precision="2" prefix="¥">
            <template #suffix>
              <el-tag type="success" size="small">{{ baddebtSummary.recoveryCount }}笔</el-tag>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 功能区 -->
    <el-tabs v-model="activeTab">
      <!-- 坏账计提 -->
      <el-tab-pane label="坏账计提" name="provision">
        <el-card>
          <el-form :model="provisionForm" :rules="provisionRules" ref="provisionFormRef" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="计提日期" prop="provisionDate">
                  <el-date-picker
                    v-model="provisionForm.provisionDate"
                    type="date"
                    placeholder="选择日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="计提方法" prop="method">
                  <el-select v-model="provisionForm.method" placeholder="请选择" style="width: 100%" @change="handleMethodChange">
                    <el-option label="账龄分析法" value="aging" />
                    <el-option label="余额百分比法" value="percentage" />
                    <el-option label="个别认定法" value="individual" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            
            <!-- 账龄分析法参数 -->
            <div v-if="provisionForm.method === 'aging'" class="method-params">
              <div class="params-header">
                <h4>账龄分析法计提比例</h4>
                <el-tag type="info" size="small">根据账龄区间设置不同计提比例</el-tag>
              </div>
              <el-table :data="agingRates" border class="aging-rates-table" stripe style="width: 100%">
                <el-table-column prop="agingRange" label="账龄区间" min-width="120">
                  <template #default="{ row }">
                    <el-tag 
                      :type="getAgingRangeType(row.agingRange)" 
                      size="small" 
                      effect="plain"
                    >
                      {{ row.agingRange }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="amount" label="应收金额" min-width="160" align="right">
                  <template #default="{ row }">
                    <span class="amount-text">{{ formatMoney(row.amount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="计提比例" min-width="180" align="center">
                  <template #default="{ row }">
                    <div class="rate-input-wrapper">
                      <el-input-number
                        v-model="row.rate"
                        :min="0"
                        :max="100"
                        :precision="2"
                        size="small"
                        class="rate-input"
                        @change="calculateProvision"
                      />
                      <span class="rate-suffix">%</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="provisionAmount" label="计提金额" min-width="160" align="right">
                  <template #default="{ row }">
                    <span class="provision-amount" :class="{ 'has-provision': row.provisionAmount > 0 }">
                      {{ formatMoney(row.provisionAmount) }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="操作建议" min-width="120" align="center">
                  <template #default="{ row }">
                    <el-tooltip 
                      :content="getProvisionSuggestion(row)" 
                      placement="top"
                      effect="dark"
                    >
                      <el-tag 
                        :type="getSuggestionType(row)" 
                        size="small"
                        effect="light"
                      >
                        {{ getProvisionLevel(row) }}
                      </el-tag>
                    </el-tooltip>
                  </template>
                </el-table-column>
              </el-table>
              <div class="aging-summary">
                <el-row :gutter="20">
                  <el-col :span="6">
                    <div class="summary-item">
                      <span class="label">应收总额：</span>
                      <span class="value">{{ formatMoney(getTotalReceivable()) }}</span>
                    </div>
                  </el-col>
                  <el-col :span="6">
                    <div class="summary-item">
                      <span class="label">计提总额：</span>
                      <span class="value highlight">{{ formatMoney(getTotalProvision()) }}</span>
                    </div>
                  </el-col>
                  <el-col :span="6">
                    <div class="summary-item">
                      <span class="label">整体比例：</span>
                      <span class="value">{{ getOverallRate() }}%</span>
                    </div>
                  </el-col>
                  <el-col :span="6">
                    <div class="summary-item">
                      <span class="label">风险评级：</span>
                      <span class="value">
                        <el-tag :type="getOverallRiskType()" size="small">
                          {{ getOverallRiskLevel() }}
                        </el-tag>
                      </span>
                    </div>
                  </el-col>
                </el-row>
                <el-row :gutter="20" style="margin-top: 12px">
                  <el-col :span="24">
                    <div class="summary-item">
                      <span class="label">专业建议：</span>
                      <span class="value advice">{{ getOverallAdvice() }}</span>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </div>
            
            <!-- 余额百分比法参数 -->
            <div v-if="provisionForm.method === 'percentage'" class="method-params">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="应收账款余额">
                    <el-input :value="formatMoney(2580000)" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="计提比例">
                    <el-input-number
                      v-model="provisionForm.percentageRate"
                      :min="0"
                      :max="100"
                      :precision="2"
                      style="width: 100%"
                      @change="calculateProvision"
                    />
                    <span style="margin-left: 5px">%</span>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="期初余额">
                  <el-input-number v-model="provisionForm.beginBalance" :precision="2" style="width: 100%" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="应计提金额">
                  <el-input-number v-model="provisionForm.shouldProvision" :precision="2" style="width: 100%" disabled />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="本期计提金额" prop="currentProvision">
                  <el-input-number v-model="provisionForm.currentProvision" :precision="2" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="期末余额">
                  <el-input-number v-model="endBalance" :precision="2" style="width: 100%" disabled />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item label="计提说明" prop="remark">
              <el-input v-model="provisionForm.remark" type="textarea" :rows="2" placeholder="请输入计提说明" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleSubmitProvision">确认计提</el-button>
              <el-button @click="handleResetProvision">重置</el-button>
              <el-button type="success" @click="handleGenerateVoucher">生成凭证</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <!-- 坏账核销 -->
      <el-tab-pane label="坏账核销" name="writeoff">
        <div class="search-form">
          <el-form :model="writeoffSearchForm" inline>
            <el-form-item label="客户">
              <el-select v-model="writeoffSearchForm.customerId" placeholder="请选择客户" clearable style="width: 150px">
                <el-option label="ABC公司" value="1" />
                <el-option label="XYZ集团" value="2" />
              </el-select>
            </el-form-item>
            <el-form-item label="核销状态">
              <el-select v-model="writeoffSearchForm.status" placeholder="请选择" clearable style="width: 120px">
                <el-option label="待核销" value="pending" />
                <el-option label="已核销" value="writeoff" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleWriteoffSearch">搜索</el-button>
              <el-button icon="Refresh" @click="handleWriteoffReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <div class="table-wrapper">
          <div class="table-header">
            <el-button type="danger" icon="Delete" @click="handleBatchWriteoff">批量核销</el-button>
            <el-button icon="Download" @click="handleExportWriteoff">导出</el-button>
          </div>
          
          <el-table :data="writeoffData" v-loading="loading" @selection-change="handleWriteoffSelection">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="customerName" label="客户名称" width="150" />
            <el-table-column prop="invoiceNo" label="发票号" width="120" />
            <el-table-column prop="invoiceDate" label="发票日期" width="100" />
            <el-table-column prop="amount" label="应收金额" width="120" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.amount) }}
              </template>
            </el-table-column>
            <el-table-column prop="overdueDays" label="逾期天数" width="100">
              <template #default="{ row }">
                <span class="overdue-days">{{ row.overdueDays }}天</span>
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="核销原因" min-width="200" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'pending' ? 'warning' : 'danger'" size="small">
                  {{ row.status === 'pending' ? '待核销' : '已核销' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button link type="danger" @click="handleWriteoffItem(row)" v-if="row.status === 'pending'">核销</el-button>
                <el-button link type="info" @click="handleViewWriteoff(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      
      <!-- 坏账收回 -->
      <el-tab-pane label="坏账收回" name="recovery">
        <div class="search-form">
          <el-form :model="recoverySearchForm" inline>
            <el-form-item label="客户">
              <el-select v-model="recoverySearchForm.customerId" placeholder="请选择客户" clearable style="width: 150px">
                <el-option label="ABC公司" value="1" />
                <el-option label="XYZ集团" value="2" />
              </el-select>
            </el-form-item>
            <el-form-item label="收回日期">
              <el-date-picker
                v-model="recoverySearchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleRecoverySearch">搜索</el-button>
              <el-button icon="Refresh" @click="handleRecoveryReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <div class="table-wrapper">
          <div class="table-header">
            <el-button type="success" icon="Plus" @click="handleAddRecovery">登记收回</el-button>
          </div>
          
          <el-table :data="recoveryData" v-loading="loading">
            <el-table-column prop="customerName" label="客户名称" width="150" />
            <el-table-column prop="originalInvoiceNo" label="原发票号" width="120" />
            <el-table-column prop="writeoffDate" label="核销日期" width="100" />
            <el-table-column prop="writeoffAmount" label="核销金额" width="120" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.writeoffAmount) }}
              </template>
            </el-table-column>
            <el-table-column prop="recoveryDate" label="收回日期" width="100" />
            <el-table-column prop="recoveryAmount" label="收回金额" width="120" align="right">
              <template #default="{ row }">
                <span class="recovery-amount">{{ formatMoney(row.recoveryAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="recoveryMethod" label="收回方式" width="100" />
            <el-table-column prop="remark" label="备注" min-width="200" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button link type="info" @click="handleViewRecovery(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      
      <!-- 坏账统计 -->
      <el-tab-pane label="坏账统计" name="statistics">
        <el-row :gutter="20" class="statistics-row">
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>坏账率分析</span>
              </template>
              <div ref="baddebtRateChart" style="height: 300px"></div>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>坏账构成分析</span>
              </template>
              <div ref="baddebtCompositionChart" style="height: 300px"></div>
            </el-card>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" class="statistics-row">
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>账龄分布图</span>
              </template>
              <div ref="agingDistributionChart" style="height: 300px"></div>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>客户坏账排行</span>
              </template>
              <el-table :data="customerBaddebtRanking.slice(0, 8)" border max-height="300">
                <el-table-column type="index" label="排名" width="60" />
                <el-table-column prop="customerName" label="客户名称" />
                <el-table-column prop="baddebtAmount" label="坏账金额" width="120" align="right">
                  <template #default="{ row }">
                    {{ formatMoney(row.baddebtAmount) }}
                  </template>
                </el-table-column>
                <el-table-column prop="baddebtRate" label="坏账率" width="80" align="right">
                  <template #default="{ row }">
                    {{ row.baddebtRate }}%
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 坏账收回对话框 -->
    <el-dialog v-model="recoveryDialog" title="坏账收回登记" width="600px">
      <el-form :model="recoveryForm" :rules="recoveryRules" ref="recoveryFormRef" label-width="100px">
        <el-form-item label="已核销项目" prop="writeoffId">
          <el-select v-model="recoveryForm.writeoffId" placeholder="请选择" style="width: 100%">
            <el-option
              v-for="item in writeoffItems"
              :key="item.id"
              :label="`${item.customerName} - ${item.invoiceNo} - ${formatMoney(item.amount)}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="收回日期" prop="recoveryDate">
          <el-date-picker
            v-model="recoveryForm.recoveryDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="收回金额" prop="recoveryAmount">
          <el-input-number
            v-model="recoveryForm.recoveryAmount"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="收回方式" prop="recoveryMethod">
          <el-select v-model="recoveryForm.recoveryMethod" placeholder="请选择" style="width: 100%">
            <el-option label="现金" value="cash" />
            <el-option label="银行转账" value="transfer" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="recoveryForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recoveryDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitRecovery">确认</el-button>
      </template>
    </el-dialog>
    
    <!-- 坏账回收详情对话框 -->
    <el-dialog v-model="recoveryDetailDialog" title="坏账回收详情" width="800px">
      <div v-if="currentRecoveryDetail" class="recovery-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="原始发票号">{{ currentRecoveryDetail.originalInvoiceNo }}</el-descriptions-item>
          <el-descriptions-item label="客户名称">{{ currentRecoveryDetail.customerName }}</el-descriptions-item>
          <el-descriptions-item label="原始金额">{{ formatMoney(currentRecoveryDetail.originalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="收回金额">{{ formatMoney(currentRecoveryDetail.recoveryAmount) }}</el-descriptions-item>
          <el-descriptions-item label="收回日期">{{ currentRecoveryDetail.recoveryDate }}</el-descriptions-item>
          <el-descriptions-item label="收回方式">{{ getRecoveryMethodText(currentRecoveryDetail.recoveryMethod) }}</el-descriptions-item>
          <el-descriptions-item label="银行账户" v-if="currentRecoveryDetail.bankAccount">{{ currentRecoveryDetail.bankAccount }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ currentRecoveryDetail.operator }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="recovery-timeline">
          <h4>回收进程</h4>
          <el-timeline>
            <el-timeline-item
              v-for="item in currentRecoveryDetail.timeline"
              :key="item.id"
              :timestamp="item.date"
              :type="item.type"
            >
              <el-card>
                <h4>{{ item.title }}</h4>
                <p>{{ item.description }}</p>
                <p class="operator-info">操作人：{{ item.operator }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
        
        <div class="recovery-documents" v-if="currentRecoveryDetail.documents && currentRecoveryDetail.documents.length > 0">
          <h4>相关文档</h4>
          <el-table :data="currentRecoveryDetail.documents" border>
            <el-table-column prop="fileName" label="文件名称" />
            <el-table-column prop="fileType" label="文件类型" width="100" />
            <el-table-column prop="uploadDate" label="上传日期" width="120" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleDownloadDocument(row)">下载</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="recoveryDetailDialog = false">关闭</el-button>
        <el-button type="primary" @click="handlePrintRecoveryDetail">打印详情</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'

const activeTab = ref('provision')
const loading = ref(false)
const recoveryDialog = ref(false)
const recoveryDetailDialog = ref(false)
const provisionFormRef = ref()
const recoveryFormRef = ref()
const baddebtRateChart = ref(null)
const baddebtCompositionChart = ref(null)
const agingDistributionChart = ref(null)
let baddebtRateChartInstance = null
let baddebtCompositionChartInstance = null
let agingDistributionChartInstance = null
const currentRecoveryDetail = ref(null)

// 坏账概览数据
const baddebtSummary = ref({
  provision: 129000,
  thisYearProvision: 45000,
  writeOff: 68000,
  writeOffCount: 8,
  recovery: 25000,
  recoveryCount: 3
})

// 计提表单
const provisionForm = ref({
  provisionDate: new Date().toISOString().split('T')[0],
  method: 'aging',
  percentageRate: 5.0,
  beginBalance: 84000,
  shouldProvision: 129000,
  currentProvision: 45000,
  remark: ''
})

// 账龄分析法比例
const agingRates = ref([
  { agingRange: '未逾期', amount: 1850000, rate: 0.5, provisionAmount: 9250 },
  { agingRange: '1-30天', amount: 310000, rate: 5, provisionAmount: 15500 },
  { agingRange: '31-60天', amount: 260000, rate: 10, provisionAmount: 26000 },
  { agingRange: '61-90天', amount: 105000, rate: 30, provisionAmount: 31500 },
  { agingRange: '90天以上', amount: 55000, rate: 80, provisionAmount: 44000 }
])

// 核销搜索表单
const writeoffSearchForm = ref({
  customerId: '',
  status: ''
})

// 收回搜索表单
const recoverySearchForm = ref({
  customerId: '',
  dateRange: []
})

// 收回表单
const recoveryForm = ref({
  writeoffId: '',
  recoveryDate: new Date().toISOString().split('T')[0],
  recoveryAmount: 0,
  recoveryMethod: 'transfer',
  remark: ''
})

// 核销数据
const writeoffData = ref([
  {
    id: 1,
    customerName: 'ABC公司',
    invoiceNo: 'INV20250003',
    invoiceDate: '2025-07-01',
    amount: 123000,
    overdueDays: 180,
    reason: '客户破产无法收回',
    status: 'pending'
  },
  {
    id: 2,
    customerName: 'XYZ集团',
    invoiceNo: 'INV20250015',
    invoiceDate: '2025-06-15',
    amount: 89000,
    overdueDays: 210,
    reason: '客户失联无法联系',
    status: 'writeoff'
  }
])

// 收回数据
const recoveryData = ref([
  {
    id: 1,
    customerName: 'ABC公司',
    originalInvoiceNo: 'INV20240005',
    writeoffDate: '2024-12-01',
    writeoffAmount: 56000,
    recoveryDate: '2025-08-01',
    recoveryAmount: 25000,
    recoveryMethod: 'bank_transfer',
    remark: '部分收回',
    originalAmount: 56000,
    bankAccount: '6222000012345678',
    operator: '张会计',
    timeline: [
      {
        id: 1,
        date: '2024-12-01',
        title: '坏账核销',
        description: '经审批后对ABC公司应收账款进行坏账核销',
        operator: '李经理',
        type: 'warning'
      },
      {
        id: 2,
        date: '2025-07-20',
        title: '回收协商',
        description: '客户主动联系，协商还款事宜',
        operator: '王业务员',
        type: 'info'
      },
      {
        id: 3,
        date: '2025-08-01',
        title: '部分回收',
        description: '客户转账25000元，部分偿还应收账款',
        operator: '张会计',
        type: 'success'
      }
    ],
    documents: [
      {
        fileName: '回收协议.pdf',
        fileType: 'PDF',
        uploadDate: '2025-07-20'
      },
      {
        fileName: '银行转账凭证.jpg',
        fileType: 'JPG',
        uploadDate: '2025-08-01'
      }
    ]
  },
  {
    id: 2,
    customerName: '东方贸易',
    originalInvoiceNo: 'INV20240012',
    writeoffDate: '2024-11-15',
    writeoffAmount: 34000,
    recoveryDate: '2025-07-25',
    recoveryAmount: 34000,
    recoveryMethod: 'cash',
    remark: '全额收回',
    originalAmount: 34000,
    bankAccount: '',
    operator: '李会计',
    timeline: [
      {
        id: 1,
        date: '2024-11-15',
        title: '坏账核销',
        description: '应收账款超期120天，申请坏账核销',
        operator: '李经理',
        type: 'warning'
      },
      {
        id: 2,
        date: '2025-07-25',
        title: '现金回收',
        description: '客户现金支付全部欠款',
        operator: '李会计',
        type: 'success'
      }
    ],
    documents: [
      {
        fileName: '现金收据.pdf',
        fileType: 'PDF',
        uploadDate: '2025-07-25'
      }
    ]
  }
])

// 已核销项目
const writeoffItems = ref([
  { id: 1, customerName: 'ABC公司', invoiceNo: 'INV20240005', amount: 56000 },
  { id: 2, customerName: 'XYZ集团', invoiceNo: 'INV20240010', amount: 34000 }
])

// 客户坏账排行
const customerBaddebtRanking = ref([
  { customerName: 'ABC公司', baddebtAmount: 123000, baddebtRate: 4.8 },
  { customerName: 'XYZ集团', baddebtAmount: 89000, baddebtRate: 3.2 },
  { customerName: '华通科技', baddebtAmount: 67000, baddebtRate: 2.9 },
  { customerName: '恒大商贸', baddebtAmount: 45000, baddebtRate: 2.1 },
  { customerName: '中建材料', baddebtAmount: 38000, baddebtRate: 1.8 },
  { customerName: '东方贸易', baddebtAmount: 34000, baddebtRate: 1.6 },
  { customerName: '南方电子', baddebtAmount: 28000, baddebtRate: 1.4 },
  { customerName: '北京科技', baddebtAmount: 25000, baddebtRate: 1.2 },
  { customerName: '海通物流', baddebtAmount: 18000, baddebtRate: 0.9 },
  { customerName: '联创包装', baddebtAmount: 15000, baddebtRate: 0.7 }
])

const endBalance = computed(() => {
  return provisionForm.value.beginBalance + provisionForm.value.currentProvision
})

const provisionRules = {
  provisionDate: [{ required: true, message: '请选择计提日期', trigger: 'change' }],
  method: [{ required: true, message: '请选择计提方法', trigger: 'change' }],
  currentProvision: [{ required: true, message: '请输入计提金额', trigger: 'blur' }],
  remark: [{ required: true, message: '请输入计提说明', trigger: 'blur' }]
}

const recoveryRules = {
  writeoffId: [{ required: true, message: '请选择已核销项目', trigger: 'change' }],
  recoveryDate: [{ required: true, message: '请选择收回日期', trigger: 'change' }],
  recoveryAmount: [{ required: true, message: '请输入收回金额', trigger: 'blur' }],
  recoveryMethod: [{ required: true, message: '请选择收回方式', trigger: 'change' }]
}

const formatMoney = (amount) => {
  if (!amount) return '¥0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const initCharts = () => {
  // 坏账率分析图表
  if (baddebtRateChart.value) {
    baddebtRateChartInstance = echarts.init(baddebtRateChart.value)
    const option = {
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['坏账率', '行业平均']
      },
      xAxis: {
        type: 'category',
        data: ['2020', '2021', '2022', '2023', '2024', '2025']
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: '{value}%'
        }
      },
      series: [
        {
          name: '坏账率',
          type: 'line',
          data: [2.1, 2.8, 3.2, 2.9, 3.5, 3.8],
          itemStyle: { color: '#f56c6c' }
        },
        {
          name: '行业平均',
          type: 'line',
          data: [3.0, 3.2, 3.8, 3.5, 4.1, 4.3],
          itemStyle: { color: '#909399' }
        }
      ]
    }
    baddebtRateChartInstance.setOption(option)
  }
  
  // 坏账构成分析图表
  if (baddebtCompositionChart.value) {
    baddebtCompositionChartInstance = echarts.init(baddebtCompositionChart.value)
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c}万元 ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '坏账构成',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          data: [
            { value: 15.6, name: '制造业', itemStyle: { color: '#409eff' } },
            { value: 12.3, name: '贸易业', itemStyle: { color: '#67c23a' } },
            { value: 8.9, name: '服务业', itemStyle: { color: '#e6a23c' } },
            { value: 6.7, name: '建筑业', itemStyle: { color: '#f56c6c' } },
            { value: 4.5, name: '其他', itemStyle: { color: '#909399' } }
          ]
        }
      ]
    }
    baddebtCompositionChartInstance.setOption(option)
  }
  
  // 账龄分布图表
  if (agingDistributionChart.value) {
    agingDistributionChartInstance = echarts.init(agingDistributionChart.value)
    const option = {
      tooltip: {
        trigger: 'axis',
        formatter: '{b}<br/>{a}: {c}万元'
      },
      xAxis: {
        type: 'category',
        data: ['未逾期', '1-30天', '31-60天', '61-90天', '91-120天', '120天以上']
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: '{value}万'
        }
      },
      series: [
        {
          name: '坏账金额',
          type: 'bar',
          data: [8.5, 12.3, 15.6, 18.9, 24.5, 32.1],
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#f56c6c' },
              { offset: 1, color: '#ff9999' }
            ])
          }
        }
      ]
    }
    agingDistributionChartInstance.setOption(option)
  }
}

const handleMethodChange = () => {
  calculateProvision()
}

const getAgingRangeType = (range) => {
  const typeMap = {
    '未逾期': 'success',
    '1-30天': '',
    '31-60天': 'warning',
    '61-90天': 'warning',
    '91-120天': 'danger',
    '120天以上': 'danger'
  }
  return typeMap[range] || ''
}

const getTotalReceivable = () => {
  return agingRates.value.reduce((sum, item) => sum + item.amount, 0)
}

const getTotalProvision = () => {
  return agingRates.value.reduce((sum, item) => sum + item.provisionAmount, 0)
}

const getOverallRate = () => {
  const total = getTotalReceivable()
  const provision = getTotalProvision()
  return total > 0 ? ((provision / total) * 100).toFixed(2) : '0.00'
}

const getProvisionLevel = (row) => {
  if (row.rate === 0) return '无风险'
  if (row.rate <= 5) return '低风险'
  if (row.rate <= 20) return '中风险'
  if (row.rate <= 50) return '高风险'
  return '极高风险'
}

const getSuggestionType = (row) => {
  if (row.rate === 0) return 'success'
  if (row.rate <= 5) return 'info'
  if (row.rate <= 20) return 'warning'
  if (row.rate <= 50) return 'danger'
  return 'danger'
}

const getProvisionSuggestion = (row) => {
  const agingRangeAdvice = {
    '未逾期': '建议保持0-1%的预防性计提',
    '1-30天': '建议计提1-3%，加强催收',
    '31-60天': '建议计提5-10%，重点关注回款',
    '61-90天': '建议计提10-30%，启动法务程序',
    '91-120天': '建议计提30-50%，考虑债权转让',
    '120天以上': '建议计提50-100%，积极追讨或核销'
  }
  
  const baseAdvice = agingRangeAdvice[row.agingRange] || '请根据实际情况调整计提比例'
  const currentAdvice = `当前计提${row.rate}%，金额${formatMoney(row.provisionAmount)}`
  
  return `${baseAdvice}\n\n${currentAdvice}`
}

const getOverallRiskLevel = () => {
  const rate = parseFloat(getOverallRate())
  if (rate <= 1) return '极低风险'
  if (rate <= 3) return '低风险'
  if (rate <= 8) return '中等风险'
  if (rate <= 15) return '较高风险'
  return '高风险'
}

const getOverallRiskType = () => {
  const rate = parseFloat(getOverallRate())
  if (rate <= 1) return 'success'
  if (rate <= 3) return 'info'
  if (rate <= 8) return 'warning'
  if (rate <= 15) return 'danger'
  return 'danger'
}

const getOverallAdvice = () => {
  const rate = parseFloat(getOverallRate())
  const total = getTotalReceivable()
  
  if (rate <= 1) {
    return '应收账款质量优秀，建议保持现有催收政策'
  } else if (rate <= 3) {
    return '应收账款风险可控，建议加强30天以上账龄的催收力度'
  } else if (rate <= 8) {
    return '应收账款存在一定风险，建议完善信用管理制度，重点关注60天以上逾期客户'
  } else if (rate <= 15) {
    return '应收账款风险较高，建议启动专项清收行动，考虑调整信用政策'
  } else {
    return '应收账款风险极高，建议立即启动风险控制措施，必要时寻求专业机构协助'
  }
}

const calculateProvision = () => {
  if (provisionForm.value.method === 'aging') {
    const total = agingRates.value.reduce((sum, item) => {
      item.provisionAmount = item.amount * item.rate / 100
      return sum + item.provisionAmount
    }, 0)
    provisionForm.value.shouldProvision = total
    provisionForm.value.currentProvision = total - provisionForm.value.beginBalance
  } else if (provisionForm.value.method === 'percentage') {
    const total = 2580000 * provisionForm.value.percentageRate / 100
    provisionForm.value.shouldProvision = total
    provisionForm.value.currentProvision = total - provisionForm.value.beginBalance
  }
}

const handleSubmitProvision = async () => {
  const valid = await provisionFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  ElMessage.success('坏账计提成功')
  handleResetProvision()
}

const handleResetProvision = () => {
  provisionFormRef.value?.resetFields()
}

const handleGenerateVoucher = () => {
  ElMessage.success('凭证生成功能开发中')
}

const handleWriteoffSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('查询完成')
  }, 500)
}

const handleWriteoffReset = () => {
  writeoffSearchForm.value = {
    customerId: '',
    status: ''
  }
}

const handleWriteoffSelection = (selection) => {
  // 处理核销选择
}

const handleBatchWriteoff = () => {
  ElMessage.info('批量核销功能开发中')
}

const handleExportWriteoff = () => {
  ElMessage.success('导出功能开发中')
}

const handleWriteoffItem = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要核销 ${row.customerName} 的应收账款 ${formatMoney(row.amount)} 吗？`,
      '确认核销',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    row.status = 'writeoff'
    ElMessage.success('核销成功')
  } catch {
    // 取消核销
  }
}

const handleViewWriteoff = (row) => {
  ElMessage.info(`查看核销详情：${row.invoiceNo}`)
}

const handleRecoverySearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('查询完成')
  }, 500)
}

const handleRecoveryReset = () => {
  recoverySearchForm.value = {
    customerId: '',
    dateRange: []
  }
}

const handleAddRecovery = () => {
  recoveryDialog.value = true
}

const handleViewRecovery = (row) => {
  currentRecoveryDetail.value = row
  recoveryDetailDialog.value = true
}

const handleSubmitRecovery = async () => {
  const valid = await recoveryFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  recoveryDialog.value = false
  ElMessage.success('坏账收回登记成功')
}

const getRecoveryMethodText = (method) => {
  const methodMap = {
    'bank_transfer': '银行转账',
    'cash': '现金支付',
    'check': '支票',
    'other': '其他方式'
  }
  return methodMap[method] || method
}

const handleDownloadDocument = (document) => {
  ElMessage.success(`下载文档：${document.fileName}`)
}

const handlePrintRecoveryDetail = () => {
  ElMessage.success('打印回收详情')
}

onMounted(() => {
  nextTick(() => {
    initCharts()
  })
  
  window.addEventListener('resize', () => {
    baddebtRateChartInstance?.resize()
    baddebtCompositionChartInstance?.resize()
    agingDistributionChartInstance?.resize()
  })
})
</script>

<style scoped lang="scss">
.baddebt-overview {
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

.method-params {
  margin: 20px 0;
  padding: 20px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  
  .params-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h4 {
      margin: 0;
      color: #1f2937;
      font-size: 16px;
      font-weight: 600;
    }
  }
  
  .aging-rates-table {
    border-radius: 6px;
    overflow: hidden;
    box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
    
    .amount-text {
      font-weight: 600;
      color: #374151;
    }
    
    .rate-input-wrapper {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      
      .rate-input {
        width: 80px;
        
        :deep(.el-input-number__decrease),
        :deep(.el-input-number__increase) {
          background: #f3f4f6;
          border-color: #d1d5db;
          
          &:hover {
            background: #e5e7eb;
          }
        }
        
        :deep(.el-input__inner) {
          text-align: center;
          font-weight: 500;
        }
      }
      
      .rate-suffix {
        font-size: 12px;
        color: #6b7280;
        font-weight: 500;
      }
    }
    
    .provision-amount {
      font-weight: 600;
      color: #6b7280;
      
      &.has-provision {
        color: #dc2626;
      }
    }
  }
  
  .aging-summary {
    margin-top: 20px;
    padding: 16px;
    background: #ffffff;
    border-radius: 6px;
    border: 1px solid #e5e7eb;
    
    .summary-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      
      .label {
        font-size: 14px;
        color: #6b7280;
        font-weight: 500;
        min-width: 70px;
      }
      
      .value {
        font-size: 16px;
        font-weight: 600;
        color: #374151;
        text-align: right;
        flex: 1;
        
        &.highlight {
          color: #dc2626;
          font-size: 18px;
        }
        
        &.advice {
          font-size: 14px;
          color: #059669;
          font-weight: 500;
          text-align: left;
          margin-left: 10px;
          line-height: 1.5;
        }
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

.overdue-days {
  color: #f56c6c;
  font-weight: 600;
}

.recovery-amount {
  color: #67c23a;
  font-weight: 600;
}

@media (max-width: 768px) {
  .baddebt-overview {
    .el-col {
      margin-bottom: 16px;
    }
  }
}

.statistics-row {
  margin-bottom: 20px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.recovery-detail {
  .recovery-timeline {
    margin-top: 20px;
    
    h4 {
      margin-bottom: 16px;
      color: #303133;
      font-size: 16px;
      font-weight: 600;
    }
    
    .operator-info {
      margin-top: 8px;
      color: #909399;
      font-size: 12px;
    }
  }
  
  .recovery-documents {
    margin-top: 20px;
    
    h4 {
      margin-bottom: 16px;
      color: #303133;
      font-size: 16px;
      font-weight: 600;
    }
  }
}

@media (max-width: 768px) {
  .statistics-row {
    .el-col {
      margin-bottom: 20px;
    }
  }
}
</style>