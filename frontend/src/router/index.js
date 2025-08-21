import { createRouter, createWebHistory } from 'vue-router'
import store from '@/store'
import { ElMessage } from 'element-plus'

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
  },
  {
    path: '/test',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/test/permission',
    meta: { title: '系统测试', icon: 'Tools' },
    children: [
      {
        path: 'permission',
        name: 'PermissionTest',
        component: () => import('@/views/test/PermissionTest.vue'),
        meta: { title: '权限测试', icon: 'Lock' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 白名单页面，不需要登录即可访问
const whiteList = ['/login']

router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 财务管理系统` : '财务管理系统'
  
  // 获取token
  const hasToken = store.getters.token
  
  if (hasToken) {
    if (to.path === '/login') {
      // 如果已登录，访问登录页面则重定向到首页
      next('/dashboard')
    } else {
      // 检查是否已有用户信息
      const hasUserInfo = store.getters.user.id
      if (hasUserInfo) {
        next()
      } else {
        try {
          // 尝试初始化用户信息
          await store.dispatch('initUserInfo')
          next()
        } catch (error) {
          console.error('Failed to get user info:', error)
          ElMessage.error('获取用户信息失败，请重新登录')
          await store.dispatch('logout')
          next('/login')
        }
      }
    }
  } else {
    // 没有token
    if (whiteList.includes(to.path)) {
      // 在白名单中，直接放行
      next()
    } else {
      // 不在白名单中，重定向到登录页
      next('/login')
    }
  }
})

export default router