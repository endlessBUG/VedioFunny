import axios from 'axios'
import store from '@/store'
import router from '@/router'

// 创建axios实例
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API || 'http://localhost:8082', // API网关地址
  timeout: 15000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 在请求发送前做些什么
    if (store.getters.token) {
      // 让每个请求携带自定义token 请根据实际情况自行修改
      config.headers['Authorization'] = 'Bearer ' + store.getters.token
    }
    return config
  },
  error => {
    // 对请求错误做些什么
    console.log(error) // for debug
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  /**
   * 如果你想获取http信息，例如headers或status
   * Please return  response => response
  */

  /**
   * 通过判断状态码统一处理响应，根据后端API进行修改
   * 也可以通过HTTP状态码判断请求结果
   */
  response => {
    const res = response.data

    // 如果返回的状态码为200，说明接口请求成功，可以正常拿到数据
    // 否则的话抛出错误
    if (res.code && res.code !== 200) {
      // 处理认证相关错误码
      // 50008: 非法的token; 50012: 其他客户端登录了; 50014: Token 过期了;
      // 40101: 缺少认证令牌; 40102: 令牌验证失败; 40103: 认证服务异常
      if (res.code === 50008 || res.code === 50012 || res.code === 50014 || 
          res.code === 40101 || res.code === 40102) {
        // 这些是token相关的错误，需要重新登录
        store.dispatch('user/resetToken').then(() => {
          router.push('/login')
        })
      }
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      return res
    }
  },
  error => {
    console.log('err' + error) // for debug
    
    // 处理401未授权错误
    if (error.response && error.response.status === 401) {
      // 清除token并跳转到登录页
      store.dispatch('user/resetToken').then(() => {
        router.push('/login')
      })
      return Promise.reject(error)
    }
    
    // 其他错误直接抛出，让页面组件自己处理
    return Promise.reject(error)
  }
)

export default service 