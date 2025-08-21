// 根据用户权限生成菜单
export function generateMenus(resources) {
  if (!resources || resources.length === 0) {
    return []
  }

  // 路由路径映射
  const routePathMap = {
    // 基础设置
    '/system/accounts': '/settings/account',
    '/system/auxiliary': '/settings/auxiliary',
    '/system/balance': '/settings/balance',
    '/system/currency': '/settings/currency',
    '/system/parameters': '/settings/system',
    '/system/organization': '/settings/organization',
    '/system/users': '/settings/permission',
    
    // 日常核算
    '/voucher/entry': '/voucher/entry',
    '/voucher/review': '/voucher/review',
    '/voucher/posting': '/voucher/posting',
    '/voucher/query': '/voucher/query',
    '/voucher/books': '/voucher/books',
    '/voucher/closing': '/voucher/closing',
    
    // 往来管理
    '/receivable/management': '/accounts/receivable',
    '/receivable/aging': '/accounts/aging',
    '/receivable/reconciliation': '/accounts/customer-reconcile',
    '/payable/management': '/accounts/payable',
    '/payable/reconciliation': '/accounts/supplier-reconcile',
    '/credit/management': '/accounts/credit',
    '/baddebt/provision': '/accounts/baddebt',
    
    // 资金管理
    '/cash/management': '/cash/management',
    '/cash/bank': '/cash/bank',
    '/cash/reconciliation': '/cash/reconciliation',
    '/cash/check': '/cash/check',
    '/cash/report': '/cash/report',
    
    // 报表管理
    '/reports/financial': '/reports/financial',
    '/reports/analysis': '/reports/analysis',
    '/reports/cost': '/reports/cost',
    '/reports/budget': '/reports/budget',
    '/reports/performance': '/reports/performance'
  }

  // 图标映射
  const iconMap = {
    '基础设置': 'Setting',
    '日常核算': 'Document',
    '往来管理': 'User',
    '资金管理': 'Wallet',
    '报表管理': 'DataAnalysis',
    // 子菜单图标
    '科目管理': 'List',
    '辅助核算': 'Coin',
    '期初余额': 'Money',
    '币种汇率': 'CreditCard',
    '系统参数': 'Tools',
    '组织架构': 'OfficeBuilding',
    '用户权限': 'UserFilled',
    '凭证录入': 'Edit',
    '凭证审核': 'Check',
    '凭证记账': 'Notebook',
    '凭证查询': 'Search',
    '账簿查询': 'Reading',
    '期末结账': 'Calendar',
    '应收管理': 'Coin',
    '账龄分析': 'DataLine',
    '客户对账': 'Checked',
    '应付管理': 'Money',
    '供应商对账': 'CircleCheck',
    '信用管理': 'CreditCard',
    '坏账准备': 'Warning',
    '资金管理': 'Wallet',
    '银行存款': 'BankCard',
    '资金对账': 'DocumentChecked',
    '支票管理': 'Ticket',
    '资金报表': 'DataAnalysis',
    '财务报表': 'DataBoard',
    '财务分析': 'TrendCharts',
    '成本核算': 'PieChart',
    '预算管理': 'SetUp',
    '绩效考核': 'Trophy'
  }

  // 转换资源为菜单格式
  const convertToMenu = (resource) => {
    const menu = {
      id: resource.id,
      name: resource.resourceName,
      path: routePathMap[resource.url] || resource.url,
      icon: iconMap[resource.resourceName] || 'Document',
      children: []
    }

    // 处理子菜单
    if (resource.children && resource.children.length > 0) {
      // 过滤出菜单类型的资源（不包括按钮和接口权限）
      const menuChildren = resource.children.filter(child => 
        child.resourceType === 'submenu' || 
        child.resourceType === 'menu-grouping'
      )
      
      if (menuChildren.length > 0) {
        menu.children = menuChildren
          .map(child => convertToMenu(child))
          .sort((a, b) => a.sortOrder - b.sortOrder)
      }
    }

    // 如果没有子菜单，移除children属性
    if (menu.children.length === 0) {
      delete menu.children
    }

    return menu
  }

  // 转换资源列表为菜单
  if (Array.isArray(resources)) {
    return resources
      .filter(resource => 
        resource.resourceType === 'menu-grouping' || 
        resource.resourceType === 'submenu'
      )
      .map(resource => convertToMenu(resource))
      .sort((a, b) => a.sortOrder - b.sortOrder)
  } else {
    // 单个资源对象
    return [convertToMenu(resources)]
  }
}

// 获取所有菜单路径（用于权限验证）
export function getAllMenuPaths(menus) {
  const paths = []
  
  const extractPaths = (menu) => {
    if (menu.path) {
      paths.push(menu.path)
    }
    if (menu.children) {
      menu.children.forEach(child => extractPaths(child))
    }
  }
  
  menus.forEach(menu => extractPaths(menu))
  return paths
}