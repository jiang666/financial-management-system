<template>
  <el-container class="main-layout">
    <el-aside :width="sidebarWidth" class="sidebar">
      <div class="logo-container">
        <img src="/vite.svg" alt="logo" class="logo" />
        <span v-show="!collapsed" class="company-name">财务管理系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        :collapse-transition="false"
        :unique-opened="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/dashboard/index">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        
        <el-sub-menu index="/settings">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>基础设置</span>
          </template>
          <el-menu-item index="/settings/account">
            <el-icon><Notebook /></el-icon>
            <span>科目管理</span>
          </el-menu-item>
          <el-menu-item index="/settings/auxiliary">
            <el-icon><DataBoard /></el-icon>
            <span>辅助核算</span>
          </el-menu-item>
          <el-menu-item index="/settings/balance">
            <el-icon><Coin /></el-icon>
            <span>期初余额</span>
          </el-menu-item>
          <el-menu-item index="/settings/currency">
            <el-icon><Money /></el-icon>
            <span>币种汇率</span>
          </el-menu-item>
          <el-menu-item index="/settings/system">
            <el-icon><Tools /></el-icon>
            <span>系统参数</span>
          </el-menu-item>
          <el-menu-item index="/settings/organization">
            <el-icon><OfficeBuilding /></el-icon>
            <span>组织架构</span>
          </el-menu-item>
          <el-menu-item index="/settings/permission">
            <el-icon><User /></el-icon>
            <span>用户权限</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/voucher">
          <template #title>
            <el-icon><DocumentCopy /></el-icon>
            <span>日常核算</span>
          </template>
          <el-menu-item index="/voucher/management">
            <el-icon><DocumentAdd /></el-icon>
            <span>凭证管理</span>
          </el-menu-item>
          <el-menu-item index="/voucher/books">
            <el-icon><Notebook /></el-icon>
            <span>账簿查询</span>
          </el-menu-item>
          <el-menu-item index="/voucher/closing">
            <el-icon><Calendar /></el-icon>
            <span>期末结账</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/cash">
          <template #title>
            <el-icon><Wallet /></el-icon>
            <span>出纳管理</span>
          </template>
          <el-menu-item index="/cash/management">
            <el-icon><Money /></el-icon>
            <span>现金管理</span>
          </el-menu-item>
          <el-menu-item index="/cash/bank">
            <el-icon><CreditCard /></el-icon>
            <span>银行账户</span>
          </el-menu-item>
          <el-menu-item index="/cash/reconciliation">
            <el-icon><DocumentChecked /></el-icon>
            <span>银行对账</span>
          </el-menu-item>
          <el-menu-item index="/cash/check">
            <el-icon><Ticket /></el-icon>
            <span>支票管理</span>
          </el-menu-item>
          <el-menu-item index="/cash/report">
            <el-icon><DataLine /></el-icon>
            <span>资金日报</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/accounts">
          <template #title>
            <el-icon><Files /></el-icon>
            <span>往来账款</span>
          </template>
          <el-menu-item index="/accounts/receivable">
            <el-icon><Sell /></el-icon>
            <span>应收管理</span>
          </el-menu-item>
          <el-menu-item index="/accounts/payable">
            <el-icon><ShoppingCart /></el-icon>
            <span>应付管理</span>
          </el-menu-item>
          <el-menu-item index="/accounts/aging">
            <el-icon><Timer /></el-icon>
            <span>账龄分析</span>
          </el-menu-item>
          <el-menu-item index="/accounts/baddebt">
            <el-icon><Warning /></el-icon>
            <span>坏账管理</span>
          </el-menu-item>
          <el-menu-item index="/accounts/customer-reconcile">
            <el-icon><UserFilled /></el-icon>
            <span>客户对账</span>
          </el-menu-item>
          <el-menu-item index="/accounts/supplier-reconcile">
            <el-icon><OfficeBuilding /></el-icon>
            <span>供应商对账</span>
          </el-menu-item>
          <el-menu-item index="/accounts/credit">
            <el-icon><Medal /></el-icon>
            <span>信用管理</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/reports">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>报表分析</span>
          </template>
          <el-menu-item index="/reports/financial">
            <el-icon><Document /></el-icon>
            <span>财务报表</span>
          </el-menu-item>
          <el-menu-item index="/reports/analysis">
            <el-icon><TrendCharts /></el-icon>
            <span>经营分析</span>
          </el-menu-item>
          <el-menu-item index="/reports/budget">
            <el-icon><Money /></el-icon>
            <span>预算管理</span>
          </el-menu-item>
          <el-menu-item index="/reports/cost">
            <el-icon><PieChart /></el-icon>
            <span>成本分析</span>
          </el-menu-item>
          <el-menu-item index="/reports/performance">
            <el-icon><Trophy /></el-icon>
            <span>绩效考核</span>
          </el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="/test">
          <template #title>
            <el-icon><Tools /></el-icon>
            <span>系统测试</span>
          </template>
          <el-menu-item index="/test/permission">
            <el-icon><Lock /></el-icon>
            <span>权限测试</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleSidebar">
            <component :is="collapsed ? 'Expand' : 'Fold'" />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta.title">
              {{ currentRoute.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              <span>{{ realName || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item>
                  <el-icon><Setting /></el-icon>
                  系统设置
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const store = useStore()

const collapsed = computed(() => store.state.sidebar.collapsed)
const realName = computed(() => store.getters.realName || store.getters.username)
const currentRoute = computed(() => route)
const activeMenu = computed(() => route.path)
const sidebarWidth = computed(() => collapsed.value ? '64px' : '240px')

const toggleSidebar = () => {
  store.commit('TOGGLE_SIDEBAR')
}

const handleLogout = async () => {
  try {
    await store.dispatch('logout')
    ElMessage.success('退出成功')
    router.push('/login')
  } catch (error) {
    console.error('Logout error:', error)
    ElMessage.error('退出失败')
  }
}

// 修复菜单展开/收起时的布局问题
onMounted(() => {
  // 强制修复布局的核心函数
  const forceFixLayout = () => {
    const mainLayout = document.querySelector('.main-layout')
    const aside = document.querySelector('.el-aside')
    const container = document.querySelector('.main-layout > .el-container')
    const menu = document.querySelector('.el-menu')
    
    if (mainLayout) {
      // 强制flex布局
      mainLayout.style.display = 'flex'
      mainLayout.style.flexDirection = 'row'
      mainLayout.style.gap = '0'
    }
    
    if (aside) {
      // 固定侧边栏宽度
      const isCollapsed = collapsed.value
      const targetWidth = isCollapsed ? '64px' : '240px'
      
      aside.style.width = targetWidth
      aside.style.minWidth = targetWidth
      aside.style.maxWidth = targetWidth
      aside.style.flex = `0 0 ${targetWidth}`
      aside.style.margin = '0'
      aside.style.padding = '0'
      aside.style.border = 'none'
    }
    
    if (container) {
      // 确保主容器占满剩余空间
      container.style.flex = '1 1 auto'
      container.style.margin = '0'
      container.style.padding = '0'
      container.style.minWidth = '0'
    }
    
    if (menu) {
      // 防止菜单溢出
      menu.style.overflowX = 'hidden'
      menu.style.border = 'none'
      menu.style.width = '100%'
    }
  }
  
  // 监听所有可能导致布局变化的事件
  const setupLayoutFix = () => {
    // 使用 MutationObserver 监听 DOM 变化
    const observer = new MutationObserver(() => {
      requestAnimationFrame(forceFixLayout)
    })
    
    // 观察整个菜单容器
    const menuElement = document.querySelector('.el-menu')
    if (menuElement) {
      observer.observe(menuElement, {
        childList: true,
        subtree: true,
        attributes: true,
        attributeFilter: ['class', 'style', 'aria-expanded']
      })
    }
    
    // 监听点击事件
    document.addEventListener('click', (e) => {
      if (e.target.closest('.el-sub-menu__title') || 
          e.target.closest('.el-menu-item')) {
        // 立即修复
        forceFixLayout()
        // 动画过程中多次修复
        const timings = [0, 50, 100, 150, 200, 250, 300, 350]
        timings.forEach(delay => {
          setTimeout(forceFixLayout, delay)
        })
      }
    })
    
    // 监听窗口调整
    window.addEventListener('resize', forceFixLayout)
    
    // 监听过渡事件
    document.addEventListener('transitionend', (e) => {
      if (e.target.closest('.el-menu') || 
          e.target.closest('.el-aside')) {
        forceFixLayout()
      }
    })
  }
  
  // 初始化
  forceFixLayout()
  setupLayoutFix()
  
  // 确保初始状态正确
  nextTick(() => {
    forceFixLayout()
    setTimeout(forceFixLayout, 100)
    setTimeout(forceFixLayout, 300)
  })
})
</script>

<style scoped lang="scss">
.main-layout {
  height: 100vh;
  width: 100vw;
  display: flex !important;
  flex-direction: row !important;
  overflow: hidden;
  
  // 修复 Element Plus 的默认样式
  ::v-deep .el-aside {
    border-right: none !important;
    box-shadow: none !important;
    margin: 0 !important;
    padding: 0 !important;
    flex: 0 0 240px !important;
    width: 240px !important;
    min-width: 240px !important;
    max-width: 240px !important;
    transition: all 0.3s !important;
    
    &.is-collapse {
      flex: 0 0 64px !important;
      width: 64px !important;
      min-width: 64px !important;
      max-width: 64px !important;
    }
  }
  
  ::v-deep > .el-container {
    flex: 1 1 auto !important;
    margin: 0 !important;
    padding: 0 !important;
    min-width: 0 !important;
    overflow: hidden;
    
    .el-main {
      padding: 0;
      margin: 0 !important;
    }
  }
  
  .sidebar {
    background-color: #304156;
    height: 100vh;
    border: none !important;
    margin: 0 !important;
    padding: 0 !important;
    overflow: hidden !important;
    position: relative !important;
    z-index: 1000;
    display: flex;
    flex-direction: column;
    
    .logo-container {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #2b2f3a;
      
      .logo {
        width: 32px;
        height: 32px;
      }
      
      .company-name {
        margin-left: 10px;
        color: #fff;
        font-size: 16px;
        font-weight: 500;
        white-space: nowrap;
      }
    }
    
    .el-menu {
      border: none !important;
      flex: 1;
      overflow-y: auto;
      overflow-x: hidden !important;
      width: 100%;
      
      &::-webkit-scrollbar {
        width: 6px;
      }
      
      &::-webkit-scrollbar-track {
        background: #1f2d3d;
      }
      
      &::-webkit-scrollbar-thumb {
        background: #67748e;
        border-radius: 3px;
      }
      
      ::v-deep .el-menu-item,
      ::v-deep .el-sub-menu__title {
        margin: 0 !important;
        padding-right: 20px !important;
      }
      
      ::v-deep .el-sub-menu {
        .el-menu {
          border: none !important;
          background-color: #1f2d3d !important;
        }
        
        &.is-opened {
          .el-sub-menu__title {
            border-bottom-color: transparent !important;
          }
        }
      }
      
      ::v-deep .el-menu--collapse {
        border: none !important;
        width: 64px !important;
      }
    }
  }
  
  .header {
    background-color: #fff;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    
    .header-left {
      display: flex;
      align-items: center;
      
      .collapse-btn {
        font-size: 20px;
        cursor: pointer;
        margin-right: 20px;
        
        &:hover {
          color: #409EFF;
        }
      }
    }
    
    .header-right {
      .user-info {
        display: flex;
        align-items: center;
        cursor: pointer;
        
        span {
          margin: 0 5px;
        }
        
        &:hover {
          color: #409EFF;
        }
      }
    }
  }
  
  .main-content {
    background-color: #f0f2f5;
    padding: 20px;
    overflow-y: auto;
  }
}
</style>