import { createStore } from 'vuex'
import user from './modules/user'
import app from './modules/app'

const store = createStore({
  modules: {
    user,
    app
  },
  state: {
    sidebar: {
      opened: true
    }
  },
  mutations: {
    TOGGLE_SIDEBAR(state) {
      state.sidebar.opened = !state.sidebar.opened
    }
  },
  getters: {
    // 用户相关getters
    token: state => state.user.token,
    userInfo: state => state.user.userInfo,
    isLoggedIn: state => !!state.user.token,
    
    // 布局相关getters
    sidebarOpened: state => state.sidebar.opened
  }
})

export default store 