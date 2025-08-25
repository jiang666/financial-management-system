# 系统参数页面API集成指南

## 概述
这份指南帮助您在**不修改现有页面样式和布局**的情况下，集成后端系统参数API。

## 集成步骤

### 1. 导入API服务
在您的 `system/index.vue` 文件中添加导入：

```javascript
import { systemParamApi } from '@/api/system-params'
```

### 2. 在组件加载时获取参数
在 `created()` 或 `mounted()` 钩子中添加：

```javascript
created() {
  this.loadSystemParameters()
},

methods: {
  async loadSystemParameters() {
    try {
      // 加载所有参数
      const res = await systemParamApi.getParameters()
      if (res.data && res.data.content) {
        // 处理参数，映射到您的表单数据
        res.data.content.forEach(param => {
          // 根据 param.paramKey 映射到对应的表单字段
          this.mapParameterToForm(param)
        })
      }
    } catch (error) {
      console.error('加载参数失败', error)
    }
  },
  
  mapParameterToForm(param) {
    // 根据参数键映射到表单
    switch (param.paramKey) {
      case 'company.name':
        this.basicForm.companyName = param.paramValue
        break
      case 'company.tax_number':
        this.basicForm.taxNumber = param.paramValue
        break
      // ... 添加其他映射
    }
  }
}
```

### 3. 修改保存方法
修改您现有的保存方法，使用API批量更新：

```javascript
async saveBasicSettings() {
  try {
    // 准备要更新的参数
    const updates = [
      { paramKey: 'company.name', paramValue: this.basicForm.companyName || '' },
      { paramKey: 'company.tax_number', paramValue: this.basicForm.taxNumber || '' },
      { paramKey: 'company.legal_person', paramValue: this.basicForm.legalPerson || '' },
      { paramKey: 'company.address', paramValue: this.basicForm.address || '' },
      { paramKey: 'company.phone', paramValue: this.basicForm.phone || '' }
    ]
    
    // 调用批量更新API
    const result = await systemParamApi.batchUpdate(updates)
    
    if (result.data && result.data.successCount > 0) {
      this.$message.success(`成功更新 ${result.data.successCount} 个参数`)
    }
  } catch (error) {
    this.$message.error('保存失败：' + error.message)
  }
}
```

## 参数键对照表

### 基本设置
| 表单字段 | 参数键 | 参数类型 |
|---------|--------|----------|
| 公司名称 | company.name | STRING |
| 纳税人识别号 | company.tax_number | STRING |
| 法人代表 | company.legal_person | STRING |
| 注册地址 | company.address | STRING |
| 联系电话 | company.phone | STRING |

### 会计期间设置
| 表单字段 | 参数键 | 参数类型 |
|---------|--------|----------|
| 会计准则 | accounting.standard | ENUM |
| 记账本位币 | accounting.currency | STRING |
| 账套启用日期 | accounting.start_date | DATE |
| 会计年度起始月 | accounting.fiscal_year_start | INTEGER |

### 凭证设置
| 表单字段 | 参数键 | 参数类型 |
|---------|--------|----------|
| 凭证自动编号 | voucher.auto_number | BOOLEAN |
| 凭证编号规则 | voucher.number_rule | ENUM |
| 审核流程 | voucher.approval_flow | ENUM |

### 权限控制
| 表单字段 | 参数键 | 参数类型 |
|---------|--------|----------|
| 密码最小长度 | password.min_length | INTEGER |
| 密码有效期 | password.expire_days | INTEGER |
| 最大登录尝试 | login.max_attempts | INTEGER |
| 会话超时 | session.timeout | INTEGER |

### 数据备份设置
| 表单字段 | 参数键 | 参数类型 |
|---------|--------|----------|
| 启用自动备份 | backup.auto_enable | BOOLEAN |
| 备份时间 | backup.schedule | TIME |
| 备份保留天数 | backup.retention_days | INTEGER |

## 注意事项

1. **类型转换**：
   - BOOLEAN类型：需要将字符串 "true"/"false" 转换为布尔值
   - INTEGER类型：需要将字符串转换为数字
   - DATE/TIME类型：确保格式正确

2. **错误处理**：
   - 使用 try-catch 捕获API调用错误
   - 显示友好的错误提示

3. **保持现有功能**：
   - 不要删除现有的验证逻辑
   - 保留现有的UI交互效果
   - 维持原有的数据结构

## 完整示例
查看 `api-integration.js` 文件获取完整的集成示例代码。