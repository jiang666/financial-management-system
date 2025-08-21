import { createStore } from 'vuex'
import { login as loginApi, logout as logoutApi, getCurrentUser, getCurrentUserResources } from '@/api/auth'
import { 
  getToken, 
  setToken, 
  removeToken, 
  getRefreshToken, 
  setRefreshToken, 
  removeRefreshToken,
  getUserInfo,
  setUserInfo,
  removeUserInfo,
  getUserResources,
  setUserResources,
  removeUserResources,
  clearAuth
} from '@/utils/token'

const store = createStore({
  state: {
    user: {
      id: '',
      username: '',
      realName: '',
      email: '',
      phone: '',
      department: '',
      position: '',
      status: '',
      createTime: null,
      lastLoginTime: null
    },
    token: getToken() || '',
    refreshToken: getRefreshToken() || '',
    authorities: [],
    resources: getUserResources() || null,
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
      setUserInfo(state.user)
    },
    SET_TOKEN(state, token) {
      state.token = token
      setToken(token)
    },
    SET_REFRESH_TOKEN(state, refreshToken) {
      state.refreshToken = refreshToken
      setRefreshToken(refreshToken)
    },
    SET_AUTHORITIES(state, authorities) {
      state.authorities = authorities
    },
    SET_RESOURCES(state, resources) {
      state.resources = resources
      setUserResources(resources)
    },
    CLEAR_AUTH(state) {
      state.user = {
        id: '',
        username: '',
        realName: '',
        email: '',
        phone: '',
        department: '',
        position: '',
        status: '',
        createTime: null,
        lastLoginTime: null
      }
      state.token = ''
      state.refreshToken = ''
      state.authorities = []
      state.resources = null
      clearAuth()
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
        const response = await loginApi(userInfo)
        const { token, refreshToken, userInfo: user, authorities, resources } = response.data
        
        // 保存Token和用户信息
        commit('SET_TOKEN', token)
        commit('SET_REFRESH_TOKEN', refreshToken)
        commit('SET_USER', user)
        commit('SET_AUTHORITIES', authorities || [])
        commit('SET_RESOURCES', resources || null)
        
        return response
      } catch (error) {
        console.error('Login failed:', error)
        throw error
      }
    },
    
    // 用户登出
    async logout({ commit }) {
      try {
        await logoutApi()
      } catch (error) {
        console.error('Logout error:', error)
      } finally {
        commit('CLEAR_AUTH')
      }
    },
    
    // 获取用户信息
    async getUserInfo({ commit }) {
      try {
        const response = await getCurrentUser()
        const user = response.data
        commit('SET_USER', user)
        return user
      } catch (error) {
        console.error('Get user info failed:', error)
        throw error
      }
    },
    
    // 获取用户权限资源
    async getUserResources({ commit }) {
      try {
        const response = await getCurrentUserResources()
        const resources = response.data
        commit('SET_RESOURCES', resources)
        return resources
      } catch (error) {
        console.error('Get user resources failed:', error)
        throw error
      }
    },
    
    // 初始化用户信息（页面刷新时调用）
    async initUserInfo({ commit, state }) {
      if (!state.token) {
        return false
      }
      
      try {
        // 从localStorage恢复用户信息
        const cachedUserInfo = getUserInfo()
        const cachedResources = getUserResources()
        
        if (cachedUserInfo) {
          commit('SET_USER', cachedUserInfo)
        }
        
        if (cachedResources) {
          commit('SET_RESOURCES', cachedResources)
        }
        
        // 如果没有缓存的用户信息，则重新获取
        if (!cachedUserInfo) {
          await this.dispatch('getUserInfo')
        }
        
        if (!cachedResources) {
          await this.dispatch('getUserResources')
        }
        
        return true
      } catch (error) {
        console.error('Init user info failed:', error)
        commit('CLEAR_AUTH')
        return false
      }
    }
  },
  getters: {
    isLoggedIn: state => !!state.token,
    token: state => state.token,
    user: state => state.user,
    username: state => state.user.username,
    realName: state => state.user.realName,
    authorities: state => state.authorities,
    resources: state => state.resources,
    sidebarCollapsed: state => state.sidebar.collapsed,
    
    // 检查是否有指定权限
    hasAuthority: (state) => (authority) => {
      return state.authorities.includes(authority)
    },
    
    // 检查是否有指定资源权限
    hasResource: (state) => (resourceCode) => {
      if (!state.resources) return false
      
      const checkResource = (resource) => {
        if (resource.resourceCode === resourceCode) return true
        if (resource.children) {
          return resource.children.some(child => checkResource(child))
        }
        return false
      }
      
      return checkResource(state.resources)
    }
  }
})

export default store