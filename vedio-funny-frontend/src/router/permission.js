import router from './index'
import store from '@/store'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register', '/404'] // 白名单路由

router.beforeEach(async (to, from, next) => {
  // 开始进度条
  NProgress.start()

  // 设置页面标题
  document.title = `${to.meta.title || '页面'} - VedioFun管理系统`

  // 获取token
  const hasToken = getToken()

  if (hasToken) {
    if (to.path === '/login') {
      // 如果已登录，重定向到首页
      next({ path: '/' })
      NProgress.done()
    } else {
      next()
    }
  } else {
    // 没有token
    if (whiteList.indexOf(to.path) !== -1) {
      // 在白名单中，直接放行
      next()
    } else {
      // 其他页面需要登录
      next(`/login?redirect=${to.path}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  // 结束进度条
  NProgress.done()
}) 