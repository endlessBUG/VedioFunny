const state = {
  sidebar: {
    opened: true
  }
}

const mutations = {
  TOGGLE_SIDEBAR: (state) => {
    state.sidebar.opened = !state.sidebar.opened
  }
}

const actions = {
  toggleSideBar({ commit }) {
    commit('TOGGLE_SIDEBAR')
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
} 