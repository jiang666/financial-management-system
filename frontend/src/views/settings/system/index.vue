<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">系统参数</h2>
      <p class="description">配置系统基本参数，包括会计期间、凭证字号等设置</p>
    </div>
    
    <el-row :gutter="20" v-loading="loading">
      <el-col :span="12">
        <el-card header="基本设置">
          <el-form :model="basicForm" label-width="120px">
            <el-form-item label="公司名称">
              <el-input v-model="basicForm.companyName" />
            </el-form-item>
            <el-form-item label="纳税人识别号">
              <el-input v-model="basicForm.taxNumber" />
            </el-form-item>
            <el-form-item label="法人代表">
              <el-input v-model="basicForm.legalPerson" />
            </el-form-item>
            <el-form-item label="注册地址">
              <el-input v-model="basicForm.address" type="textarea" rows="2" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="basicForm.phone" />
            </el-form-item>
            <el-form-item label="电子邮箱">
              <el-input v-model="basicForm.email" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveBasicSettings">保存基本设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card header="会计期间设置">
          <el-form :model="periodForm" label-width="120px">
            <el-form-item label="会计年度">
              <el-date-picker
                v-model="periodForm.fiscalYear"
                type="year"
                placeholder="选择会计年度"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="启用期间">
              <el-date-picker
                v-model="periodForm.startPeriod"
                type="month"
                placeholder="选择启用期间"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="当前期间">
              <el-date-picker
                v-model="periodForm.currentPeriod"
                type="month"
                placeholder="选择当前期间"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="是否按期结账">
              <el-switch v-model="periodForm.isPeriodClose" />
            </el-form-item>
            <el-form-item label="自动结转损益">
              <el-switch v-model="periodForm.autoCarryForward" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="savePeriodSettings">保存期间设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-lg">
      <el-col :span="12">
        <el-card header="凭证设置">
          <el-form :model="voucherForm" label-width="120px">
            <el-form-item label="凭证字设置">
              <el-table :data="voucherTypes" border>
                <el-table-column prop="code" label="凭证字" width="100" />
                <el-table-column prop="name" label="名称" width="150" />
                <el-table-column label="启用状态" width="100">
                  <template #default="{ row }">
                    <el-switch v-model="row.enabled" />
                  </template>
                </el-table-column>
                <el-table-column label="操作">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="editVoucherType(row)">编辑</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-form-item>
            <el-form-item label="凭证编号规则">
              <el-select v-model="voucherForm.numberRule" style="width: 100%">
                <el-option label="按月重新编号" value="monthly" />
                <el-option label="按年重新编号" value="yearly" />
                <el-option label="连续编号" value="continuous" />
              </el-select>
            </el-form-item>
            <el-form-item label="编号位数">
              <el-input-number v-model="voucherForm.numberLength" :min="3" :max="8" style="width: 100%" />
            </el-form-item>
            <el-form-item label="审核后允许修改">
              <el-switch v-model="voucherForm.allowEditAfterAudit" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveVoucherSettings">保存凭证设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card header="权限控制">
          <el-form :model="permissionForm" label-width="120px">
            <el-form-item label="制单人可修改">
              <el-switch v-model="permissionForm.creatorCanEdit" />
            </el-form-item>
            <el-form-item label="审核人可修改">
              <el-switch v-model="permissionForm.auditorCanEdit" />
            </el-form-item>
            <el-form-item label="记账人可修改">
              <el-switch v-model="permissionForm.bookkeeperCanEdit" />
            </el-form-item>
            <el-form-item label="跨期间查询">
              <el-switch v-model="permissionForm.crossPeriodQuery" />
            </el-form-item>
            <el-form-item label="数据导出权限">
              <el-checkbox-group v-model="permissionForm.exportRoles">
                <el-checkbox label="admin">管理员</el-checkbox>
                <el-checkbox label="finance">财务主管</el-checkbox>
                <el-checkbox label="accountant">会计</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="登录失败锁定">
              <el-switch v-model="permissionForm.loginLock" />
            </el-form-item>
            <el-form-item label="失败次数限制" v-if="permissionForm.loginLock">
              <el-input-number v-model="permissionForm.maxFailAttempts" :min="3" :max="10" style="width: 100%" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="savePermissionSettings">保存权限设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-lg">
      <el-col :span="24">
        <el-card header="数据备份设置">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form :model="backupForm" label-width="120px">
                <el-form-item label="自动备份">
                  <el-switch v-model="backupForm.autoBackup" />
                </el-form-item>
                <el-form-item label="备份频率" v-if="backupForm.autoBackup">
                  <el-select v-model="backupForm.frequency" style="width: 100%">
                    <el-option label="每日" value="daily" />
                    <el-option label="每周" value="weekly" />
                    <el-option label="每月" value="monthly" />
                  </el-select>
                </el-form-item>
                <el-form-item label="备份时间" v-if="backupForm.autoBackup">
                  <el-time-picker
                    v-model="backupForm.backupTime"
                    format="HH:mm"
                    placeholder="选择备份时间"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="保留天数">
                  <el-input-number v-model="backupForm.retentionDays" :min="7" :max="365" style="width: 100%" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveBackupSettings">保存备份设置</el-button>
                  <el-button type="success" @click="manualBackup" class="ml-sm">立即备份</el-button>
                </el-form-item>
              </el-form>
            </el-col>
            <el-col :span="12">
              <div class="backup-list">
                <h4>备份历史</h4>
                <el-table :data="backupHistory" size="small">
                  <el-table-column prop="filename" label="文件名" />
                  <el-table-column prop="size" label="大小" width="100" />
                  <el-table-column prop="createTime" label="创建时间" width="150" />
                  <el-table-column label="操作" width="120">
                    <template #default="{ row }">
                      <el-button link type="primary" @click="downloadBackup(row)">下载</el-button>
                      <el-button link type="danger" @click="deleteBackup(row)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { systemParamApi } from '@/api/system-params'

