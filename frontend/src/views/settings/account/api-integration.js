// 科目管理页面 - API集成示例
// 展示如何对接后端科目管理API

import { accountsAPI } from '@/api'
import { ElMessage } from 'element-plus'

// ========== 科目管理API集成 ==========

// 加载科目列表
export const loadAccounts = async (params = {}) => {
  try {
    const response = await accountsAPI.getAccounts({
      page: params.page || 0,
      size: params.size || 20,
      code: params.code,
      name: params.name,
      accountType: params.accountType,
      level: params.level,
      status: params.status
    })
    return {
      data: response.content || [],
      total: response.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('加载科目列表失败')
    throw error
  }
}

// 获取科目树
export const loadAccountTree = async () => {
  try {
    const response = await accountsAPI.getAccountsTree()
    return response || []
  } catch (error) {
    ElMessage.error('加载科目树失败')
    throw error
  }
}

// 创建科目
export const createAccount = async (accountData) => {
  try {
    await accountsAPI.createAccount({
      code: accountData.code,
      name: accountData.name,
      englishName: accountData.englishName,
      accountType: accountData.type,
      balanceDirection: accountData.direction === '借' ? 'DEBIT' : 'CREDIT',
      parentId: accountData.parentId,
      memoCode: accountData.memoCode,
      currencyCode: 'CNY',
      isQuantity: false,
      auxiliaryTypes: accountData.auxiliary || [],
      status: 1
    })
    ElMessage.success('科目创建成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '创建科目失败')
    throw error
  }
}

// 更新科目
export const updateAccount = async (accountId, accountData) => {
  try {
    await accountsAPI.updateAccount(accountId, {
      name: accountData.name,
      englishName: accountData.englishName,
      memoCode: accountData.memoCode,
      currencyCode: 'CNY',
      isQuantity: false,
      auxiliaryTypes: accountData.auxiliary || [],
      status: accountData.status === '启用' ? 1 : 0
    })
    ElMessage.success('科目更新成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '更新科目失败')
    throw error
  }
}

// 删除科目
export const deleteAccount = async (accountId) => {
  try {
    await accountsAPI.deleteAccount(accountId)
    ElMessage.success('科目删除成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '删除科目失败')
    throw error
  }
}

// 搜索科目
export const searchAccounts = async (keyword) => {
  try {
    const response = await accountsAPI.searchAccounts(keyword)
    return response || []
  } catch (error) {
    ElMessage.error('搜索科目失败')
    throw error
  }
}

// 生成科目编码
export const generateAccountCode = async (parentCode) => {
  try {
    const response = await accountsAPI.generateAccountCode(parentCode)
    return response.code
  } catch (error) {
    ElMessage.error('生成科目编码失败')
    throw error
  }
}

// ========== 数据转换工具 ==========

// 将后端科目数据转换为前端树形格式
export const transformAccountToTree = (accounts) => {
  const accountMap = {}
  const tree = []
  
  // 第一步：创建映射
  accounts.forEach(account => {
    accountMap[account.id] = {
      id: account.id,
      code: account.code,
      name: account.name,
      type: getAccountTypeName(account.accountType),
      direction: account.balanceDirection === 'DEBIT' ? '借' : '贷',
      balance: account.beginningBalance || 0,
      status: account.status === 1 ? '启用' : '停用',
      level: account.level,
      parentId: account.parentId,
      auxiliary: account.auxiliaryType ? account.auxiliaryType.split(',') : [],
      children: []
    }
  })
  
  // 第二步：构建树
  accounts.forEach(account => {
    const node = accountMap[account.id]
    if (account.parentId && accountMap[account.parentId]) {
      accountMap[account.parentId].children.push(node)
    } else {
      tree.push(node)
    }
  })
  
  return tree
}

// 科目类型映射
const getAccountTypeName = (type) => {
  const typeMap = {
    'ASSET': '资产',
    'LIABILITY': '负债',
    'EQUITY': '权益',
    'INCOME': '收入',
    'COST': '成本',
    'EXPENSE': '费用'
  }
  return typeMap[type] || type
}

// ========== 使用示例 ==========
/*
// 在组件中使用:
import { loadAccountTree, createAccount, updateAccount } from './api-integration'

// 加载科目树
const loadData = async () => {
  loading.value = true
  try {
    const accounts = await loadAccountTree()
    tableData.value = transformAccountToTree(accounts)
  } finally {
    loading.value = false
  }
}

// 保存科目
const handleSave = async () => {
  const valid = await accountFormRef.value.validate()
  if (!valid) return
  
  try {
    if (accountForm.value.id) {
      await updateAccount(accountForm.value.id, accountForm.value)
    } else {
      // 如果需要自动生成编码
      if (!accountForm.value.code && accountForm.value.parentId) {
        const parentAccount = findAccountById(accountForm.value.parentId)
        accountForm.value.code = await generateAccountCode(parentAccount.code)
      }
      await createAccount(accountForm.value)
    }
    accountDialog.value = false
    await loadData()
  } catch (error) {
    console.error('保存科目失败:', error)
  }
}
*/