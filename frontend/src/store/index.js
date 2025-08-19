import { createStore } from 'vuex'

const store = createStore({
  state: {
    user: {
      username: '',
      realName: '',
      role: '',
      token: ''
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
    login({ commit }, userInfo) {
      // 模拟登录
      return new Promise((resolve) => {
        commit('SET_USER', {
          username: userInfo.username,
          realName: '张三',
          role: 'admin'
        })
        commit('SET_TOKEN', 'mock-token-' + Date.now())
        resolve()
      })
    },
    logout({ commit }) {
      commit('SET_USER', {
        username: '',
        realName: '',
        role: '',
        token: ''
      })
      commit('SET_TOKEN', '')
    }
  },
  getters: {
    isLoggedIn: state => !!state.user.token,
    username: state => state.user.username,
    realName: state => state.user.realName,
    sidebarCollapsed: state => state.sidebar.collapsed
  }
})

export default store