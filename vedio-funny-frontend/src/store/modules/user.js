import { login, logout, getInfo } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

const state = {
  token: getToken(),
  userInfo: (() => {
    const info = localStorage.getItem('userInfo')
    return info ? JSON.parse(info) : null
  })(),
  roles: (() => {
    const info = localStorage.getItem('userInfo')
    if (info) {
      const userInfo = JSON.parse(info)
      return userInfo.roles || (userInfo.userType === 'ADMIN' ? ['ROLE_ADMIN'] : ['ROLE_GUEST'])
    }
    return []
  })(),
  permissions: (() => {
    const info = localStorage.getItem('userInfo')
    if (info) {
      const userInfo = JSON.parse(info)
      return userInfo.permissions || []
    }
    return []
  })()
}

const mutations = {
  SET_TOKEN: (state, token) => {
    state.token = token
  },
  SET_USER_INFO: (state, userInfo) => {
    state.userInfo = userInfo
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
    // 使用后端返回的角色和权限，如果没有则使用默认值
    state.roles = userInfo.roles || (userInfo.userType === 'ADMIN' ? ['ROLE_ADMIN'] : ['ROLE_GUEST'])
    state.permissions = userInfo.permissions || []
    
    // 打印用户权限信息
    console.log('=== 用户权限信息 ===')
    console.log('用户信息:', userInfo)
    console.log('角色列表:', state.roles)
    console.log('权限列表:', state.permissions)
    console.log('==================')
  },
  RESET_STATE: (state) => {
    state.token = ''
    state.userInfo = null
    localStorage.removeItem('userInfo')
    state.roles = []
    state.permissions = []
  },
  INIT_USER_FROM_STORAGE: (state) => {
    const info = localStorage.getItem('userInfo')
    if (info) {
      const userInfo = JSON.parse(info)
      state.userInfo = userInfo
      state.roles = userInfo.roles || (userInfo.userType === 'ADMIN' ? ['ROLE_ADMIN'] : ['ROLE_GUEST'])
      state.permissions = userInfo.permissions || []
      
      console.log('=== 从localStorage初始化用户权限 ===')
      console.log('用户信息:', userInfo)
      console.log('角色列表:', state.roles)
      console.log('权限列表:', state.permissions)
      console.log('===================================')
    }
  }
}

const actions = {
  // 用户登录
  login({ commit }, userInfo) {
    const { username, password, remember } = userInfo
    return new Promise((resolve, reject) => {
      login({ username: username.trim(), password: password, remember: remember })
        .then(response => {
          const { data } = response
          if (data && data.accessToken) {
            // 设置token
            commit('SET_TOKEN', data.accessToken)
            setToken(data.accessToken)
            
            // 设置用户信息
            if (data.userInfo) {
              commit('SET_USER_INFO', data.userInfo)
            }
            
            resolve(data)
          } else {
            reject(new Error('登录失败，未获取到有效令牌'))
          }
        })
        .catch(error => {
          // 处理错误信息，提取后端返回的具体错误
          let errorMessage = '登录失败'
          
          if (error.response && error.response.data) {
            // 后端返回的错误信息
            const responseData = error.response.data
            errorMessage = responseData.message || '登录失败'
            
            // 如果是运行时错误，提取具体的错误信息
            if (errorMessage.includes('运行时错误:')) {
              errorMessage = errorMessage.replace('运行时错误:', '').trim()
            }
          } else if (error.message) {
            errorMessage = error.message
          }
          
          reject(new Error(errorMessage))
        })
    })
  },

  // 初始化用户信息（从localStorage）
  initUserFromStorage({ commit }) {
    commit('INIT_USER_FROM_STORAGE')
  },

  // 获取用户信息
  getInfo({ commit, state }) {
    return new Promise((resolve, reject) => {
      if (!state.token) {
        reject(new Error('获取用户信息失败，请重新登录'))
        return
      }

      getInfo()
        .then(response => {
          const { data } = response
          
          if (!data) {
            reject(new Error('验证失败，请重新登录'))
            return
          }

          commit('SET_USER_INFO', data)
          resolve(data)
        })
        .catch(error => {
          reject(error)
        })
    })
  },

  // 用户登出
  logout({ commit, dispatch }) {
    return new Promise((resolve, reject) => {
      logout()
        .then(() => {
          commit('RESET_STATE')
          removeToken()
          resolve()
        })
        .catch(error => {
          // 即使登出接口失败，也要清除本地token
          commit('RESET_STATE')
          removeToken()
          reject(error)
        })
    })
  },

  // 重置token
  resetToken({ commit }) {
    return new Promise(resolve => {
      commit('RESET_STATE')
      removeToken()
      resolve()
    })
  }
}

const getters = {
  token: state => state.token,
  userInfo: state => state.userInfo,
  userId: state => state.userInfo?.userId,
  username: state => state.userInfo?.username,
  nickname: state => state.userInfo?.nickname,
  avatar: state => state.userInfo?.avatar,
  email: state => state.userInfo?.email,
  userType: state => state.userInfo?.userType,
  roles: state => state.roles,
  permissions: state => state.permissions
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
} 