// 加载状态
const loading = ref(false)

// 参数映射表
const paramMap = ref({})

// 表单数据
const basicForm = ref({
  companyName: '',
  taxNumber: '',
  legalPerson: '',
  address: '',
  phone: '',
  email: ''
})

const periodForm = ref({
  fiscalYear: null,
  startPeriod: null,
  currentPeriod: null,
  isPeriodClose: true,
  autoCarryForward: true
})

const voucherForm = ref({
  numberRule: 'monthly',
  numberLength: 4,
  allowEditAfterAudit: false
})

const permissionForm = ref({
  creatorCanEdit: true,
  auditorCanEdit: false,
  bookkeeperCanEdit: false,
  crossPeriodQuery: false,
  exportRoles: [],
  loginLock: true,
  maxFailAttempts: 5
})

const backupForm = ref({
  autoBackup: true,
  frequency: 'daily',
  backupTime: new Date('2025-08-15 02:00:00'),
  retentionDays: 30
})

const voucherTypes = ref([
  { code: '记', name: '记账凭证', enabled: true },
  { code: '收', name: '收款凭证', enabled: true },
  { code: '付', name: '付款凭证', enabled: true },
  { code: '转', name: '转账凭证', enabled: false }
])

const backupHistory = ref([
  { filename: 'backup_20250815.sql', size: '125MB', createTime: '2025-08-15 02:00:00' },
  { filename: 'backup_20250814.sql', size: '123MB', createTime: '2025-08-14 02:00:00' },
  { filename: 'backup_20250813.sql', size: '121MB', createTime: '2025-08-13 02:00:00' }
])

// 加载所有系统参数
const loadAllParameters = async () => {
  loading.value = true
  try {
    const res = await systemParamApi.getParameters({ size: 100 })
    
    if (res.code === 200 && res.data && res.data.content) {
      // 创建参数映射
      paramMap.value = {}
      res.data.content.forEach(param => {
        paramMap.value[param.paramKey] = param
      })
      
      // 映射到表单数据
      mapParamsToForms()
    }
  } catch (error) {
    console.error('加载参数失败:', error)
    ElMessage.warning('加载系统参数失败，使用默认值')
    // 使用默认值
    setDefaultValues()
  } finally {
    loading.value = false
  }
}

