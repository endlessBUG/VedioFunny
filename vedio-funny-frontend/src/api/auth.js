import request from '@/utils/request'

/**
 * 认证相关API
 */

// 用户注册
export function register(data) {
  return request({
    url: '/api/auth/auth/register',
    method: 'post',
    data: data
  })
}

// 用户登录
export function login(data) {
  return request({
    url: '/api/auth/auth/login',
    method: 'post',
    data: data
  })
}

// 用户登出
export function logout() {
  return request({
    url: '/api/auth/auth/logout',
    method: 'post'
  })
}

// 获取用户信息
export function getInfo() {
  return request({
    url: '/api/auth/auth/userinfo',
    method: 'get'
  })
}

// 刷新令牌
export function refreshToken(refreshToken) {
  return request({
    url: '/api/auth/auth/refresh',
    method: 'post',
    data: { refreshToken }
  })
}

// 验证令牌
export function validateToken() {
  return request({
    url: '/api/auth/auth/validate',
    method: 'post'
  })
}

// 检查用户名是否存在
export function checkUsername(username) {
  return request({
    url: '/api/auth/auth/check-username',
    method: 'get',
    params: { username }
  })
} 