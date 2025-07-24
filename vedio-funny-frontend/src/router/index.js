import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/login-test',
    name: 'LoginTest',
    component: () => import('@/views/LoginTest.vue'),
    hidden: true
  },
  {
    path: '/login-simple',
    name: 'LoginSimple',
    component: () => import('@/views/LoginSimple.vue'),
    hidden: true
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'dashboard', permission: 'dashboard:view' }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    meta: { title: '用户管理', icon: 'User', permission: 'user:menu' },
    children: [
      { path: 'list', name: 'UserList', component: () => import('@/views/user/UserList.vue'), meta: { title: '用户列表' } },
      { path: 'profile', name: 'UserProfile', component: () => import('@/views/user/UserProfile.vue'), meta: { title: '用户资料' } }
    ]
  },
  {
    path: '/model',
    component: Layout,
    meta: { title: '模型管理', icon: 'Cpu', permission: 'model:menu' },
    children: [
      { path: 'list', name: 'ModelList', component: () => import('@/views/model/ModelList.vue'), meta: { title: '模型列表' } },
      { path: 'market', name: 'ModelMarketMenu', component: () => import('@/views/ModelMarket.vue'), meta: { title: '模型市场' } },
      { path: 'add', name: 'ModelAdd', component: () => import('@/views/model/ModelAdd.vue'), meta: { title: '添加模型' }, hidden: true },
      { path: 'edit/:id', name: 'ModelEdit', component: () => import('@/views/model/ModelEdit.vue'), meta: { title: '编辑模型' }, hidden: true }
    ]
  },
  {
    path: '/server',
    component: Layout,
    meta: { title: '服务器管理', icon: 'Monitor', permission: 'server:menu' },
    children: [
      { path: 'list', name: 'ServerList', component: () => import('@/views/server/ServerList.vue'), meta: { title: '服务器列表' } }
    ]
  },
  {
    path: '/system',
    component: Layout,
    meta: { title: '系统管理', icon: 'Setting', permission: 'system:menu' },
    children: [
      { path: 'settings', name: 'SystemSettings', component: () => import('@/views/system/Settings.vue'), meta: { title: '系统设置' } }
    ]
  },
  {
    path: '/role',
    component: Layout,
    meta: { title: '角色管理', icon: 'User', permission: 'role:menu' },
    children: [
      { path: 'list', name: 'RoleList', component: () => import('@/views/role/RoleList.vue'), meta: { title: '角色管理', icon: 'User' } }
    ]
  },
  {
    path: '/permission',
    component: Layout,
    meta: { title: '权限管理', icon: 'Lock', permission: 'permission:menu' },
    children: [
      { path: 'list', name: 'PermissionList', component: () => import('@/views/permission/PermissionList.vue'), meta: { title: '权限管理', icon: 'Lock' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('Admin-Token')
  if (to.path === '/login' || to.path === '/register') {
    next()
  } else {
    if (token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router 