// 设置默认值
const setDefaultValues = () => {
  basicForm.value = {
    companyName: '示例科技有限公司',
    taxNumber: '91110000123456789X',
    legalPerson: '张三',
    address: '北京市朝阳区示例街道123号',
    phone: '010-12345678',
    email: 'company@example.com'
  }
  
  periodForm.value = {
    fiscalYear: new Date('2025-01-01'),
    startPeriod: new Date('2025-01-01'),
    currentPeriod: new Date('2025-08-01'),
    isPeriodClose: true,
    autoCarryForward: true
  }
  
  permissionForm.value.exportRoles = ['admin', 'finance']
}

// 将参数映射到各个表单
const mapParamsToForms = () => {
  // 基本设置
  if (paramMap.value['company.name']) {
    basicForm.value.companyName = paramMap.value['company.name'].paramValue || ''
  }
  if (paramMap.value['company.tax_number']) {
    basicForm.value.taxNumber = paramMap.value['company.tax_number'].paramValue || ''
  }
  if (paramMap.value['company.legal_person']) {
    basicForm.value.legalPerson = paramMap.value['company.legal_person'].paramValue || ''
  }
  if (paramMap.value['company.address']) {
    basicForm.value.address = paramMap.value['company.address'].paramValue || ''
  }
  if (paramMap.value['company.phone']) {
    basicForm.value.phone = paramMap.value['company.phone'].paramValue || ''
  }
  if (paramMap.value['company.email']) {
    basicForm.value.email = paramMap.value['company.email'].paramValue || ''
  }
  
  // 会计期间设置
  if (paramMap.value['period.fiscal_year']) {
    const year = paramMap.value['period.fiscal_year'].paramValue
    periodForm.value.fiscalYear = year ? new Date(year + '-01-01') : null
  }
  if (paramMap.value['period.start_period']) {
    const period = paramMap.value['period.start_period'].paramValue
    periodForm.value.startPeriod = period ? new Date(period + '-01') : null
  }
  if (paramMap.value['period.current_period']) {
    const period = paramMap.value['period.current_period'].paramValue
    periodForm.value.currentPeriod = period ? new Date(period + '-01') : null
  }
  if (paramMap.value['period.is_period_close']) {
    periodForm.value.isPeriodClose = paramMap.value['period.is_period_close'].paramValue === 'true'
  }
  if (paramMap.value['period.auto_carry_forward']) {
    periodForm.value.autoCarryForward = paramMap.value['period.auto_carry_forward'].paramValue === 'true'
  }
  
  // 凭证设置
  if (paramMap.value['voucher.number_rule']) {
    const ruleMap = { 'YEARLY': 'yearly', 'MONTHLY': 'monthly', 'CONTINUOUS': 'continuous' }
    voucherForm.value.numberRule = ruleMap[paramMap.value['voucher.number_rule'].paramValue] || 'monthly'
  }
  if (paramMap.value['voucher.number_length']) {
    voucherForm.value.numberLength = parseInt(paramMap.value['voucher.number_length'].paramValue) || 4
  }
  if (paramMap.value['voucher.allow_edit_after_audit']) {
    voucherForm.value.allowEditAfterAudit = paramMap.value['voucher.allow_edit_after_audit'].paramValue === 'true'
  }
  if (paramMap.value['voucher.types']) {
    try {
      voucherTypes.value = JSON.parse(paramMap.value['voucher.types'].paramValue)
    } catch (e) {
      console.error('解析凭证类型失败:', e)
    }
  }
  
  // 权限控制
  if (paramMap.value['permission.creator_can_edit']) {
    permissionForm.value.creatorCanEdit = paramMap.value['permission.creator_can_edit'].paramValue === 'true'
  }
  if (paramMap.value['permission.auditor_can_edit']) {
    permissionForm.value.auditorCanEdit = paramMap.value['permission.auditor_can_edit'].paramValue === 'true'
  }
  if (paramMap.value['permission.bookkeeper_can_edit']) {
    permissionForm.value.bookkeeperCanEdit = paramMap.value['permission.bookkeeper_can_edit'].paramValue === 'true'
  }
  if (paramMap.value['permission.cross_period_query']) {
    permissionForm.value.crossPeriodQuery = paramMap.value['permission.cross_period_query'].paramValue === 'true'
  }
  if (paramMap.value['permission.export_roles']) {
    try {
      permissionForm.value.exportRoles = JSON.parse(paramMap.value['permission.export_roles'].paramValue)
    } catch (e) {
      permissionForm.value.exportRoles = ['admin', 'finance']
    }
  }
  if (paramMap.value['login.lock_enabled']) {
    permissionForm.value.loginLock = paramMap.value['login.lock_enabled'].paramValue === 'true'
  }
  if (paramMap.value['login.max_attempts']) {
    permissionForm.value.maxFailAttempts = parseInt(paramMap.value['login.max_attempts'].paramValue) || 5
  }
  
  // 备份设置
  if (paramMap.value['backup.auto_enable']) {
    backupForm.value.autoBackup = paramMap.value['backup.auto_enable'].paramValue === 'true'
  }
  if (paramMap.value['backup.frequency']) {
    backupForm.value.frequency = paramMap.value['backup.frequency'].paramValue.toLowerCase()
  }
  if (paramMap.value['backup.schedule']) {
    const [hour, minute] = paramMap.value['backup.schedule'].paramValue.split(':')
    const date = new Date()
    date.setHours(parseInt(hour) || 2)
    date.setMinutes(parseInt(minute) || 0)
    backupForm.value.backupTime = date
  }
  if (paramMap.value['backup.retention_days']) {
    backupForm.value.retentionDays = parseInt(paramMap.value['backup.retention_days'].paramValue) || 30
  }
}

