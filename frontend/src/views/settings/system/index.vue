<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">系统参数</h2>
      <p class="description">配置系统基本参数，包括会计期间、凭证字号等设置</p>
    </div>
    
    <el-row :gutter="20">
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
import { ElMessage } from 'element-plus'

const basicForm = ref({
  companyName: '示例科技有限公司',
  taxNumber: '91110000123456789X',
  legalPerson: '张三',
  address: '北京市朝阳区示例街道123号',
  phone: '010-12345678',
  email: 'company@example.com'
})

const periodForm = ref({
  fiscalYear: new Date('2025-01-01'),
  startPeriod: new Date('2025-01-01'),
  currentPeriod: new Date('2025-08-01'),
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
  exportRoles: ['admin', 'finance'],
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

onMounted(() => {
  // 初始化数据
})

const saveBasicSettings = () => {
  ElMessage.success('基本设置保存成功')
}

const savePeriodSettings = () => {
  ElMessage.success('会计期间设置保存成功')
}

const saveVoucherSettings = () => {
  ElMessage.success('凭证设置保存成功')
}

const savePermissionSettings = () => {
  ElMessage.success('权限设置保存成功')
}

const saveBackupSettings = () => {
  ElMessage.success('备份设置保存成功')
}

const editVoucherType = (row) => {
  ElMessage.info(`编辑凭证字：${row.name}`)
}

const manualBackup = () => {
  ElMessage.success('手动备份已启动')
}

const downloadBackup = (row) => {
  ElMessage.success(`下载备份文件：${row.filename}`)
}

const deleteBackup = (row) => {
  ElMessage.success(`删除备份文件：${row.filename}`)
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