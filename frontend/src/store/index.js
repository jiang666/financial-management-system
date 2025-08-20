import { createStore } from 'vuex'
import { authAPI } from '@/api'

const store = createStore({
  state: {
    user: {
      id: '',
      username: '',
      realName: '',
      email: '',
      roles: [],
      permissions: [],
      token: localStorage.getItem('token') || ''
    },
    sidebar: {
      collapsed: false
    },
    settings: {
      accountingPeriod: '',
      companyName: '示例公司',
      fiscalYear: 2025
    }
  },
  mutations: {
    SET_USER(state, user) {
      state.user = { ...state.user, ...user }
    },
    SET_TOKEN(state, token) {
      state.user.token = token
      if (token) {
        localStorage.setItem('token', token)
      } else {
        localStorage.removeItem('token')
      }
    },
    CLEAR_USER(state) {
      state.user = {
        id: '',
        username: '',
        realName: '',
        email: '',
        roles: [],
        permissions: [],
        token: ''
      }
      localStorage.removeItem('token')
    },
    TOGGLE_SIDEBAR(state) {
      state.sidebar.collapsed = !state.sidebar.collapsed
    },
    SET_SIDEBAR_STATE(state, collapsed) {
      state.sidebar.collapsed = collapsed
    },
    SET_SETTINGS(state, settings) {
      state.settings = { ...state.settings, ...settings }
    }
  },
  actions: {
    // 用户登录
    async login({ commit }, userInfo) {
      try {
        const response = await authAPI.login({
          username: userInfo.username,
          password: userInfo.password
        })
        
        const { accessToken, userInfo: user, permissions } = response
        
        // 保存token
        commit('SET_TOKEN', accessToken)
        
        // 保存用户信息
        commit('SET_USER', {
          id: user.id,
          username: user.username,
          realName: user.realName,
          email: user.email,
          roles: user.roles || [],
          permissions: permissions || []
        })
        
        return response
      } catch (error) {
        console.error('Login failed:', error)
        throw error
      }
    },
    
    // 获取用户信息
    async getCurrentUser({ commit, state }) {
      if (!state.user.token) {
        throw new Error('No token found')
      }
      
      try {
        const userInfo = await authAPI.getCurrentUser()
        
        commit('SET_USER', {
          id: userInfo.id,
          username: userInfo.username,
          realName: userInfo.realName,
          email: userInfo.email,
          roles: userInfo.roles || []
        })
        
        return userInfo
      } catch (error) {
        console.error('Get user info failed:', error)
        commit('CLEAR_USER')
        throw error
      }
    },
    
    // 用户登出
    async logout({ commit }) {
      try {
        await authAPI.logout()
      } catch (error) {
        console.error('Logout API failed:', error)
      } finally {
        commit('CLEAR_USER')
      }
    }
  },
  getters: {
    isLoggedIn: state => !!state.user.token,
    token: state => state.user.token,
    username: state => state.user.username,
    realName: state => state.user.realName,
    userInfo: state => state.user,
    userRoles: state => state.user.roles,
    userPermissions: state => state.user.permissions,
    sidebarCollapsed: state => state.sidebar.collapsed,
    
    // 权限检查
    hasPermission: state => permission => {
      return state.user.permissions.includes(permission)
    },
    
    hasRole: state => role => {
      return state.user.roles.some(r => r.code === role)
    }
  }
})

export default store