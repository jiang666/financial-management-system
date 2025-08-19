// 模拟数据
export const mockAccounts = [
  { id: '1001', code: '1001', name: '库存现金', type: '资产', level: 1, parentId: null, balance: 50000, direction: '借', status: '启用' },
  { id: '1002', code: '1002', name: '银行存款', type: '资产', level: 1, parentId: null, balance: 2000000, direction: '借', status: '启用' },
  { id: '100201', code: '100201', name: '工商银行', type: '资产', level: 2, parentId: '1002', balance: 1500000, direction: '借', status: '启用' },
  { id: '100202', code: '100202', name: '建设银行', type: '资产', level: 2, parentId: '1002', balance: 500000, direction: '借', status: '启用' },
  { id: '1122', code: '1122', name: '应收账款', type: '资产', level: 1, parentId: null, balance: 800000, direction: '借', status: '启用' },
  { id: '1405', code: '1405', name: '库存商品', type: '资产', level: 1, parentId: null, balance: 1200000, direction: '借', status: '启用' },
  { id: '2202', code: '2202', name: '应付账款', type: '负债', level: 1, parentId: null, balance: 600000, direction: '贷', status: '启用' },
  { id: '4001', code: '4001', name: '实收资本', type: '权益', level: 1, parentId: null, balance: 5000000, direction: '贷', status: '启用' },
  { id: '6001', code: '6001', name: '主营业务收入', type: '收入', level: 1, parentId: null, balance: 3000000, direction: '贷', status: '启用' },
  { id: '6401', code: '6401', name: '主营业务成本', type: '成本', level: 1, parentId: null, balance: 1800000, direction: '借', status: '启用' }
]

export const mockAuxiliaryItems = [
  { id: 1, type: '部门', code: 'D001', name: '财务部', status: '启用', createTime: '2025-01-01' },
  { id: 2, type: '部门', code: 'D002', name: '销售部', status: '启用', createTime: '2025-01-01' },
  { id: 3, type: '部门', code: 'D003', name: '采购部', status: '启用', createTime: '2025-01-01' },
  { id: 4, type: '客户', code: 'C001', name: 'ABC公司', status: '启用', createTime: '2025-01-02' },
  { id: 5, type: '客户', code: 'C002', name: 'XYZ公司', status: '启用', createTime: '2025-01-02' },
  { id: 6, type: '供应商', code: 'S001', name: '原材料供应商A', status: '启用', createTime: '2025-01-03' },
  { id: 7, type: '供应商', code: 'S002', name: '设备供应商B', status: '启用', createTime: '2025-01-03' },
  { id: 8, type: '项目', code: 'P001', name: '2025年新产品开发', status: '启用', createTime: '2025-01-05' },
  { id: 9, type: '项目', code: 'P002', name: '市场推广项目', status: '启用', createTime: '2025-01-06' }
]

export const mockCurrencies = [
  { id: 1, code: 'CNY', name: '人民币', symbol: '¥', exchangeRate: 1.0000, isBase: true, status: '启用' },
  { id: 2, code: 'USD', name: '美元', symbol: '$', exchangeRate: 7.2365, isBase: false, status: '启用' },
  { id: 3, code: 'EUR', name: '欧元', symbol: '€', exchangeRate: 7.8523, isBase: false, status: '启用' },
  { id: 4, code: 'JPY', name: '日元', symbol: '¥', exchangeRate: 0.0486, isBase: false, status: '启用' },
  { id: 5, code: 'GBP', name: '英镑', symbol: '£', exchangeRate: 9.1234, isBase: false, status: '启用' }
]

export const mockOrganizations = [
  { id: '1', name: '总公司', type: '公司', parentId: null, manager: '张总', employeeCount: 500, status: '启用' },
  { id: '2', name: '财务部', type: '部门', parentId: '1', manager: '李主任', employeeCount: 20, status: '启用' },
  { id: '3', name: '销售部', type: '部门', parentId: '1', manager: '王经理', employeeCount: 50, status: '启用' },
  { id: '4', name: '采购部', type: '部门', parentId: '1', manager: '赵经理', employeeCount: 15, status: '启用' },
  { id: '5', name: '人力资源部', type: '部门', parentId: '1', manager: '刘主管', employeeCount: 10, status: '启用' },
  { id: '6', name: '会计组', type: '小组', parentId: '2', manager: '陈组长', employeeCount: 8, status: '启用' },
  { id: '7', name: '出纳组', type: '小组', parentId: '2', manager: '周组长', employeeCount: 5, status: '启用' }
]

export const mockUsers = [
  { id: 1, username: 'admin', realName: '系统管理员', role: '超级管理员', department: '信息部', email: 'admin@example.com', phone: '13800138000', status: '启用', lastLogin: '2025-08-15 09:00:00' },
  { id: 2, username: 'finance01', realName: '张会计', role: '财务主管', department: '财务部', email: 'zhang@example.com', phone: '13800138001', status: '启用', lastLogin: '2025-08-15 08:30:00' },
  { id: 3, username: 'finance02', realName: '李会计', role: '会计', department: '财务部', email: 'li@example.com', phone: '13800138002', status: '启用', lastLogin: '2025-08-15 08:45:00' },
  { id: 4, username: 'cashier01', realName: '王出纳', role: '出纳', department: '财务部', email: 'wang@example.com', phone: '13800138003', status: '启用', lastLogin: '2025-08-15 09:15:00' },
  { id: 5, username: 'audit01', realName: '赵审计', role: '审计员', department: '审计部', email: 'zhao@example.com', phone: '13800138004', status: '启用', lastLogin: '2025-08-14 18:00:00' }
]

export const mockRoles = [
  { id: 1, name: '超级管理员', code: 'admin', description: '拥有所有权限', userCount: 1, status: '启用' },
  { id: 2, name: '财务主管', code: 'finance_manager', description: '财务部门管理权限', userCount: 1, status: '启用' },
  { id: 3, name: '会计', code: 'accountant', description: '凭证录入、查询权限', userCount: 2, status: '启用' },
  { id: 4, name: '出纳', code: 'cashier', description: '现金银行管理权限', userCount: 1, status: '启用' },
  { id: 5, name: '审计员', code: 'auditor', description: '查询审计权限', userCount: 1, status: '启用' }
]