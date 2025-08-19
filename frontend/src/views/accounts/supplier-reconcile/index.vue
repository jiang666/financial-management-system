<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">供应商对账</h2>
      <p class="description">接收和核对供应商对账单，处理差异调整</p>
    </div>
    
    <el-tabs v-model="activeTab">
      <!-- 对账单接收 -->
      <el-tab-pane label="对账单接收" name="receive">
        <div class="table-wrapper">
          <div class="table-header">
            <el-button type="primary" icon="Plus" @click="handleReceiveStatement">接收对账单</el-button>
            <el-button icon="Download" @click="handleBatchDownload">批量下载</el-button>
          </div>
          
          <el-table :data="paginatedReceivedStatements" v-loading="loading" @selection-change="handleStatementSelection" style="width: 100%" stripe>
            <el-table-column type="selection" width="55" />
            <el-table-column prop="statementNo" label="对账单号" width="140" show-overflow-tooltip />
            <el-table-column prop="supplierName" label="供应商名称" min-width="130" show-overflow-tooltip />
            <el-table-column prop="period" label="对账期间" width="110" />
            <el-table-column prop="receiveDate" label="接收日期" width="110" />
            <el-table-column prop="supplierBalance" label="供应商余额" width="140" align="right">
              <template #default="{ row }">
                <span class="balance-amount">{{ formatMoney(row.supplierBalance) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="ourBalance" label="我方余额" width="120" align="right">
              <template #default="{ row }">
                <span class="balance-amount">{{ formatMoney(row.ourBalance) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="difference" label="差异金额" width="130" align="right">
              <template #default="{ row }">
                <span v-if="row.difference !== 0" class="difference-amount">
                  {{ formatMoney(row.difference) }}
                </span>
                <span v-else class="no-difference">¥0.00</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="核对状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getReconcileStatusType(row.status)" size="small">
                  {{ getReconcileStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button link type="primary" size="small" @click="handleReconcile(row)">核对</el-button>
                  <el-button link type="success" size="small" @click="handleConfirm(row)" v-if="row.status === 'reconciled'">确认</el-button>
                  <el-button link type="info" size="small" @click="handleViewDetail(row)">详情</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页 -->
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="receivedPagination.currentPage"
              v-model:page-size="receivedPagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="receivedStatements.length"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @size-change="handleReceivedSizeChange"
              @current-change="handleReceivedCurrentChange"
            />
          </div>
        </div>
      </el-tab-pane>
      
      <!-- 核对明细 -->
      <el-tab-pane label="核对明细" name="reconcile">
        <el-card v-if="currentStatement">
          <template #header>
            <div class="card-header">
              <span>{{ currentStatement.supplierName }} - {{ currentStatement.period }} 对账明细</span>
              <el-tag :type="getReconcileStatusType(currentStatement.status)">
                {{ getReconcileStatusText(currentStatement.status) }}
              </el-tag>
            </div>
          </template>
          
          <el-row :gutter="20" class="reconcile-summary">
            <el-col :span="8">
              <el-statistic title="供应商余额" :value="currentStatement.supplierBalance" :precision="2" prefix="¥" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="我方余额" :value="currentStatement.ourBalance" :precision="2" prefix="¥" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="差异金额" :value="Math.abs(currentStatement.difference)" :precision="2" prefix="¥">
                <template #suffix>
                  <el-tag v-if="currentStatement.difference !== 0" type="warning" size="small">有差异</el-tag>
                  <el-tag v-else type="success" size="small">无差异</el-tag>
                </template>
              </el-statistic>
            </el-col>
          </el-row>
          
          <el-tabs v-model="reconcileTab">
            <el-tab-pane label="供应商明细" name="supplier">
              <el-table :data="supplierDetail" border>
                <el-table-column prop="date" label="日期" width="100" />
                <el-table-column prop="documentNo" label="单据号" width="140" />
                <el-table-column prop="summary" label="摘要" min-width="200" />
                <el-table-column prop="debit" label="借方" width="120" align="right">
                  <template #default="{ row }">
                    {{ row.debit ? formatMoney(row.debit) : '' }}
                  </template>
                </el-table-column>
                <el-table-column prop="credit" label="贷方" width="120" align="right">
                  <template #default="{ row }">
                    {{ row.credit ? formatMoney(row.credit) : '' }}
                  </template>
                </el-table-column>
                <el-table-column prop="balance" label="余额" width="120" align="right">
                  <template #default="{ row }">
                    {{ formatMoney(row.balance) }}
                  </template>
                </el-table-column>
                <el-table-column prop="matched" label="匹配状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.matched ? 'success' : 'warning'" size="small">
                      {{ row.matched ? '已匹配' : '未匹配' }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            
            <el-tab-pane label="我方明细" name="our">
              <el-table :data="ourDetail" border>
                <el-table-column prop="date" label="日期" width="100" />
                <el-table-column prop="documentNo" label="单据号" width="140" />
                <el-table-column prop="summary" label="摘要" min-width="200" />
                <el-table-column prop="debit" label="借方" width="120" align="right">
                  <template #default="{ row }">
                    {{ row.debit ? formatMoney(row.debit) : '' }}
                  </template>
                </el-table-column>
                <el-table-column prop="credit" label="贷方" width="120" align="right">
                  <template #default="{ row }">
                    {{ row.credit ? formatMoney(row.credit) : '' }}
                  </template>
                </el-table-column>
                <el-table-column prop="balance" label="余额" width="120" align="right">
                  <template #default="{ row }">
                    {{ formatMoney(row.balance) }}
                  </template>
                </el-table-column>
                <el-table-column prop="matched" label="匹配状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.matched ? 'success' : 'warning'" size="small">
                      {{ row.matched ? '已匹配' : '未匹配' }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            
            <el-tab-pane label="差异分析" name="difference">
              <el-table :data="differenceItems" border>
                <el-table-column prop="type" label="差异类型" width="120">
                  <template #default="{ row }">
                    <el-tag :type="getDifferenceType(row.type)" size="small">
                      {{ row.typeName }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="documentNo" label="单据号" width="140" />
                <el-table-column prop="date" label="日期" width="100" />
                <el-table-column prop="amount" label="差异金额" width="120" align="right">
                  <template #default="{ row }">
                    <span class="difference-amount">{{ formatMoney(row.amount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="reason" label="差异原因" min-width="200" />
                <el-table-column prop="suggestion" label="处理建议" min-width="200" />
                <el-table-column label="操作" width="120">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="handleAdjust(row)">调整</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
          
          <div class="reconcile-actions">
            <el-button type="primary" @click="handleAutoReconcile">自动核对</el-button>
            <el-button type="success" @click="handleSaveReconcile">保存核对结果</el-button>
            <el-button type="warning" @click="handleReportDifference">报告差异</el-button>
          </div>
        </el-card>
        
        <el-empty v-else description="请选择对账单进行核对" />
      </el-tab-pane>
      
      <!-- 确认回复 -->
      <el-tab-pane label="确认回复" name="confirm">
        <el-table :data="paginatedConfirmList" v-loading="loading" style="width: 100%" stripe>
          <el-table-column prop="statementNo" label="对账单号" width="140" show-overflow-tooltip />
          <el-table-column prop="supplierName" label="供应商名称" min-width="130" show-overflow-tooltip />
          <el-table-column prop="period" label="对账期间" width="110" />
          <el-table-column prop="reconcileDate" label="核对日期" width="110">
            <template #default="{ row }">
              <span v-if="row.reconcileDate">{{ row.reconcileDate }}</span>
              <el-tag v-else type="info" size="small">待核对</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="difference" label="差异金额" width="130" align="right">
            <template #default="{ row }">
              <span v-if="row.difference !== 0" class="difference-amount">
                {{ formatMoney(row.difference) }}
              </span>
              <span v-else class="no-difference">¥0.00</span>
            </template>
          </el-table-column>
          <el-table-column prop="confirmStatus" label="确认状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getConfirmStatusType(row.confirmStatus)" size="small">
                {{ getConfirmStatusText(row.confirmStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="replyContent" label="回复内容" min-width="150" show-overflow-tooltip />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button link type="primary" size="small" @click="handleReply(row)">回复</el-button>
                <el-button link type="success" size="small" @click="handleConfirmFinal(row)" v-if="row.confirmStatus === 'pending'">确认</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="confirmPagination.currentPage"
            v-model:page-size="confirmPagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="confirmList.length"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @size-change="handleConfirmSizeChange"
            @current-change="handleConfirmCurrentChange"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 接收对账单对话框 -->
    <el-dialog v-model="receiveDialog" title="接收对账单" width="600px">
      <el-form :model="receiveForm" label-width="100px">
        <el-form-item label="供应商">
          <el-select v-model="receiveForm.supplierId" placeholder="请选择供应商" style="width: 100%">
            <el-option label="华强供应商" value="1" />
            <el-option label="金泰材料" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="对账期间">
          <el-date-picker
            v-model="receiveForm.period"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="对账单文件">
          <el-upload
            class="upload-demo"
            drag
            action="#"
            :before-upload="handleBeforeUpload"
            :auto-upload="false"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 Excel、PDF 格式
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="receiveDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReceive">确认接收</el-button>
      </template>
    </el-dialog>
    
    <!-- 回复对话框 -->
    <el-dialog v-model="replyDialog" title="确认回复" width="600px">
      <el-form :model="replyForm" label-width="100px">
        <el-form-item label="对账结果">
          <el-radio-group v-model="replyForm.result">
            <el-radio label="agree">同意</el-radio>
            <el-radio label="disagree">不同意</el-radio>
            <el-radio label="partial">部分同意</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="调整金额" v-if="replyForm.result === 'partial'">
          <el-input-number v-model="replyForm.adjustAmount" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input v-model="replyForm.content" type="textarea" :rows="4" placeholder="请输入回复内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReply">发送回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('reconcile')
const reconcileTab = ref('supplier')
const loading = ref(false)
const receiveDialog = ref(false)
const replyDialog = ref(false)
const currentStatement = ref({
  id: 1,
  statementNo: 'SP20250001',
  supplierName: '华强供应商',
  period: '2025-08',
  receiveDate: '2025-08-31',
  supplierBalance: 156000,
  ourBalance: 144000,
  difference: 12000,
  status: 'received'
})

// 分页设置
const receivedPagination = ref({
  currentPage: 1,
  pageSize: 10
})

const confirmPagination = ref({
  currentPage: 1,
  pageSize: 10
})

// 接收表单
const receiveForm = ref({
  supplierId: '',
  period: '',
  file: null
})

// 回复表单
const replyForm = ref({
  result: 'agree',
  adjustAmount: 0,
  content: ''
})

// 已接收的对账单
const receivedStatements = ref([
  {
    id: 1,
    statementNo: 'SP20250001',
    supplierName: '华强供应商',
    period: '2025-08',
    receiveDate: '2025-08-31',
    supplierBalance: 156000,
    ourBalance: 144000,
    difference: 12000,
    status: 'received'
  },
  {
    id: 2,
    statementNo: 'SP20250002',
    supplierName: '金泰材料',
    period: '2025-08',
    receiveDate: '2025-08-31',
    supplierBalance: 234000,
    ourBalance: 234000,
    difference: 0,
    status: 'reconciled'
  },
  {
    id: 3,
    statementNo: 'SP20250003',
    supplierName: '正大钢材',
    period: '2025-08',
    receiveDate: '2025-08-31',
    supplierBalance: 89000,
    ourBalance: 92000,
    difference: -3000,
    status: 'reconciling'
  },
  {
    id: 4,
    statementNo: 'SP20250004',
    supplierName: '永泰机械',
    period: '2025-08',
    receiveDate: '2025-08-31',
    supplierBalance: 167000,
    ourBalance: 167000,
    difference: 0,
    status: 'confirmed'
  },
  {
    id: 5,
    statementNo: 'SP20250005',
    supplierName: '海通物流',
    period: '2025-08',
    receiveDate: '2025-08-31',
    supplierBalance: 45000,
    ourBalance: 45000,
    difference: 0,
    status: 'reconciled'
  },
  {
    id: 6,
    statementNo: 'SP20250006',
    supplierName: '南方电子',
    period: '2025-08',
    receiveDate: '2025-08-31',
    supplierBalance: 298000,
    ourBalance: 305000,
    difference: -7000,
    status: 'received'
  },
  {
    id: 7,
    statementNo: 'SP20250007',
    supplierName: '中科设备',
    period: '2025-08',
    receiveDate: '2025-08-31',
    supplierBalance: 412000,
    ourBalance: 412000,
    difference: 0,
    status: 'confirmed'
  },
  {
    id: 8,
    statementNo: 'SP20250008',
    supplierName: '东方化工',
    period: '2025-08',
    receiveDate: '2025-08-31',
    supplierBalance: 78000,
    ourBalance: 84000,
    difference: -6000,
    status: 'reconciling'
  },
  {
    id: 9,
    statementNo: 'SP20250009',
    supplierName: '联创包装',
    period: '2025-08',
    receiveDate: '2025-08-31',
    supplierBalance: 125000,
    ourBalance: 120000,
    difference: 5000,
    status: 'received'
  },
  {
    id: 10,
    statementNo: 'SP20250010',
    supplierName: '北方工具',
    period: '2025-08',
    receiveDate: '2025-08-31',
    supplierBalance: 67000,
    ourBalance: 67000,
    difference: 0,
    status: 'reconciled'
  },
  {
    id: 11,
    statementNo: 'SP20250011',
    supplierName: '盛达五金',
    period: '2025-08',
    receiveDate: '2025-08-31',
    supplierBalance: 189000,
    ourBalance: 195000,
    difference: -6000,
    status: 'received'
  },
  {
    id: 12,
    statementNo: 'SP20250012',
    supplierName: '新星橡胶',
    period: '2025-08',
    receiveDate: '2025-08-31',
    supplierBalance: 134000,
    ourBalance: 134000,
    difference: 0,
    status: 'confirmed'
  }
])

// 分页计算
const paginatedReceivedStatements = computed(() => {
  const start = (receivedPagination.value.currentPage - 1) * receivedPagination.value.pageSize
  const end = start + receivedPagination.value.pageSize
  return receivedStatements.value.slice(start, end)
})

// 供应商明细
const supplierDetail = ref([
  {
    date: '2025-08-01',
    documentNo: 'PI20250001',
    summary: '原材料采购发票',
    debit: 0,
    credit: 156000,
    balance: 156000,
    matched: true
  },
  {
    date: '2025-08-05',
    documentNo: 'PI20250002',
    summary: '设备配件采购',
    debit: 0,
    credit: 45000,
    balance: 201000,
    matched: true
  },
  {
    date: '2025-08-10',
    documentNo: 'PM20250001',
    summary: '预付账款',
    debit: 50000,
    credit: 0,
    balance: 151000,
    matched: false
  },
  {
    date: '2025-08-15',
    documentNo: 'PI20250003',
    summary: '办公用品采购',
    debit: 0,
    credit: 8500,
    balance: 159500,
    matched: true
  },
  {
    date: '2025-08-20',
    documentNo: 'PM20250002',
    summary: '货款结算',
    debit: 34000,
    credit: 0,
    balance: 125500,
    matched: true
  },
  {
    date: '2025-08-25',
    documentNo: 'PI20250004',
    summary: '维修服务费',
    debit: 0,
    credit: 12000,
    balance: 137500,
    matched: false
  },
  {
    date: '2025-08-28',
    documentNo: 'PM20250003',
    summary: '转账付款',
    debit: 18500,
    credit: 0,
    balance: 119000,
    matched: true
  }
])

// 我方明细
const ourDetail = ref([
  {
    date: '2025-08-01',
    documentNo: 'PI20250001',
    summary: '原材料采购发票',
    debit: 0,
    credit: 144000,
    balance: 144000,
    matched: false
  },
  {
    date: '2025-08-05',
    documentNo: 'PI20250002',
    summary: '设备配件采购',
    debit: 0,
    credit: 45000,
    balance: 189000,
    matched: true
  },
  {
    date: '2025-08-08',
    documentNo: 'PM20250001',
    summary: '预付账款',
    debit: 50000,
    credit: 0,
    balance: 139000,
    matched: true
  },
  {
    date: '2025-08-15',
    documentNo: 'PI20250003',
    summary: '办公用品采购',
    debit: 0,
    credit: 8500,
    balance: 147500,
    matched: true
  },
  {
    date: '2025-08-20',
    documentNo: 'PM20250002',
    summary: '货款结算',
    debit: 34000,
    credit: 0,
    balance: 113500,
    matched: true
  },
  {
    date: '2025-08-22',
    documentNo: 'PI20250005',
    summary: '运输费用',
    debit: 0,
    credit: 5500,
    balance: 119000,
    matched: false
  },
  {
    date: '2025-08-28',
    documentNo: 'PM20250003',
    summary: '转账付款',
    debit: 18500,
    credit: 0,
    balance: 100500,
    matched: true
  },
  {
    date: '2025-08-30',
    documentNo: 'PI20250006',
    summary: '服务费发票',
    debit: 0,
    credit: 25000,
    balance: 125500,
    matched: false
  }
])

// 差异项目
const differenceItems = ref([
  {
    type: 'timing',
    typeName: '时间差异',
    documentNo: 'PI20250001',
    date: '2025-08-01',
    amount: 12000,
    reason: '发票确认时间不一致，供应商8月1日记账，我方8月3日记账',
    suggestion: '核实发票确认日期，统一记账时间'
  },
  {
    type: 'amount',
    typeName: '金额差异',
    documentNo: 'PM20250001',
    date: '2025-08-10',
    amount: 2000,
    reason: '预付账款记录金额不一致',
    suggestion: '核实预付款凭证，确认实际支付金额'
  },
  {
    type: 'missing',
    typeName: '缺失项目',
    documentNo: 'PI20250004',
    date: '2025-08-25',
    amount: 12000,
    reason: '维修服务费供应商已入账，我方未记录',
    suggestion: '补充维修服务费记录'
  },
  {
    type: 'extra',
    typeName: '多余项目',
    documentNo: 'PI20250005',
    date: '2025-08-22',
    amount: -5500,
    reason: '运输费用我方已记录，但供应商未入账',
    suggestion: '与供应商确认运输费用是否应计入本期'
  },
  {
    type: 'rounding',
    typeName: '舍入差异',
    documentNo: 'PI20250006',
    date: '2025-08-30',
    amount: -25,
    reason: '服务费计算精度差异',
    suggestion: '统一小数点保留位数，进行舍入调整'
  }
])

// 确认列表
const confirmList = ref([
  {
    statementNo: 'SP20250001',
    supplierName: '华强供应商',
    period: '2025-08',
    reconcileDate: '2025-09-01',
    difference: 12000,
    confirmStatus: 'pending',
    replyContent: ''
  },
  {
    statementNo: 'SP20250002',
    supplierName: '金泰材料',
    period: '2025-08',
    reconcileDate: '2025-09-01',
    difference: 0,
    confirmStatus: 'confirmed',
    replyContent: '确认无误，同意对账结果'
  },
  {
    statementNo: 'SP20250003',
    supplierName: '正大钢材',
    period: '2025-08',
    reconcileDate: '2025-09-02',
    difference: -3000,
    confirmStatus: 'pending',
    replyContent: ''
  },
  {
    statementNo: 'SP20250004',
    supplierName: '永泰机械',
    period: '2025-08',
    reconcileDate: '2025-09-02',
    difference: 0,
    confirmStatus: 'confirmed',
    replyContent: '对账一致，已确认'
  },
  {
    statementNo: 'SP20250005',
    supplierName: '海通物流',
    period: '2025-08',
    reconcileDate: '2025-09-03',
    difference: 0,
    confirmStatus: 'confirmed',
    replyContent: '核对完毕，无差异'
  },
  {
    statementNo: 'SP20250006',
    supplierName: '南方电子',
    period: '2025-08',
    reconcileDate: '2025-09-03',
    difference: -7000,
    confirmStatus: 'rejected',
    replyContent: '存在差异，需要进一步核实'
  },
  {
    statementNo: 'SP20250007',
    supplierName: '中科设备',
    period: '2025-08',
    reconcileDate: '2025-09-04',
    difference: 0,
    confirmStatus: 'confirmed',
    replyContent: '账务核对无误'
  },
  {
    statementNo: 'SP20250008',
    supplierName: '东方化工',
    period: '2025-08',
    reconcileDate: '',
    difference: -6000,
    confirmStatus: 'pending',
    replyContent: ''
  },
  {
    statementNo: 'SP20250009',
    supplierName: '联创包装',
    period: '2025-08',
    reconcileDate: '2025-09-04',
    difference: 5000,
    confirmStatus: 'pending',
    replyContent: ''
  },
  {
    statementNo: 'SP20250010',
    supplierName: '北方工具',
    period: '2025-08',
    reconcileDate: '2025-09-05',
    difference: 0,
    confirmStatus: 'confirmed',
    replyContent: '对账结果准确'
  }
])

// 分页计算
const paginatedConfirmList = computed(() => {
  const start = (confirmPagination.value.currentPage - 1) * confirmPagination.value.pageSize
  const end = start + confirmPagination.value.pageSize
  return confirmList.value.slice(start, end)
})

const formatMoney = (amount) => {
  if (!amount) return '¥0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const getReconcileStatusType = (status) => {
  const map = {
    'received': 'info',
    'reconciling': 'warning',
    'reconciled': 'success',
    'confirmed': 'primary'
  }
  return map[status] || ''
}

const getReconcileStatusText = (status) => {
  const map = {
    'received': '已接收',
    'reconciling': '核对中',
    'reconciled': '已核对',
    'confirmed': '已确认'
  }
  return map[status] || status
}

const getConfirmStatusType = (status) => {
  const map = {
    'pending': 'warning',
    'confirmed': 'success',
    'rejected': 'danger'
  }
  return map[status] || ''
}

const getConfirmStatusText = (status) => {
  const map = {
    'pending': '待确认',
    'confirmed': '已确认',
    'rejected': '已拒绝'
  }
  return map[status] || status
}

const getDifferenceType = (type) => {
  const map = {
    'timing': 'warning',
    'amount': 'danger',
    'missing': 'info'
  }
  return map[type] || ''
}

const handleReceiveStatement = () => {
  receiveDialog.value = true
}

const handleBatchDownload = () => {
  ElMessage.success('批量下载功能开发中')
}

const handleStatementSelection = (selection) => {
  // 处理选择
}

const handleReconcile = (row) => {
  currentStatement.value = row
  activeTab.value = 'reconcile'
}

const handleConfirm = (row) => {
  row.status = 'confirmed'
  ElMessage.success('对账单确认成功')
}

const handleViewDetail = (row) => {
  currentStatement.value = row
  activeTab.value = 'reconcile'
}

const handleAutoReconcile = () => {
  ElMessage.success('自动核对完成')
}

const handleSaveReconcile = () => {
  if (currentStatement.value) {
    currentStatement.value.status = 'reconciled'
  }
  ElMessage.success('核对结果已保存')
}

const handleReportDifference = () => {
  ElMessage.info('差异报告功能开发中')
}

const handleAdjust = (row) => {
  ElMessage.info(`调整差异：${row.documentNo}`)
}

const handleReply = (row) => {
  replyDialog.value = true
}

const handleConfirmFinal = (row) => {
  row.confirmStatus = 'confirmed'
  ElMessage.success('确认成功')
}

const handleBeforeUpload = (file) => {
  const isValid = file.type === 'application/vnd.ms-excel' || 
                  file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                  file.type === 'application/pdf'
  
  if (!isValid) {
    ElMessage.error('只能上传 Excel 或 PDF 文件！')
    return false
  }
  
  return true
}

const handleSubmitReceive = () => {
  receiveDialog.value = false
  ElMessage.success('对账单接收成功')
}

const handleSubmitReply = () => {
  replyDialog.value = false
  ElMessage.success('回复发送成功')
}

// 分页处理
const handleReceivedSizeChange = (size) => {
  receivedPagination.value.pageSize = size
  receivedPagination.value.currentPage = 1
}

const handleReceivedCurrentChange = (page) => {
  receivedPagination.value.currentPage = page
}

const handleConfirmSizeChange = (size) => {
  confirmPagination.value.pageSize = size
  confirmPagination.value.currentPage = 1
}

const handleConfirmCurrentChange = (page) => {
  confirmPagination.value.currentPage = page
}
</script>

<style scoped lang="scss">
.table-wrapper {
  .table-header {
    margin-bottom: 16px;
    display: flex;
    gap: 10px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reconcile-summary {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
}

.reconcile-actions {
  margin-top: 20px;
  text-align: center;
  
  .el-button {
    margin: 0 8px;
  }
}

.difference-amount {
  color: #f56c6c;
  font-weight: 600;
}

.no-difference {
  color: #67c23a;
  font-weight: 600;
}

.balance-amount {
  color: #303133;
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

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
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