// 格式化年月
const formatYearMonth = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = (d.getMonth() + 1).toString().padStart(2, '0')
  return `${year}-${month}`
}

// 格式化时间
const formatTime = (date) => {
  if (!date) return '02:00'
  const d = new Date(date)
  const hour = d.getHours().toString().padStart(2, '0')
  const minute = d.getMinutes().toString().padStart(2, '0')
  return `${hour}:${minute}`
}

// 页面加载时初始化
onMounted(() => {
  loadAllParameters()
})

// 保存基本设置
const saveBasicSettings = async () => {
  loading.value = true
  try {
    const updates = [
      { paramKey: 'company.name', paramValue: basicForm.value.companyName || '' },
      { paramKey: 'company.tax_number', paramValue: basicForm.value.taxNumber || '' },
      { paramKey: 'company.legal_person', paramValue: basicForm.value.legalPerson || '' },
      { paramKey: 'company.address', paramValue: basicForm.value.address || '' },
      { paramKey: 'company.phone', paramValue: basicForm.value.phone || '' },
      { paramKey: 'company.email', paramValue: basicForm.value.email || '' }
    ]
    
    const res = await systemParamApi.batchUpdate(updates)
    
    if (res.code === 200) {
      ElMessage.success('基本设置保存成功')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存基本设置失败')
  } finally {
    loading.value = false
  }
}

// 保存会计期间设置
const savePeriodSettings = async () => {
  loading.value = true
  try {
    const updates = [
      { 
        paramKey: 'period.fiscal_year', 
        paramValue: periodForm.value.fiscalYear ? new Date(periodForm.value.fiscalYear).getFullYear().toString() : ''
      },
      { 
        paramKey: 'period.start_period', 
        paramValue: periodForm.value.startPeriod ? formatYearMonth(periodForm.value.startPeriod) : ''
      },
      { 
        paramKey: 'period.current_period', 
        paramValue: periodForm.value.currentPeriod ? formatYearMonth(periodForm.value.currentPeriod) : ''
      },
      { paramKey: 'period.is_period_close', paramValue: periodForm.value.isPeriodClose.toString() },
      { paramKey: 'period.auto_carry_forward', paramValue: periodForm.value.autoCarryForward.toString() }
    ]
    
    const res = await systemParamApi.batchUpdate(updates)
    
    if (res.code === 200) {
      ElMessage.success('会计期间设置保存成功')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存会计期间设置失败')
  } finally {
    loading.value = false
  }
}

// 保存凭证设置
const saveVoucherSettings = async () => {
  loading.value = true
  try {
    const ruleMap = { 'yearly': 'YEARLY', 'monthly': 'MONTHLY', 'continuous': 'CONTINUOUS' }
    
    const updates = [
      { paramKey: 'voucher.number_rule', paramValue: ruleMap[voucherForm.value.numberRule] || 'MONTHLY' },
      { paramKey: 'voucher.number_length', paramValue: voucherForm.value.numberLength.toString() },
      { paramKey: 'voucher.allow_edit_after_audit', paramValue: voucherForm.value.allowEditAfterAudit.toString() },
      { paramKey: 'voucher.types', paramValue: JSON.stringify(voucherTypes.value) }
    ]
    
    const res = await systemParamApi.batchUpdate(updates)
    
    if (res.code === 200) {
      ElMessage.success('凭证设置保存成功')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存凭证设置失败')
  } finally {
    loading.value = false
  }
}

// 保存权限设置
const savePermissionSettings = async () => {
  loading.value = true
  try {
    const updates = [
      { paramKey: 'permission.creator_can_edit', paramValue: permissionForm.value.creatorCanEdit.toString() },
      { paramKey: 'permission.auditor_can_edit', paramValue: permissionForm.value.auditorCanEdit.toString() },
      { paramKey: 'permission.bookkeeper_can_edit', paramValue: permissionForm.value.bookkeeperCanEdit.toString() },
      { paramKey: 'permission.cross_period_query', paramValue: permissionForm.value.crossPeriodQuery.toString() },
      { paramKey: 'permission.export_roles', paramValue: JSON.stringify(permissionForm.value.exportRoles) },
      { paramKey: 'login.lock_enabled', paramValue: permissionForm.value.loginLock.toString() },
      { paramKey: 'login.max_attempts', paramValue: permissionForm.value.maxFailAttempts.toString() }
    ]
    
    const res = await systemParamApi.batchUpdate(updates)
    
    if (res.code === 200) {
      ElMessage.success('权限设置保存成功')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存权限设置失败')
  } finally {
    loading.value = false
  }
}

// 保存备份设置
const saveBackupSettings = async () => {
  loading.value = true
  try {
    const updates = [
      { paramKey: 'backup.auto_enable', paramValue: backupForm.value.autoBackup.toString() },
      { paramKey: 'backup.frequency', paramValue: backupForm.value.frequency.toUpperCase() },
      { 
        paramKey: 'backup.schedule', 
        paramValue: backupForm.value.backupTime ? formatTime(backupForm.value.backupTime) : '02:00'
      },
      { paramKey: 'backup.retention_days', paramValue: backupForm.value.retentionDays.toString() }
    ]
    
    const res = await systemParamApi.batchUpdate(updates)
    
    if (res.code === 200) {
      ElMessage.success('备份设置保存成功')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存备份设置失败')
  } finally {
    loading.value = false
  }
}

// 编辑凭证类型
const editVoucherType = (row) => {
  ElMessage.info(`编辑凭证字：${row.name}`)
}

// 手动备份
const manualBackup = () => {
  ElMessage.success('手动备份已启动')
}

// 下载备份
const downloadBackup = (row) => {
  ElMessage.success(`下载备份文件：${row.filename}`)
}

// 删除备份
const deleteBackup = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该备份文件吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success(`删除备份文件：${row.filename}`)
  } catch (e) {
    // 用户取消
  }
}
</script>

<style scoped lang="scss">
.backup-list {
  h4 {
    margin-bottom: 15px;
    color: #303133;
  }
}
</style>