// 系统参数页面API集成示例
// 将这些方法添加到您的 system/index.vue 组件中

import { systemParamApi } from '@/api/system-params'
import { ElMessage } from 'element-plus'

export default {
  data() {
    return {
      // ... 您现有的data
      loading: false,
      paramMap: {} // 存储参数键值对映射
    }
  },
  
  created() {
    this.loadAllParameters()
  },
  
  methods: {
    // 加载所有系统参数
    async loadAllParameters() {
      this.loading = true
      try {
        const res = await systemParamApi.getParameters({ size: 100 })
        
        if (res.code === 200 && res.data && res.data.content) {
          // 创建参数映射
          this.paramMap = {}
          res.data.content.forEach(param => {
            this.paramMap[param.paramKey] = param
          })
          
          // 映射到表单数据
          this.mapParamsToForms()
          
          ElMessage.success('系统参数加载成功')
        }
      } catch (error) {
        console.error('加载参数失败:', error)
        ElMessage.error('加载系统参数失败，使用默认值')
      } finally {
        this.loading = false
      }
    },
    
    // 将参数映射到各个表单
    mapParamsToForms() {
      // 基本设置
      if (this.paramMap['company.name']) {
        this.basicForm.companyName = this.paramMap['company.name'].paramValue
      }
      if (this.paramMap['company.tax_number']) {
        this.basicForm.taxNumber = this.paramMap['company.tax_number'].paramValue
      }
      if (this.paramMap['company.legal_person']) {
        this.basicForm.legalPerson = this.paramMap['company.legal_person'].paramValue
      }
      if (this.paramMap['company.address']) {
        this.basicForm.address = this.paramMap['company.address'].paramValue
      }
      if (this.paramMap['company.phone']) {
        this.basicForm.phone = this.paramMap['company.phone'].paramValue
      }
      if (this.paramMap['company.email']) {
        this.basicForm.email = this.paramMap['company.email'].paramValue
      }
      
      // 会计期间设置
      if (this.paramMap['period.fiscal_year']) {
        this.periodForm.fiscalYear = new Date(this.paramMap['period.fiscal_year'].paramValue + '-01-01')
      }
      if (this.paramMap['period.start_period']) {
        this.periodForm.startPeriod = new Date(this.paramMap['period.start_period'].paramValue + '-01')
      }
      if (this.paramMap['period.current_period']) {
        this.periodForm.currentPeriod = new Date(this.paramMap['period.current_period'].paramValue + '-01')
      }
      if (this.paramMap['period.is_period_close']) {
        this.periodForm.isPeriodClose = this.paramMap['period.is_period_close'].paramValue === 'true'
      }
      if (this.paramMap['period.auto_carry_forward']) {
        this.periodForm.autoCarryForward = this.paramMap['period.auto_carry_forward'].paramValue === 'true'
      }
      
      // 凭证设置
      if (this.paramMap['voucher.number_rule']) {
        // 映射枚举值: MONTHLY -> monthly
        const ruleMap = { 'YEARLY': 'yearly', 'MONTHLY': 'monthly', 'CONTINUOUS': 'continuous' }
        this.voucherForm.numberRule = ruleMap[this.paramMap['voucher.number_rule'].paramValue] || 'monthly'
      }
      if (this.paramMap['voucher.number_length']) {
        this.voucherForm.numberLength = parseInt(this.paramMap['voucher.number_length'].paramValue) || 4
      }
      if (this.paramMap['voucher.allow_edit_after_audit']) {
        this.voucherForm.allowEditAfterAudit = this.paramMap['voucher.allow_edit_after_audit'].paramValue === 'true'
      }
      if (this.paramMap['voucher.types']) {
        try {
          this.voucherTypes = JSON.parse(this.paramMap['voucher.types'].paramValue)
        } catch (e) {
          console.error('解析凭证类型失败:', e)
        }
      }
      
      // 权限控制
      if (this.paramMap['permission.creator_can_edit']) {
        this.permissionForm.creatorCanEdit = this.paramMap['permission.creator_can_edit'].paramValue === 'true'
      }
      if (this.paramMap['permission.auditor_can_edit']) {
        this.permissionForm.auditorCanEdit = this.paramMap['permission.auditor_can_edit'].paramValue === 'true'
      }
      if (this.paramMap['permission.bookkeeper_can_edit']) {
        this.permissionForm.bookkeeperCanEdit = this.paramMap['permission.bookkeeper_can_edit'].paramValue === 'true'
      }
      if (this.paramMap['permission.cross_period_query']) {
        this.permissionForm.crossPeriodQuery = this.paramMap['permission.cross_period_query'].paramValue === 'true'
      }
      if (this.paramMap['permission.export_roles']) {
        try {
          this.permissionForm.exportRoles = JSON.parse(this.paramMap['permission.export_roles'].paramValue)
        } catch (e) {
          console.error('解析导出权限失败:', e)
        }
      }
      if (this.paramMap['login.lock_enabled']) {
        this.permissionForm.loginLock = this.paramMap['login.lock_enabled'].paramValue === 'true'
      }
      if (this.paramMap['login.max_attempts']) {
        this.permissionForm.maxFailAttempts = parseInt(this.paramMap['login.max_attempts'].paramValue) || 5
      }
      
      // 备份设置
      if (this.paramMap['backup.auto_enable']) {
        this.backupForm.autoBackup = this.paramMap['backup.auto_enable'].paramValue === 'true'
      }
      if (this.paramMap['backup.frequency']) {
        this.backupForm.frequency = this.paramMap['backup.frequency'].paramValue.toLowerCase()
      }
      if (this.paramMap['backup.schedule']) {
        // 将 HH:mm 格式转换为 Date 对象
        const [hour, minute] = this.paramMap['backup.schedule'].paramValue.split(':')
        const date = new Date()
        date.setHours(parseInt(hour) || 2)
        date.setMinutes(parseInt(minute) || 0)
        this.backupForm.backupTime = date
      }
      if (this.paramMap['backup.retention_days']) {
        this.backupForm.retentionDays = parseInt(this.paramMap['backup.retention_days'].paramValue) || 30
      }
    },
    
    // 保存基本设置（修改原有方法）
    async saveBasicSettings() {
      this.loading = true
      try {
        const updates = [
          { paramKey: 'company.name', paramValue: this.basicForm.companyName || '' },
          { paramKey: 'company.tax_number', paramValue: this.basicForm.taxNumber || '' },
          { paramKey: 'company.legal_person', paramValue: this.basicForm.legalPerson || '' },
          { paramKey: 'company.address', paramValue: this.basicForm.address || '' },
          { paramKey: 'company.phone', paramValue: this.basicForm.phone || '' },
          { paramKey: 'company.email', paramValue: this.basicForm.email || '' }
        ]
        
        const res = await systemParamApi.batchUpdate(updates)
        
        if (res.code === 200 && res.data) {
          ElMessage.success(`基本设置保存成功，更新了 ${res.data.successCount} 个参数`)
        }
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存基本设置失败：' + (error.message || '未知错误'))
      } finally {
        this.loading = false
      }
    },
    
    // 保存会计期间设置（修改原有方法）
    async savePeriodSettings() {
      this.loading = true
      try {
        const updates = [
          { 
            paramKey: 'period.fiscal_year', 
            paramValue: this.periodForm.fiscalYear ? new Date(this.periodForm.fiscalYear).getFullYear().toString() : ''
          },
          { 
            paramKey: 'period.start_period', 
            paramValue: this.periodForm.startPeriod ? this.formatYearMonth(this.periodForm.startPeriod) : ''
          },
          { 
            paramKey: 'period.current_period', 
            paramValue: this.periodForm.currentPeriod ? this.formatYearMonth(this.periodForm.currentPeriod) : ''
          },
          { paramKey: 'period.is_period_close', paramValue: this.periodForm.isPeriodClose.toString() },
          { paramKey: 'period.auto_carry_forward', paramValue: this.periodForm.autoCarryForward.toString() }
        ]
        
        const res = await systemParamApi.batchUpdate(updates)
        
        if (res.code === 200 && res.data) {
          ElMessage.success('会计期间设置保存成功')
        }
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败：' + (error.message || '未知错误'))
      } finally {
        this.loading = false
      }
    },
    
    // 保存凭证设置（修改原有方法）
    async saveVoucherSettings() {
      this.loading = true
      try {
        // 映射前端值到后端枚举
        const ruleMap = { 'yearly': 'YEARLY', 'monthly': 'MONTHLY', 'continuous': 'CONTINUOUS' }
        
        const updates = [
          { paramKey: 'voucher.number_rule', paramValue: ruleMap[this.voucherForm.numberRule] || 'MONTHLY' },
          { paramKey: 'voucher.number_length', paramValue: this.voucherForm.numberLength.toString() },
          { paramKey: 'voucher.allow_edit_after_audit', paramValue: this.voucherForm.allowEditAfterAudit.toString() },
          { paramKey: 'voucher.types', paramValue: JSON.stringify(this.voucherTypes) }
        ]
        
        const res = await systemParamApi.batchUpdate(updates)
        
        if (res.code === 200 && res.data) {
          ElMessage.success('凭证设置保存成功')
        }
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败：' + (error.message || '未知错误'))
      } finally {
        this.loading = false
      }
    },
    
    // 保存权限设置（修改原有方法）
    async savePermissionSettings() {
      this.loading = true
      try {
        const updates = [
          { paramKey: 'permission.creator_can_edit', paramValue: this.permissionForm.creatorCanEdit.toString() },
          { paramKey: 'permission.auditor_can_edit', paramValue: this.permissionForm.auditorCanEdit.toString() },
          { paramKey: 'permission.bookkeeper_can_edit', paramValue: this.permissionForm.bookkeeperCanEdit.toString() },
          { paramKey: 'permission.cross_period_query', paramValue: this.permissionForm.crossPeriodQuery.toString() },
          { paramKey: 'permission.export_roles', paramValue: JSON.stringify(this.permissionForm.exportRoles) },
          { paramKey: 'login.lock_enabled', paramValue: this.permissionForm.loginLock.toString() },
          { paramKey: 'login.max_attempts', paramValue: this.permissionForm.maxFailAttempts.toString() }
        ]
        
        const res = await systemParamApi.batchUpdate(updates)
        
        if (res.code === 200 && res.data) {
          ElMessage.success('权限设置保存成功')
        }
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败：' + (error.message || '未知错误'))
      } finally {
        this.loading = false
      }
    },
    
    // 保存备份设置（修改原有方法）
    async saveBackupSettings() {
      this.loading = true
      try {
        const updates = [
          { paramKey: 'backup.auto_enable', paramValue: this.backupForm.autoBackup.toString() },
          { paramKey: 'backup.frequency', paramValue: this.backupForm.frequency.toUpperCase() },
          { 
            paramKey: 'backup.schedule', 
            paramValue: this.backupForm.backupTime ? this.formatTime(this.backupForm.backupTime) : '02:00'
          },
          { paramKey: 'backup.retention_days', paramValue: this.backupForm.retentionDays.toString() }
        ]
        
        const res = await systemParamApi.batchUpdate(updates)
        
        if (res.code === 200 && res.data) {
          ElMessage.success('备份设置保存成功')
        }
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败：' + (error.message || '未知错误'))
      } finally {
        this.loading = false
      }
    },
    
    // 辅助方法：格式化年月
    formatYearMonth(date) {
      if (!date) return ''
      const d = new Date(date)
      const year = d.getFullYear()
      const month = (d.getMonth() + 1).toString().padStart(2, '0')
      return `${year}-${month}`
    },
    
    // 辅助方法：格式化时间
    formatTime(date) {
      if (!date) return '02:00'
      const d = new Date(date)
      const hour = d.getHours().toString().padStart(2, '0')
      const minute = d.getMinutes().toString().padStart(2, '0')
      return `${hour}:${minute}`
    }
  }
}