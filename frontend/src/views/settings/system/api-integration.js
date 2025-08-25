// 系统参数API集成辅助文件
// 这个文件提供了如何在现有页面中集成后端API的示例代码

import { systemParamApi } from '@/api/system-params'

// 集成示例：在您的现有组件中添加以下方法

export const apiIntegration = {
  // 1. 在 created 或 mounted 钩子中加载参数
  async loadSystemParameters() {
    try {
      // 加载基本设置参数
      const basicParams = await systemParamApi.getParameters({ category: 'BASIC' })
      if (basicParams.data && basicParams.data.content) {
        // 将参数值映射到表单
        basicParams.data.content.forEach(param => {
          switch (param.paramKey) {
            case 'company.name':
              this.basicForm.companyName = param.paramValue
              break
            case 'company.tax_number':
              this.basicForm.taxNumber = param.paramValue
              break
            case 'company.legal_person':
              this.basicForm.legalPerson = param.paramValue
              break
            case 'company.address':
              this.basicForm.address = param.paramValue
              break
            case 'company.phone':
              this.basicForm.phone = param.paramValue
              break
            // 添加其他字段映射
          }
        })
      }

      // 加载财务核算参数（凭证设置等）
      const financialParams = await systemParamApi.getParameters({ category: 'FINANCIAL' })
      if (financialParams.data && financialParams.data.content) {
        financialParams.data.content.forEach(param => {
          switch (param.paramKey) {
            case 'voucher.auto_number':
              this.voucherForm.autoNumber = param.paramValue === 'true'
              break
            case 'voucher.number_rule':
              this.voucherForm.numberRule = param.paramValue
              break
            // 添加其他凭证设置映射
          }
        })
      }

      // 加载系统管理参数（权限控制、备份设置等）
      const systemParams = await systemParamApi.getParameters({ category: 'SYSTEM' })
      // ... 处理系统参数
      
    } catch (error) {
      this.$message.error('加载系统参数失败：' + error.message)
    }
  },

  // 2. 保存基本设置
  async saveBasicSettings() {
    try {
      const updates = [
        { paramKey: 'company.name', paramValue: this.basicForm.companyName },
        { paramKey: 'company.tax_number', paramValue: this.basicForm.taxNumber },
        { paramKey: 'company.legal_person', paramValue: this.basicForm.legalPerson },
        { paramKey: 'company.address', paramValue: this.basicForm.address },
        { paramKey: 'company.phone', paramValue: this.basicForm.phone }
      ]

      const result = await systemParamApi.batchUpdate(updates)
      if (result.data && result.data.successCount > 0) {
        this.$message.success('基本设置保存成功')
      }
    } catch (error) {
      this.$message.error('保存失败：' + error.message)
    }
  },

  // 3. 保存凭证设置
  async saveVoucherSettings() {
    try {
      const updates = [
        { paramKey: 'voucher.auto_number', paramValue: String(this.voucherForm.autoNumber) },
        { paramKey: 'voucher.number_rule', paramValue: this.voucherForm.numberRule },
        { paramKey: 'voucher.approval_flow', paramValue: this.voucherForm.approvalFlow }
      ]

      const result = await systemParamApi.batchUpdate(updates)
      if (result.data && result.data.successCount > 0) {
        this.$message.success('凭证设置保存成功')
      }
    } catch (error) {
      this.$message.error('保存失败：' + error.message)
    }
  },

  // 4. 保存权限控制设置
  async saveSecuritySettings() {
    try {
      const updates = [
        { paramKey: 'password.min_length', paramValue: String(this.securityForm.passwordMinLength) },
        { paramKey: 'password.expire_days', paramValue: String(this.securityForm.passwordExpireDays) },
        { paramKey: 'login.max_attempts', paramValue: String(this.securityForm.maxLoginAttempts) },
        { paramKey: 'session.timeout', paramValue: String(this.securityForm.sessionTimeout) }
      ]

      const result = await systemParamApi.batchUpdate(updates)
      if (result.data && result.data.successCount > 0) {
        this.$message.success('权限控制设置保存成功')
      }
    } catch (error) {
      this.$message.error('保存失败：' + error.message)
    }
  },

  // 5. 保存备份设置
  async saveBackupSettings() {
    try {
      const updates = [
        { paramKey: 'backup.auto_enable', paramValue: String(this.backupForm.autoBackup) },
        { paramKey: 'backup.schedule', paramValue: this.backupForm.backupTime },
        { paramKey: 'backup.retention_days', paramValue: String(this.backupForm.retentionDays) }
      ]

      const result = await systemParamApi.batchUpdate(updates)
      if (result.data && result.data.successCount > 0) {
        this.$message.success('备份设置保存成功')
      }
    } catch (error) {
      this.$message.error('保存失败：' + error.message)
    }
  }
}

// 使用方法：
// 1. 在您的组件中导入 systemParamApi
// 2. 在 created() 或 mounted() 中调用 loadSystemParameters
// 3. 修改保存方法，调用对应的API
// 4. 保持您现有的页面样式和布局不变