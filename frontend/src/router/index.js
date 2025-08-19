import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/dashboard',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard/index',
    children: [
      {
        path: 'index',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      }
    ]
  },
  {
    path: '/settings',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/settings/account',
    meta: { title: '基础设置', icon: 'Setting' },
    children: [
      {
        path: 'account',
        name: 'AccountManagement',
        component: () => import('@/views/settings/account/index.vue'),
        meta: { title: '科目管理', icon: 'Notebook' }
      },
      {
        path: 'auxiliary',
        name: 'AuxiliaryAccounting',
        component: () => import('@/views/settings/auxiliary/index.vue'),
        meta: { title: '辅助核算', icon: 'DataBoard' }
      },
      {
        path: 'balance',
        name: 'InitialBalance',
        component: () => import('@/views/settings/balance/index.vue'),
        meta: { title: '期初余额', icon: 'Coin' }
      },
      {
        path: 'currency',
        name: 'CurrencyExchange',
        component: () => import('@/views/settings/currency/index.vue'),
        meta: { title: '币种汇率', icon: 'Money' }
      },
      {
        path: 'system',
        name: 'SystemParams',
        component: () => import('@/views/settings/system/index.vue'),
        meta: { title: '系统参数', icon: 'Tools' }
      },
      {
        path: 'organization',
        name: 'Organization',
        component: () => import('@/views/settings/organization/index.vue'),
        meta: { title: '组织架构', icon: 'OfficeBuilding' }
      },
      {
        path: 'permission',
        name: 'Permission',
        component: () => import('@/views/settings/permission/index.vue'),
        meta: { title: '用户权限', icon: 'User' }
      }
    ]
  },
  {
    path: '/voucher',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/voucher/management',
    meta: { title: '日常核算', icon: 'DocumentCopy' },
    children: [
      {
        path: 'management',
        name: 'VoucherManagement',
        component: () => import('@/views/voucher/management/index.vue'),
        meta: { title: '凭证管理', icon: 'DocumentAdd' }
      },
      {
        path: 'books',
        name: 'BookQuery',
        component: () => import('@/views/voucher/books/index.vue'),
        meta: { title: '账簿查询', icon: 'Notebook' }
      },
      {
        path: 'closing',
        name: 'PeriodClosing',
        component: () => import('@/views/voucher/closing/index.vue'),
        meta: { title: '期末结账', icon: 'Calendar' }
      }
    ]
  },
  {
    path: '/cash',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/cash/management',
    meta: { title: '出纳管理', icon: 'Wallet' },
    children: [
      {
        path: 'management',
        name: 'CashManagement',
        component: () => import('@/views/cash/management/index.vue'),
        meta: { title: '现金管理', icon: 'Money' }
      },
      {
        path: 'bank',
        name: 'BankAccount',
        component: () => import('@/views/cash/bank/index.vue'),
        meta: { title: '银行账户', icon: 'CreditCard' }
      },
      {
        path: 'reconciliation',
        name: 'BankReconciliation',
        component: () => import('@/views/cash/reconciliation/index.vue'),
        meta: { title: '银行对账', icon: 'DocumentChecked' }
      },
      {
        path: 'check',
        name: 'CheckManagement',
        component: () => import('@/views/cash/check/index.vue'),
        meta: { title: '支票管理', icon: 'Ticket' }
      },
      {
        path: 'report',
        name: 'CashReport',
        component: () => import('@/views/cash/report/index.vue'),
        meta: { title: '资金日报', icon: 'DataLine' }
      }
    ]
  },
  {
    path: '/accounts',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/accounts/receivable',
    meta: { title: '往来账款', icon: 'Files' },
    children: [
      {
        path: 'receivable',
        name: 'AccountsReceivable',
        component: () => import('@/views/accounts/receivable/index.vue'),
        meta: { title: '应收管理', icon: 'Sell' }
      },
      {
        path: 'payable',
        name: 'AccountsPayable',
        component: () => import('@/views/accounts/payable/index.vue'),
        meta: { title: '应付管理', icon: 'ShoppingCart' }
      },
      {
        path: 'aging',
        name: 'AgingAnalysis',
        component: () => import('@/views/accounts/aging/index.vue'),
        meta: { title: '账龄分析', icon: 'Timer' }
      },
      {
        path: 'baddebt',
        name: 'BadDebtManagement',
        component: () => import('@/views/accounts/baddebt/index.vue'),
        meta: { title: '坏账管理', icon: 'Warning' }
      },
      {
        path: 'customer-reconcile',
        name: 'CustomerReconcile',
        component: () => import('@/views/accounts/customer-reconcile/index.vue'),
        meta: { title: '客户对账', icon: 'UserFilled' }
      },
      {
        path: 'supplier-reconcile',
        name: 'SupplierReconcile',
        component: () => import('@/views/accounts/supplier-reconcile/index.vue'),
        meta: { title: '供应商对账', icon: 'OfficeBuilding' }
      },
      {
        path: 'credit',
        name: 'CreditManagement',
        component: () => import('@/views/accounts/credit/index.vue'),
        meta: { title: '信用管理', icon: 'Medal' }
      }
    ]
  },
  {
    path: '/reports',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/reports/financial',
    meta: { title: '报表分析', icon: 'DataAnalysis' },
    children: [
      {
        path: 'financial',
        name: 'FinancialReports',
        component: () => import('@/views/reports/financial/index.vue'),
        meta: { title: '财务报表', icon: 'Document' }
      },
      {
        path: 'analysis',
        name: 'BusinessAnalysis',
        component: () => import('@/views/reports/analysis/index.vue'),
        meta: { title: '经营分析', icon: 'TrendCharts' }
      },
      {
        path: 'budget',
        name: 'BudgetManagement',
        component: () => import('@/views/reports/budget/index.vue'),
        meta: { title: '预算管理', icon: 'Money' }
      },
      {
        path: 'cost',
        name: 'CostAnalysis',
        component: () => import('@/views/reports/cost/index.vue'),
        meta: { title: '成本分析', icon: 'PieChart' }
      },
      {
        path: 'performance',
        name: 'PerformanceEvaluation',
        component: () => import('@/views/reports/performance/index.vue'),
        meta: { title: '绩效考核', icon: 'Trophy' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 财务管理系统` : '财务管理系统'
  next()
})

export default router